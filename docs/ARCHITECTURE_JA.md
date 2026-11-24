# 二画面・検索の設計

## 表示の所有権

NaviScreenが車載Surface・VirtualDisplay・Presentation・NavigationViewForAutoを所有。
MainActivityがタブレットNavigationViewを所有。どちらもGoogleMapをRealMapsに登録する。
共有するのはNavigatorとCameraLinkの値だけ。タブレットを閉じたときはtabletの登録だけ解除し、車側を終了しない。
Google SDK内の描画資源やOSのメモリ管理はアプリ外なので、この構造だけで実機表示維持を保証はできない。

## 位置の同期

緯度・経度・zoom・bearing・tiltの5値。独立モードが既定。
「車と同じ場所を開く」は1回のコピー。同期ONはカメラ移動コールバックごとに相手へ反映。
同じ値の再送を許容誤差で止め、二つのコールバックが無限に反射しないようにする。
閉じる際の位置保存は相手へ再送しない。
異なる画面比率で四隅の地理範囲までは一致しない。

## APIキーと初期化

端末のAPI設定でNavigation/Gemini/任意Placesキーを入力し、AES-GCMで暗号化、Android Keystoreの鍵で保護。
キー自体はdiagnostic JSONやイベントへ記録しない。NavigationApi.setApiKeyは初期化前・プロセス内1回のみ。
Navigator初期化は利用規約と位置情報権限の確認後。Activityの寿命とは切り離してApplicationに保持。
この版では経路検索・startGuidanceを呼ばない。

## Gemini検索

ユーザーが検索ボタンを押す→HTTPS generateContent→googleMaps grounding→本文＋出典付き候補を表示。
本文は参考回答。選択可能な候補はgroundingChunks.mapsに限定。
候補を内部表示する場合のみPlace IDをPlaces API（New）へ問い合わせ、取得した位置へ両地図を移動・ピン追加。
LLMが生成した緯度経度を正解として採用せず、地図出典のない候補の位置は補完しない。
検索の質問文・APIキーを診断イベントに保存しない。ただし画面スクリーンショットには検索結果が写る。

## 観測

car_surface: AAから来たスクロール・スケールなど。物理の生キーではない。
car_camera/tablet_camera: GoogleMapのカメラ変更。
car_sdk/tablet_sdk: View生成とmap_ready/map_loaded。
car_map_generation: 車載View再生成回数。タブレットを開閉しただけで増えるなら要調査。
map_loaded: SDKが通知した読み込み完了。車載全画面の描画の視覚的保証ではない。

## 次段階

実機の二画面維持・操作性を確認後に目的地設定・ルート・音声案内へ進む。
Navigation SDKの実経路データを使う際は、AA NavigationManagerとTurn-by-Turn feed等を追加する。
ホーム統合は署名権限付きService等の限定IPCで実装。DUMP保護のテストReceiverを一般公開しない。

## 0.2.1 診断の分離

VehicleLocationProbeはApplicationが所有し、NaviScreenからCarContextのattach/detachを通知。地図SDKに依存せず、CarSensorsと端末GPS APIを観測する。LocationProbeStateが各系列のstatus・時刻・Locationの有無を保持。二つのAPI値をナビへ注入しない。
ロードマップはROADMAP_JA.md。現行と将来設計の境界をそこに示す。
