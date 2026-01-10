package com.example.hagoitaandroid.model
// kotlin
data class Hit(
    val expectedTime: Long,
    val actualTime: Long,
    val offsetMillis: Long,
    val result: HitResult
)

enum class HitResult {
    PERFECT, EARLY, LATE, MISS
}

data class GameResult(
    val hits: List<Hit> = emptyList(),
    //scoreはヒットスコアから計算されたスコア
    val score: Int = 0,
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val winner: Winner? = null
)
enum class Winner {
    PLAYER,
    OPPONENT
}