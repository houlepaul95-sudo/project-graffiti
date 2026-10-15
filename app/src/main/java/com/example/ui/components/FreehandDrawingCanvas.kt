package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GraffitiBrushEngine
import com.example.engine.GraffitiRenderer
import com.example.engine.MascotRenderer
import com.example.model.BrushType
import com.example.model.DrawPoint
import com.example.model.DripDrop
import com.example.model.FreehandStroke
import com.example.model.GraffitiConfig
import com.example.model.GraffitiPalettes
import com.example.model.MascotCharacter
import com.example.model.StreetStencil
import com.example.model.WallOption
import com.example.model.WallType
import kotlin.math.hypot

@Composable
fun FreehandDrawingCanvas(
    isProSubscribed: Boolean,
    onSaveRequested: (Bitmap, WallType) -> Unit,
    onRequirePro: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Active brush settings
    var selectedBrush by remember { mutableStateOf(BrushType.SPRAY) }
    var selectedStencil by remember { mutableStateOf<StreetStencil?>(null) }
    var selectedMascot by remember { mutableStateOf<MascotCharacter?>(null) }
    var selectedColorIndex by remember { mutableIntStateOf(0) } // Default Neon Pink
    var selectedColor by remember { mutableStateOf(GraffitiPalettes.FILLS[0].color) }
    var strokeWidth by remember { mutableFloatStateOf(BrushType.SPRAY.defaultSize) }
    var dripLengthMultiplier by remember { mutableFloatStateOf(1f) }
    var selectedWallType by remember { mutableStateOf(WallType.RED_BRICK) }

    // Canvas size
    var canvasSize by remember { mutableStateOf(IntSize(1080, 800)) }

    // Bitmaps: drawing layer bitmap
    var drawingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var drawingCanvas by remember { mutableStateOf<Canvas?>(null) }

    // Stroke history for undo/redo
    val undoStack = remember { mutableStateListOf<FreehandStroke>() }
    val redoStack = remember { mutableStateListOf<FreehandStroke>() }

    // Temporary working state for active stroke being drawn
    var currentStrokePoints = remember { mutableStateListOf<DrawPoint>() }
    var currentStrokeDrips = remember { mutableStateListOf<DripDrop>() }
    var lastDripDistance by remember { mutableFloatStateOf(0f) }

    // Redraw counter to trigger Compose recomposition when drawing on native bitmap
    var renderTrigger by remember { mutableIntStateOf(0) }

    // Initialize or resize bitmap when canvasSize changes
    LaunchedEffect(canvasSize) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            val oldBitmap = drawingBitmap
            val newBmp = Bitmap.createBitmap(canvasSize.width, canvasSize.height, Bitmap.Config.ARGB_8888)
            val newCanvas = Canvas(newBmp)
            if (oldBitmap != null) {
                newCanvas.drawBitmap(oldBitmap, 0f, 0f, null)
            } else {
                // Replay strokes if any
                for (stroke in undoStack) {
                    GraffitiBrushEngine.drawStroke(newCanvas, stroke)
                }
            }
            drawingBitmap = newBmp
            drawingCanvas = newCanvas
            renderTrigger++
        }
    }

    // Helper to redraw entire canvas from undoStack
    fun redrawAllStrokes() {
        val bmp = drawingBitmap ?: return
        bmp.eraseColor(android.graphics.Color.TRANSPARENT)
        val cv = drawingCanvas ?: Canvas(bmp)
        for (stroke in undoStack) {
            GraffitiBrushEngine.drawStroke(cv, stroke)
        }
        renderTrigger++
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("freehand_drawing_canvas_page")
    ) {
        // ---- 1. Top Mini Tool Strip (Undo, Redo, Wall, Clear, Save) ----
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Undo button
                IconButton(
                    onClick = {
                        if (undoStack.isNotEmpty()) {
                            val popped = undoStack.removeAt(undoStack.size - 1)
                            redoStack.add(popped)
                            redrawAllStrokes()
                        }
                    },
                    enabled = undoStack.isNotEmpty(),
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_draw_undo")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo stroke",
                        tint = if (undoStack.isNotEmpty()) Color.White else Color(0xFF555B70),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Redo button
                IconButton(
                    onClick = {
                        if (redoStack.isNotEmpty()) {
                            val popped = redoStack.removeAt(redoStack.size - 1)
                            undoStack.add(popped)
                            val cv = drawingCanvas
                            if (cv != null) {
                                GraffitiBrushEngine.drawStroke(cv, popped)
                                renderTrigger++
                            } else {
                                redrawAllStrokes()
                            }
                        }
                    },
                    enabled = redoStack.isNotEmpty(),
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_draw_redo")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo stroke",
                        tint = if (redoStack.isNotEmpty()) Color.White else Color(0xFF555B70),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Stroke count badge
                Text(
                    text = "${undoStack.size} tags",
                    fontSize = 10.sp,
                    color = Color(0xFF8E95AA),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Clear canvas button
                IconButton(
                    onClick = {
                        undoStack.clear()
                        redoStack.clear()
                        drawingBitmap?.eraseColor(android.graphics.Color.TRANSPARENT)
                        renderTrigger++
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_draw_clear")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Canvas",
                        tint = Color(0xFFFF50B4),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Save / Export button
            Button(
                onClick = {
                    val bmp = drawingBitmap
                    if (bmp != null) {
                        onSaveRequested(bmp, selectedWallType)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isProSubscribed) Color(0xFF00DCFF) else Color(0xFFFFDC00),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("btn_draw_save_art")
            ) {
                Icon(
                    imageVector = if (isProSubscribed) Icons.Default.Save else Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isProSubscribed) "SAVE ART" else "SAVE / PRINT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Mascot Stamp Active Tip Banner
        if (selectedMascot != null && selectedMascot != MascotCharacter.NONE) {
            Surface(
                color = Color(0xFF00DCFF).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00DCFF).copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✨ CHARACTER ADD-ON: Tap canvas to stamp ${selectedMascot?.displayName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00DCFF)
                    )
                    IconButton(
                        onClick = { selectedMascot = null },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Exit stamp mode",
                            tint = Color(0xFF00DCFF),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // ---- 2. Interactive Drawing Canvas Viewport ----
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141724)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .border(1.5.dp, Color(0xFF2C3247), RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .onSizeChanged { size ->
                        if (size.width > 0 && size.height > 0) {
                            canvasSize = size
                        }
                    }
            ) {
                // Background wall rendering
                val dummyConfig = remember(selectedWallType) {
                    GraffitiConfig(
                        wallIndex = GraffitiPalettes.WALLS.indexOfFirst { it.type == selectedWallType }.coerceAtLeast(0)
                    )
                }

                ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                    // Draw wall background
                    drawIntoCanvas { canvas ->
                        GraffitiRenderer.drawWall(
                            canvas.nativeCanvas,
                            size.width,
                            size.height,
                            dummyConfig,
                            context
                        )
                    }

                    // Composite drawing layer
                    renderTrigger.let { _ ->
                        drawingBitmap?.let { bmp ->
                            drawImage(bmp.asImageBitmap())
                        }
                    }
                }

                // Touch & Drag gesture listener on canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedBrush, selectedStencil, selectedMascot, selectedColor, strokeWidth, dripLengthMultiplier) {
                            detectTapGestures { offset ->
                                val point = DrawPoint(offset.x, offset.y)
                                val cv = drawingCanvas ?: return@detectTapGestures
                                val colorArgb = selectedColor.toArgb()

                                val activeMascot = selectedMascot
                                val activeStencil = selectedStencil

                                if (activeMascot != null && activeMascot != MascotCharacter.NONE) {
                                    if (activeMascot.isPro && !isProSubscribed) {
                                        onRequirePro()
                                    } else {
                                        val mascotSize = (strokeWidth * 3.5f).coerceIn(120f, 360f)
                                        MascotRenderer.drawMascotAt(cv, activeMascot, point.x, point.y, mascotSize)
                                        renderTrigger++

                                        val stroke = FreehandStroke(
                                            brushType = selectedBrush,
                                            color = colorArgb,
                                            strokeWidth = strokeWidth,
                                            points = listOf(point),
                                            stencil = null,
                                            mascot = activeMascot
                                        )
                                        undoStack.add(stroke)
                                        redoStack.clear()
                                    }
                                } else if (activeStencil != null) {
                                    // Stamp the downtown street stencil with active color
                                    GraffitiBrushEngine.drawStencil(cv, activeStencil, point.x, point.y, strokeWidth, colorArgb)
                                    renderTrigger++

                                    val stroke = FreehandStroke(
                                        brushType = selectedBrush,
                                        color = colorArgb,
                                        strokeWidth = strokeWidth,
                                        points = listOf(point),
                                        stencil = activeStencil
                                    )
                                    undoStack.add(stroke)
                                    redoStack.clear()
                                } else {
                                    GraffitiBrushEngine.drawDot(cv, point, selectedBrush, colorArgb, strokeWidth)
                                    renderTrigger++

                                    val stroke = FreehandStroke(
                                        brushType = selectedBrush,
                                        color = colorArgb,
                                        strokeWidth = strokeWidth,
                                        points = listOf(point)
                                    )
                                    undoStack.add(stroke)
                                    redoStack.clear()
                                }
                            }
                        }
                        .pointerInput(selectedBrush, selectedStencil, selectedMascot, selectedColor, strokeWidth, dripLengthMultiplier) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val startPoint = DrawPoint(offset.x, offset.y)
                                    val cv = drawingCanvas

                                    val activeMascot = selectedMascot
                                    if (activeMascot != null && activeMascot != MascotCharacter.NONE) {
                                        if (activeMascot.isPro && !isProSubscribed) {
                                            onRequirePro()
                                        } else if (cv != null) {
                                            val mascotSize = (strokeWidth * 3.5f).coerceIn(120f, 360f)
                                            MascotRenderer.drawMascotAt(cv, activeMascot, startPoint.x, startPoint.y, mascotSize)
                                            renderTrigger++

                                            val stroke = FreehandStroke(
                                                brushType = selectedBrush,
                                                color = selectedColor.toArgb(),
                                                strokeWidth = strokeWidth,
                                                points = listOf(startPoint),
                                                stencil = null,
                                                mascot = activeMascot
                                            )
                                            undoStack.add(stroke)
                                            redoStack.clear()
                                        }
                                        return@detectDragGestures
                                    }

                                    currentStrokePoints.clear()
                                    currentStrokeDrips.clear()
                                    lastDripDistance = 0f
                                    currentStrokePoints.add(startPoint)

                                    if (cv != null) {
                                        val activeStencil = selectedStencil
                                        if (activeStencil != null) {
                                            GraffitiBrushEngine.drawStencil(cv, activeStencil, startPoint.x, startPoint.y, strokeWidth, selectedColor.toArgb())
                                        } else {
                                            GraffitiBrushEngine.drawDot(cv, startPoint, selectedBrush, selectedColor.toArgb(), strokeWidth)
                                        }
                                        renderTrigger++
                                    }
                                },
                                onDrag = { change, _ ->
                                    if (selectedMascot != null && selectedMascot != MascotCharacter.NONE) {
                                        change.consume()
                                        return@detectDragGestures
                                    }
                                    change.consume()
                                    val newPoint = DrawPoint(change.position.x, change.position.y)
                                    val points = currentStrokePoints
                                    val cv = drawingCanvas

                                    if (points.isNotEmpty() && cv != null) {
                                        val prevPoint = points.last()
                                        val segDist = hypot(newPoint.x - prevPoint.x, newPoint.y - prevPoint.y)

                                        val activeStencil = selectedStencil
                                        if (activeStencil != null) {
                                            // Trail stencils at spacing intervals
                                            lastDripDistance += segDist
                                            if (lastDripDistance > strokeWidth * 2.2f) {
                                                lastDripDistance = 0f
                                                GraffitiBrushEngine.drawStencil(cv, activeStencil, newPoint.x, newPoint.y, strokeWidth, selectedColor.toArgb())
                                            }
                                        } else {
                                            // Draw segment onto native bitmap
                                            GraffitiBrushEngine.drawSegment(
                                                canvas = cv,
                                                from = prevPoint,
                                                to = newPoint,
                                                brushType = selectedBrush,
                                                color = selectedColor.toArgb(),
                                                strokeWidth = strokeWidth
                                            )

                                            // If Drip or Mop brush, trigger downward gravity drips at spacing
                                            if (selectedBrush == BrushType.DRIP || selectedBrush == BrushType.MOP) {
                                                lastDripDistance += segDist
                                                val dripSpacing = if (selectedBrush == BrushType.MOP) strokeWidth * 3.2f else strokeWidth * 2.4f
                                                if (lastDripDistance > dripSpacing) {
                                                    lastDripDistance = 0f
                                                    val drip = GraffitiBrushEngine.createDripFromPoint(
                                                        x = newPoint.x,
                                                        y = newPoint.y,
                                                        strokeWidth = strokeWidth,
                                                        color = selectedColor.toArgb(),
                                                        dripMultiplier = dripLengthMultiplier
                                                    )
                                                    currentStrokeDrips.add(drip)
                                                    GraffitiBrushEngine.drawDrip(cv, drip)
                                                }
                                            }
                                        }

                                        renderTrigger++
                                    }
                                    currentStrokePoints.add(newPoint)
                                },
                                onDragEnd = {
                                    if (selectedMascot != null && selectedMascot != MascotCharacter.NONE) {
                                        currentStrokePoints.clear()
                                        currentStrokeDrips.clear()
                                        return@detectDragGestures
                                    }
                                    if (currentStrokePoints.isNotEmpty()) {
                                        val stroke = FreehandStroke(
                                            brushType = selectedBrush,
                                            color = selectedColor.toArgb(),
                                            strokeWidth = strokeWidth,
                                            points = currentStrokePoints.toList(),
                                            drips = currentStrokeDrips.toList(),
                                            stencil = selectedStencil
                                        )
                                        undoStack.add(stroke)
                                        redoStack.clear()
                                    }
                                    currentStrokePoints.clear()
                                    currentStrokeDrips.clear()
                                },
                                onDragCancel = {
                                    currentStrokePoints.clear()
                                    currentStrokeDrips.clear()
                                }
                            )
                        }
                )

                // Watermark tag in bottom corner
                Text(
                    text = "${selectedBrush.displayName.uppercase()} • ${strokeWidth.toInt()}PX",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }

        // ---- 3. Downtown Street Brush Selector Deck ----
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191C2A)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .border(1.dp, Color(0xFF2B3045), RoundedCornerShape(10.dp))
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BrushType.values()) { brush ->
                    val isSelected = selectedBrush == brush && selectedStencil == null && selectedMascot == null
                    val activeColor = when (brush) {
                        BrushType.SPRAY -> Color(0xFFFF50B4)
                        BrushType.CHISEL -> Color(0xFF00DCFF)
                        BrushType.DRIP -> Color(0xFF8CFF50)
                        BrushType.FAT_CAP -> Color(0xFFFFDC00)
                        BrushType.FLARE_CAP -> Color(0xFFFF8C28)
                        BrushType.MOP -> Color(0xFFB464FF)
                        BrushType.ERASER -> Color(0xFFCFD8DC)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedBrush = brush
                            selectedStencil = null
                            selectedMascot = null
                            strokeWidth = brush.defaultSize
                        },
                        label = {
                            Text(
                                text = brush.displayName,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (brush) {
                                    BrushType.SPRAY -> Icons.Default.FormatPaint
                                    BrushType.CHISEL -> Icons.Default.Brush
                                    BrushType.DRIP -> Icons.Default.WaterDrop
                                    BrushType.FAT_CAP -> Icons.Default.FormatColorFill
                                    BrushType.FLARE_CAP -> Icons.Default.AutoAwesome
                                    BrushType.MOP -> Icons.Default.WaterDrop
                                    BrushType.ERASER -> Icons.Default.AutoFixHigh
                                },
                                contentDescription = null,
                                tint = if (isSelected) activeColor else Color(0xFF8E95AA),
                                modifier = Modifier.size(13.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activeColor.copy(alpha = 0.2f),
                            selectedLabelColor = activeColor,
                            containerColor = Color(0xFF141622),
                            labelColor = Color(0xFFB0B7CD)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFF262A3B),
                            selectedBorderColor = activeColor
                        ),
                        modifier = Modifier.testTag("chip_brush_${brush.id}")
                    )
                }
            }
        }

        // ---- 3b. Downtown Stencils & Street FX Blaster Rack ----
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131522)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .border(1.dp, Color(0xFF282D40), RoundedCornerShape(10.dp))
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Stencil label tag
                item {
                    Text(
                        text = "STENCILS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFDC00),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }

                // Stencil Chips
                items(StreetStencil.values()) { stencil ->
                    val isSelected = selectedStencil == stencil && selectedMascot == null
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedStencil = if (isSelected) null else stencil
                            if (selectedStencil != null) {
                                selectedMascot = null
                            }
                        },
                        label = {
                            Text(
                                text = "${stencil.iconEmoji} ${stencil.displayName}",
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFDC00).copy(alpha = 0.22f),
                            selectedLabelColor = Color(0xFFFFDC00),
                            containerColor = Color(0xFF181B28),
                            labelColor = Color(0xFF9FA6BC)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFF262B3D),
                            selectedBorderColor = Color(0xFFFFDC00)
                        ),
                        modifier = Modifier.testTag("chip_stencil_${stencil.id}")
                    )
                }

                // Street FX Divider
                item {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color(0xFF2B3045))
                    )
                }

                // FX 1: Splatter Bomb
                item {
                    Surface(
                        color = Color(0xFFFF50B4).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF50B4)),
                        modifier = Modifier
                            .clickable {
                                val cv = drawingCanvas
                                val bmp = drawingBitmap
                                if (cv != null && bmp != null) {
                                    val cx = (canvasSize.width * (0.25f + Math.random().toFloat() * 0.5f))
                                    val cy = (canvasSize.height * (0.25f + Math.random().toFloat() * 0.5f))
                                    val colorArgb = selectedColor.toArgb()
                                    GraffitiBrushEngine.drawSplatterBomb(cv, cx, cy, strokeWidth * 3.5f, colorArgb, 38)
                                    renderTrigger++
                                    undoStack.add(
                                        FreehandStroke(
                                            brushType = BrushType.SPRAY,
                                            color = colorArgb,
                                            strokeWidth = strokeWidth,
                                            points = listOf(DrawPoint(cx, cy))
                                        )
                                    )
                                    redoStack.clear()
                                }
                            }
                            .testTag("btn_fx_splatter_bomb")
                    ) {
                        Text(
                            text = "💥 Splat Bomb",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF50B4),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // FX 2: Slime Drips
                item {
                    Surface(
                        color = Color(0xFF8CFF50).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8CFF50)),
                        modifier = Modifier
                            .clickable {
                                val cv = drawingCanvas
                                if (cv != null) {
                                    val cx = (canvasSize.width * (0.2f + Math.random().toFloat() * 0.6f))
                                    val cy = (canvasSize.height * (0.25f + Math.random().toFloat() * 0.35f))
                                    val colorArgb = selectedColor.toArgb()
                                    val drip = GraffitiBrushEngine.createDripFromPoint(cx, cy, strokeWidth * 1.3f, colorArgb, 1.8f)
                                    GraffitiBrushEngine.drawDrip(cv, drip)
                                    renderTrigger++
                                    undoStack.add(
                                        FreehandStroke(
                                            brushType = BrushType.DRIP,
                                            color = colorArgb,
                                            strokeWidth = strokeWidth,
                                            points = listOf(DrawPoint(cx, cy)),
                                            drips = listOf(drip)
                                        )
                                    )
                                    redoStack.clear()
                                }
                            }
                            .testTag("btn_fx_slime_drips")
                    ) {
                        Text(
                            text = "💧 Slime Drip",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8CFF50),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // FX 3: Star Glint
                item {
                    Surface(
                        color = Color(0xFF00DCFF).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00DCFF)),
                        modifier = Modifier
                            .clickable {
                                val cv = drawingCanvas
                                if (cv != null) {
                                    val cx = (canvasSize.width * (0.25f + Math.random().toFloat() * 0.5f))
                                    val cy = (canvasSize.height * (0.2f + Math.random().toFloat() * 0.5f))
                                    GraffitiBrushEngine.drawStencil(cv, StreetStencil.STAR_FLARE, cx, cy, strokeWidth * 1.5f, android.graphics.Color.WHITE)
                                    renderTrigger++
                                    undoStack.add(
                                        FreehandStroke(
                                            brushType = BrushType.SPRAY,
                                            color = android.graphics.Color.WHITE,
                                            strokeWidth = strokeWidth * 1.5f,
                                            points = listOf(DrawPoint(cx, cy)),
                                            stencil = StreetStencil.STAR_FLARE
                                        )
                                    )
                                    redoStack.clear()
                                }
                            }
                            .testTag("btn_fx_star_glint")
                    ) {
                        Text(
                            text = "✨ Star Glint",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00DCFF),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // ---- 3c. Street Mascot Character Add-On Rack ----
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161928)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .border(
                    width = 1.dp,
                    color = if (selectedMascot != null) Color(0xFF00DCFF).copy(alpha = 0.8f) else Color(0xFF282D42),
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF00DCFF),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "CHARACTERS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00DCFF),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(end = 2.dp)
                        )
                    }
                }

                items(MascotCharacter.values()) { mascot ->
                    val isSelected = selectedMascot == mascot
                    val isLocked = mascot.isPro && !isProSubscribed
                    val mascotEmoji = when (mascot) {
                        MascotCharacter.NONE -> "🚫"
                        MascotCharacter.SPRAY_CAN -> "🧴"
                        MascotCharacter.ALLEY_RAT -> "🐀"
                        MascotCharacter.MECH_BOT -> "🤖"
                        MascotCharacter.ASTRO_CADET -> "👨‍🚀"
                        MascotCharacter.STREET_BULLDOG -> "🐶"
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isLocked) {
                                onRequirePro()
                            } else {
                                selectedMascot = if (isSelected || mascot == MascotCharacter.NONE) null else mascot
                                if (selectedMascot != null) {
                                    selectedStencil = null
                                }
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$mascotEmoji ${mascot.displayName}",
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
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
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00DCFF).copy(alpha = 0.22f),
                            selectedLabelColor = Color(0xFF00DCFF),
                            containerColor = Color(0xFF141724),
                            labelColor = Color(0xFF9FA6BC)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isLocked) Color(0xFF635222) else Color(0xFF262B3D),
                            selectedBorderColor = Color(0xFF00DCFF)
                        ),
                        modifier = Modifier.testTag("chip_mascot_${mascot.id}")
                    )
                }
            }
        }

        // ---- 4. Sliders (Size & Drips) ----
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 1.dp)
        ) {
            Text(
                text = "SIZE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF8E95AA),
                modifier = Modifier.width(36.dp)
            )

            Slider(
                value = strokeWidth,
                onValueChange = { strokeWidth = it },
                valueRange = 8f..68f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00DCFF),
                    activeTrackColor = Color(0xFF00DCFF),
                    inactiveTrackColor = Color(0xFF262B3D)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("slider_brush_size")
            )

            // Preview dot
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size((strokeWidth * 0.45f).coerceIn(8f, 26f).dp)
                    .clip(CircleShape)
                    .background(selectedColor)
                    .border(1.dp, Color.White, CircleShape)
            )

            // If Drip or Mop brush selected, show drip length control
            if (selectedBrush == BrushType.DRIP || selectedBrush == BrushType.MOP) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DRIP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF8CFF50),
                    modifier = Modifier.width(32.dp)
                )
                Slider(
                    value = dripLengthMultiplier,
                    onValueChange = { dripLengthMultiplier = it },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF8CFF50),
                        activeTrackColor = Color(0xFF8CFF50),
                        inactiveTrackColor = Color(0xFF262B3D)
                    ),
                    modifier = Modifier
                        .weight(0.7f)
                        .testTag("slider_drip_multiplier")
                )
            }
        }

        // ---- 5. Street Art Color Swatches Palette ----
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(GraffitiPalettes.FILLS) { colorOption ->
                val isSelected = selectedColor == colorOption.color
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorOption.color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color(0xFF2C3247),
                            shape = CircleShape
                        )
                        .clickable {
                            selectedColor = colorOption.color
                        }
                        .testTag("draw_color_${colorOption.name}")
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (colorOption.color == Color.White) Color.Black else Color.White)
                        )
                    }
                }
            }
        }

        // ---- 6. Wall Texture Quick Selectors ----
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(GraffitiPalettes.WALLS.take(6)) { wall ->
                val isSelected = selectedWallType == wall.type
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedWallType = wall.type },
                    label = {
                        Text(
                            text = wall.name,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00DCFF).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFF00DCFF),
                        containerColor = Color(0xFF161824),
                        labelColor = Color(0xFF8E95AA)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color(0xFF25293A),
                        selectedBorderColor = Color(0xFF00DCFF)
                    ),
                    modifier = Modifier.testTag("draw_wall_${wall.type.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}
