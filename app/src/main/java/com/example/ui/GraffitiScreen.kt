package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppNavigationPage
import com.example.ui.components.BlackbookBottomSheet
import com.example.ui.components.CloudSyncDialog
import com.example.ui.components.ExportBottomSheet
import com.example.ui.components.FreehandDrawingCanvas
import com.example.ui.components.LegalDocsDialog
import com.example.ui.components.LegalTab
import com.example.ui.components.ProSubscriptionDialog
import com.example.ui.components.RateUsDialog
import com.example.ui.components.WildstyleStudioPage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraffitiScreen(
    viewModel: GraffitiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val config by viewModel.config.collectAsStateWithLifecycle()
    val savedCreations by viewModel.savedCreations.collectAsStateWithLifecycle()
    val isProSubscribed by viewModel.isProSubscribed.collectAsStateWithLifecycle()
    val freeSavesUsed by viewModel.freeSavesUsed.collectAsStateWithLifecycle()
    val presetTags by viewModel.presetTags.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val activePage by viewModel.activePage.collectAsStateWithLifecycle()
    val showRateUsDialog by viewModel.showRateUsDialog.collectAsStateWithLifecycle()

    var showExportSheet by remember { mutableStateOf(false) }
    var showBlackbookSheet by remember { mutableStateOf(false) }
    var showProDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf(false) }
    var legalDialogInitialTab by remember { mutableStateOf(LegalTab.TERMS) }
    var showTopOverflowMenu by remember { mutableStateOf(false) }
    var pendingProAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val requireProUnlock: (() -> Unit) -> Unit = { action ->
        if (isProSubscribed) {
            action()
        } else {
            pendingProAction = action
            showProDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF50B4))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatPaint,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PROJECT GRAFFITI",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    // Street Artist Pro Pass VIP Trigger
                    IconButton(
                        onClick = { showProDialog = true },
                        modifier = Modifier.testTag("action_pro_pass")
                    ) {
                        BadgedBox(
                            badge = {
                                if (!isProSubscribed) {
                                    Badge(
                                        containerColor = Color(0xFFFF50B4),
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = "VIP",
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Unlock Street Pro Pass",
                                tint = if (isProSubscribed) Color(0xFF00DCFF) else Color(0xFFFFDC00)
                            )
                        }
                    }

                    // Blackbook gallery button with count badge
                    IconButton(
                        onClick = { showBlackbookSheet = true },
                        modifier = Modifier.testTag("action_blackbook")
                    ) {
                        BadgedBox(
                            badge = {
                                if (savedCreations.isNotEmpty()) {
                                    Badge(
                                        containerColor = Color(0xFF00DCFF),
                                        contentColor = Color.Black
                                    ) {
                                        Text(
                                            text = "${savedCreations.size}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = "Blackbook Gallery",
                                tint = Color(0xFFC7CBD8)
                            )
                        }
                    }

                    // Overflow Menu Button (Cloud Sync, Terms of Service, Privacy Policy, VIP Pass)
                    Box {
                        IconButton(
                            onClick = { showTopOverflowMenu = true },
                            modifier = Modifier.testTag("action_more_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options & Legal",
                                tint = Color(0xFFC7CBD8)
                            )
                        }

                        DropdownMenu(
                            expanded = showTopOverflowMenu,
                            onDismissRequest = { showTopOverflowMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF1B1E2D))
                                .border(1.dp, Color(0xFF2B3145), RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Cloud Sync (Android)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (currentUser != null) "Connected: ${currentUser?.email?.take(20)}" else "Google Account Backup",
                                            fontSize = 10.sp,
                                            color = Color(0xFF8E95AA)
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = if (currentUser != null) Color(0xFF8CFF50) else Color(0xFF00DCFF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    showTopOverflowMenu = false
                                    showCloudSyncDialog = true
                                },
                                modifier = Modifier.testTag("menu_cloud_sync")
                            )

                            HorizontalDivider(color = Color(0xFF262B3D))

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Terms of Service",
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF00DCFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showTopOverflowMenu = false
                                    legalDialogInitialTab = LegalTab.TERMS
                                    showLegalDialog = true
                                },
                                modifier = Modifier.testTag("menu_terms_of_service")
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Privacy Policy",
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Policy,
                                        contentDescription = null,
                                        tint = Color(0xFF00DCFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showTopOverflowMenu = false
                                    legalDialogInitialTab = LegalTab.PRIVACY
                                    showLegalDialog = true
                                },
                                modifier = Modifier.testTag("menu_privacy_policy")
                            )

                            HorizontalDivider(color = Color(0xFF262B3D))

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isProSubscribed) "Street Artist VIP Active 👑" else "Street Artist VIP Pass ($9.99)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFDC00)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFFFDC00),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showTopOverflowMenu = false
                                    showProDialog = true
                                },
                                modifier = Modifier.testTag("menu_pro_pass")
                            )

                            HorizontalDivider(color = Color(0xFF262B3D))

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Rate Project Graffiti ⭐",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFDC00)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFDC00),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showTopOverflowMenu = false
                                    viewModel.openRateUsDialog()
                                },
                                modifier = Modifier.testTag("menu_rate_app")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF14141C)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF14141C),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ---- Top Page Selector Navigation Tabs ----
            TabRow(
                selectedTabIndex = if (activePage == AppNavigationPage.FREEHAND_CANVAS) 0 else 1,
                containerColor = Color(0xFF141724),
                contentColor = Color(0xFF00DCFF),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (activePage == AppNavigationPage.FREEHAND_CANVAS) 0 else 1]),
                        color = if (activePage == AppNavigationPage.FREEHAND_CANVAS) Color(0xFF00DCFF) else Color(0xFFFF50B4),
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF222638))
            ) {
                Tab(
                    selected = activePage == AppNavigationPage.FREEHAND_CANVAS,
                    onClick = { viewModel.setNavigationPage(AppNavigationPage.FREEHAND_CANVAS) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = null,
                                tint = if (activePage == AppNavigationPage.FREEHAND_CANVAS) Color(0xFF00DCFF) else Color(0xFF7E8498),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FREEHAND CANVAS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (activePage == AppNavigationPage.FREEHAND_CANVAS) Color(0xFF00DCFF) else Color(0xFF7E8498)
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_freehand_canvas")
                )

                Tab(
                    selected = activePage == AppNavigationPage.WILDSTYLE_STUDIO,
                    onClick = { viewModel.setNavigationPage(AppNavigationPage.WILDSTYLE_STUDIO) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (activePage == AppNavigationPage.WILDSTYLE_STUDIO) Color(0xFFFF50B4) else Color(0xFF7E8498),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WILDSTYLE & CREW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (activePage == AppNavigationPage.WILDSTYLE_STUDIO) Color(0xFFFF50B4) else Color(0xFF7E8498)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isProSubscribed) Color(0xFF8CFF50) else Color(0xFFFFDC00)
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isProSubscribed) "PRO" else "VIP",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.testTag("tab_wildstyle_studio")
                )
            }

            // ---- Active Page Display ----
            when (activePage) {
                AppNavigationPage.FREEHAND_CANVAS -> {
                    FreehandDrawingCanvas(
                        isProSubscribed = isProSubscribed,
                        onSaveRequested = { bitmap, wallType ->
                            if (isProSubscribed || freeSavesUsed < 1) {
                                viewModel.saveDrawnArtwork(context, bitmap, wallType) { success, msg ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            } else {
                                requireProUnlock {
                                    viewModel.saveDrawnArtwork(context, bitmap, wallType) { success, msg ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                }
                            }
                        },
                        onRequirePro = {
                            showProDialog = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AppNavigationPage.WILDSTYLE_STUDIO -> {
                    WildstyleStudioPage(
                        config = config,
                        isProSubscribed = isProSubscribed,
                        presetTags = presetTags,
                        onUpdateConfig = { newConfig ->
                            viewModel.updateConfig(newConfig)
                        },
                        onRandomize = { viewModel.randomize() },
                        onReshuffleSplats = { viewModel.reshuffleSplats() },
                        onSaveRequested = {
                            requireProUnlock {
                                showExportSheet = true
                            }
                        },
                        onSavePdfRequested = {
                            requireProUnlock {
                                viewModel.saveToPdf(context) { success, message, uri ->
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = message,
                                            actionLabel = if (uri != null) "OPEN" else null
                                        )
                                        if (result == SnackbarResult.ActionPerformed && uri != null) {
                                            viewModel.openPdf(context, uri)
                                        }
                                    }
                                }
                            }
                        },
                        onSaveBlackbookRequested = {
                            requireProUnlock {
                                viewModel.saveToBlackbook()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Wildstyle piece saved to Blackbook! 🎨")
                                }
                            }
                        },
                        onShareRequested = {
                            requireProUnlock {
                                viewModel.shareGraffiti(context)
                            }
                        },
                        onRequirePro = {
                            showProDialog = true
                        },
                        onSwitchToDrawingCanvas = {
                            viewModel.setNavigationPage(AppNavigationPage.FREEHAND_CANVAS)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Export Bottom Sheet
    if (showExportSheet) {
        ExportBottomSheet(
            onDismiss = { showExportSheet = false },
            isProSubscribed = isProSubscribed,
            freeSavesUsed = freeSavesUsed,
            onSaveToGallery = { isTransparent ->
                requireProUnlock {
                    viewModel.saveToGallery(context, isTransparent) { success, message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                }
            },
            onSaveToPdf = {
                requireProUnlock {
                    viewModel.saveToPdf(context) { success, message, uri ->
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = if (uri != null) "OPEN" else null
                            )
                            if (result == SnackbarResult.ActionPerformed && uri != null) {
                                viewModel.openPdf(context, uri)
                            }
                        }
                    }
                }
            },
            onShare = { isTransparent ->
                requireProUnlock {
                    viewModel.shareGraffiti(context, isTransparent)
                }
            },
            onSharePdf = {
                requireProUnlock {
                    viewModel.sharePdf(context)
                }
            },
            onSaveToBlackbook = {
                requireProUnlock {
                    viewModel.saveToBlackbook()
                    scope.launch {
                        snackbarHostState.showSnackbar("Saved to Blackbook! 🎨")
                    }
                }
            }
        )
    }

    // Blackbook Bottom Sheet
    if (showBlackbookSheet) {
        BlackbookBottomSheet(
            savedList = savedCreations,
            onDismiss = { showBlackbookSheet = false },
            onLoad = { item ->
                viewModel.loadSaved(item)
                viewModel.setNavigationPage(AppNavigationPage.WILDSTYLE_STUDIO)
                scope.launch {
                    snackbarHostState.showSnackbar("Loaded '${item.text}' from Blackbook")
                }
            },
            onDelete = { id ->
                viewModel.deleteSaved(id)
            },
            currentUser = currentUser,
            isSyncing = isSyncing,
            onOpenCloudSync = { showCloudSyncDialog = true },
            onOpenTerms = {
                legalDialogInitialTab = LegalTab.TERMS
                showLegalDialog = true
            },
            onOpenPrivacy = {
                legalDialogInitialTab = LegalTab.PRIVACY
                showLegalDialog = true
            }
        )
    }

    // Lifetime VIP Pass Dialog ($9.99 One-Time Payment Unlocks Everything)
    if (showProDialog) {
        ProSubscriptionDialog(
            onDismiss = {
                showProDialog = false
                pendingProAction = null
            },
            onSubscribe = {
                val activity = context as? android.app.Activity
                viewModel.purchasePro(activity) {
                    showProDialog = false
                    val pending = pendingProAction
                    pendingProAction = null
                    if (pending != null) {
                        pending()
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar("🎉 All Features & Wildstyles Unlocked Forever!")
                    }
                }
            },
            onRestore = {
                viewModel.restoreSubscription()
                showProDialog = false
                val pending = pendingProAction
                pendingProAction = null
                if (pending != null) {
                    pending()
                }
                scope.launch {
                    snackbarHostState.showSnackbar("Purchases restored! All features unlocked.")
                }
            },
            onOpenTerms = {
                legalDialogInitialTab = LegalTab.TERMS
                showLegalDialog = true
            },
            onOpenPrivacy = {
                legalDialogInitialTab = LegalTab.PRIVACY
                showLegalDialog = true
            }
        )
    }

    // Terms of Service and Privacy Policy Dialog
    if (showLegalDialog) {
        LegalDocsDialog(
            initialTab = legalDialogInitialTab,
            onDismiss = { showLegalDialog = false }
        )
    }

    // Firebase Cloud Sync on Android Dialog
    if (showCloudSyncDialog) {
        CloudSyncDialog(
            currentUser = currentUser,
            isSyncing = isSyncing,
            lastSyncTime = lastSyncTime,
            onSignIn = {
                viewModel.signInWithGoogle(
                    context = context,
                    onSuccess = { user ->
                        scope.launch {
                            snackbarHostState.showSnackbar("Signed in as ${user.email ?: "Google user"}! Synced to cloud.")
                        }
                    },
                    onError = { err ->
                        scope.launch {
                            snackbarHostState.showSnackbar("Sign-in notice: $err")
                        }
                    }
                )
            },
            onSignOut = {
                viewModel.signOut(context) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Signed out of Google account.")
                    }
                }
            },
            onSyncNow = {
                viewModel.syncWithCloud { success, msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            },
            onOpenTerms = {
                legalDialogInitialTab = LegalTab.TERMS
                showLegalDialog = true
            },
            onOpenPrivacy = {
                legalDialogInitialTab = LegalTab.PRIVACY
                showLegalDialog = true
            },
            onDismiss = { showCloudSyncDialog = false }
        )
    }

    // Rate Us Prompt (opens first thing on launch or on user request)
    if (showRateUsDialog) {
        RateUsDialog(
            onRateOnPlayStore = {
                viewModel.onUserRated()
                scope.launch {
                    snackbarHostState.showSnackbar("Thank you for your rating & review on Google Play! ⭐")
                }
            },
            onRemindLater = {
                viewModel.dismissRateUs(neverAskAgain = false)
            },
            onNoThanks = {
                viewModel.dismissRateUs(neverAskAgain = true)
            },
            onDismiss = {
                viewModel.dismissRateUs(neverAskAgain = false)
            }
        )
    }
}
