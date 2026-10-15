package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GraffitiConfig
import com.example.model.GraffitiPalettes
import com.example.model.MascotCharacter
import com.example.model.MascotPosition
import com.example.model.WildstylePresets
import com.example.model.WildstyleTemplate

@Composable
fun WildstyleStudioPage(
    config: GraffitiConfig,
    isProSubscribed: Boolean,
    presetTags: List<String>,
    onUpdateConfig: (GraffitiConfig) -> Unit,
    onRandomize: () -> Unit,
    onReshuffleSplats: () -> Unit,
    onSaveRequested: () -> Unit,
    onSavePdfRequested: () -> Unit,
    onSaveBlackbookRequested: () -> Unit,
    onShareRequested: () -> Unit,
    onRequirePro: () -> Unit,
    onSwitchToDrawingCanvas: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isFullscreenCanvas by remember { mutableStateOf(false) }
    var selectedTemplateId by remember { mutableStateOf(WildstylePresets.TEMPLATES[0].id) }
    var showCharacterControls by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .testTag("wildstyle_studio_page")
    ) {
        // ---- 1. VIP Subscription Status Banner ----
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isProSubscribed) Color(0xFF192A22) else Color(0xFF281E19)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .border(
                    width = 1.dp,
                    color = if (isProSubscribed) Color(0xFF8CFF50) else Color(0xFFFFDC00),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                    if (!isProSubscribed) onRequirePro()
                }
                .testTag("banner_wildstyle_vip_tier")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isProSubscribed) Color(0xFF8CFF50) else Color(0xFFFFDC00))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isProSubscribed) "VIP PASS ACTIVE • UNLOCKED" else "PAID TIER: SAVE LOCKED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (isProSubscribed) Color(0xFF8CFF50) else Color(0xFFFFDC00)
                        )
                        Text(
                            text = if (isProSubscribed)
                                "All crazy wildstyles & characters ready to save & export"
                            else
                                "Preview wildstyles & crew free • Saving requires VIP Pass",
                            fontSize = 10.sp,
                            color = Color(0xFFCCD2E3)
                        )
                    }
                }

                if (!isProSubscribed) {
                    Button(
                        onClick = onRequirePro,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFDC00),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "UNLOCK $9.99",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // ---- 2. Live Wildstyle & Character Canvas (BIGGER CANVAS) ----
        GraffitiCanvas(
            config = config,
            onRandomize = onRandomize,
            onReshuffleSplats = onReshuffleSplats,
            isFullscreen = isFullscreenCanvas,
            onToggleFullscreen = { isFullscreenCanvas = !isFullscreenCanvas },
            onCycleMascot = {
                val mascots = MascotCharacter.values()
                val nextIdx = (mascots.indexOf(config.mascot) + 1) % mascots.size
                val nextMascot = mascots[nextIdx]
                if (nextMascot.isPro && !isProSubscribed) {
                    onRequirePro()
                } else {
                    onUpdateConfig(config.copy(mascot = nextMascot))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // ---- 3. Pick from Selection of Real Wild Graffiti Templates ----
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Style,
                    contentDescription = null,
                    tint = Color(0xFFFF50B4),
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SELECT WILD GRAFFITI TEMPLATE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = Color.White
                )
            }

            Text(
                text = "${WildstylePresets.TEMPLATES.size} TEMPLATES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF50B4)
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(WildstylePresets.TEMPLATES) { template ->
                val isSelected = selectedTemplateId == template.id
                val isLocked = template.isPro && !isProSubscribed
                val cardBorder = when {
                    isSelected -> template.accentColor
                    isLocked -> Color(0xFF6B5824)
                    else -> Color(0xFF2B3045)
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF222638) else Color(0xFF161824)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(200.dp)
                        .clickable {
                            if (isLocked) {
                                onRequirePro()
                            } else {
                                selectedTemplateId = template.id
                                val wallIdx = GraffitiPalettes.WALLS.indexOfFirst { it.type == template.wallType }.coerceAtLeast(0)
                                onUpdateConfig(
                                    config.copy(
                                        masterStyleId = template.id,
                                        fontStyle = template.fontStyle,
                                        fillColorIndex = template.fillColorIndex,
                                        isGradient = template.isGradient,
                                        gradientColorIndex = template.gradientColorIndex,
                                        outlineColorIndex = template.outlineColorIndex,
                                        wallIndex = wallIdx,
                                        mascot = template.defaultMascot,
                                        outlineThickness = template.outlineThickness,
                                        shadowDepth = template.shadowDepth,
                                        highlights = true,
                                        splats = true,
                                        drips = true
                                    )
                                )
                            }
                        }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = cardBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag("wildstyle_template_${template.id}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = template.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            when {
                                isLocked -> {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2010)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Subscription Blocked",
                                                tint = Color(0xFFFFDC00),
                                                modifier = Modifier.size(9.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "VIP",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFFFDC00)
                                            )
                                        }
                                    }
                                }
                                isSelected -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = template.accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = template.subtitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = template.accentColor,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Text(
                            text = template.description,
                            fontSize = 9.sp,
                            color = Color(0xFF8E95AA),
                            maxLines = 2,
                            lineHeight = 12.sp
                        )

                        // Subscription block badge if locked
                        if (isLocked) {
                            Spacer(modifier = Modifier.height(5.dp))
                            Surface(
                                color = Color(0xFFFFDC00).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFDC00).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFFFFDC00),
                                        modifier = Modifier.size(8.5.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "VIP SUBSCRIPTION BLOCKED",
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFDC00),
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Mascot indicator tag
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(template.accentColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Crew: ${template.defaultMascot.displayName}",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC7CBD8)
                            )
                        }
                    }
                }
            }
        }

        // ---- 4. Graffiti Character Add-On (Incorporate into canvas if needed) ----
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161928)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .border(
                    width = 1.dp,
                    color = if (config.mascot != MascotCharacter.NONE) Color(0xFFFF50B4).copy(alpha = 0.7f) else Color(0xFF2B3145),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFF50B4),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GRAFFITI CHARACTER ADD-ON",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (config.mascot != MascotCharacter.NONE) Color(0xFFFF50B4) else Color(0xFF282C40)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (config.mascot != MascotCharacter.NONE) "ON CANVAS" else "ADD-ON",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Mascot Position selector
                    if (config.mascot != MascotCharacter.NONE) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            MascotPosition.values().forEach { pos ->
                                val isPosSelected = config.mascotPosition == pos
                                Text(
                                    text = pos.displayName.take(5),
                                    fontSize = 8.5.sp,
                                    fontWeight = if (isPosSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isPosSelected) Color(0xFF00DCFF) else Color(0xFF7E8498),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isPosSelected) Color(0xFF00DCFF).copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { onUpdateConfig(config.copy(mascotPosition = pos)) }
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = if (config.mascot != MascotCharacter.NONE)
                        "Incorporate companion character with your letters • Tap below to change or remove"
                    else
                        "Incorporate street characters into your canvas if needed",
                    fontSize = 9.sp,
                    color = Color(0xFF8E95AA),
                    modifier = Modifier.padding(vertical = 3.dp)
                )

                // Mascot selector chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MascotCharacter.values()) { mascot ->
                        val isSelected = config.mascot == mascot
                        val isLocked = mascot.isPro && !isProSubscribed
                        val mascotEmoji = when (mascot) {
                            MascotCharacter.NONE -> "🚫"
                            MascotCharacter.SPRAY_CAN -> "🧴"
                            MascotCharacter.ALLEY_RAT -> "🐀"
                            MascotCharacter.MECH_BOT -> "🤖"
                            MascotCharacter.ASTRO_CADET -> "👨‍🚀"
                            MascotCharacter.STREET_BULLDOG -> "🐶"
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF262C42) else Color(0xFF141622)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable {
                                    if (isLocked) {
                                        onRequirePro()
                                    } else {
                                        onUpdateConfig(config.copy(mascot = mascot))
                                    }
                                }
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = when {
                                        isSelected -> Color(0xFFFF50B4)
                                        isLocked -> Color(0xFF5A481B)
                                        else -> Color(0xFF25293B)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .testTag("mascot_select_${mascot.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(mascotEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = mascot.displayName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFFB0B7CD)
                                        )
                                        if (isLocked) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "VIP Locked",
                                                tint = Color(0xFFFFDC00),
                                                modifier = Modifier.size(9.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = mascot.tagTitle,
                                        fontSize = 8.sp,
                                        color = if (isSelected) Color(0xFFFF50B4) else Color(0xFF7E8498)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ---- 5. Custom Text / Name Input ----
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D28)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .border(1.dp, Color(0xFF282B3C), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = config.text,
                    onValueChange = { onUpdateConfig(config.copy(text = it)) },
                    placeholder = { Text("type crazy tag or crew name", color = Color(0xFF7E8498), fontSize = 12.sp) },
                    trailingIcon = {
                        if (config.text.isNotEmpty()) {
                            IconButton(onClick = { onUpdateConfig(config.copy(text = "")) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear text",
                                    tint = Color(0xFF8E95AA),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF50B4),
                        unfocusedBorderColor = Color(0xFF33374C),
                        cursorColor = Color(0xFFFF50B4)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                // Quick preset tag chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(top = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetTags) { tag ->
                        val isSelected = config.text.equals(tag, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateConfig(config.copy(text = tag)) },
                            label = { Text(tag, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF50B4),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF141620),
                                labelColor = Color(0xFFB0B7CD)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFF242738),
                                selectedBorderColor = Color(0xFFFF50B4)
                            )
                        )
                    }
                }
            }
        }

        // ---- 5. Gemini AI Custom Graffiti Designer (Clean & Collapsible) ----
        GeminiGraffitiAiCard(
            currentConfig = config,
            onApplyDesign = { newConfig -> onUpdateConfig(newConfig) },
            onSwitchToDrawingCanvas = onSwitchToDrawingCanvas,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ---- 6. Save & Export Controls Bar (ONLY ABLE TO SAVE ON PAID TIER) ----
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191D2C)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .border(
                    width = 1.dp,
                    color = if (isProSubscribed) Color(0xFF00DCFF) else Color(0xFFFFDC00),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "EXPORT & BLACKBOOK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )

                    if (!isProSubscribed) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFFFDC00),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LOCKED TO PAID TIER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFDC00)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Save to Gallery
                    Button(
                        onClick = {
                            if (isProSubscribed) {
                                onSaveRequested()
                            } else {
                                onRequirePro()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isProSubscribed) Color(0xFF00DCFF) else Color(0xFFFFDC00),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_wildstyle_save_gallery")
                    ) {
                        Icon(
                            imageVector = if (isProSubscribed) Icons.Default.Save else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isProSubscribed) "SAVE ART" else "UNLOCK & SAVE",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Save to Blackbook
                    Button(
                        onClick = {
                            if (isProSubscribed) {
                                onSaveBlackbookRequested()
                            } else {
                                onRequirePro()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF50B4),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_wildstyle_save_blackbook")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BLACKBOOK",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // PDF Poster
                    OutlinedButton(
                        onClick = {
                            if (isProSubscribed) {
                                onSavePdfRequested()
                            } else {
                                onRequirePro()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFDC00)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_wildstyle_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFFFDC00),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PDF POSTER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Share
                    OutlinedButton(
                        onClick = {
                            if (isProSubscribed) {
                                onShareRequested()
                            } else {
                                onRequirePro()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00DCFF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_wildstyle_share")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF00DCFF),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SHARE PIECE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
