# Theta.Navi.Hatch — 完成形と実装ロードマップ

更新: 2026-11-24（日本時間）／対象: CX-8・非タッチMazda Connect・車載Androidタブレット

この文書は将来の設計・開発順序です。記載した機能がすべて同梱APKで動くという意味ではありません。候補機能は優先順位と実機結果で採否を決めます。工程番号は提案であり、納期の約束ではありません。

## 1. 完成形の方針

タブレット1台で検索・目的地設定・案内を実行し、Android Autoへ地図を出す。車側は地図を広く表示し、ジョグダイヤルのパン・ズームをできるだけ少ない手順で使えるようにする。検索、経路選択、詳細設定はタブレット側に置く。必要になった時だけ同じ場所をタブレットの独立した地図Viewで開き、マルチタッチで操作する。

走行案内中は次の曲がり角や残距離も必要になるため、「操作ボタンを減らすこと」と「案内情報をなくすこと」は分ける。車側にPAN・＋・−とコンパクトな案内情報、タブレットに操作パネルを置く設計を第一候補とする。Android Autoのシステムバー、ホストのフォーカス、Googleの帰属表示はアプリの自由配置部分ではない。[S4, S5]

Google Navigation SDKを主経路とする。OpenStreetMap（OSM）版、CarPlay／Ottocast P2 Plus経由の自由UI、別スマホの電話着信連携は、標準案で解決できない点を確認した後の別工程とする。AA基本構成はタブレット中心。最新回答により、Plan Bの小型CarPlay無線化アダプター追加は許容。

## 2. 現在地と実装済みの境界

| 項目 | 0.2.1での状態 | 残る確認 |
|---|---|---|
| Google実地図／別々の車・タブレットView | コード実装済み | 有効キー・GPU・実車での描画 |
| 車と同じ位置をタブレットで開く／独立・同期／片側を閉じる | コード実装済み | 開閉中も車地図が消えないこと、案内開始後の追従との整合 |
| PAN・＋・−等のボタン比較 | コード実装済み | CX-8のフォーカス順・操作数・ダイヤル入力 |
| Gemini Maps Grounding検索／Placesでピン表示 | コード実装済み | 有効キーによる実応答、出典・モデル対応 |
| 渋滞レイヤー | コード実装済み | 実表示。渋滞を考慮した経路計算は別機能 |
| 車両位置診断 | 0.2.1で追加 | Android Auto CarHardwareLocationと端末GPS APIを比較。車載実機は未確認 |
| 目的地への経路、走行・音声案内 | 未実装 | P2で実装 |
| 接続に応じた自作ホームの切替 | 未実装 | 現在の模擬接続フラグはホーム連携の完成ではない |
| OBD2、CarPlay、別スマホ着信 | 未実装・調査候補 | 標準案での必要性を確認して着手 |

現在のAPKは車の位置を取得してもNavigatorの位置へ注入しない。Car Hardware APIから値が届くこと、車の物理GPSが由来であること、測位が改善したことの3点は別々に判定する。

## 3. 段階と完了条件

| 工程 | 主な成果物 | 依存 | 完了の判定 |
|---|---|---|---|
| P0 実車の能力確認 | 地図・ボタン・二画面・位置診断の記録 | APIキー、タブレット、AA導入経路 | 起動/再接続・位置状態・実ダイヤルのログと画面を取得。非対応も事実として記録 |
| P1 案内の共通基盤 | NavigationSessionController、状態モデル、通知・停止処理 | P0で基本表示が成立 | タブレットを閉じてもセッション維持。終了操作・権限取消・切断で状態が破綻しない |
| P2 基本ナビ完成 | 単一目的地、ルート確認、走行・音声案内、再検索、到着・終了 | P1 | SDKシミュレーター→DHU→実機で一連の案内。音声重複・終了後発話がない |
| P3 地図操作の仕上げ | 追従/閲覧切替、二画面、昼夜、ジョグ操作最適化 | P2 | タブレット閲覧が車の追従を勝手に解除しない。操作回数を現状Google Mapsと比較 |
| P4 検索・旅行機能 | 音声検索、経由地、沿道検索、履歴・お気に入り | P2、検索API | 検索→選択→経路→案内が完結。検索失敗時も既存案内が続く |
| P5 日常利用の安定化 | 自作ホーム連携、復帰、課金管理、配布運用 | P3/P4 | エンジンON/OFF相当の接続反復・長時間・音楽/電話割込を検証 |
| P6 測位の高度化 | 車位置/GPS/SDK位置の比較、必要ならOBD2実験 | P0の測位結果 | 改善指標が得られ、対応API経路が存在する機能だけ採用 |
| B 自由UIの別案 | OSM版またはCarPlay/AI Box試作 | P0〜P3で標準案の限界を特定 | 機器数・復帰率・操作性が標準案を上回る場合だけ継続 |

まずP0とP1を行い、P2を完成させる。OBD2や自由UIの調査のために基本ナビを止めない。

## 4. 機能と実装方法

| ID | 機能・ユーザーの操作 | 実装案 | 工程・制約 |
|---|---|---|---|
| N01 | 候補を目的地として指定 | 検索結果をPlace IDと名称のDestinationへ変換。位置だけのピンとは分離 | P2。選択だけで案内開始しない |
| N02 | 経路を確認して開始 | NavigatorへWaypointを渡して経路計算。RouteStatus成功後に距離・所要時間・目的地を提示 | P2。失敗理由を表示、古い非同期結果を棄却 |
| N03 | 走行案内 | 単一のNavigator.startGuidance。車側の追従、曲がり角・残距離・ETAを更新 | P2。ViewごとにNavigatorや案内を開始しない |
| N04 | 音声案内 | AudioGuidanceSettingsで音声/警告のみ/ミュート、音量設定。日本語・実車出力を確認 | P2。Geminiによる曲がり角の生成や重複TTSは使わない |
| N05 | 経路逸脱・渋滞に応じた再検索 | SDKの再検索・経路変更イベントを購読し、routeRevisionと案内表示を更新 | P2。単なる交通色レイヤーとは別 |
| N06 | 到着・中止 | ArrivalListener、stopGuidance、必要に応じclearDestinations。AA側へ終了通知、音声・通知更新停止 | P2。戻る/画面を閉じるだけでは案内終了しない |
| N07 | 有料道路・高速・フェリー回避 | 対応するRoutingOptionsを保存し経路を再計算。どの条件を使ったか表示 | P4。料金の完全性や回避成功を保証しない |
| N08 | 代替ルートを比較 | まずSDKの対応機能を使用。より詳細な事前計画が必要ならRoutes APIとroute tokenを検討 | P4。別APIのpolylineだけ描いてSDK経路と一致したと扱わない |
| N09 | 経由地追加・順序変更・スキップ | Waypointリストを一元化。到着とcontinueToNextDestination等を制御 | P4。案内中の変更前後で残り行程を確認 |
| N10 | 出発/到着時刻を指定した計画 | 対応するRoutes API条件で別途見積り、出発時に実経路を再計算 | P4以降。SDKのその場のETAと予定見積りを区別 |
| M01 | 車は追従、タブレットは自由閲覧 | 車/タブレット別のCameraMode: FOLLOW、BROWSE、OVERVIEW。同期モードと追従の優先順位を定義 | P3。現在の座標同期だけでは案内追従の完成ではない |
| M02 | 同じ場所を開く・車へ反映 | CameraLinkに画面別モードを追加。一回コピーと継続同期を明確化 | P3。タブレットView破棄で車Viewを再作成しない |
| M03 | ダイヤルの最短操作 | SurfaceCallbackの実入力を計測、PAN・＋・−の配置/感度を比較 | P0/P3。生キーの横取りや必ず一押しという前提にしない |
| M04 | 北向き/進行方向、2D/3D、全体表示 | Cameraの向き・傾き・追従モードと経路boundsを制御 | P3。復帰操作はタブレットとホスト許可範囲へ配置 |
| M05 | 昼夜・文字・地図の見やすさ | SDK対応の表示設定を適用、夜間実車で確認 | P3。案内視認性とGoogle帰属表示を維持 |
| M06 | 車側の案内カード最小化 | TBT feedをCar App LibraryのRoutingInfo/Step/TravelEstimate等へ変換 | P2/P3。ホスト共通UIは消せない |
| S01 | 通常の施設/住所検索 | Places検索・Autocomplete、必要ならGeocoding。FieldMaskとsession tokenを設計 | P4。Geminiなしでも目的地を設定できる経路を用意 |
| S02 | 会話型検索 | 既存GeminiSearchをSearchRepositoryへ移動。Maps出典とPlace IDを保持、検索文脈を限定 | P4。AIの推測した座標をルート入力にしない |
| S03 | 音声で検索・案内開始 | タブレットのSpeechRecognizer等で文字化→候補→利用者が選択。音声の停止/キャンセルも用意 | P4。常時録音を既定にしない、ステアリング音声キーの割当は実機依存 |
| S04 | 経路沿いの店・駐車場・休憩 | 経路周辺のPlaces候補を検索し、追加の迂回時間を計算できる時だけ数値表示 | P4。営業中・空車はデータ取得時刻と情報源を表示 |
| S05 | Google Mapsから場所を共有 | ACTION_SEND/geo等を受け、許可した形式からPlace ID/座標/検索語を抽出し確認 | P4。短縮URLを任意URL取得器にしない。外部アプリの経路内部データ抽出は前提にしない |
| S06 | 履歴・お気に入り・自宅/職場 | Room等で利用者の名前/Place ID/順序を保存、削除・エクスポート | P4。第三者APIレスポンスの保存範囲は利用条件を確認 |
| I01 | AA接続時だけホームに操作パネル | CarConnectionで実接続を購読、署名保護Binder/Service等でTheta.Home.Hatchへ状態配信 | P5。テストReceiverの一般公開は禁止。両アプリの署名を確認 |
| I02 | ナビを開かずミュート/再表示/帰宅 | ホームWidget/明示Intentから限定コマンド。地点変更は確認可能にする | P5。Activity起動制限下では勝手な前面化を前提にしない |
| I03 | 音楽と案内の共存 | SDK音声とOSのaudio focus動作を確認、音楽の一時減音・復帰を検証 | P2/P5。音楽サービスごとの再生機能は別モジュール |
| I04 | スマホへの電話着信 | まず車とスマホの既存Bluetooth電話を維持できるか実測。別端末通話の操作・音声転送は別研究 | P5/B。タブレットのTelecomだけで別スマホの通話は扱えない |
| I05 | メーター/HUD表示 | NavigationManager.updateTripへ必要情報を渡す | P2以降。CX-8の表示対応は車種/ホスト依存、アプリ側送信だけでは保証不可 |
| L01 | 車位置と端末位置の比較 | CarSensorsのCarHardwareLocationとLocationManager GPSを別系列で収集 | P0。0.2.1診断追加、GPSアンテナの物理的由来は断定しない |
| L02 | SDKが使う位置を確認 | RoadSnappedLocationProviderの出力を第三系列として比較、適切に購読解除 | P6。出力位置の観測と入力位置の差替えは別 |
| L03 | トンネル・低精度・GPSロスト | 位置時刻/更新間隔/精度/道路吸着結果を観測。古い位置を現在地と扱わない | P6。根拠なく車速補正済みと表示しない |
| L04 | OBD2車速補助 | BLE/SPP等の対応アダプタから車速を読み出し、時刻同期して端末位置と比較 | P6研究。Google SDKへの任意位置注入を前提にしない |
| Q01 | 通信断・再接続・画面OFF | SDKとOSのライフサイクル/通知を整備、復帰後も同じセッションを再表示 | P1/P5。完全オフラインを現版の保証にしない |
| Q02 | 安定性・熱・電池・メモリ | 画面別フレーム/メモリ/CPU、位置更新頻度を計測。閉じたViewを解放 | P5。二画面連続稼働で検証 |
| Q03 | API費用・認証・配布 | API制限・クォータ・予算通知、キー再設定、内部共有時の署名、製品版署名 | P5。SDKとGemini/Placesの請求を分ける |
| Q04 | 遠隔・半自動テスト | ADB/Tailscale、状態JSON、JSONL、SDKシミュレーター、DHU、実車記録を組み合わせる | 全工程。ADB操作を物理ダイヤル成功と扱わない |

## 5. P1/P2の実装設計

### セッションと所有権

`NavigationSessionController`をApplicationから参照する独立クラスに置き、Navigatorの初期化・目的地・開始/停止・イベント購読を一元化する。ActivityとNaviScreenは表示とコマンド発行に徹する。必要な位置フォアグラウンドサービスや通知は、SDKが持つ管理機能と対象OSの要件を確認して構成し、二重の位置サービスを無条件に追加しない。[S1, S2, S8]

提案状態: IDLE → ROUTE_LOADING → PREVIEW → GUIDING → ARRIVED → IDLE。計算失敗はERROR、案内中の再計算はGUIDINGの下のreroutingフラグ、経由地到着は次行程待ちとして区別する。mute、carConnected、tabletOpen、cameraModeは別の状態値。ミュートは案内停止ではない。

`sessionId`、`routeRequestId`、`routeRevision`を持ち、古い計算結果が新しい目的地を上書きしないようにする。開始の連打は一度だけ受理。終了・ホストからの停止・最終目的地到着の後は音声とAAの更新を止める。OSがプロセスを終了した場合は行程を再提示して復帰し、音声案内の無条件再開はしない。

### 経路と案内の流れ

1. Place ID等を持つDestinationを選択。現在のピン表示と、目的地設定コマンドを分離する。
2. SDK7.9.0で利用できるWaypointとsetDestination(s)系APIを確認して呼び、RouteStatusを評価する。7.xと8.xでは戻り値/コールバックが変わるため最新版サンプルをそのまま貼らない。
3. 経路全体と所要時間を提示し、「案内開始」でstartGuidance。
4. 音声設定を適用。TBT feedの次案内、残距離、ETA等を表示モデルへ変換する。[S1, S3, S6]
5. Android AutoのNavigationManager callbackを登録してnavigationStarted、案内中updateTrip、終了時navigationEnded。onStopNavigationを受けたらアプリ側案内・音声も停止する。[S5]
6. 経路変更・再検索・残距離/時間・到着をSDKイベントから処理。タイマーだけで架空の走行進捗を作らない。
7. 閲覧用地図の開閉は上記セッションを終了しない。車のFOLLOWとタブレットBROWSEを独立させる。

### 音声・車載UI

音声はSDKの案内を第一候補とし、ミュート、警告のみ、通常案内、音量調整をタブレット側に置く。音声の重複、Bluetooth接続中の出力先、音楽再生の減音、電話中の割込と復帰を実車で確認する。別スマホの通話音声経路は別に検証する。[S3, S4]

車のTBT表示はSDKのデータから作り、残距離やレーンをAIの文章から推測しない。情報がない項目は非表示/不明にする。レーン、交差点拡大、速度制限などはデータとホストが対応する範囲で追加する。停止操作をタブレットに置くだけでなくホストの停止要求にも必ず応答する。

### 追加・変更対象ファイル案

- `NavigationSessionController.java`（新規）: ナビの状態遷移・非同期経路・SDKイベント。
- `NavigationState.java`（新規）: セッション、目的地、行程、音声、ETA、エラー。
- `CarNavigationBridge.java`（新規）: TBT feed → NavigationTemplate/Trip、ホスト停止。
- `SearchRepository.java`（新規）: Gemini/Placesと通常検索、検索の取消と古い結果除外。
- `NaviApp.java`: ControllerとRepositoryの所有。状態JSONはschemaを更新。
- `RealMaps.java` / `CameraLink.java`: FOLLOWとBROWSEを分離、案内中のカメラ競合を防止。
- `MainActivity.java`: 経路プレビュー、開始、停止、ミュート、経由地。
- `NaviScreen.java`: PAN/ズームに加え、案内データ更新。View所有権を維持。
- `VehicleLocationProbe.java`: 診断専用を維持し、本番ナビ測位へ黙って接続しない。

## 6. 車からの位置情報・測位改善

Android AutoではCar App API 3以降のCarSensorsから車位置を購読できる。機器が供給するかは別問題なので、0.2.1の診断をP0に含める。車速の専用取得は別のCarInfo APIと権限を使う候補で、今回のlocation内speedがあるだけで車輪速取得済みとは扱わない。[S9]

判断順序は「APIから値が届くか」→「古くないか」→「位置・速度・方位がどう変化するか」→「端末GPS APIとの差」→「SDKが既に使っている位置との関係」→「改善する余地があるか」。同じ値が来ることだけで物理GPSの由来や融合経路を断定しない。診断手順は`VEHICLE_LOCATION_PROBE_JA.md`。

GoogleのNavigation SDKではcustom LocationSourceが機能しないと公式に記載されている。そのためOBD2由来の自作推定位置やCarHardwareLocationをGoogleMap.setLocationSourceへ渡すだけで走行案内を補正できる設計にはしない。[S10]

OBD2の実験は車速の読み出し・時刻同期・GNSS/IMUとの比較から始める。採用時はドリフト、トンネル出入口、停車判定を検証する。注入が必要なら対応する別ナビエンジンの選定まで含む独立プロジェクトとして評価。SDKシミュレーターを本番測位入力の代用品にしない。

## 7. Google Maps連携、OSM、CarPlayの扱い

Google Mapsアプリの共有から場所を受け取る機能は候補に入れるが、Google Mapsアプリを自動操作して内部ルート・交通データを抜き取る方法を基本設計にしない。公式Maps URLsは外部アプリを開く入口であり、任意の検索結果一覧や計算済みルートを返すAPIとして設計しない。[S11]

OSMは現APKでは未使用。採用するならMapLibre Native等の描画、利用条件に合うタイル提供元、経路エンジン（例: Valhalla/GraphHopperの評価）、検索、音声/TBT、交通情報を別途揃える。Googleの経路・検索・交通データをOSM上へ自由に移せるとは仮定せず、各APIの地図表示条件・保存条件を確認する。費用、オフライン、更新頻度、交通品質を比較して判断する。

CarPlay案はプランB。iOSのCore Locationには外部アクセサリ由来を示すisProducedByAccessoryがあり、公式資料にはCarPlayも例示される。ただしこれはAndroid APKから呼べるAPIではなく、AndroidタブレットをCarPlay送信機にする実装を提供するものでもない。[S12]

P2 Plusを使う場合は、そのAndroid OSへ車位置がどのproviderやメーカーAPIで渡されるか、ジョグ入力の形式、タッチ付きタブレットと役割を統合できるかを別検証する。今回のAPKのCarPlay診断値はNOT_IMPLEMENTED_ANDROID_APK。iPhone用Core Location診断やAI Box用アダプタを作る工程を将来候補として残す。追加機器・切断/復帰・USB/無線接続の複雑さを含めて標準案と比較する。

## 8. リリース前の受入条件

- 二画面: 案内中にタブレット地図の開閉・画面回転・ホーム移動を繰り返しても車地図/音声/経路が維持される。
- 経路: 成功/経路なし/権限拒否/通信断/別目的地への連打を処理。古い応答で目的地を戻さない。
- 音声: 出力先を確認し、案内重複・停止後発話・電話後の音量固定がない。
- 車操作: PANへの到達回数、ズーム、パン、戻るを実ダイヤルで記録。期待どおりでない操作も一覧化。
- 位置: Car APIの非対応・待機・古い値を成功と表示しない。端末/GPS/SDK/模擬値の出所を区別。
- テスト: 単体テスト、SDKシミュレーター、DHU、停車実機、同乗者が記録する走行確認を分離。自動テストの成功だけで実車OKとしない。
- 遠隔: Tailscale経由の特定ADB serial、更新署名、ログ回収を確認。キーは収集ログに含めない。
- 復帰: USB抜差し、Bluetooth/無線接続、電源断相当、圏外、低メモリ・熱での復帰を記録。
- 配布: デバッグ用Receiver/署名/許可ホストを製品版へ流用するかを明示的に見直し、Android Autoの配布経路を確認。

## 9. Claude Codeへの作業指示

このロードマップ全体を一度に実装せず、P0の観測結果を残した後P1→P2へ進む。実装する工程について、変更ファイル、使用SDK版、課金対象API、テスト項目、未確認事項をPRまたは作業記録へ書く。標準案を維持し、CarPlayやOBD2への方針転換は比較結果を示す。

「機能コードを追加」「単体試験成功」「実SDKシミュレーター成功」「DHU成功」「CX-8実車成功」を別々に記録する。音声案内の導入後も、テストのために利用者の車で自動的に案内を開始しない。SDKシミュレーションは明示コマンドと表示を付け、実走行と混同しない。

## 10. 一次資料（確認日: 2026-11-24）

以下は実装方法の根拠・調査入口。APIの正確な引数・依存関係は採用版のAARとドキュメントで再確認する。とくに同梱7.9.0と現行8.xのAPIを混同しない。

- S1 単一目的地: https://developers.google.com/maps/documentation/navigation/android-sdk/route
- S2 Navigator: https://developers.google.com/maps/documentation/navigation/android-sdk/reference/com/google/android/libraries/navigation/Navigator
- S3 音声設定: https://developers.google.com/maps/documentation/navigation/android-sdk/reference/com/google/android/libraries/navigation/AudioGuidanceSettings
- S4 二画面とAA: https://developers.google.com/maps/documentation/navigation/android-sdk/android-auto
- S5 Car App Libraryナビ: https://developer.android.com/training/cars/apps/navigation
- S6 TBT feed: https://developers.google.com/maps/documentation/navigation/android-sdk/tbt-feed
- S7 事前経路とtoken: https://developers.google.com/maps/documentation/navigation/android-sdk/customize-route
- S8 Foreground service要件: https://developer.android.com/develop/background-work/services/fgs/service-types
- S9 Car Hardware API: https://developer.android.com/training/cars/apps/library/car-hardware-api
- S10 NavSDKで機能しないAPI: https://developers.google.com/maps/documentation/navigation/android-sdk/reference
- S11 Maps URLs: https://developers.google.com/maps/documentation/urls/get-started
- S12 CarPlayを含む位置の由来: https://developer.apple.com/documentation/corelocation/cllocationsourceinformation/isproducedbyaccessory
- S13 Gemini Maps Grounding: https://ai.google.dev/gemini-api/docs/maps-grounding
- S14 SDK更新履歴: https://developers.google.com/maps/documentation/navigation/android-sdk/release-notes
- S15 CarValueの時刻・状態: https://developer.android.com/reference/androidx/car/app/hardware/common/CarValue

8.1.0にはroute token APIや経路沿いデータ先読みの変更があるが、0.2.1は7.9.0を固定している。更新時はAGP/JDK/minSdk等の依存条件、車地図の描画、音声、通知、位置診断をまとめて回帰試験する。新しい資料にある機能を旧版の実装済み機能として記載しない。[S7, S14]

## 11. 2026-11-24 最新のPlan B補足
CX-8は有線CarPlayのみ。iPlay60 Pro（Pad Proではない）・SIM内蔵と確定。小型無線化アダプターの追加購入は許容。CarDeckを先行例とした具体案をCARPLAY_PLAN_B_JA.md、端末/候補製品をADAPTER_AND_DEVICE_JA.md、SIM/TailscaleをSIM_TAILSCALE_JA.md、検証をCARPLAY_TEST_PLAN_JA.mdへ追加した。Androidから公式iOS APIを直接呼べないことと、非公式CarPlay送信実装が存在しないことを混同しない。現APKにCarPlay機能は未実装のまま。
