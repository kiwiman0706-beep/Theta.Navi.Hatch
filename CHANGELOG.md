# 変更履歴

## 0.3.0
- Theta Hatch シリーズに合わせた：アプリ ID を `app.thetahatch.navi` に（0.2.1 の `com.theta.navihatch` とは別アプリとして入る。入れ直し）
- 署名をシリーズ共通鍵に。Releases は GitHub Actions がビルド・署名（試作鍵はリポジトリに入れない）
- 「更新を確認」：Home／KidsHome（更新ハブ）に頼む。ハブが無ければ Releases を開く
- Navigation SDK の API キーのアプリ制限を、新しいパッケージ名＋共通鍵の SHA-1 に登録し直す必要あり（docs/API_SETUP_JA.md）
- 地図・二画面・Gemini 検索・車両位置診断の中身は 0.2.1 と同じ

## 0.2.1
- 車両位置診断（VehicleLocationProbe）と完成形のロードマップ。詳しくは VALIDATION.md
