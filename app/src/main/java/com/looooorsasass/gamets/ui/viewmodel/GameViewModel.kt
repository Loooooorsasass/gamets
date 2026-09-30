package com.looooorsasass.gamets.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.looooorsasass.gamets.data.model.GameStats
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Trạng thái UI chuẩn MVVM (Clean Architecture) cho Game:
 * Sử dụng Sealed Interface để biểu diễn các trạng thái bất biến Type-safe: Loading, Success, Error.
 */
sealed interface GameUiState {
    data object Loading : GameUiState

    data class Success(
        val stats: GameStats,
        val isProcessing: Boolean = false,
        val userNotification: String? = null
    ) : GameUiState

    data class Error(
        val message: String,
        val throwable: Throwable? = null,
        val canRetry: Boolean = true
    ) : GameUiState
}

/**
 * ViewModel chuẩn kiến trúc MVVM của Google Play 2026:
 * - Quản lý trạng thái an toàn qua StateFlow.
 * - Bẫy lỗi toàn diện qua CoroutineExceptionHandler và try-catch nghiêm ngặt.
 * - Tránh Crash và ANR (Application Not Responding) vượt ngưỡng 1.09% của Google Play Store.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "GameViewModel"
    }

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Bẫy lỗi cấp độ Coroutine Context để ngăn chặn crash tầng hệ thống
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "Lỗi Coroutine không xử lý: ${exception.message}", exception)
        _uiState.update {
            GameUiState.Error(
                message = "Đã xảy ra sự cố ngoài dự kiến: ${exception.localizedMessage ?: "Lỗi hệ thống"}",
                throwable = exception,
                canRetry = true
            )
        }
    }

    init {
        loadInitialGameStats()
    }

    /**
     * Nạp dữ liệu thống kê ban đầu một cách an toàn
     */
    fun loadInitialGameStats() {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _uiState.value = GameUiState.Loading
            try {
                // Giả lập đọc dữ liệu từ Local Cache / Database với IO Dispatcher
                delay(300) // Nhẹ nhàng để tránh lag frame
                val initialStats = GameStats.initial()

                withContext(Dispatchers.Main) {
                    _uiState.value = GameUiState.Success(
                        stats = initialStats,
                        userNotification = "Dữ liệu trò chơi đã sẵn sàng!"
                    )
                }
            } catch (e: CancellationException) {
                // Luôn re-throw CancellationException để tuân thủ cơ chế Coroutines
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi nạp dữ liệu thống kê trò chơi: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = GameUiState.Error(
                        message = "Không thể tải dữ liệu game. Vui lòng thử lại!",
                        throwable = e,
                        canRetry = true
                    )
                }
            }
        }
    }

    /**
     * Ghi nhận người chơi vượt qua một màn chơi an toàn
     */
    fun onLevelCompleted(
        level: Int,
        earnedScore: Long,
        earnedStars: Int,
        earnedCoins: Int,
        timeSpentSec: Int
    ) {
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            try {
                val currentState = _uiState.value
                if (currentState !is GameUiState.Success) return@launch

                val currentStats = currentState.stats
                val newHighest = maxOf(currentStats.highestClearedLevel, level)
                val newBestTime = if (currentStats.bestTimeSeconds == 0) timeSpentSec else minOf(currentStats.bestTimeSeconds, timeSpentSec)

                val updatedStats = currentStats.copy(
                    currentLevel = level + 1,
                    highestClearedLevel = newHighest,
                    totalScore = currentStats.totalScore + earnedScore,
                    totalStars = currentStats.totalStars + earnedStars,
                    coins = currentStats.coins + earnedCoins,
                    gamesPlayed = currentStats.gamesPlayed + 1,
                    gamesWon = currentStats.gamesWon + 1,
                    bestTimeSeconds = newBestTime,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )

                withContext(Dispatchers.Main) {
                    _uiState.value = GameUiState.Success(
                        stats = updatedStats,
                        userNotification = "Chúc mừng! Bạn đã hoàn thành Màn $level thành công!"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi cập nhật kết quả màn chơi: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = GameUiState.Error(
                        message = "Không thể lưu tiến trình: ${e.localizedMessage}",
                        throwable = e,
                        canRetry = true
                    )
                }
            }
        }
    }

    /**
     * Sử dụng Khiên bảo vệ an toàn
     */
    fun useShield() {
        val currentState = _uiState.value
        if (currentState is GameUiState.Success) {
            if (currentState.stats.shields > 0) {
                val updated = currentState.stats.copy(
                    shields = currentState.stats.shields - 1,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
                _uiState.value = currentState.copy(
                    stats = updated,
                    userNotification = "Đã kích hoạt Khiên Hộ Mệnh!"
                )
            } else {
                _uiState.value = currentState.copy(
                    userNotification = "Bạn đã hết Khiên Hộ Mệnh. Hãy ghé Cửa Hàng để nhận thêm!"
                )
            }
        }
    }

    /**
     * Sử dụng Chìa khóa mở khóa màn chơi an toàn
     */
    fun useKey() {
        val currentState = _uiState.value
        if (currentState is GameUiState.Success) {
            if (currentState.stats.keys > 0) {
                val updated = currentState.stats.copy(
                    keys = currentState.stats.keys - 1,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
                _uiState.value = currentState.copy(
                    stats = updated,
                    userNotification = "Đã sử dụng 1 Chìa Khóa Vạn Năng!"
                )
            } else {
                _uiState.value = currentState.copy(
                    userNotification = "Bạn đã hết Chìa Khóa. Hãy vượt màn để nhận thêm!"
                )
            }
        }
    }

    /**
     * Thu thập tiền vàng thưởng
     */
    fun addBonusCoins(amount: Int) {
        val currentState = _uiState.value
        if (currentState is GameUiState.Success) {
            val updated = currentState.stats.copy(
                coins = currentState.stats.coins + amount,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            _uiState.value = currentState.copy(
                stats = updated,
                userNotification = "+$amount Xu đã được cộng vào kho!"
            )
        }
    }

    /**
     * Tuân thủ Chính sách Google Play Store 2026:
     * Tính năng "Xóa tài khoản và dữ liệu cá nhân" (User Data Deletion Policy).
     * Xóa toàn bộ dữ liệu tiến trình, chỉ số game và hoàn nguyên về trạng thái ban đầu an toàn.
     */
    fun deleteAccountAndData(onCompleted: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _uiState.value = GameUiState.Loading
            try {
                val context = getApplication<Application>().applicationContext
                val sharedPrefs = context.getSharedPreferences("gamets_prefs", android.content.Context.MODE_PRIVATE)
                sharedPrefs.edit().clear().apply()

                val freshStats = GameStats.initial().copy(
                    coins = 0,
                    keys = 0,
                    shields = 0
                )

                delay(400)

                withContext(Dispatchers.Main) {
                    _uiState.value = GameUiState.Success(
                        stats = freshStats,
                        userNotification = "Đã xóa toàn bộ tài khoản và dữ liệu cá nhân thành công!"
                    )
                    onCompleted?.invoke()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi xóa dữ liệu tài khoản: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = GameUiState.Error(
                        message = "Không thể hoàn tất xóa dữ liệu: ${e.localizedMessage}",
                        throwable = e,
                        canRetry = true
                    )
                }
            }
        }
    }

    /**
     * Xóa thông báo người dùng sau khi đã hiển thị trên UI
     */
    fun clearUserNotification() {
        val currentState = _uiState.value
        if (currentState is GameUiState.Success && currentState.userNotification != null) {
            _uiState.value = currentState.copy(userNotification = null)
        }
    }
}
