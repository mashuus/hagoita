package com.example.hagoitaandroid.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hagoitaandroid.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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

    private var botAccuracy: Float = 0.7f
    fun setDifficulty(accuracy: Float) { this.botAccuracy = accuracy }

    private var gameJob: Job? = null
    private var isPlayerTurn = true

    // --- センサー・サウンド用 ---
    private var sensorManager: SensorManager? = null
    private val SHAKE_THRESHOLD = 15.0f
    private var lastShakeTime: Long = 0

    private var flowPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private var hitSoundId: Int = 0

    fun initSystem(context: Context, manager: SensorManager) {
        sensorManager = manager
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
        hitSoundId = soundPool?.load(context, R.raw.hit, 1) ?: 0

        flowPlayer?.release()
        flowPlayer = MediaPlayer.create(context, R.raw.flow).apply {
            isLooping = true
        }
    }

    private fun playHitSound() {
        soundPool?.play(hitSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    private fun startFlowSound() {
        flowPlayer?.apply {
            try {
                if (isPlaying) pause()
                seekTo(0)
                setVolume(1.0f, 1.0f)
                start()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun stopFlowSound() {
        try {
            if (flowPlayer?.isPlaying == true) {
                flowPlayer?.pause()
                flowPlayer?.seekTo(0)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun resetGame() {
        gameJob?.cancel()
        gameJob = null
        stopFlowSound()
        isPlayerTurn = false
        _uiState.update {
            it.copy(isRallyActive = false, targetPos = null, ballPos = Position(0.5f, 0.75f))
        }
    }

    override fun onCleared() {
        super.onCleared()
        resetGame()
        sensorManager?.unregisterListener(this)
        flowPlayer?.release()
        soundPool?.release()
    }

    fun startGame() {
        resetGame()
        _uiState.value = GameUiState()
        isPlayerTurn = true
        spawnTarget(isPlayerSide = true)
    }

    fun onPlayerAction() {
        // ラリー中でない、または相手のターンの時は無視
        if (_uiState.value.isGameOver || !isPlayerTurn) return

        // ホーム画面等での誤作動防止
        val currentState = _uiState.value
        if (currentState.targetPos == null && !currentState.isRallyActive) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShakeTime < 500) return
        lastShakeTime = currentTime

        // 自分が打つ音を鳴らす
        playHitSound()
        isPlayerTurn = false
        _uiState.update { it.copy(isRallyActive = true) }

        // ラリーのループを開始（まだ動いていない場合のみ）
        if (gameJob == null || gameJob?.isCompleted == true) {
            runRallyLoop()
        }
    }

    private fun runRallyLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            try {
                var currentToEnemy = true // 最初はプレイヤーから敵へ飛ばす

                while (isActive) {
                    // 1. ターゲット（着弾地点）の決定
                    spawnTarget(isPlayerSide = !currentToEnemy)
                    val target = _uiState.value.targetPos ?: break
                    val startBallPos = _uiState.value.ballPos
                    val startPlayerPos = _uiState.value.playerPos
                    val startEnemyPos = _uiState.value.enemyPos

                    // 2. 移動音開始
                    startFlowSound()

                    // 神難易度なら速くする
                    val totalDuration = if (botAccuracy >= 0.99f) 800L else 2000L
                    val steps = (totalDuration / 20L).toInt()

                    for (i in 1..steps) {
                        delay(20L)
                        val progress = i.toFloat() / steps

                        // 各種座標の更新
                        val currentBallX = startBallPos.x + (target.x - startBallPos.x) * progress
                        val currentBallY = startBallPos.y + (target.y - startBallPos.y) * progress
                        val currentEnemyX = if (currentToEnemy) startEnemyPos.x + (target.x - startEnemyPos.x) * progress else startEnemyPos.x
                        val currentEnemyY = if (currentToEnemy) startEnemyPos.y + (target.y - startEnemyPos.y) * progress else startEnemyPos.y
                        val currentPlayerX = if (!currentToEnemy) startPlayerPos.x + (target.x - startPlayerPos.x) * progress else startPlayerPos.x
                        val currentPlayerY = if (!currentToEnemy) startPlayerPos.y + (target.y - startPlayerPos.y) * progress else startPlayerPos.y

                        _uiState.update {
                            it.copy(
                                ballPos = Position(currentBallX, currentBallY),
                                enemyPos = Position(currentEnemyX, currentEnemyY),
                                playerPos = Position(currentPlayerX, currentPlayerY)
                            )
                        }
                    }

                    // 3. 到着したので音を止める
                    stopFlowSound()

                    if (currentToEnemy) {
                        // --- 敵の陣地に到着 ---
                        delay(300)
                        if (Random.nextFloat() < botAccuracy) {
                            playHitSound() // 敵が打ち返す音
                            currentToEnemy = false // 次はプレイヤーへ
                        } else {
                            processScore(isPlayerWin = true) // 敵のミス
                            break
                        }
                    } else {
                        // --- 自分の陣地に到着 ---
                        isPlayerTurn = true
                        val playerDelay = if (botAccuracy >= 0.99f) 300L else 1000L

                        // プレイヤーが振るのを待つ
                        val waitStartTime = System.currentTimeMillis()
                        while (isPlayerTurn && System.currentTimeMillis() - waitStartTime < playerDelay) {
                            delay(10)
                        }

                        if (isPlayerTurn) {
                            processScore(isPlayerWin = false) // プレイヤーのミス
                            break
                        } else {
                            // 成功時、onPlayerAction経由で playHitSound が鳴り、isPlayerTurnがfalseになっている
                            currentToEnemy = true
                        }
                    }
                }
            } finally {
                stopFlowSound()
            }
        }
    }

    private fun spawnTarget(isPlayerSide: Boolean) {
        val nextY = if (isPlayerSide) Random.nextFloat() * 0.15f + 0.7f else Random.nextFloat() * 0.15f + 0.15f
        val nextX = Random.nextFloat() * 0.6f + 0.2f
        _uiState.update { it.copy(targetPos = Position(nextX, nextY)) }
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

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = sqrt(x * x + y * y + z * z)
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
