package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Standard AdMob Banner component.
 * By default, uses Google's official test banner ad unit ID until a live banner ad unit ID is provided.
 * Hidden automatically if the user is a VIP Pass subscriber.
 */
@Composable
fun AdBanner(
    isProSubscribed: Boolean,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111", // Google official test banner ad unit
    modifier: Modifier = Modifier
) {
    if (isProSubscribed) return

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
