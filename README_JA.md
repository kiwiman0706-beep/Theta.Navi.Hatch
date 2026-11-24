# Theta.Navi.Hatch 0.2.1-vehicle-probe

**引き継ぎ資料r2:** `00_START_HERE_JA.md`を最初に読む。CarDeck・無線化アダプター・iPlay60 Pro/SIM対応の研究資料を追加。Theta APKは変更なし。

**0.2.1追加:** 車両位置診断と、走行・音声案内を含む完成形のロードマップ。
- [完成形・機能別実装方法・開発順序](docs/ROADMAP_JA.md)
- [車両位置診断の操作・判定・ADB手順](docs/VEHICLE_LOCATION_PROBE_JA.md)

診断はTEST LABでテストモードON→「車両位置情報の診断 ON/OFF」。地図APIキーなしでも、Android Autoの車載画面と正確な位置情報の許可があれば試せます。返された位置が車の物理GPS由来か、測位が改善するかは別途判定します。CarPlay直接診断や走行・音声案内はまだ実装していません。


2026-11-24。Google Navigation SDKの実地図と、GeminiのGoogle Maps検索を組み込んだ実機検証用APKです。
0.1の架空地図を置き換えました。APIキー未設定・認証失敗時に架空地図へ切り替わることはありません。

## まず使う

1. 同梱APKをタブレットへインストール。0.1と同じ署名・パッケージで更新できます。
2. アプリの「API設定」でNavigation SDKキーを入力・保存。
3. 「地図を開始」→位置情報を許可→Googleの利用規約を本人が確認。
4. Googleの地図が表示されるか確認。初期の表示中心は大阪・なんば付近です。GPS現在地ではありません。「現在地」で移動します。
5. Gemini APIキーも設定すると、下の検索欄でGoogle Maps Groundingによる検索を試せます。
6. Android Autoで本アプリを起動し、二画面のテストへ進みます。

このAPKには利用可能なAPIキーを同梱していません。利用者のキーとサービス側設定が必要です。
キーはチャット・ログへ貼らず、端末のAPI設定画面で入力してください。

## 必要なキー・サービス

| 機能 | 必要な設定 |
|---|---|
| Googleの地図・二画面表示 | Google Cloudの課金設定、Navigation SDK for Androidの有効化、APIキー |
| Geminiで場所を検索 | Gemini APIキー、Google Maps Grounding対応モデルの利用権限・利用枠／課金 |
| 検索候補をこのアプリの地図にピン表示 | Places API（New）の有効化。Placesキー欄は任意で、未設定ならNavigationキーを使用 |
| 検索候補の出典を見る | Geminiの結果に付いたGoogle Mapsリンク。Placesキーは不要 |

GeminiキーだけでNavigation SDKの認証を代用することはできません。
モデルの初期値は `gemini-2.5-flash`。API設定でモデル名を変更できます。対応・利用可能性は利用するアカウントで確認してください。
API利用料が発生する場合があります。APIキーを入れるだけで全機能が無料になる構成ではありません。

詳細は `docs/API_SETUP_JA.md`。

## 二画面の操作

- **車と同じ場所をタブレットで開く**: 車側の緯度・経度・倍率・向き・傾きをタブレット側へコピー。タブレットは別のNavigationViewなので、車載Viewを移し替えません。
- **独立**: タブレットのマルチタッチで地図を動かしても車側の位置は変わりません。初期設定です。
- **同期**: 片方の表示位置・倍率・向き・傾きの変更をもう片方へ反映。
- **タブレットの表示位置を車へ送る**: 独立モードでも、選んだ表示位置だけ車へ送れます。
- **タブレットの地図を閉じる**: タブレットのViewだけを破棄。車側のNavigationViewForAutoや共有Navigatorは終了させません。
- **地図を広く**: タブレットの右パネルを一時的に隠して地図を拡大。上の同じボタンで戻せます。

画面の縦横比が違うため、同期しても地図の四隅の表示範囲は一致しません。中心・倍率・向き・傾きを揃える仕様です。
タブレットを閉じても車の地図が本当に残るか、車載Surfaceが維持されるかは実車の写真・動画とログの両方で検証します。

## 車載側のボタン

切替候補:
- `map_only`（初期）: PAN・＋・−
- `minimal`: PAN・＋
- `full`: PAN・＋・−・現在地

Car App Library 1.7.0のNavigationTemplateは通常ActionStripに最低1操作を要求するため、その枠を＋に使用します。地図用ActionStripはPANと、必要時のみ−・現在地です。SDKのコンパス、ズームボタン、現在地ボタン、マップツールバー、屋内階層ボタンを無効化。
NavigationViewForAutoは車載向けのSDK表示を使用します。タブレットのNavigationViewもナビ用ヘッダー・ETA等を無効化しています。

Android Auto共通のアプリ一覧・システムバー・分割画面はホスト側の管理で、ここから消しません。Googleロゴ・著作権表示も隠しません。
PANボタンは地図操作コールバックを受けるため残します。ダイヤル回転が必ずズームになる、起動時に必ず地図へフォーカスが入る、といった保証はありません。

## Gemini検索

Googleの `googleMaps` ツールを有効にして検索。候補は応答の `groundingMetadata.groundingChunks[].maps` から作ります。
本文にAIが書いた施設・座標を勝手にピンへ変換しません。出典候補がない場合はその旨を表示。
Google Mapsの出典リンクを回答の直後に表示します。

検索ボタンを押した時だけ入力文をGoogleへ送信します。「地図中心を検索に使う」がONなら、画面に表示した中心の緯度・経度も送ります。GPS追跡結果を継続送信する機能ではありません。
「このアプリの地図に表示」は、出典のPlace IDをPlaces API（New）に渡して位置を取得し、両方の地図へピンを追加します。
選択は地図の表示位置を変えるだけで、自動で走行案内や音声案内を開始しません。

## 今回の範囲

実地図、SDK初期化、二画面、マルチタッチ、車載ボタン比較、渋滞レイヤー、Gemini検索、Place IDからの位置取得、遠隔診断を実装。
**目的地までの走行案内・音声案内・経路の再検索・OBD2補正は未実装**です。Navigation SDKは組み込んでいますが、`Navigator.startGuidance()` は呼びません。
既存ホームアプリへの統合、CarPlay、別スマホ着信連携もこの版の範囲外。

## 実車Android Autoへ導入する際の注意

ADBでタブレットに入ることと、車載Android Autoの一覧に出ることは別です。
Car App Library製アプリには「提供元不明のアプリ」設定の例外が適用されないため、公式の実車テスト導入経路はPlay内部アプリ共有／内部テストです。
内部共有等で再署名されると、キーに登録するSHA-1も変わります。更新インストールが署名違いで拒否された場合、無断でアンインストールせずログを先に回収してください。
https://developer.android.com/training/cars/testing

## Claude Code / 遠隔テスト

ZIPを展開し、`CLAUDE.md` を読ませてください。

```sh
python tools/theta_adb.py --serial SERIAL install Theta.Navi.Hatch-0.2.1-vehicle-probe.apk
python tools/theta_adb.py --serial SERIAL cmd enable
python tools/theta_adb.py --serial SERIAL status
python tools/theta_adb.py --serial SERIAL smoke
python tools/theta_adb.py --serial SERIAL capture --out captures/baseline
```

SERIALは実際のADB接続先。Tailscale設定・ADB認証はAPK外で実施します。`docs/REMOTE_ADB_JA.md`を参照。
`smoke`はAPI課金のある検索を実行しません。実地図描画・物理ダイヤル・マルチタッチの視覚的な証明でもありません。

## ビルド

JDK17、Android SDK36、Gradle8.13、AGP8.13.2。Navigation SDK7.9.0、Car App Library1.7.0。
SDK7.9.0を固定し、実行時のキー設定APIとAndroid Auto用Viewを利用。

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`。
署名は0.1と同じ試作専用 `test-signing.jks`。alias `theta-test`、store/key password `theta-test-only`。本番署名に流用しないこと。
Android 8.0(API26)以上、Google Play services、SDK対応のGPU/メモリが必要です。Android Auto自体の対応条件は別途確認。

## 診断・秘密情報

状態JSONはAPIキーを含みません。APIキーはAndroid KeystoreのAES/GCMで暗号化して端末内に保存。バックアップ・端末移行から除外。
設定ダイアログはスクリーンショットを禁止し、キー入力中のタブレットキーイベントも記録しません。
APIキーを変更した場合はSDKのプロセス再起動が必要です。車載接続中に自動再起動はしません。
この試作はデバッグ可能なAPKです。端末を管理するADB利用者から秘密を完全に隔離する設計ではなく、本番公開するGemini機能にはバックエンド等の設計が必要です。

通常の診断JSONには地図中心の座標が含まれ、スクリーンショットには検索結果が含まれる場合があります。共有先を確認してください。
`car-map-only-NOT-host-UI.png` はSDK地図のスナップショットで、AA共通UIやフォーカスは写りません。`tablet-screen.png`はタブレット画面です。

この環境で確認できた内容と未検証事項は `VALIDATION.md`。
