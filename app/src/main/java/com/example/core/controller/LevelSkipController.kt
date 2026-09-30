package com.example.core.controller

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.core.time.NetworkTimeManager
import com.example.data.local.GameProgressEntity
import com.example.data.local.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Controller duy nhất quản lý tập trung và xác thực nghiêm ngặt tính năng Bỏ qua màn chơi (Level Skip).
 * 
 * Các biện pháp an toàn và bảo mật:
 * 1. Single Controller: Mọi thao tác kiểm tra, khởi tạo, trừ lượt hoặc hoàn thành skip đều phải qua Controller này.
 * 2. Chống gian lận đồng hồ máy (Anti-Clock Tampering):
 *    - Sử dụng đồng hồ mạng hiệu chuẩn từ [NetworkTimeManager].
 *    - Lưu trữ High-Water Mark mốc thời gian lớn nhất từng ghi nhận.
 *    - Bắt buộc kiểm tra kết nối mạng và hiệu chuẩn giờ internet khi người chơi hết token và muốn xem quảng cáo.
 * 3. Thời gian hồi chiêu nghiêm ngặt: 2 ngày (48 giờ = 172,800,000 ms) giữa 2 lần xem quảng cáo skip.
 * 4. Bảo vệ đồng thời (Mutex concurrency lock) chống race condition và spam nút bấm.
 * 5. Lưu trữ đồng bộ 2 lớp: Room Database + SharedPreferences dự phòng.
 */
class LevelSkipController private constructor(
    private val context: Context,
    private val repository: GameRepository,
    private val networkTimeManager: NetworkTimeManager
) {
    companion object {
        private const val TAG = "LevelSkipController"
        const val COOLDOWN_DURATION_MILLIS: Long = 2L * 24L * 60L * 60L * 1000L // 48 giờ (2 ngày)
        
        private const val PREFS_NAME = "secure_skip_controller_prefs"
        private const val KEY_LAST_SKIP_TIMESTAMP = "last_skip_network_timestamp"
        private const val KEY_MAX_RECORDED_NETWORK_TIME = "max_recorded_network_time"
        private const val KEY_CONSECUTIVE_SKIPS = "consecutive_skips_count"

        @Volatile
        private var instance: LevelSkipController? = null

        fun getInstance(
            context: Context,
            repository: GameRepository,
            networkTimeManager: NetworkTimeManager
        ): LevelSkipController {
            return instance ?: synchronized(this) {
                instance ?: LevelSkipController(
                    context.applicationContext,
                    repository,
                    networkTimeManager
                ).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val operationMutex = Mutex()

    sealed interface SkipAvailability {
        data class AvailableWithToken(val tokensRemaining: Int) : SkipAvailability
        data object AvailableWithAd : SkipAvailability
        data class CooldownActive(val remainingMillis: Long, val formattedRemainingTime: String) : SkipAvailability
        data class RequiresInternet(val message: String) : SkipAvailability
    }

    sealed interface SkipExecutionResult {
        data class Success(val updatedProgress: GameProgressEntity, val message: String) : SkipExecutionResult
        data class CooldownNotElapsed(val remainingMillis: Long, val formattedTime: String) : SkipExecutionResult
        data class InternetRequired(val message: String) : SkipExecutionResult
        data class Error(val reason: String) : SkipExecutionResult
    }

    /**
     * Lấy mốc thời gian internet an toàn nhất hiện tại và cập nhật High-Water Mark
     */
    private fun getVerifiedCurrentTime(): Long {
        val internetTime = networkTimeManager.getCurrentInternetTimeMillis()
        val maxRecorded = prefs.getLong(KEY_MAX_RECORDED_NETWORK_TIME, 0L)
        
        // Cập nhật mốc thời gian cao nhất đã biết để chống quay ngược thời gian
        if (internetTime > maxRecorded) {
            prefs.edit().putLong(KEY_MAX_RECORDED_NETWORK_TIME, internetTime).apply()
        }
        
        return maxOf(internetTime, maxRecorded)
    }

    /**
     * Lấy mốc thời gian sử dụng lượt bỏ qua gần nhất từ cả Database và SharedPreferences dự phòng
     */
    private fun getSecureLastSkipTimestamp(progress: GameProgressEntity): Long {
        val dbTimestamp = progress.lastSkipAdTimestamp
        val prefsTimestamp = prefs.getLong(KEY_LAST_SKIP_TIMESTAMP, 0L)
        return maxOf(dbTimestamp, prefsTimestamp)
    }

    /**
     * Kiểm tra trạng thái khả dụng của tính năng Skip đối với tiến trình hiện tại
     */
    fun checkSkipAvailability(progress: GameProgressEntity): SkipAvailability {
        // 1. Ưu tiên kiểm tra token có sẵn
        if (progress.skipTokens > 0) {
            return SkipAvailability.AvailableWithToken(progress.skipTokens)
        }

        // 2. Nếu hết token, bắt buộc kiểm tra điều kiện xem quảng cáo với giờ internet
        val now = getVerifiedCurrentTime()
        val lastSkipTime = getSecureLastSkipTimestamp(progress)

        if (lastSkipTime > 0L) {
            val elapsed = now - lastSkipTime
            if (elapsed < COOLDOWN_DURATION_MILLIS) {
                val remaining = COOLDOWN_DURATION_MILLIS - elapsed
                return SkipAvailability.CooldownActive(
                    remainingMillis = remaining,
                    formattedRemainingTime = formatCooldownTime(remaining)
                )
            }
        }

        // 3. Nếu chưa kết nối mạng hoặc chưa đồng bộ được giờ internet thì nhắc người dùng
        if (!networkTimeManager.isNetworkAvailable()) {
            return SkipAvailability.RequiresInternet("Cần kết nối internet để xác thực thời gian nhận lượt bỏ qua (2 ngày/lần).")
        }

        return SkipAvailability.AvailableWithAd
    }

    /**
     * Thực thi việc sử dụng Token Skip có sẵn
     */
    suspend fun executeTokenSkip(currentProgress: GameProgressEntity): SkipExecutionResult = operationMutex.withLock {
        withContext(Dispatchers.IO) {
            if (currentProgress.skipTokens <= 0) {
                return@withContext SkipExecutionResult.Error("Không còn lượt bỏ qua có sẵn.")
            }

            val newProgress = currentProgress.copy(
                skipTokens = maxOf(0, currentProgress.skipTokens - 1)
            )

            repository.saveProgress(newProgress)
            Log.i(TAG, "Đã sử dụng 1 token bỏ qua. Số token còn lại: ${newProgress.skipTokens}")

            SkipExecutionResult.Success(
                updatedProgress = newProgress,
                message = "Đã sử dụng 1 lượt bỏ qua màn chơi!"
            )
        }
    }

    /**
     * Xác thực trước khi mở quảng cáo thưởng Skip
     */
    suspend fun validateAdSkipEligibility(currentProgress: GameProgressEntity): SkipExecutionResult = operationMutex.withLock {
        withContext(Dispatchers.IO) {
            if (currentProgress.skipTokens > 0) {
                return@withContext SkipExecutionResult.Error("Bạn đang có sẵn ${currentProgress.skipTokens} lượt bỏ qua, hãy sử dụng token!")
            }

            if (!networkTimeManager.isNetworkAvailable()) {
                return@withContext SkipExecutionResult.InternetRequired(
                    "Cần kết nối internet để xác thực thời gian hồi chiêu lượt bỏ qua (2 ngày/lần)."
                )
            }

            val now = getVerifiedCurrentTime()
            val lastSkipTime = getSecureLastSkipTimestamp(currentProgress)

            if (lastSkipTime > 0L) {
                val elapsed = now - lastSkipTime
                if (elapsed < COOLDOWN_DURATION_MILLIS) {
                    val remaining = COOLDOWN_DURATION_MILLIS - elapsed
                    val formatted = formatCooldownTime(remaining)
                    Log.w(TAG, "Từ chối xem quảng cáo Skip: Chưa hết thời gian hồi chiêu ($formatted còn lại)")
                    return@withContext SkipExecutionResult.CooldownNotElapsed(
                        remainingMillis = remaining,
                        formattedTime = formatted
                    )
                }
            }

            // Đủ điều kiện
            SkipExecutionResult.Success(
                updatedProgress = currentProgress,
                message = "Hợp lệ để xem quảng cáo thưởng bỏ qua màn!"
            )
        }
    }

    /**
     * Hoàn tất lượt xem quảng cáo thưởng Skip:
     * - Cập nhật chính xác mốc thời gian internet đã hiệu chuẩn.
     * - Lưu đồng bộ vào Database và SharedPreferences.
     * - KHÔNG cộng dồn thêm token dư thừa vào kho.
     */
    suspend fun completeAdSkip(currentProgress: GameProgressEntity): SkipExecutionResult = operationMutex.withLock {
        withContext(Dispatchers.IO) {
            val now = getVerifiedCurrentTime()
            val lastSkipTime = getSecureLastSkipTimestamp(currentProgress)

            // Kiểm tra bảo vệ lần cuối phòng trường hợp callback bị gọi gian lận
            if (lastSkipTime > 0L && (now - lastSkipTime) < (COOLDOWN_DURATION_MILLIS - 60_000L)) {
                val remaining = COOLDOWN_DURATION_MILLIS - (now - lastSkipTime)
                return@withContext SkipExecutionResult.CooldownNotElapsed(
                    remainingMillis = remaining,
                    formattedTime = formatCooldownTime(remaining)
                )
            }

            // Lưu mốc thời gian vào SharedPreferences
            prefs.edit()
                .putLong(KEY_LAST_SKIP_TIMESTAMP, now)
                .putLong(KEY_MAX_RECORDED_NETWORK_TIME, now)
                .apply()

            // Lưu mốc thời gian vào Database và cộng đúng +1 lượt Skip vào kho
            val newProgress = currentProgress.copy(
                skipTokens = currentProgress.skipTokens + 1,
                lastSkipAdTimestamp = now
            )
            repository.saveProgress(newProgress)

            Log.i(TAG, "Hoàn tất nhận thưởng +1 lượt skip qua quảng cáo. Timestamp mới: $now, skipTokens: ${newProgress.skipTokens}")

            SkipExecutionResult.Success(
                updatedProgress = newProgress,
                message = "🎉 Đã nhận thành công +1 lượt Bỏ qua màn chơi (lần tiếp theo sau 2 ngày)!"
            )
        }
    }

    /**
     * Định dạng thời gian đếm ngược thân thiện
     */
    fun formatCooldownTime(remainingMillis: Long): String {
        if (remainingMillis <= 0L) return "0 giờ"
        
        val totalHours = (remainingMillis + 3599999L) / (60L * 60L * 1000L)
        val days = totalHours / 24L
        val hours = totalHours % 24L

        return if (days > 0) {
            "${days} ngày ${hours} giờ"
        } else {
            "${totalHours} giờ"
        }
    }
}
