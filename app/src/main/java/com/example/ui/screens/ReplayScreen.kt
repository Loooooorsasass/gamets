package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.engine.Point
import com.example.core.i18n.Strings
import com.example.data.shop.ShopCatalog
import com.example.ui.components.LanguageToggleSwitch
import com.example.ui.components.MazeCanvas
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun ReplayScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lang = uiState.language
    val game = uiState.activeGame
    val replay = uiState.replay
    val maze = game.maze
    val levelDef = game.levelDef

    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
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
                    text = Strings.noReplayData(lang),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Strings.noReplayDataSub(lang),
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A))
                ) {
                    Text(Strings.backToHome(lang), fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val history: List<Point> = game.pathHistory
    val currentStep: Point = if (history.isNotEmpty()) {
        history[replay.currentIndex.coerceIn(0, history.size - 1)]
    } else maze.start

    val trailSoFar: List<Point> = if (history.isNotEmpty()) {
        history.subList(0, (replay.currentIndex + 1).coerceAtMost(history.size))
    } else emptyList()

    val theme = ShopCatalog.getThemeById(uiState.progress.currentMazeThemeId)
    val skin = ShopCatalog.getSkinById(uiState.progress.currentSkinId)

    Box(modifier = modifier.fillMaxSize().background(theme.bgColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Strings.back(lang),
                        tint = theme.wallColor
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val titleText = if (levelDef.tier == "DAILY") Strings.tabDaily(lang)
                    else if (levelDef.tier == "SUPER") Strings.superTierTitle(lang)
                    else if (levelDef.tier != null) "${Strings.impossibleSectionTitle(lang)} ${levelDef.tier}"
                    else "${Strings.levelNum(lang, levelDef.numericLevel ?: 1)}"

                    Text(
                        text = "🎬 ${Strings.replayTitle(lang)}: $titleText",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = theme.wallColor
                    )
                    Text(
                        text = Strings.replayStep(lang, replay.currentIndex + 1, history.size),
                        fontSize = 12.sp,
                        color = theme.accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LanguageToggleSwitch(
                        currentLanguage = lang,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        isCompact = true
                    )
                    IconButton(onClick = { viewModel.startReplay() }) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = Strings.replayFromStart(lang),
                            tint = theme.wallColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOARD CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentAlignment = Alignment.Center
            ) {
                MazeCanvas(
                    maze = maze,
                    player = currentStep,
                    visitedCells = trailSoFar.map { it.y * maze.w + it.x }.toSet(),
                    theme = theme,
                    skin = skin,
                    vision = null,
                    replayTrail = trailSoFar,
                    onMove = { _, _ -> false },
                    modifier = Modifier.testTag("replay_canvas")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // REPLAY CONTROLS
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.panelColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.wallColor.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Playback progress slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${replay.currentIndex + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.wallColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = replay.currentIndex.toFloat(),
                            onValueChange = { newVal ->
                                viewModel.seekReplay(newVal.toInt())
                            },
                            valueRange = 0f..(history.size - 1).coerceAtLeast(1).toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = theme.accentColor,
                                activeTrackColor = theme.accentColor
                            ),
                            modifier = Modifier.weight(1f).testTag("replay_slider")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${history.size}",
                            fontSize = 12.sp,
                            color = theme.wallColor.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons row: Step-Back, Play/Pause, Step-Forward, and Speeds
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Step Back button
                        IconButton(
                            onClick = { viewModel.seekReplay(replay.currentIndex - 1) },
                            enabled = replay.currentIndex > 0,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = Strings.stepBack(lang),
                                tint = if (replay.currentIndex > 0) theme.wallColor else theme.wallColor.copy(alpha = 0.3f)
                            )
                        }

                        // Play/Pause button
                        Button(
                            onClick = { viewModel.toggleReplayPlayPause() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                            modifier = Modifier.size(width = 110.dp, height = 44.dp).testTag("replay_play_pause_btn")
                        ) {
                            Icon(
                                imageVector = if (replay.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (replay.isPlaying) Strings.pause(lang) else Strings.play(lang),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Step Forward button
                        IconButton(
                            onClick = { viewModel.seekReplay(replay.currentIndex + 1) },
                            enabled = replay.currentIndex < history.size - 1,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = Strings.stepForward(lang),
                                tint = if (replay.currentIndex < history.size - 1) theme.wallColor else theme.wallColor.copy(alpha = 0.3f)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Speed buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1f, 2f, 4f).forEach { spd ->
                                Button(
                                    onClick = { viewModel.setReplaySpeed(spd) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (replay.speed == spd) theme.accentColor else theme.panelColor
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.wallColor.copy(alpha = 0.2f)),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "${spd.toInt()}x",
                                        color = if (replay.speed == spd) Color.Black else theme.wallColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(text = Strings.backToHome(lang), color = theme.wallColor)
            }
        }
    }
}
