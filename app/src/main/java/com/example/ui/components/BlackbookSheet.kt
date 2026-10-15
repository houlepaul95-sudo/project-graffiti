package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedGraffiti
import com.example.model.GraffitiPalettes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextDecoration
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlackbookBottomSheet(
    savedList: List<SavedGraffiti>,
    onDismiss: () -> Unit,
    onLoad: (SavedGraffiti) -> Unit,
    onDelete: (Long) -> Unit,
    currentUser: FirebaseUser? = null,
    isSyncing: Boolean = false,
    onOpenCloudSync: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    text = "YOUR BLACKBOOK",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
                Text(
                    text = "${savedList.size} Pieces",
                    fontSize = 12.sp,
                    color = Color(0xFF00DCFF),
                    fontWeight = FontWeight.Bold
                )
            }

            // Cloud Sync Status Pill
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2334)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCloudSync() }
                    .border(1.dp, Color(0xFF2C3247), RoundedCornerShape(10.dp))
                    .padding(bottom = 8.dp)
                    .testTag("card_blackbook_cloud_sync")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = if (currentUser != null) Color(0xFF8CFF50) else Color(0xFF00DCFF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentUser != null)
                                "Cloud Synced (${currentUser.email?.take(22) ?: "Google Account"})"
                            else
                                "Cloud Backup • Tap to Sync",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCCD2E3)
                        )
                    }

                    Text(
                        text = if (currentUser != null) "SYNCED" else "ENABLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00DCFF),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (savedList.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = Color(0xFF3E4358),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No saved tags yet!",
                            color = Color(0xFF8F95A8),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tap 'SAVE' to store your street art tags here",
                            color = Color(0xFF5D6277),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier.height(340.dp)
                ) {
                    items(savedList, key = { it.id }) { item ->
                        BlackbookItem(
                            item = item,
                            onLoad = {
                                onLoad(item)
                                onDismiss()
                            },
                            onDelete = { onDelete(item.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer with legal links
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Terms of Service",
                    fontSize = 10.sp,
                    color = Color(0xFF8E95AA),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { onOpenTerms() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("link_terms_from_blackbook")
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
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { onOpenPrivacy() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("link_privacy_from_blackbook")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BlackbookItem(
    item: SavedGraffiti,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(item.createdAt))
    val fillColor = GraffitiPalettes.FILLS.getOrElse(item.fillColorIndex) { GraffitiPalettes.FILLS[0] }.color
    val outlineColor = GraffitiPalettes.OUTLINES.getOrElse(item.outlineColorIndex) { GraffitiPalettes.OUTLINES[0] }.color

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212E)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2C3042), RoundedCornerShape(12.dp))
            .clickable(onClick = onLoad)
            .testTag("blackbook_item_${item.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Color indicator swatch
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(fillColor)
                    .border(2.dp, outlineColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.text,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = "${item.fontStyleName} • $dateStr",
                    fontSize = 11.sp,
                    color = Color(0xFF7E8498)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_saved_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete saved piece",
                    tint = Color(0xFFFF50B4),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
