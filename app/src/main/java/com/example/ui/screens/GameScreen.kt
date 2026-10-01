package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.components.CoordinateGuideMapDialog
import com.example.ui.components.DPad
import com.example.ui.components.GridControlMode
import com.example.ui.components.LanguageToggleSwitch
import com.example.ui.components.MazeCanvas
import com.example.ui.components.MoveSensitivityDialog
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
 * Premium Studio Puzzle Screen:
 * - 1. Board is Hero: Maze takes ~75% of the screen area without clutter.
 * - 2. Top Bar: [Back] -> LEVEL XX (Size) -> [Reset] + [Settings].
 * - 3. Minimalist HUD: Time + Moves (+ Wolf status/Impossible coords only when applicable).
 * - 4. Controls Hierarchy: Touch/Swipe on board, Undo (secondary), Hint/Skip (assist), D-Pad in Step mode.
 * - 5. Dark-First Palette: Background #090B16, Quiet UI, High-contrast Maze.
 */
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
                .background(MazeBgDark),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = Strings.preparingLevel(lang),
                    color = MazeTextH1,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF090B16)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Strings.backToHome(lang), fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val theme = ShopCatalog.getThemeById(uiState.progress.currentMazeThemeId)
    val skin = ShopCatalog.getSkinById(uiState.progress.currentSkinId)

    var showInGameSettingsDialog by remember { mutableStateOf(false) }
    var showSkipConfirmDialog by remember { mutableStateOf(false) }
    var showCoordGuideDialog by remember { mutableStateOf(false) }
    var showSensitivityDialog by remember { mutableStateOf(false) }
    var showCoordinatesOnHUD by remember { mutableStateOf(levelDef.isFinal) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MazeBgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─────────────────────────────────────────────────────────────
            // 1. TOP BAR: Back -> Level & Size -> Reset & In-Game Settings
            // ─────────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (>= 48dp touch target)
                PuzzleTopBarButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = Strings.back(lang),
                    onClick = { viewModel.requestExit() },
                    modifier = Modifier.testTag("game_back_button")
                )

                // Level Title & Grid Dimensions (Clean Typography)
                val titleMain = when {
                    levelDef.isWolfChase -> if (lang == AppLanguage.VI) "WOLF CHASE ${levelDef.numericLevel ?: 1}" else "WOLF CHASE ${levelDef.numericLevel ?: 1}"
                    levelDef.tier == "DAILY" -> "DAILY PUZZLE"
                    levelDef.isFinal -> if (levelDef.tier == "SUPER") "SUPER MAZE" else "IMPOSSIBLE ${levelDef.numericLevel ?: 1}"
                    else -> "LEVEL ${levelDef.numericLevel ?: 1}"
                }
                val subtitle = "${maze.w} × ${maze.h}"

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = titleMain,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MazeTextH1,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MazeTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Right Utility Group (Reset & In-Game Settings)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PuzzleTopBarButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = Strings.reset(lang),
                        onClick = { viewModel.resetCurrentLevel() },
                        modifier = Modifier.testTag("game_reset_level_top_btn")
                    )

                    PuzzleTopBarButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = { showInGameSettingsDialog = true },
                        modifier = Modifier.testTag("game_settings_top_btn")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ─────────────────────────────────────────────────────────────
            // 2. MINIMALIST HUD: Time + Moves (+ Mode Toggle / Wolf / Coords)
            // ─────────────────────────────────────────────────────────────
            val isUntimedLevel = !levelDef.isWolfChase && (levelDef.noTimer || levelDef.numericLevel == 1)
            val remainingSec = if (game.timeLimitSec > 0) maxOf(0, game.timeLimitSec - game.elapsedSec) else 0
            val isAutoMode = game.controlMode == GridControlMode.AUTO

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MazeSurface1,
                border = BorderStroke(1.dp, MazeEdgeHighlight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (game.timeLimitSec > 0 && remainingSec <= 10) MazeDanger else MazeTextBody,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = when {
                                isUntimedLevel -> "∞"
                                game.timeLimitSec > 0 -> "${remainingSec}s"
                                else -> "${game.elapsedSec}s"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (game.timeLimitSec > 0 && remainingSec <= 10) MazeDanger else MazeTextH1
                        )
                    }

                    // Special Mode Status (Wolf / Coords)
                    if (levelDef.isWolfChase) {
                        val isPlayerFrozen = System.currentTimeMillis() < game.playerFrozenUntilMs
                        val wolfText = when {
                            isPlayerFrozen -> if (lang == AppLanguage.VI) "❄️ ĐÓNG BĂNG" else "❄️ FROZEN"
                            !game.wolfActive -> "WOLF: ${maxOf(1, game.wolfCountdownSec)}s"
                            else -> "WOLF: ${game.wolfDistanceCells} CELLS"
                        }
                        Text(
                            text = wolfText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPlayerFrozen) Color(0xFF38BDF8) else MazeDanger
                        )
                    } else if (showCoordinatesOnHUD || levelDef.isFinal) {
                        Text(
                            text = "${MazeConfig.encodeCoord(game.player)} → ${MazeConfig.encodeCoord(maze.goal)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MazeAmber
                        )
                    }

                    // Moves Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MazeTextBody,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${game.moves} ${if (lang == AppLanguage.VI) "bước" else "moves"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MazeTextH1
                        )
                    }

                    // Mode Pill (SWIPE / STEP)
                    Surface(
                        onClick = { viewModel.toggleControlMode() },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAutoMode) MazeAmber.copy(alpha = 0.15f) else MazeSurface2,
                        border = BorderStroke(1.dp, if (isAutoMode) MazeAmber.copy(alpha = 0.6f) else MazeEdgeHighlight),
                        modifier = Modifier.testTag("toggle_control_mode_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isAutoMode) Icons.Default.Swipe else Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = if (isAutoMode) MazeAmber else MazeTextBody,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isAutoMode) "SWIPE" else "STEP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoMode) MazeAmber else MazeTextBody
                            )
                        }
                    }
                }
            }

            // Slim Timer Progress Bar (Only when time limit applies)
            val hasTimeLimit = game.timeLimitSec > 0 || (levelDef.isWolfChase && !game.wolfActive)
            if (hasTimeLimit) {
                Spacer(modifier = Modifier.height(3.dp))
                val headStartSec = if (levelDef.isWolfChase) {
                    if ((levelDef.numericLevel ?: 1) <= 5) 3 else 5
                } else 1
                val timeRatio = if (game.timeLimitSec > 0) {
                    (maxOf(0, game.timeLimitSec - game.elapsedSec).toFloat() / game.timeLimitSec.toFloat()).coerceIn(0f, 1f)
                } else if (levelDef.isWolfChase && !game.wolfActive) {
                    (maxOf(0, game.wolfCountdownSec).toFloat() / maxOf(1, headStartSec).toFloat()).coerceIn(0f, 1f)
                } else {
                    1f
                }
                val timerColor = when {
                    timeRatio > 0.4f -> MazeSuccess
                    timeRatio > 0.2f -> MazeAmber
                    else -> MazeDanger
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MazeSurface2)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(timeRatio)
                            .clip(RoundedCornerShape(2.dp))
                            .background(timerColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ─────────────────────────────────────────────────────────────
            // 3. MAZE CANVAS BOARD (HERO COMPONENT - MAXIMIZED SPACE)
            // ─────────────────────────────────────────────────────────────
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

            Spacer(modifier = Modifier.height(4.dp))

            // ─────────────────────────────────────────────────────────────
            // 4. CONTROLS: Undo (Secondary), Hint (Assist), D-Pad in STEP
            // ─────────────────────────────────────────────────────────────
            if (isAutoMode) {
                // In AUTO (Swipe) Mode: Compact Action Bar (Max Board Area)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PuzzleActionButton(
                        icon = Icons.AutoMirrored.Filled.Undo,
                        label = Strings.undo(lang),
                        badgeText = null,
                        accentColor = MazeTextH1,
                        onClick = { viewModel.undoMove() }
                    )

                    PuzzleActionButton(
                        icon = Icons.Default.Lightbulb,
                        label = Strings.hint(lang),
                        badgeText = "${uiState.progress.hintCount}",
                        accentColor = MazeAmber,
                        onClick = { viewModel.useHint(activity) }
                    )

                    PuzzleActionButton(
                        icon = Icons.Default.FastForward,
                        label = Strings.skip(lang),
                        badgeText = "${uiState.progress.skipTokens}",
                        accentColor = MazeCyan,
                        onClick = {
                            if (uiState.progress.skipTokens > 0) {
                                showSkipConfirmDialog = true
                            } else {
                                viewModel.skipCurrentLevel(activity)
                            }
                        }
                    )
                }
            } else {
                // In STEP Mode: D-Pad Controller with Side Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Actions: Undo & Hint
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PuzzleActionButton(
                            icon = Icons.AutoMirrored.Filled.Undo,
                            label = Strings.undo(lang),
                            badgeText = null,
                            accentColor = MazeTextH1,
                            onClick = { viewModel.undoMove() }
                        )

                        PuzzleActionButton(
                            icon = Icons.Default.Lightbulb,
                            label = Strings.hint(lang),
                            badgeText = "${uiState.progress.hintCount}",
                            accentColor = MazeAmber,
                            onClick = { viewModel.useHint(activity) }
                        )
                    }

                    // Center: Minimalist D-Pad
                    DPad(
                        onMove = { dx, dy -> viewModel.tryMove(dx, dy) },
                        sensitivity = uiState.progress.moveSensitivity,
                        accentColor = MazeAmber,
                        wallColor = Color(0xFFF05AAB),
                        modifier = Modifier.testTag("game_dpad")
                    )

                    // Right Actions: Skip & Free Coins
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PuzzleActionButton(
                            icon = Icons.Default.FastForward,
                            label = Strings.skip(lang),
                            badgeText = "${uiState.progress.skipTokens}",
                            accentColor = MazeCyan,
                            onClick = {
                                if (uiState.progress.skipTokens > 0) {
                                    showSkipConfirmDialog = true
                                } else {
                                    viewModel.skipCurrentLevel(activity)
                                }
                            }
                        )

                        PuzzleActionButton(
                            icon = Icons.Default.Speed,
                            label = Strings.sensitivityLabel(lang),
                            badgeText = "${(uiState.progress.moveSensitivity * 100).toInt()}%",
                            accentColor = MazeTextBody,
                            onClick = { showSensitivityDialog = true }
                        )
                    }
                }
            }

            // Banner Ad (Anchored Cleanly at bottom)
            AdMobBannerView(
                isBannerAllowed = uiState.progress.isBannerAdAllowed(),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // ─────────────────────────────────────────────────────────────
        // 5. IN-GAME SETTINGS MODAL (Consolidates Audio, Sensitivity, Language, Coords, Exit)
        // ─────────────────────────────────────────────────────────────
        if (showInGameSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showInGameSettingsDialog = false },
                containerColor = MazeSurface1,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = if (lang == AppLanguage.VI) "Cài Đặt Ván Chơi" else "Game Settings",
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
                                    color = if (uiState.progress.soundEnabled) MazeAmber.copy(alpha = 0.2f) else MazeSurface2,
                                    border = BorderStroke(1.dp, if (uiState.progress.soundEnabled) MazeAmber else MazeEdgeHighlight),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (uiState.progress.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                            contentDescription = "Sound",
                                            tint = if (uiState.progress.soundEnabled) MazeAmber else MazeTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = { viewModel.toggleMusic() },
                                    shape = CircleShape,
                                    color = if (uiState.progress.musicEnabled) MazeAmber.copy(alpha = 0.2f) else MazeSurface2,
                                    border = BorderStroke(1.dp, if (uiState.progress.musicEnabled) MazeAmber else MazeEdgeHighlight),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (uiState.progress.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                            contentDescription = "Music",
                                            tint = if (uiState.progress.musicEnabled) MazeAmber else MazeTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = { viewModel.toggleHaptics() },
                                    shape = CircleShape,
                                    color = if (uiState.progress.hapticEnabled) MazeAmber.copy(alpha = 0.2f) else MazeSurface2,
                                    border = BorderStroke(1.dp, if (uiState.progress.hapticEnabled) MazeAmber else MazeEdgeHighlight),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Vibration,
                                            contentDescription = "Haptics",
                                            tint = if (uiState.progress.hapticEnabled) MazeAmber else MazeTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Sensitivity Button
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
                                    text = "${(uiState.progress.moveSensitivity * 100).toInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MazeAmber
                                )
                            }
                        }

                        // Language Toggle
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

                        // Coordinate Map Guide
                        Surface(
                            onClick = { showCoordGuideDialog = true },
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
                                    text = if (lang == AppLanguage.VI) "Xem Bản Đồ Tọa Độ (AA00)" else "Coordinate Guide Map",
                                    fontSize = 13.sp,
                                    color = MazeTextH1
                                )
                                Text(
                                    text = "→",
                                    fontSize = 14.sp,
                                    color = MazeAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showInGameSettingsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF090B16)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (lang == AppLanguage.VI) "Đóng" else "Close", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showInGameSettingsDialog = false
                            viewModel.requestExit()
                        }
                    ) {
                        Text(Strings.backToHome(lang), color = MazeDanger)
                    }
                }
            )
        }

        // EXIT CONFIRMATION MODAL
        if (game.showExitDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissExitDialog() },
                containerColor = MazeSurface1,
                shape = RoundedCornerShape(20.dp),
                title = { Text(text = Strings.exitTitle(lang), fontWeight = FontWeight.Bold, color = MazeTextH1) },
                text = { Text(text = Strings.exitMessage(lang, game.moves, game.elapsedSec), color = MazeTextBody) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.saveAndExit() },
                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF090B16)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_and_exit_btn")
                    ) {
                        Text(Strings.saveAndExit(lang), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row {
                        OutlinedButton(
                            onClick = { viewModel.discardAndExit() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MazeDanger),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(Strings.discardAndExit(lang))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(onClick = { viewModel.dismissExitDialog() }) {
                            Text(Strings.cancel(lang), color = MazeTextBody)
                        }
                    }
                }
            )
        }

        // SENSITIVITY MODAL
        if (showSensitivityDialog) {
            MoveSensitivityDialog(
                sensitivity = uiState.progress.moveSensitivity,
                onSensitivityChange = { viewModel.setMoveSensitivity(it) },
                onDismiss = { showSensitivityDialog = false },
                lang = lang
            )
        }

        // COORDINATE MAP MODAL
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
                containerColor = MazeSurface1,
                shape = RoundedCornerShape(20.dp),
                title = { Text(text = Strings.skipConfirmTitle(lang), fontWeight = FontWeight.Bold, color = MazeTextH1) },
                text = { Text(text = Strings.skipConfirmBody(lang, uiState.progress.skipTokens), color = MazeTextBody) },
                confirmButton = {
                    Button(
                        onClick = {
                            showSkipConfirmDialog = false
                            viewModel.skipCurrentLevel(activity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF090B16)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = Strings.skipConfirmBtn(lang), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSkipConfirmDialog = false }) {
                        Text(text = Strings.cancel(lang), color = MazeTextBody)
                    }
                }
            )
        }

        // ─────────────────────────────────────────────────────────────
        // 6. WIN OVERLAY MODAL (Focused on "SOLVED" Achievement)
        // ─────────────────────────────────────────────────────────────
        if (game.gameOver && game.hasWon) {
            CleanPuzzleWinModal(
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
                onReplay = { viewModel.startReplay() },
                onHome = { viewModel.navigateTo(AppScreen.HOME) }
            )
        }

        // ─────────────────────────────────────────────────────────────
        // 7. LOSS OVERLAY MODAL (Clean Puzzle Try Again)
        // ─────────────────────────────────────────────────────────────
        if (game.gameOver && !game.hasWon) {
            CleanPuzzleLossModal(
                reason = uiState.toastMessage ?: Strings.lossTitle(lang),
                language = lang,
                isWolfChase = levelDef.isWolfChase,
                canReview = levelDef.isReplaySupported && game.pathHistory.isNotEmpty(),
                onReview = { viewModel.startReplay() },
                onRetry = { viewModel.restartCurrentRun() },
                onAddExtraTimeAd = { viewModel.triggerRewardedAd(activity, "TIME") },
                onGoHome = { viewModel.navigateTo(AppScreen.HOME) }
            )
        }
    }
}

@Composable
private fun PuzzleTopBarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MazeSurface1,
        border = BorderStroke(1.dp, MazeEdgeHighlight),
        modifier = modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MazeTextH1,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PuzzleActionButton(
    icon: ImageVector,
    label: String,
    badgeText: String?,
    accentColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = MazeSurface1,
        border = BorderStroke(1.dp, MazeEdgeHighlight),
        modifier = Modifier
            .height(44.dp)
            .width(105.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MazeTextH1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MazeSurface2,
                    border = BorderStroke(1.dp, MazeEdgeHighlight)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Clean Studio Win Modal:
 * Focus on SOLVED status, stars, time, moves, and quick Next Level CTA.
 */
@Composable
private fun CleanPuzzleWinModal(
    levelDef: LevelDef,
    language: AppLanguage,
    moves: Int,
    elapsedSec: Int,
    earnedStars: Int,
    collectedCoins: Int,
    collectedKeys: Int,
    collectedShields: Int,
    justUnlockedImpossible: Boolean,
    justUnlockedWolfMode: Boolean,
    justUnlockedNextTier: Boolean,
    justClearedSuper: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
            border = BorderStroke(1.dp, MazeEdgeHighlight),
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .testTag("win_modal")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (language == AppLanguage.VI) "HOÀN THÀNH" else "SOLVED",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MazeTextH1,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stars display
                if (!levelDef.isFinal) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= earnedStars) MazeStar else Color(0xFF332B45),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Stats: Time & Moves
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MazeSurface2,
                    border = BorderStroke(1.dp, MazeEdgeHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (language == AppLanguage.VI) "THỜI GIAN" else "TIME",
                                fontSize = 10.sp,
                                color = MazeTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${elapsedSec}s",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MazeTextH1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(MazeEdgeHighlight)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (language == AppLanguage.VI) "SỐ BƯỚC" else "MOVES",
                                fontSize = 10.sp,
                                color = MazeTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$moves",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MazeTextH1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rewards row (subtle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+${50 + collectedCoins} Coins   •   +${1 + collectedKeys} Key",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MazeAmber
                    )
                }

                if (justUnlockedWolfMode || justUnlockedImpossible || justClearedSuper || justUnlockedNextTier) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            justUnlockedWolfMode -> if (language == AppLanguage.VI) "Mở khóa Chế độ Sói Truy Đuổi!" else "Unlocked Wolf Chase Mode!"
                            justUnlockedImpossible -> Strings.impossibleUnlocked(language)
                            justClearedSuper -> Strings.superCleared(language)
                            else -> Strings.nextTierUnlocked(language)
                        },
                        color = MazeSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions: Next Level (Hero Button), Replay, Home
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("win_next_level_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF090B16))
                    ) {
                        Text(
                            text = when {
                                levelDef.tier == "DAILY" -> if (language == AppLanguage.VI) "Về Thử Thách Ngày" else "Back to Daily"
                                levelDef.isFinal -> Strings.nextTier(language)
                                else -> Strings.nextLevel(language)
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReplay,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("win_replay_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MazeEdgeHighlight)
                        ) {
                            Text(text = Strings.replayRoute(language), color = MazeTextH1, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onHome,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MazeEdgeHighlight)
                        ) {
                            Text(text = Strings.backToHome(language), color = MazeTextBody, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean Studio Loss Modal
 */
@Composable
private fun CleanPuzzleLossModal(
    reason: String,
    language: AppLanguage,
    isWolfChase: Boolean = false,
    canReview: Boolean = false,
    onReview: () -> Unit = {},
    onRetry: () -> Unit,
    onAddExtraTimeAd: () -> Unit,
    onGoHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
            border = BorderStroke(1.dp, MazeDanger.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (language == AppLanguage.VI) "THỬ LẠI" else "TRY AGAIN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MazeDanger,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = reason,
                    fontSize = 13.sp,
                    color = MazeTextBody,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF090B16))
                    ) {
                        Text(Strings.retry(language), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    if (!isWolfChase) {
                        OutlinedButton(
                            onClick = onAddExtraTimeAd,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MazeEdgeHighlight)
                        ) {
                            Text(Strings.watchAdExtraTime(language), color = MazeTextH1, fontSize = 12.sp)
                        }
                    }

                    if (canReview) {
                        OutlinedButton(
                            onClick = onReview,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MazeEdgeHighlight)
                        ) {
                            Text(Strings.reviewPath(language), color = MazeTextBody, fontSize = 12.sp)
                        }
                    }

                    TextButton(
                        onClick = onGoHome,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Strings.backToHome(language), color = MazeTextMuted, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
