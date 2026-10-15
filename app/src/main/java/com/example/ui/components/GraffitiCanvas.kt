package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Text
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GraffitiRenderer
import com.example.model.GraffitiConfig
import com.example.model.MascotCharacter

@Composable
fun GraffitiCanvas(
    config: GraffitiConfig,
    onRandomize: () -> Unit,
    onReshuffleSplats: () -> Unit,
    isFullscreen: Boolean = false,
    onToggleFullscreen: (() -> Unit)? = null,
    onCycleMascot: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(1f) }

    // Bounce animation when randomized or config changes
    LaunchedEffect(config.splatSeed, config.fillColorIndex) {
        scaleAnim.animateTo(
            targetValue = 1.02f,
            animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
        )
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
        )
    }

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFF2A2D3E), RoundedCornerShape(16.dp))
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .scale(scaleAnim.value)
            .testTag("graffiti_canvas_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("graffiti_canvas")
        ) {
            val canvasWidth = size.width.toInt()
            val canvasHeight = size.height.toInt()
            if (canvasWidth > 0 && canvasHeight > 0) {
                GraffitiRenderer.drawGraffiti(
                    canvas = drawContext.canvas.nativeCanvas,
                    width = canvasWidth,
                    height = canvasHeight,
                    config = config,
                    isPreview = true,
                    context = context
                )
            }
        }

        // Normal View / Fullscreen toggle button
        if (onToggleFullscreen != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Surface(
                    color = Color(0xDD141724),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier.border(1.dp, Color(0xFF00DCFF).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onToggleFullscreen() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("toggle_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Toggle normal full view",
                            tint = Color(0xFF00DCFF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFullscreen) "STUDIO VIEW" else "NORMAL FULL VIEW",
                            fontSize = 9.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Character Add-On button on canvas
        if (onCycleMascot != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Surface(
                    color = Color(0xDD141724),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier.border(
                        1.dp,
                        if (config.mascot != MascotCharacter.NONE) Color(0xFFFF50B4).copy(alpha = 0.8f) else Color(0xFF454B64),
                        RoundedCornerShape(8.dp)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onCycleMascot() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("canvas_character_addon_button")
                    ) {
                        val mascotEmoji = when (config.mascot) {
                            MascotCharacter.NONE -> "➕"
                            MascotCharacter.SPRAY_CAN -> "🧴"
                            MascotCharacter.ALLEY_RAT -> "🐀"
                            MascotCharacter.MECH_BOT -> "🤖"
                            MascotCharacter.ASTRO_CADET -> "👨‍🚀"
                            MascotCharacter.STREET_BULLDOG -> "🐶"
                        }
                        Text(
                            text = mascotEmoji,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (config.mascot == MascotCharacter.NONE) "ADD CHARACTER" else config.mascot.displayName.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (config.mascot != MascotCharacter.NONE) Color(0xFFFF50B4) else Color(0xFFC7CBD8)
                        )
                    }
                }
            }
        }

        // Floating quick actions on bottom right of the canvas
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            Surface(
                color = Color(0xCC12131A),
                shape = CircleShape,
                shadowElevation = 6.dp
            ) {
                FilledIconButton(
                    onClick = onRandomize,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFFF50B4),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("quick_randomize_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Randomize Graffiti",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Floating spray splat re-shuffle button on top right of the canvas
        if (config.splats) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Surface(
                    color = Color(0xCC12131A),
                    shape = CircleShape,
                    shadowElevation = 6.dp
                ) {
                    FilledIconButton(
                        onClick = onReshuffleSplats,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF00DCFF),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("quick_splat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatPaint,
                            contentDescription = "Reshuffle Paint Splats",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
