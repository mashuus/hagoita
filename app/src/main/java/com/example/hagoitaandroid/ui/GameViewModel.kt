// ファイルパス: app/src/main/java/com/example/hagoitaandroid/ui/GameViewModel.kt
package com.example.hagoitaandroid.ui

import android.util.Log // Logを使うためにインポート
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    fun startGame() {
        Log.d("GameViewModel", "ゲームが開始されました")
    }

    fun recordHit() {
        // ▼▼▼ このログがクリック時に表示されれば連携成功 ▼▼▼
        Log.d("GameViewModel", "ヒットを記録！画面がクリックされました。")
    }

    fun endGame() {
        Log.d("GameViewModel", "ゲームが終了しました")
    }
}
