package com.example.core.ads

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Trình quản lý Quảng Cáo AdMob (Banner, Interstitial & Rewarded)
 * Tích hợp chuẩn Google Mobile Ads SDK cho ứng dụng MazeX:
 * - App ID: ca-app-pub-9274001498438994~2708718458
 * - Banner ID: ca-app-pub-9274001498438994/2536318907
 * - Interstitial ID: ca-app-pub-9274001498438994/3255513363
 * - Rewarded ID: ca-app-pub-9274001498438994/2265031172
 * - Tuân thủ nghiêm ngặt Chính sách AdMob & Google Play Families cho lứa tuổi 3+ (MAX_AD_CONTENT_RATING_G, COPPA, TFUA, NPA).
 */
class AdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isAdMobInitialized = false

    // Thời điểm mở game lần đầu trong phiên
    val appStartTimeMillis: Long = System.currentTimeMillis()

    // Thời điểm lần cuối cùng hiển thị quảng cáo xen kẽ sau ván (mili-giây)
    @Volatile
    private var lastInterstitialShowTimeMillis: Long = 0L

    companion object {
        private const val TAG = "AdManager"
        val appLaunchTimeMillis: Long = System.currentTimeMillis()
        @Volatile
        private var instance: AdManager? = null

        fun getInstance(context: Context): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager(context.applicationContext).also { instance = it }
            }
        }

        /**
         * Áp dụng cấu hình nội dung quảng cáo chuẩn 3+ (Rated G - General Audiences & COPPA)
         * trước khi khởi tạo SDK hoặc tải bất kỳ đơn vị quảng cáo nào.
         */
        fun applyFamilySafeConfiguration() {
            try {
                val requestConfiguration = RequestConfiguration.Builder()
                    .setMaxAdContentRating(AdConstants.MAX_CONTENT_RATING)
                    .setTagForChildDirectedTreatment(AdConstants.TAG_FOR_CHILD_DIRECTED)
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
                MobileAds.setRequestConfiguration(requestConfiguration)
            } catch (e: Exception) {
                Log.w(TAG, "Không thể thiết lập RequestConfiguration 3+: ${e.message}")
            }
        }

        /**
         * Tạo AdRequest chuẩn 3+ (Xếp hạng tối đa G) dùng chung cho cả Banner, Interstitial và Rewarded.
         */
        fun buildFamilySafeAdRequest(): AdRequest {
            val extras = Bundle().apply {
                putString("max_ad_content_rating", RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            }
            return AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
        }

        /**
         * Phát hiện môi trường máy ảo Cloud Streaming Emulator không có phần cứng MediaCodec video.
         * Đã loại bỏ kiểm tra /dev/dri/renderD128 để đảm bảo 100% điện thoại Android thật đều chạy SDK AdMob thật.
         */
        fun isEmulatorEnvironment(): Boolean {
            val hardware = Build.HARDWARE.lowercase()
            val model = Build.MODEL.lowercase()
            val product = Build.PRODUCT.lowercase()
            val fingerprint = Build.FINGERPRINT.lowercase()
            return (hardware.contains("goldfish")
                    || hardware.contains("ranchu")
                    || hardware.contains("cutf_cvm")
                    || hardware.contains("vsoc")
                    || model.contains("sdk_gphone")
                    || model.contains("google_sdk")
                    || model.contains("android sdk built for")
                    || product.contains("sdk_gphone")
                    || product.contains("emulator")
                    || fingerprint.startsWith("generic"))
        }
    }

    init {
        initializeSdkIfNeeded()
    }

    fun initializeSdkIfNeeded() {
        if (isAdMobInitialized) return
        try {
            applyFamilySafeConfiguration()
            MobileAds.initialize(context) { status ->
                isAdMobInitialized = true
                Log.d(TAG, "AdMob Initialized thành công (MazeX App ID: ${AdConstants.ADMOB_APP_ID}): $status")
                loadInterstitialAd()
                loadRewardedAd()
            }
        } catch (e: Exception) {
            Log.w(TAG, "AdMob initialization warning: ${e.message}")
        }
    }

    fun isInterstitialReady(): Boolean = interstitialAd != null

    fun isRewardedReady(): Boolean = rewardedAd != null

    /**
     * Tải trước quảng cáo xen giữa (Interstitial Ad: AdConstants.ADMOB_INTERSTITIAL_ID = ca-app-pub-9274001498438994/3255513363)
     * Nếu đơn vị quảng cáo mới tạo chưa có fill từ máy chủ, tự động nạp đơn vị dự phòng để đảm bảo quảng cáo luôn xuất hiện.
     */
    fun loadInterstitialAd(useFallbackUnit: Boolean = false) {
        if (!AdConstants.isInterstitialConfigured) {
            interstitialAd = null
            return
        }

        val adUnitId = if (useFallbackUnit) {
            AdConstants.FALLBACK_INTERSTITIAL_ID
        } else {
            AdConstants.ADMOB_INTERSTITIAL_ID.trim()
        }
        try {
            applyFamilySafeConfiguration()
            val adRequest = buildFamilySafeAdRequest()
            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        Log.d(TAG, "Interstitial Ad đã tải thành công ($adUnitId)")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        Log.d(TAG, "Interstitial Ad chưa sẵn sàng ($adUnitId): ${loadAdError.message}")
                        if (!useFallbackUnit) {
                            loadInterstitialAd(useFallbackUnit = true)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Exception nạp Interstitial Ad: ${e.message}")
            interstitialAd = null
        }
    }

    /**
     * Kiểm tra xem còn trong thời gian đầu mở game hay không (1 phút 30 giây đầu không có quảng cáo xen giữa)
     */
    fun isInFirstMinuteNoAdPeriod(now: Long = System.currentTimeMillis()): Boolean {
        return (now - appStartTimeMillis) < AdConstants.FIRST_MINUTE_NO_INTERSTITIAL_MILLIS
    }

    /**
     * Kiểm tra xem thời gian giãn cách giữa các lần hiện quảng cáo đã trôi qua chưa.
     */
    fun canShowInterstitialWithCooldown(): Boolean {
        val now = System.currentTimeMillis()
        if (isInFirstMinuteNoAdPeriod(now)) {
            return false
        }
        if (lastInterstitialShowTimeMillis > 0L) {
            val elapsed = now - lastInterstitialShowTimeMillis
            if (elapsed < AdConstants.INTERSTITIAL_COOLDOWN_MILLIS) {
                return false
            }
        }
        return true
    }

    /**
     * Thời gian còn lại của Cooldown (giây), trả về 0 nếu đã sẵn sàng
     */
    fun getInterstitialRemainingCooldownSeconds(): Long {
        val now = System.currentTimeMillis()
        val firstMinDiff = AdConstants.FIRST_MINUTE_NO_INTERSTITIAL_MILLIS - (now - appStartTimeMillis)
        val cooldownDiff = if (lastInterstitialShowTimeMillis > 0L) {
            AdConstants.INTERSTITIAL_COOLDOWN_MILLIS - (now - lastInterstitialShowTimeMillis)
        } else {
            0L
        }
        val maxDiff = maxOf(firstMinDiff, cooldownDiff)
        return if (maxDiff > 0) (maxDiff + 999L) / 1000L else 0L
    }

    /**
     * Hiển thị quảng cáo xen kẽ Interstitial (khi thắng màn chơi).
     * @param activity Activity đang hiển thị
     * @param onDismissed Callback khi quảng cáo đóng lại hoặc được bỏ qua
     */
    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit): Boolean {
        // Kiểm tra điều kiện thời gian đầu mở game và cooldown
        if (!canShowInterstitialWithCooldown()) {
            val remainingSec = getInterstitialRemainingCooldownSeconds()
            Log.d(TAG, "Bỏ qua quảng cáo xen giữa: Đang trong thời gian bảo vệ người chơi (còn ${remainingSec}s)")
            onDismissed()
            return false
        }

        val currentAd = interstitialAd
        if (currentAd != null) {
            try {
                currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        lastInterstitialShowTimeMillis = System.currentTimeMillis()
                        interstitialAd = null
                        loadInterstitialAd()
                        onDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.w(TAG, "Lỗi hiển thị Interstitial Ad: ${adError.message}")
                        lastInterstitialShowTimeMillis = System.currentTimeMillis()
                        interstitialAd = null
                        loadInterstitialAd()
                        onDismissed()
                    }
                }
                currentAd.show(activity)
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi show Interstitial Ad: ${e.message}")
                lastInterstitialShowTimeMillis = System.currentTimeMillis()
                interstitialAd = null
                loadInterstitialAd()
                onDismissed()
                return false
            }
        } else {
            // Chưa có quảng cáo nạp sẵn -> kích hoạt nạp lại ngay lập tức
            loadInterstitialAd()
            onDismissed()
            return false
        }
    }

    /**
     * Tải trước quảng cáo nhận thưởng (Rewarded Ad: AdConstants.ADMOB_REWARDED_ID = ca-app-pub-9274001498438994/2265031172)
     */
    fun loadRewardedAd(useFallbackUnit: Boolean = false) {
        if (!AdConstants.isRewardedConfigured) {
            rewardedAd = null
            return
        }

        val adUnitId = if (useFallbackUnit) {
            AdConstants.FALLBACK_REWARDED_ID
        } else {
            AdConstants.ADMOB_REWARDED_ID.trim()
        }
        try {
            applyFamilySafeConfiguration()
            val adRequest = buildFamilySafeAdRequest()
            RewardedAd.load(
                context,
                adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        Log.d(TAG, "Rewarded Ad đã tải thành công ($adUnitId)")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        Log.d(TAG, "Rewarded Ad chưa sẵn sàng ($adUnitId): ${loadAdError.message}")
                        if (!useFallbackUnit) {
                            loadRewardedAd(useFallbackUnit = true)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Exception nạp Rewarded Ad: ${e.message}")
            rewardedAd = null
        }
    }

    /**
     * Hiển thị quảng cáo nhận thưởng thực tế từ Google AdMob (không hiển thị bảng quảng cáo mô phỏng)
     * @param activity Activity hiển thị
     * @param onRewardEarned Gọi khi người chơi xem xong và nhận thưởng
     * @param onDismissed Gọi khi đóng quảng cáo
     * @return true nếu hiển thị được AdMob thật, false nếu quảng cáo thật chưa tải xong
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissed: () -> Unit
    ): Boolean {
        val currentAd = rewardedAd
        if (currentAd != null) {
            try {
                var earned = false
                currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        rewardedAd = null
                        loadRewardedAd()
                        if (earned) {
                            onRewardEarned()
                        }
                        onDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.w(TAG, "Lỗi hiển thị Rewarded Ad: ${adError.message}")
                        rewardedAd = null
                        loadRewardedAd()
                        onDismissed()
                    }
                }

                currentAd.show(activity) { _ ->
                    earned = true
                }
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi runtime khi show Rewarded Ad: ${e.message}")
                rewardedAd = null
                loadRewardedAd()
                onDismissed()
                return false
            }
        } else {
            // Không có ad thật sẵn sàng -> chỉ tải lại ad thật, tuyệt đối không hiện dialog mô phỏng
            loadRewardedAd()
            onDismissed()
            return false
        }
    }
}
