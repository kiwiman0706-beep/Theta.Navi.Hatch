# 変更履歴

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
