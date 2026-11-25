# 変更履歴

## 0.3.2
- 二画面の同期のずれを直した（P0 第1回の R12。DHU で、開き直したタブレットの中心が車と約 75 m ずれていた）。Navigation SDK の地図は走行案内がなくても「現在地を追う」表示に入り、カメラを動かし続けていた。追う表示に入ったら CameraLink の位置へ戻して解く。車の地図は知らせなしで追う表示に戻るので、カメラが動くたびにも確かめる。SDK と取り合いにならないよう 10 秒に 5 回まで（超えたら追う表示のままにして `follow_kept` を記録）
- DHU で dualcheck が通過（中心の差 0 m）。ADB の pan で動かした車の地図が引き戻されないことも確認
- 状態 JSON に `car_following`・`tablet_following`・`follow_cancelled`・`follow_refused` を足した（schema は 3 のまま、項目の追加だけ）
- ランチャーアイコンを、青い台に白い矢印のアダプティブアイコンにした（白い矢印だけの絵は Android Auto の一覧で白い丸に見えた）。DHU で確認
- 実車ではまだ確かめていない（R04 が Play 内部テスト待ち）

## 0.3.1
- P0 実機確認の第1回（2026-11-25）の結果を受けた版。地図・二画面・検索・車両位置診断の中身は 0.3.0 と同じ
- release を debuggable でなくした（Google Play は debuggable を受け付けないため）。Android Auto 18 はサイドロードした Car App Library のナビアプリを「提供元不明のアプリ」でも一覧に出さず、Play 経由（内部テスト）で入れる必要がある
- Release に Play 内部テスト用の AAB（`ThetaNaviHatch-<版>.aab`、共通鍵で署名）も添える。APK は今までどおり 1 つ
- ADB の診断：`cmd export` で診断ファイルを `Android/data/app.thetahatch.navi/files/export/` に写し、`cmd export_clear` で消す。`tools/theta_adb.py` の status・capture は run-as を使わずに動く（debuggable な旧版では従来どおり run-as）
- 結果は docs/p0/RESULT_20261125.md

## 0.3.0
- Theta Hatch シリーズに合わせた：アプリ ID を `app.thetahatch.navi` に（0.2.1 の `com.theta.navihatch` とは別アプリとして入る。入れ直し）
- 署名をシリーズ共通鍵に。Releases は GitHub Actions がビルド・署名（試作鍵はリポジトリに入れない）
- 「更新を確認」：Home／KidsHome（更新ハブ）に頼む。ハブが無ければ Releases を開く
- Navigation SDK の API キーのアプリ制限を、新しいパッケージ名＋共通鍵の SHA-1 に登録し直す必要あり（docs/API_SETUP_JA.md）
- 地図・二画面・Gemini 検索・車両位置診断の中身は 0.2.1 と同じ

## 0.2.1
- 車両位置診断（VehicleLocationProbe）と完成形のロードマップ。詳しくは VALIDATION.md
