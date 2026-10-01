package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.engine.LevelDef
import com.example.core.engine.MazeConfig
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.ui.components.CloudSyncDialog
import com.example.ui.components.GlowingProgressBar
import com.example.ui.components.LanguageToggleSwitch
import com.example.ui.components.MoveSensitivityDialog
import com.example.ui.components.TactileImpossibleTierButton
import com.example.ui.components.WolfEquipmentDialog
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeBgDark
import com.example.ui.theme.MazeCyan
import com.example.ui.theme.MazeDanger
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeStar
import com.example.ui.theme.MazeSuccess
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextH1
import com.example.ui.theme.MazeTextMuted
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

/**
 * Premium Studio "Puzzle Library" Home Screen:
 * - 1. Top Bar: Brand, Stats Pill (Stars + Coins), and Settings.
 * - 2. Continue Hero: Highlight current level & one-click play.
 * - 3. Puzzles Grid: Clean, compact puzzle level selector (1 to 100).
 * - 4. Special Modes: Wolf Chase, Daily Challenge, One Line, Impossible.
 * - 5. Bottom Navigation: Shop, Achievements, Leaderboard, Cloud.
 */
@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val progress = uiState.progress
    val records = uiState.levelRecords
    val saveSlot = uiState.saveSlot
    val lang = uiState.language

    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSensitivityDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var resetCheckState by remember { mutableStateOf(false) }
    var selectedLevelDefForDialog by remember { mutableStateOf<LevelDef?>(null) }
    var displayedLevelCount by remember { mutableIntStateOf(24) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MazeBgDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ─────────────────────────────────────────────────────────────
        // 1. TOP BAR: Brand + Stats Pill + Settings
        // ─────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_icon_art),
                    contentDescription = "MazeX Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MazeEdgeHighlight, RoundedCornerShape(10.dp))
                )
                Column {
                    Text(
                        text = "MAZEX",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MazeTextH1,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (lang == AppLanguage.VI) "Hành trình giải đố" else "Your puzzle journey",
                        fontSize = 11.sp,
                        color = MazeTextMuted
                    )
                }
            }

            // Right Group: Stats Pill & Settings Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Compact Stats Pill (Stars + Coins)
                Surface(
                    onClick = { viewModel.navigateTo(AppScreen.SHOP) },
                    shape = RoundedCornerShape(999.dp),
                    color = MazeSurface1,
                    border = BorderStroke(1.dp, MazeEdgeHighlight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MazeStar, modifier = Modifier.size(13.dp))
                            Text(text = "${progress.totalStars}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MazeTextH1)
                        }

                        Box(modifier = Modifier.width(1.dp).height(12.dp).background(MazeEdgeHighlight))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MazeAmber, modifier = Modifier.size(13.dp))
                            Text(text = "${progress.coins}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MazeTextH1)
                        }
                    }
                }

                // Settings Button (>= 48dp target)
                Surface(
                    onClick = { showSettingsDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MazeSurface1,
                    border = BorderStroke(1.dp, MazeEdgeHighlight),
                    modifier = Modifier.size(42.dp).testTag("home_settings_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MazeTextH1,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─────────────────────────────────────────────────────────────
        // 2. HERO CONTINUE CARD (One-click Resume / Play Next Level)
        // ─────────────────────────────────────────────────────────────
        val currentTargetLevel = when {
            saveSlot != null && saveSlot.defId.toIntOrNull() != null -> saveSlot.defId.toInt()
            else -> minOf(MazeConfig.MAX_LEVEL, progress.highestCleared + 1)
        }
        val currentLevelDef = MazeConfig.generateLevelDef(currentTargetLevel)

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
            border = BorderStroke(1.dp, MazeEdgeHighlight),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("continue_hero_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (saveSlot != null) {
                            if (lang == AppLanguage.VI) "TIẾP TỤC VÁN CHƠI" else "CONTINUE GAME"
                        } else {
                            if (lang == AppLanguage.VI) "MÀN TIẾP THEO" else "NEXT PUZZLE"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MazeAmber,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "${progress.highestCleared} / ${MazeConfig.MAX_LEVEL} ${if (lang == AppLanguage.VI) "Đã hoàn thành" else "Solved"}",
                        fontSize = 11.sp,
                        color = MazeTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LEVEL $currentTargetLevel",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MazeTextH1
                        )
                        Text(
                            text = "${currentLevelDef.w} × ${currentLevelDef.h} • ${Strings.stepsUnit(lang, currentLevelDef.target)}",
                            fontSize = 12.sp,
                            color = MazeTextBody
                        )
                    }

                    Button(
                        onClick = {
                            if (saveSlot != null) {
                                viewModel.resumeFromSave()
                            } else {
                                viewModel.startLevel(currentLevelDef)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF090B16)),
                        modifier = Modifier.height(44.dp).testTag("home_hero_play_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (lang == AppLanguage.VI) "CHƠI NGAY" else "PLAY",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                GlowingProgressBar(
                    progress = (progress.highestCleared.toFloat() / MazeConfig.MAX_LEVEL.toFloat()).coerceIn(0f, 1f),
                    activeColor = MazeAmber,
                    glowColor = MazeAmber,
                    trackColor = MazeSurface2
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────────────
        // 3. PUZZLES LIBRARY (Compact Level Grid / Selector)
        // ─────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (lang == AppLanguage.VI) "DANH SÁCH MÀN CHƠI" else "PUZZLES",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MazeTextH1,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "1 – $displayedLevelCount",
                fontSize = 12.sp,
                color = MazeTextMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of Compact Level Buttons
        val levelRows = (1..displayedLevelCount).chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in levelRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (levelNum in row) {
                        val def = MazeConfig.generateLevelDef(levelNum)
                        val isCleared = levelNum <= progress.highestCleared
                        val isCurrent = levelNum == progress.highestCleared + 1
                        val isLocked = levelNum > progress.highestCleared + 1
                        val stars = records[def.id]?.stars ?: (if (isCleared) 3 else 0)

                        Surface(
                            onClick = {
                                if (isCleared) {
                                    selectedLevelDefForDialog = def
                                } else if (isCurrent) {
                                    viewModel.startLevel(def)
                                }
                            },
                            enabled = !isLocked,
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isCurrent -> MazeAmber.copy(alpha = 0.15f)
                                isCleared -> MazeSurface1
                                else -> MazeSurface2
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isCurrent -> MazeAmber
                                    isCleared -> MazeEdgeHighlight
                                    else -> Color.Transparent
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("level_btn_$levelNum")
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isLocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = MazeTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$levelNum",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MazeTextMuted
                                    )
                                } else {
                                    Text(
                                        text = "$levelNum",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isCurrent) MazeAmber else MazeTextH1
                                    )
                                    if (isCleared) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            for (s in 1..3) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (s <= stars) MazeStar else MazeTextMuted.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(9.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "${def.w}×${def.h}",
                                            fontSize = 9.sp,
                                            color = MazeAmber,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (displayedLevelCount < MazeConfig.MAX_LEVEL) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { displayedLevelCount = (displayedLevelCount + 24).coerceAtMost(MazeConfig.MAX_LEVEL) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MazeEdgeHighlight),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            ) {
                Text(
                    text = if (lang == AppLanguage.VI) "Xem thêm màn chơi..." else "Load more levels...",
                    fontSize = 12.sp,
                    color = MazeTextBody
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────
        // 4. SPECIAL MODES SECTION
        // ─────────────────────────────────────────────────────────────
        Text(
            text = if (lang == AppLanguage.VI) "CHẾ ĐỘ ĐẶC BIỆT" else "SPECIAL MODES",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MazeTextH1,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        val isWolfUnlocked = progress.highestCleared >= MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE
        val isOneLineUnlocked = progress.highestCleared >= 20
        val isImpossibleUnlocked = progress.highestCleared >= MazeConfig.PASSES_TO_UNLOCK_IMPOSSIBLE

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 4A. Wolf Chase Mode Card
            SpecialModeItem(
                title = if (lang == AppLanguage.VI) "SÓI TRUY ĐUỔI" else "WOLF CHASE",
                subtitle = "5×5 → 30×30 • Real-time chase",
                tag = if (isWolfUnlocked) "${progress.wolfHighestCleared}/100" else "Level 5 to unlock",
                isUnlocked = isWolfUnlocked,
                accentColor = Color(0xFFF97316),
                onClick = {
                    if (isWolfUnlocked) {
                        viewModel.startWolfChaseLevel((progress.wolfHighestCleared + 1).coerceAtMost(MazeConfig.WOLF_MAX_LEVEL))
                    }
                }
            )

            // 4B. Daily Challenge Mode Card
            val dailyDone = progress.dailyAllCompleted || (progress.dailyStage1Time > 0 && progress.dailyStage2Time > 0 && progress.dailyStage3Time > 0)
            SpecialModeItem(
                title = if (lang == AppLanguage.VI) "THỬ THÁCH NGÀY" else "DAILY CHALLENGE",
                subtitle = if (lang == AppLanguage.VI) "3 câu đố mới mỗi ngày" else "3 fresh puzzles every day",
                tag = if (dailyDone) "✓ Solved" else "${progress.dailyAttemptsUsed}/3 attempts",
                isUnlocked = true,
                accentColor = Color(0xFF10B981),
                onClick = { viewModel.navigateTo(AppScreen.DAILY_CHALLENGE) }
            )

            // 4C. One Line Mode Card
            SpecialModeItem(
                title = if (lang == AppLanguage.VI) "VẼ MỘT NÉT" else "ONE LINE",
                subtitle = if (lang == AppLanguage.VI) "Nối toàn bộ ma trận không nhấc tay" else "Draw one continuous path",
                tag = if (isOneLineUnlocked) "${progress.oneLineHighestUnlocked} / 23" else "Level 20 to unlock",
                isUnlocked = isOneLineUnlocked,
                accentColor = MazeCyan,
                onClick = {
                    if (isOneLineUnlocked) {
                        viewModel.navigateTo(AppScreen.ONELINE_GAME)
                    }
                }
            )

            // 4D. Impossible Mode Card
            SpecialModeItem(
                title = if (lang == AppLanguage.VI) "MÊ CUNG KHỔNG LỒ" else "IMPOSSIBLE MAZE",
                subtitle = "300×300 → 1000×1000 • Mega coordinates",
                tag = if (isImpossibleUnlocked) "Tier ${progress.impossibleCleared + 1}" else "Level 100 to unlock",
                isUnlocked = isImpossibleUnlocked,
                accentColor = MazeDanger,
                onClick = {
                    if (isImpossibleUnlocked) {
                        val def = MazeConfig.impossibleDefForTier((progress.impossibleCleared + 1).coerceAtMost(MazeConfig.IMPOSSIBLE_TIER_SIZES.size))
                        viewModel.startLevel(def)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────
        // 5. BOTTOM NAVIGATION & SHORTCUTS (Shop, Achievements, Leaderboard, Gear)
        // ─────────────────────────────────────────────────────────────
        Text(
            text = if (lang == AppLanguage.VI) "KHÁM PHÁ THÊM" else "MORE",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MazeTextH1,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MoreShortcutButton(
                label = Strings.tabShop(lang),
                icon = Icons.Default.WorkspacePremium,
                onClick = { viewModel.navigateTo(AppScreen.SHOP) },
                modifier = Modifier.weight(1f)
            )

            MoreShortcutButton(
                label = Strings.tabLeaderboard(lang),
                icon = Icons.Default.Star,
                onClick = { viewModel.navigateTo(AppScreen.LEADERBOARD) },
                modifier = Modifier.weight(1f)
            )

            MoreShortcutButton(
                label = Strings.tabAchievements(lang),
                icon = Icons.Default.MonetizationOn,
                onClick = { viewModel.navigateTo(AppScreen.ACHIEVEMENTS) },
                modifier = Modifier.weight(1f)
            )

            MoreShortcutButton(
                label = if (lang == AppLanguage.VI) "Trang Bị" else "Gear",
                icon = Icons.Default.Speed,
                onClick = { viewModel.openWolfEquipmentDialog() },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ─────────────────────────────────────────────────────────────
    // DIALOGS & OVERLAYS
    // ─────────────────────────────────────────────────────────────

    // Full In-App Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = MazeSurface1,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (lang == AppLanguage.VI) "Cài Đặt Ứng Dụng" else "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MazeTextH1
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sound & Music Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == AppLanguage.VI) "Âm thanh & Rung" else "Audio & Haptics",
                            color = MazeTextBody,
                            fontSize = 13.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = { viewModel.toggleSound() },
                                shape = CircleShape,
                                color = if (progress.soundEnabled) MazeAmber.copy(alpha = 0.2f) else MazeSurface2,
                                border = BorderStroke(1.dp, if (progress.soundEnabled) MazeAmber else MazeEdgeHighlight),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (progress.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                        contentDescription = "Sound",
                                        tint = if (progress.soundEnabled) MazeAmber else MazeTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { viewModel.toggleMusic() },
                                shape = CircleShape,
                                color = if (progress.musicEnabled) MazeAmber.copy(alpha = 0.2f) else MazeSurface2,
                                border = BorderStroke(1.dp, if (progress.musicEnabled) MazeAmber else MazeEdgeHighlight),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (progress.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                        contentDescription = "Music",
                                        tint = if (progress.musicEnabled) MazeAmber else MazeTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { viewModel.toggleHaptics() },
                                shape = CircleShape,
                                color = if (progress.hapticEnabled) MazeAmber.copy(alpha = 0.2f) else MazeSurface2,
                                border = BorderStroke(1.dp, if (progress.hapticEnabled) MazeAmber else MazeEdgeHighlight),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = "Haptics",
                                        tint = if (progress.hapticEnabled) MazeAmber else MazeTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Move Sensitivity Option
                    Surface(
                        onClick = { showSensitivityDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MazeSurface2,
                        border = BorderStroke(1.dp, MazeEdgeHighlight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Strings.sensitivityLabel(lang),
                                fontSize = 13.sp,
                                color = MazeTextH1,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${(progress.moveSensitivity * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MazeAmber
                            )
                        }
                    }

                    // Language Selection Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.languageLabel(lang),
                            fontSize = 13.sp,
                            color = MazeTextBody
                        )
                        LanguageToggleSwitch(
                            currentLanguage = lang,
                            onLanguageSelected = { viewModel.setLanguage(it) },
                            isCompact = true
                        )
                    }

                    // Cloud Sync Trigger
                    Surface(
                        onClick = {
                            showSettingsDialog = false
                            viewModel.openCloudSync()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MazeSurface2,
                        border = BorderStroke(1.dp, MazeEdgeHighlight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Strings.cloudSyncTitle(lang),
                                fontSize = 13.sp,
                                color = MazeTextH1
                            )
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MazeCyan, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Reset & Account Deletion (Google Play Compliance)
                    Surface(
                        onClick = {
                            showSettingsDialog = false
                            showResetConfirmDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MazeDanger.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MazeDanger.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == AppLanguage.VI) "Xóa tài khoản & Dữ liệu" else "Delete Account & Data",
                                fontSize = 13.sp,
                                color = MazeDanger,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "→",
                                fontSize = 14.sp,
                                color = MazeDanger,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF090B16)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (lang == AppLanguage.VI) "Đóng" else "Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Reset Progress / Delete Data Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetConfirmDialog = false
                resetCheckState = false
            },
            containerColor = MazeSurface1,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (lang == AppLanguage.VI) "Xóa Tài Khoản & Dữ Liệu?" else "Delete Account & Data?",
                    fontWeight = FontWeight.Bold,
                    color = MazeDanger
                )
            },
            text = {
                Column {
                    Text(
                        text = if (lang == AppLanguage.VI)
                            "Hành động này sẽ xóa toàn bộ tiến trình, số sao, tiền xu và trang bị của bạn về trạng thái ban đầu. Không thể khôi phục sau khi xóa."
                        else
                            "This will permanently delete all your progress, stars, coins and items. This cannot be undone.",
                        fontSize = 13.sp,
                        color = MazeTextBody
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { resetCheckState = !resetCheckState }
                    ) {
                        Checkbox(
                            checked = resetCheckState,
                            onCheckedChange = { resetCheckState = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == AppLanguage.VI) "Tôi hiểu và muốn xóa vĩnh viễn" else "I understand and confirm deletion",
                            fontSize = 12.sp,
                            color = MazeTextH1
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetCheckState) {
                            showResetConfirmDialog = false
                            resetCheckState = false
                            viewModel.deleteAccountAndData()
                        }
                    },
                    enabled = resetCheckState,
                    colors = ButtonDefaults.buttonColors(containerColor = MazeDanger),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (lang == AppLanguage.VI) "Xác nhận xóa" else "Confirm Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showResetConfirmDialog = false
                    resetCheckState = false
                }) {
                    Text(if (lang == AppLanguage.VI) "Hủy" else "Cancel", color = MazeTextBody)
                }
            }
        )
    }

    // Level Replay / Detail Modal
    selectedLevelDefForDialog?.let { def ->
        val record = records[def.id]
        AlertDialog(
            onDismissRequest = { selectedLevelDefForDialog = null },
            containerColor = MazeSurface1,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "${Strings.levelNum(lang, def.numericLevel ?: 1)} (${def.w}×${def.h})",
                    fontWeight = FontWeight.Bold,
                    color = MazeTextH1
                )
            },
            text = {
                Column {
                    Text(
                        text = if (record != null) {
                            if (lang == AppLanguage.VI)
                                "Kỷ lục: ${record.bestTimeSec}s • ${record.bestMoves} bước"
                            else
                                "Best: ${record.bestTimeSec}s • ${record.bestMoves} moves"
                        } else {
                            if (lang == AppLanguage.VI) "Bạn đã hoàn thành màn chơi này!" else "You have solved this puzzle!"
                        },
                        fontSize = 13.sp,
                        color = MazeTextBody
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedLevelDefForDialog = null
                        viewModel.startLevel(def)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF090B16)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (lang == AppLanguage.VI) "Chơi lại" else "Replay", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLevelDefForDialog = null }) {
                    Text(if (lang == AppLanguage.VI) "Đóng" else "Close", color = MazeTextBody)
                }
            }
        )
    }

    // Sensitivity Dialog
    if (showSensitivityDialog) {
        MoveSensitivityDialog(
            sensitivity = progress.moveSensitivity,
            onSensitivityChange = { viewModel.setMoveSensitivity(it) },
            onDismiss = { showSensitivityDialog = false },
            lang = lang
        )
    }

    // Cloud Sync Dialog
    if (uiState.showCloudSyncDialog) {
        CloudSyncDialog(
            lastSyncTime = progress.lastCloudSyncTime,
            totalStars = progress.totalStars,
            highestCleared = progress.highestCleared,
            language = lang,
            onSyncConfirmed = { viewModel.confirmCloudSync() },
            onDismiss = { viewModel.closeCloudSync() }
        )
    }

    // Wolf Equipment Dialog
    if (uiState.showWolfEquipmentDialog) {
        WolfEquipmentDialog(
            progress = progress,
            language = lang,
            onEquip = { viewModel.equipEquipment(it) },
            onUnlock = { viewModel.buyEquipment(it) },
            onBuyShield = { viewModel.buyShieldPack() },
            onDismiss = { viewModel.closeWolfEquipmentDialog() }
        )
    }
}

@Composable
private fun SpecialModeItem(
    title: String,
    subtitle: String,
    tag: String,
    isUnlocked: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = isUnlocked,
        shape = RoundedCornerShape(14.dp),
        color = if (isUnlocked) MazeSurface1 else MazeSurface2,
        border = BorderStroke(1.dp, if (isUnlocked) MazeEdgeHighlight else Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) MazeTextH1 else MazeTextMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MazeTextBody
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isUnlocked) accentColor.copy(alpha = 0.15f) else MazeSurface2,
                border = BorderStroke(1.dp, if (isUnlocked) accentColor.copy(alpha = 0.5f) else MazeEdgeHighlight)
            ) {
                Text(
                    text = tag,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) accentColor else MazeTextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MoreShortcutButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MazeSurface1,
        border = BorderStroke(1.dp, MazeEdgeHighlight),
        modifier = modifier.height(60.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MazeAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MazeTextH1,
                maxLines = 1
            )
        }
    }
}
