package com.example.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val db: AppDatabase) {
    val progressFlow: Flow<GameProgressEntity?> = db.gameProgressDao().getProgress()
    val levelRecordsFlow: Flow<List<LevelRecordEntity>> = db.levelRecordDao().getAllRecords()
    val saveSlotFlow: Flow<SaveSlotEntity?> = db.saveSlotDao().getSaveSlot()
    val achievementsFlow: Flow<List<AchievementEntity>> = db.achievementDao().getAllAchievements()

    suspend fun ensureInitialized() {
        val currentProgress = db.gameProgressDao().getProgressOnce()
        if (currentProgress == null) {
            db.gameProgressDao().insertOrUpdate(
                GameProgressEntity(
                    id = 1,
                    highestCleared = 0,
                    totalStars = 0,
                    coins = 100,
                    gems = 0,
                    hintCount = 1, // Tặng 1 lượt gợi ý khởi đầu cho tài khoản mới
                    skipTokens = 1, // Tặng 1 lượt bỏ qua khởi đầu cho tài khoản mới
                    keys = 0,
                    shieldCount = 0,
                    equippedGearId = "",
                    unlockedGears = "",
                    impossibleCleared = 0,
                    superCleared = false
                )
            )
        }

        val currentAchievements = db.achievementDao().getAllAchievements().firstOrNull()
        if (currentAchievements.isNullOrEmpty()) {
            val defaultAchievements = listOf(
                AchievementEntity(
                    id = "first_step",
                    title = "Bước Đầu Tiên",
                    description = "Vượt qua Màn 1 của Mê Cung",
                    currentProgress = 0,
                    maxProgress = 1,
                    isUnlocked = false,
                    isClaimed = false,
                    rewardCoins = 50,
                    iconName = "flag"
                ),
                AchievementEntity(
                    id = "fifty_levels",
                    title = "Nhà Thám Hiểm",
                    description = "Hoàn thành 50 màn chơi thường",
                    currentProgress = 0,
                    maxProgress = 50,
                    isUnlocked = false,
                    isClaimed = false,
                    rewardCoins = 300,
                    iconName = "explore"
                ),
                AchievementEntity(
                    id = "perfect_speed",
                    title = "Tốc Độ Ánh Sáng",
                    description = "Đạt 3 sao trong bất kỳ màn chơi nào 5 lần",
                    currentProgress = 0,
                    maxProgress = 5,
                    isUnlocked = false,
                    isClaimed = false,
                    rewardCoins = 150,
                    iconName = "speed"
                ),
                AchievementEntity(
                    id = "no_wall_hit",
                    title = "Đường Đi Chuẩn Xác",
                    description = "Hoàn thành màn mà không đâm tường lần nào",
                    currentProgress = 0,
                    maxProgress = 1,
                    isUnlocked = false,
                    isClaimed = false,
                    rewardCoins = 100,
                    iconName = "check"
                ),
                AchievementEntity(
                    id = "fog_master",
                    title = "Thần Đèn Sương Mù",
                    description = "Chinh phục màn có hiệu ứng sương mù Fog-of-War",
                    currentProgress = 0,
                    maxProgress = 1,
                    isUnlocked = false,
                    isClaimed = false,
                    rewardCoins = 200,
                    iconName = "visibility"
                ),
                AchievementEntity(
                    id = "daily_champion",
                    title = "Quán Quân Ngày",
                    description = "Hoàn thành thử thách Thử Thách Hàng Ngày",
                    currentProgress = 0,
                    maxProgress = 1,
                    isUnlocked = false,
                    isClaimed = false,
                    rewardCoins = 250,
                    iconName = "military_tech"
                )
            )
            db.achievementDao().insertAll(defaultAchievements)
        }
    }

    suspend fun saveProgress(progress: GameProgressEntity) {
        db.gameProgressDao().insertOrUpdate(progress)
    }

    suspend fun recordLevelCompletion(
        levelId: String,
        stars: Int,
        timeSec: Int,
        moves: Int,
        pathHistoryJson: String = "",
        seedStr: String = "",
        width: Int = 0,
        height: Int = 0
    ) {
        val existing = db.levelRecordDao().getRecordForLevel(levelId)
        val bestStars = existing?.let { maxOf(it.stars, stars) } ?: stars
        val bestTime = existing?.let { minOf(it.bestTimeSec, timeSec) } ?: timeSec
        val bestMoves = existing?.let { minOf(it.bestMoves, moves) } ?: moves
        val savedPath = if (pathHistoryJson.isNotEmpty()) pathHistoryJson else (existing?.pathHistoryJson ?: "")
        val savedSeed = if (seedStr.isNotEmpty()) seedStr else (existing?.seedStr ?: "")
        val savedWidth = if (width > 0) width else (existing?.width ?: 0)
        val savedHeight = if (height > 0) height else (existing?.height ?: 0)

        db.levelRecordDao().insertOrUpdate(
            LevelRecordEntity(
                levelId = levelId,
                stars = bestStars,
                bestTimeSec = bestTime,
                bestMoves = bestMoves,
                completedAt = System.currentTimeMillis(),
                pathHistoryJson = savedPath,
                seedStr = savedSeed,
                width = savedWidth,
                height = savedHeight
            )
        )
    }

    suspend fun getLevelRecord(levelId: String): LevelRecordEntity? {
        return db.levelRecordDao().getRecordForLevel(levelId)
    }

    suspend fun saveGameSlot(slot: SaveSlotEntity) {
        db.saveSlotDao().save(slot)
    }

    suspend fun clearSaveSlot() {
        db.saveSlotDao().deleteSaveSlot()
    }

    suspend fun updateAchievementProgress(id: String, increment: Int = 1, absolute: Int? = null) {
        val ach = db.achievementDao().getById(id) ?: return
        val newProgress = absolute ?: (ach.currentProgress + increment)
        val isUnlocked = ach.isUnlocked || (newProgress >= ach.maxProgress)
        db.achievementDao().update(
            ach.copy(
                currentProgress = newProgress.coerceAtMost(ach.maxProgress),
                isUnlocked = isUnlocked
            )
        )
    }

    suspend fun claimAchievement(id: String): Int {
        val ach = db.achievementDao().getById(id) ?: return 0
        if (ach.isUnlocked && !ach.isClaimed) {
            db.achievementDao().update(ach.copy(isClaimed = true))
            val current = db.gameProgressDao().getProgressOnce() ?: GameProgressEntity()
            db.gameProgressDao().insertOrUpdate(current.copy(coins = current.coins + ach.rewardCoins))
            return ach.rewardCoins
        }
        return 0
    }

    suspend fun resetAllProgress() {
        db.levelRecordDao().clearAll()
        db.saveSlotDao().deleteSaveSlot()
        db.achievementDao().clearAll()
        db.gameProgressDao().insertOrUpdate(
            GameProgressEntity(
                id = 1,
                highestCleared = 0,
                totalStars = 0,
                coins = 100,
                gems = 0,
                hintCount = 1,
                skipTokens = 1,
                impossibleCleared = 0,
                superCleared = false
            )
        )
    }

    // Room Database Methods cho MazeLevel
    suspend fun saveMazeLevel(level: MazeLevelEntity) {
        db.mazeLevelDao().insertLevel(level)
    }

    suspend fun getMazeLevel(levelId: String): MazeLevelEntity? {
        return db.mazeLevelDao().getLevelSync(levelId)
    }

    fun getMazeLevelFlow(levelId: String): Flow<MazeLevelEntity?> {
        return db.mazeLevelDao().getLevel(levelId)
    }
}

