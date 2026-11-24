# 手元の Claude Code に渡す依頼文（P0 実機確認）

PC で `git clone https://github.com/kiwiman0706-beep/Theta.Navi.Hatch.git` → `cd Theta.Navi.Hatch` → `claude` を起動し、下の枠をそのまま貼る。

```
Theta.Navi.Hatch（https://github.com/kiwiman0706-beep/Theta.Navi.Hatch）の P0 実機確認をお願いします。

## 最初に読む
- このリポジトリの CLAUDE.md、00_START_HERE_JA.md、VALIDATION.md、docs/TEST_MATRIX_JA.md、docs/VEHICLE_LOCATION_PROBE_JA.md、docs/REMOTE_ADB_JA.md
- シリーズ共通の決まり：https://github.com/kiwiman0706-beep/Theta.Hatch.Series を隣のフォルダに clone（済みなら git pull）して README の「最新ガイドラインの要点」と progress/navi.md を読む

## 端末
- 車載タブレット ALLDOCUBE iPlay60 Pro（SIM 内蔵）。ワイヤレスデバッグを Tailscale 経由でつなぐ
- adb の serial は必ず明示する。分からなければ `adb devices -l` を見せて私に確認する
- adb が無ければ Android Studio の platform-tools の場所を探すか、入れ方を教えて

## 手順
1. 端末の情報を読むだけで集める（Android の版、機種、Google Play 開発者サービスの版、Android Auto の版、SIM の通信状態、空き容量）。root・OS の書き換え・設定の変更はしない
2. https://github.com/kiwiman0706-beep/Theta.Navi.Hatch/releases/latest の ThetaNaviHatch-<版>.apk を取得し、SHA256SUMS と照合してから入れる（`python tools/theta_adb.py --serial <serial> install <apk>`）。署名の違いで入らなくても、勝手にアンインストールしない
3. API キーは私がタブレットの「API設定」で入力する。チャット・コマンド・ログにキーを出さない
4. `cmd enable` → `status` → `smoke` → `capture --out captures/p0-<日付>`。smoke は画面・操作の確認だけで、実地図が出た証明ではない
5. 地図が出たら、タブレットの画面を私が見て確認。Android Auto への出し方（Play の内部共有か、Android Auto の開発者設定の「提供元不明のアプリ」か）を調べて、私に選ばせる
6. 車での確認（二画面・ジョグダイヤル・車両位置の診断 vehiclecheck）は私が停車中にやる。ADB の入力をダイヤルの成功と扱わない。写真・動画は私が撮って渡す
7. TEST_MATRIX と VEHICLE_LOCATION_PROBE の表に、結果を「成功／失敗／未確認」で付ける。単体テスト・SDK・DHU・実車を分けて書く

## 結果を GitHub へ
- このリポジトリは公開。位置の座標・検索の内容・スクリーンショット・端末のシリアル・Tailscale の IP・アカウント名は push しない（captures/ は .gitignore 済み。生のログは手元に残す）
- 結果のまとめを docs/p0/RESULT_<日付>.md に書く（座標は書かない。必要なら「端末 GPS との差 約 x m」のように丸めた値だけ）。VALIDATION.md にも短く追記
- コードを直したら versionName と versionCode を上げ、CHANGELOG.md に同じ版の節を書く（main に push すると Release ができる）
- main にコミットして push（コミットの前に `git diff --stat` を見せる）
- Theta.Hatch.Series の progress/navi.md の「実機で確かめること」と状態を直して、既定ブランチ ccr-aa31c115-muwnvz に push する（ほかのアプリのファイルは触らない）

分からないこと・お金がかかること（API の有効化、課金の設定、機器の購入）は、進める前に私に聞いてください。
```
