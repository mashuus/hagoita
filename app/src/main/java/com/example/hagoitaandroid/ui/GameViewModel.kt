package com.example.hagoitaandroid.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hagoitaandroid.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

// 座標管理用
data class Position(val x: Float, val y: Float)

// UIの状態
data class GameUiState(
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val ballPos: Position = Position(0.5f, 0.75f),
    val playerPos: Position = Position(0.5f, 0.75f),
    val enemyPos: Position = Position(0.5f, 0.25f),
    val targetPos: Position? = null,
    val isGameOver: Boolean = false,
    val winnerLabel: String = "",
    val isRallyActive: Boolean = false
)

class GameViewModel : ViewModel(), SensorEventListener {
    private val winScore = 7
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameJob: Job? = null
    private var isPlayerTurn = true

    // --- センサー用設定 ---
    private var sensorManager: SensorManager? = null
    private val SHAKE_THRESHOLD = 15.0f // スイング判定の閾値（適宜調整）
    private var lastShakeTime: Long = 0

    // --- 音声用プロパティ ---
    private var hitPlayer: MediaPlayer? = null
    private var flowPlayer: MediaPlayer? = null

    // センサーと音声を初期化
    fun initSystem(context: Context, manager: SensorManager) {
        // センサー初期化
        sensorManager = manager
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        // 音声初期化 (重複作成防止のため、一旦releaseしてから作成)
        hitPlayer?.release()
        flowPlayer?.release()
        hitPlayer = MediaPlayer.create(context, R.raw.hit)
        flowPlayer = MediaPlayer.create(context, R.raw.flow).apply {
            isLooping = true // 移動音はループ再生
        }
    }

    // ヒット音再生
    private fun playHitSound() {
        hitPlayer?.apply {
            try {
                if (isPlaying) {
                    pause() // 一旦止める
                }
                seekTo(0) // 最初に戻す
                start()   // 再生
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 移動音開始
    private fun startFlowSound() {
        flowPlayer?.apply {
            try {
                if (isPlaying) {
                    pause()
                }
                seekTo(0) // 毎回最初から再生させる
                // 音量も確実に1.0に戻す
                setVolume(1.0f, 1.0f)
                start()
            } catch (e: Exception) {
                android.util.Log.e("GameViewModel", "Flow再生エラー: ${e.message}")
            }
        }
    }

    // 移動音停止
    private fun stopFlowSound() {
        if (flowPlayer?.isPlaying == true) {
            flowPlayer?.pause()
            flowPlayer?.seekTo(0)
        }
    }

    // ゲームを完全にリセット（ホームに戻る時に呼ぶ）
    fun resetGame() {
        // 1. ラリーの計算（コルーチン）を即座にキャンセルする
        gameJob?.cancel()
        gameJob = null

        // 2. 移動音 (flowPlayer) を確実に停止する
        try {
            flowPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
                player.seekTo(0)
                player.setVolume(0f, 0f)
            }
        } catch (e: Exception) {
            android.util.Log.e("GameViewModel", "Error stopping flowPlayer: ${e.message}")
        }

        // 3. 打撃音 (hitPlayer) も停止する
        try {
            hitPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
                player.seekTo(0)
                player.setVolume(0f, 0f)
            }
        } catch (e: Exception) {
            android.util.Log.e("GameViewModel", "Error stopping hitPlayer: ${e.message}")
        }

        // 4. フラグを折る
        _uiState.update { it.copy(isRallyActive = false) }
        isPlayerTurn = true
    }

    override fun onCleared() {
        super.onCleared()
        resetGame()
        sensorManager?.unregisterListener(this)
        hitPlayer?.release()
        flowPlayer?.release()
        hitPlayer = null
        flowPlayer = null
    }

    // ゲーム開始
    fun startGame() {
        resetGame()
        // 音量を元に戻す
        flowPlayer?.setVolume(1.0f, 1.0f)
        hitPlayer?.setVolume(1.0f, 1.0f)
        _uiState.value = GameUiState()
        isPlayerTurn = true
        spawnTarget(isPlayerSide = true)
    }

    // プレイヤーのアクション（ボタンまたはセンサーから呼ばれる）
    fun onPlayerAction() {
        if (!isPlayerTurn || _uiState.value.isGameOver) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShakeTime < 500) return
        lastShakeTime = currentTime

        playHitSound()

        isPlayerTurn = false
        _uiState.update { it.copy(isRallyActive = true) }
        startBallMovement(toEnemy = true)
    }

    private fun spawnTarget(isPlayerSide: Boolean) {
        val nextY = if (isPlayerSide) {
            Random.nextFloat() * 0.15f + 0.7f
        } else {
            Random.nextFloat() * 0.15f + 0.15f
        }
        val nextX = Random.nextFloat() * 0.6f + 0.2f
        _uiState.update { it.copy(targetPos = Position(nextX, nextY)) }
    }

    // ボールの移動アニメーション（2秒かけて到達するように調整）
    private fun startBallMovement(toEnemy: Boolean) {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            try {
                spawnTarget(isPlayerSide = !toEnemy)
                val target = _uiState.value.targetPos ?: return@launch

                val startBallPos = _uiState.value.ballPos
                val startPlayerPos = _uiState.value.playerPos
                val startEnemyPos = _uiState.value.enemyPos

                // --- 移動音を開始 ---
                delay(10)
                startFlowSound()

                val totalDuration = 2000L
                val frameDelay = 20L
                val steps = (totalDuration / frameDelay).toInt()

                for (i in 1..steps) {
                    delay(frameDelay)
                    val progress = i.toFloat() / steps

                    val currentBallX = startBallPos.x + (target.x - startBallPos.x) * progress
                    val currentBallY = startBallPos.y + (target.y - startBallPos.y) * progress

                    // キャラクターの移動計算（省略せずに記述）
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

                stopFlowSound()

                if (toEnemy) {
                    delay(300) // 打ち返しまでの「間」
                    if (Random.nextFloat() < 0.90f) {
                        playHitSound() // 打撃音
                        startBallMovement(toEnemy = false)
                    } else {
                        processScore(isPlayerWin = true)
                    }
                } else {
                    isPlayerTurn = true
                    delay(1000)
                    if (isPlayerTurn) processScore(isPlayerWin = false)
                }
            } finally {
                // コルーチンがキャンセルされた場合（ラリー終了時など）も確実に音を止める
                stopFlowSound()
            }
        }
    }

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
                ballPos = Position(0.5f, 0.75f),
                targetPos = null,
                isRallyActive = false
            )
        }

        if (!_uiState.value.isGameOver) {
            isPlayerTurn = true
            spawnTarget(isPlayerSide = true)
        }
    }

    // --- センサーイベントの処理 ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // 加速度の大きさを計算
            val acceleration = sqrt(x * x + y * y + z * z)

            // 閾値を超えたら「振った」とみなす
            if (acceleration > SHAKE_THRESHOLD) {
                onPlayerAction()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun updatePlayerScore(delta: Int) {
        _uiState.update { it.copy(playerScore = (it.playerScore + delta).coerceAtLeast(0)) }
    }
    fun updateOpponentScore(delta: Int) {
        _uiState.update { it.copy(opponentScore = (it.opponentScore + delta).coerceAtLeast(0)) }
    }
}