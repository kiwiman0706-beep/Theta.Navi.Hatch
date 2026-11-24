# Theta.Home.Hatch との連動（構想）

更新: 2026-11-24。まだ実装していない。ROADMAP の I01・I02（P5）を具体にしたもの。

## 内蔵か連動か

| | 内蔵（Home の APK に Navi を入れる） | 連動（別アプリのまま、Home から操作） |
| --- | --- | --- |
| APK の大きさ | Home が約 50MB 増える（Navigation SDK） | Home は変わらない |
| 地図キー | Home のパッケージ名で登録し直し。Home の利用者全員に地図の課金設定が要る | Navi だけ |
| 落ちたとき | 地図の不具合でホームごと落ちる | Navi だけ |
| Android Auto | Home が「ナビアプリ」として車に出る | Navi が車に出る（今のまま） |
| 更新 | Home と同じ速さでしか出せない | 別々に出せる。どちらもハブ（Home）が更新 |

**まず連動**にする。同じ共通鍵で署名するので、`signature` 権限で守った窓口だけで安全につなげる。
連動で足りないと分かったら、その窓口を使って Home の中に操作パネルを「内蔵っぽく」見せる（地図そのものは Navi に置いたまま）。

## 連動の形（案）

### Navi が出すもの

- ContentProvider `app.thetahatch.navi.theta`（権限 `app.thetahatch.permission.SHARE`、`protectionLevel="signature"`）
  - `query /drive`：`carConnected`（CarConnection の実値。模擬フラグではない）・`state`（IDLE／PREVIEW／GUIDING／ARRIVED）・目的地の名前・残り距離・ETA・ミュート・`at`
  - 変化したら `notifyChange`。Home は ContentObserver で受ける
- Intent（`setPackage("app.thetahatch.navi")`、同じ権限で守る）
  - `app.thetahatch.action.NAVI_SEARCH`：タブレットの検索画面を開く（extra `query` 任意）
  - `app.thetahatch.action.NAVI_COMMAND`：extra `cmd` = `mute`・`unmute`・`recenter`・`go_home`（自宅を目的地に。開始は Navi の画面で利用者が押す）
- 走行案内そのものはまだ無い（P2）。それまで `state` は IDLE だけ、`carConnected` と地図の状態だけを出す

### Home がすること

- Android Auto の接続中だけ「ドライブ」の操作パネル（タブかドロワー）を出す。接続は Home 自身も CarConnection で確かめられる
- パネル：検索（Navi を開く）・自宅へ・ミュート・次の案内（P2 以降）・音楽（Theta Music）
- Navi が入っていなければパネルは出さない（`resolveContentProvider` で確かめる）

### 共通設定

- Gemini のキー・モデルは Home（設定のハブ）から受け取る（共通設定 §4）。Navigation SDK のキーは Navi だけのもの（パッケージ名と SHA-1 に縛られるので共有しない）

## 決めること・Series に頼むこと

- 上の Intent・ContentProvider を spec §6.2 の一覧に足すか（Series のスレッドで）
- Home 側のパネルの置き場所（Home のスレッドで）
- P0（実車で地図・二画面が動くか）が済むまでは作らない
