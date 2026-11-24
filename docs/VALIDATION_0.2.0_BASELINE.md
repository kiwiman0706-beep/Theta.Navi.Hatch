# 検証記録 — 2026-11-24（日本時間）

成果物: Theta.Navi.Hatch 0.2.0-realmap-test / versionCode 2
パッケージ: com.theta.navihatch
minSdk 26 / targetSdk 36 / compileSdk 36

## この環境で確認したこと

- JDK17 / Gradle8.13 / AGP8.13.2で assembleDebug / testDebugUnitTest / lintDebug 成功。
- Navigation SDK 7.9.0の実ライブラリをAPKへ組み込み。実行時キー設定、NavigationView / NavigationViewForAutoの参照をコンパイル確認。
- JUnit + Robolectric（API33 / native graphics）で16テスト合格、失敗0、スキップ0。
  - CameraLink 6件: 座標・倍率の検証、独立/同期、二方向の反映ループ抑制、片側detach、同じ場所を開く操作。
  - GeminiSearch 5件: Google Mapsツールと位置送信ON/OFF、出典候補の解析・重複排除・URL制限、モデル本文の座標をピンにしない動作。
  - AppTest 5件: 車載テンプレート3構成の操作数、タブレット開閉時の状態維持、未設定キーで成功を偽装しない動作、テストモードのコマンド拒否/許可、設定前の画面描画。
- lint: 0 errors / 7 warnings。警告は同期SharedPreferences保存1、テスト依存更新候補1、Car App Library許可ホストのprivate resource参照1、公開CarAppService1、日本語文字列3。詳細は添付レポート。
- APK署名検証成功（v2）。0.1と同じ試作証明書を使用。
- zipalign: 4-byte / 16-KiB page alignment検査成功。これは各端末のGPU・ネイティブライブラリ動作保証ではない。
- Python遠隔ADB補助ツールの構文とCLIヘルプを確認。
- タブレット画面のRobolectric描画を目視確認。キー未設定の案内と検索欄が表示される。

## 証拠ファイル

- validation/TEST-*.xml: 16テストの結果。
- validation/build.log: 最終ビルド・テスト・Lintの結果。
- validation/lint-results-debug.txt: 警告詳細。
- validation/signature.txt / badging.txt / zipalign.txt: 配布APKの検証。
- validation/APK_SHA256.txt: 配布APKのSHA-256。
- validation/tablet-setup-ROBOLECTRIC.png: キー未設定時のView描画。実タブレット・実地図・実車のスクリーンショットではない。

## まだ確認できていないこと

この環境には利用者のAPIキー、車載タブレット、Mazda Connectの実機接続がありません。
**Google認証後の実地図描画、Geminiへの実検索、Places APIへの実照会、実車での同時表示は未検証です。**
APIキーの設定画面やHTTP処理を実装したことと、有効キーでサービスが応答したことは別です。

- APIキーの有効化・課金・Android署名制限を含むSDK認証と実タイルの表示。
- Geminiモデルの利用権限・課金・Maps Groundingの応答、Place IDと位置解決。
- Android実機へのインストール・起動・メモリ/GPU/Play servicesの適合性。
- NavigationViewとNavigationViewForAutoの同時描画、タブレット地図を開閉しても車載画面が消えないこと。
- マルチタッチ操作、同期時の滑らかさ、画面回転、バックグラウンドと復帰。
- 車載ホストが実際に表示するPAN/＋/−、システムバー・分割画面・フォーカス。
- CX-8の物理ダイヤル回転・押下・上下左右とSurfaceCallbackの対応。
- Tailscale越しのADB導入、smoke/dualcheck/captureの実行。
- Play内部アプリ共有/内部テストからのAndroid Auto実車起動。

CameraLinkのテストはViewではなく座標の受け渡しロジックの検証です。
AppTestの開閉テストもSDK未初期化の状態遷移であり、実SDK描画の継続を証明しません。
ReceiverテストはRobolectric内で同じReceiverを動的登録して実行し、端末のDUMP権限検証そのものは行っていません。
SurfaceCallbackをテストから呼ぶことは物理ダイヤル入力の証拠ではありません。

実機では docs/TEST_MATRIX_JA.md のR01〜R23を使用してください。
`dualcheck`のSTATE_PASSはSDK状態・View世代・カメラ一致の判定です。画面が真っ黒になっていないことは実車の写真/動画でも確認します。
`car-map-only-NOT-host-UI.png`はSDK地図だけのsnapshotであり、Android Autoのボタン・フォーカス・システムUIは含みません。

音声案内・目的地への走行案内はこの版に未実装です。
