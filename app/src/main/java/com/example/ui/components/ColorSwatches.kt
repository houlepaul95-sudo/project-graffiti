package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColorOption
import com.example.model.WallOption
import com.example.model.WallType

@Composable
fun ColorPaletteRow(
    title: String,
    colors: List<ColorOption>,
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = Color(0xFFB0B5C6),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            itemsIndexed(colors) { index, option ->
                val isSelected = index == selectedIndex
                ColorSwatchItem(
                    name = option.name,
                    color = option.color,
                    isSelected = isSelected,
                    onClick = { onColorSelected(index) }
                )
            }
        }
    }
}

@Composable
fun WallPaletteRow(
    walls: List<WallOption>,
    selectedIndex: Int,
    onWallSelected: (Int) -> Unit,
    isProSubscribed: Boolean = false,
    onShowPaywall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "WALL / BACKGROUND",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = Color(0xFFB0B5C6),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            itemsIndexed(walls) { index, wall ->
                val isSelected = index == selectedIndex
                val isLocked = !isProSubscribed && wall.isPro
                WallSwatchItem(
                    name = wall.name,
                    color = wall.color,
                    isTransparent = wall.type == WallType.TRANSPARENT,
                    isSelected = isSelected,
                    isLocked = isLocked,
                    onClick = {
                        if (isLocked) {
                            onShowPaywall()
                        } else {
                            onWallSelected(index)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorSwatchItem(
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color.White else Color(0xFF333748)
    val borderWidth = if (isSelected) 3.dp else 1.5.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .border(borderWidth, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .testTag("color_swatch_$name")
    ) {
        if (isSelected) {
            val checkColor = if (color == Color.White || color == Color(0xFFFFDC00) || color == Color(0xFF00DCFF) || color == Color(0xFF8CFF50)) {
                Color.Black
            } else {
                Color.White
            }
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "$name selected",
                tint = checkColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WallSwatchItem(
    name: String,
    color: Color,
    isTransparent: Boolean,
    isSelected: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF00DCFF) else if (isLocked) Color(0xFFFFDC00).copy(alpha = 0.6f) else Color(0xFF333748)
    val borderWidth = if (isSelected) 2.5.dp else 1.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isTransparent) Color(0xFF262834) else color)
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("wall_swatch_$name")
    ) {
        if (isTransparent) {
            Text(
                text = "PNG",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (isLocked) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Pro Wall Locked",
                    tint = Color(0xFFFFDC00),
                    modifier = Modifier.size(16.dp)
                )
            }
        } else if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "$name selected",
                tint = if (color == Color(0xFFF0F0F0)) Color.Black else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
