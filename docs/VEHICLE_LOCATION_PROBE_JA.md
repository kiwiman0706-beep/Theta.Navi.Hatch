# 車両位置情報の診断 — 0.2.1

目的は「CX-8とAndroid Autoが、本アプリへ車位置を返すか」を実測することです。地図用APIキーは不要。Google Navigation SDKの初期化・案内開始も不要です。Android Autoで本アプリの車載画面を起動できる導入環境は必要です。

## 取得する2系列

| 系列 | API | 分かること／分からないこと |
|---|---|---|
| 車API | CarContext → CarHardwareManager → CarSensors.addCarHardwareLocationListener | Android Autoホストが返したLocation。物理的に車のGPSアンテナ由来か、車速融合済みかまでは保証しない |
| 端末GPS API | Android LocationManagerのGPS_PROVIDER | Android OSがGPS providerとして返すLocation。AA接続中の内部融合の有無までは断定しない |

Car API 3未満なら車側はHOST_API_BELOW_3と記録。AAOSの車内Android OSと、タブレット投影のAndroid Autoは別の環境です。本APKはAndroid Auto向けです。

この版ではGoogleのナビ測位へ診断値を渡さず、地図カメラも動かしません。CarPlayの直接診断は未実装で、状態JSONにNOT_IMPLEMENTED_ANDROID_APKと記録します。CarPlay研究はROADMAP_JA.mdのプランBへ記載しています。

## タブレットで操作

1. 本APKを更新インストールし、Android AutoでTheta.Navi.Hatchを開く。
2. タブレット側のTEST LABでテストモードをON。
3. 「車両位置情報の診断 ON/OFF」を押す。正確な位置情報を許可する。
4. 車APIと端末GPS APIの状態・サンプル数を確認。少なくとも30秒観測する。
5. ログを回収し、同じボタンでOFF。テストモードOFFでも購読を解除する。

診断は既定OFFで、プロセス再起動後もOFFです。ONのままAAが切断されると購読を解除し、同じプロセスで再接続された場合は再開します。タブレットの地図を閉じる操作だけでは診断を止めません。
診断中は位置座標・時刻等を端末内ファイルへ保存します。独自のサーバ送信はありません。ログを共有する前に経路・自宅等の位置が含まれることを確認してください。

## Claude / ADB

最初にタブレットで正確な位置情報を許可し、車載側で本アプリを開いてください。

```sh
python tools/theta_adb.py --serial SERIAL vehiclecheck
```

このコマンドはテストモードを有効化して観測し、最大約35秒待ちます。3件の有効座標または一部の確定状態が得られると早く終了します。最後に診断の購読を停止します（テストモードはONのまま）。結果はOBSERVATION_ONLYであり、車GPSが使えたという一律PASSは返しません。

連続観測・比較用:

```sh
python tools/theta_adb.py --serial SERIAL cmd enable
python tools/theta_adb.py --serial SERIAL cmd vehicle_probe --a 1
python tools/theta_adb.py --serial SERIAL status
python tools/theta_adb.py --serial SERIAL capture --out captures/vehicle-location
python tools/theta_adb.py --serial SERIAL cmd vehicle_probe --a 0
```

`state.json`の`vehicle_location_probe`に現在の詳細、`vehicle-location.jsonl`に診断履歴が入ります。1MiBを超えるとpreviousへローテーション。通常のlogcatには座標本体ではなく状態だけを記録します。車のLocation内にspeed/bearingが含まれた場合のみ保存し、独立した車輪速やOBD2データと呼びません。

## 判定の読み方

| 状態 | 意味・次の確認 |
|---|---|
| WAITING_FOR_HOST | AAでアプリの車載画面を起動していない。模擬接続ボタンでは代用不可 |
| HOST_API_BELOW_3 | 接続ホストのCar App APIが不足。位置APIを呼ばず記録 |
| PERMISSION_REQUIRED / PERMISSION_DENIED | 位置許可がない、または呼出しが拒否。設定確認後に診断をOFF→ON |
| WAITING_FOR_CALLBACK | 購読要求後の待機。取得成功ではない |
| NO_CALLBACK_AFTER_30S | 30秒以内にコールバックなし。非対応と断定せず接続/権限/屋外条件を確認 |
| SUCCESS + RECENT | 有効座標と最近の時刻を観測。物理GPSの由来や精度改善までは未確定 |
| SUCCESS_WITHOUT_LOCATION | ホストは成功と返したがLocationがnull |
| UNIMPLEMENTED | ホストがこの値を未実装として通知 |
| UNAVAILABLE / UNKNOWN | 一時的に取得不能または状態不明。未実装と区別 |
| STALE_CALLBACK / STALE_FIX / STALE_CAR_VALUE | 受信/測位/CarValueの時刻が15秒超。古い位置を比較対象にしない |
| FIX_TIME_UNKNOWN / CAR_CLOCK_UNKNOWN | 時刻がない/将来など。新しい正解位置と扱わない |
| GPS_DISABLED / GPS_PROVIDER_ABSENT | 端末GPS API側の設定またはproviderなし |
| DISCONNECTED / STOPPED | ホスト切断または停止。残った最終サンプルは履歴であり現在の取得成功ではない |
| SUBSCRIBE_ERROR_* | API呼出し例外。クラス名と状況を記録して調査 |

CarValueの時刻はelapsedRealtime系。Location.getTimeは壁時計、getElapsedRealtimeNanosは単調時刻としてそれぞれ保存します。精度・高度・速度・方位が欠ける場合は0で補完せずnull。mockフラグも保存します。

比較距離separation_mは、両系列がSUCCESS、RECENT、測位時刻差5秒以内、mockでない場合だけ計算します。距離差が小さいことは車位置のほうが正しい証拠ではありません。DHUからの模擬データはmock=falseで届く可能性もあるため、ホスト名と試験条件も併記します。

## 実機の比較手順と受入条件

- V01 未接続: WAITING_FOR_HOST。架空の車座標を出さない。
- V02 位置許可なし: PERMISSION_REQUIRED等。地図APIキーのエラーと混同しない。
- V03 停車・屋外・AA接続: 状態、更新頻度、CarValue時刻、provider、座標、精度、速度/方位の有無を記録。
- V04 診断OFF: コールバック数が増えず、位置購読を解除。再ONで新しいセッション。
- V05 AA切断/再接続: DISCONNECTED→再購読。旧セッションの遅延コールバックを無視。
- V06 タブレット地図を閉じる: 車地図と位置診断が継続するか確認。
- V07 地図キー未設定: 診断単体が動くか確認。車載側の設定案内が表示されても診断結果を回収可能。
- V08 更新停止/古い時刻/未実装: 各状態が分離される。時刻不明を成功とみなさない。
- V09 走行比較: 運転者は操作せず、同乗者が記録。屋外、立体道路、トンネル出入口等で二系列を照合。車のアンテナを外す等の車両改変は不要。
- V10 次工程: SDKのRoadSnappedLocationを第三系列として記録する拡張を検討。取得できた車位置をSDKへ注入できるとは仮定しない。

V01〜V10は実機未確認です。

### 結果（2026-11-25、P0 第1回）

成功／失敗／未確認／—（その層では確かめられない）。単体テスト・実機SDK（タブレット単体）・DHU・実車を分ける。

| ID | 単体テスト | 実機SDK | DHU | 実車 |
|---|---|---|---|---|
| V01 | 成功（未接続時の待機） | 未確認 | 未確認 | 未確認 |
| V02 | — | 未確認 | 未確認 | 未確認 |
| V03 | — | — | 未確認 | 未確認（AA の一覧に出ず、車載画面を起動できない。R04） |
| V04 | 成功（テストモード必須・解除） | 未確認 | 未確認 | 未確認 |
| V05 | 成功（旧 Car API の回避と切断） | — | 未確認 | 未確認 |
| V06 | — | — | 未確認 | 未確認 |
| V07 | — | 未確認 | 未確認 | 未確認 |
| V08 | 成功（古い／不明／未来の時刻、未実装の区別） | — | 未確認 | 未確認 |
| V09 | — | — | — | 未確認 |
| V10 | — | — | — | 未確認（将来の拡張） |

実車の V03〜V06 は、Android Auto の一覧に Theta Navi Hatch が出ること（R04）が前提。0.3.1 から Play の内部テストで入れて確かめる。単体テストは状態・時刻判定と購読開始条件の確認であり、CX-8から位置が返った証拠ではありません。

## 調査根拠

- Android Auto Car Hardware APIと権限: https://developer.android.com/training/cars/apps/library/car-hardware-api
- CarSensors: https://developer.android.com/reference/androidx/car/app/hardware/info/CarSensors
- CarHardwareLocation: https://developer.android.com/reference/androidx/car/app/hardware/info/CarHardwareLocation
- CarValue: https://developer.android.com/reference/androidx/car/app/hardware/common/CarValue
- SDKのcustom LocationSource制限: https://developers.google.com/maps/documentation/navigation/android-sdk/reference
- CarPlayを含む外部位置情報: https://developer.apple.com/documentation/corelocation/cllocationsourceinformation/isproducedbyaccessory

確認日: 2026-11-24。これらはAPIの存在・仕様の根拠であり、CX-8と当該タブレットの対応証明ではありません。
