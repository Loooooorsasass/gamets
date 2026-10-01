package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ads.AdConstants
import com.example.core.ads.AdManager
import com.example.core.audio.SoundManager
import com.example.core.billing.BillingManager
import com.example.core.controller.LevelSkipController
import com.example.core.time.NetworkTimeManager
import com.example.core.engine.CollectibleType
import com.example.core.engine.EAST
import com.example.core.engine.GoalDirectionHint
import com.example.core.engine.LevelDef
import com.example.core.engine.Maze
import com.example.core.engine.MazeBuilder
import com.example.core.engine.MazeCollectible
import com.example.core.engine.MazeConfig
import com.example.core.engine.MazeGenerator
import com.example.core.engine.MazeSolver
import com.example.core.engine.NORTH
import com.example.core.engine.Point
import com.example.core.engine.SOUTH
import com.example.core.engine.WEST
import com.example.core.haptics.HapticManager
import com.example.data.cloud.DailyChallengeManager
import com.example.data.local.AchievementEntity
import com.example.data.local.AppDatabase
import com.example.data.local.GameProgressEntity
import com.example.data.local.GameRepository
import com.example.data.local.LevelRecordEntity
import com.example.data.local.SaveSlotEntity
import com.example.data.shop.ShopCatalog
import com.example.ui.components.GridControlMode
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray

enum class AppScreen {
    HOME,
    PLAYING,
    REPLAY,
    LEADERBOARD,
    ACHIEVEMENTS,
    DAILY_CHALLENGE,
    SHOP,
    ONELINE_GAME
}

data class ActiveGameState(
    val maze: Maze? = null,
    val levelDef: LevelDef? = null,
    val player: Point = Point(0, 0),
    val moves: Int = 0,
    val elapsedSec: Int = 0,
    val timeLimitSec: Int = 0,
    val visitedCells: Set<Int> = emptySet(),
    val pathHistory: List<Point> = emptyList(),
    val distCache: IntArray? = null,
    val goalDistCache: IntArray? = null,
    val goalDirectionHint: GoalDirectionHint? = null,
    val collectibles: List<MazeCollectible> = emptyList(),
    val collectedCoinsInRun: Int = 0,
    val collectedKeysInRun: Int = 0,
    val collectedShieldsInRun: Int = 0,
    val wolfPos: Point? = null,
    val wolfCubPos: Point? = null,
    val wolfCubActive: Boolean = false,
    val wolfCubDistanceTraveled: Int = 0,
    val wolfCountdownSec: Int = 0,
    val wolfActive: Boolean = false,
    val wolfFrenzy: Boolean = false,
    val wolfSpeedMs: Long = MazeConfig.WOLF_NORMAL_STEP_MS,
    val wolfDistanceCells: Int = 0,
    val wolfStunnedUntilMs: Long = 0L,
    val playerFrozenUntilMs: Long = 0L,
    val wolfNextIceThrowAtMs: Long = 0L,
    val wolfIceBeamUntilMs: Long = 0L,
    val freeAegisShieldInRun: Int = 0,
    val shieldTriggeredCount: Int = 0,
    val gearDurabilityConsumedInRun: Boolean = false,
    val wallHits: Int = 0,
    val isCelebratingWin: Boolean = false,
    val gameOver: Boolean = false,
    val hasWon: Boolean = false,
    val earnedStars: Int = 0,
    val secPerCell: Double = 0.0,
    val isPaused: Boolean = false,
    val showExitDialog: Boolean = false,
    val showHint: Boolean = false,
    val justUnlockedImpossible: Boolean = false,
    val justUnlockedWolfMode: Boolean = false,
    val justUnlockedNextTier: Boolean = false,
    val nextTierDef: LevelDef? = null,
    val justClearedSuper: Boolean = false,
    val controlMode: GridControlMode = GridControlMode.AUTO
)

data class ReplayState(
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val speed: Float = 1f
)

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val progress: GameProgressEntity = GameProgressEntity(),
    val language: AppLanguage = AppLanguage.VI,
    val showFirstLaunchLanguageDialog: Boolean = false,
    val levelRecords: Map<String, LevelRecordEntity> = emptyMap(),
    val saveSlot: SaveSlotEntity? = null,
    val achievements: List<AchievementEntity> = emptyList(),
    val activeGame: ActiveGameState = ActiveGameState(),
    val replay: ReplayState = ReplayState(),
    val rewardAdPrompt: String? = null,
    val pendingInterstitial: Boolean = false,
    val showWolfEquipmentDialog: Boolean = false,
    val showCloudSyncDialog: Boolean = false,
    val showResetModal: Boolean = false,
    val selectedLeaderboardLevel: Int = 1,
    val toastMessage: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repository = GameRepository(db)

    private val progressMutex = Mutex()

    suspend fun mutateProgress(block: (GameProgressEntity) -> GameProgressEntity): GameProgressEntity {
        return progressMutex.withLock {
            val current = _uiState.value.progress
            val updated = block(current)
            repository.saveProgress(updated)
            _uiState.update { it.copy(progress = updated) }
            updated
        }
    }

    val soundManager = SoundManager(application)
    val hapticManager = HapticManager(application)
    val adManager = AdManager.getInstance(application)
    val networkTimeManager = NetworkTimeManager.getInstance(application)
    val levelSkipController = LevelSkipController.getInstance(application, repository, networkTimeManager)
    val billingManager = BillingManager(application) { tier ->
        onVipPurchaseGranted(tier)
    }

    private fun getWolfHeadStartSeconds(def: LevelDef?): Int {
        if (def?.isWolfChase == true && (def.numericLevel ?: 1) in 1..5) return 3
        return MazeConfig.WOLF_HEAD_START_SECONDS
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.destroy()
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var wolfJob: Job? = null
    private var winCelebrationJob: Job? = null
    private var replayJob: Job? = null
    private var gamesClearedSinceLastInterstitial: Int = 0

    private fun registerGameClearedAndShouldShowInterstitial(progress: GameProgressEntity): Boolean {
        gamesClearedSinceLastInterstitial++
        if (gamesClearedSinceLastInterstitial >= AdConstants.GAMES_PER_INTERSTITIAL_AD) {
            gamesClearedSinceLastInterstitial = 0
            return progress.isInterstitialAdAllowed() && adManager.canShowInterstitialWithCooldown()
        }
        return false
    }
    private var hasHandledInitialLaunch: Boolean = false

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }

        viewModelScope.launch {
            repository.progressFlow.collect { p ->
                if (p != null) {
                    val normalizedP = checkAndResetDailyProgress(p)
                    if (normalizedP != p) {
                        repository.saveProgress(normalizedP)
                    }
                    val lang = if (normalizedP.language == "en") AppLanguage.EN else AppLanguage.VI
                    _uiState.update { it.copy(progress = normalizedP, language = lang) }
                    soundManager.isSoundEnabled = normalizedP.soundEnabled
                    soundManager.isMusicEnabled = normalizedP.musicEnabled
                    hapticManager.isHapticEnabled = normalizedP.hapticEnabled

                    if (!hasHandledInitialLaunch) {
                        hasHandledInitialLaunch = true
                        if (!normalizedP.hasSelectedInitialLanguage) {
                            // Khi vừa tải game và mở lên lần đầu: mở sẵn Màn 1 Mê Cung và hiển thị bảng chọn ngôn ngữ
                            startLevelInternal(MazeConfig.generateLevelDef(1), playSound = false)
                            _uiState.update { it.copy(showFirstLaunchLanguageDialog = true) }
                        } else if (normalizedP.highestCleared == 0 && _uiState.value.activeGame.maze == null) {
                            // Nếu chưa qua Màn 1, mở ngay Màn 1 Mê Cung
                            startLevelInternal(MazeConfig.generateLevelDef(1), playSound = false)
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            repository.levelRecordsFlow.collect { records ->
                val map = records.associateBy { it.levelId }
                _uiState.update { it.copy(levelRecords = map) }
            }
        }

        viewModelScope.launch {
            repository.saveSlotFlow.collect { slot ->
                _uiState.update { it.copy(saveSlot = slot) }
            }
        }

        viewModelScope.launch {
            repository.achievementsFlow.collect { list ->
                _uiState.update { it.copy(achievements = list) }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        soundManager.playClick()
        val prevScreen = _uiState.value.currentScreen
        val active = _uiState.value.activeGame
        if (screen != AppScreen.PLAYING && active.levelDef?.isWolfChase == true && !active.gameOver && !active.isCelebratingWin &&
            (active.moves > 0 || active.elapsedSec >= getWolfHeadStartSeconds(active.levelDef))
        ) {
            consumeWolfGearDurabilityIfNeeded()
        }
        if (screen == AppScreen.PLAYING && _uiState.value.activeGame.maze != null) {
            resumeTimer()
        } else if (screen != AppScreen.PLAYING) {
            pauseTimer()
        }
        // Nếu người chơi ra màn hình chính sau 1 phút 40 giây kể từ lúc mở hoặc lần hiện trước -> hiển thị quảng cáo xen giữa
        val shouldTriggerInterstitialOnExit = (screen == AppScreen.HOME) &&
            (prevScreen == AppScreen.PLAYING || prevScreen == AppScreen.ONELINE_GAME) &&
            _uiState.value.progress.isInterstitialAdAllowed() &&
            adManager.canShowInterstitialWithCooldown()
        _uiState.update {
            it.copy(
                currentScreen = screen,
                pendingInterstitial = it.pendingInterstitial || shouldTriggerInterstitialOnExit
            )
        }
    }

    /**
     * Giảm 1 độ bền của Trang Bị Sói Đuổi sau mỗi ván Sói Đuổi.
     * Khi độ bền về 0 (hỏng sau 4-5 ván game Sói Đuổi), trang bị sẽ hỏng và cần mua mới.
     */
    private fun applyWolfGearDurabilityConsumption(p: GameProgressEntity): Pair<GameProgressEntity, String?> {
        val normalizedGear = ShopCatalog.normalizeGearId(p.equippedGearId)
        if (normalizedGear.isBlank()) return p to null
        val gearObj = ShopCatalog.getEquipmentById(normalizedGear) ?: return p to null
        val currentDur = p.getGearDurability(normalizedGear)
        if (currentDur <= 0) {
            val unlocked = p.unlockedGears.split(",").map { ShopCatalog.normalizeGearId(it) }.filter { it.isNotEmpty() && it != normalizedGear }
            return p.copy(equippedGearId = "", unlockedGears = unlocked.joinToString(",")) to null
        }
        val nextDur = maxOf(0, currentDur - 1)
        val isBroken = nextDur == 0
        val unlockedSet = p.unlockedGears.split(",").map { ShopCatalog.normalizeGearId(it) }.filter { it.isNotEmpty() }.toMutableSet()
        if (isBroken) {
            unlockedSet.remove(normalizedGear)
        }
        val nextEquipped = if (isBroken) "" else normalizedGear
        val updated = when (normalizedGear) {
            "boots_haste" -> p.copy(
                bootsHasteDurability = nextDur,
                equippedGearId = nextEquipped,
                unlockedGears = unlockedSet.joinToString(",")
            )
            "shield_aegis" -> p.copy(
                shieldAegisDurability = nextDur,
                equippedGearId = nextEquipped,
                unlockedGears = unlockedSet.joinToString(",")
            )
            "compass_vision" -> p.copy(
                compassVisionDurability = nextDur,
                equippedGearId = nextEquipped,
                unlockedGears = unlockedSet.joinToString(",")
            )
            else -> p
        }
        val isVi = _uiState.value.language == AppLanguage.VI
        val brokenMsg = if (isBroken) {
            if (isVi) "💥 ${gearObj.nameVi} đã dùng hết ${gearObj.maxDurability} ván Sói Đuổi và bị hỏng!"
            else "💥 ${gearObj.nameEn} broke after ${gearObj.maxDurability} Wolf Chase games!"
        } else null
        return updated to brokenMsg
    }

    private fun consumeWolfGearDurabilityIfNeeded() {
        val game = _uiState.value.activeGame
        if (game.levelDef?.isWolfChase != true || game.gearDurabilityConsumedInRun) return
        val (updatedProgress, brokenMsg) = applyWolfGearDurabilityConsumption(_uiState.value.progress)
        _uiState.update {
            it.copy(
                progress = updatedProgress,
                toastMessage = brokenMsg ?: it.toastMessage,
                activeGame = it.activeGame.copy(gearDurabilityConsumedInRun = true)
            )
        }
        viewModelScope.launch {
            repository.saveProgress(updatedProgress)
        }
    }

    fun startLevel(def: LevelDef) {
        startLevelInternal(def, playSound = true, customSeed = null)
    }

    fun restartCurrentRun() {
        val active = _uiState.value.activeGame
        val def = active.levelDef ?: return
        val currentSeed = active.maze?.seedStr?.toLongOrNull()
        startLevelInternal(def, playSound = true, customSeed = currentSeed)
    }

    fun finishCurrentRunForExit() {
        val active = _uiState.value.activeGame
        if (active.levelDef?.isWolfChase == true && !active.gameOver && !active.isCelebratingWin &&
            (active.moves > 0 || active.elapsedSec >= getWolfHeadStartSeconds(active.levelDef))
        ) {
            consumeWolfGearDurabilityIfNeeded()
        }
        pauseTimer()
    }

    fun startWolfChaseLevel(wolfLevel: Int) {
        val progress = _uiState.value.progress
        if (progress.highestCleared < MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE) {
            val msg = if (_uiState.value.language == AppLanguage.VI) {
                "🔒 Cần vượt qua Màn ${MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE} chế độ thường để mở khóa Chế Độ Sói Đuổi!"
            } else {
                "🔒 Clear Normal Level ${MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE} to unlock Wolf Chase Mode!"
            }
            _uiState.update { it.copy(toastMessage = msg) }
            return
        }
        val clamped = wolfLevel.coerceIn(1, MazeConfig.WOLF_MAX_LEVEL)
        startLevelInternal(MazeConfig.wolfChaseDef(clamped), playSound = true, customSeed = null)
    }

    private fun computeLevelTimeLimitSec(def: LevelDef, equippedGearId: String): Int {
        if (def.numericLevel == 1 || (def.noTimer && !def.isFinal && !def.isWolfChase)) {
            return 0
        }
        val baseSec = when {
            // Thời gian của game Sói Đuổi giảm đi 30%
            def.isWolfChase -> maxOf(18, (maxOf(28, (def.target * 1.85).toInt()) * 0.70).toInt())
            def.tier == "SUPER" -> maxOf(180, (def.target * 1.6).toInt())
            def.tier != null && def.tier.startsWith("IMPOSSIBLE") || (def.tier?.toIntOrNull() != null) -> maxOf(45, (def.target * 1.8).toInt())
            else -> maxOf(20, def.target * 2)
        }
        // Trang bị Giày Thần Tốc (+15s thời gian) CHỈ có tác dụng trong màn Sói Đuổi khi còn độ bền (> 0)
        val normalizedGear = ShopCatalog.normalizeGearId(equippedGearId)
        val hasDurability = _uiState.value.progress.getGearDurability(normalizedGear) > 0
        return if (def.isWolfChase && normalizedGear == "boots_haste" && hasDurability) {
            baseSec + 15
        } else {
            baseSec
        }
    }

    private fun adjustVisionForGear(baseVision: Int?, equippedGearId: String, isWolfChase: Boolean = false): Int? {
        if (baseVision == null) return null
        val normalizedGear = ShopCatalog.normalizeGearId(equippedGearId)
        val hasDurability = _uiState.value.progress.getGearDurability(normalizedGear) > 0
        // Trang bị La Bàn Tiên Tri CHỈ có tác dụng trong màn Sói Đuổi khi còn độ bền (> 0)
        return if (isWolfChase && normalizedGear == "compass_vision" && hasDurability) baseVision + 3 else baseVision
    }

    private fun startLevelInternal(def: LevelDef, playSound: Boolean, customSeed: Long? = null) {
        if (playSound) {
            soundManager.playClick()
        }
        val progress = _uiState.value.progress
        val equippedGear = ShopCatalog.normalizeGearId(progress.equippedGearId)
        val hasGearDurability = progress.getGearDurability(equippedGear) > 0
        val activeGear = if (hasGearDurability) equippedGear else ""
        val adjustedDef = def.copy(vision = adjustVisionForGear(def.vision, activeGear, def.isWolfChase))
        val seed = customSeed ?: MazeBuilder.randomSeed().toLong()
        val levelData = MazeGenerator.generateFromDef(adjustedDef, seed)
        val maze = levelData.toMaze()
        val distCache = MazeSolver.computeDistances(maze)
        val goalDistCache = MazeSolver.computeDistancesToGoal(maze)
        val goalHint = MazeSolver.computeGoalDirectionHint(maze, maze.start, goalDistCache)
        val collectibles = MazeSolver.generateCollectibles(maze, adjustedDef)
        val timeLimitSec = computeLevelTimeLimitSec(adjustedDef, activeGear)
        val freeAegis = if (adjustedDef.isWolfChase && activeGear == "shield_aegis") 1 else 0

        // Lưu cấu trúc logic của level vào Room database
        viewModelScope.launch {
            repository.saveMazeLevel(MazeGenerator.toEntity(levelData))
        }

        val preservedControlMode = _uiState.value.activeGame.controlMode

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.PLAYING,
                activeGame = ActiveGameState(
                    maze = maze,
                    levelDef = adjustedDef,
                    player = maze.start,
                    moves = 0,
                    elapsedSec = 0,
                    timeLimitSec = timeLimitSec,
                    visitedCells = setOf(maze.start.y * maze.w + maze.start.x),
                    pathHistory = listOf(maze.start),
                    distCache = distCache,
                    goalDistCache = goalDistCache,
                    goalDirectionHint = goalHint,
                    collectibles = collectibles,
                    wolfPos = if (adjustedDef.isWolfChase) maze.start else null,
                    wolfCubPos = null,
                    wolfCubActive = false,
                    wolfCubDistanceTraveled = 0,
                    wolfCountdownSec = if (adjustedDef.isWolfChase) getWolfHeadStartSeconds(adjustedDef) else 0,
                    wolfActive = false,
                    wolfFrenzy = false,
                    wolfSpeedMs = MazeConfig.WOLF_NORMAL_STEP_MS,
                    wolfDistanceCells = 0,
                    wolfStunnedUntilMs = 0L,
                    playerFrozenUntilMs = 0L,
                    wolfNextIceThrowAtMs = 0L,
                    wolfIceBeamUntilMs = 0L,
                    freeAegisShieldInRun = freeAegis,
                    gearDurabilityConsumedInRun = false,
                    wallHits = 0,
                    gameOver = false,
                    hasWon = false,
                    earnedStars = 0,
                    secPerCell = 0.0,
                    isPaused = false,
                    showExitDialog = false,
                    showHint = false,
                    controlMode = preservedControlMode
                )
            )
        }
        startTimer()
    }

    /**
     * Xác nhận chọn ngôn ngữ khi vừa tải và mở game lần đầu -> Mở ngay Màn 1 game Mê Cung (không tính thời gian)
     */
    fun selectInitialLanguageAndStartLevel1(lang: AppLanguage) {
        soundManager.playClick()
        hapticManager.performButtonFeedback()
        val updatedProgress = _uiState.value.progress.copy(
            language = lang.code,
            hasSelectedInitialLanguage = true
        )
        _uiState.update {
            it.copy(
                language = lang,
                progress = updatedProgress,
                showFirstLaunchLanguageDialog = false
            )
        }
        viewModelScope.launch {
            repository.saveProgress(updatedProgress)
        }
        val currentDef = _uiState.value.activeGame.levelDef
        if (_uiState.value.currentScreen != AppScreen.PLAYING || currentDef?.numericLevel != 1 || _uiState.value.activeGame.maze == null) {
            startLevelInternal(MazeConfig.generateLevelDef(1), playSound = false)
        }
    }

    fun checkAndResetDailyProgress(p: GameProgressEntity): GameProgressEntity {
        var result = p
        // Đảm bảo quảng cáo AdMob (Banner, Interstitial, Rewarded) luôn được bật hiển thị nếu chưa có giao dịch IAP thật
        if (com.example.core.ads.AdConstants.PRODUCT_ID_VIP_2.isBlank() &&
            com.example.core.ads.AdConstants.PRODUCT_ID_VIP_3.isBlank() &&
            (result.isVipNoAds || result.vipTier > 0 || result.vip1DisableBanner || result.vip1DisableInterstitial)
        ) {
            result = result.copy(
                isVipNoAds = false,
                vipTier = 0,
                vip1DisableBanner = false,
                vip1DisableInterstitial = false
            )
        }
        // Nếu người chơi đang ở dữ liệu mặc định cũ ("midnight_cyber" chưa mua), chuyển về bản đồ đen mặc định ("classic_black")
        if (result.unlockedThemes == "midnight_cyber" && result.currentMazeThemeId == "midnight_cyber") {
            result = result.copy(
                currentMazeThemeId = "classic_black",
                unlockedThemes = "classic_black"
            )
        } else if (!result.unlockedThemes.split(",").map { it.trim() }.contains("classic_black")) {
            val themesSet = result.unlockedThemes.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
            themesSet.add("classic_black")
            result = result.copy(unlockedThemes = themesSet.joinToString(","))
        }

        // Loại bỏ các trang bị có độ bền = 0 (không tặng miễn phí vật phẩm)
        val validGears = result.unlockedGears
            .split(",")
            .map { ShopCatalog.normalizeGearId(it) }
            .filter { it.isNotEmpty() && result.getGearDurability(it) > 0 }
        val validEquipped = ShopCatalog.normalizeGearId(result.equippedGearId).let {
            if (it.isNotEmpty() && result.getGearDurability(it) > 0) it else ""
        }
        if (validGears.joinToString(",") != result.unlockedGears || validEquipped != result.equippedGearId) {
            result = result.copy(
                unlockedGears = validGears.joinToString(","),
                equippedGearId = validEquipped
            )
        }

        val today = DailyChallengeManager.getTodayDateKey()
        if (result.dailyDate != today) {
            return result.copy(
                dailyDate = today,
                dailyStage = 1,
                dailyAttemptsUsed = 0,
                dailyStage1Time = 0,
                dailyStage2Time = 0,
                dailyStage3Time = 0,
                dailyAllCompleted = false
            )
        }
        return result
    }

    fun startDailyChallenge(stage: Int? = null) {
        soundManager.playClick()
        val currentP = checkAndResetDailyProgress(_uiState.value.progress)
        if (currentP != _uiState.value.progress) {
            viewModelScope.launch { repository.saveProgress(currentP) }
        }

        // Kiểm tra nếu đã hết 3 lượt hôm nay hoặc đã hoàn thành toàn bộ
        if (currentP.dailyAllCompleted || currentP.dailyAttemptsUsed >= DailyChallengeManager.MAX_DAILY_ATTEMPTS) {
            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.DAILY_CHALLENGE,
                    toastMessage = Strings.dailyCooldownTitle(it.language)
                )
            }
            return
        }

        val allowedStage = currentP.dailyStage.coerceIn(1, 3)
        val targetStage = stage ?: allowedStage
        if (targetStage > allowedStage) {
            _uiState.update {
                it.copy(
                    toastMessage = if (it.language == AppLanguage.VI) "🔒 Hãy hoàn thành Chặng $allowedStage trước!" else "🔒 Clear Stage $allowedStage first!"
                )
            }
            return
        }

        // Tăng số lượt thử thách đã dùng trong ngày ngay khi bắt đầu lượt chơi
        viewModelScope.launch {
            mutateProgress { p ->
                p.copy(dailyAttemptsUsed = p.dailyAttemptsUsed + 1)
            }
        }

        val todayDate = DailyChallengeManager.getTodayDateKey()
        val size = DailyChallengeManager.getDailySize(targetStage)
        val target = DailyChallengeManager.getDailyTarget(targetStage)
        val seed = DailyChallengeManager.getDailySeed(todayDate, targetStage)

        val equippedGear = currentP.equippedGearId
        val def = LevelDef(
            id = "DAILY_${todayDate}_STAGE_$targetStage",
            numericLevel = null,
            tier = "DAILY",
            w = size,
            h = size,
            target = target,
            moveCap = null,
            vision = adjustVisionForGear(MazeConfig.VISION_WINDOW, equippedGear),
            isFinal = false,
            noTimer = false
        )

        val levelData = MazeGenerator.generateFromDef(def, seed.toLong())
        val maze = levelData.toMaze()
        val distCache = MazeSolver.computeDistances(maze)
        val goalDistCache = MazeSolver.computeDistancesToGoal(maze)
        val goalHint = MazeSolver.computeGoalDirectionHint(maze, maze.start, goalDistCache)
        val collectibles = MazeSolver.generateCollectibles(maze, def)
        val timeLimitSec = computeLevelTimeLimitSec(def, equippedGear)

        // Lưu cấu trúc logic của level vào Room database
        viewModelScope.launch {
            repository.saveMazeLevel(MazeGenerator.toEntity(levelData))
        }

        val preservedControlMode = _uiState.value.activeGame.controlMode

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.PLAYING,
                activeGame = ActiveGameState(
                    maze = maze,
                    levelDef = def,
                    player = maze.start,
                    moves = 0,
                    elapsedSec = 0,
                    timeLimitSec = timeLimitSec,
                    visitedCells = setOf(maze.start.y * maze.w + maze.start.x),
                    pathHistory = listOf(maze.start),
                    distCache = distCache,
                    goalDistCache = goalDistCache,
                    goalDirectionHint = goalHint,
                    collectibles = collectibles,
                    wallHits = 0,
                    gameOver = false,
                    hasWon = false,
                    earnedStars = 0,
                    secPerCell = 0.0,
                    isPaused = false,
                    showExitDialog = false,
                    showHint = false,
                    controlMode = preservedControlMode
                )
            )
        }
        startTimer()
    }

    fun resumeFromSave() {
        val slot = _uiState.value.saveSlot ?: return
        soundManager.playClick()
        val equippedGear = _uiState.value.progress.equippedGearId
        val isImp = slot.defId.startsWith("IMPOSSIBLE")
        val isWolf = slot.defId.startsWith("WOLF_")
        val rawDef = when {
            isWolf -> {
                val wLvl = slot.defId.removePrefix("WOLF_").toIntOrNull() ?: 1
                MazeConfig.wolfChaseDef(wLvl)
            }
            isImp -> {
                if (slot.tier == "SUPER") MazeConfig.superDef()
                else MazeConfig.impossibleDefForTier(slot.tier?.toIntOrNull() ?: 1)
            }
            else -> {
                val num = slot.defId.toIntOrNull() ?: 1
                MazeConfig.generateLevelDef(num)
            }
        }
        val def = rawDef.copy(vision = adjustVisionForGear(rawDef.vision, equippedGear, rawDef.isWolfChase))

        val seed = slot.seedStr.toULongOrNull() ?: 42UL
        val maze = MazeBuilder.buildMaze(def.w, def.h, def.target, seed, def.algorithmIndex)
        val distCache = MazeSolver.computeDistances(maze)
        val goalDistCache = MazeSolver.computeDistancesToGoal(maze)
        val playerPt = Point(slot.playerX, slot.playerY)
        val goalHint = MazeSolver.computeGoalDirectionHint(maze, playerPt, goalDistCache)
        val collectibles = MazeSolver.generateCollectibles(maze, def)
        val timeLimitSec = computeLevelTimeLimitSec(def, equippedGear)
        val freeAegis = if (def.isWolfChase && ShopCatalog.normalizeGearId(equippedGear) == "shield_aegis") 1 else 0

        val visitedSet = try {
            val jsonArr = JSONArray(slot.visitedIndicesJson)
            (0 until jsonArr.length()).map { jsonArr.getInt(it) }.toSet()
        } catch (_: Exception) {
            setOf(slot.playerY * def.w + slot.playerX)
        }

        val historyList = try {
            val jsonArr = JSONArray(slot.pathHistoryJson)
            (0 until jsonArr.length()).map { idx ->
                val str = jsonArr.getString(idx)
                val parts = str.split(",")
                Point(parts[0].toInt(), parts[1].toInt())
            }
        } catch (_: Exception) {
            listOf(Point(slot.playerX, slot.playerY))
        }

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.PLAYING,
                activeGame = ActiveGameState(
                    maze = maze,
                    levelDef = def,
                    player = playerPt,
                    moves = slot.moves,
                    elapsedSec = slot.elapsedSec,
                    timeLimitSec = timeLimitSec,
                    visitedCells = visitedSet,
                    pathHistory = historyList,
                    distCache = distCache,
                    goalDistCache = goalDistCache,
                    goalDirectionHint = goalHint,
                    collectibles = collectibles.filter { c -> !visitedSet.contains(c.y * def.w + c.x) },
                    wolfPos = if (def.isWolfChase && slot.elapsedSec >= getWolfHeadStartSeconds(def)) maze.start else null,
                    wolfCubPos = null,
                    wolfCubActive = false,
                    wolfCubDistanceTraveled = 0,
                    wolfCountdownSec = if (def.isWolfChase) maxOf(0, getWolfHeadStartSeconds(def) - slot.elapsedSec) else 0,
                    wolfActive = def.isWolfChase && slot.elapsedSec >= getWolfHeadStartSeconds(def),
                    wallHits = 0,
                    gameOver = false,
                    hasWon = false,
                    isPaused = false
                )
            )
        }
        startTimer()
    }

    private var lastStepModeMoveTimeMs = 0L

    fun onWallHit() {
        soundManager.playBump()
        hapticManager.performWallHitFeedback()
        _uiState.update {
            it.copy(activeGame = it.activeGame.copy(wallHits = it.activeGame.wallHits + 1))
        }
    }

    fun tryMove(dx: Int, dy: Int, fromCanvas: Boolean = false): Boolean {
        val game = _uiState.value.activeGame
        val maze = game.maze ?: return false
        if (game.gameOver || game.isCelebratingWin || game.isPaused) return false

        val isAuto = game.controlMode == GridControlMode.AUTO
        val nowMs = System.currentTimeMillis()
        if (!isAuto && !fromCanvas) {
            // Tốc độ tối đa chế độ từng bước là 0.1 giây / bước (100ms / ô)
            if (nowMs - lastStepModeMoveTimeMs < 100L) {
                return false
            }
        }

        var curX = game.player.x
        var curY = game.player.y
        var movedAny = false
        var currentGame = game

        while (true) {
            val iterNowMs = System.currentTimeMillis()
            if (iterNowMs < currentGame.playerFrozenUntilMs) {
                val remSec = String.format(java.util.Locale.US, "%.1f", (currentGame.playerFrozenUntilMs - iterNowMs) / 1000f)
                val isVi = _uiState.value.language == AppLanguage.VI
                _uiState.update {
                    it.copy(
                        toastMessage = if (isVi) "❄️ Đang bị đóng băng bởi Băng Xuyên Tường của Sói (${remSec}s)!"
                        else "❄️ Frozen by Wolf's Wall-Piercing Ice (${remSec}s)!"
                    )
                }
                break
            }

            val dir = when {
                dx == 1 && dy == 0 -> EAST
                dx == -1 && dy == 0 -> WEST
                dx == 0 && dy == 1 -> NORTH
                dx == 0 && dy == -1 -> SOUTH
                else -> return false
            }

            val mask = maze.cellAt(curX, curY)
            if ((mask and dir) == 0) {
                if (!movedAny) {
                    soundManager.playBump()
                    hapticManager.performWallHitFeedback()
                    _uiState.update {
                        it.copy(activeGame = it.activeGame.copy(wallHits = it.activeGame.wallHits + 1))
                    }
                }
                break
            }

            val nx = curX + dx
            val ny = curY + dy

            // Check Undo
            if (currentGame.pathHistory.size >= 2) {
                val prevPoint = currentGame.pathHistory[currentGame.pathHistory.size - 2]
                if (nx == prevPoint.x && ny == prevPoint.y) {
                    return undoMove()
                }
            }

            val newPlayer = Point(nx, ny)
            val newMoves = currentGame.moves + 1
            val nIdx = ny * maze.w + nx
            val newVisited = currentGame.visitedCells + nIdx
            val newHistory = currentGame.pathHistory + newPlayer
            val newGoalHint = currentGame.goalDistCache?.let {
                MazeSolver.computeGoalDirectionHint(maze, newPlayer, it)
            }

            soundManager.playStep()
            hapticManager.performMoveFeedback()

            if (currentGame.levelDef?.moveCap != null && newMoves > currentGame.levelDef.moveCap) {
                handleLoss("🚫 Vượt quá ${currentGame.levelDef.moveCap} bước di chuyển!")
                break
            }

            val isWolfChase = currentGame.levelDef?.isWolfChase == true
            val currentWolfPos = if (isWolfChase) (currentGame.wolfPos ?: maze.start) else null
            val distToWolf = if (isWolfChase && currentWolfPos != null) {
                MazeSolver.computePathDistance(maze, currentWolfPos, newPlayer)
            } else 0
            val wolfNowActive = currentGame.wolfActive

            val canWolfIceThrowNow = isWolfChase && wolfNowActive &&
                distToWolf > MazeConfig.WOLF_ICE_MIN_DISTANCE_CELLS &&
                nowMs >= currentGame.wolfNextIceThrowAtMs &&
                nowMs >= currentGame.wolfStunnedUntilMs &&
                (nx != maze.goal.x || ny != maze.goal.y)

            val newFrozenUntilMs = if (canWolfIceThrowNow) {
                nowMs + MazeConfig.WOLF_ICE_FREEZE_DURATION_MS
            } else {
                currentGame.playerFrozenUntilMs
            }
            val newNextIceThrowAtMs = if (canWolfIceThrowNow) {
                nowMs + MazeConfig.WOLF_ICE_COOLDOWN_MS
            } else {
                currentGame.wolfNextIceThrowAtMs
            }
            val newIceBeamUntilMs = if (canWolfIceThrowNow) {
                nowMs + MazeConfig.WOLF_ICE_FREEZE_DURATION_MS
            } else {
                currentGame.wolfIceBeamUntilMs
            }

            val isVietnamese = _uiState.value.language == AppLanguage.VI
            val wolfEventToast = when {
                canWolfIceThrowNow -> {
                    if (isVietnamese) "❄️ Sói ném Băng Xuyên Tường (Khoảng cách $distToWolf ô > 25 ô)! Bạn bị đóng băng 2 giây! (Hồi chiêu: 15s)"
                    else "❄️ Wolf cast Wall-Piercing Ice (Distance $distToWolf > 25 cells)! Frozen for 2s! (Cooldown: 15s)"
                }
                else -> null
            }

            if (wolfEventToast != null) {
                _uiState.update { it.copy(toastMessage = wolfEventToast) }
            }

            val reachedGoal = (nx == maze.goal.x && ny == maze.goal.y)
            val headStartSec = getWolfHeadStartSeconds(currentGame.levelDef)

            currentGame = currentGame.copy(
                player = newPlayer,
                moves = newMoves,
                visitedCells = newVisited,
                pathHistory = newHistory,
                goalDirectionHint = newGoalHint ?: currentGame.goalDirectionHint,
                wolfPos = currentWolfPos,
                wolfActive = wolfNowActive,
                wolfDistanceCells = distToWolf,
                wolfCountdownSec = if (isWolfChase) maxOf(0, headStartSec - currentGame.elapsedSec) else 0,
                playerFrozenUntilMs = newFrozenUntilMs,
                wolfNextIceThrowAtMs = newNextIceThrowAtMs,
                wolfIceBeamUntilMs = newIceBeamUntilMs,
                isCelebratingWin = reachedGoal || currentGame.isCelebratingWin
            )

            curX = nx
            curY = ny
            movedAny = true

            if (reachedGoal || !isAuto) {
                break
            }

            // Check stop conditions at new cell (curX, curY):
            // 1. Wall ahead
            val nextMask = maze.cellAt(curX, curY)
            if ((nextMask and dir) == 0) {
                break
            }

            // 2. Intersection (Ngã 3 / Ngã 4)
            var atIntersection = false
            if (dx != 0) {
                val upOpen = (nextMask and NORTH) != 0
                val downOpen = (nextMask and SOUTH) != 0
                if (upOpen || downOpen) {
                    atIntersection = true
                }
            } else if (dy != 0) {
                val leftOpen = (nextMask and WEST) != 0
                val rightOpen = (nextMask and EAST) != 0
                if (leftOpen || rightOpen) {
                    atIntersection = true
                }
            }

            if (atIntersection) {
                break
            }
        }

        if (movedAny) {
            lastStepModeMoveTimeMs = System.currentTimeMillis()
            _uiState.update { it.copy(activeGame = currentGame) }
            if (currentGame.isCelebratingWin) {
                startWinCelebrationCountdown()
                return true
            }
            checkWolfPlayerCollision()
        }
        return movedAny
    }

    private fun startWinCelebrationCountdown() {
        if (winCelebrationJob?.isActive == true) return
        timerJob?.cancel()
        wolfJob?.cancel()
        soundManager.playStarChime()
        hapticManager.performWinFeedback()

        winCelebrationJob = viewModelScope.launch {
            delay(1000L) // Đúng 1 giây hiệu ứng bắn ánh sáng tại lá cờ rồi mới tính chiến thắng
            val latest = _uiState.value.activeGame
            if (latest.isCelebratingWin && !latest.gameOver) {
                _uiState.update {
                    it.copy(
                        activeGame = it.activeGame.copy(
                            isCelebratingWin = false,
                            hasWon = true,
                            gameOver = true
                        )
                    )
                }
                handleWin()
            }
        }
    }

    /**
     * Kiểm tra va chạm giữa Người Chơi và Sói:
     * - Nếu đứng cùng 1 ô (hoặc chạm nhau):
     *   + Nếu có Khiên (shieldCount > 0): Tự động kích hoạt Khiên bảo vệ 1 lần chạm, làm choáng Sói 2.2 giây!
     *   + Nếu không có Khiên: Thua cuộc!
     */
    private fun checkWolfPlayerCollision() {
        val state = _uiState.value
        val game = state.activeGame
        if (game.gameOver || game.isCelebratingWin || !game.wolfActive) return
        val wolf = game.wolfPos
        val cub = game.wolfCubPos
        val atWolf = wolf != null && wolf.x == game.player.x && wolf.y == game.player.y
        val atCub = cub != null && cub.x == game.player.x && cub.y == game.player.y
        if (!atWolf && !atCub) return

        val now = System.currentTimeMillis()
        if (now < game.wolfStunnedUntilMs) return

        val currentProgress = state.progress
        val isVi = state.language == AppLanguage.VI
        if (game.freeAegisShieldInRun > 0) {
            // Kích hoạt Khiên Aegis Cổ Đại (+1 Khiên miễn phí mỗi màn Sói Đuổi) trước tiên!
            soundManager.playStarChime()
            hapticManager.performWinFeedback()
            _uiState.update {
                it.copy(
                    toastMessage = if (isVi) {
                        "🛡️ Khiên Aegis Cổ Đại tự động kích hoạt! Đã chặn Sói miễn phí & làm choáng Sói!"
                    } else {
                        "🛡️ Ancient Aegis Shield Auto-Activated! Blocked & stunned the Wolf for free!"
                    },
                    activeGame = it.activeGame.copy(
                        freeAegisShieldInRun = maxOf(0, it.activeGame.freeAegisShieldInRun - 1),
                        wolfStunnedUntilMs = now + 2200L,
                        shieldTriggeredCount = it.activeGame.shieldTriggeredCount + 1,
                        wolfCubActive = false,
                        wolfCubPos = null,
                        wolfCubDistanceTraveled = 0
                    )
                )
            }
        } else if (currentProgress.shieldCount > 0) {
            // Tự động kích hoạt Khiên bảo vệ người chơi 1 lần chạm vào Sói!
            val updatedProgress = currentProgress.copy(
                shieldCount = maxOf(0, currentProgress.shieldCount - 1)
            )
            soundManager.playStarChime()
            hapticManager.performWinFeedback()
            _uiState.update {
                it.copy(
                    progress = updatedProgress,
                    toastMessage = if (isVi) {
                        "🛡️ Khiên Hộ Mệnh tự động kích hoạt! Đã chặn Sói & làm choáng Sói (Còn ${updatedProgress.shieldCount} Khiên)!"
                    } else {
                        "🛡️ Shield Auto-Activated! Blocked & stunned the Wolf (${updatedProgress.shieldCount} Shields left)!"
                    },
                    activeGame = it.activeGame.copy(
                        wolfStunnedUntilMs = now + 2200L,
                        shieldTriggeredCount = it.activeGame.shieldTriggeredCount + 1,
                        wolfCubActive = false,
                        wolfCubPos = null,
                        wolfCubDistanceTraveled = 0
                    )
                )
            }
            viewModelScope.launch {
                repository.saveProgress(updatedProgress)
            }
        } else {
            val lossMsg = if (isVi) {
                "🐺 Sói dữ đã đuổi kịp bạn! Hãy mua Khiên dùng 1 lần hoặc trang bị Khiên Aegis Cổ Đại trong Cửa Hàng!"
            } else {
                "🐺 The Wolf caught you! Buy single-use Shields or equip Ancient Aegis Shield in the Shop!"
            }
            handleLoss(lossMsg)
        }
    }

    fun handleWinDirect() {
        val game = _uiState.value.activeGame
        if (game.gameOver || game.hasWon) return
        if (game.isCelebratingWin) {
            winCelebrationJob?.cancel()
            _uiState.update {
                it.copy(
                    activeGame = it.activeGame.copy(
                        isCelebratingWin = false,
                        hasWon = true,
                        gameOver = true
                    )
                )
            }
            handleWin()
        } else {
            _uiState.update {
                it.copy(activeGame = it.activeGame.copy(isCelebratingWin = true))
            }
            startWinCelebrationCountdown()
        }
    }

    /**
     * Hoàn tác 1 bước đi trong Mê cung (Undo)
     */
    fun undoMove(fromCanvas: Boolean = false): Boolean {
        val game = _uiState.value.activeGame
        if (game.gameOver || game.isCelebratingWin || game.isPaused || game.pathHistory.size <= 1) return false
        val nowMs = System.currentTimeMillis()
        if (nowMs < game.playerFrozenUntilMs) return false
        val isAuto = game.controlMode == GridControlMode.AUTO
        if (!isAuto && !fromCanvas) {
            if (nowMs - lastStepModeMoveTimeMs < 100L) {
                return false
            }
        }
        lastStepModeMoveTimeMs = nowMs
        val maze = game.maze ?: return false
        val newHistory = game.pathHistory.dropLast(1)
        val prevPoint = newHistory.last()
        val newVisited = newHistory.map { it.y * maze.w + it.x }.toSet()
        val newGoalHint = game.goalDistCache?.let {
            MazeSolver.computeGoalDirectionHint(maze, prevPoint, it)
        }
        val isWolfChase = game.levelDef?.isWolfChase == true
        val wolfPt = if (isWolfChase) (game.wolfPos ?: maze.start) else null
        val distToWolf = if (isWolfChase && wolfPt != null) {
            MazeSolver.computePathDistance(maze, wolfPt, prevPoint)
        } else 0

        soundManager.playStep()

        _uiState.update {
            it.copy(
                activeGame = it.activeGame.copy(
                    player = prevPoint,
                    moves = maxOf(0, it.activeGame.moves - 1),
                    visitedCells = newVisited,
                    pathHistory = newHistory,
                    goalDirectionHint = newGoalHint,
                    wolfDistanceCells = distToWolf
                )
            )
        }
        checkWolfPlayerCollision()
        return true
    }

    /**
     * Đi lại từ đầu màn hiện tại (Reset / Vẽ lại)
     */
    fun resetCurrentLevel() {
        val game = _uiState.value.activeGame
        val maze = game.maze ?: return
        val def = game.levelDef ?: return
        soundManager.playClick()
        if (def.isWolfChase && !game.gameOver && (game.moves > 0 || game.elapsedSec >= getWolfHeadStartSeconds(def))) {
            consumeWolfGearDurabilityIfNeeded()
        }
        winCelebrationJob?.cancel()
        val progress = _uiState.value.progress
        val equippedGear = ShopCatalog.normalizeGearId(progress.equippedGearId)
        val activeGear = if (progress.getGearDurability(equippedGear) > 0) equippedGear else ""
        val freeAegis = if (def.isWolfChase && activeGear == "shield_aegis") 1 else 0
        val goalHint = game.goalDistCache?.let {
            MazeSolver.computeGoalDirectionHint(maze, maze.start, it)
        }
        _uiState.update {
            it.copy(
                activeGame = it.activeGame.copy(
                    player = maze.start,
                    moves = 0,
                    elapsedSec = 0,
                    timeLimitSec = computeLevelTimeLimitSec(def, activeGear),
                    visitedCells = setOf(maze.start.y * maze.w + maze.start.x),
                    pathHistory = listOf(maze.start),
                    goalDirectionHint = goalHint,
                    wolfPos = if (def.isWolfChase) maze.start else null,
                    wolfCubPos = null,
                    wolfCubActive = false,
                    wolfCubDistanceTraveled = 0,
                    wolfCountdownSec = if (def.isWolfChase) getWolfHeadStartSeconds(def) else 0,
                    wolfActive = false,
                    wolfFrenzy = false,
                    wolfSpeedMs = MazeConfig.WOLF_NORMAL_STEP_MS,
                    wolfDistanceCells = 0,
                    wolfStunnedUntilMs = 0L,
                    playerFrozenUntilMs = 0L,
                    wolfNextIceThrowAtMs = 0L,
                    wolfIceBeamUntilMs = 0L,
                    freeAegisShieldInRun = freeAegis,
                    gearDurabilityConsumedInRun = false,
                    isCelebratingWin = false,
                    gameOver = false,
                    hasWon = false
                )
            )
        }
        startTimer()
    }

    /**
     * Cài đặt chế độ điều khiển ('AUTO' hoặc 'STEP_BY_STEP')
     */
    fun setControlMode(mode: GridControlMode) {
        _uiState.update {
            it.copy(activeGame = it.activeGame.copy(controlMode = mode))
        }
    }

    /**
     * Bật/tắt chuyển đổi chế độ điều khiển (dùng cho nút Toggle trên UI)
     */
    fun toggleControlMode() {
        val current = _uiState.value.activeGame.controlMode
        val next = if (current == GridControlMode.AUTO) {
            GridControlMode.STEP_BY_STEP
        } else {
            GridControlMode.AUTO
        }
        setControlMode(next)
    }

    fun handleOneLineWin(steps: Int) {
        val game = _uiState.value.activeGame
        _uiState.update {
            it.copy(activeGame = it.activeGame.copy(moves = steps))
        }
        handleWin()
    }

    fun updateOneLineProgress(unlockedIndex: Int) {
        val currentProgress = _uiState.value.progress
        if (unlockedIndex > currentProgress.oneLineHighestUnlocked) {
            val updated = currentProgress.copy(
                oneLineHighestUnlocked = unlockedIndex,
                coins = currentProgress.coins + 30
            )
            viewModelScope.launch {
                repository.saveProgress(updated)
            }
        }
    }

    fun onOneLinePathChanged(pathSize: Int) {
        val game = _uiState.value.activeGame
        if (game.moves != pathSize) {
            _uiState.update {
                it.copy(activeGame = it.activeGame.copy(moves = pathSize))
            }
        }
    }

    private fun handleWin(customProgress: GameProgressEntity? = null) {
        pauseTimer()
        soundManager.playWin()
        hapticManager.performWinFeedback()

        val game = _uiState.value.activeGame
        val levelDef = game.levelDef ?: return
        val currentProgress = customProgress ?: _uiState.value.progress

        var earnedStars = 3
        var spc = 0.0
        if (!levelDef.isFinal && !levelDef.noTimer && levelDef.numericLevel != 1) {
            spc = game.elapsedSec.toDouble() / maxOf(1, levelDef.target)
            earnedStars = maxOf(1, MazeConfig.starsFromPace(spc))
        }

        var justUnlockedImpossible = false
        var justUnlockedWolfMode = false
        var justUnlockedNextTier = false
        var nextTierDef: LevelDef? = null
        var justClearedSuper = false

        var newHighest = currentProgress.highestCleared
        var newWolfHighest = currentProgress.wolfHighestCleared
        var newImpCleared = currentProgress.impossibleCleared
        var newSuperCleared = currentProgress.superCleared
        val isAlreadyCleared = _uiState.value.levelRecords.containsKey(levelDef.id)
        var newCoins = currentProgress.coins
        var newKeys = currentProgress.keys
        var newShields = currentProgress.shieldCount
        if (!isAlreadyCleared) {
            newCoins += 50
            newKeys += 1
        }

        var newGems = currentProgress.gems
        var dailyStage1Time = currentProgress.dailyStage1Time
        var dailyStage2Time = currentProgress.dailyStage2Time
        var dailyStage3Time = currentProgress.dailyStage3Time
        var nextDailyStage = currentProgress.dailyStage
        var dailyAllDone = currentProgress.dailyAllCompleted
        var newDailyAttempts = currentProgress.dailyAttemptsUsed

        if (levelDef.tier == "DAILY") {
            val stageNum = when {
                levelDef.id.contains("STAGE_1") -> 1
                levelDef.id.contains("STAGE_2") -> 2
                levelDef.id.contains("STAGE_3") -> 3
                else -> currentProgress.dailyStage.coerceIn(1, 3)
            }

            when (stageNum) {
                1 -> {
                    dailyStage1Time = game.elapsedSec
                    nextDailyStage = 2
                    newDailyAttempts = maxOf(newDailyAttempts, 1)
                    newCoins += 50
                }
                2 -> {
                    dailyStage2Time = game.elapsedSec
                    nextDailyStage = 3
                    newDailyAttempts = maxOf(newDailyAttempts, 2)
                    newCoins += 75
                }
                3 -> {
                    dailyStage3Time = game.elapsedSec
                    nextDailyStage = 4
                    dailyAllDone = true
                    newDailyAttempts = 3
                    newCoins += 150
                    newGems += 5
                    newKeys += 2
                }
            }
        }

        if (levelDef.isWolfChase && levelDef.numericLevel != null) {
            newWolfHighest = maxOf(currentProgress.wolfHighestCleared, levelDef.numericLevel)
            if (!isAlreadyCleared) {
                newCoins += 40
            }
        } else if (!levelDef.isFinal && levelDef.numericLevel != null) {
            val wasUnlocked = currentProgress.highestCleared >= MazeConfig.PASSES_TO_UNLOCK_IMPOSSIBLE
            val wasWolfUnlocked = currentProgress.highestCleared >= MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE
            newHighest = maxOf(currentProgress.highestCleared, levelDef.numericLevel)
            justUnlockedImpossible = !wasUnlocked && (newHighest >= MazeConfig.PASSES_TO_UNLOCK_IMPOSSIBLE)
            justUnlockedWolfMode = !wasWolfUnlocked && (newHighest >= MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE)
        } else if (levelDef.tier == "SUPER") {
            if (!currentProgress.superCleared) {
                newSuperCleared = true
                justClearedSuper = true
                newCoins += 1000
                newKeys += 5
            }
        } else if (levelDef.tier != null && levelDef.tier != "DAILY" && !levelDef.isWolfChase) {
            val tierNum = levelDef.tier.toIntOrNull() ?: 1
            val impSpc = game.elapsedSec.toDouble() / maxOf(1, levelDef.target)
            if (impSpc < 0.5 && tierNum == currentProgress.impossibleCleared + 1) {
                newImpCleared = tierNum
                justUnlockedNextTier = true
                nextTierDef = if (tierNum < MazeConfig.IMPOSSIBLE_TIER_SIZES.size) {
                    MazeConfig.impossibleDefForTier(tierNum + 1)
                } else {
                    MazeConfig.superDef()
                }
            }
        }

        val pathJson = if (levelDef.isReplaySupported) {
            val historyArr = JSONArray()
            game.pathHistory.forEach { pt -> historyArr.put("${pt.x},${pt.y}") }
            historyArr.toString()
        } else {
            ""
        }
        val mazeSeedStr = game.maze?.seedStr ?: ""

        val baseFinalProgress = currentProgress.copy(
            highestCleared = newHighest,
            wolfHighestCleared = newWolfHighest,
            impossibleCleared = newImpCleared,
            superCleared = newSuperCleared,
            coins = newCoins,
            keys = newKeys,
            shieldCount = newShields,
            gems = newGems,
            totalStars = currentProgress.totalStars + earnedStars,
            dailyStage = nextDailyStage,
            dailyAttemptsUsed = newDailyAttempts,
            dailyStage1Time = dailyStage1Time,
            dailyStage2Time = dailyStage2Time,
            dailyStage3Time = dailyStage3Time,
            dailyAllCompleted = dailyAllDone
        )

        val (finalProgress, gearBrokenMsg) = if (levelDef.isWolfChase && !game.gearDurabilityConsumedInRun) {
            applyWolfGearDurabilityConsumption(baseFinalProgress)
        } else {
            baseFinalProgress to null
        }

        val shouldShowInterstitial = registerGameClearedAndShouldShowInterstitial(finalProgress)
        // Cập nhật ngay lập tức vào _uiState để mọi màn chơi tiếp theo đều dùng dữ liệu mới nhất đã bị trừ token/cộng coin
        _uiState.update {
            it.copy(
                progress = finalProgress,
                toastMessage = gearBrokenMsg ?: it.toastMessage,
                pendingInterstitial = shouldShowInterstitial,
                activeGame = it.activeGame.copy(
                    gameOver = true,
                    hasWon = true,
                    earnedStars = earnedStars,
                    secPerCell = spc,
                    gearDurabilityConsumedInRun = true,
                    collectedShieldsInRun = it.activeGame.collectedShieldsInRun + (newShields - currentProgress.shieldCount),
                    justUnlockedImpossible = justUnlockedImpossible,
                    justUnlockedWolfMode = justUnlockedWolfMode,
                    justUnlockedNextTier = justUnlockedNextTier,
                    nextTierDef = nextTierDef,
                    justClearedSuper = justClearedSuper
                )
            )
        }

        viewModelScope.launch {
            // Save record with path history, seed, and dimensions
            repository.recordLevelCompletion(
                levelId = levelDef.id,
                stars = earnedStars,
                timeSec = game.elapsedSec,
                moves = game.moves,
                pathHistoryJson = pathJson,
                seedStr = mazeSeedStr,
                width = levelDef.w,
                height = levelDef.h
            )

            // Clear saved slot if matching
            val currentSlot = _uiState.value.saveSlot
            if (currentSlot != null && currentSlot.defId == levelDef.id) {
                repository.clearSaveSlot()
            }

            // Update achievements
            if (levelDef.numericLevel != null) {
                repository.updateAchievementProgress("first_step", absolute = newHighest)
                repository.updateAchievementProgress("fifty_levels", absolute = newHighest)
            }
            if (earnedStars == 3) {
                repository.updateAchievementProgress("perfect_speed", increment = 1)
            }
            if (game.wallHits == 0) {
                repository.updateAchievementProgress("no_wall_hit", increment = 1)
            }
            if (levelDef.vision != null) {
                repository.updateAchievementProgress("fog_master", increment = 1)
            }
            if (levelDef.tier == "DAILY") {
                repository.updateAchievementProgress("daily_champion", increment = 1)
            }

            // Save final progress to DB
            repository.saveProgress(finalProgress)
        }
    }

    private fun handleLoss(reason: String) {
        pauseTimer()
        soundManager.playLose()
        val game = _uiState.value.activeGame
        val isWolfChase = game.levelDef?.isWolfChase == true
        val oldProgress = _uiState.value.progress
        val (updatedProgress, brokenMsg) = if (isWolfChase && !game.gearDurabilityConsumedInRun) {
            applyWolfGearDurabilityConsumption(oldProgress)
        } else {
            oldProgress to null
        }
        val finalMsg = if (brokenMsg != null) "$reason\n$brokenMsg" else reason
        _uiState.update {
            it.copy(
                progress = updatedProgress,
                toastMessage = finalMsg,
                activeGame = it.activeGame.copy(
                    gameOver = true,
                    hasWon = false,
                    gearDurabilityConsumedInRun = true
                )
            )
        }
        if (updatedProgress != oldProgress || isWolfChase) {
            viewModelScope.launch {
                repository.saveProgress(updatedProgress)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        wolfJob?.cancel()
        winCelebrationJob?.cancel()
        val initialDef = _uiState.value.activeGame.levelDef
        // Không tính thời gian ở Màn 1 game Mê Cung (trừ chế độ Sói đuổi)
        if (!((initialDef?.isWolfChase) ?: false) && (initialDef?.numericLevel == 1 || (initialDef?.noTimer == true && !initialDef.isFinal))) {
            timerJob = null
            wolfJob = null
            return
        }
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _uiState.value.activeGame
                val levelDef = current.levelDef
                if (!(levelDef?.isWolfChase ?: false) && (levelDef?.numericLevel == 1 || (levelDef?.noTimer == true && !levelDef.isFinal))) {
                    break
                }
                if (!current.gameOver && !current.isCelebratingWin && !current.isPaused && !_uiState.value.showFirstLaunchLanguageDialog) {
                    val newSec = current.elapsedSec + 1
                    val limitSec = current.timeLimitSec
                    if (limitSec > 0 && newSec > limitSec) {
                        val isVi = _uiState.value.language == AppLanguage.VI
                        handleLoss(if (isVi) "⏱️ Hết thời gian thử thách — Thua!" else "⏱️ Time's up — Game Over!")
                        break
                    } else if (limitSec == 0 && levelDef != null && !levelDef.isFinal && !levelDef.noTimer && levelDef.target > 0) {
                        val spc = newSec.toDouble() / levelDef.target
                        if (spc > 2.0) {
                            handleLoss("⏱️ Quá thời gian (Tốc độ > 2.0s/ô) — Thua!")
                            break
                        }
                    }

                    val isWolfMode = levelDef?.isWolfChase == true
                    val remainingSec = if (limitSec > 0) maxOf(0, limitSec - newSec) else 999
                    val remainingRatio = if (limitSec > 0) remainingSec.toFloat() / limitSec.toFloat() else 1f
                    val isFrenzy = isWolfMode && limitSec > 0 && remainingRatio <= 0.30f
                    val currentWolfSpeedMs = when {
                        !isWolfMode -> MazeConfig.WOLF_NORMAL_STEP_MS
                        limitSec > 0 && remainingRatio <= 0.30f -> MazeConfig.WOLF_FRENZY_STEP_MS // 0.3s / 1 ô
                        limitSec > 0 && remainingRatio <= 0.50f -> MazeConfig.WOLF_HALF_TIME_STEP_MS // 0.35s / 1 ô
                        else -> MazeConfig.WOLF_NORMAL_STEP_MS // 0.4s / 1 ô
                    }

                    // Sói bắt đầu truy đuổi sau 3 giây (màn 1-5) hoặc 5 giây (từ màn 6) kể từ khi bắt đầu trò chơi
                    val headStartSec = getWolfHeadStartSeconds(levelDef)
                    val newWolfCountdown = if (isWolfMode) maxOf(0, headStartSec - newSec) else 0
                    val justActivatedWolf = isWolfMode && !current.wolfActive && newSec >= headStartSec
                    val wolfNowActive = current.wolfActive || justActivatedWolf
                    val isVi = _uiState.value.language == AppLanguage.VI
                    val wolfStartToast = if (justActivatedWolf) {
                        if (isVi) "🐺 Đã qua $headStartSec giây — Sói dữ bắt đầu truy đuổi!"
                        else "🐺 $headStartSec seconds elapsed — The Wolf starts chasing!"
                    } else null

                    _uiState.update {
                        it.copy(
                            toastMessage = wolfStartToast ?: it.toastMessage,
                            activeGame = it.activeGame.copy(
                                elapsedSec = newSec,
                                wolfCountdownSec = newWolfCountdown,
                                wolfActive = wolfNowActive,
                                wolfFrenzy = isFrenzy,
                                wolfSpeedMs = currentWolfSpeedMs
                            )
                        )
                    }
                    if (justActivatedWolf) {
                        checkWolfPlayerCollision()
                    }
                }
            }
        }

        if (initialDef?.isWolfChase == true) {
            startWolfChaseLoop()
        }
    }

    private fun startWolfChaseLoop() {
        wolfJob?.cancel()
        wolfJob = viewModelScope.launch {
            while (isActive) {
                val current = _uiState.value.activeGame
                val levelDef = current.levelDef
                if (levelDef?.isWolfChase != true || current.gameOver || current.isCelebratingWin) {
                    if (current.gameOver || current.isCelebratingWin) break
                    delay(80)
                    continue
                }
                if (current.isPaused || !current.wolfActive || _uiState.value.showFirstLaunchLanguageDialog) {
                    delay(80)
                    continue
                }
                val now = System.currentTimeMillis()
                if (now < current.wolfStunnedUntilMs) {
                    delay(80)
                    continue
                }

                val limitSec = current.timeLimitSec
                val remainingSec = if (limitSec > 0) maxOf(0, limitSec - current.elapsedSec) else 999
                val remainingRatio = if (limitSec > 0) remainingSec.toFloat() / limitSec.toFloat() else 1f
                // Tốc độ Sói:
                // - Bình thường: 0.4 giây / 1 ô (400ms)
                // - Thời gian còn dưới 1 nửa (<50%): 0.35 giây / 1 ô (350ms)
                // - Thời gian còn dưới 30% (<30%): 0.3 giây / 1 ô (300ms)
                val isFrenzy = limitSec > 0 && remainingRatio <= 0.30f
                val stepIntervalMs = when {
                    limitSec > 0 && remainingRatio <= 0.30f -> MazeConfig.WOLF_FRENZY_STEP_MS
                    limitSec > 0 && remainingRatio <= 0.50f -> MazeConfig.WOLF_HALF_TIME_STEP_MS
                    else -> MazeConfig.WOLF_NORMAL_STEP_MS
                }

                delay(stepIntervalMs)

                val latest = _uiState.value.activeGame
                if (latest.gameOver || latest.isCelebratingWin || latest.isPaused || !latest.wolfActive) continue
                val afterDelayNow = System.currentTimeMillis()
                if (afterDelayNow < latest.wolfStunnedUntilMs) continue

                val maze = latest.maze ?: continue
                val wolfPos = latest.wolfPos ?: maze.start
                val nextWolfPos = MazeSolver.computeNextWolfStep(maze, wolfPos, latest.player)
                val distAfterStep = MazeSolver.computePathDistance(maze, nextWolfPos, latest.player)

                // Kỹ năng Sói Triệu Hồi Sói Con:
                // - Khi khoảng cách từ 25 ô trở lên, sói triệu hồi sói con nhỏ và chạy nhanh (2 bước / nhịp)
                // - Tầm chạy là 50 ô, ngoài 50 ô sói con biến mất
                var cubPos = latest.wolfCubPos
                var cubActive = latest.wolfCubActive
                var cubDist = latest.wolfCubDistanceTraveled
                var justSummonedCub = false

                if (distAfterStep >= MazeConfig.WOLF_ICE_MIN_DISTANCE_CELLS && !cubActive) {
                    cubActive = true
                    cubPos = nextWolfPos
                    cubDist = 0
                    justSummonedCub = true
                }

                if (cubActive && cubPos != null) {
                    val cubStep1 = MazeSolver.computeNextWolfStep(maze, cubPos, latest.player)
                    cubDist += 1
                    if (cubStep1 == latest.player) {
                        cubPos = cubStep1
                    } else {
                        val cubStep2 = MazeSolver.computeNextWolfStep(maze, cubStep1, latest.player)
                        cubPos = cubStep2
                        cubDist += 1
                    }
                    val distCubToPlayer = MazeSolver.computePathDistance(maze, cubPos, latest.player)
                    if (cubPos != latest.player && (cubDist > 50 || distCubToPlayer > 50)) {
                        cubActive = false
                        cubPos = null
                        cubDist = 0
                    }
                }

                // Kỹ năng Sói Ném Băng Xuyên Tường:
                // - Chỉ có tác dụng khi khoảng cách giữa người chơi và sói là trên 25 ô (> 25 ô)
                // - Người chơi dính bị đóng băng 2 giây (2000ms)
                // - Thời gian hồi của sói là 15 giây (15000ms)
                val canCastIceThrow = distAfterStep > MazeConfig.WOLF_ICE_MIN_DISTANCE_CELLS &&
                    afterDelayNow >= latest.wolfNextIceThrowAtMs
                val newFrozenUntilMs = if (canCastIceThrow) {
                    afterDelayNow + MazeConfig.WOLF_ICE_FREEZE_DURATION_MS
                } else {
                    latest.playerFrozenUntilMs
                }
                val newNextIceThrowAtMs = if (canCastIceThrow) {
                    afterDelayNow + MazeConfig.WOLF_ICE_COOLDOWN_MS
                } else {
                    latest.wolfNextIceThrowAtMs
                }
                val newIceBeamUntilMs = if (canCastIceThrow) {
                    afterDelayNow + MazeConfig.WOLF_ICE_FREEZE_DURATION_MS
                } else {
                    latest.wolfIceBeamUntilMs
                }

                if (canCastIceThrow) {
                    soundManager.playBump()
                    hapticManager.performWallHitFeedback()
                }

                val isVi = _uiState.value.language == AppLanguage.VI
                val eventToast = when {
                    canCastIceThrow -> {
                        if (isVi) "❄️ Sói ném Băng Xuyên Tường (Khoảng cách $distAfterStep ô)! Bạn bị đóng băng 2 giây! (Hồi chiêu: 15s)"
                        else "❄️ Wolf cast Wall-Piercing Ice (Distance $distAfterStep cells)! Frozen for 2s! (Cooldown: 15s)"
                    }
                    justSummonedCub -> {
                        if (isVi) "🐕 Sói triệu hồi Sói Con chạy nhanh truy đuổi (Tầm chạy: 50 ô)!"
                        else "🐕 Wolf summoned a fast Wolf Cub to chase you (Range: 50 cells)!"
                    }
                    else -> null
                }

                _uiState.update {
                    it.copy(
                        toastMessage = eventToast ?: it.toastMessage,
                        activeGame = it.activeGame.copy(
                            wolfPos = nextWolfPos,
                            wolfCubPos = cubPos,
                            wolfCubActive = cubActive,
                            wolfCubDistanceTraveled = cubDist,
                            wolfFrenzy = isFrenzy,
                            wolfSpeedMs = stepIntervalMs,
                            wolfDistanceCells = distAfterStep,
                            playerFrozenUntilMs = newFrozenUntilMs,
                            wolfNextIceThrowAtMs = newNextIceThrowAtMs,
                            wolfIceBeamUntilMs = newIceBeamUntilMs
                        )
                    )
                }
                checkWolfPlayerCollision()
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        wolfJob?.cancel()
        wolfJob = null
    }

    private fun resumeTimer() {
        if (timerJob == null || timerJob?.isActive == false) {
            startTimer()
        }
    }

    fun requestExit() {
        val game = _uiState.value.activeGame
        if (!game.gameOver) {
            pauseTimer()
            _uiState.update { it.copy(activeGame = it.activeGame.copy(showExitDialog = true)) }
        } else {
            navigateTo(AppScreen.HOME)
        }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(activeGame = it.activeGame.copy(showExitDialog = false)) }
        resumeTimer()
    }

    fun saveAndExit() {
        val game = _uiState.value.activeGame
        val maze = game.maze ?: return
        val def = game.levelDef ?: return

        val visitedArr = JSONArray(game.visitedCells.toList())
        val historyArr = JSONArray(game.pathHistory.map { "${it.x},${it.y}" })

        val slot = SaveSlotEntity(
            id = 1,
            defId = def.id,
            tier = def.tier,
            w = def.w,
            h = def.h,
            seedStr = maze.seedStr,
            playerX = game.player.x,
            playerY = game.player.y,
            moves = game.moves,
            elapsedSec = game.elapsedSec,
            visitedIndicesJson = visitedArr.toString(),
            pathHistoryJson = historyArr.toString()
        )

        viewModelScope.launch {
            repository.saveGameSlot(slot)
            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.HOME,
                    toastMessage = "✓ Đã lưu ván chơi thành công!"
                )
            }
        }
    }

    fun discardAndExit() {
        val currentSlot = _uiState.value.saveSlot
        val defId = _uiState.value.activeGame.levelDef?.id
        viewModelScope.launch {
            if (currentSlot != null && currentSlot.defId == defId) {
                repository.clearSaveSlot()
            }
            _uiState.update { it.copy(currentScreen = AppScreen.HOME) }
        }
    }

    // Replay mode controls (chỉ hỗ trợ cho màn dưới 50x50)
    fun startReplay() {
        val def = _uiState.value.activeGame.levelDef
        if (def != null && !def.isReplaySupported) return
        val history = _uiState.value.activeGame.pathHistory
        if (history.isEmpty()) return
        soundManager.playClick()
        pauseTimer()
        replayJob?.cancel()

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.REPLAY,
                replay = ReplayState(currentIndex = 0, isPlaying = true, speed = 1f)
            )
        }
        playReplayLoop()
    }

    private fun playReplayLoop() {
        replayJob?.cancel()
        replayJob = viewModelScope.launch {
            while (isActive) {
                val replay = _uiState.value.replay
                val history = _uiState.value.activeGame.pathHistory
                if (!replay.isPlaying) {
                    delay(100)
                    continue
                }

                if (replay.currentIndex < history.size - 1) {
                    val nextIdx = replay.currentIndex + 1
                    soundManager.playStep()
                    _uiState.update { it.copy(replay = it.replay.copy(currentIndex = nextIdx)) }
                    val interval = (240 / replay.speed).toLong().coerceAtLeast(40)
                    delay(interval)
                } else {
                    _uiState.update { it.copy(replay = it.replay.copy(isPlaying = false)) }
                    soundManager.playStarChime()
                    break
                }
            }
        }
    }

    fun toggleReplayPlayPause() {
        val playing = !_uiState.value.replay.isPlaying
        _uiState.update { it.copy(replay = it.replay.copy(isPlaying = playing)) }
        if (playing) {
            val history = _uiState.value.activeGame.pathHistory
            if (_uiState.value.replay.currentIndex >= history.size - 1) {
                _uiState.update { it.copy(replay = it.replay.copy(currentIndex = 0)) }
            }
            playReplayLoop()
        } else {
            replayJob?.cancel()
        }
    }

    fun setReplaySpeed(multiplier: Float) {
        _uiState.update { it.copy(replay = it.replay.copy(speed = multiplier)) }
    }

    fun seekReplay(stepIndex: Int) {
        replayJob?.cancel()
        val history = _uiState.value.activeGame.pathHistory
        if (history.isEmpty()) return
        val clamped = stepIndex.coerceIn(0, history.size - 1)
        _uiState.update {
            it.copy(replay = it.replay.copy(currentIndex = clamped, isPlaying = false))
        }
    }

    fun loadAndStartReplay(levelId: String) {
        soundManager.playClick()
        pauseTimer()
        replayJob?.cancel()

        viewModelScope.launch {
            val record = repository.getLevelRecord(levelId)
            val historyList = if (record != null && record.pathHistoryJson.isNotEmpty()) {
                try {
                    val jsonArr = JSONArray(record.pathHistoryJson)
                    (0 until jsonArr.length()).map { idx ->
                        val str = jsonArr.getString(idx)
                        val parts = str.split(",")
                        Point(parts[0].toInt(), parts[1].toInt())
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

            if (historyList.isEmpty()) {
                _uiState.update { it.copy(toastMessage = "Chưa có dữ liệu xem lại cho màn này") }
                return@launch
            }

            val isImp = levelId.startsWith("IMPOSSIBLE")
            val isDaily = levelId.startsWith("DAILY")
            val def = when {
                isDaily -> {
                    val dateStr = levelId.removePrefix("DAILY_")
                    LevelDef(
                        id = "DAILY_$dateStr",
                        numericLevel = null,
                        tier = "DAILY",
                        w = DailyChallengeManager.DAILY_SIZE,
                        h = DailyChallengeManager.DAILY_SIZE,
                        target = DailyChallengeManager.DAILY_TARGET,
                        moveCap = null,
                        vision = MazeConfig.VISION_WINDOW,
                        isFinal = false,
                        noTimer = false
                    )
                }
                isImp -> {
                    if (levelId.contains("SUPER")) MazeConfig.superDef()
                    else {
                        val tierNum = levelId.removePrefix("IMPOSSIBLE_").toIntOrNull() ?: 1
                        MazeConfig.impossibleDefForTier(tierNum)
                    }
                }
                else -> {
                    val num = levelId.toIntOrNull() ?: 1
                    MazeConfig.generateLevelDef(num)
                }
            }

            if (!def.isReplaySupported) {
                return@launch
            }

            val seed = record?.seedStr?.toULongOrNull() ?: 42UL
            val maze = MazeBuilder.buildMaze(def.w, def.h, def.target, seed, def.algorithmIndex)
            val distCache = MazeSolver.computeDistances(maze)

            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.REPLAY,
                    activeGame = ActiveGameState(
                        maze = maze,
                        levelDef = def,
                        player = historyList.firstOrNull() ?: maze.start,
                        moves = historyList.size - 1,
                        elapsedSec = record?.bestTimeSec ?: 0,
                        visitedCells = historyList.map { pt -> pt.y * maze.w + pt.x }.toSet(),
                        pathHistory = historyList,
                        distCache = distCache,
                        gameOver = true,
                        hasWon = true
                    ),
                    replay = ReplayState(currentIndex = 0, isPlaying = true, speed = 1f)
                )
            }
            playReplayLoop()
        }
    }

    // Interstitial & Rewarded Ad triggers
    fun onOneLineGameCleared(activity: Activity?) {
        if (registerGameClearedAndShouldShowInterstitial(_uiState.value.progress)) {
            triggerInterstitial(activity)
        }
    }

    fun triggerInterstitial(activity: Activity?) {
        val p = _uiState.value.progress
        if (p.isInterstitialAdAllowed() && activity != null) {
            val shown = adManager.showInterstitialAd(activity) {
                _uiState.update { it.copy(pendingInterstitial = false) }
            }
            if (!shown) {
                _uiState.update { it.copy(pendingInterstitial = false) }
            }
        } else {
            _uiState.update { it.copy(pendingInterstitial = false) }
        }
    }

    fun dismissInterstitial() {
        _uiState.update { it.copy(pendingInterstitial = false) }
    }

    fun showRewardedAdPrompt(rewardType: String) {
        _uiState.update { it.copy(rewardAdPrompt = rewardType) }
    }

    fun dismissRewardedAd() {
        _uiState.update { it.copy(rewardAdPrompt = null) }
    }

    fun triggerRewardedAd(activity: Activity?, rewardType: String) {
        if (rewardType == "TIME" && _uiState.value.activeGame.levelDef?.isWolfChase == true) {
            return
        }
        if (activity != null) {
            val shown = adManager.showRewardedAd(
                activity = activity,
                onRewardEarned = { onRewardEarned(rewardType) },
                onDismissed = { dismissRewardedAd() }
            )
            if (!shown) {
                adManager.loadRewardedAd()
            }
        }
    }

    fun onRewardEarned(type: String) {
        soundManager.playStarChime()
        val currentProgress = _uiState.value.progress
        val newAdViews = currentProgress.accumulatedRewardedAdViews + 1
        var newVipTier = currentProgress.vipTier
        var newVipExpires = currentProgress.vipExpiresAtMillis

        // Đủ 30 lần xem kích hoạt trạng thái VIP 1 (3 ngày không có quảng cáo)
        var unlockedVip1Notice = false
        if (newAdViews >= AdConstants.VIP1_REQUIRED_AD_VIEWS && currentProgress.vipTier == 0) {
            newVipTier = 1
            newVipExpires = System.currentTimeMillis() + AdConstants.VIP1_DURATION_MILLIS
            unlockedVip1Notice = true
        }

        val baseProgress = currentProgress.copy(
            accumulatedRewardedAdViews = newAdViews,
            vipTier = newVipTier,
            vipExpiresAtMillis = newVipExpires
        )

        when (type) {
            "HINT" -> {
                val now = networkTimeManager.getCurrentInternetTimeMillis()
                val todayId = now / 86400000L
                val storedDayId = currentProgress.lastHintAdDate / 86400000L
                val hintViewsToday = if (storedDayId == todayId) currentProgress.hintAdViewsToday else 0
                val newHintViewsToday = hintViewsToday + 1

                val newProgress = baseProgress.copy(
                    hintCount = baseProgress.hintCount + 1,
                    hintAdViewsToday = newHintViewsToday,
                    lastHintAdDate = now
                )
                soundManager.playStarChime()
                _uiState.update {
                    it.copy(
                        progress = newProgress,
                        rewardAdPrompt = null,
                        toastMessage = if (unlockedVip1Notice)
                            "🎉 Đã tích lũy đủ 30 lượt xem & KÍCH HOẠT VIP 1 (3 ngày)!"
                        else
                            "🎉 Đã nhận thành công +1 lượt Gợi ý (Hôm nay: $newHintViewsToday/2 lần)!"
                    )
                }
                viewModelScope.launch {
                    repository.saveProgress(newProgress)
                }
            }
            "SKIP" -> {
                viewModelScope.launch {
                    val result = levelSkipController.completeAdSkip(baseProgress)
                    when (result) {
                        is LevelSkipController.SkipExecutionResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    progress = result.updatedProgress,
                                    rewardAdPrompt = null,
                                    toastMessage = if (unlockedVip1Notice)
                                        "🎉 Đã tích lũy đủ 30 lượt xem & KÍCH HOẠT VIP 1 (3 ngày)!"
                                    else
                                        result.message
                                )
                            }
                        }
                        is LevelSkipController.SkipExecutionResult.CooldownNotElapsed -> {
                            _uiState.update {
                                it.copy(
                                    rewardAdPrompt = null,
                                    toastMessage = "⏳ Thời gian hồi chiêu chưa hết (${result.formattedTime} còn lại). Không thể nhận lượt bỏ qua!"
                                )
                            }
                        }
                        is LevelSkipController.SkipExecutionResult.InternetRequired -> {
                            _uiState.update {
                                it.copy(
                                    rewardAdPrompt = null,
                                    toastMessage = "🌐 ${result.message}"
                                )
                            }
                        }
                        is LevelSkipController.SkipExecutionResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    rewardAdPrompt = null,
                                    toastMessage = result.reason
                                )
                            }
                        }
                    }
                }
            }
            "TIME" -> {
                val current = _uiState.value.activeGame
                val reduced = maxOf(0, current.elapsedSec - 30)
                viewModelScope.launch {
                    repository.saveProgress(baseProgress)
                }
                _uiState.update {
                    it.copy(
                        rewardAdPrompt = null,
                        toastMessage = if (unlockedVip1Notice)
                            "🎉 Đã tích lũy đủ 30 lượt xem & KÍCH HOẠT VIP 1 (3 ngày)!"
                        else
                            "✓ Đã thêm 30 giây (+1 tích lũy VIP 1: $newAdViews/30)!",
                        activeGame = it.activeGame.copy(elapsedSec = reduced)
                    )
                }
            }
            "COINS" -> {
                val newProgress = baseProgress.copy(coins = baseProgress.coins + 50)
                viewModelScope.launch {
                    repository.saveProgress(newProgress)
                }
                _uiState.update {
                    it.copy(
                        rewardAdPrompt = null,
                        toastMessage = if (unlockedVip1Notice)
                            "🎉 Đã tích lũy đủ 30 lượt xem & KÍCH HOẠT VIP 1 (3 ngày)!"
                        else
                            "✓ Nhận thành công +50 Xu (+1 tích lũy VIP 1: $newAdViews/30)!"
                    )
                }
            }
        }
    }

    fun useHint(activity: Activity? = null) {
        val currentProgress = _uiState.value.progress
        val now = networkTimeManager.getCurrentInternetTimeMillis()
        if (currentProgress.hintCount > 0) {
            val updatedProgress = currentProgress.copy(hintCount = maxOf(0, currentProgress.hintCount - 1))
            soundManager.playStarChime()
            _uiState.update {
                it.copy(
                    progress = updatedProgress,
                    activeGame = it.activeGame.copy(showHint = true),
                    toastMessage = "💡 Đã sử dụng 1 lượt gợi ý đường đi!"
                )
            }
            viewModelScope.launch {
                repository.saveProgress(updatedProgress)
            }
        } else if (currentProgress.canHint(now)) {
            triggerRewardedAd(activity, "HINT")
        } else {
            _uiState.update {
                it.copy(toastMessage = "⏳ Bạn đã đạt giới hạn xem quảng cáo gợi ý trong ngày (tối đa 2 lần/ngày). Vui lòng quay lại vào ngày mai!")
            }
        }
    }

    fun skipCurrentLevel(activity: Activity? = null) {
        val currentProgress = _uiState.value.progress
        val availability = levelSkipController.checkSkipAvailability(currentProgress)

        when (availability) {
            is LevelSkipController.SkipAvailability.AvailableWithToken -> {
                val updatedProgress = currentProgress.copy(
                    skipTokens = maxOf(0, currentProgress.skipTokens - 1)
                )
                soundManager.playStarChime()
                _uiState.update {
                    it.copy(
                        progress = updatedProgress,
                        toastMessage = "Đã sử dụng 1 lượt bỏ qua màn chơi!"
                    )
                }
                viewModelScope.launch {
                    levelSkipController.executeTokenSkip(currentProgress)
                    handleWin(customProgress = updatedProgress)
                }
            }
            is LevelSkipController.SkipAvailability.AvailableWithAd -> {
                viewModelScope.launch {
                    when (val result = levelSkipController.validateAdSkipEligibility(currentProgress)) {
                        is LevelSkipController.SkipExecutionResult.Success -> {
                            triggerRewardedAd(activity, "SKIP")
                        }
                        is LevelSkipController.SkipExecutionResult.CooldownNotElapsed -> {
                            _uiState.update {
                                it.copy(toastMessage = "⏳ Bạn đã đạt giới hạn xem quảng cáo skip (1 lần mỗi 2 ngày). Cần đợi thêm ${result.formattedTime} nữa!")
                            }
                        }
                        is LevelSkipController.SkipExecutionResult.InternetRequired -> {
                            _uiState.update { it.copy(toastMessage = "🌐 ${result.message}") }
                        }
                        is LevelSkipController.SkipExecutionResult.Error -> {
                            _uiState.update { it.copy(toastMessage = result.reason) }
                        }
                    }
                }
            }
            is LevelSkipController.SkipAvailability.CooldownActive -> {
                _uiState.update {
                    it.copy(toastMessage = "⏳ Bạn đã đạt giới hạn xem quảng cáo skip (1 lần mỗi 2 ngày). Cần đợi thêm ${availability.formattedRemainingTime} nữa!")
                }
            }
            is LevelSkipController.SkipAvailability.RequiresInternet -> {
                _uiState.update {
                    it.copy(toastMessage = "🌐 ${availability.message}")
                }
            }
        }
    }

    // =========================================================================
    // VIP MONETIZATION METHODS
    // =========================================================================

    /**
     * Kích hoạt VIP 1 bằng 2,000 Vàng (Luôn giữ cấp VIP cao nhất người chơi sở hữu)
     */
     fun activateVip1WithCoins() {
         val p = _uiState.value.progress
         if (p.coins < AdConstants.VIP1_COINS_PRICE) {
             _uiState.update { it.copy(toastMessage = "Bạn cần đủ 2,000 Xu để kích hoạt VIP 1!") }
             return
         }
         val now = networkTimeManager.getCurrentInternetTimeMillis()
         val currentHighestTier = p.highestOwnedVipTier(now)
         val newTier = maxOf(currentHighestTier, 1)
         val baseTime = if (currentHighestTier in 1..2 && p.vipExpiresAtMillis > now) p.vipExpiresAtMillis else now
         val expires = if (newTier == 3) Long.MAX_VALUE else baseTime + AdConstants.VIP1_DURATION_MILLIS
         val updated = p.copy(
             coins = p.coins - AdConstants.VIP1_COINS_PRICE,
             vipTier = newTier,
             isVipNoAds = (newTier == 3 || p.isVipNoAds),
             vipExpiresAtMillis = expires
         )
         viewModelScope.launch {
             repository.saveProgress(updated)
         }
         soundManager.playStarChime()
         _uiState.update { it.copy(toastMessage = "🎉 Chúc mừng! Đã kích hoạt quyền lợi VIP (Cấp hiện tại: VIP $newTier)!") }
     }

    /**
     * Kích hoạt VIP 1 miễn phí bằng 30 lượt xem quảng cáo thưởng (Luôn giữ cấp VIP cao nhất người chơi sở hữu)
     */
    fun activateVip1WithAdViews() {
        val p = _uiState.value.progress
        if (p.accumulatedRewardedAdViews < AdConstants.VIP1_REQUIRED_AD_VIEWS) {
            _uiState.update {
                it.copy(toastMessage = "Bạn cần tích lũy đủ 30 lượt xem quảng cáo (${p.accumulatedRewardedAdViews}/30)!")
            }
            return
        }
        val now = networkTimeManager.getCurrentInternetTimeMillis()
        val currentHighestTier = p.highestOwnedVipTier(now)
        val newTier = maxOf(currentHighestTier, 1)
        val baseTime = if (currentHighestTier in 1..2 && p.vipExpiresAtMillis > now) p.vipExpiresAtMillis else now
        val expires = if (newTier == 3) Long.MAX_VALUE else baseTime + AdConstants.VIP1_DURATION_MILLIS
        val updated = p.copy(
            vipTier = newTier,
            isVipNoAds = (newTier == 3 || p.isVipNoAds),
            vipExpiresAtMillis = expires
        )
        viewModelScope.launch {
            repository.saveProgress(updated)
        }
        soundManager.playStarChime()
        _uiState.update { it.copy(toastMessage = "🎉 Chúc mừng! Đã kích hoạt VIP từ 30 lượt xem (Cấp hiện tại: VIP $newTier)!") }
    }

    /**
     * Cấu hình tùy chọn cho VIP 1: Tắt banner
     */
    fun toggleVip1Banner(disabled: Boolean) {
        val p = _uiState.value.progress
        val updated = p.copy(vip1DisableBanner = disabled)
        viewModelScope.launch {
            repository.saveProgress(updated)
        }
        _uiState.update {
            it.copy(toastMessage = if (disabled) "✓ Đã tắt quảng cáo Banner góc dưới màn hình" else "✓ Đã bật lại quảng cáo Banner")
        }
    }

    /**
     * Cấu hình tùy chọn cho VIP 1: Tắt quảng cáo sau game (interstitial)
     */
    fun toggleVip1Interstitial(disabled: Boolean) {
        val p = _uiState.value.progress
        val updated = p.copy(vip1DisableInterstitial = disabled)
        viewModelScope.launch {
            repository.saveProgress(updated)
        }
        _uiState.update {
            it.copy(toastMessage = if (disabled) "✓ Đã tắt quảng cáo sau khi thắng ván" else "✓ Đã bật lại quảng cáo sau khi thắng ván")
        }
    }

    /**
     * Mua gói VIP 2 hoặc VIP 3 qua Google Play In-App Billing
     */
    fun purchaseVipRealMoney(activity: Activity?, tier: Int) {
        if (activity != null) {
            billingManager.purchaseVip(activity, tier) {
                onVipPurchaseGranted(tier)
            }
        } else {
            onVipPurchaseGranted(tier)
        }
    }

    /**
     * Mở khóa quyền lợi khi giao dịch hoàn tất (Luôn giữ cấp độ VIP cao nhất người chơi sở hữu)
     */
    fun onVipPurchaseGranted(tier: Int) {
        val p = _uiState.value.progress
        val now = networkTimeManager.getCurrentInternetTimeMillis()
        val currentHighestTier = p.highestOwnedVipTier(now)
        when (tier) {
            2 -> {
                val newTier = maxOf(currentHighestTier, 2)
                val baseTime = if (currentHighestTier in 1..2 && p.vipExpiresAtMillis > now) p.vipExpiresAtMillis else now
                val expires = if (newTier == 3) Long.MAX_VALUE else baseTime + AdConstants.VIP2_DURATION_MILLIS
                val updated = p.copy(
                    vipTier = newTier,
                    isVipNoAds = (newTier == 3 || p.isVipNoAds),
                    vipExpiresAtMillis = expires,
                    hintCount = p.hintCount + AdConstants.VIP2_BONUS_HINTS,
                    skipTokens = p.skipTokens + AdConstants.VIP2_BONUS_SKIPS
                )
                viewModelScope.launch {
                    repository.saveProgress(updated)
                }
                soundManager.playStarChime()
                _uiState.update {
                    it.copy(toastMessage = "👑 Chúc mừng! Đã kích hoạt gói VIP 2 (+5 gợi ý, +2 bỏ qua • Cấp hiện tại: VIP $newTier)!")
                }
            }
            3 -> {
                val updated = p.copy(
                    vipTier = 3,
                    vipExpiresAtMillis = Long.MAX_VALUE,
                    isVipNoAds = true,
                    hintCount = p.hintCount + AdConstants.VIP3_BONUS_HINTS,
                    skipTokens = p.skipTokens + AdConstants.VIP3_BONUS_SKIPS
                )
                viewModelScope.launch {
                    repository.saveProgress(updated)
                }
                soundManager.playStarChime()
                _uiState.update {
                    it.copy(toastMessage = "👑 Huyền Thoại! Đã mở khóa VIP 3 VĨNH VIỄN (Tắt mọi quảng cáo, +15 gợi ý, +5 bỏ qua)!")
                }
            }
        }
    }

    /**
     * Tự động kiểm tra và đồng bộ cấp độ VIP cao nhất hoặc hạ cấp nếu gói có thời hạn đã hết hạn
     */
    fun checkAndExpireVipIfNeeded() {
        val p = _uiState.value.progress
        if (p.isVipNoAds && p.vipTier < 3) {
            // Người chơi đã sở hữu VIP 3 vĩnh viễn -> luôn giữ cấp cao nhất là VIP 3
            val restored = p.copy(vipTier = 3, vipExpiresAtMillis = Long.MAX_VALUE)
            viewModelScope.launch {
                repository.saveProgress(restored)
            }
            return
        }
        if (!p.isVipNoAds && p.vipTier in 1..2 && p.vipExpiresAtMillis > 0L) {
            val now = networkTimeManager.getCurrentInternetTimeMillis()
            if (now >= p.vipExpiresAtMillis) {
                val expired = p.copy(vipTier = 0, vipExpiresAtMillis = 0L)
                viewModelScope.launch {
                    repository.saveProgress(expired)
                }
                _uiState.update {
                    it.copy(toastMessage = "Gói VIP của bạn đã hết hạn. Hãy gia hạn để tiếp tục nhận đặc quyền!")
                }
            }
        }
    }

    // Shop actions
    fun buySkin(skinId: String) {
        val skin = ShopCatalog.getSkinById(skinId)
        val p = _uiState.value.progress
        if (p.coins >= skin.priceCoins) {
            val unlocked = p.unlockedSkins.split(",").toMutableSet()
            unlocked.add(skinId)
            val updated = p.copy(
                coins = p.coins - skin.priceCoins,
                unlockedSkins = unlocked.joinToString(","),
                currentSkinId = skinId
            )
            viewModelScope.launch {
                repository.saveProgress(updated)
                repository.updateAchievementProgress("skin_collector", absolute = unlocked.size)
            }
            soundManager.playStarChime()
            _uiState.update { it.copy(toastMessage = "Đã mua & trang bị skin ${skin.name}!") }
        } else {
            _uiState.update { it.copy(toastMessage = "Không đủ xu! Hãy xem quảng cáo hoặc hoàn thành thêm màn.") }
        }
    }

    fun equipSkin(skinId: String) {
        val p = _uiState.value.progress
        viewModelScope.launch {
            repository.saveProgress(p.copy(currentSkinId = skinId))
        }
        soundManager.playClick()
    }

    fun buyTheme(themeId: String) {
        val theme = ShopCatalog.getThemeById(themeId)
        val p = _uiState.value.progress
        if (p.coins >= theme.priceCoins) {
            val unlocked = p.unlockedThemes.split(",").toMutableSet()
            unlocked.add(themeId)
            val updated = p.copy(
                coins = p.coins - theme.priceCoins,
                unlockedThemes = unlocked.joinToString(","),
                currentMazeThemeId = themeId
            )
            viewModelScope.launch {
                repository.saveProgress(updated)
            }
            soundManager.playStarChime()
            _uiState.update { it.copy(toastMessage = "Đã mua & đổi giao diện ${theme.name}!") }
        } else {
            _uiState.update { it.copy(toastMessage = "Không đủ xu!") }
        }
    }

    fun equipTheme(themeId: String) {
        val p = _uiState.value.progress
        viewModelScope.launch {
            repository.saveProgress(p.copy(currentMazeThemeId = themeId))
        }
        soundManager.playClick()
    }

    fun openWolfEquipmentDialog() {
        soundManager.playClick()
        _uiState.update { it.copy(showWolfEquipmentDialog = true) }
    }

    fun closeWolfEquipmentDialog() {
        _uiState.update { it.copy(showWolfEquipmentDialog = false) }
    }

    private fun applyGearToActiveWolfGameIfNeeded(gearId: String) {
        val active = _uiState.value.activeGame
        val def = active.levelDef ?: return
        if (!def.isWolfChase) return
        val normalized = ShopCatalog.normalizeGearId(gearId)
        val newTimeLimit = computeLevelTimeLimitSec(def, normalized)
        val newFreeAegis = if (normalized == "shield_aegis" && active.shieldTriggeredCount == 0) 1 else 0
        _uiState.update {
            it.copy(
                activeGame = it.activeGame.copy(
                    timeLimitSec = newTimeLimit,
                    freeAegisShieldInRun = newFreeAegis
                )
            )
        }
    }

    fun buyEquipment(gearId: String, useKeys: Boolean = false) {
        val normalizedId = ShopCatalog.normalizeGearId(gearId)
        val gear = ShopCatalog.getEquipmentById(normalizedId) ?: return
        val p = _uiState.value.progress
        val isVi = _uiState.value.language == AppLanguage.VI
        val unlocked = p.unlockedGears
            .split(",")
            .map { ShopCatalog.normalizeGearId(it) }
            .filter { it.isNotEmpty() && p.getGearDurability(it) > 0 }
            .toMutableSet()

        if (unlocked.contains(normalizedId) && p.getGearDurability(normalizedId) > 0) {
            equipEquipment(normalizedId)
            return
        }

        if (p.coins >= gear.priceCoins) {
            unlocked.add(normalizedId)
            val baseUpdated = p.copy(
                coins = p.coins - gear.priceCoins,
                unlockedGears = unlocked.joinToString(","),
                equippedGearId = normalizedId
            )
            val updated = when (normalizedId) {
                "boots_haste" -> baseUpdated.copy(bootsHasteDurability = gear.maxDurability)
                "shield_aegis" -> baseUpdated.copy(shieldAegisDurability = gear.maxDurability)
                "compass_vision" -> baseUpdated.copy(compassVisionDurability = gear.maxDurability)
                else -> baseUpdated
            }
            viewModelScope.launch { repository.saveProgress(updated) }
            soundManager.playStarChime()
            _uiState.update {
                it.copy(
                    progress = updated,
                    toastMessage = if (isVi) "🎉 Đã mua & trang bị ${gear.nameVi} (Độ bền: ${gear.maxDurability} ván Sói Đuổi)!" else "🎉 Purchased & equipped ${gear.nameEn} (Durability: ${gear.maxDurability} Wolf Chase runs)!"
                )
            }
            applyGearToActiveWolfGameIfNeeded(normalizedId)
        } else {
            _uiState.update {
                it.copy(toastMessage = if (isVi) "🪙 Cần ${gear.priceCoins} Xu để mua ${gear.nameVi}!" else "🪙 Need ${gear.priceCoins} Coins to buy ${gear.nameEn}!")
            }
        }
    }

    fun equipEquipment(gearId: String) {
        val normalizedId = ShopCatalog.normalizeGearId(gearId)
        val p = _uiState.value.progress
        val isVi = _uiState.value.language == AppLanguage.VI
        if (normalizedId.isNotEmpty() && p.getGearDurability(normalizedId) <= 0) {
            buyEquipment(normalizedId)
            return
        }
        val currentNormalized = ShopCatalog.normalizeGearId(p.equippedGearId)
        val nextGear = if (currentNormalized == normalizedId) "" else normalizedId
        val updated = p.copy(equippedGearId = nextGear)
        viewModelScope.launch {
            repository.saveProgress(updated)
        }
        soundManager.playClick()
        val gearObj = ShopCatalog.getEquipmentById(nextGear)
        _uiState.update {
            it.copy(
                progress = updated,
                toastMessage = if (gearObj != null) {
                    val dur = updated.getGearDurability(nextGear)
                    if (isVi) "✓ Đã trang bị ${gearObj.nameVi} (Còn $dur/${gearObj.maxDurability} ván Sói Đuổi)" else "✓ Equipped ${gearObj.nameEn} ($dur/${gearObj.maxDurability} runs left)"
                } else {
                    if (isVi) "Đã tháo trang bị hỗ trợ" else "Unequipped support gear"
                }
            )
        }
        applyGearToActiveWolfGameIfNeeded(nextGear)
    }

    fun buyShieldPack(useKeys: Boolean = false) {
        val p = _uiState.value.progress
        val isVi = _uiState.value.language == AppLanguage.VI
        val shieldPriceCoins = ShopCatalog.SINGLE_USE_SHIELD_PRICE_COINS
        val shieldPriceKeys = ShopCatalog.SINGLE_USE_SHIELD_PRICE_KEYS
        if (useKeys) {
            if (p.keys >= shieldPriceKeys) {
                val updated = p.copy(keys = p.keys - shieldPriceKeys, shieldCount = p.shieldCount + 1)
                viewModelScope.launch { repository.saveProgress(updated) }
                soundManager.playStarChime()
                _uiState.update {
                    it.copy(
                        progress = updated,
                        toastMessage = if (isVi) "🛡️ Đã đổi $shieldPriceKeys Chìa Khóa lấy +1 Khiên Dùng 1 Lần (Mất ngay sau khi kích hoạt)!" else "🛡️ Exchanged $shieldPriceKeys Keys for +1 Single-Use Shield!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(toastMessage = if (isVi) "🔑 Cần $shieldPriceKeys Chìa Khóa để đổi 1 Khiên Dùng 1 Lần!" else "🔑 Need $shieldPriceKeys Keys for 1 Single-Use Shield!")
                }
            }
        } else {
            if (p.coins >= shieldPriceCoins) {
                val updated = p.copy(coins = p.coins - shieldPriceCoins, shieldCount = p.shieldCount + 1)
                viewModelScope.launch { repository.saveProgress(updated) }
                soundManager.playStarChime()
                _uiState.update {
                    it.copy(
                        progress = updated,
                        toastMessage = if (isVi) "🛡️ Đã mua +1 Khiên Dùng 1 Lần (Mất ngay sau khi kích hoạt)!" else "🛡️ Purchased +1 Single-Use Shield (Consumed immediately on activation)!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(toastMessage = if (isVi) "🪙 Cần $shieldPriceCoins Xu để mua 1 Khiên Dùng 1 Lần!" else "🪙 Need $shieldPriceCoins Coins for 1 Single-Use Shield!")
                }
            }
        }
    }

    fun buySingleUseHint() {
        val p = _uiState.value.progress
        val isVi = _uiState.value.language == AppLanguage.VI
        val price = ShopCatalog.SINGLE_USE_HINT_PRICE_COINS
        if (p.coins >= price) {
            val updated = p.copy(coins = p.coins - price, hintCount = p.hintCount + 1)
            viewModelScope.launch { repository.saveProgress(updated) }
            soundManager.playStarChime()
            _uiState.update {
                it.copy(
                    progress = updated,
                    toastMessage = if (isVi) "💡 Đã mua +1 Gợi Ý Dùng 1 Lần (Mất ngay sau khi kích hoạt)!" else "💡 Purchased +1 Single-Use Hint!"
                )
            }
        } else {
            _uiState.update {
                it.copy(toastMessage = if (isVi) "🪙 Cần $price Xu để mua 1 Gợi Ý Dùng 1 Lần!" else "🪙 Need $price Coins for 1 Single-Use Hint!")
            }
        }
    }

    fun buySingleUseSkip() {
        val p = _uiState.value.progress
        val isVi = _uiState.value.language == AppLanguage.VI
        val price = ShopCatalog.SINGLE_USE_SKIP_PRICE_COINS
        if (p.coins >= price) {
            val updated = p.copy(coins = p.coins - price, skipTokens = p.skipTokens + 1)
            viewModelScope.launch { repository.saveProgress(updated) }
            soundManager.playStarChime()
            _uiState.update {
                it.copy(
                    progress = updated,
                    toastMessage = if (isVi) "⏭️ Đã mua +1 Vé Bỏ Qua Dùng 1 Lần (Mất ngay sau khi kích hoạt)!" else "⏭️ Purchased +1 Single-Use Skip Token!"
                )
            }
        } else {
            _uiState.update {
                it.copy(toastMessage = if (isVi) "🪙 Cần $price Xu để mua 1 Vé Bỏ Qua Dùng 1 Lần!" else "🪙 Need $price Coins for 1 Single-Use Skip Token!")
            }
        }
    }

    fun consumeOneLineHint(activity: Activity? = null): Boolean {
        val p = _uiState.value.progress
        val isVi = _uiState.value.language == AppLanguage.VI
        if (p.hintCount > 0) {
            val updated = p.copy(hintCount = maxOf(0, p.hintCount - 1))
            viewModelScope.launch { repository.saveProgress(updated) }
            soundManager.playStarChime()
            _uiState.update {
                it.copy(
                    progress = updated,
                    toastMessage = if (isVi) "💡 Đã kích hoạt 1 Gợi Ý Dùng 1 Lần (Đã trừ ngay 1 lượt, còn ${updated.hintCount})!" else "💡 Activated 1 Single-Use Hint (${updated.hintCount} left)!"
                )
            }
            return true
        }
        val price = ShopCatalog.SINGLE_USE_HINT_PRICE_COINS
        if (p.coins >= price) {
            val updated = p.copy(coins = p.coins - price)
            viewModelScope.launch { repository.saveProgress(updated) }
            soundManager.playStarChime()
            _uiState.update {
                it.copy(
                    progress = updated,
                    toastMessage = if (isVi) "💡 Đã mua & kích hoạt ngay 1 Gợi Ý Dùng 1 Lần (-$price Xu)!" else "💡 Bought & activated 1 Single-Use Hint (-$price Coins)!"
                )
            }
            return true
        }
        val now = networkTimeManager.getCurrentInternetTimeMillis()
        if (p.canHint(now)) {
            triggerRewardedAd(activity, "HINT")
        } else {
            _uiState.update {
                it.copy(toastMessage = if (isVi) "🪙 Cần 1 Gợi Ý hoặc $price Xu để bật Hướng Dẫn!" else "🪙 Need 1 Hint or $price Coins to activate Hint!")
            }
        }
        return false
    }

    fun buyVipPass() {
        val p = _uiState.value.progress
        val unlockedSkins = p.unlockedSkins.split(",").toMutableSet().apply { add("vip_crown") }
        val updated = p.copy(
            vipTier = 3,
            vipExpiresAtMillis = Long.MAX_VALUE,
            isVipNoAds = true,
            coins = p.coins + 500,
            unlockedSkins = unlockedSkins.joinToString(","),
            currentSkinId = "vip_crown"
        )
        viewModelScope.launch {
            repository.saveProgress(updated)
        }
        soundManager.playStarChime()
        val lang = _uiState.value.language
        val vipMsg = if (lang == AppLanguage.VI) "👑 Chúc mừng! Bạn đã là thành viên VIP!" else "👑 Congratulations! You are already a VIP member!"
        _uiState.update { it.copy(toastMessage = vipMsg) }
    }

    fun claimAchievement(id: String) {
        viewModelScope.launch {
            val reward = repository.claimAchievement(id)
            if (reward > 0) {
                soundManager.playStarChime()
                _uiState.update { it.copy(toastMessage = "+$reward Xu từ thành tựu!") }
            }
        }
    }

    fun toggleSound() {
        val p = _uiState.value.progress
        val newVal = !p.soundEnabled
        soundManager.isSoundEnabled = newVal
        if (newVal) soundManager.playClick()
        viewModelScope.launch { repository.saveProgress(p.copy(soundEnabled = newVal)) }
    }

    fun toggleMusic() {
        soundManager.playClick()
        val p = _uiState.value.progress
        val newVal = !p.musicEnabled
        soundManager.isMusicEnabled = newVal
        viewModelScope.launch { repository.saveProgress(p.copy(musicEnabled = newVal)) }
    }

    fun toggleHaptics() {
        soundManager.playClick()
        val p = _uiState.value.progress
        val newVal = !p.hapticEnabled
        hapticManager.isHapticEnabled = newVal
        if (newVal) hapticManager.performMoveFeedback()
        viewModelScope.launch { repository.saveProgress(p.copy(hapticEnabled = newVal)) }
    }

    fun setMoveSensitivity(sensitivity: Float) {
        val clamped = Math.max(0.5f, Math.min(2.0f, Math.round(sensitivity * 100f) / 100f))
        val p = _uiState.value.progress
        val updated = p.copy(moveSensitivity = clamped)
        _uiState.update { it.copy(progress = updated) }
        viewModelScope.launch {
            repository.saveProgress(updated)
        }
    }

    fun toggleLanguage() {
        soundManager.playClick()
        hapticManager.performButtonFeedback()
        val currentLang = _uiState.value.language
        val nextLang = currentLang.other
        _uiState.update { it.copy(language = nextLang) }
        val p = _uiState.value.progress
        viewModelScope.launch {
            repository.saveProgress(p.copy(language = nextLang.code))
        }
    }

    fun setLanguage(lang: AppLanguage) {
        if (_uiState.value.language == lang) return
        soundManager.playClick()
        hapticManager.performButtonFeedback()
        _uiState.update { it.copy(language = lang) }
        val p = _uiState.value.progress
        viewModelScope.launch {
            repository.saveProgress(p.copy(language = lang.code))
        }
    }

    fun openCloudSync() {
        soundManager.playClick()
        _uiState.update { it.copy(showCloudSyncDialog = true) }
    }

    fun closeCloudSync() {
        _uiState.update { it.copy(showCloudSyncDialog = false) }
    }

    fun confirmCloudSync() {
        val now = System.currentTimeMillis()
        val p = _uiState.value.progress
        viewModelScope.launch {
            repository.saveProgress(p.copy(lastCloudSyncTime = now))
        }
        soundManager.playStarChime()
    }

    fun openResetModal() {
        soundManager.playClick()
        _uiState.update { it.copy(showResetModal = true) }
    }

    fun closeResetModal() {
        _uiState.update { it.copy(showResetModal = false) }
    }

    fun confirmResetAll() {
        viewModelScope.launch {
            repository.resetAllProgress()
            val level1Def = MazeConfig.generateLevelDef(1)
            val seed = MazeBuilder.randomSeed()
            val level1Data = MazeGenerator.generateFromDef(level1Def, seed.toLong())
            val level1Maze = level1Data.toMaze()
            val level1Dist = MazeSolver.computeDistances(level1Maze)
            _uiState.update {
                it.copy(
                    showResetModal = false,
                    showFirstLaunchLanguageDialog = true,
                    currentScreen = AppScreen.PLAYING,
                    activeGame = ActiveGameState(
                        maze = level1Maze,
                        levelDef = level1Def,
                        player = level1Maze.start,
                        moves = 0,
                        elapsedSec = 0,
                        visitedCells = setOf(level1Maze.start.y * level1Maze.w + level1Maze.start.x),
                        pathHistory = listOf(level1Maze.start),
                        distCache = level1Dist
                    ),
                    toastMessage = if (it.language == AppLanguage.VI) "Đã xóa toàn bộ tiến trình!" else "All progress reset!"
                )
            }
        }
    }

    fun addCoins(amount: Int) {
        viewModelScope.launch {
            mutateProgress { currentProgress ->
                currentProgress.copy(coins = currentProgress.coins + amount)
            }
        }
    }

    fun completeOneLineLevel(levelIndex: Int, activity: Activity?) {
        viewModelScope.launch {
            mutateProgress { p ->
                val nextLevel = levelIndex + 1
                val isNewUnlock = nextLevel > p.oneLineHighestUnlocked
                val newHighest = maxOf(p.oneLineHighestUnlocked, nextLevel)
                val earnedCoins = if (isNewUnlock) 60 else 30
                p.copy(
                    oneLineHighestUnlocked = newHighest,
                    coins = p.coins + earnedCoins
                )
            }
            soundManager.playWin()
            hapticManager.performWinFeedback()
            onOneLineGameCleared(activity)
        }
    }

    fun deleteAccountAndData() {
        viewModelScope.launch {
            repository.resetAllProgress()
            val prefs = getApplication<Application>().getSharedPreferences("gamets_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            val initialProgress = GameProgressEntity(
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
            _uiState.update {
                it.copy(
                    progress = initialProgress,
                    currentScreen = AppScreen.HOME,
                    activeGame = ActiveGameState(),
                    toastMessage = if (it.language == AppLanguage.VI) "Đã xóa toàn bộ tài khoản và dữ liệu thành công!" else "Account and all data deleted successfully!"
                )
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun selectLeaderboardLevel(lvl: Int) {
        _uiState.update { it.copy(selectedLeaderboardLevel = lvl) }
    }
}
