# Theta.Navi.Hatch

## Theta Hatch シリーズ共通（必ず参照）

このアプリは Theta Hatch シリーズの 1 つです。シリーズ共通の仕様・デザイン・鍵の決まりは
GitHub の kiwiman0706-beep/Theta.Hatch.Series にあり、このリポジトリの docs より優先します。

- 作業を始める前に、Theta.Hatch.Series を読める状態にする
  （クラウドのスレッドでは add_repo で kiwiman0706-beep/Theta.Hatch.Series を追加して clone する）
- 読むもの：
  - `spec/THETA_HATCH_SPEC.md`（共通仕様）、`spec/THETA_VAULT_SPEC.md`、`spec/THETA_SHARED_SETTINGS.md`、`spec/THETA_GOOGLE_SETUP.md`
  - `design/README.md`（スキン：system・dark・light・pop・alert・scifi。色は `design/skins/skins.json`）
  - `spec/THETA_HATCH_SPEC.md` §6.1（アプリ内自動更新は必須）、`docs/MIGRATION.md`・`catalog/apps.json`（このアプリの目標の ID・リポジトリ名）
  - `secrets/README.md`（署名鍵はシリーズ共通。Secret の名前は ANDROID_KEYSTORE_B64・ANDROID_KEYSTORE_PASSWORD・ANDROID_KEY_ALIAS・ANDROID_KEY_PASSWORD）
- 共通仕様の写しをこのリポジトリに増やさない（変えるときは Theta.Hatch.Series 側を直す）
- 鍵・パスワード・API キーをリポジトリに入れない
- 移行や大きな変更の方針と進み具合は、Theta.Hatch.Series の `progress/<id>.md` に書く（書き方は `progress/_TEMPLATE.md`。自分のアプリのファイルだけを触る）
- 作品の版や概要が変わったら、Theta.Hatch.Series の README の一覧も直す

## このリポジトリ

- アプリ ID `app.thetahatch.navi`（0.3.0〜）。0.2.1 までは `com.theta.navihatch`＋試作鍵（Git には入れていない）
- 版は `app/build.gradle` の versionName。上げて main に push すると `release.yml` が `v<版>` の Release を作る。CHANGELOG.md に同じ版の節を書く
- 引き継ぎ ZIP（handoff-r2）の `research/binaries`・`research/upstream`（第三者の APK・ソース ZIP）は Git に入れていない。要るときは利用者の手元の ZIP から
- Theta.Home.Hatch への内蔵／連動の構想は `docs/HOME_LINK_JA.md`

## 0.2.1 の引き継ぎ（元の文）

README_JA.md、VALIDATION.md、docs/API_SETUP_JA.md、docs/TEST_MATRIX_JA.md、docs/ROADMAP_JA.md、docs/VEHICLE_LOCATION_PROBE_JA.mdを最初に読む。
ユーザーはCX-8の非タッチ車載画面とジョグダイヤルを地図に特化し、タブレット側を操作パネル・マルチタッチ地図として使いたい。
今回からGoogle Navigation SDKの実地図を使用する。架空地図へ戻して「実地図で確認できた」と言わない。
CarPlayはプランB。最新条件: CX-8は有線CarPlayのみ、ALLDOCUBE iPlay60 Pro（Pad Proではない）・SIM内蔵。小型のCarPlay無線化アダプター追加はユーザー許容済み。00_START_HERE_JA.mdとdocs/CARPLAY_PLAN_B_JA.mdも読む。既存ホームへの統合は別工程。

## 0.2の実装

- 車: NaviScreen → VirtualDisplay + Presentation + NavigationViewForAuto。
- タブレット: MainActivity → NavigationView。通常のマルチタッチが有効。
- CameraLinkは表示位置のコピー・双方向同期だけを行う。Viewの移動・共有をしない。
- NavigatorはApplicationに保持。タブレットを閉じる/onStop/onDestroyでcleanupやstopGuidanceを呼ばない。
- 地図キーはNavigationApi.setApiKeyでプロセス内1回。保存はAndroid Keystore暗号化。
- GeminiはGoogle Maps Grounding。候補はgroundingChunks.mapsから抽出。本文の推測座標は不採用。
- Places API（New）でPlace IDを位置へ解決。キーが使えなければ出典リンクのみ利用可。
- 走行案内、音声、目的地経路はまだ未実装。検索候補選択で勝手に走行案内を開始しない。

## ボタン制限の確認済み事項

NavigationTemplateは通常ActionStripが必須で、ActionStrip.Builderは空を拒否する（Car App Library1.7.0のソースと単体テストで確認）。
その必須操作を＋にする。map_only: 通常＋、地図PAN/−。minimal: 通常＋、地図PAN。full: 通常＋、地図PAN/−/現在地。
したがって全アプリ操作をパン・ズームだけにすることはコード上で可能。ただしAA共通UI、Googleロゴ、ホストのフォーカス制御は別。
Googleの一部サンプルの空ActionStripをそのままコピーすると実際のライブラリで例外になるため注意。

## 遠隔テスト

- adb serialを必ず明示。ユーザーPCのTailscale/ADB認証を確認する。
- APIキーは端末設定画面で入力。チャット/シェル引数/ログへ出さない。設定ダイアログはFLAG_SECURE。
- 手元の作業フォルダの APIKEY.txt（Git 対象外）からADBで入れてよい。手順は docs/PROMPT_LOCAL_P0_JA.md の 3。中身は表示しない。
- test modeをenableし、status→smoke→capture。smokeはUI/制御だけで、実地図表示の証明ではない。
- 実地図が車・タブレット双方で表示されたらdualcheckとTEST_MATRIXを実施。
- map_loadedはSDKコールバックの到達であり、実車画面のピクセルやGoogle API課金設定の全面的な正常性を証明しない。
- 地図を閉じた後もcar_generationが変わらないか、car_map_ready/loadedとSurfaceが残るか確認し、ユーザーの車載写真・動画と照合。
- 車のダイヤル入力はユーザーが停車中に行う。ADB inputを物理ダイヤルの証拠にしない。
- キーなし・API拒否時にテストをPASS扱いしない。空の地図を合成画像で埋めない。
- Play内部共有で署名が変わったらキーのSHA-1設定と更新導入経路を確認。無断アンインストールしない。

## ビルド・更新

JDK17 / Gradle8.13 / AGP8.13.2 / compile,target36 / min26 / NavSDK7.9.0 / CAL1.7.0。
`./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`
更新はversionCodeとversionNameを上げる。署名はシリーズ共通鍵（0.3.0〜）。公開製品にはAPIキー配信・バックエンド等を検討。
キーを再設定した場合、SDKのプロセス再起動が必要。車載接続中に自動でforce-stopしない。

## 0.2.1 車両位置診断

VehicleLocationProbeがCarSensorsと端末LocationManagerを購読。TESTモード内で明示ON、切断/停止時に解除し、generationで遅延コールバックを棄却。Car API 3未満を検出して回避する。地図SDKのキー/初期化は不要。
状態JSON schema 3のvehicle_location_probe、vehicle-location.jsonlを回収する。vehiclecheckは最大約35秒の観測後に購読停止。取得成功と物理GPS由来・精度改善を区別し、Google SDKへ注入しない。模擬AA接続は本物のCarContextを作らない。
ROADMAPは将来設計。P0実車検証→P1セッション→P2走行/音声案内の順に進め、まだ存在しない機能を実装済みとして扱わない。
