# iPlay60 Proと無線化アダプター
調査日: 2026-11-24 JST。購入候補は互換認定ではない。

## 端末
ユーザー確定: ALLDOCUBE iPlay60 Pro（Pad Pro/Mini Proではない）、SIM内蔵。
メーカーの製品ページはHelio G99、4G LTEを記載。Android 14版と15版が存在し、14出荷機への15更新は提供されないとの注記あり。手元機のOSはADBで取得する。CarDeckの掲載条件は14以上なので、どちらもOS条件上は候補になるが、ShizukuやGPUの実動作は別。
公式: https://www.alldocube.com/jp/products/iplay60pro/
資料上のOSを端末の実測結果として扱わない。ROM/bootloader資料を探すときPad ProやMiniのものを混用しない。

## 接続構成
タブレット内CarDeck → Bluetoothで接続開始・Wi-Fiで画面等を転送 → CarPlay無線化アダプター → USB → CX-8有線CarPlay。
タブレットのSIM → 地図/Gemini/Places/Tailscale。タブレットのUSBは別途充電に使える構想。
アダプターは車のCarPlayデータ対応USB端子へ。充電専用端子では動かない。

## 比較候補
| 候補 | メーカー記載の用途 | 本件で未確認 |
|---|---|---|
| Ottocast U2AIR Pro | 純正有線CarPlayを無線化。通常対象はiPhone | CX-8年式/マツコネ版、CarDeck、HID/位置転送、再接続 |
| CarlinKit CPC200-U2W Mini | 純正有線CarPlayを無線化。純正ノブ操作の継続を記載 | 同上。ノブ継続の記載はThetaのパン/ズーム割当成功を意味しない |

https://www.ottocast.com/en-jp/products/u2-air-pro
https://www.carlinkit.com/u2w-mini
https://www.carlinkit.com/manuals_mini

どちらも通常のAndroidアプリを投影する製品ではない。「iPhone向け」と書かれているのは通常用途として正しい。今回Android側がCarPlay送信端を実装する実験であり、メーカー保証対象と見なさない。
AA専用無線化アダプター、Android画面をCarPlay受信画面にするAutoKit系USBドングル、AI Boxを買い間違えない。
現時点では上記どちらを買えばCarDeckで必ず動くという根拠はない。価格を固定せず、日本向け正規販売条件・CX-8年式対応・返品条件・技適表示・更新手段を購入時に確認する。
2機種を同時購入する必要はない。まず1個で試験し、アダプター側かCarDeck側か切り分ける。

## 注文前に記録
車の年式・Mazda Connectソフト版、現在の有線CarPlay動作（可能なら借りたiPhoneで基準試験）、USB端子、製品の正確な型番/ハード版/ファーム版。
メーカーが別マツダ車で報告した電源対策をCX-8へ無条件に流用しない。電源不足の兆候があるときだけ公式手順を確認。
アダプターがiPhoneで動いてもCarDeckで動くとは限らない。iPhoneがなければ対照試験未実施と記録し、それだけで作業全体は止めない。
