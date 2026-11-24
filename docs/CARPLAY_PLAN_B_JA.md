# Plan B: タブレットをCarPlay送信端へ
## 結論と証拠の段階
CarDeckが最も近い。公開READMEは未root Androidから無線CarPlay送信、Audi MIB3で映像と入力の成功を報告。
同梱ソースには仮想ディスプレイとHIDナビ入力の処理が存在する。これはCX-8 + iPlay60 Pro + 市販無線化アダプターでの成功証拠ではない。
現Theta APKにCarPlay機能を追加したわけではない。以下はClaudeが実装する設計と検証計画。

## 上流3件
| Project | 役割 | 今回の扱い |
|---|---|---|
| CarDeck | Android14+、Shizuku、無線CarPlay送信、仮想画面 | 第一候補。公開APKで疎通してからfork |
| CarPToCarL | Android12+、CarLife+の映像/音声を無線CarPlayへ | プロトコル、位置、接続復旧の比較資料。Theta UIの直接基盤としては優先しない |
| Pi2Carplay | Raspberry Pi 5/独自Androidの有線CarPlay | 有線USB・GPS・別スマホ通話の研究資料。タブレットへそのまま導入不可 |

CarDeck/CarPToCarLはMITのソース。Pi2Carplayは複数ライセンスと外部バイナリ依存がありLICENSE-SCOPE.md参照。いずれもApple公式認証済み製品と説明しない。

## CarDeckのソース案内
ソースZIP内のapp/src/main/java/io/github/shaonialife/cardeck/を起点にする。
- MainActivity.kt、CarPlayService.kt、SessionController.kt: 接続、サービス、ライフサイクル。
- net/WifiCarPlayConnector.kt: Wi-Fiとプロセスのネットワーク束縛。SIM通信との分離が重要。
- capture/、render/DualDisplayComposer.kt、launcher/: 仮想ディスプレイ、合成、アプリの起動。
- input/HidNavigation.kt: 正負の回転deltaや方向/押下をDPADへ変換。
- input/ConsumerKeys.kt: 媒体キー。地図操作と混同しない。
- protocol/NativeBridge.ktと同梱ネイティブ.so: HID報文の分類・解析。公開ZIPにRustソースは見つからず、ネイティブ再ビルド可否は別調査。
- shizuku/: 入力注入と仮想画面の権限経路。
- docs/HID-KNOB.md: BMW iDrive向けの調査記録。CX-8への適合は未確認。
README/過去ドキュメントと現コードは差がある。HIDの報文解析は現mainではRustへ移っており、古い説明だけでKotlinを直さない。

## ジョグを地図専用にする実装案
現コードの一般DPAD化では回転と上下傾倒を同じキーに潰す場合があり、後段だけでは区別できない。
まずHID分類直後のraw delta / hat / button maskを、deviceUUID・descriptor hash・時刻・session generationと共に記録。
その段階から、Thetaが車側の前面地図である場合だけZoom(delta)、Pan(dx,dy)、ModeToggle、Recenter等の限定コマンドへ変換する。
初期案: 回転=ズーム、上下左右傾倒=パン、押下=追従/閲覧、戻る=車側メニュー復帰。実車のイベントに合わせ変更する。
音量やメディアキー、ホーム/戻るなど車共通の操作は地図へ一律に吸収しない。
新IPCは明示宛先・署名権限などで保護。公開broadcastで任意アプリから操縦できる設計は避ける。
エンコーダの解像度・安全領域・地図の表示領域を分け、パン量は実表示サイズで正規化。

## 車とタブレットの別画面
現ThetaのNavigationViewForAutoはAA向け。CarDeckで通常Android Activityを仮想画面へ起動する経路へそのまま流用できると決めつけない。
CarDeck用の専用CarMapActivity（提案）を設け、通常SDK Viewを使えるか契約/SDK制約と実描画を検証。タブレットMainActivityと異なるView・画面IDを持たせる。
既存CameraLink/CameraStateをモデルとして再利用。一回コピー、独立、同期、追従を分け、タブレット側onStop/onDestroyで車ViewやNavigatorを終了しない。
同一Activityを両ディスプレイへ起動してタスクが片側へ移る問題を避け、Activity/launchMode/taskAffinity/displayIdを実測する。
Shizuku/仮想ディスプレイでGoogle SDKが描けるかは未検証。SurfaceView/GL、画面OFF、規約ダイアログ、ロゴ帰属も試験。
Google Navigation SDKを非公式CarPlay表示へ採用できるかは、公式利用条件/対応環境も確認してから製品化する。

## GPS・車速
AA診断は既存VehicleLocationProbeを使う。CarDeck接続だけではAndroid AutoのCarContextは生成されない。
CarPlay側は受信メッセージの能力宣言・位置購読・サンプル・終了を独立のCarPlayLocationProbeとして設計する。
CarPToCarL docs/current/とソースのCarGpsFix/onCarLocation、Pi2Carplay src/bridge/gps_forward.h、gps_stats.h、nmea.hが調査入口。
位置受信は上流でも実験扱いの部分があり、アダプターが全フィールドを転送する保証もない。
source=CARPLAY_ACCESSORY、rxElapsedRealtime、sourceTime、lat/lon、accuracy、speed、bearing、validityを分離し、端末GPSと比較する。物理アンテナ/車輪速融合は不明と明示。
Google SDKへ任意の車位置を注入する機能と、観測だけする機能を分ける。現APKのNOT_IMPLEMENTED_ANDROID_APKは正しい実装状態のまま。

## スマホ着信・音声
SIM内蔵タブレットでもユーザーが受けたいのは別スマホへの電話。
最初はスマホと車の既存Bluetooth電話がCarPlay表示中も共存するかを確認。タブレットをHFPクライアント化するには一般APKでは済まない可能性がある。
Pi2CarplayのBluetooth通話実装は専用Android構成の参考で、stock iPlayへの移植成功ではない。
地図音声・音楽・電話・車マイク・復帰は別テスト。接続成功だけで電話も対応済みにしない。

## 段階
B0 端末情報/SIM遠隔経路 → B1 未改造CarDeckとアダプター → B2 HID原始入力 → B3 車専用地図と独立タブレット → B4 案内/音声/GPS → B5 再起動・長時間・ホーム連携。
AAのP0/P1/P2は維持。Plan Bの接続が失敗してもAA版を壊さず比較記録を残す。
