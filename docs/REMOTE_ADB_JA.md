# 自宅PC → Tailscale → 車載タブレット

ユーザーの想定:
1. 自宅PCでClaude Code Localを起動し、スマホからRemoteで操作。
2. 車載タブレットと自宅PCを同じtailnetに接続。
3. 自宅PCのADBでタブレットへ接続。
4. ClaudeがAPKの導入・状態読取り・コマンドテストを実施。
5. ユーザーが実車のダイヤルを操作し、Claudeがログと画面を照合。

**TailscaleにログインするだけではADBは有効になりません。** 初回のADB認証・ペアリング、タブレット側の確認操作が必要です。
タブレットの機種、OS、無線ADBの待受インターフェースによって、tailnetアドレスから接続できるかが異なります。

## 方法A: Android 11以降のワイヤレスデバッグ

- タブレット: 開発者向けオプション → ワイヤレスデバッグ → ペア設定コード。
- ペアリング用ポートと、通常の接続用ポートは別です。
- 両端のTailscale疎通を確認し、実際に表示されたポートを使います。
- mDNSによる自動発見はtailnet越しに期待せず、IP:ポートを明示します。

```sh
adb pair TABLET_TAILSCALE_IP:PAIR_PORT
adb connect TABLET_TAILSCALE_IP:CONNECT_PORT
adb devices -l
```

ペアリングコードはその場で入力。コードをリポジトリやログへ残さない。
Wi-Fi切替/再起動でワイヤレスデバッグがOFFになったり接続ポートが変わることがあります。
端末がWi-Fi側アドレスだけで待ち受ける場合、Tailscale IPへの置換では動きません。タイムアウトをアプリの不具合と判断しないこと。

### 携帯回線（SIM）だけで使う（2026-11-25 iPlay60 Pro で確認）

ワイヤレスデバッグは Wi-Fi 接続中でないと ON にできない。車内で SIM 回線だけにするときは、一度 Wi-Fi（スマホのテザリングで可）につないで方法Aで pair・connect し、その接続のまま `adb -s TABLET_TAILSCALE_IP:CONNECT_PORT tcpip 5555` を実行する。Wi-Fi を切っても `adb connect TABLET_TAILSCALE_IP:5555` で Tailscale 越しに使える（再起動で切れる）。iPlay60 Pro・Android 15 ではワイヤレスデバッグの待ち受けが Tailscale のアドレスでも届いた。tailnet の経路は中継（DERP）になることがあり、APK の転送は遅くなる。

## 方法B: 一度USB認証してADB TCPモード

機種が対応する場合のみ。タブレットをPCにUSB接続して認証し、その接続で実行:

```sh
adb -s USB_SERIAL tcpip 5555
```

その後、自宅PCから:

```sh
adb connect TABLET_TAILSCALE_IP:5555
adb -s TABLET_TAILSCALE_IP:5555 get-state
```

初回に別PCでUSB認証した場合、自宅PCのADB鍵は未認証の可能性があります。タブレット側の承認が必要です。
従来型TCP ADBはLAN側にも待ち受ける場合があります。インターネットへのポート転送は行わず、信頼できるネットワークでのみ使用。tailnet ACLは自宅PCから必要ポートに限定してください。Tailscale ACLはWi-Fi LAN側への接続まで防ぐものではありません。
使い終わったら対象端末に `adb -s SERIAL usb` を実行するか、デバッグを無効化します。再起動後も5555が維持されるとは限りません。

## インストールとテスト

以下は `SERIAL` を実際のIP:ポートに置き換えます。

```sh
python tools/theta_adb.py --serial SERIAL install Theta.Navi.Hatch-0.2.1-vehicle-probe.apk
python tools/theta_adb.py --serial SERIAL cmd enable
python tools/theta_adb.py --serial SERIAL cmd connect --a 1
python tools/theta_adb.py --serial SERIAL status
python tools/theta_adb.py --serial SERIAL smoke
# 実地図が車・タブレットで表示された後のみ:
python tools/theta_adb.py --serial SERIAL dualcheck
python tools/theta_adb.py --serial SERIAL capture --out captures/baseline
```

WindowsでPython実行名が `py` の場合は読み替え。
接続に失敗したら、まず `adb devices -l` と `tailscale ping TABLET_TAILSCALE_IP` を確認。
ADB接続認証・tailnet接続をAPKから自動で変更する機能はありません。

## 実車の入力測定

```sh
python tools/theta_adb.py --serial SERIAL cmd connect --a 0
python tools/theta_adb.py --serial SERIAL cmd mark --value BEFORE_DIAL_RIGHT
```

ユーザーが停車中にダイヤルを1ノッチ右回転し、次を実行:

```sh
python tools/theta_adb.py --serial SERIAL capture --out captures/dial-right
```

「パンモードの外/中」「回転」「上下左右へ倒す」「押下」「戻る」を1つずつ分けて測定。
`adb shell input keyevent` はAndroid Auto経由のダイヤル入力を再現するものではありません。

## Android Autoホストの確認

**Android Auto 18.0 では、adb で入れた（サイドロードした）Car App Library のナビアプリは、AA の開発者設定「提供元不明のアプリ」を ON にしても一覧に出ない**（2026-11-25 iPlay60 Pro で確認。logcat の `CAR.VALIDATOR` に `Package DENIED; failed all other checks [app.thetahatch.navi]`）。メディアアプリ（Movie・Book・Music Hatch）は出る。Play Console の内部テストで入れる。DHU はこの審査を飛ばすので、DHU で出ても実車で出る証拠にならない。

タブレットで起動できても車の一覧に出ないときはREADMEのPlay内部共有/内部テストの項を確認。
内部共有を利用すると再署名されることがあるため、毎回のADB更新が同一署名で可能か確認してください。

PC上のDHUを使う場合: Android Auto開発者設定でヘッドユニットサーバーを開始し、

```sh
adb -s SERIAL forward tcp:5277 tcp:5277
```

Android SDKのDesktop Head Unitを起動します。Tailscale越しでもADB forward経由の接続が候補になりますが、帯域・遅延の影響があります。
DHUと実車を同時に接続せず、切り替えてテストします。DHUのロータリー入力検証もCX-8実車の結果と区別します。
公式手順: https://developer.android.com/training/cars/testing/dhu

AndroidワイヤレスADB公式: https://developer.android.com/tools/adb#wireless
Tailscale ACL: https://tailscale.com/kb/1018/acls
