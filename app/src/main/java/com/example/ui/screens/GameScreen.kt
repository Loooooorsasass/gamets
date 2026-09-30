package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.engine.EAST
import com.example.core.engine.GoalDirectionHint
import com.example.core.engine.LevelDef
import com.example.core.engine.MazeConfig
import com.example.core.engine.NORTH
import com.example.core.engine.Point
import com.example.core.engine.SOUTH
import com.example.core.engine.WEST
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.data.shop.ShopCatalog
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.DPad
import com.example.ui.components.LanguageToggleSwitch
import com.example.ui.components.MazeCanvas
import com.example.ui.components.MoveSensitivityDialog
import com.example.ui.theme.MazeAccent
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeCyan
import com.example.ui.theme.MazeDanger
import com.example.ui.theme.MazeStar
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextH1
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lang = uiState.language
    val game = uiState.activeGame
    val maze = game.maze
    val levelDef = game.levelDef
    val context = LocalContext.current
    val activity = context as? Activity

    // Android Physical Back Handler
    BackHandler {
        viewModel.requestExit()
    }

    if (maze == null || levelDef == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = Strings.preparingLevel(lang),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A))
                ) {
                    Text(Strings.backToHome(lang))
                }
            }
        }
        return
    }

    val theme = ShopCatalog.getThemeById(uiState.progress.currentMazeThemeId)
    val skin = ShopCatalog.getSkinById(uiState.progress.currentSkinId)
    var showSkipConfirmDialog by remember { mutableStateOf(false) }
    var showCoordGuideDialog by remember { mutableStateOf(false) }
    var showSensitivityDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP BAR: Back button, Level Title, SHOP CTA & Tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StudioGlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Strings.back(lang),
                        tint = theme.wallColor,
                        accentColor = theme.accentColor,
                        onClick = { viewModel.requestExit() },
                        modifier = Modifier.testTag("game_back_button")
                    )

                    if (!levelDef.isWolfChase) {
                        // Nút Cửa Hàng gọn gàng chuẩn Studio
                        Surface(
                            onClick = { viewModel.navigateTo(AppScreen.SHOP) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2B2011),
                            border = BorderStroke(1.dp, MazeAmber),
                            modifier = Modifier.testTag("game_vip_shop_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Strings.tabShop(lang),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MazeAmber
                                )
                            }
                        }
                    }
                }

                val titleText = when {
                    levelDef.isWolfChase -> if (lang == AppLanguage.VI) "🐺 Sói Đuổi #${levelDef.numericLevel ?: 1} — ${maze.w}×${maze.h}" else "🐺 Wolf #${levelDef.numericLevel ?: 1} — ${maze.w}×${maze.h}"
                    levelDef.tier == "DAILY" -> "${Strings.tabDaily(lang)} — ${maze.w}×${maze.h}"
                    levelDef.isFinal -> "${if (levelDef.tier == "SUPER") Strings.superTierTitle(lang) else Strings.impossibleSectionTitle(lang)} — ${maze.w}×${maze.h}"
                    else -> {
                        val algLabel = if (lang == AppLanguage.VI) "TT${levelDef.algorithmIndex}" else "Alg ${levelDef.algorithmIndex}"
                        "${Strings.levelNum(lang, levelDef.numericLevel ?: 1)} — ${maze.w}×${maze.h} ($algLabel)"
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = theme.wallColor
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = Strings.reachFlagHint(lang),
                            fontSize = 10.5.sp,
                            color = theme.accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StudioGlassIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = Strings.reset(lang),
                        tint = theme.wallColor,
                        accentColor = theme.accentColor,
                        onClick = { viewModel.resetCurrentLevel() },
                        modifier = Modifier.testTag("game_reset_level_top_btn")
                    )
                    StudioGlassIconButton(
                        icon = Icons.Default.Map,
                        contentDescription = if (lang == AppLanguage.VI) "Bản đồ hướng dẫn tọa độ" else "Coordinate Guide Map",
                        tint = Color(0xFF38BDF8),
                        accentColor = theme.accentColor,
                        isHighlighted = showCoordGuideDialog,
                        onClick = { showCoordGuideDialog = true },
                        modifier = Modifier.testTag("game_coord_map_btn")
                    )
                    StudioGlassIconButton(
                        icon = Icons.Default.Speed,
                        contentDescription = Strings.sensitivityLabel(lang),
                        tint = MazeCyan,
                        accentColor = theme.accentColor,
                        isHighlighted = showSensitivityDialog,
                        onClick = { showSensitivityDialog = true },
                        modifier = Modifier.testTag("game_sensitivity_btn")
                    )
                    StudioGlassIconButton(
                        icon = if (uiState.progress.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound",
                        tint = if (uiState.progress.soundEnabled) theme.accentColor else theme.wallColor.copy(alpha = 0.55f),
                        accentColor = theme.accentColor,
                        onClick = { viewModel.toggleSound() }
                    )
                    LanguageToggleSwitch(
                        currentLanguage = lang,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        isCompact = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // HUD STATS BAR (Clean Bar with Player Coordinate + Goal Coordinate side-by-side)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.panelColor)
                    .border(
                        1.dp,
                        theme.wallColor.copy(alpha = 0.35f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HudItem(
                    icon = Icons.Default.Timeline,
                    label = Strings.steps(lang),
                    value = "${game.moves}",
                    textColor = theme.accentColor
                )

                val isUntimedLevel = !levelDef.isWolfChase && (levelDef.noTimer || levelDef.numericLevel == 1)
                val remainingSec = if (game.timeLimitSec > 0) maxOf(0, game.timeLimitSec - game.elapsedSec) else 0
                HudItem(
                    icon = Icons.Default.Timer,
                    label = Strings.time(lang),
                    value = when {
                        isUntimedLevel -> "∞"
                        game.timeLimitSec > 0 -> "${remainingSec}s"
                        else -> "${game.elapsedSec}s"
                    },
                    textColor = when {
                        isUntimedLevel -> Color(0xFF16A34A)
                        game.wolfFrenzy -> Color(0xFFEF4444)
                        game.timeLimitSec > 0 && remainingSec <= 10 -> Color(0xFFD97706)
                        else -> theme.wallColor
                    }
                )

                HudItem(
                    icon = Icons.Default.MyLocation,
                    label = Strings.coordinates(lang),
                    value = MazeConfig.encodeCoord(game.player),
                    textColor = theme.wallColor
                )

                HudItem(
                    icon = Icons.Default.Flag,
                    label = if (lang == AppLanguage.VI) "Tọa độ đích" else "Goal Pos",
                    value = MazeConfig.encodeCoord(maze.goal),
                    textColor = Color(0xFFEF4444)
                )

                // Control Mode Switcher integrated cleanly into HUD
                val isAutoMode = game.controlMode == com.example.ui.components.GridControlMode.AUTO
                Surface(
                    onClick = { viewModel.toggleControlMode() },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAutoMode) theme.accentColor.copy(alpha = 0.18f) else theme.bgColor,
                    border = BorderStroke(
                        1.dp,
                        if (isAutoMode) theme.accentColor else theme.wallColor.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.testTag("toggle_control_mode_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isAutoMode) Icons.Default.Swipe else Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = if (isAutoMode) theme.accentColor else theme.wallColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isAutoMode) Strings.controlAuto(lang) else Strings.controlStep(lang),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAutoMode) theme.accentColor else theme.wallColor
                        )
                    }
                }

                if (levelDef.isWolfChase) {
                    val isPlayerCurrentlyFrozen = System.currentTimeMillis() < game.playerFrozenUntilMs
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPlayerCurrentlyFrozen) Color(0xFF0284C7).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, if (isPlayerCurrentlyFrozen) Color(0xFF0284C7) else Color(0xFFEF4444).copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = if (isPlayerCurrentlyFrozen) "❄️ BĂNG" else if (!game.wolfActive) "🐺 ${maxOf(1, game.wolfCountdownSec)}s" else "🐺 ${game.wolfDistanceCells}ô",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPlayerCurrentlyFrozen) Color(0xFF0284C7) else Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // THANH THỜI GIAN TRỰC QUAN (DYNAMIC TIMER PROGRESS BAR)
            val hasTimeLimit = game.timeLimitSec > 0 || (levelDef.isWolfChase && !game.wolfActive)
            if (hasTimeLimit) {
                Spacer(modifier = Modifier.height(2.dp))
                val timeRatio = if (game.timeLimitSec > 0) {
                    (maxOf(0, game.timeLimitSec - game.elapsedSec).toFloat() / game.timeLimitSec.toFloat()).coerceIn(0f, 1f)
                } else if (levelDef.isWolfChase && !game.wolfActive) {
                    (maxOf(0, game.wolfCountdownSec).toFloat() / 15f).coerceIn(0f, 1f)
                } else {
                    1f
                }
                val timerColor = when {
                    timeRatio > 0.5f -> Color(0xFF22C55E)
                    timeRatio > 0.25f -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = timerColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(timeRatio)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(timerColor.copy(alpha = 0.75f), timerColor)
                                    )
                                )
                        )
                    }
                    Text(
                        text = if (game.timeLimitSec > 0) "${maxOf(0, game.timeLimitSec - game.elapsedSec)}s" else "🐺 ${maxOf(1, game.wolfCountdownSec)}s",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = timerColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Tính đường gợi ý trực tiếp từ vị trí hiện tại của người chơi tới Đích (hỗ trợ cả Impossible Mode!)
            val activeHintPath = remember(game.showHint, game.player, game.goalDistCache, maze) {
                if (!game.showHint) {
                    null
                } else {
                    val distArr = game.goalDistCache
                    if (distArr != null) {
                        val list = ArrayList<Point>(64)
                        var curr = game.player
                        var safety = 0
                        while ((curr.x != maze.goal.x || curr.y != maze.goal.y) && safety < 80) {
                            safety++
                            val curIdx = curr.y * maze.w + curr.x
                            val curD = distArr[curIdx]
                            if (curD <= 0) break
                            val mask = maze.cellAt(curr.x, curr.y)
                            var nextPt: Point? = null
                            var bestD = curD
                            val dirs = intArrayOf(EAST, WEST, NORTH, SOUTH)
                            val dxs = intArrayOf(1, -1, 0, 0)
                            val dys = intArrayOf(0, 0, 1, -1)
                            for (i in 0 until 4) {
                                if ((mask and dirs[i]) != 0) {
                                    val nx = curr.x + dxs[i]
                                    val ny = curr.y + dys[i]
                                    if (nx in 0 until maze.w && ny in 0 until maze.h) {
                                        val nd = distArr[ny * maze.w + nx]
                                        if (nd in 0 until bestD) {
                                            bestD = nd
                                            nextPt = Point(nx, ny)
                                        }
                                    }
                                }
                            }
                            if (nextPt != null) {
                                list.add(nextPt)
                                curr = nextPt
                            } else {
                                break
                            }
                        }
                        list
                    } else {
                        maze.spine
                    }
                }
            }

            // MAZE CANVAS BOARD (EXPANDED TO FULL SCREEN SIZE WITH WEIGHT 1F)
            val currentTickMs = System.currentTimeMillis()
            val isPlayerCurrentlyFrozen = currentTickMs < game.playerFrozenUntilMs
            val isIceBeamCurrentlyActive = currentTickMs < game.wolfIceBeamUntilMs
            val totalAvailableShields = uiState.progress.shieldCount + (if (levelDef.isWolfChase) game.freeAegisShieldInRun else 0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                MazeCanvas(
                    maze = maze,
                    player = game.player,
                    visitedCells = game.visitedCells,
                    pathHistory = game.pathHistory,
                    theme = theme,
                    skin = skin,
                    vision = levelDef.vision,
                    hintPath = activeHintPath,
                    collectibles = emptyList(),
                    wolfPos = game.wolfPos,
                    wolfCubPos = game.wolfCubPos,
                    isWolfFrenzy = game.wolfFrenzy,
                    isWolfStunned = currentTickMs < game.wolfStunnedUntilMs,
                    hasShieldProtection = totalAvailableShields > 0,
                    isPlayerFrozen = isPlayerCurrentlyFrozen,
                    isIceBeamActive = isIceBeamCurrentlyActive,
                    controlMode = game.controlMode,
                    moveSensitivity = uiState.progress.moveSensitivity,
                    onMove = { dx, dy -> viewModel.tryMove(dx, dy, fromCanvas = true) },
                    onUndo = { viewModel.undoMove(fromCanvas = true) },
                    onWallHit = { viewModel.onWallHit() },
                    onWin = { viewModel.handleWinDirect() },
                    modifier = Modifier.testTag("game_maze_canvas")
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // D-PAD & STUDIO ASSIST CONTROLS (COMPACT & ERGONOMIC)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Quick Actions (Hint, Undo)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StudioAssistButton(
                        icon = Icons.Default.Lightbulb,
                        label = Strings.hint(lang),
                        badgeText = "${uiState.progress.hintCount}",
                        accentColor = Color(0xFFFBBF24),
                        onClick = { viewModel.useHint(activity) }
                    )
                    StudioAssistButton(
                        icon = Icons.AutoMirrored.Filled.Undo,
                        label = Strings.undo(lang),
                        badgeText = null,
                        accentColor = theme.accentColor,
                        onClick = { viewModel.undoMove() }
                    )
                }

                // Center: Studio Pro Tactile D-Pad
                DPad(
                    onMove = { dx, dy -> viewModel.tryMove(dx, dy) },
                    sensitivity = uiState.progress.moveSensitivity,
                    accentColor = theme.accentColor,
                    wallColor = theme.wallColor,
                    modifier = Modifier.testTag("game_dpad")
                )

                // Right Quick Actions (Skip, Free Coins)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StudioAssistButton(
                        icon = Icons.Default.FastForward,
                        label = Strings.skip(lang),
                        badgeText = "${uiState.progress.skipTokens}",
                        accentColor = Color(0xFF38BDF8),
                        onClick = {
                            if (uiState.progress.skipTokens > 0) {
                                showSkipConfirmDialog = true
                            } else {
                                viewModel.skipCurrentLevel(activity)
                            }
                        }
                    )
                    StudioAssistButton(
                        icon = Icons.Default.MonetizationOn,
                        label = "+50 Xu",
                        badgeText = "AD",
                        accentColor = Color(0xFF34D399),
                        onClick = { viewModel.triggerRewardedAd(activity, "COINS") }
                    )
                }
            }

            // QUẢNG CÁO BANNER ADMOB DƯỚI CÙNG (ALWAYS VISIBLE & CLEANLY ANCHORED)
            AdMobBannerView(
                isBannerAllowed = uiState.progress.isBannerAdAllowed(),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // EXIT CONFIRMATION MODAL (Android back flow)
        if (game.showExitDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissExitDialog() },
                title = { Text(text = Strings.exitTitle(lang), fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = Strings.exitMessage(lang, game.moves, game.elapsedSec))
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Strings.languageLabel(lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            LanguageToggleSwitch(
                                currentLanguage = lang,
                                onLanguageSelected = { viewModel.setLanguage(it) },
                                isCompact = false
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            onClick = { showSensitivityDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MazeSurface2,
                            border = BorderStroke(1.dp, MazeCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = MazeCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = Strings.sensitivityLabel(lang),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MazeTextH1
                                    )
                                }
                                Text(
                                    text = "${(uiState.progress.moveSensitivity * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MazeCyan
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.saveAndExit() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("save_and_exit_btn")
                    ) {
                        Text(Strings.saveAndExit(lang))
                    }
                },
                dismissButton = {
                    Row {
                        OutlinedButton(
                            onClick = { viewModel.discardAndExit() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MazeDanger)
                        ) {
                            Text(Strings.discardAndExit(lang))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(onClick = { viewModel.dismissExitDialog() }) {
                            Text(Strings.cancel(lang))
                        }
                    }
                }
            )
        }

        // SENSITIVITY ADJUSTMENT MODAL
        if (showSensitivityDialog) {
            MoveSensitivityDialog(
                sensitivity = uiState.progress.moveSensitivity,
                onSensitivityChange = { viewModel.setMoveSensitivity(it) },
                onDismiss = { showSensitivityDialog = false },
                lang = lang
            )
        }

        // COORDINATE & DIRECTION GUIDE MAP MODAL (HỖ TRỢ CẢ CHẾ ĐỘ IMPOSSIBLE & SÓI ĐUỔI)
        if (showCoordGuideDialog) {
            CoordinateGuideMapDialog(
                player = game.player,
                goal = maze.goal,
                wolf = game.wolfPos,
                goalHint = game.goalDirectionHint,
                isImpossible = levelDef.isFinal,
                mazeW = maze.w,
                mazeH = maze.h,
                language = lang,
                onDismiss = { showCoordGuideDialog = false }
            )
        }

        // SKIP CONFIRMATION MODAL
        if (showSkipConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showSkipConfirmDialog = false },
                title = { Text(text = Strings.skipConfirmTitle(lang), fontWeight = FontWeight.Bold) },
                text = { Text(text = Strings.skipConfirmBody(lang, uiState.progress.skipTokens)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showSkipConfirmDialog = false
                            viewModel.skipCurrentLevel(activity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber)
                    ) {
                        Text(text = Strings.skipConfirmBtn(lang), fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSkipConfirmDialog = false }) {
                        Text(text = Strings.cancel(lang))
                    }
                }
            )
        }

        // WIN OVERLAY MODAL
        if (game.gameOver && game.hasWon) {
            GameWinModal(
                levelDef = levelDef,
                language = lang,
                moves = game.moves,
                elapsedSec = game.elapsedSec,
                earnedStars = game.earnedStars,
                collectedCoins = game.collectedCoinsInRun,
                collectedKeys = game.collectedKeysInRun,
                collectedShields = game.collectedShieldsInRun,
                justUnlockedImpossible = game.justUnlockedImpossible,
                justUnlockedWolfMode = game.justUnlockedWolfMode,
                justUnlockedNextTier = game.justUnlockedNextTier,
                justClearedSuper = game.justClearedSuper,
                onNextLevel = {
                    if (levelDef.tier == "DAILY") {
                        viewModel.navigateTo(AppScreen.DAILY_CHALLENGE)
                    } else if (levelDef.isWolfChase) {
                        val nextWolf = ((levelDef.numericLevel ?: 1) + 1).coerceAtMost(MazeConfig.WOLF_MAX_LEVEL)
                        viewModel.startWolfChaseLevel(nextWolf)
                    } else if (levelDef.isFinal) {
                        val nextDef = game.nextTierDef ?: levelDef
                        viewModel.startLevel(nextDef)
                    } else if (levelDef.numericLevel != null) {
                        val nextNum = (levelDef.numericLevel + 1).coerceAtMost(MazeConfig.MAX_LEVEL)
                        viewModel.startLevel(MazeConfig.generateLevelDef(nextNum))
                    } else {
                        viewModel.navigateTo(AppScreen.HOME)
                    }
                },
                onReplay = { viewModel.startReplay() }
            )
        }

        // LOSS OVERLAY MODAL
        if (game.gameOver && !game.hasWon) {
            GameLossModal(
                reason = uiState.toastMessage ?: Strings.lossTitle(lang),
                language = lang,
                isWolfChase = levelDef.isWolfChase,
                canReview = levelDef.isReplaySupported && game.pathHistory.isNotEmpty(),
                onReview = { viewModel.startReplay() },
                onRetry = { viewModel.startLevel(levelDef) },
                onAddExtraTimeAd = { viewModel.triggerRewardedAd(activity, "TIME") },
                onOpenShop = { viewModel.navigateTo(AppScreen.SHOP) },
                onGoHome = { viewModel.navigateTo(AppScreen.HOME) }
            )
        }
    }
}

@Composable
private fun StudioGlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    accentColor: Color,
    isHighlighted: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(shape)
            .background(
                if (isHighlighted) accentColor.copy(alpha = 0.25f) else MazeSurface2
            )
            .border(
                width = 1.dp,
                color = if (isHighlighted) accentColor else tint.copy(alpha = 0.4f),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun HudItem(
    icon: ImageVector,
    label: String,
    value: String,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(textColor.copy(alpha = 0.15f))
                .border(1.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
        }
        Column {
            Text(
                text = label,
                fontSize = 8.5.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = value,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GoalDirectionRadarBanner(
    player: Point,
    goal: Point,
    wolf: Point?,
    mazeW: Int,
    mazeH: Int,
    hint: GoalDirectionHint,
    isImpossible: Boolean,
    language: AppLanguage,
    onClickExpand: () -> Unit
) {
    Surface(
        onClick = onClickExpand,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.92f),
        border = BorderStroke(
            1.dp,
            if (isImpossible) Color(0xFFF43F5E).copy(alpha = 0.75f) else Color(0xFF38BDF8).copy(alpha = 0.65f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_direction_radar_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mini Radar Map
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF475569), RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pad = 5.dp.toPx()
                    val plotW = (size.width - pad * 2f).coerceAtLeast(1f)
                    val plotH = (size.height - pad * 2f).coerceAtLeast(1f)
                    val maxX = (mazeW - 1).coerceAtLeast(1).toFloat()
                    val maxY = (mazeH - 1).coerceAtLeast(1).toFloat()

                    val px = pad + (player.x / maxX) * plotW
                    val py = pad + (1f - (player.y / maxY)) * plotH
                    val gx = pad + (goal.x / maxX) * plotW
                    val gy = pad + (1f - (goal.y / maxY)) * plotH

                    drawCircle(color = Color(0xFFEF4444), radius = 3.5.dp.toPx(), center = Offset(gx, gy))
                    drawCircle(color = Color(0xFF38BDF8), radius = 3.5.dp.toPx(), center = Offset(px, py))

                    if (wolf != null) {
                        val wx = pad + (wolf.x / maxX) * plotW
                        val wy = pad + (1f - (wolf.y / maxY)) * plotH
                        drawCircle(color = Color(0xFFF97316), radius = 3.2.dp.toPx(), center = Offset(wx, wy))
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isImpossible) {
                        if (language == AppLanguage.VI) "🧭 LA BÀN TIÊN TRI (IMPOSSIBLE)" else "🧭 ORACLE COMPASS (IMPOSSIBLE)"
                    } else {
                        if (language == AppLanguage.VI) "🧭 LA BÀN TIÊN TRI" else "🧭 ORACLE COMPASS"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isImpossible) Color(0xFFFDA4AF) else Color(0xFF38BDF8)
                )
                Text(
                    text = if (language == AppLanguage.VI) {
                        "Còn ${hint.remainingStepsToGoal} bước tới Đích"
                    } else {
                        "${hint.remainingStepsToGoal} steps left to Goal"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFBBF24),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CoordinateGuideMapDialog(
    player: com.example.core.engine.Point,
    goal: com.example.core.engine.Point,
    wolf: com.example.core.engine.Point? = null,
    goalHint: GoalDirectionHint? = null,
    isImpossible: Boolean = false,
    mazeW: Int,
    mazeH: Int,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val dx = goal.x - player.x
    val dy = goal.y - player.y
    val playerCode = MazeConfig.encodeCoord(player)
    val goalCode = MazeConfig.encodeCoord(goal)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = if (language == AppLanguage.VI) "🗺️ Bản Đồ Định Vị Tọa Độ" else "🗺️ Coordinate & Compass Map",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. SƠ ĐỒ LA BÀN & VỊ TRÍ TƯƠNG ĐỐI GIỮA NGƯỜI CHƠI VÀ ĐÍCH
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val pad = 18.dp.toPx()
                        val plotW = (w - pad * 2f).coerceAtLeast(1f)
                        val plotH = (h - pad * 2f).coerceAtLeast(1f)

                        // Lưới tọa độ minh họa
                        val gridColor = Color(0xFF334155)
                        for (i in 0..4) {
                            val gx = pad + (plotW * i / 4f)
                            val gy = pad + (plotH * i / 4f)
                            drawLine(gridColor, Offset(gx, pad), Offset(gx, pad + plotH), strokeWidth = 1f)
                            drawLine(gridColor, Offset(pad, gy), Offset(pad + plotW, gy), strokeWidth = 1f)
                        }

                        // Quy đổi tọa độ người chơi và đích lên bản đồ
                        val maxX = (mazeW - 1).coerceAtLeast(1).toFloat()
                        val maxY = (mazeH - 1).coerceAtLeast(1).toFloat()

                        val px = pad + (player.x / maxX) * plotW
                        val py = pad + (1f - (player.y / maxY)) * plotH

                        val gx = pad + (goal.x / maxX) * plotW
                        val gy = pad + (1f - (goal.y / maxY)) * plotH

                        // Không vẽ đường chỉ hướng - chỉ hiển thị điểm Người chơi và Đích trên bản đồ
                        // Vẽ điểm Đích (Cờ Đỏ)
                        drawCircle(
                            color = Color(0x44EF4444),
                            radius = 12.dp.toPx(),
                            center = Offset(gx, gy)
                        )
                        drawCircle(
                            color = Color(0xFFEF4444),
                            radius = 6.dp.toPx(),
                            center = Offset(gx, gy)
                        )

                        // Vẽ điểm Người Chơi (Xanh Cyan)
                        drawCircle(
                            color = Color(0x4438BDF8),
                            radius = 12.dp.toPx(),
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = Color(0xFF38BDF8),
                            radius = 6.dp.toPx(),
                            center = Offset(px, py)
                        )

                        // Vẽ điểm Sói (nếu có)
                        if (wolf != null) {
                            val wx = pad + (wolf.x / maxX) * plotW
                            val wy = pad + (1f - (wolf.y / maxY)) * plotH
                            drawCircle(
                                color = Color(0x55F97316),
                                radius = 11.dp.toPx(),
                                center = Offset(wx, wy)
                            )
                            drawCircle(
                                color = Color(0xFFF97316),
                                radius = 5.5.dp.toPx(),
                                center = Offset(wx, wy)
                            )
                        }
                    }

                    // Nhãn hướng Bắc / Nam / Tây / Đông trên bản đồ
                    Text(
                        text = if (language == AppLanguage.VI) "↑ BẮC (+Số)" else "↑ NORTH (+Num)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                    Text(
                        text = if (language == AppLanguage.VI) "↓ NAM (-Số)" else "↓ SOUTH (-Num)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                    Text(
                        text = if (language == AppLanguage.VI) "← TÂY" else "← W",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    Text(
                        text = if (language == AppLanguage.VI) "ĐÔNG (+Chữ) →" else "E (+Letter) →",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                // 2. BẢNG ĐỐI CHIẾU TỌA ĐỘ HIỆN TẠI & ĐÍCH
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (language == AppLanguage.VI) "🔵 BẠN ĐANG Ở" else "🔵 YOUR POS",
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = playerCode,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (language == AppLanguage.VI) "🚩 TỌA ĐỘ ĐÍCH" else "🚩 GOAL POS",
                                fontSize = 10.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = goalCode,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                    }
                }

                // 3. SỐ BƯỚC CÒN LẠI TỚI ĐÍCH (LA BÀN CHỈ BÁO SỐ BƯỚC)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2B1F0D),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (language == AppLanguage.VI) "🧭 La Bàn Định Vị" else "🧭 Oracle Compass",
                            fontSize = 11.sp,
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (goalHint != null) {
                                if (language == AppLanguage.VI) "Còn ${goalHint.remainingStepsToGoal} bước tới Đích"
                                else "${goalHint.remainingStepsToGoal} steps left to Goal"
                            } else {
                                if (language == AppLanguage.VI) "Đang tính toán..." else "Calculating..."
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                // 4. CÁCH ĐỌC TỌA ĐỘ NHANH
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = if (language == AppLanguage.VI) "📌 Cách đọc mã tọa độ (VD: $playerCode):" else "📌 How to read coordinates (e.g. $playerCode):",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = if (language == AppLanguage.VI)
                            "• 2 Chữ cái đầu (AA → ZZ): Cột Ngang. Đi sang PHẢI chữ tăng (AA→AB→AC), sang TRÁI chữ giảm."
                        else
                            "• First 2 Letters (AA → ZZ): Column. Moving RIGHT increases letters (AA→AB→AC), LEFT decreases.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = if (language == AppLanguage.VI)
                            "• 2 Chữ số sau (00 → 99): Hàng Dọc. Đi lên TRÊN số tăng (00→01→02), xuống DƯỚI số giảm."
                        else
                            "• Last 2 Digits (00 → 99): Row. Moving UP increases numbers (00→01→02), DOWN decreases.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = Strings.understood(language),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    )
}

@Composable
private fun StudioAssistButton(
    icon: ImageVector,
    label: String,
    badgeText: String?,
    accentColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .width(92.dp)
            .height(32.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(MazeSurface2, MazeSurface1)
                )
            )
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.45f),
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MazeTextH1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = accentColor.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.65f))
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GameWinModal(
    levelDef: LevelDef,
    language: AppLanguage,
    moves: Int,
    elapsedSec: Int,
    earnedStars: Int,
    collectedCoins: Int = 0,
    collectedKeys: Int = 0,
    collectedShields: Int = 0,
    justUnlockedImpossible: Boolean,
    justUnlockedWolfMode: Boolean = false,
    justUnlockedNextTier: Boolean,
    justClearedSuper: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                .testTag("win_modal")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 ${Strings.winTitle(language)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stars display
                if (!levelDef.isFinal) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= earnedStars) MazeStar else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val isUntimedWin = !levelDef.isWolfChase && (levelDef.noTimer || levelDef.numericLevel == 1)
                Text(
                    text = if (isUntimedWin) {
                        if (language == AppLanguage.VI) "$moves bước • Không tính thời gian" else "$moves moves • No time limit"
                    } else {
                        Strings.winStats(language, moves, elapsedSec)
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hiển thị phần thưởng & vật phẩm thu thập được trong màn
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🪙 +${50 + collectedCoins} Xu",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFBBF24)
                        )
                        Text(
                            text = "🔑 +${1 + collectedKeys} Chìa",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF59E0B)
                        )
                        if (collectedShields > 0) {
                            Text(
                                text = "🛡️ +$collectedShields Khiên",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }

                if (justUnlockedWolfMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.VI) {
                            "🐺 ĐÃ MỞ KHÓA CHẾ ĐỘ SÓI TRUY ĐUỔI (5×5 → 30×30)!"
                        } else {
                            "🐺 UNLOCKED WOLF CHASE MODE (5×5 → 30×30)!"
                        },
                        color = Color(0xFFF97316),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                if (justUnlockedImpossible) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "👑 ${Strings.impossibleUnlocked(language)}",
                        color = MazeDanger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (justClearedSuper) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "👑 ${Strings.superCleared(language)}",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                if (justUnlockedNextTier) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🔥 ${Strings.nextTierUnlocked(language)}",
                        color = MazeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Chỉ hiển thị nút Tiếp tục và nút Xem lại
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("win_next_level_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = when {
                                levelDef.tier == "DAILY" -> if (language == AppLanguage.VI) "Trở về Thử Thách Ngày" else "Back to Daily Challenge"
                                levelDef.isFinal -> Strings.nextTier(language)
                                else -> "${Strings.nextLevel(language)} (${levelDef.w + 1}×${levelDef.h + 1})"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onReplay,
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("win_replay_btn"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Strings.replayRoute(language), fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameLossModal(
    reason: String,
    language: AppLanguage,
    isWolfChase: Boolean = false,
    canReview: Boolean = false,
    onReview: () -> Unit = {},
    onRetry: () -> Unit,
    onAddExtraTimeAd: () -> Unit,
    onOpenShop: () -> Unit = {},
    onGoHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(2.dp, MazeDanger, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💔 ${Strings.lossTitle(language)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MazeDanger
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = reason,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canReview) {
                        OutlinedButton(
                            onClick = onReview,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Replay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Strings.reviewPath(language), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.retry(language), fontWeight = FontWeight.Bold)
                    }

                    if (!isWolfChase) {
                        OutlinedButton(
                            onClick = onAddExtraTimeAd,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(Strings.watchAdExtraTime(language))
                        }
                    }

                    // Nút dẫn vào Shop Màu & VIP
                    Surface(
                        onClick = onOpenShop,
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF2B1F0D),
                        border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("loss_vip_shop_btn")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Strings.tabShop(language),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.5.sp,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }

                    TextButton(
                        onClick = onGoHome,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Strings.backToHome(language), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
