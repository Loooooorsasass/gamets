package com.example.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.looooorsasass.gamets.MainActivity
import com.example.core.time.NetworkTimeManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Trình quản lý Thông Báo Flash Sale & Quà Tặng Miễn Phí Song Ngữ (Việt / Anh)
 * Gửi thông báo đúng theo ngôn ngữ người chơi đã lựa chọn trong ứng dụng.
 */
object FlashSaleNotificationManager {

    private const val TAG = "FlashSaleNotification"
    const val CHANNEL_ID = "flash_sale_vip_channel"
    private const val PREFS_NAME = "vip_notification_prefs"
    private const val KEY_LAST_FLASH_NOTIF_DATE = "last_flash_notif_date"
    private const val KEY_LAST_FREE_VIP_NOTIF_TIME = "last_free_vip_notif_time"
    private const val KEY_SAVED_LANGUAGE = "saved_player_language"

    const val NOTIF_ID_FLASH_SALE = 1001
    const val NOTIF_ID_FREE_VIP = 1002
    const val NOTIF_ID_DAILY_REWARD = 1003

    fun saveLanguage(context: Context, langCode: String) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_SAVED_LANGUAGE, if (langCode == "en") "en" else "vi").apply()
        } catch (e: Exception) {
            Log.w(TAG, "Lỗi lưu ngôn ngữ thông báo: ${e.message}")
        }
    }

    fun getSelectedLanguage(context: Context): String {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_SAVED_LANGUAGE, "vi") ?: "vi"
        } catch (e: Exception) {
            "vi"
        }
    }

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val isEn = getSelectedLanguage(context) == "en"
            val name = if (isEn) "VIP Flash Sale & Free Rewards" else "Khuyến Mãi VIP & Quà Tặng Miễn Phí"
            val descriptionText = if (isEn) "Notifications for VIP discounts and free VIP packs" else "Thông báo giờ vàng giảm giá gói VIP và thông báo nhận gói VIP 1 miễn phí"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Kiểm tra trạng thái mạng và đồng hồ Internet để bắn thông báo Giờ Vàng Flash Sale hoặc Gói Miễn Phí
     */
    fun checkAndNotifyIfOnline(
        context: Context,
        accumulatedAdViews: Int,
        vipTier: Int,
        langCode: String? = null
    ) {
        if (langCode != null) {
            saveLanguage(context, langCode)
        }
        val timeManager = NetworkTimeManager.getInstance(context)
        if (!timeManager.isNetworkAvailable()) {
            Log.d(TAG, "Thiết bị chưa có mạng, bỏ qua kiểm tra thông báo internet.")
            return
        }

        timeManager.syncWithInternetTime { success ->
            val calendar = timeManager.getInternetCalendar()
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // 1. Kiểm tra Giờ Vàng Flash Sale (9h - 12h các ngày Sun, Mon, Wed, Fri)
            if (timeManager.isFlashSaleActive()) {
                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val todayStr = dateFormat.format(Date(calendar.timeInMillis))
                val lastNotifDate = prefs.getString(KEY_LAST_FLASH_NOTIF_DATE, "")

                if (lastNotifDate != todayStr) {
                    showFlashSaleNotification(context, langCode)
                    prefs.edit().putString(KEY_LAST_FLASH_NOTIF_DATE, todayStr).apply()
                }
            }

            // 2. Kiểm tra Gói VIP 1 Miễn Phí (Đủ 30 lượt xem quảng cáo thưởng)
            if (accumulatedAdViews >= 30 && vipTier == 0) {
                val lastFreeNotifTime = prefs.getLong(KEY_LAST_FREE_VIP_NOTIF_TIME, 0L)
                val now = System.currentTimeMillis()
                // Báo cách nhau tối thiểu 12 tiếng để không làm phiền
                if (now - lastFreeNotifTime > 12 * 3600 * 1000L) {
                    showFreeVipReadyNotification(context, langCode)
                    prefs.edit().putLong(KEY_LAST_FREE_VIP_NOTIF_TIME, now).apply()
                }
            }
        }
    }

    /**
     * Bắn thông báo Giờ Vàng Flash Sale (Song ngữ theo lựa chọn của người chơi)
     */
    fun showFlashSaleNotification(context: Context, langCode: String? = null) {
        try {
            initNotificationChannel(context)
            val lang = langCode ?: getSelectedLanguage(context)
            val isEn = lang == "en"

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("NAVIGATE_TO", "shop")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (isEn) {
                "⚡ FLASH SALE GOLDEN HOURS STARTED!"
            } else {
                "⚡ GIỜ VÀNG FLASH SALE ĐÃ BẮT ĐẦU!"
            }

            val shortText = if (isEn) {
                "VIP 2 only $0.699, VIP 3 only $2.599! 3-Hour Flash Discount!"
            } else {
                "VIP 2 chỉ còn $0.699, VIP 3 chỉ còn $2.599! Giảm giá sốc trong 3 giờ hôm nay!"
            }

            val bigText = if (isEn) {
                "⚡ FLASH SALE GOLDEN HOURS STARTED!\n" +
                        "VIP 2 (15 Days No-Ads) only $0.699 (Original $1.25).\n" +
                        "VIP 3 (Lifetime No-Ads) only $2.599 (Original $4.00)!\n" +
                        "Golden Hours: 9:00 AM – 12:00 PM. Tap to open Shop & claim offer now!"
            } else {
                "⚡ GIỜ VÀNG FLASH SALE ĐÃ BẮT ĐẦU!\n" +
                        "Gói VIP 2 (15 ngày không quảng cáo) chỉ còn $0.699 (giá gốc $1.25).\n" +
                        "Gói VIP 3 (Trọn đời không quảng cáo) chỉ còn $2.599 (giá gốc $4.00)!\n" +
                        "Khung giờ vàng từ 9:00 - 12:00. Bấm để vào Cửa Hàng nhận ưu đãi ngay!"
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle(title)
                .setContentText(shortText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIF_ID_FLASH_SALE, builder.build())
            Log.i(TAG, "Đã gửi thông báo Flash Sale thành công (Ngôn ngữ: $lang)")
        } catch (e: SecurityException) {
            Log.w(TAG, "Chưa được cấp quyền POST_NOTIFICATIONS: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi gửi thông báo Flash Sale: ${e.message}")
        }
    }

    /**
     * Bắn thông báo Gói VIP 1 Miễn Phí đã sẵn sàng kích hoạt (Song ngữ theo lựa chọn của người chơi)
     */
    fun showFreeVipReadyNotification(context: Context, langCode: String? = null) {
        try {
            initNotificationChannel(context)
            val lang = langCode ?: getSelectedLanguage(context)
            val isEn = lang == "en"

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("NAVIGATE_TO", "shop")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (isEn) {
                "🎁 FREE VIP 1 PACK READY TO CLAIM!"
            } else {
                "🎁 BẠN CÓ GÓI VIP 1 MIỄN PHÍ!"
            }

            val shortText = if (isEn) {
                "Accumulated 30 video views! Claim 3 days of VIP 1 No-Ads now."
            } else {
                "Đã tích lũy đủ 30 lượt xem video! Vào nhận ngay 3 ngày VIP 1 không quảng cáo."
            }

            val bigText = if (isEn) {
                "🎉 Congratulations! You have watched 30/30 rewarded ads.\n" +
                        "Your Free VIP 1 Gift Pack (3 days No-Ads) is ready.\n" +
                        "Tap to open the app and activate VIP 1 now!"
            } else {
                "🎉 Xin chúc mừng! Bạn đã tích lũy đủ 30/30 lượt xem quảng cáo thưởng.\n" +
                        "Gói quà VIP 1 miễn phí (3 ngày không có quảng cáo) đã sẵn sàng.\n" +
                        "Bấm để kích hoạt và tùy chọn tắt quảng cáo ngay!"
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.btn_star_big_on)
                .setContentTitle(title)
                .setContentText(shortText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIF_ID_FREE_VIP, builder.build())
            Log.i(TAG, "Đã gửi thông báo Gói VIP 1 Miễn Phí thành công (Ngôn ngữ: $lang)")
        } catch (e: SecurityException) {
            Log.w(TAG, "Chưa được cấp quyền POST_NOTIFICATIONS: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi gửi thông báo Free VIP: ${e.message}")
        }
    }
}
