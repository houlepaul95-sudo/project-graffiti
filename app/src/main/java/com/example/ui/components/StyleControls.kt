package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.model.GraffitiConfig
import com.example.model.GraffitiFontStyle

/**
 * Quick Action Buttons matching the user's original Android Java layout:
 * Row 1: [COLOR] [OUTLINE] [WALL] [LEAN]
 * Row 2: [SPLATS] [RANDOM] [SAVE]
 */
@Composable
fun QuickActionButtons(
    onCycleColor: () -> Unit,
    onCycleOutline: () -> Unit,
    onCycleWall: () -> Unit,
    onToggleLean: () -> Unit,
    onToggleSplats: () -> Unit,
    onRandomize: () -> Unit,
    onSave: () -> Unit,
    isLean: Boolean,
    hasSplats: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        // Row 1: COLOR, OUTLINE, WALL, LEAN
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StreetButton(
                label = "COLOR",
                icon = Icons.Default.FormatColorFill,
                onClick = onCycleColor,
                modifier = Modifier.weight(1f).testTag("quick_btn_color")
            )
            StreetButton(
                label = "OUTLINE",
                icon = Icons.Default.Layers,
                onClick = onCycleOutline,
                modifier = Modifier.weight(1f).testTag("quick_btn_outline")
            )
            StreetButton(
                label = "WALL",
                icon = Icons.Default.InvertColors,
                onClick = onCycleWall,
                modifier = Modifier.weight(1f).testTag("quick_btn_wall")
            )
            StreetButton(
                label = "LEAN",
                icon = Icons.Default.TextFields,
                onClick = onToggleLean,
                isActive = isLean,
                modifier = Modifier.weight(1f).testTag("quick_btn_lean")
            )
        }

        // Row 2: SPLATS, RANDOM, SAVE
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StreetButton(
                label = "SPLATS",
                icon = Icons.Default.FormatPaint,
                onClick = onToggleSplats,
                isActive = hasSplats,
                modifier = Modifier.weight(1f).testTag("quick_btn_splats")
            )
            StreetButton(
                label = "RANDOM",
                icon = Icons.Default.Shuffle,
                onClick = onRandomize,
                accentColor = Color(0xFFFF50B4),
                modifier = Modifier.weight(1f).testTag("quick_btn_random")
            )
            StreetButton(
                label = "SAVE",
                icon = Icons.Default.Save,
                onClick = onSave,
                accentColor = Color(0xFF00DCFF),
                modifier = Modifier.weight(1f).testTag("quick_btn_save")
            )
        }
    }
}

@Composable
fun StreetButton(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    isActive: Boolean = false,
    accentColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val bg = when {
        accentColor != null -> accentColor
        isActive -> Color(0xFF353950)
        else -> Color(0xFF222533)
    }
    val contentColor = when {
        accentColor != null -> if (accentColor == Color(0xFF00DCFF)) Color.Black else Color.White
        isActive -> Color(0xFF00DCFF)
        else -> Color.White
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, if (isActive) Color(0xFF00DCFF) else Color(0xFF323648), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = " $label",
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun FontStyleSelector(
    selectedStyle: GraffitiFontStyle,
    onStyleSelected: (GraffitiFontStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = "GRAFFITI FONTS & LETTER STYLES",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color(0xFFB0B5C6)
            )
            Text(
                text = selectedStyle.styleCategory,
                fontSize = 11.sp,
                color = Color(0xFF00DCFF),
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            items(GraffitiFontStyle.values()) { style ->
                val isSelected = style == selectedStyle
                val cardBorderColor = if (isSelected) Color(0xFF00DCFF) else Color(0xFF2C3042)
                val cardBg = if (isSelected) Color(0xFF1E2333) else Color(0xFF161822)

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(185.dp)
                        .border(if (isSelected) 2.dp else 1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                        .clickable { onStyleSelected(style) }
                        .testTag("font_style_${style.name}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = style.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color(0xFF00DCFF) else Color.White
                            )
                            if (isSelected) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00DCFF)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = style.styleCategory.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF50B4),
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Text(
                            text = style.description,
                            fontSize = 11.sp,
                            color = Color(0xFF8E95AA),
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SlidersAndEffects(
    config: GraffitiConfig,
    onOutlineThicknessChange: (Float) -> Unit,
    onShadowDepthChange: (Float) -> Unit,
    onToggleDrips: () -> Unit,
    onToggleHighlights: () -> Unit,
    onToggleGradient: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        // Outline Thickness Slider
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191B24)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "OUTLINE THICKNESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9EA3B5)
                    )
                    Text(
                        text = "${config.outlineThickness.toInt()} px",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00DCFF)
                    )
                }
                Slider(
                    value = config.outlineThickness,
                    onValueChange = onOutlineThicknessChange,
                    valueRange = 4f..28f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00DCFF),
                        activeTrackColor = Color(0xFF00DCFF)
                    ),
                    modifier = Modifier.testTag("slider_outline_thickness")
                )
            }
        }

        // 3D Shadow Depth Slider
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191B24)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "3D BLOCK DEPTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9EA3B5)
                    )
                    Text(
                        text = "${config.shadowDepth.toInt()} px",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF50B4)
                    )
                }
                Slider(
                    value = config.shadowDepth,
                    onValueChange = onShadowDepthChange,
                    valueRange = 2f..28f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF50B4),
                        activeTrackColor = Color(0xFFFF50B4)
                    ),
                    modifier = Modifier.testTag("slider_shadow_depth")
                )
            }
        }

        // Toggles: Drips, Highlights, Gradient
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            EffectToggleCard(
                title = "PAINT DRIPS",
                checked = config.drips,
                onCheckedChange = { onToggleDrips() },
                modifier = Modifier.weight(1f).testTag("toggle_drips")
            )
            EffectToggleCard(
                title = "3D SHINE",
                checked = config.highlights,
                onCheckedChange = { onToggleHighlights() },
                modifier = Modifier.weight(1f).testTag("toggle_highlights")
            )
            EffectToggleCard(
                title = "GRADIENT",
                checked = config.isGradient,
                onCheckedChange = { onToggleGradient() },
                modifier = Modifier.weight(1f).testTag("toggle_gradient")
            )
        }
    }
}

@Composable
private fun EffectToggleCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (checked) Color(0xFF222634) else Color(0xFF161820)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .border(1.dp, if (checked) Color(0xFF00DCFF) else Color(0xFF2B2F40), RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp).fillMaxWidth()
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (checked) Color.White else Color(0xFF7E8498)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00DCFF),
                    uncheckedThumbColor = Color(0xFF5E6476),
                    uncheckedTrackColor = Color(0xFF1E202B)
                ),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
