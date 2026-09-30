package com.example.core.time

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import android.util.Log
import com.example.core.ads.AdConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Trình quản lý Đồng Hồ Internet (Internet Time Clock & Tamper-proof Time Sync)
 * 
 * Đảm bảo:
 * 1. Lấy giờ chuẩn từ máy chủ internet (Google / WorldTime), chống gian lận chỉnh giờ thiết bị.
 * 2. Xác định chính xác khung giờ vàng Flash Sale (T2, T4, T6, CN từ 9:00 - 12:00).
 * 3. Hỗ trợ đếm ngược hết hạn gói VIP theo thời gian thực.
 */
class NetworkTimeManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "NetworkTimeManager"
        @Volatile
        private var instance: NetworkTimeManager? = null

        fun getInstance(context: Context): NetworkTimeManager {
            return instance ?: synchronized(this) {
                instance ?: NetworkTimeManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Lưu mốc thời gian internet đã hiệu chuẩn cùng elapsedRealtime để chống sửa giờ máy
    private var calibratedInternetTimeMillis: Long = 0L
    private var calibratedElapsedRealtimeMillis: Long = 0L
    private var isSyncedWithInternet: Boolean = false

    private val _isInternetSyncedFlow = MutableStateFlow(false)
    val isInternetSyncedFlow: StateFlow<Boolean> = _isInternetSyncedFlow.asStateFlow()

    init {
        syncWithInternetTime()
    }

    /**
     * Kiểm tra thiết bị có đang kết nối internet hay không (Wifi hoặc 4G/5G)
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Đồng bộ thời gian thực từ internet ở chế độ nền
     */
    fun syncWithInternetTime(onCompleted: ((Boolean) -> Unit)? = null) {
        scope.launch {
            val success = fetchNetworkTime()
            _isInternetSyncedFlow.value = success
            withContext(Dispatchers.Main) {
                onCompleted?.invoke(success)
            }
        }
    }

    private fun fetchNetworkTime(): Boolean {
        val endpoints = listOf(
            "https://clients3.google.com/generate_204",
            "https://www.google.com",
            "https://timeapi.io/api/time/current/zone?timeZone=UTC"
        )

        for (endpoint in endpoints) {
            try {
                val url = URL(endpoint)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.requestMethod = "HEAD"
                connection.instanceFollowRedirects = false
                connection.connect()

                val serverDate = connection.date
                val elapsed = SystemClock.elapsedRealtime()
                connection.disconnect()

                if (serverDate > 0L) {
                    calibratedInternetTimeMillis = serverDate
                    calibratedElapsedRealtimeMillis = elapsed
                    isSyncedWithInternet = true
                    Log.i(TAG, "Đã đồng bộ đồng hồ internet thành công: ${java.util.Date(serverDate)}")
                    return true
                }
            } catch (e: Exception) {
                Log.d(TAG, "Thử sync time với $endpoint thất bại: ${e.message}")
            }
        }

        // Nếu offline, tạm thời dùng System.currentTimeMillis()
        if (!isSyncedWithInternet) {
            calibratedInternetTimeMillis = System.currentTimeMillis()
            calibratedElapsedRealtimeMillis = SystemClock.elapsedRealtime()
        }
        return false
    }

    /**
     * Lấy mốc thời gian hiện tại chuẩn Internet (hoặc fallback giờ hệ thống)
     */
    fun getCurrentInternetTimeMillis(): Long {
        return if (isSyncedWithInternet && calibratedElapsedRealtimeMillis > 0L) {
            calibratedInternetTimeMillis + (SystemClock.elapsedRealtime() - calibratedElapsedRealtimeMillis)
        } else {
            System.currentTimeMillis()
        }
    }

    /**
     * Lấy đối tượng Calendar theo múi giờ thiết bị nhưng dựa trên mốc thời gian Internet đã hiệu chuẩn
     */
    fun getInternetCalendar(): Calendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = getCurrentInternetTimeMillis()
        return cal
    }

    /**
     * Kiểm tra có đang trong khung giờ vàng Flash Sale giảm giá VIP 2 & VIP 3 không:
     * - Ngày: Thứ 2 (MONDAY), Thứ 4 (WEDNESDAY), Thứ 6 (FRIDAY), Chủ Nhật (SUNDAY)
     * - Giờ: 9:00 - 12:00 trưa (9 <= hour < 12)
     */
    fun isFlashSaleActive(): Boolean {
        val calendar = getInternetCalendar()
        return AdConstants.isFlashSaleActive(calendar)
    }

    /**
     * Thời gian còn lại của đợt Flash Sale đang diễn ra (mili-giây),
     * trả về 0 nếu hiện tại không nằm trong khung giờ vàng.
     */
    fun getFlashSaleRemainingMillis(): Long {
        val calendar = getInternetCalendar()
        return AdConstants.getFlashSaleRemainingMillis(calendar)
    }

    /**
     * Tính thời gian (mili-giây) từ thời điểm hiện tại tới đợt Flash Sale tiếp theo
     */
    fun getMillisUntilNextFlashSale(): Long {
        val calendar = getInternetCalendar()
        val currentMillis = calendar.timeInMillis
        val candidate = calendar.clone() as Calendar

        // Tìm trong 7 ngày tới
        for (i in 0..7) {
            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            val isDiscountDay = dayOfWeek == Calendar.MONDAY ||
                    dayOfWeek == Calendar.WEDNESDAY ||
                    dayOfWeek == Calendar.FRIDAY ||
                    dayOfWeek == Calendar.SUNDAY

            if (isDiscountDay) {
                candidate.set(Calendar.HOUR_OF_DAY, 9)
                candidate.set(Calendar.MINUTE, 0)
                candidate.set(Calendar.SECOND, 0)
                candidate.set(Calendar.MILLISECOND, 0)

                if (candidate.timeInMillis > currentMillis) {
                    return candidate.timeInMillis - currentMillis
                }
            }
            candidate.add(Calendar.DAY_OF_MONTH, 1)
        }
        return 0L
    }

    /**
     * Định dạng đếm ngược thời gian VIP còn lại (dưới dạng: "2 ngày 14:25:38" hoặc "08:15:42")
     */
    fun formatRemainingDuration(remainingMillis: Long): String {
        if (remainingMillis <= 0L) return "00:00:00"
        if (remainingMillis == Long.MAX_VALUE) return "Vĩnh viễn (Trọn đời)"

        val totalSeconds = remainingMillis / 1000L
        val days = totalSeconds / (24 * 3600L)
        val hours = (totalSeconds % (24 * 3600L)) / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return if (days > 0) {
            String.format(Locale.getDefault(), "%d ngày %02d:%02d:%02d", days, hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}
