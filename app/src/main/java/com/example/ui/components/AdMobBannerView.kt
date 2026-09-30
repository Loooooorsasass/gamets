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
 * - Kích thước chuẩn AdSize.BANNER (320x50).
 * - Nạp AdRequest chuẩn 3+ (COPPA / Family Safe Rated G).
 * - Quản lý vòng đời chặt chẽ với DisposableEffect để giải phóng tài nguyên WebView.
 */
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier,
    isBannerAllowed: Boolean = true
) {
    if (!isBannerAllowed) return

    var currentAdView by remember { mutableStateOf<AdView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                currentAdView?.destroy()
            } catch (e: Exception) {
                Log.w("AdMobBannerView", "Error destroying AdView: ${e.message}")
            }
            currentAdView = null
        }
    }

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
                            Log.d("AdMobBannerView", "Banner Ad loaded successfully")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w("AdMobBannerView", "Banner Ad failed to load: ${error.message}")
                        }
                    }
                    try {
                        loadAd(AdManager.buildFamilySafeAdRequest())
                    } catch (e: Exception) {
                        Log.w("AdMobBannerView", "Initial banner load error: ${e.message}")
                    }
                    currentAdView = this
                }
            }
        )
    }
}
