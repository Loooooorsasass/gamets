package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.engine.OneLineGenerator
import com.example.core.engine.OneLineMapResult
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.GlowingProgressBar
import com.example.ui.components.LanguageToggleSwitch
import com.example.ui.components.OneLineGameView
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
import com.example.ui.theme.StudioMapPalette
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

enum class OneLineCategory(val icon: String) {
    SQUARE("⬛"),
    SPECIAL_SHAPES("🦖");

    fun getDisplayName(lang: AppLanguage): String {
        return when (this) {
            SQUARE -> Strings.categorySquare(lang)
            SPECIAL_SHAPES -> Strings.categorySpecial(lang)
        }
    }
}

data class OneLineLevelConfig(
    val id: Int,
    val rows: Int,
    val cols: Int,
    val labelVi: String,
    val labelEn: String,
    val category: OneLineCategory,
    val shapeType: String = "GRID",
    val fillRate: Float = 0.90f
) {
    val size: Int get() = maxOf(rows, cols)
    fun getLabel(lang: AppLanguage): String = if (lang == AppLanguage.VI) labelVi else labelEn
}

// GIAI ĐOẠN 1: HÌNH VUÔNG TỪ 3x3 ĐẾN 15x15
val SQUARE_LEVELS = listOf(
    OneLineLevelConfig(1, 3, 3, "3 × 3", "3 × 3", OneLineCategory.SQUARE, "GRID", 1.00f),
    OneLineLevelConfig(2, 4, 4, "4 × 4", "4 × 4", OneLineCategory.SQUARE, "GRID", 1.00f),
    OneLineLevelConfig(3, 5, 5, "5 × 5", "5 × 5", OneLineCategory.SQUARE, "GRID", 0.96f),
    OneLineLevelConfig(4, 6, 6, "6 × 6", "6 × 6", OneLineCategory.SQUARE, "GRID", 0.94f),
    OneLineLevelConfig(5, 7, 7, "7 × 7", "7 × 7", OneLineCategory.SQUARE, "GRID", 0.92f),
    OneLineLevelConfig(6, 8, 8, "8 × 8", "8 × 8", OneLineCategory.SQUARE, "GRID", 0.90f),
    OneLineLevelConfig(7, 9, 9, "9 × 9", "9 × 9", OneLineCategory.SQUARE, "GRID", 0.90f),
    OneLineLevelConfig(8, 10, 10, "10 × 10", "10 × 10", OneLineCategory.SQUARE, "GRID", 0.88f),
    OneLineLevelConfig(9, 11, 11, "11 × 11", "11 × 11", OneLineCategory.SQUARE, "GRID", 0.88f),
    OneLineLevelConfig(10, 12, 12, "12 × 12", "12 × 12", OneLineCategory.SQUARE, "GRID", 0.87f),
    OneLineLevelConfig(11, 13, 13, "13 × 13", "13 × 13", OneLineCategory.SQUARE, "GRID", 0.86f),
    OneLineLevelConfig(12, 14, 14, "14 × 14", "14 × 14", OneLineCategory.SQUARE, "GRID", 0.85f),
    OneLineLevelConfig(13, 15, 15, "15 × 15 (Max)", "15 × 15 (Max)", OneLineCategory.SQUARE, "GRID", 0.85f)
)

// GIAI ĐOẠN 2: HÌNH ĐẶC BIỆT
val SPECIAL_SHAPE_LEVELS = listOf(
    OneLineLevelConfig(14, 8, 5, "Hình Chữ Nhật (Đứng)", "Rectangle (Vertical)", OneLineCategory.SPECIAL_SHAPES, "RECT_VERTICAL"),
    OneLineLevelConfig(15, 6, 10, "Hình Chữ Nhật (Ngang)", "Rectangle (Horizontal)", OneLineCategory.SPECIAL_SHAPES, "RECT_HORIZONTAL"),
    OneLineLevelConfig(16, 9, 9, "Ngôi Sao ⭐", "Star ⭐", OneLineCategory.SPECIAL_SHAPES, "STAR"),
    OneLineLevelConfig(17, 10, 10, "Khủng Long T-Rex 🦖", "T-Rex Dinosaur 🦖", OneLineCategory.SPECIAL_SHAPES, "DINOSAUR"),
    OneLineLevelConfig(18, 9, 9, "Trái Tim ❤️", "Heart ❤️", OneLineCategory.SPECIAL_SHAPES, "HEART"),
    OneLineLevelConfig(19, 9, 9, "Vương Miện Hoàng Gia 👑", "Royal Crown 👑", OneLineCategory.SPECIAL_SHAPES, "CROWN"),
    OneLineLevelConfig(20, 9, 9, "Kim Cương Lấp Lánh 💎", "Sparkling Diamond 💎", OneLineCategory.SPECIAL_SHAPES, "DIAMOND"),
    OneLineLevelConfig(21, 9, 9, "Thanh Gươm Huyền Thoại ⚔️", "Legendary Sword ⚔️", OneLineCategory.SPECIAL_SHAPES, "SWORD"),
    OneLineLevelConfig(22, 9, 9, "Ngôi Nhà Ấm Áp 🏠", "Cozy House 🏠", OneLineCategory.SPECIAL_SHAPES, "HOUSE"),
    OneLineLevelConfig(23, 9, 9, "Chữ Thập Cổ Điển ➕", "Classic Cross ➕", OneLineCategory.SPECIAL_SHAPES, "CROSS")
)

val ONE_LINE_LEVELS = SQUARE_LEVELS + SPECIAL_SHAPE_LEVELS

val SHAPE_PATTERNS = mapOf(
    "STAR" to arrayOf(
        "....*....",
        "...***...",
        "*********",
        ".*******.",
        "..*****..",
        ".*******.",
        "**.....**",
        "*.......*",
        "........."
    ),
    "DINOSAUR" to arrayOf(
        "....******",
        "....******",
        "....**....",
        "....****..",
        "***.****..",
        "*********.",
        ".********.",
        "..******..",
        "...*..*...",
        "..**..**.."
    ),
    "HEART" to arrayOf(
        ".***.***.",
        "*********",
        "*********",
        "*********",
        ".*******.",
        "..*****..",
        "...***...",
        "....*....",
        "........."
    ),
    "CROWN" to arrayOf(
        "*...*...*",
        "**..*..**",
        "*********",
        "*********",
        "*********",
        "*********",
        ".........",
        ".........",
        "........."
    ),
    "DIAMOND" to arrayOf(
        "....*....",
        "...***...",
        "..*****..",
        ".*******.",
        "*********",
        ".*******.",
        "..*****..",
        "...***...",
        "....*...."
    ),
    "SWORD" to arrayOf(
        "....*....",
        "....*....",
        "....*....",
        "....*....",
        "...***...",
        "*********",
        "....*....",
        "....*....",
        "...***..."
    ),
    "HOUSE" to arrayOf(
        "....*....",
        "...***...",
        "..*****..",
        ".*******.",
        "*********",
        ".*******.",
        ".*..*..*.",
        ".*..*..*.",
        ".*..*..*.",
        ".*******."
    ),
    "CROSS" to arrayOf(
        "...***...",
        "...***...",
        "...***...",
        "*********",
        "*********",
        "*********",
        "...***...",
        "...***...",
        "...***..."
    )
)

@Composable
fun OneLineScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lang = uiState.language
    val coroutineScope = rememberCoroutineScope()

    // Tiến trình mở khóa lưu trong Database
    val highestUnlockedIndex = uiState.progress.oneLineHighestUnlocked.coerceIn(0, ONE_LINE_LEVELS.lastIndex)

    // Khởi tạo màn chơi hiện tại theo tiến độ cao nhất của người chơi
    var selectedLevelIndex by remember {
        mutableIntStateOf(highestUnlockedIndex)
    }

    val currentLevel = ONE_LINE_LEVELS[selectedLevelIndex.coerceIn(0, ONE_LINE_LEVELS.lastIndex)]

    var oneLineViewRef by remember { mutableStateOf<OneLineGameView?>(null) }
    var currentSteps by remember { mutableIntStateOf(1) }
    var remainingCells by remember { mutableIntStateOf(0) }
    var totalCells by remember { mutableIntStateOf(0) }
    var showWinDialog by remember { mutableStateOf(false) }

    // Tính năng Hướng Dẫn: Free 1 lần mỗi màn
    var isHintUsedInCurrentLevel by remember { mutableStateOf(false) }
    var isHintShowing by remember { mutableStateOf(false) }

    var currentMapResult by remember { mutableStateOf<OneLineMapResult?>(null) }
    var selectedCategory by remember { mutableStateOf(currentLevel.category) }

    val levelSelectorListState = rememberLazyListState()

    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    fun generateLevelMap(config: OneLineLevelConfig): OneLineMapResult {
        return if (config.category == OneLineCategory.SQUARE) {
            OneLineGenerator.generateGridMap(
                rows = config.rows,
                cols = config.cols,
                seed = config.id.toLong() * 10007L + 42L,
                fillRate = config.fillRate
            )
        } else {
            val mask = SHAPE_PATTERNS[config.shapeType] ?: emptyArray()
            OneLineGenerator.generateShapedMap(
                rows = config.rows,
                cols = config.cols,
                maskPattern = mask,
                seed = config.id.toLong() * 10007L + 88L,
                shapeType = config.shapeType
            )
        }
    }

    val equippedTheme = com.example.data.shop.ShopCatalog.getThemeById(uiState.progress.currentMazeThemeId)

    fun loadLevelIndex(index: Int) {
        if (index > highestUnlockedIndex) {
            Toast.makeText(context, Strings.completePrevLevel(lang), Toast.LENGTH_SHORT).show()
            return
        }

        selectedLevelIndex = index
        showWinDialog = false
        isHintShowing = false
        isHintUsedInCurrentLevel = false

        val lvl = ONE_LINE_LEVELS[index]
        selectedCategory = lvl.category

        val mapRes = generateLevelMap(lvl)
        currentMapResult = mapRes
        totalCells = mapRes.totalTargetCells

        oneLineViewRef?.setPlayerSkinId(uiState.progress.currentSkinId)
        oneLineViewRef?.updateStudioColors(
            equippedTheme.wallColor.toArgb(),
            equippedTheme.accentColor.toArgb(),
            equippedTheme.panelColor.toArgb()
        )
        oneLineViewRef?.loadLevel(mapRes.rows, mapRes.cols, mapRes.grid, mapRes.startPoint, mapRes.solutionPath)
    }

    // Tự động cuộn đến màn chơi đã chọn trong danh sách
    LaunchedEffect(selectedLevelIndex, equippedTheme.id, uiState.progress.currentSkinId) {
        val visibleLevels = ONE_LINE_LEVELS.filter { it.category == selectedCategory }
        val targetSubIndex = visibleLevels.indexOfFirst { it.id == currentLevel.id }
        if (targetSubIndex >= 0) {
            levelSelectorListState.animateScrollToItem(maxOf(0, targetSubIndex - 1))
        }

        val lvl = currentLevel
        val mapRes = generateLevelMap(lvl)
        currentMapResult = mapRes
        totalCells = mapRes.totalTargetCells
        isHintShowing = false
        isHintUsedInCurrentLevel = false
        oneLineViewRef?.setPlayerSkinId(uiState.progress.currentSkinId)
        oneLineViewRef?.updateStudioColors(
            equippedTheme.wallColor.toArgb(),
            equippedTheme.accentColor.toArgb(),
            equippedTheme.panelColor.toArgb()
        )
        oneLineViewRef?.loadLevel(mapRes.rows, mapRes.cols, mapRes.grid, mapRes.startPoint, mapRes.solutionPath)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MazeBgDark)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // 1. TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier.testTag("oneline_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = Strings.back(lang),
                    tint = MazeTextH1
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Strings.oneLineTitle(lang),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MazeTextH1
                )
                Text(
                    text = if (currentLevel.category == OneLineCategory.SQUARE) Strings.squareRouteSub(lang) else "${Strings.specialShapeSub(lang)}: ${currentLevel.getLabel(lang)}",
                    fontSize = 11.sp,
                    color = MazeTextMuted
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = { viewModel.navigateTo(AppScreen.SHOP) },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF2B1F0D),
                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                    modifier = Modifier.testTag("oneline_vip_shop_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.tabShop(lang),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }

                LanguageToggleSwitch(
                    currentLanguage = lang,
                    onLanguageSelected = { viewModel.setLanguage(it) },
                    isCompact = true
                )

                Surface(
                    onClick = { viewModel.navigateTo(AppScreen.SHOP) },
                    shape = RoundedCornerShape(12.dp),
                    color = MazeSurface2,
                    border = BorderStroke(1.dp, MazeAmber.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🪙", fontSize = 12.sp)
                        Text(
                            text = "${uiState.progress.coins}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MazeCoin
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. QUICK OPTIONS BAR: CHƠI TIẾP vs CHƠI LẠI vs TIẾN ĐỘ
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MazeEdgeHighlight, RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.onelineProgressStatus(lang, highestUnlockedIndex, ONE_LINE_LEVELS.size),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MazeAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    GlowingProgressBar(
                        progress = (highestUnlockedIndex) / ONE_LINE_LEVELS.size.toFloat(),
                        activeColor = MazeAmber,
                        glowColor = MazeAmberGlow,
                        trackColor = MazeSurface2
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Nút Chơi Lại (Màn 1)
                    Surface(
                        onClick = { loadLevelIndex(0) },
                        shape = RoundedCornerShape(8.dp),
                        color = MazeSurface2,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Replay Level 1",
                                tint = MazeTextBody,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = Strings.levelNum(lang, 1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MazeTextBody
                            )
                        }
                    }

                    // Nút Chơi Tiếp (Màn cao nhất)
                    Surface(
                        onClick = { loadLevelIndex(highestUnlockedIndex) },
                        shape = RoundedCornerShape(8.dp),
                        color = MazeAmber,
                        modifier = Modifier.testTag("oneline_quick_continue_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Continue",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = Strings.onelineContinueBtn(lang, highestUnlockedIndex + 1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. CATEGORY SWITCHER TABS (Khung vuông vs Hình đặc biệt)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OneLineCategory.values().forEach { cat ->
                val isCatSelected = (cat == selectedCategory)
                val isUnlocked = if (cat == OneLineCategory.SPECIAL_SHAPES) {
                    highestUnlockedIndex >= SQUARE_LEVELS.size
                } else true

                Surface(
                    onClick = {
                        if (!isUnlocked) {
                            Toast.makeText(context, Strings.unlockSpecialReq(lang), Toast.LENGTH_SHORT).show()
                            return@Surface
                        }
                        selectedCategory = cat
                        if (cat == OneLineCategory.SQUARE && currentLevel.category != OneLineCategory.SQUARE) {
                            loadLevelIndex(0)
                        } else if (cat == OneLineCategory.SPECIAL_SHAPES && currentLevel.category != OneLineCategory.SPECIAL_SHAPES) {
                            loadLevelIndex(SQUARE_LEVELS.size)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCatSelected) MazeAmber else MazeSurface1,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${cat.icon} ", fontSize = 13.sp)
                        Text(
                            text = cat.getDisplayName(lang),
                            fontSize = 11.sp,
                            fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isUnlocked) MazeTextMuted else if (isCatSelected) Color.Black else MazeTextBody,
                            maxLines = 1
                        )
                        if (!isUnlocked) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = Strings.locked(lang),
                                tint = MazeTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. LEVEL SELECTOR HORIZONTAL MAP
        LazyRow(
            state = levelSelectorListState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val visibleLevels = ONE_LINE_LEVELS.filter { it.category == selectedCategory }
            itemsIndexed(visibleLevels) { _, lvl ->
                val globalIdx = ONE_LINE_LEVELS.indexOf(lvl)
                val isSelected = (globalIdx == selectedLevelIndex)
                val isUnlocked = globalIdx <= highestUnlockedIndex
                val isCleared = globalIdx < highestUnlockedIndex

                Surface(
                    onClick = {
                        if (isUnlocked) {
                            loadLevelIndex(globalIdx)
                        } else {
                            Toast.makeText(context, Strings.completeLevelToUnlock(lang, globalIdx, globalIdx + 1), Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isSelected -> MazeAmber
                        isCleared -> Color(0xFF064E3B).copy(alpha = 0.5f)
                        isUnlocked -> MazeSurface2
                        else -> Color(0xFF1E293B).copy(alpha = 0.5f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isSelected -> Color.White.copy(alpha = 0.8f)
                            isCleared -> Color(0xFF10B981).copy(alpha = 0.4f)
                            else -> Color.White.copy(alpha = 0.08f)
                        }
                    ),
                    modifier = Modifier.testTag("oneline_level_pill_$globalIdx")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isCleared) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Cleared",
                                tint = if (isSelected) Color.Black else Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                        } else if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = Strings.locked(lang),
                                tint = MazeTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = lvl.getLabel(lang),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = when {
                                isSelected -> Color.Black
                                isCleared -> Color(0xFFA7F3D0)
                                isUnlocked -> MazeTextBody
                                else -> MazeTextMuted
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. PROGRESS HUD & HINT BUTTON (Không tặng miễn phí - Đồ dùng 1 lần mất ngay sau khi kích hoạt)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MazeSurface1)
                .border(1.dp, MazeEdgeHighlight, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${Strings.levelNum(lang, selectedLevelIndex + 1)}/${ONE_LINE_LEVELS.size}: ${currentLevel.getLabel(lang)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MazeAmber
                    )
                    if (selectedLevelIndex < highestUnlockedIndex) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "⭐ ${Strings.onelineLevelClearedBadge(lang)}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (lang == AppLanguage.VI) {
                        "Ô đã nối: $currentSteps/${maxOf(1, totalCells)} • Còn lại: $remainingCells ô"
                    } else {
                        "Connected: $currentSteps/${maxOf(1, totalCells)} • Remaining: $remainingCells cells"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF38BDF8)
                )
            }

            // Nút Hướng Dẫn (Đồ dùng 1 lần: mất ngay sau khi kích hoạt hoặc mua bằng 150 Xu)
            Surface(
                onClick = {
                    if (isHintShowing) {
                        isHintShowing = false
                        oneLineViewRef?.toggleHint(false)
                    } else if (isHintUsedInCurrentLevel) {
                        isHintShowing = true
                        oneLineViewRef?.toggleHint(true)
                    } else {
                        val consumed = viewModel.consumeOneLineHint(activity)
                        if (consumed) {
                            isHintUsedInCurrentLevel = true
                            isHintShowing = true
                            oneLineViewRef?.toggleHint(true)
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                color = if (isHintShowing) Color(0xFFF59E0B) else Color(0xFF0284C7),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                modifier = Modifier.testTag("oneline_hint_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = Strings.hint(lang),
                        tint = if (isHintShowing) Color.Black else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    val hintLabel = when {
                        isHintShowing -> Strings.hideHint(lang)
                        isHintUsedInCurrentLevel -> if (lang == AppLanguage.VI) "Hiện Gợi Ý" else "Show Hint"
                        uiState.progress.hintCount > 0 -> if (lang == AppLanguage.VI) "Dùng 1 Gợi Ý (${uiState.progress.hintCount})" else "Use 1 Hint (${uiState.progress.hintCount})"
                        else -> if (lang == AppLanguage.VI) "Gợi Ý (🪙150)" else "Hint (🪙150)"
                    }
                    Text(
                        text = hintLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isHintShowing) Color.Black else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 6. MAIN ONELINE CANVAS VIEW (Tương phản cao chuẩn Studio)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0B1120))
                .border(
                    width = 1.8.dp,
                    color = Color(0xFF38BDF8).copy(alpha = 0.55f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    OneLineGameView(ctx).apply {
                        val mapRes = currentMapResult ?: generateLevelMap(currentLevel)
                        setPlayerSkinId(uiState.progress.currentSkinId)
                        updateStudioColors(
                            equippedTheme.wallColor.toArgb(),
                            equippedTheme.accentColor.toArgb(),
                            equippedTheme.panelColor.toArgb()
                        )
                        loadLevel(mapRes.rows, mapRes.cols, mapRes.grid, mapRes.startPoint, mapRes.solutionPath)
                        setOnGameStateListener(object : OneLineGameView.OnGameStateListener {
                            override fun onPathChanged(currentPath: MutableList<OneLineGameView.Point>?, remaining: Int) {
                                currentSteps = currentPath?.size ?: 1
                                remainingCells = remaining
                            }

                            override fun onGameWin(totalSteps: Int) {
                                currentSteps = totalSteps
                                remainingCells = 0
                                showWinDialog = true
                                viewModel.addCoins(30)
                                viewModel.onOneLineGameCleared(activity)

                                if (selectedLevelIndex + 1 > highestUnlockedIndex && selectedLevelIndex + 1 < ONE_LINE_LEVELS.size) {
                                    viewModel.updateOneLineProgress(selectedLevelIndex + 1)
                                }
                            }

                            override fun onUndo(removedPoint: OneLineGameView.Point?) {}
                        })
                        oneLineViewRef = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("oneline_game_canvas")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 7. ACTION CONTROLS: Undo, Reset, Next Level
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo
            Button(
                onClick = { oneLineViewRef?.undoStep() },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("oneline_undo_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MazeSurface2),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MazeEdgeHighlight)
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = Strings.undo(lang),
                    tint = MazeTextH1,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Strings.undo(lang),
                    color = MazeTextH1,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            // Reset
            Button(
                onClick = {
                    isHintShowing = false
                    oneLineViewRef?.resetLevel()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("oneline_reset_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MazeSurface2),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MazeEdgeHighlight)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = Strings.reset(lang),
                    tint = MazeTextH1,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Strings.reset(lang),
                    color = MazeTextH1,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            // Next Level
            val canGoNext = (selectedLevelIndex + 1) <= highestUnlockedIndex && (selectedLevelIndex + 1) < ONE_LINE_LEVELS.size
            Button(
                onClick = {
                    if (canGoNext) {
                        loadLevelIndex(selectedLevelIndex + 1)
                    } else {
                        Toast.makeText(context, Strings.completeCurrentLevelFirst(lang), Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1.3f)
                    .height(46.dp)
                    .testTag("oneline_next_level_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canGoNext) MazeAmber else MazeSurface1
                ),
                border = if (!canGoNext) BorderStroke(1.dp, MazeEdgeHighlight) else null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = Strings.nextLevel(lang),
                    color = if (canGoNext) Color.Black else MazeTextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.NavigateNext,
                    contentDescription = Strings.nextLevel(lang),
                    tint = if (canGoNext) Color.Black else MazeTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Bottom AdMob Banner
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        AdMobBannerView(
            isBannerAllowed = uiState.progress.isBannerAdAllowed(),
            modifier = Modifier.padding(top = 2.dp)
        )
    }

    // 8. WIN POPUP DIALOG
    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = Strings.winTitle(lang),
                        tint = MazeStar
                    )
                    Text(text = Strings.onelineLevelComplete(lang, selectedLevelIndex + 1), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = Strings.onelineWinMsg(lang, currentLevel.getLabel(lang)),
                        color = MazeTextBody,
                        fontSize = 14.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MazeSurface1,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = Strings.levelReward(lang), color = MazeTextMuted)
                            Text(text = "+30 🪙", fontWeight = FontWeight.Bold, color = MazeCoin)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWinDialog = false
                        if (selectedLevelIndex + 1 < ONE_LINE_LEVELS.size) {
                            loadLevelIndex(selectedLevelIndex + 1)
                        } else {
                            Toast.makeText(context, Strings.congratsAllCleared(lang), Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber)
                ) {
                    Text(text = Strings.nextLevel(lang), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showWinDialog = false
                        isHintShowing = false
                        oneLineViewRef?.resetLevel()
                    }
                ) {
                    Text(text = Strings.retry(lang), color = MazeTextBody)
                }
            },
            containerColor = MazeSurface2
        )
    }
}
