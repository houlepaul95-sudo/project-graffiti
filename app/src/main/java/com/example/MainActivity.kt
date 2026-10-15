package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.GraffitiDatabase
import com.example.data.GraffitiRepository
import com.example.ui.GraffitiScreen
import com.example.ui.GraffitiViewModel
import com.example.ui.GraffitiViewModelFactory
import com.example.ui.theme.MyApplicationTheme

import com.revenuecat.purchases.LogHandler
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getCustomerInfoWith

class MainActivity : ComponentActivity() {

    private val viewModel: GraffitiViewModel by viewModels {
        val database = GraffitiDatabase.getDatabase(applicationContext)
        val firestoreDbId = try {
            applicationContext.getString(R.string.firestore_database_id)
        } catch (_: Exception) {
            null
        }
        val firestore = if (!firestoreDbId.isNullOrBlank()) {
            try {
                com.google.firebase.firestore.FirebaseFirestore.getInstance(firestoreDbId)
            } catch (_: Exception) {
                null
            }
        } else null
        val repository = GraffitiRepository(database.graffitiDao(), firestore)
        GraffitiViewModelFactory(repository, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasGooglePlayStore = try {
            packageManager.getPackageInfo("com.android.vending", 0) != null
        } catch (_: Exception) {
            false
        }

        // Initialize RevenueCat Purchases SDK (graceful fallback on emulators / devices without Google Play Billing)
        try {
            // Intercept and redirect internal BILLING_UNAVAILABLE errors from Log.e to Log.d on emulators
            Purchases.logHandler = object : LogHandler {
                override fun v(tag: String, msg: String) { android.util.Log.v(tag, msg) }
                override fun d(tag: String, msg: String) { android.util.Log.d(tag, msg) }
                override fun i(tag: String, msg: String) { android.util.Log.i(tag, msg) }
                override fun w(tag: String, msg: String) { android.util.Log.w(tag, msg) }
                override fun e(tag: String, msg: String, tr: Throwable?) {
                    if (msg.contains("BILLING_UNAVAILABLE", ignoreCase = true) ||
                        msg.contains("Billing service unavailable", ignoreCase = true) ||
                        msg.contains("PurchaseNotAllowedError", ignoreCase = true) ||
                        msg.contains("Billing is not available", ignoreCase = true)
                    ) {
                        android.util.Log.d("Purchases", "Notice: Google Play Billing service is not available on this device/emulator.")
                    } else {
                        android.util.Log.w("Purchases", msg, tr)
                    }
                }
            }
            Purchases.logLevel = LogLevel.WARN

            if (hasGooglePlayStore) {
                Purchases.configure(
                    PurchasesConfiguration.Builder(this, "goog_HxWDJmqOznArjjBodojfjrlwHVk").build()
                )
                if (Purchases.isConfigured) {
                    Purchases.sharedInstance.getCustomerInfoWith(
                        onError = { error ->
                            android.util.Log.d("MainActivity", "RevenueCat: Billing status info (${error.code})")
                        },
                        onSuccess = { customerInfo ->
                            val hasActiveEntitlement = customerInfo.entitlements.all.values.any { it.isActive }
                            if (hasActiveEntitlement) {
                                viewModel.subscribePro()
                            }
                        }
                    )
                }
            } else {
                android.util.Log.d("MainActivity", "Running on environment without Google Play Store; using local VIP fallback")
            }
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "RevenueCat initialization notice: ${e.message}")
        }

        val isEmulator = (android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || "google_sdk" == android.os.Build.PRODUCT
                || android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu"))

        // Initialize Google Mobile Ads (AdMob) only on real devices with Google Play Store
        if (hasGooglePlayStore && !isEmulator) {
            try {
                com.google.android.gms.ads.MobileAds.initialize(this) { status ->
                    android.util.Log.d("MainActivity", "AdMob initialized")
                }
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "AdMob initialization notice: ${e.message}")
            }
        } else {
            android.util.Log.d("MainActivity", "AdMob initialization bypassed on emulator environment")
        }

        // Block screenshots and screen recordings
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    color = Color(0xFF14141C),
                    modifier = Modifier.fillMaxSize()
                ) {
                    GraffitiScreen(viewModel = viewModel)
                }
            }
        }
    }
}
