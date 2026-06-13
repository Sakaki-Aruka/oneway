# Modrinth アップロードCLIツールMVP

あくまでもアップロードに絞り、削除、変更はサポートしない

言語はGo

- 現在のバージョンとの衝突判定
- 現在リリース済みバージョン取得
- バージョン作成関連機能
    - ファイルアップロード
    - changelog追加
    - 対応pf、ver記載
    - 依存関係指定
    - リリース状態指定
- メインdesc更新

---

# 次期リリース追加要素

- テンプレートからバージョン番号指定
    - 置き換え部分指定と更新

---

# 設計

### 衝突判定

1. リリース済みバージョン文字列取得
2. バージョン文字列と比較

### リリース済みバージョン取得

1. リリース済みバージョン文字列取得

### バージョンアップロード

-

## API エンドポイント

- [リリース済みバージョン文字列の取得](https://docs.modrinth.com/api/operations/getprojectversions/)
    - [`https://api.modrinth.com/v2/project/{id|slug}/version`](https://api.modrinth.com/v2/project/%7Bid%7Cslug%7D/version)
- [バージョン作成](https://docs.modrinth.com/api/operations/createversion/)
    - [`https://api.modrinth.com/v2/version`](https://api.modrinth.com/v2/version)

### コマンド

- `oneway not-exists "PROJECT_NAME" "VERSION"` : バージョン衝突判定
    - 存在する場合はコード 2 で終了
    - その他エラーはコード 3 以降で終了
        - バージョン名のパースは部分パースにして、変更に強く軽い構造にする
- `oneway version "PROJECT_NAME" "VERSION"` : 新規バージョン作成
    - `—-jar ファイルパス`: アップロードする jar ファイルのパス (複数OK)
    - `-v バージョン番号` : バージョン番号
    - `--versionname バージョン名` : バージョン名 (デフォルトはバージョン番号と同じ)
    - `—-changelog ファイルパス` : changelog のファイルパス
    - `-d V_バージョンID/P_プロジェクトID/F_ファイル名/T_依存タイプ` : 依存関係 (複数OK)
    - `-g ゲームバージョン(カンマ区切り)` : 対応ゲームバージョン
    - `--gameversions ゲームバージョン|ゲームバージョン` : 対応ゲームバージョンの範囲指定
    - `-l プラットフォーム(カンマ区切り)` : 対応プラットフォーム
    - `--featured` : featured フラグ(プロジェクト内での注目バージョンとしてマークするか)
    - `--status ステータス` : ステータス(公開状態の設定、scheduled 以外)
    - `-p プロジェクトID` : プロジェクトID

## ToDo

- [ ]  バージョンチェックのリクエストjson組立てる関数作成
- [ ]  APIドキュメント通りにリクエストを組み立てて確認できるか
- [ ]  マルチパートアップロードでApacheのhttpライブラリを使ってみる