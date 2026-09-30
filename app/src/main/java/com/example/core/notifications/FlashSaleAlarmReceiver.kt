package com.example.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.core.time.NetworkTimeManager

/**
 * BroadcastReceiver nhận tín hiệu Alarm báo thức định kỳ vào 9:00 AM các ngày Flash Sale
 * (Chủ Nhật, Thứ 2, Thứ 4, Thứ 6).
 * Khi chuông reo, kiểm tra mạng internet và bắn thông báo nếu đang có mạng.
 */
class FlashSaleAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FlashSaleAlarmReceiver"
        private const val REQUEST_CODE = 8888

        /**
         * Lên lịch báo thức kế tiếp cho đợt Flash Sale vào 9:00 AM các ngày quy định
         */
        fun scheduleNextAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val timeManager = NetworkTimeManager.getInstance(context)
            val millisUntilNext = timeManager.getMillisUntilNextFlashSale()
            if (millisUntilNext <= 0L) return

            val triggerAtMillis = System.currentTimeMillis() + millisUntilNext

            val intent = Intent(context, FlashSaleAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
                Log.d(TAG, "Đã lên lịch báo thức Flash Sale sau ${millisUntilNext / 1000L / 60L} phút")
            } catch (e: Exception) {
                Log.w(TAG, "Không thể đặt alarm: ${e.message}")
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG, "Nhận tín hiệu kiểm tra Flash Sale định kỳ")
        val timeManager = NetworkTimeManager.getInstance(context)

        // Chỉ gửi thông báo nếu người dùng đang bật mạng
        if (timeManager.isNetworkAvailable()) {
            timeManager.syncWithInternetTime {
                if (timeManager.isFlashSaleActive()) {
                    FlashSaleNotificationManager.showFlashSaleNotification(context)
                }
            }
        } else {
            Log.d(TAG, "Máy chưa bật mạng tại thời điểm báo thức, bỏ qua thông báo.")
        }

        // Tự động lên lịch cho đợt tiếp theo
        scheduleNextAlarm(context)
    }
}
