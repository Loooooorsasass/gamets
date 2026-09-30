package com.looooorsasass.gamets.data.model

/**
 * Cấu trúc dữ liệu lưu trữ thông tin và thống kê của trò chơi.
 * Tuân thủ mô hình Immutable Data Class trong Kotlin, đảm bảo an toàn luồng và tính toàn vẹn dữ liệu.
 */
data class GameStats(
    val currentLevel: Int = 1,
    val highestClearedLevel: Int = 0,
    val totalScore: Long = 0L,
    val totalStars: Int = 0,
    val coins: Int = 0,
    val keys: Int = 0,
    val shields: Int = 0,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val bestTimeSeconds: Int = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
) {
    /**
     * Tỷ lệ chiến thắng tính theo phần trăm (0.0% -> 100.0%)
     */
    val winRatePercent: Float
        get() = if (gamesPlayed > 0) {
            (gamesWon.toFloat() / gamesPlayed.toFloat()) * 100f
        } else {
            0f
        }

    /**
     * Kiểm tra người chơi có phải tân thủ không
     */
    val isNewPlayer: Boolean
        get() = gamesPlayed == 0 && highestClearedLevel == 0

    companion object {
        fun initial(): GameStats = GameStats(
            currentLevel = 1,
            highestClearedLevel = 0,
            totalScore = 0L,
            totalStars = 0,
            coins = 100, // Thưởng khởi đầu cho người chơi mới
            keys = 3,
            shields = 2,
            gamesPlayed = 0,
            gamesWon = 0,
            bestTimeSeconds = 0,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }
}
