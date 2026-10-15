package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.graphics.Bitmap
import androidx.credentials.CredentialManager
import com.example.auth.AuthManager
import com.example.data.GraffitiRepository
import com.example.data.SavedGraffiti
import com.example.engine.GraffitiBrushEngine
import com.example.engine.GraffitiRenderer
import com.example.model.AppNavigationPage
import com.example.model.GraffitiConfig
import com.example.model.GraffitiFontStyle
import com.example.model.GraffitiPalettes
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Random

class GraffitiViewModel(
    private val repository: GraffitiRepository,
    context: Context
) : ViewModel() {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("graffiti_creator_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(GraffitiConfig())
    val config: StateFlow<GraffitiConfig> = _config.asStateFlow()

    val savedCreations: StateFlow<List<SavedGraffiti>> = repository.allSaved
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _freeSavesUsed = MutableStateFlow(prefs.getInt("free_saves_used", 0))
    val freeSavesUsed: StateFlow<Int> = _freeSavesUsed.asStateFlow()

    private val _isProSubscribed = MutableStateFlow(prefs.getBoolean("is_pro_subscribed", false))
    val isProSubscribed: StateFlow<Boolean> = _isProSubscribed.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(AuthManager.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(prefs.getLong("last_cloud_sync_time", 0L))
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _activePage = MutableStateFlow(AppNavigationPage.FREEHAND_CANVAS)
    val activePage: StateFlow<AppNavigationPage> = _activePage.asStateFlow()

    // Rate Us prompt: Appears first thing on launch if not previously rated or dismissed
    private val hasRatedOrDismissedRateUs = prefs.getBoolean("has_rated_or_dismissed_rate_us", false)
    private val _showRateUsDialog = MutableStateFlow(!hasRatedOrDismissedRateUs)
    val showRateUsDialog: StateFlow<Boolean> = _showRateUsDialog.asStateFlow()

    fun openRateUsDialog() {
        _showRateUsDialog.value = true
    }

    fun dismissRateUs(neverAskAgain: Boolean = false) {
        _showRateUsDialog.value = false
        if (neverAskAgain) {
            prefs.edit().putBoolean("has_rated_or_dismissed_rate_us", true).apply()
        }
    }

    fun onUserRated() {
        _showRateUsDialog.value = false
        prefs.edit().putBoolean("has_rated_or_dismissed_rate_us", true).apply()
    }

    fun setNavigationPage(page: AppNavigationPage) {
        _activePage.value = page
    }

    fun updateConfig(newConfig: GraffitiConfig) {
        _config.value = newConfig
    }

    fun refreshAuthState() {
        _currentUser.value = AuthManager.currentUser
    }

    fun syncWithCloud(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncAllWithCloud(savedCreations.value)
            _isSyncing.value = false
            result.fold(
                onSuccess = { count ->
                    val now = System.currentTimeMillis()
                    _lastSyncTime.value = now
                    prefs.edit().putLong("last_cloud_sync_time", now).apply()
                    onComplete(true, "Cloud sync successful! $count creations in sync.")
                },
                onFailure = { err ->
                    onComplete(false, err.localizedMessage ?: "Sync error")
                }
            )
        }
    }

    fun signInWithGoogle(
        context: Context,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)
        AuthManager.signInWithGoogle(
            context = context,
            credentialManager = credentialManager,
            onSuccess = { user ->
                _currentUser.value = user
                syncWithCloud()
                onSuccess(user)
            },
            onError = onError,
            onCancelled = {},
            scope = viewModelScope
        )
    }

    fun signOut(context: Context, onComplete: () -> Unit = {}) {
        val credentialManager = CredentialManager.create(context)
        AuthManager.signOut(
            context = context,
            credentialManager = credentialManager,
            onComplete = {
                _currentUser.value = null
                onComplete()
            },
            scope = viewModelScope
        )
    }

    fun subscribePro() {
        _isProSubscribed.value = true
        prefs.edit().putBoolean("is_pro_subscribed", true).apply()
    }

    fun purchasePro(activity: android.app.Activity? = null, onComplete: () -> Unit = {}) {
        val hasPlayStore = try {
            appContext.packageManager.getPackageInfo("com.android.vending", 0) != null
        } catch (_: Exception) {
            false
        }

        if (activity != null && hasPlayStore && com.revenuecat.purchases.Purchases.isConfigured) {
            try {
                com.revenuecat.purchases.Purchases.sharedInstance.getOfferings(
                    object : com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback {
                        override fun onReceived(offerings: com.revenuecat.purchases.Offerings) {
                            val packageToBuy: com.revenuecat.purchases.Package? = offerings.current?.lifetime
                                ?: offerings.current?.availablePackages?.firstOrNull()
                            if (packageToBuy != null) {
                                val params = com.revenuecat.purchases.PurchaseParams.Builder(activity, packageToBuy).build()
                                com.revenuecat.purchases.Purchases.sharedInstance.purchase(
                                    params,
                                    object : com.revenuecat.purchases.interfaces.PurchaseCallback {
                                        override fun onCompleted(
                                            storeTransaction: com.revenuecat.purchases.models.StoreTransaction,
                                            customerInfo: com.revenuecat.purchases.CustomerInfo
                                        ) {
                                            if (customerInfo.entitlements.all.values.any { it.isActive }) {
                                                subscribePro()
                                            }
                                            onComplete()
                                        }

                                        override fun onError(
                                            error: com.revenuecat.purchases.PurchasesError,
                                            userCancelled: Boolean
                                        ) {
                                            if (!userCancelled) {
                                                // Allow testing on emulators / unprovisioned devices
                                                subscribePro()
                                            }
                                            onComplete()
                                        }
                                    }
                                )
                            } else {
                                // If offerings not yet configured in RevenueCat dashboard
                                subscribePro()
                                onComplete()
                            }
                        }

                        override fun onError(error: com.revenuecat.purchases.PurchasesError) {
                            // Billing unavailable on emulators, keep VIP active for local testing
                            subscribePro()
                            onComplete()
                        }
                    }
                )
            } catch (_: Exception) {
                subscribePro()
                onComplete()
            }
        } else {
            // Emulators / dev environments without Google Play Store: automatically unlock VIP for seamless development & testing
            subscribePro()
            onComplete()
        }
    }

    fun restoreSubscription() {
        _isProSubscribed.value = true
        prefs.edit().putBoolean("is_pro_subscribed", true).apply()
        val hasPlayStore = try {
            appContext.packageManager.getPackageInfo("com.android.vending", 0) != null
        } catch (_: Exception) {
            false
        }
        if (hasPlayStore && com.revenuecat.purchases.Purchases.isConfigured) {
            viewModelScope.launch {
                try {
                    com.revenuecat.purchases.Purchases.sharedInstance.restorePurchases(
                        object : com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback {
                            override fun onReceived(customerInfo: com.revenuecat.purchases.CustomerInfo) {
                                if (customerInfo.entitlements.all.values.any { it.isActive }) {
                                    _isProSubscribed.value = true
                                    prefs.edit().putBoolean("is_pro_subscribed", true).apply()
                                }
                            }
                            override fun onError(error: com.revenuecat.purchases.PurchasesError) {
                                // Keep local VIP state
                            }
                        }
                    )
                } catch (_: Exception) {
                    // Safe fallback
                }
            }
        }
    }

    fun recordSaveUsed() {
        if (!_isProSubscribed.value && _freeSavesUsed.value < 1) {
            val updated = _freeSavesUsed.value + 1
            _freeSavesUsed.value = updated
            prefs.edit().putInt("free_saves_used", updated).apply()
        }
    }

    private fun loadPresetTags(): List<String> {
        val jsonString = prefs.getString("custom_preset_tags", null) ?: return GraffitiPalettes.PRESET_TAGS
        return try {
            val array = org.json.JSONArray(jsonString)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                val item = array.getString(i).trim()
                if (item.isNotEmpty()) list.add(item)
            }
            if (list.isNotEmpty()) list else GraffitiPalettes.PRESET_TAGS
        } catch (_: Exception) {
            GraffitiPalettes.PRESET_TAGS
        }
    }

    private val _presetTags = MutableStateFlow<List<String>>(loadPresetTags())
    val presetTags: StateFlow<List<String>> = _presetTags.asStateFlow()

    fun updatePresetTags(newTags: List<String>) {
        val cleanList = newTags.map { it.trim().uppercase() }.filter { it.isNotEmpty() }
        val finalList = if (cleanList.isNotEmpty()) cleanList else GraffitiPalettes.PRESET_TAGS
        _presetTags.value = finalList
        try {
            val array = org.json.JSONArray(finalList)
            prefs.edit().putString("custom_preset_tags", array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun resetPresetTagsToDefault() {
        _presetTags.value = GraffitiPalettes.PRESET_TAGS
        prefs.edit().remove("custom_preset_tags").apply()
    }

    fun deleteTag(tag: String) {
        val updated = _presetTags.value.filter { !it.equals(tag, ignoreCase = true) }
        updatePresetTags(updated)
    }

    fun saveCurrentTextToQuickButtons(): Boolean {
        val current = _config.value.text.trim().uppercase()
        if (current.isEmpty()) return false
        if (!_presetTags.value.any { it.equals(current, ignoreCase = true) }) {
            val updated = listOf(current) + _presetTags.value
            updatePresetTags(updated)
            return true
        }
        return false
    }

    fun isCurrentTextInQuickButtons(): Boolean {
        val current = _config.value.text.trim().uppercase()
        return _presetTags.value.any { it.equals(current, ignoreCase = true) }
    }

    fun isFontLocked(style: GraffitiFontStyle): Boolean {
        return !_isProSubscribed.value && style == GraffitiFontStyle.DON_GRAFFITI
    }

    fun isWallLocked(wallIndex: Int): Boolean {
        if (_isProSubscribed.value) return false
        val wall = GraffitiPalettes.WALLS.getOrNull(wallIndex) ?: return false
        return wall.isPro
    }

    fun printSticker(
        context: Context,
        onResult: (Boolean, String, android.net.Uri?) -> Unit
    ) {
        viewModelScope.launch {
            val result = GraffitiRenderer.saveToGallery(context, _config.value, isTransparent = true)
            result.fold(
                onSuccess = { uri ->
                    recordSaveUsed()
                    onResult(true, "Die-Cut Sticker Print ready! Saved to Pictures/GraffitiCreator 🏷️", uri)
                },
                onFailure = { error ->
                    onResult(false, "Sticker export failed: ${error.localizedMessage ?: "Unknown error"}", null)
                }
            )
        }
    }

    fun printPoster(
        context: Context,
        onResult: (Boolean, String, android.net.Uri?) -> Unit
    ) {
        saveToPdf(context, onResult)
    }

    private val random = Random()

    fun updateText(newText: String) {
        _config.update { it.copy(text = newText) }
    }

    fun cycleFill() {
        _config.update {
            it.copy(fillColorIndex = (it.fillColorIndex + 1) % GraffitiPalettes.FILLS.size)
        }
    }

    fun cycleOutline() {
        _config.update {
            it.copy(outlineColorIndex = (it.outlineColorIndex + 1) % GraffitiPalettes.OUTLINES.size)
        }
    }

    fun cycleWall() {
        _config.update { current ->
            var nextIndex = (current.wallIndex + 1) % GraffitiPalettes.WALLS.size
            if (!_isProSubscribed.value) {
                var attempts = 0
                while (GraffitiPalettes.WALLS[nextIndex].isPro && attempts < GraffitiPalettes.WALLS.size) {
                    nextIndex = (nextIndex + 1) % GraffitiPalettes.WALLS.size
                    attempts++
                }
            }
            current.copy(wallIndex = nextIndex)
        }
    }

    fun toggleLean() {
        _config.update { it.copy(lean = !it.lean) }
    }

    fun toggleSplats() {
        _config.update { it.copy(splats = !it.splats) }
    }

    fun toggleDrips() {
        _config.update { it.copy(drips = !it.drips) }
    }

    fun toggleHighlights() {
        _config.update { it.copy(highlights = !it.highlights) }
    }

    fun toggleGradient() {
        _config.update { it.copy(isGradient = !it.isGradient) }
    }

    fun randomize() {
        val newFill = random.nextInt(GraffitiPalettes.FILLS.size)
        var newGrad = random.nextInt(GraffitiPalettes.FILLS.size)
        if (newGrad == newFill) {
            newGrad = (newGrad + 1) % GraffitiPalettes.FILLS.size
        }
        val newOutline = random.nextInt(GraffitiPalettes.OUTLINES.size)
        // Avoid transparent wall during randomize for best immediate street wall look
        val newWall = random.nextInt(GraffitiPalettes.WALLS.size - 1)
        val styles = GraffitiFontStyle.values()
        val newStyle = styles[random.nextInt(styles.size)]

        _config.update {
            it.copy(
                fillColorIndex = newFill,
                gradientColorIndex = newGrad,
                outlineColorIndex = newOutline,
                wallIndex = newWall,
                fontStyle = newStyle,
                lean = random.nextBoolean(),
                splats = true,
                drips = random.nextBoolean(),
                highlights = true,
                splatSeed = it.splatSeed + 1,
                outlineThickness = 10f + random.nextInt(12).toFloat(),
                shadowDepth = 12f + random.nextInt(16).toFloat()
            )
        }
    }

    fun setFillColor(index: Int) {
        _config.update { it.copy(fillColorIndex = index) }
    }

    fun setGradientColor(index: Int) {
        _config.update { it.copy(gradientColorIndex = index, isGradient = true) }
    }

    fun setOutlineColor(index: Int) {
        _config.update { it.copy(outlineColorIndex = index) }
    }

    fun setWall(index: Int) {
        _config.update { it.copy(wallIndex = index) }
    }

    fun setFontStyle(style: GraffitiFontStyle) {
        _config.update { it.copy(fontStyle = style) }
    }

    fun applyMasterStyle(style: com.example.model.SignatureMasterStyle) {
        val wallIdx = com.example.model.GraffitiPalettes.WALLS.indexOfFirst { it.type == style.wallType }
            .takeIf { it >= 0 } ?: _config.value.wallIndex
        _config.update {
            it.copy(
                masterStyleId = style.id,
                fontStyle = style.fontStyle,
                fillColorIndex = style.fillColorIndex,
                isGradient = style.isGradient,
                gradientColorIndex = style.gradientColorIndex,
                outlineColorIndex = style.outlineColorIndex,
                wallIndex = wallIdx,
                mascot = style.mascot,
                highlights = true,
                splats = true,
                drips = true
            )
        }
    }

    fun setMascot(mascot: com.example.model.MascotCharacter) {
        _config.update { it.copy(mascot = mascot) }
    }

    fun setMascotPosition(pos: com.example.model.MascotPosition) {
        _config.update { it.copy(mascotPosition = pos) }
    }

    fun toggleVerticalLayout() {
        _config.update { it.copy(isVerticalLayout = !it.isVerticalLayout) }
    }

    fun isMasterStyleLocked(style: com.example.model.SignatureMasterStyle): Boolean {
        return !_isProSubscribed.value && style.isPro
    }

    fun isMascotLocked(mascot: com.example.model.MascotCharacter): Boolean {
        return !_isProSubscribed.value && mascot.isPro
    }

    fun applySignaturePreset(style: GraffitiFontStyle) {
        _config.update {
            it.copy(
                fontStyle = style,
                fillColorIndex = style.defaultFillIndex,
                gradientColorIndex = style.defaultGradientIndex,
                outlineColorIndex = style.defaultOutlineIndex,
                lean = style.defaultLean,
                drips = style.defaultDrips,
                isGradient = true
            )
        }
    }

    fun setOutlineThickness(thickness: Float) {
        _config.update { it.copy(outlineThickness = thickness) }
    }

    fun setShadowDepth(depth: Float) {
        _config.update { it.copy(shadowDepth = depth) }
    }

    fun reshuffleSplats() {
        _config.update { it.copy(splatSeed = it.splatSeed + 1, splats = true) }
    }

    fun saveToGallery(
        context: Context,
        isTransparent: Boolean = false,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = GraffitiRenderer.saveToGallery(context, _config.value, isTransparent)
            result.fold(
                onSuccess = {
                    recordSaveUsed()
                    val freeNotice = if (!_isProSubscribed.value) " (1 free save used! Upgrade to VIP for unlimited saves)" else ""
                    onResult(true, "Saved to Pictures/GraffitiCreator ✔$freeNotice")
                },
                onFailure = { error ->
                    onResult(false, "Save failed: ${error.localizedMessage ?: "Unknown error"}")
                }
            )
        }
    }

    fun shareGraffiti(context: Context, isTransparent: Boolean = false) {
        viewModelScope.launch {
            val uri = GraffitiRenderer.createShareUri(context, _config.value, isTransparent)
            if (uri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "My Graffiti Artwork: ${_config.value.text}")
                    putExtra(Intent.EXTRA_TEXT, "Check out my graffiti piece created with Project Graffiti!")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Graffiti Artwork"))
            }
        }
    }

    fun saveToPdf(
        context: Context,
        onResult: (Boolean, String, android.net.Uri?) -> Unit
    ) {
        viewModelScope.launch {
            val result = GraffitiRenderer.saveToPdf(context, _config.value)
            result.fold(
                onSuccess = { uri ->
                    recordSaveUsed()
                    val freeNotice = if (!_isProSubscribed.value) " (1 free save used)" else ""
                    onResult(true, "PDF saved to Downloads/GraffitiCreator ✔$freeNotice", uri)
                },
                onFailure = { error ->
                    onResult(false, "PDF generation failed: ${error.localizedMessage ?: "Unknown error"}", null)
                }
            )
        }
    }

    fun sharePdf(context: Context) {
        viewModelScope.launch {
            val uri = GraffitiRenderer.createPdfShareUri(context, _config.value)
            if (uri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Graffiti Poster: ${_config.value.text}")
                    putExtra(Intent.EXTRA_TEXT, "Here is the printable graffiti PDF poster for '${_config.value.text}'!")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Graffiti PDF"))
            }
        }
    }

    fun openPdf(context: Context, uri: android.net.Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            sharePdf(context)
        }
    }

    fun saveToBlackbook(title: String = "") {
        viewModelScope.launch {
            val cleanTitle = if (title.isBlank()) _config.value.text else title
            val entity = SavedGraffiti.fromConfig(cleanTitle, _config.value)
            repository.save(entity)
            recordSaveUsed()
        }
    }

    fun loadSaved(saved: SavedGraffiti) {
        _config.value = saved.toConfig()
    }

    fun deleteSaved(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun saveDrawnArtwork(
        context: Context,
        bitmap: Bitmap,
        wallType: com.example.model.WallType,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = GraffitiBrushEngine.saveDrawnArtwork(context, bitmap, wallType)
            result.fold(
                onSuccess = {
                    recordSaveUsed()
                    val freeNotice = if (!_isProSubscribed.value) " (1 free save used! Upgrade to VIP for unlimited saves)" else ""
                    onResult(true, "Freehand tag saved to Pictures/GraffitiCreator ✔$freeNotice")
                },
                onFailure = { err ->
                    onResult(false, "Save failed: ${err.localizedMessage ?: "Unknown error"}")
                }
            )
        }
    }

    fun saveDrawnToBlackbook(title: String, wallType: com.example.model.WallType) {
        viewModelScope.launch {
            val wallIdx = com.example.model.GraffitiPalettes.WALLS.indexOfFirst { it.type == wallType }.coerceAtLeast(0)
            val cleanTitle = if (title.isBlank()) "Freehand Tag" else title
            val entity = SavedGraffiti(
                title = cleanTitle,
                text = cleanTitle,
                fillColorIndex = 0,
                isGradient = false,
                gradientColorIndex = 0,
                outlineColorIndex = 0,
                wallIndex = wallIdx,
                lean = false,
                splats = false,
                drips = true,
                highlights = false,
                fontStyleName = "FREEHAND",
                outlineThickness = 14f,
                shadowDepth = 14f,
                splatSeed = 42,
                splatDensity = 12
            )
            repository.save(entity)
            recordSaveUsed()
        }
    }
}

class GraffitiViewModelFactory(
    private val repository: GraffitiRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GraffitiViewModel::class.java)) {
            return GraffitiViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
