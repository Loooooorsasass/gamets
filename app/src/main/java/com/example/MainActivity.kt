package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.core.notifications.FlashSaleAlarmReceiver
import com.example.core.notifications.FlashSaleNotificationManager
import com.example.ui.components.CloudSyncDialog
import com.example.ui.components.WolfEquipmentDialog
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.OneLineScreen
import com.example.ui.screens.ReplayScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.theme.MazeAccent
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextMuted
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            FlashSaleAlarmReceiver.scheduleNextAlarm(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())

        // Khởi tạo Google Mobile Ads SDK (MazeX App ID: ca-app-pub-9274001498438994~2708718458) tuân thủ chuẩn 3+ (Rated G)
        com.example.core.ads.AdManager.getInstance(this).initializeSdkIfNeeded()

        // Khởi tạo thông báo và lịch báo thức Giờ Vàng
        FlashSaleNotificationManager.initNotificationChannel(this)
        FlashSaleAlarmReceiver.scheduleNextAlarm(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (intent?.getStringExtra("NAVIGATE_TO") == "shop") {
            viewModel.navigateTo(AppScreen.SHOP)
        }

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val lang = uiState.language
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.toastMessage) {
                    val msg = uiState.toastMessage
                    if (msg != null) {
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearToast()
                    }
                }

                // Gọi quảng cáo xen kẽ Interstitial mỗi khi thắng màn (nếu được phép)
                LaunchedEffect(uiState.pendingInterstitial) {
                    if (uiState.pendingInterstitial) {
                        viewModel.triggerInterstitial(this@MainActivity)
                    }
                }

                LaunchedEffect(lang) {
                    FlashSaleNotificationManager.saveLanguage(this@MainActivity, lang.code)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0.dp),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Smooth animated screen transitions
                        AnimatedContent(
                            targetState = uiState.currentScreen,
                            transitionSpec = {
                                if (targetState == AppScreen.PLAYING || targetState == AppScreen.REPLAY) {
                                    (slideInHorizontally(animationSpec = tween(350)) { it } + fadeIn(tween(350)))
                                        .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeOut(tween(300)))
                                } else {
                                    (fadeIn(animationSpec = tween(300)) + slideInHorizontally(tween(300)) { -it / 4 })
                                        .togetherWith(fadeOut(animationSpec = tween(250)))
                                }
                            },
                            label = "ScreenTransition",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) { targetScreen ->
                            when (targetScreen) {
                                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                                AppScreen.PLAYING -> GameScreen(viewModel = viewModel)
                                AppScreen.REPLAY -> ReplayScreen(viewModel = viewModel)
                                AppScreen.LEADERBOARD -> LeaderboardScreen(viewModel = viewModel)
                                AppScreen.ACHIEVEMENTS -> AchievementsScreen(viewModel = viewModel)
                                AppScreen.DAILY_CHALLENGE -> DailyChallengeScreen(viewModel = viewModel)
                                AppScreen.SHOP -> ShopScreen(viewModel = viewModel)
                                AppScreen.ONELINE_GAME -> OneLineScreen(viewModel = viewModel)
                            }
                        }

                        // Màn hình chọn ngôn ngữ ngay khi vừa tải và mở game lần đầu -> Bấm chọn là vào thẳng Màn 1 Mê Cung
                        if (uiState.showFirstLaunchLanguageDialog) {
                            FirstLaunchLanguageOverlay(
                                initialLanguage = lang,
                                onLanguagePreview = { selected -> viewModel.setLanguage(selected) },
                                onConfirmLanguage = { selected ->
                                    viewModel.selectInitialLanguageAndStartLevel1(selected)
                                }
                            )
                        }
                    }

                    // Giao diện Mở Khóa & Trang Bị Vật Phẩm Hỗ Trợ Màn Sói Đuổi
                    if (uiState.showWolfEquipmentDialog) {
                        WolfEquipmentDialog(
                            progress = uiState.progress,
                            language = lang,
                            onEquip = { gearId -> viewModel.equipEquipment(gearId) },
                            onUnlock = { gearId -> viewModel.buyEquipment(gearId) },
                            onBuyShield = { viewModel.buyShieldPack(useKeys = false) },
                            onDismiss = { viewModel.closeWolfEquipmentDialog() }
                        )
                    }

                    // Cloud Sync Dialog
                    if (uiState.showCloudSyncDialog) {
                        CloudSyncDialog(
                            lastSyncTime = uiState.progress.lastCloudSyncTime,
                            totalStars = uiState.progress.totalStars,
                            highestCleared = uiState.progress.highestCleared,
                            language = lang,
                            onSyncConfirmed = { viewModel.confirmCloudSync() },
                            onDismiss = { viewModel.closeCloudSync() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        viewModel.soundManager.resumeBgm()
        val p = viewModel.uiState.value.progress
        FlashSaleNotificationManager.saveLanguage(this, p.language)
        FlashSaleNotificationManager.checkAndNotifyIfOnline(this, p.accumulatedRewardedAdViews, p.vipTier, p.language)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }

    override fun onPause() {
        super.onPause()
        viewModel.soundManager.pauseBgm()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.soundManager.release()
    }
}

@Composable
private fun FirstLaunchLanguageOverlay(
    initialLanguage: AppLanguage,
    onLanguagePreview: (AppLanguage) -> Unit,
    onConfirmLanguage: (AppLanguage) -> Unit
) {
    var selectedLang by remember(initialLanguage) { mutableStateOf(initialLanguage) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6FFFFFF))
            .clickable(enabled = false) {}
            .systemBarsPadding()
            .padding(horizontal = 22.dp, vertical = 24.dp)
            .testTag("first_launch_language_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
            border = BorderStroke(1.5.dp, MazeAccent.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MazeAccent.copy(alpha = 0.22f),
                                    Color(0xFF38BDF8).copy(alpha = 0.14f)
                                )
                            )
                        )
                        .border(1.dp, MazeAccent.copy(alpha = 0.55f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Choose Language",
                        tint = MazeAccent,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CHỌN NGÔN NGỮ • SELECT LANGUAGE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (selectedLang == AppLanguage.VI) {
                        "Chọn ngôn ngữ game của bạn để mở ngay Màn 1 Mê Cung\n(Màn 1 không tính thời gian)"
                    } else {
                        "Select your game language to start Maze Level 1 immediately\n(No timer on Level 1)"
                    },
                    fontSize = 12.5.sp,
                    color = MazeTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppLanguage.entries.forEach { langOption ->
                        val isSelected = langOption == selectedLang
                        Surface(
                            onClick = {
                                selectedLang = langOption
                                onLanguagePreview(langOption)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MazeAccent.copy(alpha = 0.20f) else MazeSurface2,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MazeAccent else MazeEdgeHighlight
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp)
                                .testTag("first_lang_option_${langOption.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = langOption.flag,
                                        fontSize = 26.sp
                                    )
                                    Column {
                                        Text(
                                            text = langOption.displayName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = if (langOption == AppLanguage.VI) "Giao diện Tiếng Việt đầy đủ" else "Full English Interface",
                                            fontSize = 11.5.sp,
                                            color = if (isSelected) MazeAccent else MazeTextMuted
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MazeAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = { onConfirmLanguage(selectedLang) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MazeAccent,
                        contentColor = Color(0xFF06101E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("confirm_first_language_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedLang == AppLanguage.VI) "VÀO CHƠI MÀN 1 NGAY" else "PLAY LEVEL 1 NOW",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
