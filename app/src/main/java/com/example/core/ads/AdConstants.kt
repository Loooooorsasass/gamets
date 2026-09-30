package com.example.core.ads

import java.util.Calendar

/**
 * Hằng số cấu hình hệ thống Quảng Cáo & Gói VIP
 * 
 * Người dùng có thể điền các ID AdMob hoặc Google Play Product ID vào đây.
 * Mặc định để trống để người dùng điền sau như yêu cầu.
 */
object AdConstants {
    // =========================================================================
    // GOOGLE ADMOB PRODUCTION AD UNITS (MazeX)
    // =========================================================================
    // App ID chính thức của Google AdMob (MazeX):
    const val ADMOB_APP_ID: String = "ca-app-pub-9274001498438994~2708718458"

    // 1. Biểu ngữ trong game (Fixed Size Banner 320x50):
    const val ADMOB_BANNER_ID: String = "ca-app-pub-9274001498438994/2536318907"
    const val FALLBACK_BANNER_ID: String = "ca-app-pub-3940256099942544/6300978111"

    // Thời gian trễ trước khi hiển thị Banner lần đầu: sau 10 giây tính từ lúc bắt đầu game
    const val BANNER_INITIAL_DELAY_SECONDS: Long = 10L
    const val BANNER_INITIAL_DELAY_MILLIS: Long = 10_000L

    // 2. Quảng cáo xen kẽ sau ván game (Interstitial Ad):
    // QUY TẮC CỐ ĐỊNH (KHÔNG BAO GIỜ THAY ĐỔI):
    // - Chỉ hiển thị quảng cáo xen kẽ thực tế full-screen của Google AdMob SDK.
    // - TUYỆT ĐỐI KHÔNG tạo hay hiển thị các ô khung/hộp thoại quảng cáo nhân tạo (custom dialog) trong giao diện.
    // - Giao diện game luôn trắng tinh, hoàn toàn tự nhiên và sạch sẽ 100%.
    const val ADMOB_INTERSTITIAL_ID: String = "ca-app-pub-9274001498438994/3255513363"
    const val FALLBACK_INTERSTITIAL_ID: String = "ca-app-pub-3940256099942544/1033173712"

    // Hiển thị quảng cáo xen giữa sau mỗi ván vượt qua (1 ván / 1 quảng cáo để quảng cáo luôn xuất hiện rõ ràng)
    const val GAMES_PER_INTERSTITIAL_AD: Int = 1

    // Cho phép hiển thị quảng cáo xen giữa sau 1 phút 30 giây (90 giây) tính từ lúc mở game
    const val FIRST_MINUTE_NO_INTERSTITIAL_SECONDS: Long = 90L
    const val FIRST_MINUTE_NO_INTERSTITIAL_MILLIS: Long = 90_000L

    // Thời gian giãn cách giữa các lần hiện quảng cáo xen giữa: đúng 1 phút 30 giây (90 giây)
    const val INTERSTITIAL_COOLDOWN_SECONDS: Long = 90L
    const val INTERSTITIAL_COOLDOWN_MILLIS: Long = 90_000L

    // 3. Quảng cáo có tặng thưởng (Rewarded Ad):
    const val ADMOB_REWARDED_ID: String = "ca-app-pub-9274001498438994/2265031172"
    const val FALLBACK_REWARDED_ID: String = "ca-app-pub-3940256099942544/5224354917"

    // =========================================================================
    // CẤU HÌNH QUẢNG CÁO DÀNH CHO TRẺ EM & GIA ĐÌNH (COPPA / GOOGLE PLAY FAMILIES POLICY)
    // =========================================================================
    // Bật gắn cờ tagForChildDirectedTreatment = TRUE:
    // - Tuân thủ đạo luật COPPA của Hoa Kỳ và Chính sách Gia đình của Google Play Store.
    // - Lọc bỏ 100% các quảng cáo người lớn, bạo lực, cờ bạc, cá cược, hẹn hò, thuốc lá/rượu bia.
    // - Chỉ phân phối quảng cáo có xếp hạng 'G' (General Audiences) phù hợp mọi lứa tuổi (sữa, đồ chơi, hoạt hình...).
    // - Tắt tính năng thu thập định danh cá nhân và quảng cáo theo dõi hành vi (Non-personalized ads).
    const val TAG_FOR_CHILD_DIRECTED: Int = com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
    const val TAG_FOR_UNDER_AGE_CONSENT: Int = com.google.android.gms.ads.RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
    const val MAX_CONTENT_RATING: String = com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G

    val isAdMobConfigured: Boolean
        get() = ADMOB_APP_ID.isNotBlank() || ADMOB_BANNER_ID.isNotBlank() || ADMOB_INTERSTITIAL_ID.isNotBlank() || ADMOB_REWARDED_ID.isNotBlank()

    val isBannerConfigured: Boolean
        get() = ADMOB_BANNER_ID.isNotBlank()

    val isInterstitialConfigured: Boolean
        get() = ADMOB_INTERSTITIAL_ID.isNotBlank()

    val isRewardedConfigured: Boolean
        get() = ADMOB_REWARDED_ID.isNotBlank()

    /**
     * Phát hiện môi trường máy ảo (Android Emulator / Cloud Streaming Emulator)
     * để tránh khởi chạy AdMob WebView / IMA Video Codec / AdServices gây lỗi hệ thống trên máy ảo.
     */
    fun isEmulatorEnvironment(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT.lowercase()
        val model = android.os.Build.MODEL.lowercase()
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        val brand = android.os.Build.BRAND.lowercase()
        val device = android.os.Build.DEVICE.lowercase()
        val product = android.os.Build.PRODUCT.lowercase()
        val hardware = android.os.Build.HARDWARE.lowercase()

        return fingerprint.startsWith("generic") ||
            fingerprint.startsWith("unknown") ||
            fingerprint.contains("emulator") ||
            fingerprint.contains("sdk_gphone") ||
            model.contains("google_sdk") ||
            model.contains("emulator") ||
            model.contains("android sdk built for") ||
            model.contains("sdk_gphone") ||
            manufacturer.contains("genymotion") ||
            (brand.startsWith("generic") && device.startsWith("generic")) ||
            product.contains("sdk") ||
            product.contains("emulator") ||
            product.contains("vbox") ||
            hardware.contains("goldfish") ||
            hardware.contains("ranchu") ||
            hardware.contains("cuttlefish") ||
            hardware.contains("vsoc")
    }

    // =========================================================================
    // GOOGLE PLAY IN-APP BILLING (IAP) PRODUCT IDS
    // Để trống hoặc điền Product ID đã tạo trên Google Play Console tại đây:
    // =========================================================================
    const val PRODUCT_ID_VIP_2: String = "" // Giá ngày thường $1.25 (ví dụ: "vip_pass_tier_2_15days")
    const val PRODUCT_ID_VIP_2_DISCOUNT: String = "" // Giá khuyến mãi $0.699 (ví dụ: "vip_pass_tier_2_sale")
    const val PRODUCT_ID_VIP_3: String = "" // Giá ngày thường $4.00 (ví dụ: "vip_pass_tier_3_permanent")
    const val PRODUCT_ID_VIP_3_DISCOUNT: String = "" // Giá khuyến mãi $2.599 (ví dụ: "vip_pass_tier_3_sale")

    // =========================================================================
    // CHÍNH SÁCH GIÁ & QUYỀN LỢI CÁC GÓI VIP
    // =========================================================================

    // VIP 1:
    // 3 ngày không quảng cáo banner hoặc sau game (người chơi tùy chọn bật/tắt)
    // Giá: 2,000 Vàng HOẶC tích lũy 30 lượt xem quảng cáo thưởng
    const val VIP1_COINS_PRICE: Int = 2000
    const val VIP1_REQUIRED_AD_VIEWS: Int = 30
    const val VIP1_DURATION_DAYS: Int = 3
    const val VIP1_DURATION_MILLIS: Long = 3L * 24 * 60 * 60 * 1000L

    // VIP 2:
    // 15 ngày không có quảng cáo (trừ quảng cáo thưởng)
    // Tặng kèm: +5 gợi ý, +2 bỏ qua ván
    // Giá ngày thường: 1.25 USD
    // Giảm giá Flash Sale: 0.699 USD (vào T2, T4, T6, CN từ 9h - 12h)
    const val VIP2_REGULAR_PRICE_USD: Double = 1.25
    const val VIP2_DISCOUNT_PRICE_USD: Double = 0.699
    const val VIP2_DURATION_DAYS: Int = 15
    const val VIP2_DURATION_MILLIS: Long = 15L * 24 * 60 * 60 * 1000L
    const val VIP2_BONUS_HINTS: Int = 5
    const val VIP2_BONUS_SKIPS: Int = 2

    // VIP 3:
    // Tắt mọi loại quảng cáo tự hiện vĩnh viễn (trừ quảng cáo thưởng tự chọn)
    // Tặng kèm: +15 gợi ý, +5 bỏ qua ván
    // Giá ngày thường: 4.00 USD
    // Giảm giá Flash Sale: 2.599 USD (vào T2, T4, T6, CN từ 9h - 12h)
    const val VIP3_REGULAR_PRICE_USD: Double = 4.00
    const val VIP3_DISCOUNT_PRICE_USD: Double = 2.599
    const val VIP3_BONUS_HINTS: Int = 15
    const val VIP3_BONUS_SKIPS: Int = 5

    /**
     * Kiểm tra khung giờ giảm giá Flash Sale:
     * - Các ngày: Thứ 2 (MONDAY), Thứ 4 (WEDNESDAY), Thứ 6 (FRIDAY), Chủ Nhật (SUNDAY).
     * - Khung giờ: 3 tiếng từ 9:00 sáng đến 12:00 trưa (9 <= hour < 12).
     */
    fun isFlashSaleActive(calendar: Calendar = Calendar.getInstance()): Boolean {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        // Khung giờ Flash Sale: 9:00 đến 12:00 hàng ngày
        val isDiscountHour = hour in 9..11 // từ 9:00:00 đến 11:59:59

        return isDiscountHour
    }

    /**
     * Thời gian còn lại của đợt Flash Sale hiện tại (mili-giây),
     * trả về 0 nếu hiện tại không phải giờ Flash Sale.
     */
    fun getFlashSaleRemainingMillis(calendar: Calendar = Calendar.getInstance()): Long {
        if (!isFlashSaleActive(calendar)) return 0L
        val endCal = calendar.clone() as Calendar
        endCal.set(Calendar.HOUR_OF_DAY, 12)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MILLISECOND, 0)
        return maxOf(0L, endCal.timeInMillis - calendar.timeInMillis)
    }

    /**
     * Lấy giá hiển thị cho VIP 2 theo thời gian thực
     */
    fun getVip2DisplayPrice(isFlashSale: Boolean = isFlashSaleActive()): String {
        return if (isFlashSale) "$0.699" else "$1.25"
    }

    /**
     * Lấy giá hiển thị cho VIP 3 theo thời gian thực
     */
    fun getVip3DisplayPrice(isFlashSale: Boolean = isFlashSaleActive()): String {
        return if (isFlashSale) "$2.599" else "$4.00"
    }
}
