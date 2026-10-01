package com.looooorsasass.gamets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ads.AdManager
import com.example.core.i18n.AppLanguage
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.OneLineScreen
import com.example.ui.screens.ReplayScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeBgDark
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextH1
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

/**
 * MainActivity chính thức chạy toàn bộ Game Mê Cung (MazeX / Maze Game Engine):
 * 1. Khởi tạo cấu hình quảng cáo thân thiện với trẻ em & gia đình (COPPA / Age 3+ / Xếp hạng G).
 * 2. Kích hoạt giao diện hiển thị tràn viền enableEdgeToEdge() chuẩn Android 15 & 16.
 * 3. Kết nối toàn bộ 8 màn chơi hoàn chỉnh: HomeScreen, GameScreen, OneLineScreen, ShopScreen,
 *    DailyChallengeScreen, AchievementsScreen, LeaderboardScreen, ReplayScreen.
 * 4. Xử lý Predictive Back Gesture bằng BackHandler chuẩn xác.
 * 5. Lưu trữ tiến trình game tự động và bền vững qua Room Database (AppDatabase).
 */
class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Áp dụng cấu hình thân thiện với trẻ em & gia đình (COPPA / Age 3+ / Xếp hạng G)
        try {
            AdManager.getInstance(applicationContext)
        } catch (_: Exception) {}

        // Kích hoạt giao diện tràn viền chuẩn Android 15 & 16
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var showExitDialog by remember { mutableStateOf(false) }

                // Xử lý nút quay lại (Back Gesture) chuẩn Android 15/16
                BackHandler(enabled = true) {
                    when {
                        showExitDialog -> {
                            showExitDialog = false
                        }
                        uiState.currentScreen == AppScreen.HOME -> {
                            showExitDialog = true
                        }
                        else -> {
                            viewModel.navigateTo(AppScreen.HOME)
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_maze_scaffold"),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = MazeBgDark
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentScreen) {
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

                    // Hộp thoại chọn ngôn ngữ khi mới tải/mở game lần đầu
                    if (uiState.showFirstLaunchLanguageDialog) {
                        AlertDialog(
                            onDismissRequest = {},
                            containerColor = MazeSurface1,
                            title = {
                                Text(
                                    text = "🌐 Chọn Ngôn Ngữ / Select Language",
                                    color = MazeTextH1,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            text = {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Vui lòng chọn ngôn ngữ khởi đầu cho trò chơi.\nPlease choose your starting language.",
                                        color = Color.White.copy(alpha = 0.85f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.selectInitialLanguageAndStartLevel1(AppLanguage.VI) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "🇻🇳 Tiếng Việt",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.selectInitialLanguageAndStartLevel1(AppLanguage.EN) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MazeSurface2),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "🇬🇧 English",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            },
                            confirmButton = {},
                            dismissButton = {}
                        )
                    }

                    // Hộp thoại xác nhận thoát game
                    if (showExitDialog) {
                        val isVi = uiState.language == AppLanguage.VI
                        AlertDialog(
                            onDismissRequest = { showExitDialog = false },
                            containerColor = MazeSurface1,
                            title = {
                                Text(
                                    text = if (isVi) "Thoát Game?" else "Exit Game?",
                                    color = MazeTextH1,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            text = {
                                Text(
                                    text = if (isVi) "Tiến trình của bạn đã được lưu tự động. Bạn có muốn thoát không?"
                                    else "Your game progress is saved automatically. Do you want to exit?",
                                    color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = { finish() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber)
                                ) {
                                    Text(
                                        text = if (isVi) "Thoát" else "Exit",
                                        color = Color.Black
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showExitDialog = false }) {
                                    Text(
                                        text = if (isVi) "Chơi tiếp" else "Resume",
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
