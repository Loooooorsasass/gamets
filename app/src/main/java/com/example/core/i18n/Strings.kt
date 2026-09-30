package com.example.core.i18n

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    VI("vi", "Tiếng Việt", "🇻🇳"),
    EN("en", "English", "🇬🇧");

    val other: AppLanguage
        get() = if (this == VI) EN else VI
}

object Strings {
    private fun t(lang: AppLanguage, vi: String, en: String): String {
        return when (lang) {
            AppLanguage.VI -> vi
            AppLanguage.EN -> en
        }
    }

    // Top Bar & App Info
    fun appTitle(lang: AppLanguage) = t(lang, VietnameseTexts.APP_TITLE, "MazeX")
    fun appSubtitle(lang: AppLanguage) = t(lang, VietnameseTexts.APP_SUBTITLE, "Precision Step-Counting Maze")

    // Common navigation / buttons
    fun back(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_BACK, "Back")
    fun close(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_CLOSE, "Close")
    fun cancel(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_CANCEL, "Cancel")
    fun understood(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_UNDERSTOOD, "Got It")
    fun play(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_PLAY, "Play")
    fun playNow(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_PLAY_NOW, "Play Now")
    fun playAgain(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_PLAY_AGAIN, "Play Again")
    fun retry(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_RETRY, "Retry")
    fun pause(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_PAUSE, "Pause")
    fun undo(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_UNDO, "Undo")
    fun reset(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_RESET, "Reset")
    fun hint(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_HINT, "Hint")
    fun hideHint(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_HIDE_HINT, "Hide Hint")
    fun hintFree(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_FREE_HINT, "Free Hint")
    fun hintUsed(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_USED_HINT, "Used")
    fun hintFreeEnabled(lang: AppLanguage) = t(lang, VietnameseTexts.HINT_FREE_ON_MSG, "Free One-Line Path Hint activated!")
    fun hintOncePerLevel(lang: AppLanguage) = t(lang, VietnameseTexts.HINT_ONCE_PER_LEVEL_MSG, "Only 1 Free Hint allowed per level!")
    fun skip(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_SKIP, "Skip")
    fun watchAd(lang: AppLanguage) = t(lang, "Xem Ad", "Watch Ad")
    fun watchAdExtraTime(lang: AppLanguage) = t(lang, "📺 Xem Ad Nhận +30s Chơi Tiếp", "📺 Watch Ad for +30s Time")

    fun nextLevel(lang: AppLanguage, size: String = ""): String {
        val vi = if (size.isEmpty()) VietnameseTexts.BTN_NEXT_LEVEL else "${VietnameseTexts.BTN_NEXT_LEVEL} ($size)"
        val en = if (size.isEmpty()) "Next Level" else "Next Level ($size)"
        return t(lang, vi, en)
    }

    fun nextTier(lang: AppLanguage) = t(lang, "Tiếp Tục Tầng Tiếp Theo", "Continue to Next Tier")
    fun home(lang: AppLanguage) = t(lang, "Về Trang Chủ", "Home")
    fun backToHome(lang: AppLanguage) = t(lang, "Về Trang Chủ", "Back to Home")
    fun locked(lang: AppLanguage) = t(lang, VietnameseTexts.LEVEL_LOCKED, "Locked")
    fun free(lang: AppLanguage) = t(lang, "Miễn phí", "Free")
    fun reward(lang: AppLanguage) = t(lang, "Phần thưởng", "Reward")

    // Segmented Navigation tabs
    fun tabDaily(lang: AppLanguage) = t(lang, VietnameseTexts.NAV_DAILY, "Daily")
    fun tabLeaderboard(lang: AppLanguage) = t(lang, VietnameseTexts.NAV_LEADERBOARD, "Rankings")
    fun tabShop(lang: AppLanguage) = t(lang, VietnameseTexts.NAV_SHOP, "Shop")
    fun tabAchievements(lang: AppLanguage) = t(lang, VietnameseTexts.NAV_ACHIEVEMENTS, "Badges")

    // Home Screen - Resume Card
    fun resumeGameTitle(lang: AppLanguage) = t(lang, VietnameseTexts.RESUME_CARD_TITLE, "Resume Game")
    fun resumeGameSub(lang: AppLanguage, moves: Int, elapsedSec: Int): String {
        val vi = "Đã đi $moves bước · Thời gian: ${elapsedSec}s"
        val en = "$moves steps made · Time: ${elapsedSec}s"
        return t(lang, vi, en)
    }
    fun resumeButton(lang: AppLanguage) = t(lang, VietnameseTexts.BTN_RESUME, "Resume")

    // Home Screen - Daily Challenge Banner
    fun dailyBannerTitle(lang: AppLanguage) = t(lang, VietnameseTexts.DAILY_BANNER_TITLE, "Today's Maze Challenge")
    fun dailyBannerSub(lang: AppLanguage) = t(lang, VietnameseTexts.DAILY_BANNER_SUBTITLE, "Global 14×14 Maze · Earn +200 Coins")
    fun speedRace(lang: AppLanguage) = t(lang, VietnameseTexts.DAILY_BANNER_BTN, "Speed Run")

    // Home Screen - Impossible Progress Bar
    fun impossibleProgressTitle(lang: AppLanguage) = t(lang, VietnameseTexts.IMPOSSIBLE_PROGRESS_TITLE, "Progress to Unlock IMPOSSIBLE")
    fun levelsProgress(lang: AppLanguage, current: Int, total: Int): String {
        val vi = "$current / $total Màn"
        val en = "$current / $total Levels"
        return t(lang, vi, en)
    }

    // Home Screen - One-Line Mini Game Banner
    fun oneLineCardTitle(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_CARD_TITLE, "Mini-Game: One-Line Puzzle")
    fun oneLineUnlockedDesc(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_CARD_SUB_UNLOCKED, "Unlocked! 3×3 to 15×15 & Special Shapes")
    fun oneLineLockedDesc(lang: AppLanguage, current: Int, required: Int): String {
        val vi = "Cần qua Màn $required Mê Cung ($current/$required)"
        val en = "Requires Level $required Maze ($current/$required)"
        return t(lang, vi, en)
    }
    fun oneLineLockedDialogTitle(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_LOCKED_DIALOG_TITLE, "One-Line Puzzle is Locked 🔒")
    fun oneLineLockedDialogBody(lang: AppLanguage, current: Int): String {
        val vi = String.format(VietnameseTexts.ONELINE_LOCKED_DIALOG_BODY, current)
        val en = "Please complete at least 20 levels in the main Maze game to unlock One-Line Puzzle!\n\nYour current progress: $current / 20 Levels."
        return t(lang, vi, en)
    }

    // Home Screen - Level List
    fun levelListTitle(lang: AppLanguage) = t(lang, VietnameseTexts.LEVEL_SELECT_TITLE, "Level Select")
    fun showMoreLevels(lang: AppLanguage) = t(lang, VietnameseTexts.LEVEL_SHOW_MORE, "Show more levels ▾")

    // Home Screen - Impossible Mode Section
    fun impossibleSectionTitle(lang: AppLanguage) = t(lang, VietnameseTexts.IMPOSSIBLE_SECTION_TITLE, "IMPOSSIBLE MODE")
    fun impossibleUnlockedDesc(lang: AppLanguage) = t(lang, VietnameseTexts.IMPOSSIBLE_UNLOCKED_DESC, "Extreme tiers from 100×100 to 1000×1000 — Now equipped with the Live Goal Direction Radar Map!")
    fun impossibleLockedDesc(lang: AppLanguage) = t(lang, VietnameseTexts.IMPOSSIBLE_LOCKED_DESC, "Clear 50 regular levels (5×5 to 69×69) to unlock massive mazes with Live Goal Direction Radar Map.")
    fun superTierTitle(lang: AppLanguage) = t(lang, VietnameseTexts.SUPER_TIER_TITLE, "SUPER")
    fun stepsUnit(lang: AppLanguage, steps: Int): String {
        val vi = "$steps bước"
        val en = "$steps steps"
        return t(lang, vi, en)
    }

    // Settings & Bottom Home
    fun soundLabel(lang: AppLanguage) = t(lang, VietnameseTexts.SOUND_LABEL, "Sound")
    fun musicLabel(lang: AppLanguage) = t(lang, VietnameseTexts.MUSIC_LABEL, "Music")
    fun hapticLabel(lang: AppLanguage) = t(lang, VietnameseTexts.HAPTIC_LABEL, "Haptics")
    fun languageLabel(lang: AppLanguage) = t(lang, VietnameseTexts.LANGUAGE_LABEL, "Language")
    fun sensitivityLabel(lang: AppLanguage) = t(lang, "Độ nhạy di chuyển", "Move Sensitivity")
    fun sensitivitySub(lang: AppLanguage, percent: Int) = t(lang, "Độ nhạy: $percent%", "Sensitivity: $percent%")
    fun sensitivityDialogTitle(lang: AppLanguage) = t(lang, "⚡ Tùy Chỉnh Độ Nhạy Di Chuyển", "⚡ Adjust Move Sensitivity")
    fun sensitivityDesc(lang: AppLanguage) = t(lang, "Điều chỉnh tốc độ phản hồi khi vuốt kéo trên mê cung và phím D-Pad.", "Adjust responsiveness when swiping, dragging on maze and using D-Pad.")
    fun sensitivityPresetSlow(lang: AppLanguage) = t(lang, "50% Chậm", "50% Slow")
    fun sensitivityPresetNormal(lang: AppLanguage) = t(lang, "100% Chuẩn", "100% Default")
    fun sensitivityPresetFast(lang: AppLanguage) = t(lang, "150% Nhanh", "150% Fast")
    fun sensitivityPresetHyper(lang: AppLanguage) = t(lang, "200% Siêu Tốc", "200% Hyper")
    fun resetDataButton(lang: AppLanguage) = t(lang, "Đặt lại dữ liệu màn chơi", "Reset level progress")

    // Reset Progress Dialog
    fun resetDialogTitle(lang: AppLanguage) = t(lang, VietnameseTexts.RESET_DIALOG_TITLE, "⚠️ Reset Data?")
    fun resetDialogBody(lang: AppLanguage) = t(lang, VietnameseTexts.RESET_DIALOG_BODY, "This will reset all level progress, achievements, and records to 0. Coins and purchased items will be preserved.")
    fun resetCheckboxLabel(lang: AppLanguage) = t(lang, VietnameseTexts.RESET_CHECKBOX_LABEL, "I understand and wish to reset")
    fun resetConfirmButton(lang: AppLanguage) = t(lang, VietnameseTexts.RESET_CONFIRM_BTN, "Confirm Reset")
    fun resetToast(lang: AppLanguage) = t(lang, "Đã xóa toàn bộ tiến trình!", "All progress has been reset!")

    // Help Dialog (Home)
    fun helpDialogTitle(lang: AppLanguage) = t(lang, VietnameseTexts.HELP_DIALOG_TITLE, "Maze Game Instructions")
    fun helpLine1(lang: AppLanguage) = t(lang, VietnameseTexts.HELP_LINE_1, "• Control Modes:")
    fun helpLine2(lang: AppLanguage) = t(lang, VietnameseTexts.HELP_LINE_2, "  - ⚡ Auto: Swipe lightly for 1 cell. Swipe and hold to auto-glide until junctions or walls.")
    fun helpLine3(lang: AppLanguage) = t(lang, VietnameseTexts.HELP_LINE_3, "  - 👣 Step-by-Step: Each swipe precisely moves exactly 1 cell.")
    fun helpLine4(lang: AppLanguage) = t(lang, VietnameseTexts.HELP_LINE_4, "• Objective: Guide your character to the red flag / orange exit.")
    fun helpLine5(lang: AppLanguage) = t(lang, VietnameseTexts.HELP_LINE_5, "• Stars: Awarded based on your move speed (sec/step) vs target goal.")
    fun helpLine6(lang: AppLanguage) = t(lang, VietnameseTexts.HELP_LINE_6, "• Impossible Mode: Unlocks after Level 50, featuring 10×10 fog of war.")

    // Level Action Dialog (Level details popup)
    fun bestRecord(lang: AppLanguage) = t(lang, "Thành tích cao nhất:", "Personal Best:")
    fun timeLabel(lang: AppLanguage) = t(lang, "Thời gian:", "Time:")
    fun movesLabel(lang: AppLanguage) = t(lang, "Số bước đi:", "Moves taken:")
    fun targetLabel(lang: AppLanguage, target: Int): String {
        val vi = "mục tiêu: $target"
        val en = "target: $target"
        return t(lang, vi, en)
    }
    fun exactGoalLabel(lang: AppLanguage, target: Int): String {
        val vi = "Mục tiêu: đúng $target bước"
        val en = "Goal: exactly $target steps"
        return t(lang, vi, en)
    }
    fun replayRouteBtn(lang: AppLanguage) = t(lang, "🎬 Xem Lại Lộ Trình (Replay)", "🎬 Watch Replay")

    // In-Game Screen HUD & Elements
    fun preparingLevel(lang: AppLanguage) = t(lang, "Đang chuẩn bị màn chơi...", "Preparing level...")
    fun reachFlagHint(lang: AppLanguage) = t(lang, "🚩 Chạm Lá Cờ Đỏ để Về Đích", "🚩 Reach the Red Flag to Win")
    fun steps(lang: AppLanguage) = t(lang, VietnameseTexts.HUD_STEPS, "Steps")
    fun time(lang: AppLanguage) = t(lang, VietnameseTexts.HUD_TIME, "Time")
    fun coordinates(lang: AppLanguage) = t(lang, VietnameseTexts.HUD_COORDINATES, "Coordinates")
    fun controls(lang: AppLanguage) = t(lang, VietnameseTexts.CONTROLS_LABEL, "Controls:")
    fun controlAuto(lang: AppLanguage) = t(lang, VietnameseTexts.CONTROL_AUTO, "⚡ Auto")
    fun controlAutoSub(lang: AppLanguage) = t(lang, VietnameseTexts.CONTROL_AUTO_SUB, "Drag / Hold to Glide")
    fun controlStep(lang: AppLanguage) = t(lang, VietnameseTexts.CONTROL_STEP, "👣 Step-by-Step")
    fun controlStepSub(lang: AppLanguage) = t(lang, VietnameseTexts.CONTROL_STEP_SUB, "1 exact cell")

    // Exit Game Dialog
    fun exitTitle(lang: AppLanguage) = t(lang, VietnameseTexts.EXIT_DIALOG_TITLE, "Exit current game?")
    fun exitMessage(lang: AppLanguage, moves: Int, seconds: Int): String {
        val vi = VietnameseTexts.EXIT_DIALOG_BODY
        val en = "Made $moves moves in ${seconds}s. You can save to continue later."
        return t(lang, vi, en)
    }
    fun saveAndExit(lang: AppLanguage) = t(lang, VietnameseTexts.EXIT_DIALOG_SAVE_BTN, "Save & Exit")
    fun discardAndExit(lang: AppLanguage) = t(lang, VietnameseTexts.EXIT_DIALOG_QUIT_BTN, "Exit Without Saving")

    // Win / Loss Modals
    fun winTitle(lang: AppLanguage) = t(lang, VietnameseTexts.VICTORY_TITLE, "🎉 LEVEL CLEARED!")
    fun winStats(lang: AppLanguage, moves: Int, timeSec: Int): String {
        val vi = "$moves lượt di chuyển · ${timeSec}s hoàn thành"
        val en = "$moves moves taken · ${timeSec}s completed"
        return t(lang, vi, en)
    }
    fun impossibleUnlocked(lang: AppLanguage) = t(lang, "👑 Chúc mừng! Đã mở khóa IMPOSSIBLE!", "👑 Congratulations! IMPOSSIBLE Mode Unlocked!")
    fun superCleared(lang: AppLanguage) = t(lang, "👑 Huyền thoại! Bạn đã chinh phục SIÊU CẤP 1000×1000!", "👑 Legendary! You conquered SUPER 1000×1000!")
    fun nextTierUnlocked(lang: AppLanguage) = t(lang, "🔥 Tốc độ siêu việt! Đã mở tầng tiếp theo!", "🔥 Incredible speed! Next tier unlocked!")
    fun replayRoute(lang: AppLanguage) = t(lang, VietnameseTexts.VICTORY_REPLAY_BTN, "Watch Replay")
    fun lossTitle(lang: AppLanguage) = t(lang, "💔 CHƯA HOÀN THÀNH", "💔 LEVEL FAILED")
    fun reviewPath(lang: AppLanguage) = t(lang, "Xem Lại Quá Trình Đi", "Review Path Traveled")

    // Daily Challenge Screen (3 stages: 14x14, 15x15, 16x16, cooldown reset at 0h00)
    fun dailyTitle(lang: AppLanguage) = t(lang, "📅 Thử Thách Hàng Ngày", "📅 Daily Challenge")
    fun dailyDateSub(lang: AppLanguage, date: String): String {
        val vi = "Ngày: $date · Reset 0h00 mỗi ngày"
        val en = "Date: $date · Resets 00:00 Daily"
        return t(lang, vi, en)
    }
    fun dailyStageLabel(lang: AppLanguage, stage: Int): String {
        val vi = String.format(VietnameseTexts.DAILY_STAGE_LABEL, stage)
        val en = "Attempt $stage / 3"
        return t(lang, vi, en)
    }
    fun dailyStageTitle(lang: AppLanguage, stage: Int, size: Int): String {
        val vi = "Lượt $stage: Mê Cung $size×$size"
        val en = "Stage $stage: $size×$size Maze"
        return t(lang, vi, en)
    }
    fun dailyStageCleared(lang: AppLanguage, timeSec: Int): String {
        val vi = String.format(VietnameseTexts.DAILY_STAGE_CLEARED, timeSec)
        val en = "Cleared (${timeSec}s)"
        return t(lang, vi, en)
    }
    fun dailyStagePlaying(lang: AppLanguage) = t(lang, VietnameseTexts.DAILY_STAGE_PLAYING, "Ready to Play")
    fun dailyStageLocked(lang: AppLanguage) = t(lang, VietnameseTexts.DAILY_STAGE_LOCKED, "Pass Previous Stage")
    fun dailyCooldownPrefix(lang: AppLanguage) = t(lang, VietnameseTexts.DAILY_COOLDOWN_PREFIX, "Attempts reset in:")
    fun dailyCooldownTitle(lang: AppLanguage) = t(lang, VietnameseTexts.DAILY_COOLDOWN_TITLE, "⏳ 3 Attempts Used Today")
    fun dailyCooldownDesc(lang: AppLanguage) = t(lang, VietnameseTexts.DAILY_COOLDOWN_DESC, "New 3 daily attempts will be available at 00:00 (midnight)!")
    fun dailyStartStageBtn(lang: AppLanguage, stage: Int, size: Int): String {
        val vi = String.format(VietnameseTexts.DAILY_START_STAGE_BTN, stage, size, size)
        val en = "Start Stage $stage ($size×$size)"
        return t(lang, vi, en)
    }
    fun dailyContinueStageBtn(lang: AppLanguage, stage: Int, size: Int): String {
        val vi = String.format(VietnameseTexts.DAILY_CONTINUE_STAGE_BTN, stage, size, size)
        val en = "Continue Stage $stage ($size×$size)"
        return t(lang, vi, en)
    }
    fun dailyAllDoneBtn(lang: AppLanguage) = t(lang, VietnameseTexts.DAILY_ALL_DONE_BTN, "All 3 Stages Completed Today")
    fun dailyHeroTitle(lang: AppLanguage) = t(lang, "3 LƯỢT THỬ THÁCH MỖI NGÀY", "3 DAILY ATTEMPTS")
    fun dailyHeroSub(lang: AppLanguage) = t(lang, "Lượt 1 (14×14) → Lượt 2 (15×15) → Lượt 3 (16×16) · Reset 0h00", "Stage 1 (14×14) → Stage 2 (15×15) → Stage 3 (16×16) · Resets 00:00")
    fun dailyRewardBadge(lang: AppLanguage, stage: Int): String {
        return when (stage) {
            1 -> "+50 🪙"
            2 -> "+75 🪙"
            3 -> "+150 🪙 + 5 💎"
            else -> "+50 🪙"
        }
    }
    fun dailyReplayBtn(lang: AppLanguage, moves: Int, timeSec: Int): String {
        val vi = "🎬 Xem Lại Lộ Trình Lượt Này ($moves bước · ${timeSec}s)"
        val en = "🎬 Watch Stage Replay ($moves steps · ${timeSec}s)"
        return t(lang, vi, en)
    }
    fun dailyTopSpeed(lang: AppLanguage) = t(lang, "⚡ Thành Tích Hôm Nay", "⚡ Today's Record")
    fun noDailyRecords(lang: AppLanguage) = t(lang, "🏃 Chưa có lượt thi đấu hôm nay", "🏃 No daily runs completed yet")
    fun noDailyRecordsSub(lang: AppLanguage) = t(lang, "Nhấn nút 'Bắt Đầu Lượt 1 (14×14)' để mở màn thử thách!", "Tap 'Start Stage 1 (14×14)' to begin today's challenge!")

    // One-Line Continue / Replay / Level Map Options
    fun onelineContinueBtn(lang: AppLanguage, level: Int) = t(lang, String.format(VietnameseTexts.ONELINE_CONTINUE_BTN, level), "Continue (Level $level)")
    fun onelineReplayBtn(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_REPLAY_BTN, "Restart from Level 1")
    fun onelineSelectMapBtn(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_SELECT_MAP_BTN, "Level Map / Select")
    fun onelineProgressStatus(lang: AppLanguage, current: Int, total: Int) = t(lang, String.format(VietnameseTexts.ONELINE_PROGRESS_STATUS, current, total), "Progress: $current / $total levels cleared")
    fun onelineLevelClearedBadge(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_LEVEL_CLEARED_BADGE, "CLEARED")
    fun onelineCurrentTargetBadge(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_CURRENT_TARGET_BADGE, "PLAYING")

    // Leaderboard Screen
    fun leaderboardTitle(lang: AppLanguage) = t(lang, VietnameseTexts.LEADERBOARD_TITLE, "🏆 Leaderboards")
    fun tabByLevel(lang: AppLanguage) = t(lang, "Theo Màn Chơi", "By Level")
    fun tabDailyChallenge(lang: AppLanguage) = t(lang, "Thử Thách Ngày", "Daily Challenge")
    fun levelNum(lang: AppLanguage, lvl: Int): String {
        val vi = String.format(VietnameseTexts.HUD_CURRENT_LEVEL, lvl)
        val en = "Level $lvl"
        return t(lang, vi, en)
    }
    fun noLeaderboardData(lang: AppLanguage, isLevel: Boolean, lvl: Int): String {
        val vi = if (isLevel) "Chưa có thành tích cho Màn $lvl" else "Chưa có thành tích hôm nay"
        val en = if (isLevel) "No records for Level $lvl" else "No records today"
        return t(lang, vi, en)
    }
    fun noLeaderboardSub(lang: AppLanguage) = t(lang, "Hãy hoàn thành màn để ghi danh kỷ lục của bạn vào bảng xếp hạng!", "Complete levels to climb the leaderboards!")

    // Shop & VIP Screen
    fun shopTitle(lang: AppLanguage) = t(lang, VietnameseTexts.SHOP_TITLE, "Shop")
    fun tabSkins(lang: AppLanguage) = t(lang, VietnameseTexts.SHOP_TAB_SKINS, "Skins")
    fun tabThemes(lang: AppLanguage) = t(lang, VietnameseTexts.SHOP_TAB_THEMES, "Themes")
    fun tabVip(lang: AppLanguage) = t(lang, VietnameseTexts.SHOP_TAB_VIP, "VIP & Coins")
    fun buyItem(lang: AppLanguage, price: Int): String {
        val vi = String.format(VietnameseTexts.SHOP_BUY_BTN, price)
        val en = "Buy ($price Coins)"
        return t(lang, vi, en)
    }
    fun equipItem(lang: AppLanguage) = t(lang, VietnameseTexts.SHOP_EQUIP_BTN, "Equip")
    fun equippedBadge(lang: AppLanguage) = t(lang, VietnameseTexts.SHOP_EQUIPPED_BADGE, "EQUIPPED")
    fun ownedBadge(lang: AppLanguage) = t(lang, VietnameseTexts.SHOP_OWNED_BADGE, "OWNED")
    fun vipOnlyBadge(lang: AppLanguage) = t(lang, "VIP", "VIP ONLY")
    fun vipCardTitle(lang: AppLanguage) = t(lang, VietnameseTexts.VIP_TITLE, "👑 Premium VIP Pass")
    fun vipCardDesc(lang: AppLanguage) = t(lang, VietnameseTexts.VIP_DESC, "• Permanent Ad-Free Experience\n• Instantly unlock VIP Royal Crown Skin\n• +1,000 Bonus Coins")
    fun vipBuyBtn(lang: AppLanguage) = t(lang, VietnameseTexts.VIP_BTN, "Unlock VIP ($2.99)")
    fun vipActive(lang: AppLanguage) = t(lang, VietnameseTexts.VIP_ACTIVE_BADGE, "★ VIP ACTIVE ★")
    fun coinPacks(lang: AppLanguage) = t(lang, "Gói Nạp Xu Thưởng", "Coin Packs")
    fun watchAdCoinBtn(lang: AppLanguage, amount: Int): String {
        val vi = "📺 Xem Video Nhận +$amount Xu"
        val en = "📺 Watch Video for +$amount Coins"
        return t(lang, vi, en)
    }

    // Achievements Screen
    fun achievementsTitle(lang: AppLanguage) = t(lang, VietnameseTexts.ACHIEVEMENTS_TITLE, "🎖️ Badges & Achievements")
    fun achievementsProgress(lang: AppLanguage, current: Int, total: Int): String {
        val vi = String.format(VietnameseTexts.ACHIEVEMENTS_PROGRESS, current, total)
        val en = "Unlocked $current of $total Badges"
        return t(lang, vi, en)
    }

    // Replay Screen
    fun replayTitle(lang: AppLanguage) = t(lang, VietnameseTexts.REPLAY_TITLE, "🎬 Level Route Replay")
    fun replaySteps(lang: AppLanguage, current: Int, total: Int): String {
        val vi = String.format(VietnameseTexts.REPLAY_STEP_COUNTER, current, total)
        val en = "Step $current / $total"
        return t(lang, vi, en)
    }

    // Cloud Sync Dialog
    fun cloudSyncTitle(lang: AppLanguage) = t(lang, "☁️ Đồng Bộ Dữ Liệu Đám Mây", "☁️ Cloud Backup & Sync")
    fun backupCloud(lang: AppLanguage) = t(lang, "Sao Lưu Lên Đám Mây", "Backup to Cloud")
    fun restoreCloud(lang: AppLanguage) = t(lang, "Khôi Phục Từ Đám Mây", "Restore from Cloud")

    // One-Line Puzzle Screen
    fun oneLineTitle(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_TITLE, "One-Line Puzzle")
    fun oneLineSubtitle(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_SUBTITLE, "Draw entire line without lifting finger")
    fun tabSquareGrids(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_TAB_SQUARE, "Square (3×3 → 15×15)")
    fun tabSpecialShapes(lang: AppLanguage) = t(lang, VietnameseTexts.ONELINE_TAB_SPECIAL, "Special Shapes")
    fun oneLineCellsCount(lang: AppLanguage, visited: Int, total: Int): String {
        val vi = String.format(VietnameseTexts.ONELINE_CELLS_COUNT, visited, total)
        val en = "Visited: $visited / $total cells"
        return t(lang, vi, en)
    }

    // Cloud Sync
    fun cloudSyncSub(lang: AppLanguage) = t(lang, "Đồng bộ tiến trình chơi và vật phẩm lên đám mây", "Sync game progress and items to cloud")
    fun lastSyncLabel(lang: AppLanguage, timeStr: String = ""): String {
        val vi = if (timeStr.isEmpty()) "Đồng bộ lần cuối" else "Đồng bộ lần cuối: $timeStr"
        val en = if (timeStr.isEmpty()) "Last synced" else "Last synced: $timeStr"
        return t(lang, vi, en)
    }
    fun neverSynced(lang: AppLanguage) = t(lang, "Chưa từng đồng bộ", "Never synced")
    fun syncSuccessText(lang: AppLanguage) = t(lang, "Đã đồng bộ thành công!", "Cloud sync successful!")
    fun syncingText(lang: AppLanguage) = t(lang, "Đang đồng bộ...", "Syncing...")
    fun syncNowBtn(lang: AppLanguage) = t(lang, "Đồng Bộ Ngay", "Sync Now")

    // Ads
    fun adTitle(lang: AppLanguage) = t(lang, "📺 Xem Quảng Cáo Thưởng", "📺 Reward Video")
    fun adSecondsLeft(lang: AppLanguage, sec: Int): String {
        val vi = "Còn lại: ${sec}s"
        val en = "${sec}s remaining"
        return t(lang, vi, en)
    }
    fun adRewardFor(lang: AppLanguage, rewardType: String): String {
        val vi = "Phần thưởng: $rewardType"
        val en = "Reward: $rewardType"
        return t(lang, vi, en)
    }
    fun adFinished(lang: AppLanguage) = t(lang, "Quảng cáo hoàn tất!", "Ad complete!")
    fun adSimDesc(lang: AppLanguage) = t(lang, "Mô phỏng xem video quảng cáo nhận thưởng", "Simulated video ad player to earn rewards")
    fun claimAdRewardBtn(lang: AppLanguage) = t(lang, "Nhận Phần Thưởng", "Claim Reward")

    // Achievements
    fun achievementsCompleted(lang: AppLanguage, current: Int, total: Int): String {
        val vi = "Đã mở khóa $current / $total danh hiệu"
        val en = "Unlocked $current of $total Badges"
        return t(lang, vi, en)
    }
    fun noAchievements(lang: AppLanguage) = t(lang, "Chưa có danh hiệu nào", "No Badges Yet")
    fun noAchievementsSub(lang: AppLanguage) = t(lang, "Hãy vượt qua các thử thách để nhận huy hiệu độc quyền!", "Complete in-game challenges to earn exclusive badges!")
    fun claimed(lang: AppLanguage) = t(lang, "Đã Nhận", "Claimed")

    // OneLine Puzzle
    fun categorySquare(lang: AppLanguage) = t(lang, "Khung Vuông", "Square Grids")
    fun categorySpecial(lang: AppLanguage) = t(lang, "Hình Đặc Biệt", "Special Shapes")
    fun completePrevLevel(lang: AppLanguage) = t(lang, "Hãy hoàn thành màn trước đó để mở khóa!", "Complete the previous level to unlock!")
    fun maxDiff(lang: AppLanguage) = t(lang, "Cực Khó", "Max Diff")
    fun squareRouteSub(lang: AppLanguage) = t(lang, "Mê cung đường nối ô vuông từ 3x3 đến 15x15", "Grid path from 3x3 to 15x15")
    fun specialShapeSub(lang: AppLanguage) = t(lang, "Trái tim, Ngôi sao, Kim tự tháp, Chữ thập", "Hearts, Stars, Pyramids, Crosses")
    fun unlockSpecialReq(lang: AppLanguage) = t(lang, "Cần hoàn thành ít nhất 10 màn Khung Vuông!", "Requires 10 Square levels completed!")
    fun completeLevelToUnlock(lang: AppLanguage, lvl: Int, total: Int = 0): String {
        val vi = "Cần vượt Màn $lvl để mở khóa màn này!"
        val en = "Complete Level $lvl to unlock!"
        return t(lang, vi, en)
    }
    fun hamiltonianDfs(lang: AppLanguage) = t(lang, "Thuật toán Hamilton DFS", "Hamiltonian DFS")
    fun completeCurrentLevelFirst(lang: AppLanguage) = t(lang, "Hãy hoàn thành màn hiện tại trước!", "Complete current level first!")
    fun onelineLevelComplete(lang: AppLanguage, lvl: Int = 0): String {
        val vi = if (lvl > 0) "🎉 HOÀN THÀNH MÀN VẼ 1 NÉT $lvl!" else "🎉 HOÀN THÀNH MÀN VẼ 1 NÉT!"
        val en = if (lvl > 0) "🎉 ONE-LINE LEVEL $lvl CLEARED!" else "🎉 ONE-LINE LEVEL CLEARED!"
        return t(lang, vi, en)
    }
    fun onelineWinMsg(lang: AppLanguage, label: String = ""): String {
        val vi = if (label.isNotEmpty()) "Tuyệt vời! Bạn đã hoàn thành Màn $label!" else "Tuyệt vời! Bạn đã nối toàn bộ các ô mà không nhấc tay!"
        val en = if (label.isNotEmpty()) "Awesome! You completed Level $label!" else "Awesome! You connected all cells in a single stroke!"
        return t(lang, vi, en)
    }
    fun levelReward(lang: AppLanguage, coins: Int = 30): String {
        val vi = "Thưởng: +$coins Xu"
        val en = "Reward: +$coins Coins"
        return t(lang, vi, en)
    }
    fun congratsAllCleared(lang: AppLanguage) = t(lang, "Chúc mừng! Bạn đã chinh phục toàn bộ các màn 1 Nét!", "Congrats! You cleared all One-Line levels!")

    // Replay
    fun noReplayData(lang: AppLanguage) = t(lang, "Không có dữ liệu xem lại", "No Replay Data")
    fun noReplayDataSub(lang: AppLanguage) = t(lang, "Chơi và hoàn thành một màn để lưu lộ trình xem lại!", "Complete a level to record your step-by-step route!")
    fun replayStep(lang: AppLanguage, current: Int, total: Int): String {
        val vi = "Bước $current / $total"
        val en = "Step $current of $total"
        return t(lang, vi, en)
    }
    fun replayFromStart(lang: AppLanguage) = t(lang, "Xem Lại Từ Đầu", "Replay From Start")
    fun stepBack(lang: AppLanguage) = t(lang, "Lùi 1 Bước", "Step Back")
    fun stepForward(lang: AppLanguage) = t(lang, "Tới 1 Bước", "Step Forward")

    // Level map & Quick jump
    fun currentTargetLevelTag(lang: AppLanguage, lvl: Int) = t(lang, "🎯 Màn $lvl", "🎯 Level $lvl")
    fun jumpPlayingNow(lang: AppLanguage) = t(lang, "🎯 Đang Chơi", "🎯 Current")
    fun unlocksAfterLevel(lang: AppLanguage, lvl: Int) = t(lang, "Mở sau Màn $lvl", "Unlocks after Lv $lvl")
    fun tierLabel(lang: AppLanguage, tier: String) = t(lang, "Tầng $tier", "Tier $tier")

    // Achievements Localization
    fun achievementTitle(id: String, lang: AppLanguage): String {
        val vi = when (id) {
            "first_step" -> "Bước Đầu Tiên"
            "fifty_levels" -> "Nhà Thám Hiểm"
            "perfect_speed" -> "Tốc Độ Ánh Sáng"
            "no_wall_hit" -> "Đường Đi Chuẩn Xác"
            "fog_master" -> "Thần Đèn Sương Mù"
            "daily_champion" -> "Quán Quân Ngày"
            else -> id
        }
        val en = when (id) {
            "first_step" -> "First Steps"
            "fifty_levels" -> "Master Explorer"
            "perfect_speed" -> "Light Speed"
            "no_wall_hit" -> "Flawless Path"
            "fog_master" -> "Fog Navigator"
            "daily_champion" -> "Daily Champion"
            else -> id
        }
        return t(lang, vi, en)
    }

    fun achievementDesc(id: String, lang: AppLanguage): String {
        val vi = when (id) {
            "first_step" -> "Vượt qua Màn 1 của Mê Cung"
            "fifty_levels" -> "Hoàn thành 50 màn chơi thường"
            "perfect_speed" -> "Đạt 3 sao trong bất kỳ màn chơi nào 5 lần"
            "no_wall_hit" -> "Hoàn thành màn mà không đâm tường lần nào"
            "fog_master" -> "Chinh phục màn có sương mù Fog-of-War"
            "daily_champion" -> "Hoàn thành một lượt Thử Thách Hàng Ngày"
            else -> ""
        }
        val en = when (id) {
            "first_step" -> "Clear Level 1 in the Maze"
            "fifty_levels" -> "Complete 50 standard levels"
            "perfect_speed" -> "Earn 3 stars in any level 5 times"
            "no_wall_hit" -> "Complete a level without hitting any walls"
            "fog_master" -> "Conquer a level with Fog-of-War active"
            "daily_champion" -> "Complete a Daily Challenge run"
            else -> ""
        }
        return t(lang, vi, en)
    }

    // Shop & VIP localized phrases
    fun vipRemainingPrefix(lang: AppLanguage) = t(lang, "⏳ Còn lại: ", "⏳ Remaining: ")
    fun vipStatusActive(lang: AppLanguage) = t(lang, "ĐANG BẬT", "ACTIVE")
    fun vipStatusInactive(lang: AppLanguage) = t(lang, "CHƯA CÓ", "INACTIVE")
    fun clockSynced(lang: AppLanguage) = t(lang, "🌐 Đồng hồ Internet đã hiệu chuẩn", "🌐 Network time synchronized")
    fun clockDevice(lang: AppLanguage) = t(lang, "⏱️ Sử dụng giờ thiết bị", "⏱️ Using device clock")
    fun vip1OptionsTitle(lang: AppLanguage) = t(lang, "⚙️ Tùy chọn quyền lợi VIP 1:", "⚙️ VIP 1 Benefits:")
    fun vip1DisableBanner(lang: AppLanguage) = t(lang, "Tắt quảng cáo Banner góc dưới", "Hide bottom banner ads")
    fun vip1DisableInter(lang: AppLanguage) = t(lang, "Tắt quảng cáo sau khi thắng ván", "Hide ads after clearing levels")
    fun adViewsCount(lang: AppLanguage, current: Int, target: Int) = t(lang, "Tích Lũy Xem Video: $current / $target lượt", "Rewarded Videos: $current / $target watched")
    fun adViewsDesc(lang: AppLanguage) = t(lang,
        "Mỗi khi xem video (+50 vàng, +30s thời gian, gợi ý, bỏ qua ván) sẽ tích lũy +1 lượt. Đạt đủ 30 lượt sẽ kích hoạt ngay 3 ngày VIP 1 miễn phí!",
        "Every rewarded video watched grants +1 progress. Reach 30 to unlock 3 days of VIP 1 for free!"
    )
    fun claimVip1Btn(lang: AppLanguage) = t(lang, "🎁 Nhận VIP 1 (3 Ngày)", "🎁 Claim VIP 1 (3 Days)")
    fun flashSaleTitle(lang: AppLanguage, isFlashSale: Boolean) = if (isFlashSale) {
        t(lang, "⚡ FLASH SALE ĐANG BẬT!", "⚡ FLASH SALE ACTIVE!")
    } else {
        t(lang, "⚡ GIỜ VÀNG FLASH SALE (9H - 12H)", "⚡ FLASH SALE HOURS (9:00 - 12:00)")
    }
    fun flashSaleRemainingPrefix(lang: AppLanguage) = t(lang, "Còn ", "Left ")
    fun flashSaleDescActive(lang: AppLanguage) = t(lang,
        "Đang giảm giá cực sốc: VIP 2 chỉ còn $0.699, VIP 3 chỉ còn $2.599 theo giờ Internet!",
        "Massive discount now active: VIP 2 only $0.699, VIP 3 only $2.599 by network time!"
    )
    fun flashSaleDescUpcoming(lang: AppLanguage, nextTime: String) = t(lang,
        "Chủ Nhật, T2, T4, T6 (9h - 12h): VIP 2 giảm còn $0.699, VIP 3 giảm còn $2.599! (Đợt kế tiếp: $nextTime)",
        "Sun, Mon, Wed, Fri (9am - 12pm): VIP 2 at $0.699, VIP 3 at $2.599! (Next round: $nextTime)"
    )

    // Skip Confirmation & VIP Packages Localization
    fun skipConfirmTitle(lang: AppLanguage) = t(lang, "⚡ Bỏ Qua Màn Chơi?", "⚡ Skip Current Level?")
    fun skipConfirmBody(lang: AppLanguage, tokens: Int) = t(lang,
        "Bạn có muốn sử dụng 1 Thẻ Bỏ Qua để hoàn thành ngay màn này không? (Số dư: $tokens thẻ)",
        "Do you want to use 1 Skip Token to instantly clear this level? (Remaining: $tokens tokens)"
    )
    fun skipConfirmBtn(lang: AppLanguage) = t(lang, "Xác Nhận Bỏ Qua", "Confirm Skip")

    fun vip1PkgTitle(lang: AppLanguage) = t(lang, "Gói VIP 1 (3 Ngày)", "VIP 1 Package (3 Days)")
    fun vip1PkgBadge(lang: AppLanguage) = t(lang, "2,000 Xu hoặc 30 Video", "2,000 Coins or 30 Videos")
    fun vip1PkgDesc(lang: AppLanguage) = t(lang,
        "• 3 ngày không quảng cáo banner góc dưới màn hình hoặc sau ván chơi\n• Người chơi được tự do bật/tắt tùy chọn theo ý thích\n• Hết 3 ngày quảng cáo sẽ xuất hiện trở lại bình thường",
        "• 3 days ad-free experience for bottom banner and level completion\n• Toggle options on/off freely\n• Ads resume normally after 3 days"
    )
    fun vip1CoinsBtn(lang: AppLanguage) = t(lang, "2,000 🪙 Kích Hoạt", "2,000 🪙 Activate")
    fun vip1AdExchangeBtn(lang: AppLanguage, count: Int) = t(lang, "Đổi 30 Lượt ($count/30)", "Exchange 30 Ads ($count/30)")

    fun vip2PkgTitle(lang: AppLanguage) = t(lang, "Gói VIP 2 (15 Ngày)", "VIP 2 Package (15 Days)")
    fun vip2PkgDesc(lang: AppLanguage) = t(lang,
        "• 15 ngày không có quảng cáo (trừ quảng cáo nhận thưởng bạn tự chọn xem)\n• Tặng ngay: +5 lượt gợi ý miễn phí\n• Tặng ngay: +2 thẻ bỏ qua ván miễn phí",
        "• 15 days ad-free (excluding optional reward ads)\n• Instant bonus: +5 free hints\n• Instant bonus: +2 free skip tokens"
    )
    fun vip2BuyBtn(lang: AppLanguage, price: String) = t(lang, "Nạp Gói VIP 2 ($price)", "Get VIP 2 Package ($price)")

    fun vip3PkgTitle(lang: AppLanguage) = t(lang, "👑 Gói VIP 3 (Vĩnh Viễn)", "👑 VIP 3 Package (Lifetime)")
    fun vip3PkgDesc(lang: AppLanguage) = t(lang,
        "• VĨNH VIỄN tắt mọi loại quảng cáo tự hiện trọn đời (trừ quảng cáo thưởng)\n• Tặng ngay: +15 lượt gợi ý miễn phí\n• Tặng ngay: +5 thẻ bỏ qua ván miễn phí\n• Mở khóa đặc quyền trọn bộ Skins VIP Hoàng Gia",
        "• PERMANENT lifetime ad-free experience (excluding reward ads)\n• Instant bonus: +15 free hints\n• Instant bonus: +5 free skip tokens\n• Unlock exclusive Royal VIP skins collection"
    )
    fun vip3BuyBtn(lang: AppLanguage, price: String) = t(lang, "👑 Nạp Gói VIP 3 ($price)", "👑 Get VIP 3 Package ($price)")
    fun flashSaleOff(lang: AppLanguage) = t(lang, "GIẢM 45%", "45% OFF")
    fun flashSaleTag(lang: AppLanguage) = t(lang, "FLASH SALE", "FLASH SALE")
}
