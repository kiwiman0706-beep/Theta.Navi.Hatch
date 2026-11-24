# SIM + CarPlay Wi-Fi + Tailscale/ADB
## 確定前提
タブレット自体がSIM内蔵。スマホのWi-Fiテザリングを常時必要とする設計にはしない。
期待する分担はCarPlayアダプターとのローカルWi-Fi通信と、SIMによるインターネット/Tailscale。
SIM内蔵は有利な条件だが、実際のモバイルデータ契約・圏内・OS経路・VPN共存は実機で確認する。

## コードを読んで分かった注意点
同梱CarDeck mainのnet/WifiCarPlayConnector.ktはbindProcessToNetworkを使い、CarDeckプロセスを車用Wi-Fiへ束縛する。SessionController.ktのjoinCarWifiSilently付近に、別プロセスのナビ/音楽は影響を受けない設計意図の説明がある。
これは「全Androidアプリのインターネットが必ず正常」の実測証拠ではない。
**Thetaの地図/Gemini処理をCarDeckの同一プロセスへ無造作に移すと、インターネットのない車Wi-FiへAPI通信が流れる可能性**がある。
最初はCarDeckとThetaを別アプリ/別プロセスに保ち、表示起動と限定IPCで連携。統合する場合は通信をNetwork単位で分離し、DNSも含めて検証する。

## 実機確認
- 接続前: SIM通信、Tailscale接続、PCから対象serialへのadb shell応答。
- 接続後: Wi-Fiがアダプターへ移った状態で同じadb shellが応答するか。
- その状態でTheta実地図の新しい領域取得、Gemini検索1回、Places解決を確認。キャッシュ表示だけで通信成功にしない。
- CarDeck映像とHIDを動かしながら、Tailscale経由のADB応答・遅延を計測。
- SIM圏外→復帰、アダプター抜差し、画面OFF、端末再起動、Shizuku再起動を試す。
- VPN有効時だけmDNS/ローカル接続が失敗する場合、Tailscaleの該当設定とAndroidのVPN/ローカルネットワーク挙動を確認。VPNを無断解除して遠隔復旧経路を失わない。
- OSの「インターネットなしWi-Fi」確認で必要な接続維持操作がないか確認。

## ADBの限界
Tailscaleにログインしただけではadbdが必ずTailscale IPで待ち受けるわけではない。
既存REMOTE_ADB_JA.mdの認証手順を使い、Androidワイヤレスデバッグのポート変更/ネットワーク切替時停止を実測する。
USB初回認証、または承認済みワイヤレスペアリングが必要な場合は本人操作。
インターネットへのTCP5555公開やルータのポート開放はしない。認証とTailscaleアクセス制御を維持。
Shizukuは非root起動後、再起動すると再起動操作が必要。遠隔ADBが残ることとShizukuが動くことは別々に確認。
https://shizuku.rikka.app/guide/setup/

## 失敗時の復旧
現地でアダプターを外す→タブレットの通常SIM通信とTailscaleを確認→同じ対象ADBへ再接続→ログ回収。
端末初期化、root化、ROM変更、ネットワーク設定全消去は初手にしない。
