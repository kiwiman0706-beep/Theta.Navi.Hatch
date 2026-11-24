# Plan B実車試験
すべて初期値はNOT_TESTED。表示/入力の確認は停車中。結果はtemplates/carplay-results.csvへ記録。

| ID | 操作 | 合格の観測 | 失敗時の分岐 |
|---|---|---|---|
| B00 | 機種、OS、ABI、空き容量、Google Play services確認 | iPlay60 Pro、実OS/SDKを記録 | Pad/Mini用情報へ置換しない |
| B01 | SIMのみでネットとTailscale ADB | PC→対象serialが継続応答 | SIM/認証/待受を切り分け |
| B02 | Shizukuを起動、CarDeckへ必要権限 | 起動済みが確認できる | 再起動後は再実行が必要 |
| B03 | 可能ならiPhoneでアダプター基準試験 | 有線→無線変換と純正ノブが機能 | 未所有ならSKIP理由を記録 |
| B04 | 未改造CarDeck接続 | 車載画面に描画、解像度正常 | BT→Wi-Fi→発見→制御→動画のどこか記録 |
| B05 | 接続中SIM/Tailscale/Theta通信 | 遠隔ADB、新規地図領域、Gemini/Places | VPN/ネットワーク束縛/圏外を区別 |
| B06 | 回転左右各10回、傾倒四方向各5回、押下/戻る | 原始HIDと実操作が対応 | 未受信/未分類/注入先/フォーカスを分離 |
| B07 | 起動からパン・ズームまで操作 | クリック数、所要時間を記録 | 現Google Maps/AAと比較 |
| B08 | 車用地図とタブレット地図を同時表示 | 異なるView/画面ID、両画面が実地図 | Activity移動/GL/認証を調べる |
| B09 | 同じ場所をタブレットに開きピンチ | 車画面が残り独立閲覧できる | session/view所有権を修正 |
| B10 | 同期ON/OFF、位置を車へ送る | 意図した画面だけ移動 | FOLLOW/BROWSE競合を確認 |
| B11 | タブレット地図を閉じる/ホーム/回転/画面OFF | 車描画と将来の案内が継続 | Activity/Service/電池制限 |
| B12 | 抜差し・再接続を10回 | 成功数、復帰秒数、画面残留なし | 世代/ソケット/入力状態を解放 |
| B13 | 音楽+案内+別スマホ着信 | 出力先、減音、通話、復帰を記録 | 各Bluetooth/CarPlay音声経路を分離 |
| B14 | 車位置の観測開始/停止 | 状態/時刻/値が得られ停止も機能 | 非対応/無応答/古い値を区別 |
| B15 | 30〜60分連続表示 | 熱、電池、遅延、切断、メモリ | 低fps/解像度等を一項目ずつ比較 |
| B16 | 端末再起動と電源再投入 | SIM/ADB/Shizuku/車表示の復旧順 | 手動必須の操作を隠さず記録 |

B13の案内は案内機能実装後に実施。現Theta0.2.1は走行/音声案内未実装。

## 操作記録の約束
車の動画/写真は現地協力で取得。ADBスクリーンショットは通常タブレット画面で、車のエンコード出力の証拠とは限らない。
HIDログはdeveloper設定から有効化。CarDeck公開APKがdebuggableとは限らず、run-asが失敗したらログexport機能または専用debugビルドを使う。
署名が違うビルドで更新失敗した場合、無断アンインストールしない。まず別applicationIdの検証forkを作る。

## 最小コマンド例
python tools/carplay_preflight.py --serial SERIAL --out captures/pre-carplay.json
python tools/theta_adb.py --serial SERIAL status
adb -s SERIAL install -r research/binaries/CarDeck-0.1.30.apk
# Shizukuの導入・初回同意・権限は本人確認。公開APKを先に試す。
# 接続後:
python tools/carplay_preflight.py --serial SERIAL --out captures/connected-carplay.json

ログには位置、SSID、端末ID、接続認証情報が混じり得る。外部に貼る前に確認する。APIキー設定画面を録画しない。
