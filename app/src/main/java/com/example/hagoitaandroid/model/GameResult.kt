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
    val score: Int = 0
)