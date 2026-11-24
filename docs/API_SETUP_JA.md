# API設定

## 1. Navigation SDK（実地図）

Google Cloudでプロジェクトを選び、課金設定を紐付け、Navigation SDK for Androidを有効にしてキーを作成。
アプリ制限はAndroidアプリ、パッケージ名は `app.thetahatch.navi`。
Releases のAPK（0.3.0〜、シリーズ共通鍵）の署名SHA-1:

`A1:13:EA:34:83:60:56:32:E7:F5:94:4E:4E:8C:20:55:DD:AE:28:BA`

0.2.1までの試作鍵（パッケージ `com.theta.navihatch`、SHA-1 `28:C2:74:EE:…:B6:CA`）の登録は、旧APKを消したら外してよい。

API制限にはNavigation SDK for Androidを含める。Placesを同じキーで呼ぶ場合はPlaces API（New）も有効化し、API制限にも含める。
内部共有等で署名が変われば、その証明書のSHA-1を追加する。自作の別鍵で再署名した場合も同様。

アプリのAPI設定でキー入力→保存→「地図を開始」。位置情報を許可し、SDKの利用規約は利用者本人が確認する。
エラー時はキーの文字列をログに出さず、SDKエラーコード・有効API・課金・証明書・Play servicesを確認。

https://developers.google.com/maps/documentation/navigation/android-sdk/get-api-key
https://developers.google.com/maps/documentation/navigation/android-sdk/android-studio-setup

## 2. Gemini（地図検索）

Google AI Studio等で取得したGemini Developer APIキーを「Gemini APIキー」に保存。
初期モデルは `gemini-2.5-flash`。Google Maps Grounding対応・利用可能なモデル名へ設定画面で変更できる。
通常のテキスト生成が使えるキーでも、Maps Groundingの課金・利用枠・地域・モデルの条件を満たさなければ検索は失敗する。

REST: `POST https://generativelanguage.googleapis.com/v1beta/models/MODEL:generateContent`
キーは `x-goog-api-key` ヘッダー。`tools: [{googleMaps: {}}]` と、必要時のみ `toolConfig.retrievalConfig.latLng` を送信。
出典付きの候補だけを採用。リアルタイムの営業時間・駐車場空きなどが必ず正しいとは保証しない。

https://ai.google.dev/gemini-api/docs/maps-grounding
https://ai.google.dev/api/generate-content

## 3. Places API（New）（候補のアプリ内表示）

候補のPlace IDから `GET https://places.googleapis.com/v1/places/PLACE_ID`、FieldMask `id,location` で位置を取得する。
Placesキー欄が空ならNavigationキーを使う。別プロジェクトや異なる制限のキーを使いたい場合にPlacesキー欄へ入力。
アプリはAndroidパッケージ名と実際の証明書SHA-1をリクエストヘッダーに付ける。拒否される場合はキーのAPI制限・アプリ制限・対応サービスを確認し、無制限キーへの変更を既定の解決策にしない。
Gemini検索のGoogle Maps出典リンクは、このAPIが使えなくても開ける。

https://developers.google.com/maps/documentation/places/web-service/place-details
https://developers.google.com/maps/api-security-best-practices

## キーの取り扱い

この配布物には実際のキーはない。チャットへ貼り付ける必要もない。
端末入力後に一度プロセスが起動したSDKキーは変更できないため、変更後は手動でプロセスを再起動。
キー削除後も起動中SDKは古いキーをメモリに保持するので、削除を完了させる場合も再起動。
`adb shell am force-stop app.thetahatch.navi` は車載地図も終了させるため、停車中かつ切断してから行う。
