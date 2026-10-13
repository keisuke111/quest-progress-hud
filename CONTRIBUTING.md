# Contributing / 開発参加・報告

English and Japanese are both welcome. / 英語・日本語のどちらでも構いません。

## Issues / Issue

Use the [issue chooser](https://github.com/keisuke111/quest-progress-hud/issues/new/choose) for bugs and feature requests. Blank issues remain available for questions or other topics. Check existing issues before posting.

不具合・機能要望は[テンプレート](https://github.com/keisuke111/quest-progress-hud/issues/new/choose)を使ってください。質問などは自由記入でも投稿できます。投稿前に既存のIssueを確認してください。

Include the versions of Quest Progress HUD, Minecraft, NeoForge, FTB Quests and your modpack, along with reproduction steps. Unknown values are okay. Logs and screenshots are public; remove passwords, tokens, private server addresses and personal information before attaching them.

HUD・Minecraft・NeoForge・FTB Quests・MODパックのバージョンと再現手順を記載してください。不明な項目は不明で構いません。ログ・スクショは公開されるため、パスワード・トークン・非公開のサーバーアドレス・個人情報は削除してください。

## Pull requests / PR

Fork the repository, create a branch, and open a PR targeting main. Keep each PR focused on one change. For a substantial feature or a change to quest counting, discuss it in an issue first. Explain the problem, the resulting behavior and validation in the PR template.

Forkしたリポジトリでブランチを作り、main宛てにPRを提出してください。1つのPRは1つの変更に絞ります。大きな機能追加やクエストの数え方の変更は、先にIssueで相談してください。PRには問題・変更後の動作・確認結果を記載してください。

Use Java 21 and Gradle 9.2.1. Run `gradle build` to compile and run the layout, progress-tracking, configuration, editor-state, chapter-scope and toggle-input checks. For user-visible changes, also test in Minecraft and describe the exact environment. Add or adjust checks where they verify changed behavior. State clearly when in-game testing has not been performed.

Java 21とGradle 9.2.1を使用します。`gradle build`でビルドとレイアウト・進捗取得・設定・編集状態・チャプター集計・キー入力の確認を実行できます。表示や動作を変えた場合はゲーム内でも確認し、環境を記載してください。変更した動作を確認する必要がある場合は検証も更新してください。ゲーム内未確認なら、その旨を記載します。

The [README](README.md) describes current compatibility and counting rules. Preserve one-second refresh behavior, existing settings compatibility and the current all-registered-quests denominator unless the change is explicitly agreed. Include screenshots for layout changes and update both English and Japanese translations when changing settings text. Leave version bumps and release publication to the maintainer unless requested.

対応環境と集計仕様は[README](README.md)を参照してください。合意した変更を除き、1秒更新・既存設定との互換性・全登録クエストを数える分母を維持します。見た目の変更にはスクショを添付し、設定文言の変更は英語と日本語の両方を更新してください。依頼がなければバージョン変更と公開作業は管理者が行います。

## License / ライセンス

The project remains All Rights Reserved; see [LICENSE](LICENSE). These contribution guidelines do not change the license or add redistribution permissions.

本プロジェクトはAll Rights Reservedです。[LICENSE](LICENSE)を参照してください。この案内によってライセンスや再配布の許可は変更されません。
