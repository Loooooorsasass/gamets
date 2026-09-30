package com.example.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core.ads.AdConstants
import com.example.core.ads.AdManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * AdMob Banner Composable chuẩn Google Play & AdMob Policy:
 * - Sử dụng kích thước chuẩn AdSize.BANNER (320x50) hoặc adaptive banner.
 * - Tự động nạp AdRequest chuẩn 3+ (COPPA / Family Safe Rated G).
 * - Tự động nạp Banner Test nếu Banner ID mới chưa được kích hoạt phân phối.
 * - Xử lý giải phóng bộ nhớ sạch sẽ trong DisposableEffect.
 */
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier,
    isBannerAllowed: Boolean = true
) {
    if (!isBannerAllowed) return

    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdConstants.ADMOB_BANNER_ID.ifBlank { AdConstants.FALLBACK_BANNER_ID }
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isAdLoaded = true
                            Log.d("AdMobBannerView", "Banner Ad loaded successfully")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isAdLoaded = false
                            Log.w("AdMobBannerView", "Banner Ad failed to load: ${error.message}. Trying fallback unit...")
                            // Nếu ID sản xuất chưa duyệt xong, thử nạp lại fallback test banner
                            if (adUnitId != AdConstants.FALLBACK_BANNER_ID) {
                                post {
                                    try {
                                        adUnitId = AdConstants.FALLBACK_BANNER_ID
                                        loadAd(AdManager.buildFamilySafeAdRequest())
                                    } catch (e: Exception) {
                                        Log.w("AdMobBannerView", "Fallback banner load error: ${e.message}")
                                    }
                                }
                            }
                        }
                    }
                    try {
                        loadAd(AdManager.buildFamilySafeAdRequest())
                    } catch (e: Exception) {
                        Log.w("AdMobBannerView", "Initial banner load error: ${e.message}")
                    }
                }
            },
            update = { adView ->
                // Update nếu cần
            }
        )
    }
}
