# 検証記録 — 2026-11-24（日本時間）

成果物: Theta.Navi.Hatch 0.2.1-vehicle-probe / versionCode 3
パッケージ: com.theta.navihatch。minSdk26 / compileSdk・targetSdk36。
NavSDK7.9.0 / Car App Library1.7.0 / JDK17 / Gradle8.13 / AGP8.13.2。

## 今回の変更

車両位置をAndroid Auto Car Hardware APIから購読し、端末LocationManager GPSとは別系列で記録する診断を追加。APIキー・Navigation SDK初期化なしでも開始できる設計。テストモードで明示ON、停止・切断で購読解除。Car App API不足・権限不足・未実装・一時取得不能・無応答・古い値を区別する。

取得値のナビ測位への注入、CarPlayからの直接取得、走行・音声案内は追加していない。将来の機能と実装方法はdocs/ROADMAP_JA.mdに記載。

## 確認したこと

- assembleDebug / testDebugUnitTest / lintDebug 成功。
- JUnit/Robolectric API33で25テスト合格、失敗0・スキップ0。
- 既存16件: 地図カメラ状態、車載テンプレート、検索解析、キーなし状態、画面描画等。
- 新規9件: 位置の受信メタデータ、物理的由来を確定扱いしないこと、失敗時の古い位置解除、成功だがnullと未実装の区別、古い/不明/未来の時刻、数値でない精度をnull扱い、テストモード必須、未接続時の待機、旧Car APIの回避と切断。
- lint: 0 errors / 7 warnings。既存のSharedPreferences同期保存、依存更新候補、許可ホストprivate resource、公開Service、日本語文字列。詳細はvalidation/lint-results-debug.txt。
- apksigner verify成功、0.2.0と同じ試作証明書。
- zipalignの4-byte / 16-KiB page alignment検査成功。
- Python ADB補助ツールの構文・CLIヘルプ確認。
- Robolectric描画で0.2.1表記・車両位置診断ボタン・未開始状態を目視確認。

## 未検証と限界

車載タブレット・CX-8・実Android Auto・有効なGoogle/Geminiキーはこの環境にない。
そのため、実車からの位置コールバック、端末GPSの実測、CarPlay/AI Box、NavSDK実地図、Gemini/Placesの実通信、ジョグダイヤル、二画面描画の継続、遠隔ADBの実機試験は未確認。

LocationProbeTestはRobolectricと人工のLocationを使った状態・境界条件の検証。Car API 3以上の実ホストから位置が届いた証拠ではなく、現物での購読解除・再接続の確認も別途必要。
provider名やCarHardwareLocationの値だけで車の物理GPSアンテナ、車速/IMU融合、位置精度改善を断定しない。比較距離も正解位置との差ではない。

旧版からの16件のテストも実SDK地図の描画・物理入力の証拠ではない。詳細な前回記録はdocs/VALIDATION_0.2.0_BASELINE.mdへ保存。

## 証拠と実機手順

validation/TEST-*.xml、build.log、lint-results-debug.txt、signature.txt、badging.txt、zipalign.txt、APK_SHA256.txt。
validation/tablet-setup-ROBOLECTRIC.pngはAndroid Viewのホスト上描画であり、実タブレットのスクリーンショットではない。

実機ではdocs/TEST_MATRIX_JA.mdに加え、docs/VEHICLE_LOCATION_PROBE_JA.mdのV01〜V10を使用。
`vehiclecheck`はOBSERVATION_ONLYを返す。`dualcheck`は状態上の検査で、実車画面が維持される証明には写真/動画が必要。

## 引き継ぎ資料r2（同日追記）
CarPlay送信実装の研究資料、公開APK/ソース、iPlay60 Pro/SIM/無線化アダプター、Claude作業手順を追加。
Thetaのアプリソース・APK・署名鍵は元ZIPから変更なし。既存25テストは0.2.1時点の結果を保存したもので、今回再ビルド・実車試験を実施したという意味ではない。
元ファイル61件の欠落なし。新規Pythonの構文/CLIヘルプ、JSON、上流ソースZIPのCRCを確認。公開APKの取得ハッシュはresearch/CONTENTS_AND_CHECKS.jsonを参照。Shizukuは配布側の独立ハッシュ未取得、第三者APK署名検査/端末実行は未実施。
最終ZIPのCRCとMANIFEST_SHA256は梱包時に検査。tools/verify_bundle.pyで展開後に再検査可能。

## 0.3.0（2026-11-24、シリーズに合わせた版）
- アプリ ID `app.thetahatch.navi`・versionCode 4。中身の機能は 0.2.1 と同じ。「更新を確認」（ハブへ UPDATE）を追加
- クラウドの Linux（JDK21・Android SDK 36）で `assembleDebug assembleRelease testDebugUnitTest lintDebug` 成功。25 テスト合格（失敗 0）。release は鍵なしのため未署名
- 共通鍵での署名・Release 作成は GitHub Actions（release.yml）でまだ一度も通していない
- 実機・実車・有効なキーでの確認は 0.2.1 と同じく未確認
