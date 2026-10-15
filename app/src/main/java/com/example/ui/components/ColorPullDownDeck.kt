package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColorOption
import com.example.model.GraffitiConfig
import com.example.model.GraffitiFontStyle
import com.example.model.GraffitiPalettes
import com.example.model.MascotCharacter
import com.example.model.MascotPosition
import com.example.model.SignatureMasterStyle
import com.example.model.WallOption
import com.example.model.WallType

enum class ColorSubCategory(val label: String, val flairTitle: String, val subtitle: String) {
    FILL("FAT CAP FILL", "FAT CAP FILL • PRIMARY STREET HUE", "Base fill color for your letter piece"),
    GRADIENT("TWO-TONE FADE", "TWO-TONE BURN • DUAL SPRAY GRADIENT", "Bottom blend for authentic subway style"),
    OUTLINE("BUBBLE CRUST", "BUBBLE CRUST • THROWIE OUTLINE", "Crisp contour border framing the piece"),
    WALL("WALL BOMBS", "UNDERGROUND SPOTS • WALL BOMBS", "Canvas texture & urban alley backdrops")
}

/**
 * An organized, pull-down tab control deck that keeps the graffiti artwork
 * visible at all times above it, preventing the art from scrolling off-page.
 */
@Composable
fun ColorPullDownDeck(
    config: GraffitiConfig,
    onFillColorSelected: (Int) -> Unit,
    onGradientColorSelected: (Int) -> Unit,
    onOutlineColorSelected: (Int) -> Unit,
    onWallSelected: (Int) -> Unit,
    onFontStyleSelected: (GraffitiFontStyle) -> Unit,
    onOutlineThicknessChange: (Float) -> Unit,
    onShadowDepthChange: (Float) -> Unit,
    onToggleDrips: () -> Unit,
    onToggleHighlights: () -> Unit,
    onToggleGradient: () -> Unit,
    onToggleLean: () -> Unit,
    onToggleSplats: () -> Unit,
    onReshuffleSplats: () -> Unit,
    onMasterStyleSelected: (SignatureMasterStyle) -> Unit = {},
    onMascotSelected: (MascotCharacter) -> Unit = {},
    onMascotPositionSelected: (MascotPosition) -> Unit = {},
    onToggleVerticalLayout: () -> Unit = {},
    isProSubscribed: Boolean = false,
    onShowPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 0: COLORWAY VAULT, 1: TAG FONTS, 2: WET DRIP LAB
    var mainTab by remember { mutableIntStateOf(0) }
    var colorSubTab by remember { mutableStateOf(ColorSubCategory.FILL) }
    var isColorMenuExpanded by remember { mutableStateOf(false) }
    var isDeckCollapsed by remember { mutableStateOf(false) }

    val deckScrollState = rememberScrollState()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13151E)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF262A3B), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ---- PULL-DOWN HANDLE & QUICK STATUS HEADER ----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDeckCollapsed = !isDeckCollapsed }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(GraffitiPalettes.FILLS.getOrNull(config.fillColorIndex)?.color ?: Color(0xFF00DCFF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDeckCollapsed) "▲ SLIDE UP COLOR DECK" else "▼ PULL DOWN PALETTES",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color(0xFF00DCFF)
                    )
                }

                // Swatch preview summary capsules
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Fill preview dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(GraffitiPalettes.FILLS.getOrNull(config.fillColorIndex)?.color ?: Color.Cyan)
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    )
                    if (config.isGradient) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(GraffitiPalettes.FILLS.getOrNull(config.gradientColorIndex)?.color ?: Color.Magenta)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(GraffitiPalettes.OUTLINES.getOrNull(config.outlineColorIndex)?.color ?: Color.White)
                            .border(1.dp, Color.Black.copy(alpha = 0.5f), CircleShape)
                    )

                    Icon(
                        imageVector = if (isDeckCollapsed) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle color deck",
                        tint = Color(0xFF8E95AA),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!isDeckCollapsed) {
                // ---- MAIN STREET TABS PILL BAR (4 TABS) ----
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    MainTabPill(
                        label = "STYLES",
                        icon = Icons.Default.AutoAwesome,
                        isSelected = mainTab == 0,
                        onClick = { mainTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    MainTabPill(
                        label = "COLORS",
                        icon = Icons.Default.ColorLens,
                        isSelected = mainTab == 1,
                        onClick = { mainTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    MainTabPill(
                        label = "FONTS",
                        icon = Icons.Default.TextFields,
                        isSelected = mainTab == 2,
                        onClick = { mainTab = 2 },
                        modifier = Modifier.weight(1f)
                    )
                    MainTabPill(
                        label = "FX / DRIP",
                        icon = Icons.Default.WaterDrop,
                        isSelected = mainTab == 3,
                        onClick = { mainTab = 3 },
                        modifier = Modifier.weight(1f)
                    )
                }

                // ---- INTERNALLY SCROLLING CONTENT DECK ----
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(deckScrollState)
                        .padding(bottom = 12.dp)
                ) {
                    when (mainTab) {
                        0 -> {
                            // ---- 5 SIGNATURE MASTER STYLES & CREW MASCOTS ----
                            MasterStylesAndCrewTab(
                                config = config,
                                onSelectMasterStyle = onMasterStyleSelected,
                                onSelectMascot = onMascotSelected,
                                onSelectMascotPosition = onMascotPositionSelected,
                                onToggleVerticalLayout = onToggleVerticalLayout,
                                isProSubscribed = isProSubscribed,
                                onShowPaywall = onShowPaywall
                            )
                        }
                        1 -> {
                            // ---- COLORWAY VAULT WITH PULL-DOWN DROPDOWN TAB ----
                            ColorwayVaultTab(
                                config = config,
                                currentSubCategory = colorSubTab,
                                onSubCategoryChange = { colorSubTab = it },
                                isMenuExpanded = isColorMenuExpanded,
                                onToggleMenu = { isColorMenuExpanded = !isColorMenuExpanded },
                                onFillColorSelected = onFillColorSelected,
                                onGradientColorSelected = onGradientColorSelected,
                                onOutlineColorSelected = onOutlineColorSelected,
                                onWallSelected = onWallSelected,
                                onToggleGradient = onToggleGradient,
                                onOutlineThicknessChange = onOutlineThicknessChange,
                                isProSubscribed = isProSubscribed,
                                onShowPaywall = onShowPaywall
                            )
                        }
                        2 -> {
                            // ---- TAG FONTS TAB ----
                            TagTypographyTab(
                                currentStyle = config.fontStyle,
                                onSelectStyle = onFontStyleSelected,
                                isLean = config.lean,
                                onToggleLean = onToggleLean,
                                isProSubscribed = isProSubscribed,
                                onShowPaywall = onShowPaywall
                            )
                        }
                        3 -> {
                            // ---- FX & DRIPS LAB ----
                            WetDripLabTab(
                                config = config,
                                onOutlineThicknessChange = onOutlineThicknessChange,
                                onShadowDepthChange = onShadowDepthChange,
                                onToggleDrips = onToggleDrips,
                                onToggleHighlights = onToggleHighlights,
                                onToggleGradient = onToggleGradient,
                                onToggleSplats = onToggleSplats,
                                onReshuffleSplats = onReshuffleSplats
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainTabPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) Color(0xFF00DCFF) else Color(0xFF1E212D)
    val contentColor = if (isSelected) Color.Black else Color(0xFFC7CBD8)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, if (isSelected) Color(0xFF00DCFF) else Color(0xFF2C3042), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                color = contentColor
            )
        }
    }
}

/**
 * The pull-down color selector tab containing Base Fills, Gradient Burn,
 * Outline Crust, and Wall Bombs.
 */
@Composable
private fun ColorwayVaultTab(
    config: GraffitiConfig,
    currentSubCategory: ColorSubCategory,
    onSubCategoryChange: (ColorSubCategory) -> Unit,
    isMenuExpanded: Boolean,
    onToggleMenu: () -> Unit,
    onFillColorSelected: (Int) -> Unit,
    onGradientColorSelected: (Int) -> Unit,
    onOutlineColorSelected: (Int) -> Unit,
    onWallSelected: (Int) -> Unit,
    onToggleGradient: () -> Unit,
    onOutlineThicknessChange: (Float) -> Unit,
    isProSubscribed: Boolean = false,
    onShowPaywall: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp)) {

        // Pull-Down Dropdown Pill Trigger
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E2B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00DCFF).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .clickable { onToggleMenu() }
                    .testTag("color_pulldown_trigger")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PULL-DOWN COLOR CATEGORY",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8E95AA),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = currentSubCategory.flairTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00DCFF)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF262A3B)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "SELECT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = Color(0xFF00DCFF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Pull-down Menu
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = onToggleMenu,
                modifier = Modifier
                    .background(Color(0xFF181B26))
                    .border(1.dp, Color(0xFF2C3144), RoundedCornerShape(8.dp))
            ) {
                ColorSubCategory.values().forEach { category ->
                    val isChosen = category == currentSubCategory
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = category.flairTitle,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isChosen) FontWeight.Black else FontWeight.Bold,
                                    color = if (isChosen) Color(0xFF00DCFF) else Color.White
                                )
                                Text(
                                    text = category.subtitle,
                                    fontSize = 10.sp,
                                    color = Color(0xFF8E95AA)
                                )
                            }
                        },
                        trailingIcon = {
                            if (isChosen) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF00DCFF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        onClick = {
                            onSubCategoryChange(category)
                            onToggleMenu()
                        },
                        modifier = Modifier.testTag("dropdown_item_${category.name}")
                    )
                }
            }
        }

        // Quick Sub-Category Pill Bar for 1-tap switching without opening menu
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            ColorSubCategory.values().forEach { cat ->
                val isSelected = cat == currentSubCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onSubCategoryChange(cat) },
                    label = {
                        Text(
                            text = cat.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF50B4),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1C1F2E),
                        labelColor = Color(0xFF9EACB5)
                    ),
                    modifier = Modifier.weight(1f).testTag("pill_${cat.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Selected Color Sub-Category Content
        when (currentSubCategory) {
            ColorSubCategory.FILL -> {
                val currentOpt = GraffitiPalettes.FILLS.getOrNull(config.fillColorIndex)
                ActiveColorBanner(
                    tag = "FAT CAP BASE FILL",
                    colorName = currentOpt?.name ?: "Electric Cyan",
                    color = currentOpt?.color ?: Color(0xFF00DCFF)
                )

                Spacer(modifier = Modifier.height(6.dp))

                ColorSwatchesGrid(
                    colors = GraffitiPalettes.FILLS,
                    selectedIndex = config.fillColorIndex,
                    onColorSelected = onFillColorSelected,
                    tagPrefix = "fill"
                )
            }
            ColorSubCategory.GRADIENT -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Column {
                        Text(
                            text = "TWO-TONE BURN (GRADIENT)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF50B4)
                        )
                        Text(
                            text = if (config.isGradient) "Dual-color bottom blend active" else "Single flat color only",
                            fontSize = 10.sp,
                            color = Color(0xFF8E95AA)
                        )
                    }
                    Switch(
                        checked = config.isGradient,
                        onCheckedChange = { onToggleGradient() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFF50B4),
                            uncheckedThumbColor = Color(0xFF5E6476),
                            uncheckedTrackColor = Color(0xFF1E202B)
                        ),
                        modifier = Modifier.size(36.dp).testTag("deck_toggle_gradient")
                    )
                }

                if (config.isGradient) {
                    val currentGrad = GraffitiPalettes.FILLS.getOrNull(config.gradientColorIndex)
                    ActiveColorBanner(
                        tag = "BOTTOM BLEND COLOR",
                        colorName = currentGrad?.name ?: "Hot Pink",
                        color = currentGrad?.color ?: Color(0xFFFF50B4)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    ColorSwatchesGrid(
                        colors = GraffitiPalettes.FILLS,
                        selectedIndex = config.gradientColorIndex,
                        onColorSelected = onGradientColorSelected,
                        tagPrefix = "gradient"
                    )
                }
            }
            ColorSubCategory.OUTLINE -> {
                val currentOut = GraffitiPalettes.OUTLINES.getOrNull(config.outlineColorIndex)
                ActiveColorBanner(
                    tag = "THROWIE OUTLINE CRUST",
                    colorName = currentOut?.name ?: "White",
                    color = currentOut?.color ?: Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                ColorSwatchesGrid(
                    colors = GraffitiPalettes.OUTLINES,
                    selectedIndex = config.outlineColorIndex,
                    onColorSelected = onOutlineColorSelected,
                    tagPrefix = "outline"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Inline Outline Thickness Slider
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181B26)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF282C3D), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "OUTLINE STROKE SPREAD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB0B5C6)
                            )
                            Text(
                                text = "${config.outlineThickness.toInt()} px",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
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
                            modifier = Modifier.testTag("deck_slider_outline")
                        )
                    }
                }
            }
            ColorSubCategory.WALL -> {
                val currentWall = GraffitiPalettes.WALLS.getOrNull(config.wallIndex)
                ActiveColorBanner(
                    tag = if (currentWall?.isPro == true) "VIP STREET WALL • PRO PASS" else "ACTIVE STREET BACKDROP",
                    colorName = currentWall?.name ?: "Night Slate",
                    color = currentWall?.color ?: Color(0xFF1E2030)
                )

                Spacer(modifier = Modifier.height(6.dp))

                WallPaletteRow(
                    walls = GraffitiPalettes.WALLS,
                    selectedIndex = config.wallIndex,
                    onWallSelected = onWallSelected,
                    isProSubscribed = isProSubscribed,
                    onShowPaywall = onShowPaywall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ActiveColorBanner(
    tag: String,
    colorName: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF191C29))
            .border(1.dp, Color(0xFF2B2F42), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = tag,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7E8498)
                )
                Text(
                    text = colorName.uppercase(),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        Text(
            text = "TAP SWATCH TO APPLY",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00DCFF)
        )
    }
}

/**
 * A responsive 2-row horizontal scroll of colors with active selection styling.
 */
@Composable
private fun ColorSwatchesGrid(
    colors: List<ColorOption>,
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit,
    tagPrefix: String
) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(colors) { index, option ->
            val isSelected = index == selectedIndex
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF262B3D) else Color(0xFF161822)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFF00DCFF) else Color(0xFF25293A),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onColorSelected(index) }
                    .testTag("${tagPrefix}_swatch_$index")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(option.color)
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = if (option.color == Color.White) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = option.name,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) Color(0xFF00DCFF) else Color(0xFFB0B5C6),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Tag fonts tab featuring Sister Spray, Most Wasted, Don Graffiti, and Urban Jungle.
 */
@Composable
private fun TagTypographyTab(
    currentStyle: GraffitiFontStyle,
    onSelectStyle: (GraffitiFontStyle) -> Unit,
    isLean: Boolean,
    onToggleLean: () -> Unit,
    isProSubscribed: Boolean = false,
    onShowPaywall: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
            Text(
                text = "AUTHENTIC GRAFFITI TYPEFACES",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = Color(0xFF00DCFF)
            )

            // Street slant / lean toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isLean) Color(0xFF252A3D) else Color(0xFF161822))
                    .border(1.dp, if (isLean) Color(0xFF00DCFF) else Color(0xFF2C3042), RoundedCornerShape(6.dp))
                    .clickable { onToggleLean() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ITALIC SLANT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLean) Color(0xFF00DCFF) else Color(0xFF8E95AA)
                )
            }
        }

        // Font Cards
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GraffitiFontStyle.values().forEach { style ->
                val isSelected = style == currentStyle
                val isLocked = !isProSubscribed && style == GraffitiFontStyle.DON_GRAFFITI

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1B2030) else Color(0xFF151722)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else if (isLocked) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF00DCFF) else if (isLocked) Color(0xFFFFDC00).copy(alpha = 0.6f) else Color(0xFF282C3D),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (isLocked) {
                                onShowPaywall()
                            } else {
                                onSelectStyle(style)
                            }
                        }
                        .testTag("deck_font_${style.name}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = style.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color(0xFF00DCFF) else if (isLocked) Color(0xFFFFDC00) else Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isLocked) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDC00)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "PRO PASS",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                } else {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFFFF50B4) else Color(0xFF242738)
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = style.styleCategory.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isLocked) "${style.description} • Requires Pro Pass ($9.99/yr)" else style.description,
                                fontSize = 11.5.sp,
                                color = if (isLocked) Color(0xFFFFDC00).copy(alpha = 0.8f) else Color(0xFF8E95AA)
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF00DCFF) else if (isLocked) Color(0xFFFFDC00) else Color(0xFF242839)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isSelected) "ACTIVE" else if (isLocked) "UNLOCK" else "APPLY",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected || isLocked) Color.Black else Color(0xFFB0B5C6),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Wet Drips, 3D Block Extrusion, Highlights and Spray Effects tab.
 */
@Composable
private fun WetDripLabTab(
    config: GraffitiConfig,
    onOutlineThicknessChange: (Float) -> Unit,
    onShadowDepthChange: (Float) -> Unit,
    onToggleDrips: () -> Unit,
    onToggleHighlights: () -> Unit,
    onToggleGradient: () -> Unit,
    onToggleSplats: () -> Unit,
    onReshuffleSplats: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = "WET PAINT & SPRAY EFFECTS LAB",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = Color(0xFF00DCFF)
        )

        // 3D Shadow Depth Slider
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B26)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF282C3D), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "3D BLOCK EXTRUSION DEPTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB0B5C6)
                    )
                    Text(
                        text = "${config.shadowDepth.toInt()} px",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
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
                    modifier = Modifier.testTag("deck_slider_shadow")
                )
            }
        }

        // Toggle Grid (Drips, 3D Shine, Splats)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DeckToggleCard(
                title = "WET DRIPS",
                checked = config.drips,
                onCheckedChange = { onToggleDrips() },
                modifier = Modifier.weight(1f).testTag("deck_toggle_drips")
            )
            DeckToggleCard(
                title = "3D SHINE",
                checked = config.highlights,
                onCheckedChange = { onToggleHighlights() },
                modifier = Modifier.weight(1f).testTag("deck_toggle_highlights")
            )
            DeckToggleCard(
                title = "OVERSPRAY",
                checked = config.splats,
                onCheckedChange = { onToggleSplats() },
                modifier = Modifier.weight(1f).testTag("deck_toggle_splats")
            )
        }

        // Reshuffle Splatters button
        Button(
            onClick = onReshuffleSplats,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF202434)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF33384D), RoundedCornerShape(8.dp))
                .testTag("deck_reshuffle_splats")
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = null,
                tint = Color(0xFF00DCFF),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WILDCARD • RESHUFFLE DRIP & SPLAT SEED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00DCFF)
            )
        }
    }
}

@Composable
private fun DeckToggleCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (checked) Color(0xFF222636) else Color(0xFF151722)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .border(1.dp, if (checked) Color(0xFF00DCFF) else Color(0xFF2A2E3F), RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp).fillMaxWidth()
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

@Composable
fun MasterStylesAndCrewTab(
    config: GraffitiConfig,
    onSelectMasterStyle: (SignatureMasterStyle) -> Unit,
    onSelectMascot: (MascotCharacter) -> Unit,
    onSelectMascotPosition: (MascotPosition) -> Unit,
    onToggleVerticalLayout: () -> Unit,
    isProSubscribed: Boolean,
    onShowPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Section 1 Header: 5 Master Styles
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "5 SIGNATURE MASTER STYLES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color(0xFF00DCFF)
                )
                Text(
                    text = "Authentic street aesthetics complete with character mascot",
                    fontSize = 10.sp,
                    color = Color(0xFF8E95AA)
                )
            }

            if (!isProSubscribed) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2416)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onShowPaywall() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFFDC00),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "UNLOCK VIP",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFDC00)
                        )
                    }
                }
            }
        }

        // 5 Master Styles List Cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(GraffitiPalettes.SIGNATURE_MASTER_STYLES) { style ->
                val isSelected = config.masterStyleId == style.id
                val isLocked = style.isPro && !isProSubscribed
                val cardBorder = when {
                    isSelected -> style.accentColor
                    isLocked -> Color(0xFF383228)
                    else -> Color(0xFF262A3B)
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1B1E2D) else Color(0xFF151722)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(225.dp)
                        .border(if (isSelected) 2.dp else 1.dp, cardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            if (isLocked) {
                                onShowPaywall()
                            } else {
                                onSelectMasterStyle(style)
                            }
                        }
                        .testTag("style_card_${style.id}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Visual Palette Preview Bar
                        val fillColor = GraffitiPalettes.FILLS.getOrElse(style.fillColorIndex) { GraffitiPalettes.FILLS[0] }.color
                        val gradColor = GraffitiPalettes.FILLS.getOrElse(style.gradientColorIndex) { GraffitiPalettes.FILLS[0] }.color
                        val outlineColor = GraffitiPalettes.OUTLINES.getOrElse(style.outlineColorIndex) { GraffitiPalettes.OUTLINES[0] }.color

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(fillColor, gradColor, outlineColor)))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Title & Badge Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = style.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) style.accentColor else Color.White
                            )

                            when {
                                isSelected -> {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = style.accentColor),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                isLocked -> {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2416)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFFFFDC00),
                                                modifier = Modifier.size(9.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "VIP",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFFFDC00)
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16282E)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "FREE",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF00DCFF),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = style.subtitle.uppercase(),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = style.accentColor,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Text(
                            text = style.description,
                            fontSize = 10.5.sp,
                            color = Color(0xFF8E95AA),
                            lineHeight = 13.5.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Subscription block badge if locked
                        if (isLocked) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2416)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .border(1.dp, Color(0xFFFFDC00).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Subscription Blocked",
                                        tint = Color(0xFFFFDC00),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "VIP SUBSCRIPTION BLOCKED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFDC00),
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }
                        }

                        // Mascot Tag capsule
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF202334))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "MASCOT: ${style.mascot.displayName.uppercase()}",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFDC00)
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Companion Crew Characters Selector
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "COMPANION CREW CHARACTERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color(0xFFFF50B4)
            )
            Text(
                text = "Add spraycan, alley rat, mech bot, astronaut, or bulldog stickers",
                fontSize = 10.sp,
                color = Color(0xFF8E95AA)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MascotCharacter.values()) { mascot ->
                    val isSelected = config.mascot == mascot
                    val isLocked = mascot.isPro && !isProSubscribed

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isLocked) {
                                onShowPaywall()
                            } else {
                                onSelectMascot(mascot)
                            }
                        },
                        leadingIcon = {
                            if (isLocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFFFDC00),
                                    modifier = Modifier.size(12.dp)
                                )
                            } else if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = if (mascot == MascotCharacter.NONE) "NO MASCOT" else "${mascot.displayName} (${mascot.badge})",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF50B4),
                            selectedLabelColor = Color.White,
                            containerColor = if (isLocked) Color(0xFF231E18) else Color(0xFF1B1E2B),
                            labelColor = if (isLocked) Color(0xFFC09858) else Color(0xFFB0B5C6)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(0xFFFF50B4) else if (isLocked) Color(0xFF4C3E20) else Color(0xFF2C3044)
                        ),
                        modifier = Modifier.testTag("mascot_chip_${mascot.id}")
                    )
                }
            }
        }

        // Section 3: Mascot Placement & Layout Mode
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Mascot Position
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).border(1.dp, Color(0xFF2A2E3E), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "MASCOT POSITION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8E95AA)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(MascotPosition.RIGHT, MascotPosition.LEFT, MascotPosition.TOP).forEach { pos ->
                            val isPos = config.mascotPosition == pos
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isPos) Color(0xFF00DCFF) else Color(0xFF242738))
                                    .clickable { onSelectMascotPosition(pos) }
                                    .padding(vertical = 5.dp)
                            ) {
                                Text(
                                    text = pos.name.take(1) + pos.name.drop(1).lowercase(),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isPos) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Layout Orientation (Horizontal burner vs Vertical stacked column like reference photo)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).border(1.dp, Color(0xFF2A2E3E), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "STREET ORIENTATION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8E95AA)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!config.isVerticalLayout) Color(0xFFFF50B4) else Color(0xFF242738))
                                .clickable { if (config.isVerticalLayout) onToggleVerticalLayout() }
                                .padding(vertical = 5.dp)
                        ) {
                            Text(
                                text = "Horizontal",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                color = if (!config.isVerticalLayout) Color.White else Color(0xFF8E95AA)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (config.isVerticalLayout) Color(0xFFFF50B4) else Color(0xFF242738))
                                .clickable { if (!config.isVerticalLayout) onToggleVerticalLayout() }
                                .padding(vertical = 5.dp)
                        ) {
                            Text(
                                text = "Vertical Stack",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                color = if (config.isVerticalLayout) Color.White else Color(0xFF8E95AA)
                            )
                        }
                    }
                }
            }
        }
    }
}
