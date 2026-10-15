package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBottomSheet(
    onDismiss: () -> Unit,
    onSaveToGallery: (isTransparent: Boolean) -> Unit,
    onSaveToPdf: () -> Unit,
    onShare: (isTransparent: Boolean) -> Unit,
    onSharePdf: () -> Unit,
    onSaveToBlackbook: () -> Unit,
    isProSubscribed: Boolean = false,
    freeSavesUsed: Int = 0,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF161822),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            ) {
                Text(
                    text = "EXPORT & DOWNLOAD",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )

                if (isProSubscribed) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF00DCFF)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "LIFETIME VIP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                } else if (freeSavesUsed < 1) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDC00)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "1 FREE SAVE REMAINING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF50B4)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "VIP PASS REQUIRED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // PDF Option (Feature requested by user)
            ExportOptionCard(
                title = "Print & Download PDF Poster",
                subtitle = "Turn this wildstyle tag into an A4 printable street art PDF poster with specs",
                icon = Icons.Default.PictureAsPdf,
                accentColor = Color(0xFFFFDC00),
                isLocked = !isProSubscribed,
                onClick = {
                    onSaveToPdf()
                    onDismiss()
                },
                testTag = "export_save_pdf"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 1: Save standard with wall background
            ExportOptionCard(
                title = "Save to Gallery (With Wall)",
                subtitle = "High-res PNG with current brick/concrete/slate background",
                icon = Icons.Default.SaveAlt,
                accentColor = Color(0xFF00DCFF),
                isLocked = !isProSubscribed,
                onClick = {
                    onSaveToGallery(false)
                    onDismiss()
                },
                testTag = "export_save_wall"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Save transparent sticker PNG
            ExportOptionCard(
                title = "Save Transparent Sticker PNG",
                subtitle = "Cutout graffiti artwork with no background, perfect for stickers",
                icon = Icons.Default.Image,
                accentColor = Color(0xFFFF50B4),
                isLocked = !isProSubscribed,
                onClick = {
                    onSaveToGallery(true)
                    onDismiss()
                },
                testTag = "export_save_transparent"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 3: Share Image / Share PDF
            ExportOptionCard(
                title = "Share PDF Poster",
                subtitle = "Send printable PDF directly to printers, messages, or cloud",
                icon = Icons.Default.Share,
                accentColor = Color(0xFF00FFB4),
                isLocked = !isProSubscribed,
                onClick = {
                    onSharePdf()
                    onDismiss()
                },
                testTag = "export_share_pdf"
            )

            Spacer(modifier = Modifier.height(10.dp))

            ExportOptionCard(
                title = "Share Image to Instagram / Friends",
                subtitle = "Send high-res PNG to chat apps, socials, or photo editors",
                icon = Icons.Default.Share,
                accentColor = Color(0xFFCFD8DC),
                isLocked = !isProSubscribed,
                onClick = {
                    onShare(false)
                    onDismiss()
                },
                testTag = "export_share"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 5: Save to Blackbook
            ExportOptionCard(
                title = "Save to Blackbook",
                subtitle = "Keep in your sketchbook to edit or remix anytime",
                icon = Icons.Default.BookmarkBorder,
                accentColor = Color(0xFF8CFF50),
                isLocked = !isProSubscribed,
                onClick = {
                    onSaveToBlackbook()
                    onDismiss()
                },
                testTag = "export_blackbook"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isLocked: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212E)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isLocked) Color(0xFF3B2F1F) else Color(0xFF2C3042),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDC00)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "VIP LOCK",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF8F95A8),
                    lineHeight = 15.sp
                )
            }
        }
    }
}
