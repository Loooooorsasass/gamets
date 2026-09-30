package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_progress")
data class GameProgressEntity(
    @PrimaryKey val id: Int = 1,
    val highestCleared: Int = 0,
    val totalStars: Int = 0,
    val coins: Int = 100,
    val gems: Int = 10,
    val impossibleCleared: Int = 0,
    val superCleared: Boolean = false,
    val currentSkinId: String = "classic_blue",
    val currentMazeThemeId: String = "classic_black",
    val unlockedSkins: String = "classic_blue",
    val unlockedThemes: String = "classic_black",
    val isVipNoAds: Boolean = false,
    val hintCount: Int = 1, // Tặng 1 lượt gợi ý
    val skipTokens: Int = 1, // Tặng 1 lượt bỏ qua
    val lastSkipAdTimestamp: Long = 0L,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val language: String = "vi",
    val lastCloudSyncTime: Long = 0L,
    val accumulatedRewardedAdViews: Int = 0,
    val vipTier: Int = 0, // 0: Không VIP, 1: VIP 1, 2: VIP 2, 3: VIP 3
    val vipExpiresAtMillis: Long = 0L, // Timestamp hết hạn, VIP 3 = Long.MAX_VALUE
    val vip1DisableBanner: Boolean = true, // Tùy chọn VIP 1: tắt banner
    val vip1DisableInterstitial: Boolean = true, // Tùy chọn VIP 1: tắt quảng cáo sau ván
    val oneLineHighestUnlocked: Int = 0, // Màn vẽ 1 nét cao nhất đã mở khóa
    val oneLineLastPlayed: Int = 0, // Màn vẽ 1 nét chơi gần nhất
    val hintAdViewsToday: Int = 0, // Số lượt xem quảng cáo gợi ý trong ngày
    val lastHintAdDate: Long = 0L, // Thời điểm xem quảng cáo gợi ý gần nhất
    val dailyDate: String = "", // Ngày thử thách hàng ngày (YYYY-MM-DD)
    val dailyStage: Int = 1, // Lượt chơi hiện tại (1: 14x14, 2: 15x15, 3: 16x16, 4: Hoàn thành cả 3)
    val dailyAttemptsUsed: Int = 0, // Số lượt chơi đã dùng trong ngày (tối đa 3)
    val dailyStage1Time: Int = 0, // Thời gian hoàn thành lượt 1 (giây)
    val dailyStage2Time: Int = 0, // Thời gian hoàn thành lượt 2 (giây)
    val dailyStage3Time: Int = 0, // Thời gian hoàn thành lượt 3 (giây)
    val dailyAllCompleted: Boolean = false, // Đã hoàn thành cả 3 lượt hôm nay chưa
    val hasSelectedInitialLanguage: Boolean = false, // Đã chọn ngôn ngữ khi mở game lần đầu chưa
    val keys: Int = 0, // Không tặng miễn phí Chìa Khóa khi bắt đầu
    val shieldCount: Int = 0, // Không tặng miễn phí Khiên khi bắt đầu (Đồ dùng 1 lần mất ngay sau khi kích hoạt)
    val wolfHighestCleared: Int = 0, // Màn Sói Đuổi cao nhất đã vượt qua (1..26 tương ứng 5x5..30x30)
    val equippedGearId: String = "", // Không tặng miễn phí trang bị khi bắt đầu
    val unlockedGears: String = "", // Danh sách trang bị đang sở hữu (còn độ bền > 0)
    val bootsHasteDurability: Int = 0, // Độ bền Giày Thần Tốc (hỏng sau 5 ván Sói Đuổi)
    val shieldAegisDurability: Int = 0, // Độ bền Khiên Aegis Cổ Đại (hỏng sau 4 ván Sói Đuổi)
    val compassVisionDurability: Int = 0, // Độ bền La Bàn Tiên Tri (hỏng sau 5 ván Sói Đuổi)
    val moveSensitivity: Float = 1.0f // Độ nhạy di chuyển (0.5f - 2.0f, mặc định 1.0f = 100%)
) {
    fun getGearDurability(gearId: String): Int {
        return when (gearId.trim()) {
            "boots_haste", "boots_speed" -> bootsHasteDurability
            "shield_aegis", "armor_wolf" -> shieldAegisDurability
            "compass_vision" -> compassVisionDurability
            else -> 0
        }
    }
    /**
     * Lấy cấp độ VIP cao nhất mà người chơi đang sở hữu (0, 1, 2, hoặc 3)
     */
    fun highestOwnedVipTier(nowMillis: Long = System.currentTimeMillis()): Int {
        if (isVipNoAds || vipTier >= 3) return 3
        if (vipTier in 1..2 && nowMillis < vipExpiresAtMillis) return vipTier
        return 0
    }

    /**
     * Trạng thái VIP có đang kích hoạt hay không
     */
    fun isVipActive(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return highestOwnedVipTier(nowMillis) > 0
    }

    /**
     * Có được phép hiển thị quảng cáo Banner ở góc dưới màn hình không
     */
    fun isBannerAdAllowed(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val activeTier = highestOwnedVipTier(nowMillis)
        if (activeTier >= 2) return false
        if (activeTier == 1 && vip1DisableBanner) return false
        return true
    }

    /**
     * Có được phép hiển thị quảng cáo xen kẽ sau khi thắng ván không
     */
    fun isInterstitialAdAllowed(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val activeTier = highestOwnedVipTier(nowMillis)
        if (activeTier >= 2) return false
        if (activeTier == 1 && vip1DisableInterstitial) return false
        return true
    }

    /**
     * Thời gian VIP còn lại (mili-giây)
     */
    fun remainingVipDurationMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val activeTier = highestOwnedVipTier(nowMillis)
        if (activeTier == 3) return Long.MAX_VALUE
        if (activeTier == 0) return 0L
        return maxOf(0L, vipExpiresAtMillis - nowMillis)
    }

    fun canSkip(now: Long): Boolean {
        if (skipTokens > 0) return true
        val cooldownMillis = 2L * 24L * 60L * 60L * 1000L // 2 ngày
        return lastSkipAdTimestamp == 0L || (now - lastSkipAdTimestamp >= cooldownMillis)
    }

    fun canHint(now: Long): Boolean {
        if (hintCount > 0) return true
        val todayId = now / 86400000L
        val storedDayId = lastHintAdDate / 86400000L
        val viewsToday = if (storedDayId == todayId) hintAdViewsToday else 0
        return viewsToday < 2
    }
}

@Entity(tableName = "level_records")
data class LevelRecordEntity(
    @PrimaryKey val levelId: String,
    val stars: Int,
    val bestTimeSec: Int,
    val bestMoves: Int,
    val completedAt: Long,
    val pathHistoryJson: String = "",
    val seedStr: String = "",
    val width: Int = 0,
    val height: Int = 0
)

@Entity(tableName = "save_slot")
data class SaveSlotEntity(
    @PrimaryKey val id: Int = 1,
    val defId: String,
    val tier: String? = null,
    val w: Int,
    val h: Int,
    val seedStr: String,
    val playerX: Int,
    val playerY: Int,
    val moves: Int,
    val elapsedSec: Int,
    val visitedIndicesJson: String,
    val pathHistoryJson: String
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean,
    val isClaimed: Boolean,
    val rewardCoins: Int,
    val iconName: String
)

@Entity(tableName = "daily_challenge_records")
data class DailyChallengeRecordEntity(
    @PrimaryKey val dateString: String,
    val moves: Int,
    val elapsedSec: Int,
    val stars: Int,
    val completedAt: Long
)

/**
 * Entity lưu cấu trúc logic của màn chơi mê cung (Room Database Schema)
 * Quản lý vị trí các bức tường, điểm bắt đầu của người chơi và điểm đích đến.
 */
@Entity(tableName = "maze_levels")
data class MazeLevelEntity(
    @PrimaryKey val levelId: String,
    val width: Int,
    val height: Int,
    val targetSteps: Int,
    val startX: Int,
    val startY: Int,
    val endX: Int,
    val endY: Int,
    val wallsData: String,      // Chuỗi mảng bitmask tường của từng ô (CSV: "15,7,11,...")
    val optimalPath: String,   // Tọa độ đường đi mẫu tối ưu (CSV: "x,y;x,y;...")
    val seed: Long,
    val createdAt: Long = System.currentTimeMillis()
)

