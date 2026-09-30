package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.data.cloud.DailyChallengeManager
import com.example.data.cloud.LeaderboardManager
import com.example.ui.components.GlowingProgressBar
import com.example.ui.components.LanguageToggleSwitch
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeAmberGlow
import com.example.ui.theme.MazeBgDark
import com.example.ui.theme.MazeCoin
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeStar
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextH1
import com.example.ui.theme.MazeTextMuted
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun DailyChallengeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lang = uiState.language
    val progress = uiState.progress
    val todayDate = DailyChallengeManager.getTodayDateKey()

    // Countdown timer đếm ngược tới 00:00:00 hàng ngày (Reset 3 lượt)
    var cooldownMillis by remember { mutableLongStateOf(DailyChallengeManager.getCooldownRemainingMillis()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            cooldownMillis = DailyChallengeManager.getCooldownRemainingMillis()
            delay(1000L)
        }
    }

    val cooldownStr = DailyChallengeManager.formatCooldown(cooldownMillis)

    // Trạng thái các lượt chơi trong ngày (3 lượt: 14x14, 15x15, 16x16)
    val stage1Cleared = progress.dailyStage1Time > 0 || progress.dailyStage > 1
    val stage2Cleared = progress.dailyStage2Time > 0 || progress.dailyStage > 2
    val stage3Cleared = progress.dailyStage3Time > 0 || progress.dailyAllCompleted

    val isAllCompleted = progress.dailyAllCompleted || (stage1Cleared && stage2Cleared && stage3Cleared)
    val isCooldown = isAllCompleted || progress.dailyAttemptsUsed >= 3

    val currentStage = when {
        isAllCompleted -> 4
        stage2Cleared -> 3
        stage1Cleared -> 2
        else -> 1
    }

    val completedStagesCount = (if (stage1Cleared) 1 else 0) + (if (stage2Cleared) 1 else 0) + (if (stage3Cleared) 1 else 0)

    val bestDailyScoreSec = if (stage3Cleared) progress.dailyStage3Time else if (stage2Cleared) progress.dailyStage2Time else if (stage1Cleared) progress.dailyStage1Time else null
    val dailyLeaderboard = LeaderboardManager.getDailyLeaderboard(playerScoreSec = bestDailyScoreSec)

    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MazeBgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = Modifier.testTag("daily_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Strings.back(lang),
                        tint = MazeTextH1
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = Strings.dailyTitle(lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MazeTextH1
                    )
                    Text(
                        text = Strings.dailyDateSub(lang, todayDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MazeAmber
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = { viewModel.navigateTo(AppScreen.SHOP) },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF2B1F0D),
                    border = BorderStroke(1.2.dp, Color(0xFFFFD700))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "VIP Shop",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "VIP • SHOP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }

                LanguageToggleSwitch(
                    currentLanguage = lang,
                    onLanguageSelected = { viewModel.setLanguage(it) },
                    isCompact = true
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // COMPACT HERO BANNER: 3 LƯỢT THỬ THÁCH MỖI NGÀY & TIẾN ĐỘ
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFEC4899))),
                    RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = Strings.dailyHeroTitle(lang),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    // Progress Pill (e.g. 2/3 Lượt)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAllCompleted) Color(0xFF10B981) else MazeSurface2,
                        border = BorderStroke(1.dp, if (isAllCompleted) Color.White.copy(alpha = 0.5f) else MazeEdgeHighlight)
                    ) {
                        Text(
                            text = if (isAllCompleted) "🏆 3/3 XONG" else "⚡ $completedStagesCount/3 LƯỢT",
                            color = if (isAllCompleted) Color.Black else MazeAmber,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                GlowingProgressBar(
                    progress = completedStagesCount / 3f,
                    activeColor = Color(0xFF8B5CF6),
                    glowColor = Color(0xFFA855F7),
                    trackColor = MazeSurface2
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Cooldown Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Cooldown",
                            tint = if (isCooldown) MazeAmber else MazeTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Strings.dailyCooldownPrefix(lang),
                            fontSize = 11.sp,
                            color = MazeTextBody
                        )
                    }

                    Text(
                        text = "$cooldownStr (00:00)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp,
                        color = if (isCooldown) MazeAmber else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 DAILY STAGES CARDS
        Text(
            text = "🎯 3 LƯỢT THỬ THÁCH HÔM NAY",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MazeTextH1
        )

        Spacer(modifier = Modifier.height(6.dp))

        // STAGE 1: 14x14
        DailyStageCard(
            stageNumber = 1,
            size = 14,
            target = 95,
            rewardText = Strings.dailyRewardBadge(lang, 1),
            isCleared = stage1Cleared,
            clearedTimeSec = progress.dailyStage1Time,
            isCurrent = currentStage == 1 && !isCooldown,
            isLocked = false,
            language = lang,
            onPlay = { viewModel.startDailyChallenge(1) },
            onReplay = { viewModel.loadAndStartReplay("DAILY_${todayDate}_STAGE_1") }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // STAGE 2: 15x15
        DailyStageCard(
            stageNumber = 2,
            size = 15,
            target = 110,
            rewardText = Strings.dailyRewardBadge(lang, 2),
            isCleared = stage2Cleared,
            clearedTimeSec = progress.dailyStage2Time,
            isCurrent = currentStage == 2 && !isCooldown,
            isLocked = !stage1Cleared,
            language = lang,
            onPlay = { viewModel.startDailyChallenge(2) },
            onReplay = { viewModel.loadAndStartReplay("DAILY_${todayDate}_STAGE_2") }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // STAGE 3: 16x16
        DailyStageCard(
            stageNumber = 3,
            size = 16,
            target = 130,
            rewardText = Strings.dailyRewardBadge(lang, 3),
            isCleared = stage3Cleared,
            clearedTimeSec = progress.dailyStage3Time,
            isCurrent = currentStage == 3 && !isCooldown,
            isLocked = !stage2Cleared,
            isFinal = true,
            language = lang,
            onPlay = { viewModel.startDailyChallenge(3) },
            onReplay = { viewModel.loadAndStartReplay("DAILY_${todayDate}_STAGE_3") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // PRIMARY ACTION BUTTON (Bắt đầu lượt tiếp theo hoặc Cooldown thông báo)
        if (!isCooldown) {
            val targetSize = DailyChallengeManager.getDailySize(currentStage)
            Button(
                onClick = { viewModel.startDailyChallenge(currentStage) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (currentStage) {
                        1 -> Color(0xFF6366F1)
                        2 -> Color(0xFF8B5CF6)
                        else -> Color(0xFFEC4899)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("start_daily_stage_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentStage == 1) Strings.dailyStartStageBtn(lang, 1, 14) else Strings.dailyContinueStageBtn(lang, currentStage, targetSize),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.5.sp,
                    color = Color.White
                )
            }
        } else {
            // Out of turns / All completed Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MazeSurface1,
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.dailyAllDoneBtn(lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${Strings.dailyCooldownPrefix(lang)} $cooldownStr (00:00)",
                        fontSize = 12.sp,
                        color = MazeTextBody
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DAILY LEADERBOARD SECTION
        Text(
            text = Strings.dailyTopSpeed(lang),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MazeTextH1
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (dailyLeaderboard.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MazeSurface1,
                border = BorderStroke(1.dp, MazeEdgeHighlight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = Strings.noDailyRecords(lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MazeTextH1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Strings.noDailyRecordsSub(lang),
                        fontSize = 12.sp,
                        color = MazeTextBody
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dailyLeaderboard.forEach { entry ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MazeSurface1,
                        border = BorderStroke(1.dp, MazeEdgeHighlight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${entry.rank}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (entry.rank <= 3) MazeStar else MazeTextMuted,
                                    modifier = Modifier.width(32.dp)
                                )
                                Text(text = entry.avatarEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = entry.playerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MazeTextH1
                                )
                            }

                            Text(
                                text = entry.scoreText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MazeAmber
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun DailyStageCard(
    stageNumber: Int,
    size: Int,
    target: Int,
    rewardText: String,
    isCleared: Boolean,
    clearedTimeSec: Int,
    isCurrent: Boolean,
    isLocked: Boolean,
    language: AppLanguage,
    isFinal: Boolean = false,
    onPlay: () -> Unit,
    onReplay: () -> Unit
) {
    val borderColor = when {
        isCleared -> Color(0xFF10B981).copy(alpha = 0.6f)
        isCurrent -> MazeAmber
        else -> MazeEdgeHighlight
    }

    val cardBg = when {
        isCleared -> Color(0xFF064E3B).copy(alpha = 0.25f)
        isCurrent -> MazeSurface2
        else -> MazeSurface1
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isLocked && !isCleared) { onPlay() }
            .testTag("daily_stage_card_$stageNumber")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Number badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            when {
                                isCleared -> Color(0xFF10B981)
                                isCurrent -> MazeAmber
                                else -> Color(0xFF1E293B)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCleared) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(17.dp)
                        )
                    } else if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MazeTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    } else {
                        Text(
                            text = "$stageNumber",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (isCurrent) Color.Black else MazeTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Strings.dailyStageTitle(language, stageNumber, size),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isCleared) Color(0xFFA7F3D0) else if (isCurrent) MazeTextH1 else MazeTextBody
                        )
                        if (isFinal) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEC4899).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "FINAL 🔥",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFEC4899),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isCleared && clearedTimeSec > 0)
                            Strings.dailyStageCleared(language, clearedTimeSec)
                        else if (isLocked)
                            Strings.dailyStageLocked(language)
                        else
                            "${Strings.exactGoalLabel(language, target)} · $rewardText",
                        fontSize = 11.sp,
                        color = if (isCleared) Color(0xFF10B981) else MazeTextMuted
                    )
                }
            }

            // Action Button
            if (isCleared) {
                IconButton(
                    onClick = onReplay,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Replay",
                        tint = MazeAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (isCurrent) {
                Button(
                    onClick = onPlay,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color.Black),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(Strings.play(language), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
