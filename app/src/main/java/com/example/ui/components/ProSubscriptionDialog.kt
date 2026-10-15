package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ProSubscriptionDialog(
    onDismiss: () -> Unit,
    onSubscribe: () -> Unit,
    onRestore: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141724)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF00DCFF), RoundedCornerShape(20.dp))
                .testTag("dialog_pro_subscription")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top close button & tag
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFF50B4))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "LIFETIME VIP UNLOCK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("dialog_pro_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF8E95AA)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Spray Paint Can / Crown Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00DCFF), Color(0xFFFF50B4))
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatPaint,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title with brand flair
                Text(
                    text = "ONE PAYMENT UNLOCKS EVERYTHING",
                    fontSize = 18.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Pay once, save & print forever! No subscriptions or recurring charges. One payment unlocks high-res saves, posters, vinyl stickers, and all VIP walls & fonts permanently.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF00DCFF),
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price Tag Banner ($9.99 Lifetime)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2233)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFFDC00).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$9.99",
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFDC00)
                                )
                                Text(
                                    text = "  LIFETIME",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "One-Time Payment • Unlocks Everything Forever",
                                fontSize = 10.sp,
                                color = Color(0xFFB0B7CD)
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDC00)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "LIFETIME PASS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Included Features List
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProFeatureRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Crazy Wildstyle Templates & Burners",
                        desc = "Electric Chrome, Acid Toxic, Subway Wholecar, Cosmic Zero-G & Rust Freight wildstyles"
                    )
                    ProFeatureRow(
                        icon = Icons.Default.FormatPaint,
                        title = "Add & Layer Mascot Characters",
                        desc = "Spray Can Boy, Alley Rat, Cyber Mech Bot, Cosmic Astronaut & Street Bulldog"
                    )
                    ProFeatureRow(
                        icon = Icons.Default.Save,
                        title = "Save & Export All Creations",
                        desc = "Unlock saving for freehand drawings, wildstyle pieces, PDF posters & sticker PNGs"
                    )
                    ProFeatureRow(
                        icon = Icons.Default.Edit,
                        title = "Custom Quick Set Graphic Buttons",
                        desc = "Edit quick buttons to your own crew names, handles & custom tags"
                    )
                    ProFeatureRow(
                        icon = Icons.Default.PictureAsPdf,
                        title = "Print A4 Street Art Posters & Vinyl Stickers",
                        desc = "Printable wall posters with bleed & cut-ready sticker PNGs"
                    )
                    ProFeatureRow(
                        icon = Icons.Default.Lock,
                        title = "All Photo Walls & Urban Backdrops",
                        desc = "NYC Subway Train, Rooftop Skyline, Highway Overpass & Train Yard"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CTA Button ($9.99 Lifetime Access)
                Button(
                    onClick = onSubscribe,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDC00),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_unlock_pro_pass")
                ) {
                    Text(
                        text = "UNLOCK EVERYTHING • $9.99",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Friendly Dismiss: Keep creating free
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onRestore) {
                        Text(
                            text = "Restore Purchase",
                            fontSize = 11.sp,
                            color = Color(0xFF7E8498)
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Keep Creating Free ✌️",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00DCFF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Mandatory App Store & Google Play Legal Compliance Links
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Terms of Service",
                        fontSize = 10.sp,
                        color = Color(0xFF8E95AA),
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        modifier = Modifier
                            .clickable { onOpenTerms() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("link_terms_of_service")
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = Color(0xFF5E6578),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    Text(
                        text = "Privacy Policy",
                        fontSize = 10.sp,
                        color = Color(0xFF8E95AA),
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        modifier = Modifier
                            .clickable { onOpenPrivacy() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("link_privacy_policy")
                    )
                }
            }
        }
    }
}

@Composable
private fun ProFeatureRow(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF222739))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF00DCFF),
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = desc,
                fontSize = 10.sp,
                color = Color(0xFF8E95AA)
            )
        }
    }
}
