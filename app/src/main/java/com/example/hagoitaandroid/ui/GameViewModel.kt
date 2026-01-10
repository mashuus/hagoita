package com.example.hagoitaandroid.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// UIの状態を表すデータクラス
data class GameUiState(
    val playerScore: Int = 0,
    val opponentScore: Int = 0
)

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    // プレイヤーの点数操作
    fun updatePlayerScore(delta: Int) {
        _uiState.update { it.copy(playerScore = (it.playerScore + delta).coerceAtLeast(0)) }
    }

    // 相手の点数操作
    fun updateOpponentScore(delta: Int) {
        _uiState.update { it.copy(opponentScore = (it.opponentScore + delta).coerceAtLeast(0)) }
    }
}
