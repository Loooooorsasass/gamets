package com.example.data.cloud

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val avatarEmoji: String,
    val scoreText: String,
    val timeSec: Int,
    val stars: Int,
    val isCurrentPlayer: Boolean = false
)

object LeaderboardManager {
    fun getLevelLeaderboard(levelNumber: Int, playerBestTimeSec: Int?, playerStars: Int): List<LeaderboardEntry> {
        val list = mutableListOf<LeaderboardEntry>()

        if (playerBestTimeSec != null && playerBestTimeSec > 0) {
            list.add(
                LeaderboardEntry(
                    rank = 1,
                    playerName = "Bạn",
                    avatarEmoji = "🚀",
                    scoreText = "${playerBestTimeSec}s (${playerStars}★)",
                    timeSec = playerBestTimeSec,
                    stars = playerStars,
                    isCurrentPlayer = true
                )
            )
        }
        return list
    }

    fun getDailyLeaderboard(playerScoreSec: Int?): List<LeaderboardEntry> {
        val list = mutableListOf<LeaderboardEntry>()

        if (playerScoreSec != null && playerScoreSec > 0) {
            list.add(
                LeaderboardEntry(
                    rank = 1,
                    playerName = "Bạn",
                    avatarEmoji = "🚀",
                    scoreText = "${playerScoreSec}s (⭐⭐⭐)",
                    timeSec = playerScoreSec,
                    stars = 3,
                    isCurrentPlayer = true
                )
            )
        }
        return list
    }
}

object DailyChallengeManager {
    fun getTodayDateKey(): String {
        val now = LocalDate.now()
        return now.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    /**
     * Kích thước mê cung cho từng lượt: Lượt 1: 14x14, Lượt 2: 15x15, Lượt 3: 16x16
     */
    fun getDailySize(stage: Int): Int {
        return when (stage) {
            1 -> 14
            2 -> 15
            3 -> 16
            else -> 14
        }
    }

    /**
     * Số bước mục tiêu chuẩn tương ứng cho từng kích thước
     */
    fun getDailyTarget(stage: Int): Int {
        return when (stage) {
            1 -> 95   // 14x14
            2 -> 110  // 15x15
            3 -> 130  // 16x16
            else -> 95
        }
    }

    /**
     * Phần thưởng Xu cho từng lượt
     */
    fun getDailyCoinsReward(stage: Int): Int {
        return when (stage) {
            1 -> 50
            2 -> 75
            3 -> 150
            else -> 50
        }
    }

    /**
     * Seed xác định cho từng ngày và từng lượt chơi (Đảm bảo tất cả người chơi cùng nhận 1 mê cung giống nhau)
     */
    fun getDailySeed(dateStr: String = getTodayDateKey(), stage: Int = 1): ULong {
        val now = try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            LocalDate.now()
        }
        val baseSeed = (now.year * 10000L + now.dayOfYear * 7919L)
        val stageSeed = baseSeed + (stage * 1337L)
        return stageSeed.toULong()
    }

    /**
     * Tính thời gian còn lại (mili-giây) cho đến 00:00:00 ngày hôm sau
     */
    fun getCooldownRemainingMillis(): Long {
        val now = java.time.LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val duration = java.time.Duration.between(now, midnight)
        return maxOf(0L, duration.toMillis())
    }

    /**
     * Định dạng thời gian đếm ngược "HH:MM:SS"
     */
    fun formatCooldown(millis: Long): String {
        val totalSec = millis / 1000
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    const val MAX_DAILY_ATTEMPTS = 3
    const val DAILY_SIZE = 14
    const val DAILY_TARGET = 95
}
