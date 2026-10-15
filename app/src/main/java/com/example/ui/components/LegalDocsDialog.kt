package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

const val TERMS_URL = "https://projectgraffiti.app/terms"
const val PRIVACY_URL = "https://projectgraffiti.app/privacy"

enum class LegalTab(val title: String) {
    TERMS("Terms of Service"),
    PRIVACY("Privacy Policy")
}

@Composable
fun LegalDocsDialog(
    initialTab: LegalTab = LegalTab.TERMS,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(if (initialTab == LegalTab.TERMS) 0 else 1) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF141622),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(600.dp)
                .border(1.dp, Color(0xFF2C3247), RoundedCornerShape(16.dp))
                .testTag("dialog_legal_docs")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with title and close button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00DCFF).copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.Description else Icons.Default.Policy,
                                contentDescription = null,
                                tint = Color(0xFF00DCFF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PROJECT GRAFFITI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color(0xFF8E95AA)
                            )
                            Text(
                                text = if (selectedTab == 0) "Terms of Service" else "Privacy Policy",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_legal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF8E95AA)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Selector (Terms vs Privacy)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1B1E2D),
                    contentColor = Color(0xFF00DCFF),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF00DCFF),
                            height = 2.5.dp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF2B3145), RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "TERMS OF USE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (selectedTab == 0) Color(0xFF00DCFF) else Color(0xFF8E95AA)
                            )
                        },
                        modifier = Modifier.testTag("tab_terms")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "PRIVACY POLICY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (selectedTab == 1) Color(0xFF00DCFF) else Color(0xFF8E95AA)
                            )
                        },
                        modifier = Modifier.testTag("tab_privacy")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Document Body
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10121B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF222636), RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(scrollState)
                    ) {
                        if (selectedTab == 0) {
                            TermsOfServiceContent()
                        } else {
                            PrivacyPolicyContent()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom actions: Open in Browser + Done
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            openUrl(context, if (selectedTab == 0) TERMS_URL else PRIVACY_URL)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00DCFF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_open_legal_browser")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Open in Browser",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00DCFF),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_accept_legal")
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TermsOfServiceContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LegalHeader("Last Updated: October 2026")

        LegalSectionTitle("1. Agreement to Terms")
        LegalParagraph("By installing, accessing, or using Project Graffiti (\"the Application\"), you agree to be bound by these Terms of Service. If you do not agree to these Terms, do not install or use the Application.")

        LegalSectionTitle("2. License & Lifetime Pass")
        LegalParagraph("Project Graffiti offers a one-time purchase Lifetime VIP Pass ($9.99 USD or local equivalent). This purchase grants you a personal, perpetual, non-exclusive, non-transferable license to unlock all premium lettering styles, companion character mascots, photo backdrops, printable PDF posters, and cut-ready sticker exports on your authorized Google Play account without recurring subscription fees.")

        LegalSectionTitle("3. User Content & Intellectual Property Ownership")
        LegalParagraph("You retain 100% full copyright and ownership of all graffiti artwork, lettering pieces, customized tags, poster layouts, and sticker images that you create and export using Project Graffiti. You are free to use your exported artwork for personal, artistic, or commercial purposes.")

        LegalSectionTitle("4. Responsible Use & Anti-Vandalism Disclaimer")
        LegalParagraph("Project Graffiti is designed as a digital art and creative design tool. We strictly condemn and do not encourage illegal defacement, unauthorized tagging, or vandalism of physical public or private property without legal permission. Please respect your community and create on digital walls or legal street-art spaces.")

        LegalSectionTitle("5. In-App Purchases & Refund Policy")
        LegalParagraph("All in-app purchases and lifetime unlocks are processed through the Google Play Store billing system and are subject to Google Play Terms of Service. Refund requests are handled through standard Google Play customer support channels.")

        LegalSectionTitle("6. Disclaimer of Warranties & Limitation of Liability")
        LegalParagraph("Project Graffiti is provided \"AS IS\" and \"AS AVAILABLE\" without warranties of any kind. We do not guarantee uninterrupted or error-free operation. In no event shall the developers be liable for indirect, incidental, or consequential damages.")

        LegalSectionTitle("7. Contact & Support")
        LegalParagraph("For questions or support regarding these Terms, contact our team at diamondedgemetals@gmail.com.")
    }
}

@Composable
private fun PrivacyPolicyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LegalHeader("Last Updated: October 2026")

        LegalSectionTitle("1. Privacy Commitment")
        LegalParagraph("Your privacy is essential. Project Graffiti is designed as an offline-first application that respects user privacy. We do not harvest, track, sell, or rent your personal information to third parties.")

        LegalSectionTitle("2. Information We Do NOT Collect")
        LegalParagraph("• We do not collect your personal contact information (address, phone number).\n• We do not access your device's contacts, photos, camera, or microphone.\n• We do not track your location.\n• We do not use third-party advertising tracking networks.")

        LegalSectionTitle("3. Data Stored on Your Device")
        LegalParagraph("All artwork you create, customized quick-set buttons, saved Blackbook pieces, and personal palette preferences are stored locally on your device in sandboxed application storage and Room database cache. You have full control over your saved creations.")

        LegalSectionTitle("4. Storage & Media Permissions")
        LegalParagraph("The Application requests access to external storage or the Android MediaStore exclusively when you choose to save exported artwork (JPEG/PNG) or download printable PDF posters to your device's Pictures or Downloads folders. We do not inspect or read any existing files outside of the content generated by the Application.")

        LegalSectionTitle("5. Cloud Services & Authentication")
        LegalParagraph("If cloud sync or Firebase database features are enabled by you, authentication is securely facilitated through Google Identity Services / Jetpack Credential Manager. We only receive your unique authentication identifier to associate your saved cloud pieces. Passwords are never seen or stored.")

        LegalSectionTitle("6. Children's Privacy")
        LegalParagraph("Project Graffiti complies with the Children's Online Privacy Protection Act (COPPA). We do not knowingly collect personal information from children under 13.")

        LegalSectionTitle("7. Contacting Us")
        LegalParagraph("If you have questions or concerns about this Privacy Policy, please email diamondedgemetals@gmail.com.")
    }
}

@Composable
private fun LegalHeader(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF00DCFF)
    )
}

@Composable
private fun LegalSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = Color.White,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun LegalParagraph(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFF9EACB8),
        lineHeight = 15.sp
    )
}

fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
