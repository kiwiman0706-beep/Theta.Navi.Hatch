# 同梱した調査・実験材料
取得日: 2026-11-24。ファイル一覧と実ハッシュはCONTENTS_AND_CHECKS.json / UPSTREAM_MANIFEST.json / ../MANIFEST_SHA256.txt参照。

- binaries/CarDeck-0.1.30.apk: 未改造の上流リリース。リリース記載SHA256と一致。最初に試す候補。
- binaries/CarPToCarL-1.2.85.apk: 比較用。CarLife+が別途必要。Theta向け標準手順ではインストール必須ではない。
- binaries/shizuku-*.apk: 上流リリース。実ファイルSHA256を記録。配布側ハッシュがAPIにないため独立の照合は未実施。
- upstream/cardeck、upstream/CarPToCarL: mainをコミット固定した公開リポジトリZIP、コミット/リリースJSON、ライセンスと主要文書のreading-copy。
- upstream/Pi2Carplay/selected-source: Linuxカーネル本体以外の取得スナップショット。ブリッジ、GPS、Bluetooth通話、表示、スクリプト、テスト、ライセンス。単独でPiイメージを再現できる完全セットではない。
- binaries/Pi2Carplay-*-codec-sources.tar.gz、SHA256SUMS: 上流の付随コーデックソースと検証用ハッシュ。
- upstream/Shizuku: リリースメタデータと取得できたREADME/ライセンス。

## 含めていないもの
Pi用OSイメージ（約1.63GB）とPiのLinuxカーネルを含む元ソースZIP（約316MB）は、タブレット試験には直接不要なので未同梱。OPTIONAL_DOWNLOADS.jsonとtools/fetch_optional_upstream.pyで固定版を必要時に取得できる。
Android SDK/JDK/Gradleキャッシュ、Google/Gemini APIキー、CarLife+の独自配布、ユーザー認証情報は含まない。

## ソース公開の範囲
CarDeckの公開ZIPにはarm64-v8a/libcardeck_protocol.soが含まれる一方、今回のスナップショットにRustのCargo.toml/ .rsは見つからなかった。KotlinのNativeBridgeはそのバイナリを呼ぶ。したがって「公開リポジトリの全ファイルを取得した」ことは「プロトコル実装を含む全ソースを再ビルドできる」ことではない。
HIDのraw delta/hat/buttonをKotlinへ返す入口は存在するため、その後段でTheta専用ルーティングを追加する案をまず検証する。ネイティブの解析自体を変える必要があればソース可用性を調査し、単にRustを入れれば再ビルドできると仮定しない。
Pi2CarplayもプロトコルSDKは外部バイナリ依存。各LICENSE/NOTICE/LICENSE-SCOPEを保持する。

## 検証の意味
ハッシュ一致は取得整合性を示す。実車互換性・安全性・全ソースとの再現一致の保証ではない。
第三者APKのapksignerによる証明書検証と端末実行は今回未実施。上流署名の更新とfork署名を区別し、署名相違のための無断アンインストールをしない。
これらの文書にある上流作者の実車試験は、今回のCX-8での実測ではない。
