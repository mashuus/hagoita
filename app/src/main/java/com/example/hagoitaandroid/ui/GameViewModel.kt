package com.example.hagoitaandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hagoitaandroid.model.GameResult
import com.example.hagoitaandroid.model.GameState
import com.example.hagoitaandroid.model.Hit
import com.example.hagoitaandroid.model.HitResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

data class GameUiState(
    val playerScore: Int = 0,
    val opponentScore: Int = 0
)

class GameViewModel : ViewModel() {
    private val winScore = 7

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _state = MutableStateFlow<GameState>(GameState.Idle)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _currentResult = mutableListOf<Hit>()
    private var currentAllowedWindowMillis: Long = 500L // ±0.5s をデフォルトとする

    // サーブ待ちフラグ (ミス後はプレイヤーがサーブを行う)
    private var awaitingServe: Boolean = true

    // 次に期待される打ち手 (true = player の打ち番、false = bot の打ち番)
    private var nextIsPlayer: Boolean = true

    // 同一ビートの重複処理防止
    private var lastHandledExpectedTime: Long? = null

    // Bot の成功確率 (0.0〜1.0)
    var botAccuracy: Double = 0.9

    fun updatePlayerScore(delta: Int) {
        _uiState.update { it.copy(playerScore = (it.playerScore + delta).coerceAtLeast(0)) }
    }

    fun updateOpponentScore(delta: Int) {
        _uiState.update { it.copy(opponentScore = (it.opponentScore + delta).coerceAtLeast(0)) }
    }

    fun startGame(
        startTimeMillis: Long = System.currentTimeMillis(),
        beatIntervalMillis: Long = 1000L,
        totalBeats: Int = 30,
        allowedWindowMillis: Long = 500L // MISS 判定は +/-500ms
    ) {
        currentAllowedWindowMillis = allowedWindowMillis
        _currentResult.clear()
        lastHandledExpectedTime = null
        awaitingServe = true
        nextIsPlayer = true
        _state.value = GameState.Running(
            startTimeMillis = startTimeMillis,
            beatIntervalMillis = beatIntervalMillis,
            totalBeats = totalBeats,
            nextBeatIndex = 0,
            allowedWindowMillis = allowedWindowMillis
        )
        _uiState.value = GameUiState()
    }

    /**
     * プレイヤーの振動検出（UI / センサー側から呼ぶ）
     * サーブ待ちならサーブとしてラリー開始、それ以外はプレイヤーの打ちとして判定する。
     */
    fun onShakeDetected(timestampMillis: Long = System.currentTimeMillis()) {
        val s = _state.value as? GameState.Running ?: return

        // サーブ待ちならプレイヤーサーブとして開始
        if (awaitingServe && nextIsPlayer) {
            // サーブ時刻を基準としてラリー開始 (nextBeatIndex = 0 はこのサーブ)
            _state.value = s.copy(startTimeMillis = timestampMillis, nextBeatIndex = 0)
            awaitingServe = false
            // サーブはプレイヤーの打ち -> 処理して Bot の応答をスケジュール
            processHit(expected = timestampMillis, actual = timestampMillis, isPlayerHit = true)
            return
        }

        // 通常のプレイヤーの打ち（プレイヤーの打ち番であることを期待）
        if (!nextIsPlayer) {
            // 今は bot の番なのでユーザーの不正な入力は無視
            return
        }

        val expected = s.startTimeMillis + s.nextBeatIndex * s.beatIntervalMillis
        if (lastHandledExpectedTime == expected) return
        processHit(expected, timestampMillis, isPlayerHit = true)
    }

    // Bot の自動応答を開始（内部で遅延して processHit を呼ぶ）
    private fun scheduleBotResponse(beatIntervalMillis: Long, expectedTime: Long) {
        // Bot は expectedTime に対して遅延して打つ（少し誤差を入れる）
        viewModelScope.launch {
            // Bot の反応遅延をランダム化（例 0ms〜150ms）
            val jitter = Random.nextLong(0, 150)
            val botActual = expectedTime + jitter
            // 簡易的に Bot の成功/失敗を確率判定
            val willHit = Random.nextDouble() < botAccuracy
            val actualTime = if (willHit) botActual else expectedTime + currentAllowedWindowMillis + 1000L // 明確に MISS にする
            // 少し待ってから処理（実時間シミュレーション）
            val delayMillis = (actualTime - System.currentTimeMillis()).coerceAtLeast(0L)
            if (delayMillis > 0) delay(delayMillis)
            processHit(expectedTime, actualTime, isPlayerHit = false)
        }
    }

    // 共通のヒット処理（プレイヤーまたは Bot）
    private fun processHit(expected: Long, actual: Long, isPlayerHit: Boolean) {
        val s = _state.value as? GameState.Running ?: return
        // 重複処理防止
        if (lastHandledExpectedTime == expected) return

        val offset = actual - expected
        val absOffset = abs(offset)

        val hitResult = when {
            absOffset <= 50 -> HitResult.PERFECT
            absOffset <= s.allowedWindowMillis -> if (offset < 0) HitResult.EARLY else HitResult.LATE
            else -> HitResult.MISS
        }

        // スコア計算（ヒットスコアは補助、勝敗は player/opponent score）
        val addScore = when (hitResult) {
            HitResult.PERFECT -> 100
            HitResult.EARLY, HitResult.LATE -> max(0, (s.allowedWindowMillis - absOffset).toInt())
            HitResult.MISS -> 0
        }

        // 即時スコア反映
        if (hitResult == HitResult.MISS) {
            if (isPlayerHit) {
                // プレイヤーがミス -> Bot に1点
                updateOpponentScore(1)
            } else {
                // Bot がミス -> プレイヤーに1点
                updatePlayerScore(1)
            }
            // ミスが発生したらラリーは終了し、プレイヤーのサーブ待ちに戻る
            awaitingServe = true
            nextIsPlayer = true
        } else {
            // ミスでなければヒットスコアを付与（プレイヤーのみ）
            if (isPlayerHit && addScore > 0) {
                updatePlayerScore(addScore)
            }
            // ラリー継続: 次は相手の打ち番にする
            nextIsPlayer = !isPlayerHit
            awaitingServe = false
        }

        _currentResult.add(Hit(expected, actual, offset, hitResult))
        lastHandledExpectedTime = expected

        // 勝利チェック
        val ui = _uiState.value
        if (ui.playerScore >= winScore || ui.opponentScore >= winScore) {
            finishGame()
            return
        }

        // 次のビートが存在するかチェックし、Bot の番なら自動応答をスケジュール
        val nextIndex = s.nextBeatIndex + 1
        if (nextIndex >= s.totalBeats) {
            finishGame()
            return
        }

        // もしラリー継続なら nextBeatIndex を進め、Bot の打ち番ならスケジュールする
        _state.value = s.copy(nextBeatIndex = nextIndex)

        val nextExpected = s.startTimeMillis + nextIndex * s.beatIntervalMillis
        if (!nextIsPlayer && !awaitingServe) {
            // Bot の番なら Bot の応答をスケジュール
            scheduleBotResponse(s.beatIntervalMillis, nextExpected)
        }
    }

    private fun finishGame() {
        viewModelScope.launch {
            val totalScore = _currentResult.sumOf { hit ->
                when (hit.result) {
                    HitResult.PERFECT -> 100
                    HitResult.EARLY, HitResult.LATE -> max(0, (currentAllowedWindowMillis - abs(hit.offsetMillis)).toInt())
                    HitResult.MISS -> 0
                }
            }
            val ui = _uiState.value

            val winner = when {
                ui.playerScore >= winScore && ui.playerScore >= ui.opponentScore -> com.example.hagoitaandroid.model.Winner.PLAYER
                ui.opponentScore >= winScore && ui.opponentScore > ui.playerScore -> com.example.hagoitaandroid.model.Winner.OPPONENT
                ui.playerScore > ui.opponentScore -> com.example.hagoitaandroid.model.Winner.PLAYER
                ui.opponentScore > ui.playerScore -> com.example.hagoitaandroid.model.Winner.OPPONENT
                else -> null
            }

            _state.value = GameState.Finished(
                GameResult(
                    hits = _currentResult.toList(),
                    score = totalScore,
                    playerScore = ui.playerScore,
                    opponentScore = ui.opponentScore,
                    winner = winner
                )
            )
            _currentResult.clear()
            lastHandledExpectedTime = null
            awaitingServe = true
            nextIsPlayer = true
        }
    }

    fun reset() {
        _state.value = GameState.Idle
        _currentResult.clear()
        currentAllowedWindowMillis = 500L
        lastHandledExpectedTime = null
        awaitingServe = true
        nextIsPlayer = true
        _uiState.value = GameUiState()
    }
}