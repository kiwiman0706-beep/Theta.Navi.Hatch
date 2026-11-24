# Theta.Navi.Hatch — Claude Codeへの最終引き継ぎ
更新: 2026-11-24 JST。パッケージ資料改訂: handoff-r2。Theta APK自体は0.2.1のまま。

## 確定した条件
- 車: Mazda CX-8、非タッチMazda Connect、ジョグダイヤル。有線CarPlayのみ。
- 車載タブレット: **ALLDOCUBE iPlay60 Pro。Pad ProでもMini Proでもない。SIM内蔵**。
- タブレットのSIM回線を地図・Gemini・Tailscaleに、Wi-FiをCarPlay無線化アダプターに使う構成を検証する。
- ユーザーは小型のCarPlay無線化アダプターを購入してよいと明言。購入操作は依頼されていない。
- Ottocast P2 Plusも所有。ただしタブレットを主役にし、機器を増やし過ぎない。
- Android Autoを本線として保つ。CarPlay送信機化はPlan Bで、アダプターを挟む案を許容。
- Androidの実バージョン、CX-8年式/マツコネ版、SIM通信状態、Tailscaleの接続先は未取得。

## 読む順
1. このファイル、CLAUDE.md、README_JA.md、VALIDATION.md
2. docs/USER_REQUIREMENTS_JA.md（会話の要件）
3. docs/CARPLAY_PLAN_B_JA.md（方式、採用候補、ソースの読み所）
4. docs/ADAPTER_AND_DEVICE_JA.md（買い間違い防止、端末条件）
5. docs/SIM_TAILSCALE_JA.md（SIM+Wi-Fi+VPNの共存）
6. docs/CARPLAY_TEST_PLAN_JA.md（段階的試験）
7. docs/ROADMAP_JA.md、既存API設定/ADB/車位置診断資料
8. research/README_JA.md、research/SOURCES_JA.md、research/upstream/配下

## 最初にすること
- 対象端末をADBで読み取り診断。root化やOS書換えから始めない。
- 現APK0.2.1のAAテストを維持する。
- Plan Bは未改造CarDeck→アダプター接続→HID→二画面→Theta統合の順。
- 物理ダイヤルや車画面はユーザーが停車中に確認。ADB入力やログだけで実車成功にしない。
- このZIPは準備・研究資料まで。CarDeck互換アダプターやCX-8実車成功を保証しない。

## 入っているもの
既存Theta APK・全ソース・試作署名鍵・25テストの既存記録・ロードマップを維持。
CarDeck/CarPToCarLの上流ソースZIP、公開APK、リリース情報・コミット情報・ライセンス、Pi2Carplayの関連ソース抜粋と資料、取得できたShizuku APKを同梱。
research/CONTENTS_AND_CHECKS.jsonとMANIFEST_SHA256.txtが実際の収録物の正本。
Pi用の約1.63GB OSイメージとLinuxカーネル全体は用途が異なり巨大なので省略し、固定URL・取得スクリプトを用意。必要時だけ取得する。

ソースのmain固定コミットと公開APKのreleaseタグは別物。改造する場合はreleaseタグをcheckoutし、mainとの差分を確認する。
APIキー、ユーザーのADB/Tailscale認証、実機ログは含まない。第三者APKは上流配布物で、Thetaに統合済みではない。
