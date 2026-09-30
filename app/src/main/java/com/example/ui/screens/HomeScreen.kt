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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.engine.MazeConfig
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.data.shop.ShopCatalog
import com.example.ui.components.GlowingProgressBar
import kotlinx.coroutines.launch
import com.example.ui.components.LanguageToggleSwitch
import com.example.ui.components.MoveSensitivityCard
import com.example.ui.components.PremiumSegmentedNav
import com.example.ui.components.TactileImpossibleTierButton
import com.example.ui.components.TactileLevelButton
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeAmberGlow
import com.example.ui.theme.MazeBgDark
import com.example.ui.theme.MazeCoin
import com.example.ui.theme.MazeDanger
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeStar
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextH1
import com.example.ui.theme.MazeTextMuted
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

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
    var displayedLevelCount by remember { mutableIntStateOf(24) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var resetCheckState by remember { mutableStateOf(false) }
    var selectedLevelDefForDialog by remember { mutableStateOf<com.example.core.engine.LevelDef?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MazeBgDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // TOP BAR: Brand Icon, Title & Language Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_icon_art),
                    contentDescription = "MazeX Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MazeAmber.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = Strings.appTitle(lang),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MazeTextH1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Strings.appSubtitle(lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MazeTextBody
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Dedicated Language Switcher Component (VI 🇻🇳 <-> EN 🇬🇧)
            LanguageToggleSwitch(
                currentLanguage = lang,
                onLanguageSelected = { viewModel.setLanguage(it) },
                isCompact = false
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Spacer(modifier = Modifier.height(10.dp))

        // VIP ACTIVE TOP BANNER (Đưa thông báo VIP lên trên cùng khi đã kích hoạt)
        if (progress.vipTier > 0) {
            Surface(
                onClick = { viewModel.navigateTo(AppScreen.SHOP) },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF2B2011),
                border = BorderStroke(1.5.dp, MazeAmber),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vip_active_banner_top")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MazeAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = MazeAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (lang == AppLanguage.VI) "👑 ĐÃ KÍCH HOẠT VIP ${progress.vipTier} (Đặc quyền tối đa)" else "👑 VIP ${progress.vipTier} ACTIVE (Pro Privileges)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MazeAmber
                            )
                            Text(
                                text = if (lang == AppLanguage.VI) "Tắt mọi quảng cáo • +15 Gợi ý • +5 Bỏ qua • Tốc độ tối đa" else "No Ads • +15 Hints • +5 Skips • Max Speed",
                                fontSize = 10.sp,
                                color = MazeTextBody
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // STATS & UTILITY ROW: VIP Badge, Stars, Coins, Cloud Sync & Help
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút Cửa Hàng gọn gàng chuẩn Studio
                Surface(
                    onClick = { viewModel.navigateTo(AppScreen.SHOP) },
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFF2B2011),
                    border = BorderStroke(1.dp, MazeAmber),
                    modifier = Modifier.testTag("home_vip_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = Strings.tabShop(lang),
                            tint = MazeAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Strings.tabShop(lang),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MazeAmber
                        )
                    }
                }

                // Total Stars Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MazeSurface1)
                        .border(1.dp, MazeEdgeHighlight, RoundedCornerShape(999.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        tint = MazeStar,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${progress.totalStars}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MazeTextH1
                    )
                }

                // Coins Pill (Click mở Shop Bản Đồ Màu & Nhân Vật)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MazeSurface1)
                        .border(1.dp, MazeAmber.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                        .clickable { viewModel.navigateTo(AppScreen.SHOP) }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = MazeCoin,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${progress.coins}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MazeTextH1
                    )
                }

                // Nút Trang Bị Vật Phẩm Sói Đuổi & Khiên Hộ Mệnh (Click mở giao diện Trang Bị)
                val equippedGear = ShopCatalog.getEquipmentById(progress.equippedGearId)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MazeSurface1)
                        .border(1.dp, Color(0xFFF97316).copy(alpha = 0.7f), RoundedCornerShape(999.dp))
                        .clickable { viewModel.openWolfEquipmentDialog() }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("home_open_wolf_gear_btn"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = equippedGear?.iconEmoji ?: "🎒",
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (lang == AppLanguage.VI) "Trang Bị" else "Gear",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFEA580C)
                    )
                    Text(
                        text = "🛡️${progress.shieldCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            // Quick Tools: Cloud Sync & Help
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { viewModel.openCloudSync() },
                    modifier = Modifier.size(34.dp).testTag("cloud_sync_icon_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = Strings.cloudSyncTitle(lang),
                        tint = MazeTextBody,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = Strings.helpDialogTitle(lang),
                        tint = MazeTextBody,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // NAVIGATION: Segmented Control mượt mà (Hôm nay / Xếp hạng / Cửa hàng)
        PremiumSegmentedNav(
            selectedScreen = uiState.currentScreen,
            language = lang,
            onSelectScreen = { screen -> viewModel.navigateTo(screen) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Trạng thái mở khóa của từng chế độ chơi
        val dailyStageCount = (if (progress.dailyStage1Time > 0 || progress.dailyStage > 1) 1 else 0) +
                (if (progress.dailyStage2Time > 0 || progress.dailyStage > 2) 1 else 0) +
                (if (progress.dailyStage3Time > 0 || progress.dailyAllCompleted) 1 else 0)
        val isDailyCompleted = progress.dailyAllCompleted || dailyStageCount >= 3
        val isWolfModeUnlocked = progress.highestCleared >= MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE
        val isOneLineUnlocked = progress.highestCleared >= 20
        val isImpossibleUnlocked = progress.highestCleared >= MazeConfig.PASSES_TO_UNLOCK_IMPOSSIBLE
        var showOneLineLockedDialog by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()
        val levelLazyListState = rememberLazyListState()
        val currentTargetLevel = when {
            saveSlot != null && saveSlot.defId.toIntOrNull() != null -> saveSlot.defId.toInt()
            else -> minOf(MazeConfig.MAX_LEVEL, progress.highestCleared + 1)
        }

        // Tự động cuộn đến màn chơi hiện tại người chơi đang đạt được
        LaunchedEffect(currentTargetLevel) {
            val targetIndex = (currentTargetLevel - 1).coerceAtLeast(0)
            if (targetIndex > 0) {
                levelLazyListState.animateScrollToItem(maxOf(0, targetIndex - 1))
            }
        }

        // =====================================================================
        // PHẦN 1 (TRÊN ĐẦU TRANG): CÁC GAME SẴN SÀNG CHƠI NGAY
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0C2B22),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == AppLanguage.VI) "🟢 SẴN SÀNG CHƠI NGAY" else "🟢 READY TO PLAY NOW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF34D399)
                )
                Text(
                    text = if (lang == AppLanguage.VI) "Chọn màn & vào chơi lập tức" else "Select & play immediately",
                    fontSize = 10.5.sp,
                    color = Color(0xFF6EE7B7)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1A. RESUME GAME BANNER (nếu có ván đang chơi dở)
        if (saveSlot != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MazeAmber.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .testTag("resume_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val title = if (saveSlot.defId.startsWith("IMPOSSIBLE")) {
                            "${Strings.superTierTitle(lang)} (${Strings.tierLabel(lang, saveSlot.tier ?: "1")})"
                        } else {
                            "Level ${saveSlot.defId}"
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = Strings.resumeGameTitle(lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MazeAmber
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MazeAmber.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MazeAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Strings.resumeGameSub(lang, saveSlot.moves, saveSlot.elapsedSec),
                            fontSize = 11.sp,
                            color = MazeTextBody
                        )
                    }

                    Button(
                        onClick = { viewModel.resumeFromSave() },
                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp).testTag("resume_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(Strings.resumeButton(lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // 1B. GAME MÊ CUNG CHÍNH (Màn 1 → 100: Màn 1-50 xen kẽ TT 1,2 • Màn 51-100 xen kẽ TT 2,3) — LUÔN SẴN SÀNG CHƠI TRÊN ĐẦU TRANG
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, MazeAmber.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Hàng 1: Tiêu đề Danh Sách Cấp Độ bên trái + Nút Chơi Màn bên phải (tách biệt rõ ràng, không đè chữ)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.levelListTitle(lang),
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.ExtraBold,
                        color = MazeTextH1,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (saveSlot?.defId == currentTargetLevel.toString()) {
                                viewModel.resumeFromSave()
                            } else {
                                viewModel.startLevel(MazeConfig.generateLevelDef(currentTargetLevel))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MazeAmber,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp).testTag("play_current_normal_level_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == AppLanguage.VI) "Chơi Màn #$currentTargetLevel" else "Play #$currentTargetLevel",
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hàng 2: Các thẻ thông tin tiến độ & cỡ bản đồ hiện tại (nằm gọn gàng trên dòng riêng)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (lang == AppLanguage.VI) {
                                "Đã vượt: ${progress.highestCleared}/${MazeConfig.MAX_LEVEL}"
                            } else {
                                "Cleared: ${progress.highestCleared}/${MazeConfig.MAX_LEVEL}"
                            },
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp,
                            maxLines = 1,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    val targetSize = MazeConfig.mapSizeForLevel(currentTargetLevel)
                    val targetAlg = MazeConfig.algorithmIndexForLevel(currentTargetLevel)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (lang == AppLanguage.VI) {
                                "Màn #$currentTargetLevel: ${targetSize}×${targetSize} • Thuật toán $targetAlg"
                            } else {
                                "Lv #$currentTargetLevel: ${targetSize}×${targetSize} • Alg $targetAlg"
                            },
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp,
                            maxLines = 1,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (lang == AppLanguage.VI) {
                        "🧩 Màn 1–50: Xen kẽ Thuật toán 1 & 2 • Màn 51–100: Xen kẽ Thuật toán 2 & 3 (2 màn / 1 cỡ bản đồ)"
                    } else {
                        "🧩 Lv 1–50: Alternate Alg 1 & 2 • Lv 51–100: Alternate Alg 2 & 3 (2 levels per map size)"
                    },
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MazeTextBody,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Thanh chuyển nhanh theo khoảng Màn chơi (Màn 1 -> 100)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tiers = listOf(
                        Strings.jumpPlayingNow(lang) to currentTargetLevel,
                        "1 - 25 (5×5→17×17)" to 1,
                        "26 - 50 (17×17→29×29)" to 26,
                        "51 - 75 (30×30→42×42)" to 51,
                        "76 - 100 (42×42→54×54)" to 76
                    )
                    items(tiers) { (label, jumpLevel) ->
                        val isSelected = (jumpLevel == currentTargetLevel) || (jumpLevel != currentTargetLevel && currentTargetLevel in jumpLevel..(jumpLevel + 24))
                        Surface(
                            onClick = {
                                coroutineScope.launch {
                                    val targetIndex = (jumpLevel - 1).coerceAtLeast(0)
                                    levelLazyListState.animateScrollToItem(maxOf(0, targetIndex - 1))
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MazeAmber.copy(alpha = 0.2f) else MazeSurface2,
                            border = BorderStroke(1.dp, if (isSelected) MazeAmber else MazeEdgeHighlight)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MazeAmber else MazeTextBody,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // HORIZONTAL SCROLLING LEVEL MAP VỚI TỰ ĐỘNG CUỘN & PHÓNG TO MÀN ĐẠT ĐƯỢC
                val maxToShow = minOf(MazeConfig.MAX_LEVEL, maxOf(progress.highestCleared + 5, displayedLevelCount))
                val levelList = remember(maxToShow) { (1..maxToShow).toList() }

                LazyRow(
                    state = levelLazyListState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(levelList, key = { it }) { levelNum ->
                        val def = MazeConfig.generateLevelDef(levelNum)
                        val isCleared = levelNum <= progress.highestCleared
                        val reqStars = MazeConfig.starReqFor(levelNum)
                        val isUnlocked = (levelNum <= progress.highestCleared + 1) && (reqStars == 0 || progress.totalStars >= reqStars)
                        val starsEarned = records[levelNum.toString()]?.stars ?: 0
                        val isSaved = saveSlot?.defId == levelNum.toString()
                        val isCurrentTarget = (levelNum == currentTargetLevel)

                        TactileLevelButton(
                            levelNumber = levelNum,
                            size = def.w,
                            isUnlocked = isUnlocked,
                            isCleared = isCleared,
                            starsEarned = starsEarned,
                            reqStars = reqStars,
                            currentStars = progress.totalStars,
                            isSaved = isSaved,
                            isCurrentTarget = isCurrentTarget,
                            onClick = {
                                if (isSaved) {
                                    viewModel.resumeFromSave()
                                } else if (isCleared) {
                                    selectedLevelDefForDialog = def
                                } else {
                                    viewModel.startLevel(def)
                                }
                            }
                        )
                    }
                }

                if (displayedLevelCount < MazeConfig.MAX_LEVEL) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { displayedLevelCount = minOf(MazeConfig.MAX_LEVEL, displayedLevelCount + 20) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(MazeEdgeHighlight, MazeEdgeHighlight)))
                    ) {
                        Text(Strings.showMoreLevels(lang), fontSize = 12.sp, color = MazeTextBody)
                    }
                }
            }
        }

        // 1C. ĐUA TỐC ĐỘ NGÀY (Hiện trên đầu trang nếu hôm nay còn lượt sẵn sàng chơi)
        if (!isDailyCompleted) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MazeAmber.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .clickable { viewModel.navigateTo(AppScreen.DAILY_CHALLENGE) }
                    .testTag("daily_challenge_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MazeAmber.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Challenge",
                                tint = MazeAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Strings.dailyBannerTitle(lang),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MazeTextH1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = MazeAmber.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.VI) "$dailyStageCount/3 LƯỢT" else "$dailyStageCount/3 RUNS",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MazeAmber,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (lang == AppLanguage.VI) "Sẵn sàng đua tốc độ 3 chặng (14×14 → 16×16) nhận Xu & Sao!" else "Ready for 3 daily speed stages (14×14 → 16×16)!",
                                fontSize = 11.sp,
                                color = MazeTextBody
                            )
                        }
                    }

                    Surface(
                        onClick = { viewModel.navigateTo(AppScreen.DAILY_CHALLENGE) },
                        shape = RoundedCornerShape(8.dp),
                        color = MazeAmber
                    ) {
                        Text(
                            text = Strings.speedRace(lang),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 1D. CHẾ ĐỘ SÓI TRUY ĐUỔI (Hiện ở nhóm SẴN SÀNG CHƠI trên đầu trang khi đã mở khóa >= Màn 10)
        val nextWolfLevel = (progress.wolfHighestCleared + 1).coerceIn(1, MazeConfig.WOLF_MAX_LEVEL)
        if (isWolfModeUnlocked) {
            Spacer(modifier = Modifier.height(12.dp))
            WolfChasePlayableCard(
                progress = progress,
                lang = lang,
                nextWolfLevel = nextWolfLevel,
                onPlayWolfLevel = { lvl -> viewModel.startWolfChaseLevel(lvl) },
                onOpenWolfEquipment = { viewModel.openWolfEquipmentDialog() }
            )
        }

        // 1E. MINI-GAME VẼ 1 NÉT (Hiện ở nhóm SẴN SÀNG CHƠI trên đầu trang khi đã mở khóa >= Màn 20)
        if (isOneLineUnlocked) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.ONELINE_GAME) }
                    .border(1.2.dp, Color(0xFF10B981).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .testTag("oneline_minigame_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✏️", fontSize = 21.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = Strings.oneLineCardTitle(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Strings.oneLineUnlockedDesc(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MazeTextBody
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.ONELINE_GAME) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Text(Strings.play(lang), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 1F. CHẾ ĐỘ IMPOSSIBLE & SIÊU CẤP (Hiện ở nhóm SẴN SÀNG CHƠI trên đầu trang khi đã mở khóa >= Màn 50)
        if (isImpossibleUnlocked) {
            Spacer(modifier = Modifier.height(12.dp))
            ImpossibleModeCard(
                progress = progress,
                saveSlot = saveSlot,
                lang = lang,
                isImpossibleUnlocked = true,
                onSelectClearedDef = { selectedLevelDefForDialog = it },
                onStartDef = { viewModel.startLevel(it) },
                onResume = { viewModel.resumeFromSave() }
            )
        }

        // =====================================================================
        // PHẦN 2 (Ở DƯỚI TRANG): GAME CẦN LÀM GÌ & MỞ KHI NÀO (CHƯA MỞ KHÓA / CHỜ LÀM MỚI)
        // =====================================================================
        val hasBelowSection = !isWolfModeUnlocked || !isOneLineUnlocked || !isImpossibleUnlocked || isDailyCompleted
        if (hasBelowSection) {
            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF261D10),
                border = BorderStroke(1.dp, MazeAmber.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == AppLanguage.VI) "🔒 CHẾ ĐỘ SẮP MỞ KHÓA • CẦN LÀM GÌ & MỞ KHI NÀO" else "🔒 UPCOMING MODES • WHAT TO DO & WHEN TO UNLOCK",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MazeAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2A. THỬ THÁCH SÓI TRUY ĐUỔI (Khi chưa mở khóa < Màn 10)
            if (!isWolfModeUnlocked) {
                val remToUnlockWolf = maxOf(1, MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE - progress.highestCleared)
                val wolfUnlockProg = (progress.highestCleared.toFloat() / MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE.toFloat()).coerceIn(0f, 1f)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF97316).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .testTag("wolf_chase_mode_card")
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🐺", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (lang == AppLanguage.VI) "THỬ THÁCH SÓI TRUY ĐUỔI (5×5 → 30×30)" else "WOLF CHASE CHALLENGE (5×5 → 30×30)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFF97316)
                                    )
                                    Text(
                                        text = if (lang == AppLanguage.VI) {
                                            "⏰ Mở khi nào: Khi vượt qua Màn ${MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE} Mê Cung Chính"
                                        } else {
                                            "⏰ Unlocks when: Clearing Normal Level ${MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE}"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDBA74)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MazeSurface2
                            ) {
                                Text(
                                    text = "${progress.highestCleared}/${MazeConfig.PASSES_TO_UNLOCK_WOLF_MODE}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MazeAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        GlowingProgressBar(
                            progress = wolfUnlockProg,
                            activeColor = Color(0xFFF97316),
                            glowColor = MazeAmberGlow,
                            trackColor = MazeSurface2
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (lang == AppLanguage.VI) {
                                "📌 Cần làm gì: Vượt thêm $remToUnlockWolf màn thường nữa để mở khóa!\n" +
                                "🎒 Trang bị hỗ trợ màn Sói: Giày Thần Tốc (+15s), Khiên Aegis Cổ Đại (+1 Khiên miễn phí), La Bàn Tiên Tri (Radar hướng đích)."
                            } else {
                                "📌 What to do: Clear $remToUnlockWolf more Normal levels to unlock!\n" +
                                "🎒 Wolf Gear: Haste Boots (+15s), Ancient Aegis Shield (+1 free Shield), Oracle Compass (Goal Radar)."
                            },
                            fontSize = 11.5.sp,
                            color = MazeTextBody,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.openWolfEquipmentDialog() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF97316),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.VI) "🎒 Mở Trang Bị Sói Đuổi" else "🎒 Open Wolf Gear",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.startLevel(MazeConfig.generateLevelDef(currentTargetLevel)) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MazeAmber),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.VI) "Chơi Màn #$currentTargetLevel" else "Play #$currentTargetLevel",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MazeAmber
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 2B. MINI-GAME VẼ 1 NÉT (Khi chưa mở khóa < Màn 20)
            if (!isOneLineUnlocked) {
                val remToUnlockOneLine = maxOf(1, 20 - progress.highestCleared)
                val oneLineProg = (progress.highestCleared.toFloat() / 20f).coerceIn(0f, 1f)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOneLineLockedDialog = true }
                        .border(1.dp, MazeEdgeHighlight, RoundedCornerShape(16.dp))
                        .testTag("oneline_minigame_card")
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MazeSurface2),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🔒", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = Strings.oneLineCardTitle(lang),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MazeTextH1
                                    )
                                    Text(
                                        text = if (lang == AppLanguage.VI) {
                                            "⏰ Mở khi nào: Khi vượt qua Màn 20 Mê Cung Chính"
                                        } else {
                                            "⏰ Unlocks when: Clearing Normal Level 20"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MazeSurface2
                            ) {
                                Text(
                                    text = "${minOf(20, progress.highestCleared)}/20",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MazeAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        GlowingProgressBar(
                            progress = oneLineProg,
                            activeColor = Color(0xFF10B981),
                            glowColor = MazeAmberGlow,
                            trackColor = MazeSurface2
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == AppLanguage.VI) {
                                "📌 Cần làm gì: Vượt thêm $remToUnlockOneLine màn thường nữa để mở khóa chế độ Vẽ 1 Nét!"
                            } else {
                                "📌 What to do: Clear $remToUnlockOneLine more Normal levels to unlock One-Line Puzzle!"
                            },
                            fontSize = 11.5.sp,
                            color = MazeTextBody
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 2C. CHẾ ĐỘ IMPOSSIBLE & SIÊU CẤP (Khi chưa mở khóa < Màn 50)
            if (!isImpossibleUnlocked) {
                val clearedCount = minOf(progress.highestCleared, MazeConfig.PASSES_TO_UNLOCK_IMPOSSIBLE)
                val remToUnlockImp = maxOf(1, MazeConfig.PASSES_TO_UNLOCK_IMPOSSIBLE - progress.highestCleared)
                val progPercent = clearedCount / MazeConfig.PASSES_TO_UNLOCK_IMPOSSIBLE.toFloat()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MazeSurface1)
                        .border(1.dp, MazeEdgeHighlight, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Strings.impossibleSectionTitle(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MazeTextH1
                            )
                            Text(
                                text = if (lang == AppLanguage.VI) {
                                    "⏰ Mở khi nào: Khi vượt qua Màn ${MazeConfig.PASSES_TO_UNLOCK_IMPOSSIBLE} Mê Cung Chính"
                                } else {
                                    "⏰ Unlocks when: Clearing Normal Level ${MazeConfig.PASSES_TO_UNLOCK_IMPOSSIBLE}"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MazeAmber
                            )
                        }
                        Text(
                            text = Strings.levelsProgress(lang, clearedCount, MazeConfig.PASSES_TO_UNLOCK_IMPOSSIBLE),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MazeAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    GlowingProgressBar(
                        progress = progPercent,
                        activeColor = MazeAmber,
                        glowColor = MazeAmberGlow,
                        trackColor = MazeSurface2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (lang == AppLanguage.VI) {
                            "📌 Cần làm gì: Vượt thêm $remToUnlockImp màn thường nữa để mở khóa mê cung khổng lồ 75×75 → 1000×1000!"
                        } else {
                            "📌 What to do: Clear $remToUnlockImp more Normal levels to unlock giant 75×75 → 1000×1000 mazes!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MazeTextBody
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 2D. ĐUA TỐC ĐỘ NGÀY (Khi đã hoàn thành cả 3/3 lượt hôm nay -> Hiển thị ở dưới chờ làm mới ngày mai)
            if (isDailyCompleted) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(AppScreen.DAILY_CHALLENGE) }
                        .testTag("daily_challenge_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✓ ${Strings.dailyBannerTitle(lang)} (3/3)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = if (lang == AppLanguage.VI) {
                                    "⏰ Mở khi nào: Làm mới 3 lượt đua mới vào 00:00 ngày mai"
                                } else {
                                    "⏰ Unlocks when: Resets 3 new runs at 00:00 tomorrow"
                                },
                                fontSize = 11.sp,
                                color = MazeTextBody
                            )
                        }
                        Surface(
                            onClick = { viewModel.navigateTo(AppScreen.DAILY_CHALLENGE) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = if (lang == AppLanguage.VI) "Xem Lại" else "Replay",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Popup Dialog thông báo khóa One-Line
        if (showOneLineLockedDialog) {
            AlertDialog(
                onDismissRequest = { showOneLineLockedDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = Strings.locked(lang),
                            tint = MazeAmber
                        )
                        Text(
                            text = Strings.oneLineLockedDialogTitle(lang),
                            fontWeight = FontWeight.Bold,
                            color = MazeTextH1,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Text(
                        text = Strings.oneLineLockedDialogBody(lang, minOf(20, progress.highestCleared)),
                        color = MazeTextBody,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showOneLineLockedDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A))
                    ) {
                        Text(Strings.understood(lang), fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MazeSurface2
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // MOVE SENSITIVITY CONTROLLER CARD (Tăng/giảm độ nhạy vuốt và D-Pad)
        MoveSensitivityCard(
            sensitivity = progress.moveSensitivity,
            onSensitivityChange = { viewModel.setMoveSensitivity(it) },
            lang = lang,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // AUDIO, HAPTIC & LANGUAGE SETTINGS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MazeSurface1)
                .border(1.dp, MazeEdgeHighlight, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { viewModel.toggleSound() }) {
                    Icon(
                        imageVector = if (progress.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = Strings.soundLabel(lang),
                        tint = if (progress.soundEnabled) MazeAmber else MazeTextBody
                    )
                }
                IconButton(onClick = { viewModel.toggleMusic() }) {
                    Icon(
                        imageVector = if (progress.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                        contentDescription = Strings.musicLabel(lang),
                        tint = if (progress.musicEnabled) MazeAmber else MazeTextBody
                    )
                }
                IconButton(onClick = { viewModel.toggleHaptics() }) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = Strings.hapticLabel(lang),
                        tint = if (progress.hapticEnabled) MazeAmber else MazeTextBody
                    )
                }
            }

            LanguageToggleSwitch(
                currentLanguage = lang,
                onLanguageSelected = { viewModel.setLanguage(it) },
                isCompact = false
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reset progress link
        TextButton(
            onClick = { showResetConfirmDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(Strings.resetDataButton(lang), color = MazeTextMuted, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetConfirmDialog = false
                resetCheckState = false
            },
            title = { Text(Strings.resetDialogTitle(lang), color = MazeDanger, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(Strings.resetDialogBody(lang))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { resetCheckState = !resetCheckState }
                    ) {
                        Checkbox(
                            checked = resetCheckState,
                            onCheckedChange = { resetCheckState = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.resetCheckboxLabel(lang), fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmResetAll()
                        showResetConfirmDialog = false
                        resetCheckState = false
                    },
                    enabled = resetCheckState,
                    colors = ButtonDefaults.buttonColors(containerColor = MazeDanger)
                ) {
                    Text(Strings.resetConfirmButton(lang))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showResetConfirmDialog = false
                    resetCheckState = false
                }) {
                    Text(Strings.cancel(lang))
                }
            }
        )
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(Strings.helpDialogTitle(lang), fontWeight = FontWeight.Bold, color = MazeTextH1) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(Strings.helpLine1(lang), fontWeight = FontWeight.Bold, color = MazeAmber)
                    Text(Strings.helpLine2(lang))
                    Text(Strings.helpLine3(lang))
                    Text(Strings.helpLine4(lang))
                    Text(Strings.helpLine5(lang))
                    Text(Strings.helpLine6(lang))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A))
                ) {
                    Text(Strings.understood(lang))
                }
            }
        )
    }

    // Level Action Dialog (Play Again or Replay)
    val selectedDef = selectedLevelDefForDialog
    if (selectedDef != null) {
        val record = records[selectedDef.id]
        val title = when {
            selectedDef.tier == "DAILY" -> Strings.tabDaily(lang)
            selectedDef.tier == "SUPER" -> "${Strings.superTierTitle(lang)} 1000×1000"
            selectedDef.tier != null -> "${Strings.impossibleSectionTitle(lang)} (${selectedDef.tier})"
            else -> "Level ${selectedDef.numericLevel ?: selectedDef.id}"
        }

        AlertDialog(
            onDismissRequest = { selectedLevelDefForDialog = null },
            title = {
                Text(
                    text = "🎮 $title (${selectedDef.w}×${selectedDef.h})",
                    fontWeight = FontWeight.Bold,
                    color = MazeTextH1,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (record != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(Strings.bestRecord(lang), color = MazeTextBody, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                for (i in 1..3) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (i <= record.stars) MazeStar else Color(0x33FFFFFF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(Strings.timeLabel(lang), color = MazeTextBody, fontSize = 13.sp)
                            Text("${record.bestTimeSec}s", color = MazeAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(Strings.movesLabel(lang), color = MazeTextBody, fontSize = 13.sp)
                            Text("${record.bestMoves} (${Strings.targetLabel(lang, selectedDef.target)})", color = MazeTextH1, fontSize = 13.sp)
                        }
                    } else {
                        Text(
                            text = Strings.exactGoalLabel(lang, selectedDef.target),
                            color = MazeTextBody,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val defToStart = selectedDef
                            selectedLevelDefForDialog = null
                            viewModel.startLevel(defToStart)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.playAgain(lang), fontWeight = FontWeight.Bold)
                    }

                    if (selectedDef.isReplaySupported && record?.pathHistoryJson?.isNotEmpty() == true) {
                        OutlinedButton(
                            onClick = {
                                val defId = selectedDef.id
                                selectedLevelDefForDialog = null
                                viewModel.loadAndStartReplay(defId)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Strings.replayRouteBtn(lang), fontWeight = FontWeight.Bold, color = MazeTextH1)
                        }
                    }

                    TextButton(
                        onClick = { selectedLevelDefForDialog = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Strings.close(lang), color = MazeTextMuted)
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
private fun WolfChasePlayableCard(
    progress: com.example.data.local.GameProgressEntity,
    lang: AppLanguage,
    nextWolfLevel: Int,
    onPlayWolfLevel: (Int) -> Unit,
    onOpenWolfEquipment: () -> Unit
) {
    val activeGear = ShopCatalog.getEquipmentById(progress.equippedGearId)
    val activeGearDurability = if (activeGear != null) progress.getGearDurability(activeGear.id) else 0
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MazeSurface1),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, Color(0xFFF97316).copy(alpha = 0.75f), RoundedCornerShape(16.dp))
            .testTag("wolf_chase_mode_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(text = "🐺", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (lang == AppLanguage.VI) "THỬ THÁCH SÓI TRUY ĐUỔI" else "WOLF CHASE CHALLENGE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFB923C)
                        )
                        Text(
                            text = "5×5 → 30×30 • ${progress.wolfHighestCleared}/${MazeConfig.WOLF_MAX_LEVEL}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MazeAmber
                        )
                        Text(
                            text = if (lang == AppLanguage.VI) {
                                "⚡ Sói đuổi sau 5 giây bắt đầu • Thời gian -30% • ❄️ >25 ô Ném Băng Xuyên Tường"
                            } else {
                                "⚡ Wolf chases after 5s • Time -30% • ❄️ >25 cells Wall-Piercing Ice"
                            },
                            fontSize = 10.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onPlayWolfLevel(nextWolfLevel) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97316),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp).testTag("play_wolf_mode_btn")
                ) {
                    Text(
                        text = if (lang == AppLanguage.VI) "Đua Màn #$nextWolfLevel" else "Play #$nextWolfLevel",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Thanh mở giao diện Trang Bị Vật Phẩm Hỗ Trợ Màn Sói
            Surface(
                onClick = onOpenWolfEquipment,
                shape = RoundedCornerShape(10.dp),
                color = MazeSurface2,
                border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (activeGear != null && activeGearDurability > 0) {
                                if (lang == AppLanguage.VI) "🎒 Đang dùng: ${activeGear.iconEmoji} ${activeGear.nameVi} ($activeGearDurability/${activeGear.maxDurability} ván)"
                                else "🎒 Equipped: ${activeGear.iconEmoji} ${activeGear.nameEn} ($activeGearDurability/${activeGear.maxDurability} games)"
                            } else {
                                if (lang == AppLanguage.VI) "🎒 Chưa chọn trang bị hỗ trợ màn Sói (Độ bền 4–5 ván)"
                                else "🎒 No Wolf Chase gear equipped (Durability 4–5 games)"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MazeTextH1
                        )
                        Text(
                            text = if (lang == AppLanguage.VI) "Giày Thần Tốc (5 ván) • Khiên Aegis (4 ván) • La Bàn Tiên Tri (5 ván)"
                            else "Haste Boots (5 games) • Aegis Shield (4 games) • Oracle Compass (5 games)",
                            fontSize = 10.sp,
                            color = Color(0xFFFB923C)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MazeAmber
                    ) {
                        Text(
                            text = if (lang == AppLanguage.VI) "Trang Bị" else "Equip",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val wolfLevels = remember { (1..MazeConfig.WOLF_MAX_LEVEL).toList() }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(wolfLevels, key = { "wolf_$it" }) { wLvl ->
                    val wDef = MazeConfig.wolfChaseDef(wLvl)
                    val isCleared = wLvl <= progress.wolfHighestCleared
                    val isUnlocked = wLvl <= progress.wolfHighestCleared + 1
                    Surface(
                        onClick = { onPlayWolfLevel(wLvl) },
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isCleared -> Color(0xFF064E3B).copy(alpha = 0.55f)
                            isUnlocked -> MazeSurface2
                            else -> Color(0xFF0F172A)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isCleared -> Color(0xFF10B981)
                                isUnlocked -> Color(0xFFF97316)
                                else -> MazeEdgeHighlight
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isCleared) "✓ #${wLvl}" else if (isUnlocked) "🐺 #${wLvl}" else "🔒 #${wLvl}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCleared) Color(0xFF34D399) else if (isUnlocked) MazeTextH1 else MazeTextMuted
                            )
                            Text(
                                text = "${wDef.w}×${wDef.h}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color(0xFFFB923C) else MazeTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpossibleModeCard(
    progress: com.example.data.local.GameProgressEntity,
    saveSlot: com.example.data.local.SaveSlotEntity?,
    lang: AppLanguage,
    isImpossibleUnlocked: Boolean,
    onSelectClearedDef: (com.example.core.engine.LevelDef) -> Unit,
    onStartDef: (com.example.core.engine.LevelDef) -> Unit,
    onResume: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MazeSurface1),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isImpossibleUnlocked) MazeAmber.copy(alpha = 0.5f) else MazeEdgeHighlight,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = Strings.impossibleSectionTitle(lang),
                        tint = if (isImpossibleUnlocked) MazeAmber else MazeTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.impossibleSectionTitle(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isImpossibleUnlocked) MazeAmber else MazeTextH1
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = Strings.impossibleUnlockedDesc(lang),
                style = MaterialTheme.typography.bodySmall,
                color = MazeTextBody
            )

            Spacer(modifier = Modifier.height(14.dp))

            val impScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(impScroll),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (tier in 1..MazeConfig.IMPOSSIBLE_TIER_SIZES.size) {
                    val def = MazeConfig.impossibleDefForTier(tier)
                    val tierUnlocked = isImpossibleUnlocked && (tier <= progress.impossibleCleared + 1)
                    val tierCleared = tier <= progress.impossibleCleared
                    val isSaved = saveSlot?.defId == def.id

                    TactileImpossibleTierButton(
                        title = "${def.w}×${def.h}",
                        subtitle = Strings.stepsUnit(lang, def.target),
                        isUnlocked = tierUnlocked,
                        isCleared = tierCleared,
                        isSaved = isSaved,
                        onClick = {
                            if (isSaved) {
                                onResume()
                            } else if (tierCleared) {
                                onSelectClearedDef(def)
                            } else {
                                onStartDef(def)
                            }
                        }
                    )
                }

                val superDef = MazeConfig.superDef()
                val superUnlocked = isImpossibleUnlocked && progress.impossibleCleared >= MazeConfig.IMPOSSIBLE_TIER_SIZES.size
                val superCleared = progress.superCleared
                val isSuperSaved = saveSlot?.defId == superDef.id

                TactileImpossibleTierButton(
                    title = Strings.superTierTitle(lang),
                    subtitle = "1000×1000 · 10k",
                    isUnlocked = superUnlocked,
                    isCleared = superCleared,
                    isSaved = isSuperSaved,
                    isSuper = true,
                    onClick = {
                        if (isSuperSaved) {
                            onResume()
                        } else if (superCleared) {
                            onSelectClearedDef(superDef)
                        } else {
                            onStartDef(superDef)
                        }
                    }
                )
            }
        }
    }
}
