# 一次資料と根拠
確認日: 2026-11-24 JST。製品説明は要約。メーカーの一般対応とCarDeck互換を区別する。

| ID | 出典 | 確認した範囲 |
|---|---|---|
| C1 | https://github.com/shaonialife/cardeck | Androidから無線CarPlay送信、14+/Shizuku、Audi作者報告。固定版はUPSTREAM_MANIFEST |
| C2 | https://github.com/shaonialife/cardeck/releases/tag/v0.1.30 | 公開APK、SHA256、任意アプリ選択等の更新内容 |
| C3 | https://github.com/jjuugk/CarPToCarL | CarLife+を送信する別実装、公開ソースと既知の限界 |
| C4 | https://github.com/Fotsbeats/Pi2Carplay | Pi上Androidから有線送信、GPS/通話の参考実装、ライセンス範囲 |
| C5 | https://www.ottocast.com/en-jp/products/u2-air-pro | 有線CarPlay無線化。通常の対象はiPhone |
| C6 | https://www.carlinkit.com/u2w-mini | 有線CarPlay無線化、純正ノブ対応の一般説明 |
| C7 | https://www.carlinkit.com/manuals_mini | U2W Miniの公式マニュアル入口 |
| C8 | https://shizuku.rikka.app/guide/setup/ | 非root起動方法、再起動後の再実行 |
| C9 | https://github.com/RikkaApps/Shizuku/releases/tag/v13.6.0 | 取得したShizuku APKの配布元 |
| C10 | https://www.alldocube.com/jp/products/iplay60pro/ | 対象はPad ProでないiPlay60 Pro。14版/15版、SIM/LTE |
| C11 | https://developer.android.com/reference/android/net/ConnectivityManager#bindProcessToNetwork(android.net.Network) | プロセスのネットワーク束縛の公式API |

上流MIT/GPL等の許諾範囲でソース・文書・ライセンスを同梱。製品ページの丸ごと転載は行わず、必要な事実と参照URLを記録した。
公式API/ナビ/車位置/Geminiの既存資料はdocs/ROADMAP_JA.mdの一次資料欄も参照。

## 上流コードを調べた具体的な箇所
CarDeck固定コミットのinput/HidNavigation.ktのhandleRaw、net/WifiCarPlayConnector.ktのbindProcessToNetwork、SessionController.ktのjoinCarWifiSilentlyを確認。
HID報文解析はNativeBridgeへ移行しており、古いHID-KNOB.mdの説明と実コードは同一ではない。該当ソースを開いてから変更すること。
CarPToCarL docs/current/PROJECT-GUIDE.mdではGPS転送等に実験/未確認の項目がある。成功扱いしない。
Pi2Carplay LICENSE-SCOPE.mdではプロトコルSDKの実装ソース非収録、Androidベースの個別条件を記載。単一MITプロジェクトとは扱わない。
