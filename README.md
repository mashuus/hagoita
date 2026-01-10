# 羽子板アプリ (Hagoita)

音と振りで遊ぶ対戦型スマホゲーム。画面ではなく「音のタイミング」に合わせて端末を振るアクション性を重視しています。

## 概要
- 落下音に合わせて端末を振ると打撃音でラリーを継続。タイミングを外すとゲームオーバー。
- 終了時にラリーの精度（スイングのタイミング偏差）を表示。

## 主要機能
- 低遅延再生: SoundPool を使用した効果音
- 加速度検出: SensorManager (加速度センサー) による振動判定
- Jetpack Compose によるシンプルな UI
- MVVM 構成でセンサーとゲームロジックを分離

## 技術スタック
- Kotlin 1.9+
- Jetpack Compose (Material3)
- SoundPool, SensorManager
- Gradle (Wrapper) — Windows: gradlew.bat

## ディレクトリ例（実装予定 / 参照）
- app/src/main/java/com/example/hagoita/
  - ui/
    - MainActivity.kt 
    - HomeScreen.kt (UI: スタート画面)
    - GameScreen.kt (UI: スタート / リザルト)
    - GameViewModel.kt (ゲームロジック・状態)
    - theme/
     - Color.kt
     - Theme.kt
     - Type.kt
  - sensor/
    - ShakeDetector.kt (閾値判定)
  - audio/
    - SoundManager.kt (SoundPool 管理)
  - model/
    - GameResult.kt (スコア・タイミング情報)
    - GameState.kt (ゲーム状態)
