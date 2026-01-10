package com.example.hagoitaandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

// 座標管理用
data class Position(val x: Float, val y: Float)

// UIの状態（画面に表示するデータ）
data class GameUiState(
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val ballPos: Position = Position(0.5f, 0.75f),
    val playerPos: Position = Position(0.5f, 0.75f),
    val enemyPos: Position = Position(0.5f, 0.25f),
    val targetPos: Position? = null,
    val isGameOver: Boolean = false,
    val winnerLabel: String = ""
)

class GameViewModel : ViewModel() {
    private val winScore = 7
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameJob: Job? = null
    private var isPlayerTurn = true // true: プレイヤーの番, false: Botの番

    // ゲーム開始（MainActivityのonClickから呼ばれる）
    fun startGame() {
        gameJob?.cancel()
        _uiState.value = GameUiState()
        isPlayerTurn = true
        spawnTarget(isPlayerSide = true) // 自分の前にターゲットを出す
    }

    // プレイヤーがスイング（振る）した時の処理
    fun onPlayerAction() {
        if (!isPlayerTurn || _uiState.value.isGameOver) return

        // プレイヤーが打ったので次はBotの番
        isPlayerTurn = false
        startBallMovement(toEnemy = true)
    }

    // ターゲットをランダムに配置
    private fun spawnTarget(isPlayerSide: Boolean) {
        val nextY = if (isPlayerSide) {
            Random.nextFloat() * 0.15f + 0.7f // 下側（自分）
        } else {
            Random.nextFloat() * 0.15f + 0.15f // 上側（敵）
        }
        val nextX = Random.nextFloat() * 0.6f + 0.2f
        _uiState.update { it.copy(targetPos = Position(nextX, nextY)) }
    }

    // ボールの移動アニメーション
    private fun startBallMovement(toEnemy: Boolean) {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            // 1. 次の着弾地点（ターゲット）を決定
            spawnTarget(isPlayerSide = !toEnemy)
            val target = _uiState.value.targetPos ?: return@launch

            // 2. ★重要★ 「現在の座標」を移動の開始地点にする
            val startBallPos = _uiState.value.ballPos
            val startPlayerPos = _uiState.value.playerPos
            val startEnemyPos = _uiState.value.enemyPos

            val steps = 25
            for (i in 1..steps) {
                delay(25)
                val progress = i.toFloat() / steps

                // ボールの移動（現在地 -> ターゲット）
                val currentBallX = startBallPos.x + (target.x - startBallPos.x) * progress
                val currentBallY = startBallPos.y + (target.y - startBallPos.y) * progress

                // キャラクターの移動（現在地 -> ターゲット）
                // 敵へ向かうときは敵を、自分へ戻るときは自分を動かす
                val currentEnemyX = if (toEnemy) startEnemyPos.x + (target.x - startEnemyPos.x) * progress else startEnemyPos.x
                val currentEnemyY = if (toEnemy) startEnemyPos.y + (target.y - startEnemyPos.y) * progress else startEnemyPos.y

                val currentPlayerX = if (!toEnemy) startPlayerPos.x + (target.x - startPlayerPos.x) * progress else startPlayerPos.x
                val currentPlayerY = if (!toEnemy) startPlayerPos.y + (target.y - startPlayerPos.y) * progress else startPlayerPos.y

                _uiState.update {
                    it.copy(
                        ballPos = Position(currentBallX, currentBallY),
                        enemyPos = Position(currentEnemyX, currentEnemyY),
                        playerPos = Position(currentPlayerX, currentPlayerY)
                    )
                }
            }

            // 3. 到着後の判定（変更なし）
            if (toEnemy) {
                delay(200)
                if (Random.nextFloat() < 0.90f) {
                    startBallMovement(toEnemy = false) // 相手が打ち返す
                } else {
                    processScore(isPlayerWin = true) // 相手のミス
                }
            } else {
                isPlayerTurn = true
                delay(400) // プレイヤーが振るのを待つ
                if (isPlayerTurn) processScore(isPlayerWin = false) // 振らなかったらミス
            }
        }
    }

    // 得点処理
    private fun processScore(isPlayerWin: Boolean) {
        _uiState.update {
            val newPlayerScore = if (isPlayerWin) it.playerScore + 1 else it.playerScore
            val newOpponentScore = if (!isPlayerWin) it.opponentScore + 1 else it.opponentScore

            val gameOver = newPlayerScore >= winScore || newOpponentScore >= winScore
            it.copy(
                playerScore = newPlayerScore,
                opponentScore = newOpponentScore,
                isGameOver = gameOver,
                winnerLabel = if (newPlayerScore >= winScore) "あなたの勝ち！" else "相手の勝ち...",
                ballPos = Position(0.5f, 0.75f), // ボールを戻す
                targetPos = null
            )
        }

        if (!_uiState.value.isGameOver) {
            isPlayerTurn = true
            spawnTarget(isPlayerSide = true)
        }
    }

    // デバッグ用の得点操作（既存の関数）
    fun updatePlayerScore(delta: Int) {
        _uiState.update { it.copy(playerScore = (it.playerScore + delta).coerceAtLeast(0)) }
    }
    fun updateOpponentScore(delta: Int) {
        _uiState.update { it.copy(opponentScore = (it.opponentScore + delta).coerceAtLeast(0)) }
    }
}
