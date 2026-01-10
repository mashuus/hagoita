package com.example.hagoitaandroid.model

sealed class GameState {
    object Idle : GameState()
    data class Running(
        val startTimeMillis: Long,
        val beatIntervalMillis: Long,
        val totalBeats: Int,
        val nextBeatIndex: Int,
        val allowedWindowMillis: Long
    ) : GameState()

    data class Finished(val result: GameResult) : GameState()
}