package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.model.GraffitiConfig
import com.example.model.GraffitiFontStyle
import com.example.model.GraffitiPalettes
import com.example.model.MascotCharacter
import com.example.model.MascotPosition
import com.example.model.WallType
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

object GraffitiRenderer {

    private val typefaceCache = mutableMapOf<Int, Typeface>()

    fun getTypeface(context: Context?, fontResId: Int): Typeface {
        if (context == null) return Typeface.DEFAULT_BOLD
        return typefaceCache.getOrPut(fontResId) {
            try {
                ResourcesCompat.getFont(context, fontResId) ?: Typeface.DEFAULT_BOLD
            } catch (_: Exception) {
                Typeface.DEFAULT_BOLD
            }
        }
    }

    /**
     * Draws the complete graffiti artwork onto the provided Canvas.
     */
    fun drawGraffiti(
        canvas: Canvas,
        width: Int,
        height: Int,
        config: GraffitiConfig,
        isPreview: Boolean = false,
        context: Context? = null
    ) {
        if (width <= 0 || height <= 0) return

        val vw = width.toFloat()
        val vh = height.toFloat()

        // 1. Draw Wall / Background
        drawWall(canvas, vw, vh, config, isPreview, context)

        val cleanText = if (config.text.trim().isEmpty()) "GRAFFITI" else config.text.trim().uppercase()

        // 2. Splatters (spray paint droplets & satellites)
        if (config.splats) {
            drawSplatters(canvas, vw, vh, cleanText, config)
        }

        val loadedTypeface = getTypeface(context, config.fontStyle.fontResId)

        // Setup text paint for measurement with loaded graffiti typeface
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = loadedTypeface
            isFakeBoldText = true
            textSkewX = if (config.lean) config.fontStyle.fontSkew else 0f
        }

        // Calculate text size to fit viewport nicely
        var textSize = minOf(vw, vh) * 0.32f
        textPaint.textSize = textSize

        // Adjust text size to fit within 88% width
        val maxWidth = vw * 0.88f
        while (textPaint.measureText(cleanText) > maxWidth && textSize > 18f) {
            textSize *= 0.93f
            textPaint.textSize = textSize
        }

        // Vertical and horizontal centering
        val textWidth = textPaint.measureText(cleanText)
        val textBounds = Rect()
        textPaint.getTextBounds(cleanText, 0, cleanText.length, textBounds)
        val textHeight = textBounds.height().toFloat()

        val textX = (vw - textWidth) / 2f
        val textY = vh / 2f + textHeight / 2.2f

        val fillColor = GraffitiPalettes.FILLS.getOrElse(config.fillColorIndex) { GraffitiPalettes.FILLS[0] }.argb
        val gradientEndColor = GraffitiPalettes.FILLS.getOrElse(config.gradientColorIndex) { GraffitiPalettes.FILLS[0] }.argb
        val outlineColor = GraffitiPalettes.OUTLINES.getOrElse(config.outlineColorIndex) { GraffitiPalettes.OUTLINES[0] }.argb

        // 3. 3D Block Extrusion / Deep Shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = textPaint.typeface
            isFakeBoldText = textPaint.isFakeBoldText
            textSkewX = textPaint.textSkewX
            this.textSize = textPaint.textSize
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = config.outlineThickness * 0.4f
            color = AndroidColor.BLACK
            alpha = 180
        }

        // Draw isometric 3D block extrusion
        val steps = (config.shadowDepth).toInt().coerceIn(2, 28)
        val stepX = (config.shadowDepth / steps) * 0.85f
        val stepY = (config.shadowDepth / steps) * 1.1f

        for (i in steps downTo 1) {
            val offsetX = textX + i * stepX
            val offsetY = textY + i * stepY
            // Darker at back, slightly colored block near front
            shadowPaint.color = if (i > steps / 2) AndroidColor.BLACK else AndroidColor.rgb(20, 20, 30)
            shadowPaint.alpha = (140 + (steps - i) * 3).coerceIn(120, 255)
            canvas.drawText(cleanText, offsetX, offsetY, shadowPaint)
        }

        // 4. Paint Drips (authentic spray paint drips running down from letters)
        if (config.drips) {
            drawDrips(canvas, textX, textY, textWidth, textHeight, textSize, cleanText, config, gradientEndColor)
        }

        // 5. Thick Bubble Outline
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = textPaint.typeface
            isFakeBoldText = textPaint.isFakeBoldText
            textSkewX = textPaint.textSkewX
            this.textSize = textPaint.textSize
            style = Paint.Style.STROKE
            strokeWidth = config.outlineThickness.coerceAtLeast(4f)
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = outlineColor
        }
        canvas.drawText(cleanText, textX, textY, outlinePaint)

        // 6. Letter Fill (Solid or Vibrant Vertical Gradient Fade)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = textPaint.typeface
            isFakeBoldText = textPaint.isFakeBoldText
            textSkewX = textPaint.textSkewX
            this.textSize = textPaint.textSize
            style = Paint.Style.FILL
            if (config.isGradient) {
                shader = LinearGradient(
                    textX, textY - textHeight,
                    textX, textY,
                    fillColor, gradientEndColor,
                    Shader.TileMode.CLAMP
                )
            } else {
                color = fillColor
            }
        }
        canvas.drawText(cleanText, textX, textY, fillPaint)

        // 7. Highlights & Sparkles (Glossy 3D shine and star sparkles)
        if (config.highlights) {
            drawHighlights(canvas, textX, textY, textWidth, textHeight, textSize, cleanText, textPaint)
        }

        // 8. Style-Specific Special Effects from Reference Image
        drawSpecialStyleEffects(canvas, textX, textY, textWidth, textHeight, textSize, cleanText, config, textPaint)

        // 9. Companion Mascot Character Sticker
        if (config.mascot != MascotCharacter.NONE) {
            MascotRenderer.drawMascot(
                canvas = canvas,
                mascot = config.mascot,
                textBounds = textBounds,
                textX = textX,
                textY = textY,
                textWidth = textWidth,
                textHeight = textHeight,
                vw = vw,
                vh = vh,
                position = config.mascotPosition,
                isVerticalLayout = config.isVerticalLayout
            )
        }
    }

    private val wallBitmapCache = mutableMapOf<Int, Bitmap>()

    fun drawWall(canvas: Canvas, vw: Float, vh: Float, config: GraffitiConfig, context: Context?) {
        drawWall(canvas, vw, vh, config, false, context)
    }

    fun drawWall(canvas: Canvas, vw: Float, vh: Float, config: GraffitiConfig, isPreview: Boolean, context: Context?) {
        val wall = GraffitiPalettes.WALLS.getOrElse(config.wallIndex) { GraffitiPalettes.WALLS[0] }

        if (wall.type == WallType.TRANSPARENT) {
            if (isPreview) {
                // Draw checkerboard pattern for transparent background preview
                val checkPaint1 = Paint().apply { color = AndroidColor.rgb(38, 40, 52) }
                val checkPaint2 = Paint().apply { color = AndroidColor.rgb(26, 28, 38) }
                val checkSize = 36f
                var y = 0f
                var row = 0
                while (y < vh) {
                    var x = 0f
                    var col = 0
                    while (x < vw) {
                        val p = if ((row + col) % 2 == 0) checkPaint1 else checkPaint2
                        canvas.drawRect(x, y, (x + checkSize).coerceAtMost(vw), (y + checkSize).coerceAtMost(vh), p)
                        x += checkSize
                        col++
                    }
                    y += checkSize
                    row++
                }
            } else {
                // For actual export, keep transparent
                canvas.drawColor(AndroidColor.TRANSPARENT)
            }
            return
        }

        // Base wall color
        canvas.drawColor(wall.argb)

        // Wall Texture details
        when (wall.type) {
            WallType.PHOTOREAL_ALLEY -> {
                drawPhotorealAsset(canvas, vw, vh, R.drawable.urban_graffiti_wall_1790841814827, context)
            }
            WallType.SUBWAY_CAR -> {
                drawPhotorealAsset(canvas, vw, vh, R.drawable.img_subway_train_car_1791170022260, context)
            }
            WallType.ROLLUP_SHUTTER -> {
                drawPhotorealAsset(canvas, vw, vh, R.drawable.img_corrugated_shutter_1791170031789, context)
            }
            WallType.FREIGHT_TUNNEL -> {
                drawPhotorealAsset(canvas, vw, vh, R.drawable.img_freight_tunnel_1791170041622, context)
            }
            WallType.AGED_BRICK -> {
                drawPhotorealAsset(canvas, vw, vh, R.drawable.img_warehouse_brick_1791170051490, context)
            }
            WallType.RED_BRICK -> {
                drawBrickPattern(canvas, vw, vh)
            }
            WallType.CLEAN_WHITE -> {
                drawSubwayTilePattern(canvas, vw, vh)
            }
            WallType.DARK_ALLEY -> {
                drawConcreteTexture(canvas, vw, vh)
            }
            WallType.CYBER_NIGHT -> {
                drawCyberGrid(canvas, vw, vh)
            }
            WallType.ROOFTOP_SKYLINE -> {
                drawRooftopSkyline(canvas, vw, vh)
            }
            WallType.HIGHWAY_OVERPASS -> {
                drawHighwayOverpass(canvas, vw, vh)
            }
            WallType.FREIGHT_YARD -> {
                drawFreightYard(canvas, vw, vh)
            }
            else -> {
                // Subtle urban vignette
                drawVignette(canvas, vw, vh)
            }
        }
    }

    private fun drawPhotorealAsset(canvas: Canvas, vw: Float, vh: Float, resId: Int, context: Context?) {
        if (context != null) {
            try {
                var cached = wallBitmapCache[resId]
                if (cached == null || cached.isRecycled) {
                    cached = BitmapFactory.decodeResource(context.resources, resId)
                    if (cached != null) {
                        wallBitmapCache[resId] = cached
                    }
                }
                cached?.let { bmp ->
                    val src = Rect(0, 0, bmp.width, bmp.height)
                    val dst = Rect(0, 0, vw.toInt(), vh.toInt())
                    canvas.drawBitmap(bmp, src, dst, null)
                    return
                }
            } catch (_: Exception) {}
        }
        drawConcreteTexture(canvas, vw, vh)
    }

    private fun drawBrickPattern(canvas: Canvas, vw: Float, vh: Float) {
        val mortarPaint = Paint().apply {
            color = AndroidColor.rgb(75, 40, 30)
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val brickH = 34f
        val brickW = 78f
        var y = 0f
        var row = 0
        val brickRng = Random(1234)

        while (y < vh) {
            canvas.drawLine(0f, y, vw, y, mortarPaint)
            val offset = if (row % 2 == 0) 0f else brickW / 2f
            var x = -offset
            while (x < vw + brickW) {
                canvas.drawLine(x, y, x, y + brickH, mortarPaint)
                // Subtle brick speckle
                if (brickRng.nextFloat() > 0.6f) {
                    val speckPaint = Paint().apply {
                        color = AndroidColor.rgb(130, 65, 45)
                        alpha = 80
                    }
                    canvas.drawRect(x + 4, y + 4, x + brickW - 4, y + brickH - 4, speckPaint)
                }
                x += brickW
            }
            y += brickH
            row++
        }
    }

    private fun drawSubwayTilePattern(canvas: Canvas, vw: Float, vh: Float) {
        val groutPaint = Paint().apply {
            color = AndroidColor.rgb(205, 205, 215)
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }
        val tileH = 40f
        val tileW = 90f
        var y = 0f
        var row = 0
        while (y < vh) {
            canvas.drawLine(0f, y, vw, y, groutPaint)
            val offset = if (row % 2 == 0) 0f else tileW / 2f
            var x = -offset
            while (x < vw + tileW) {
                canvas.drawLine(x, y, x, y + tileH, groutPaint)
                x += tileW
            }
            y += tileH
            row++
        }
    }

    private fun drawConcreteTexture(canvas: Canvas, vw: Float, vh: Float) {
        val grainPaint = Paint().apply {
            style = Paint.Style.FILL
            color = AndroidColor.WHITE
            alpha = 18
        }
        val rng = Random(999)
        for (i in 0 until 120) {
            val cx = rng.nextFloat() * vw
            val cy = rng.nextFloat() * vh
            val r = 1f + rng.nextFloat() * 4f
            canvas.drawCircle(cx, cy, r, grainPaint)
        }
        drawVignette(canvas, vw, vh)
    }

    private fun drawCyberGrid(canvas: Canvas, vw: Float, vh: Float) {
        val linePaint = Paint().apply {
            color = AndroidColor.rgb(90, 40, 160)
            strokeWidth = 1.5f
            alpha = 70
        }
        val step = 50f
        var x = 0f
        while (x < vw) {
            canvas.drawLine(x, 0f, x, vh, linePaint)
            x += step
        }
        var y = 0f
        while (y < vh) {
            canvas.drawLine(0f, y, vw, y, linePaint)
            y += step
        }
    }

    private fun drawVignette(canvas: Canvas, vw: Float, vh: Float) {
        val vigPaint = Paint().apply {
            style = Paint.Style.FILL
            color = AndroidColor.BLACK
            alpha = 45
        }
        canvas.drawRect(0f, 0f, vw, 16f, vigPaint)
        canvas.drawRect(0f, vh - 16f, vw, vh, vigPaint)
        canvas.drawRect(0f, 0f, 16f, vh, vigPaint)
        canvas.drawRect(vw - 16f, 0f, vw, vh, vigPaint)
    }

    private fun drawSplatters(
        canvas: Canvas,
        vw: Float,
        vh: Float,
        text: String,
        config: GraffitiConfig
    ) {
        val rng = Random((text.hashCode() + config.splatSeed).toLong())
        val splatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        val count = config.splatDensity.coerceIn(8, 36)
        for (i in 0 until count) {
            val cx = vw * 0.08f + rng.nextFloat() * vw * 0.84f
            val cy = vh * 0.16f + rng.nextFloat() * vh * 0.68f
            val radius = 8f + rng.nextFloat() * 32f

            // Pick vibrant colors from Fills palette
            val colorObj = GraffitiPalettes.FILLS[rng.nextInt(GraffitiPalettes.FILLS.size)]
            splatPaint.color = colorObj.argb
            splatPaint.alpha = 130 + rng.nextInt(90) // 130 - 220

            // Main splat blob
            canvas.drawCircle(cx, cy, radius, splatPaint)

            // Spray satellites (micro splatter spray droplets radiating outward)
            val satellites = 3 + rng.nextInt(5)
            for (s in 0 until satellites) {
                val angle = rng.nextFloat() * Math.PI.toFloat() * 2f
                val dist = radius * (1.2f + rng.nextFloat() * 1.6f)
                val sx = cx + cos(angle) * dist
                val sy = cy + sin(angle) * dist
                val sr = (radius * 0.15f * rng.nextFloat() + 2f).coerceAtLeast(1.5f)
                canvas.drawCircle(sx, sy, sr, splatPaint)
            }
        }
    }

    private fun drawDrips(
        canvas: Canvas,
        textX: Float,
        textY: Float,
        textWidth: Float,
        textHeight: Float,
        textSize: Float,
        text: String,
        config: GraffitiConfig,
        dripColor: Int
    ) {
        val dripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dripColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 3f
        }

        val rng = Random((text.hashCode() * 31 + config.splatSeed).toLong())
        val numDrips = (text.length.coerceAtLeast(3) * 0.9f).toInt().coerceIn(3, 10)
        val dripSpacing = textWidth / (numDrips + 1)

        for (i in 1..numDrips) {
            val dx = textX + i * dripSpacing + (rng.nextFloat() - 0.5f) * dripSpacing * 0.4f
            val startY = textY + 2f
            val dripLength = textSize * (0.25f + rng.nextFloat() * 0.45f)
            val endY = startY + dripLength
            val dripWidth = (4f + rng.nextFloat() * 5f).coerceAtLeast(3f)

            // Draw dripping column
            val path = Path().apply {
                moveTo(dx - dripWidth / 2f, startY)
                quadTo(dx - dripWidth * 0.3f, startY + dripLength * 0.6f, dx - dripWidth * 0.4f, endY - dripWidth)
                quadTo(dx, endY + dripWidth, dx + dripWidth * 0.4f, endY - dripWidth)
                quadTo(dx + dripWidth * 0.3f, startY + dripLength * 0.6f, dx + dripWidth / 2f, startY)
                close()
            }
            canvas.drawPath(path, dripPaint)

            // Tear-drop tip bead
            val beadRadius = dripWidth * 1.1f
            canvas.drawCircle(dx, endY, beadRadius, dripPaint)

            // Optional fallen droplet underneath
            if (rng.nextFloat() > 0.45f) {
                val dropY = endY + beadRadius * 3f + rng.nextFloat() * 12f
                val smallR = beadRadius * (0.4f + rng.nextFloat() * 0.4f)
                canvas.drawCircle(dx + (rng.nextFloat() - 0.5f) * 4f, dropY, smallR, dripPaint)
            }
        }
    }

    private fun drawHighlights(
        canvas: Canvas,
        textX: Float,
        textY: Float,
        textWidth: Float,
        textHeight: Float,
        textSize: Float,
        text: String,
        baseTextPaint: Paint
    ) {
        // Subtle white inner shine shifted up-left
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = baseTextPaint.typeface
            isFakeBoldText = baseTextPaint.isFakeBoldText
            textSkewX = baseTextPaint.textSkewX
            this.textSize = baseTextPaint.textSize
            style = Paint.Style.STROKE
            strokeWidth = (textSize * 0.045f).coerceIn(2.5f, 6.5f)
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = AndroidColor.WHITE
            alpha = 110
        }

        // Draw inner shine stroke shifted slightly inwards
        canvas.drawText(text, textX - textSize * 0.015f, textY - textSize * 0.025f, highlightPaint)

        // Four-point star sparkles (graffiti shine glints)
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
            alpha = 230
        }

        val sparklePoints = listOf(
            Pair(textX + textWidth * 0.18f, textY - textHeight * 0.95f),
            Pair(textX + textWidth * 0.76f, textY - textHeight * 0.85f),
            Pair(textX + textWidth * 0.48f, textY - textHeight * 1.05f)
        )

        for ((sx, sy) in sparklePoints) {
            val starSize = textSize * 0.12f
            drawSparkleStar(canvas, sx, sy, starSize, starPaint)
        }
    }

    private fun drawSparkleStar(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint) {
        val path = Path().apply {
            moveTo(cx, cy - size)
            quadTo(cx, cy, cx + size, cy)
            quadTo(cx, cy, cx, cy + size)
            quadTo(cx, cy, cx - size, cy)
            quadTo(cx, cy, cx, cy - size)
            close()
        }
        canvas.drawPath(path, paint)
        // Center glow dot
        canvas.drawCircle(cx, cy, size * 0.25f, paint)
    }

    /**
     * Renders a high-resolution Bitmap of the graffiti design.
     */
    fun createBitmap(
        config: GraffitiConfig,
        width: Int = 1200,
        height: Int = 900,
        isTransparent: Boolean = false,
        context: Context? = null
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val renderConfig = if (isTransparent) {
            config.copy(wallIndex = GraffitiPalettes.WALLS.indexOfFirst { it.type == WallType.TRANSPARENT }.coerceAtLeast(0))
        } else {
            config
        }
        drawGraffiti(canvas, width, height, renderConfig, isPreview = false, context = context)
        return bitmap
    }

    /**
     * Saves the graffiti design to the public Pictures gallery (MediaStore).
     */
    fun saveToGallery(
        context: Context,
        config: GraffitiConfig,
        isTransparent: Boolean = false
    ): Result<Uri> {
        return try {
            val bitmap = createBitmap(config, width = 1440, height = 1080, isTransparent = isTransparent, context = context)
            val fileName = "graffiti_${System.currentTimeMillis()}.png"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GraffitiCreator")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return Result.failure(Exception("Unable to create media store record"))

            resolver.openOutputStream(uri)?.use { outStream: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a temporary image file and returns its content Uri via FileProvider for sharing.
     */
    fun createShareUri(
        context: Context,
        config: GraffitiConfig,
        isTransparent: Boolean = false
    ): Uri? {
        return try {
            val bitmap = createBitmap(config, width = 1200, height = 900, isTransparent = isTransparent, context = context)
            val cacheDir = File(context.cacheDir, "images").apply { mkdirs() }
            val shareFile = File(cacheDir, "share_graffiti.png")
            FileOutputStream(shareFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                shareFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a printable PDF poster with high-res graffiti artwork and saves to Downloads.
     */
    fun saveToPdf(
        context: Context,
        config: GraffitiConfig
    ): Result<Uri> {
        return try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            // Page Background: Dark Studio Theme
            canvas.drawColor(AndroidColor.rgb(18, 20, 28))

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Header Banner Bars (Neon accents)
            paint.color = AndroidColor.rgb(255, 80, 180)
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, paint)

            paint.color = AndroidColor.rgb(0, 220, 255)
            canvas.drawRect(0f, 12f, pageWidth.toFloat(), 16f, paint)

            // Header Text
            paint.color = AndroidColor.WHITE
            paint.textSize = 22f
            paint.isFakeBoldText = true
            paint.letterSpacing = 0.08f
            canvas.drawText("PROJECT GRAFFITI", 40f, 58f, paint)

            paint.color = AndroidColor.rgb(150, 155, 175)
            paint.textSize = 10f
            paint.isFakeBoldText = false
            paint.letterSpacing = 0.05f
            val dateStr = SimpleDateFormat("MMMM d, yyyy • h:mm a", Locale.getDefault()).format(Date())
            canvas.drawText("PRINTABLE STREET ART POSTER • $dateStr", 40f, 78f, paint)

            // Tag Name Highlight
            val tagText = "TAG: " + if (config.text.trim().isEmpty()) "GRAFFITI" else config.text.trim().uppercase()
            paint.color = AndroidColor.rgb(255, 220, 0)
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText(tagText, 40f, 108f, paint)

            // Artwork Frame Area
            val frameLeft = 40f
            val frameTop = 125f
            val frameWidth = pageWidth - 80f // 515f
            val frameHeight = 386f
            val frameRight = frameLeft + frameWidth
            val frameBottom = frameTop + frameHeight

            // Outer border for artwork
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = AndroidColor.rgb(42, 45, 62)
            canvas.drawRect(frameLeft - 2f, frameTop - 2f, frameRight + 2f, frameBottom + 2f, paint)

            // Render high-res graffiti bitmap and draw inside frame
            val artBitmap = createBitmap(config, width = 1200, height = 900, isTransparent = false, context = context)
            val srcRect = Rect(0, 0, artBitmap.width, artBitmap.height)
            val dstRect = Rect(frameLeft.toInt(), frameTop.toInt(), frameRight.toInt(), frameBottom.toInt())
            paint.style = Paint.Style.FILL
            canvas.drawBitmap(artBitmap, srcRect, dstRect, paint)

            // Specs Card underneath
            val cardTop = frameBottom + 25f
            paint.color = AndroidColor.rgb(27, 29, 40)
            canvas.drawRoundRect(frameLeft, cardTop, frameRight, cardTop + 135f, 12f, 12f, paint)

            paint.color = AndroidColor.rgb(0, 220, 255)
            paint.textSize = 13f
            paint.isFakeBoldText = true
            canvas.drawText("PIECE SPECIFICATIONS", frameLeft + 20f, cardTop + 30f, paint)

            paint.color = AndroidColor.rgb(215, 220, 235)
            paint.textSize = 10.5f
            paint.isFakeBoldText = false
            val fontStyleStr = "Letter Style: ${config.fontStyle.displayName} (Slant: ${if (config.lean) "ON" else "OFF"})"
            canvas.drawText(fontStyleStr, frameLeft + 20f, cardTop + 55f, paint)

            val fillObj = GraffitiPalettes.FILLS.getOrElse(config.fillColorIndex) { GraffitiPalettes.FILLS[0] }
            val gradObj = GraffitiPalettes.FILLS.getOrElse(config.gradientColorIndex) { GraffitiPalettes.FILLS[0] }
            val colorStr = if (config.isGradient) {
                "Colors: ${fillObj.name} to ${gradObj.name} Gradient"
            } else {
                "Colors: ${fillObj.name} Solid"
            }
            canvas.drawText(colorStr, frameLeft + 20f, cardTop + 75f, paint)

            val outlineObj = GraffitiPalettes.OUTLINES.getOrElse(config.outlineColorIndex) { GraffitiPalettes.OUTLINES[0] }
            val outlineStr = "Outline: ${outlineObj.name} (${config.outlineThickness.toInt()}px) • 3D Block: ${config.shadowDepth.toInt()}px"
            canvas.drawText(outlineStr, frameLeft + 20f, cardTop + 95f, paint)

            val wallObj = GraffitiPalettes.WALLS.getOrElse(config.wallIndex) { GraffitiPalettes.WALLS[0] }
            val effectsStr = "Wall: ${wallObj.name} • Splatters: ${if (config.splats) "ON" else "OFF"} • Drips: ${if (config.drips) "ON" else "OFF"} • 3D Shine: ${if (config.highlights) "ON" else "OFF"}"
            canvas.drawText(effectsStr, frameLeft + 20f, cardTop + 115f, paint)

            // Footer notes
            paint.color = AndroidColor.rgb(100, 105, 125)
            paint.textSize = 9f
            paint.isFakeBoldText = false
            canvas.drawText("Generated with Project Graffiti • Downloadable Printable PDF", 40f, pageHeight - 35f, paint)

            paint.color = AndroidColor.rgb(255, 80, 180)
            canvas.drawRect(0f, pageHeight - 6f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

            pdfDoc.finishPage(page)

            // Save to Downloads
            val cleanWord = (if (config.text.trim().isEmpty()) "GRAFFITI" else config.text.trim())
                .replace(Regex("[^a-zA-Z0-9]"), "_")
                .take(20)
            val fileName = "graffiti_${cleanWord}_${System.currentTimeMillis()}.pdf"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/GraffitiCreator")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Files.getContentUri("external")
                },
                contentValues
            ) ?: return Result.failure(Exception("Unable to create PDF in Downloads folder"))

            resolver.openOutputStream(uri)?.use { out ->
                pdfDoc.writeTo(out)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            pdfDoc.close()
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a temporary PDF file and returns its content Uri via FileProvider for sharing.
     */
    fun createPdfShareUri(
        context: Context,
        config: GraffitiConfig
    ): Uri? {
        return try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawColor(AndroidColor.rgb(18, 20, 28))
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            paint.color = AndroidColor.rgb(255, 80, 180)
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, paint)
            paint.color = AndroidColor.rgb(0, 220, 255)
            canvas.drawRect(0f, 12f, pageWidth.toFloat(), 16f, paint)

            paint.color = AndroidColor.WHITE
            paint.textSize = 22f
            paint.isFakeBoldText = true
            canvas.drawText("PROJECT GRAFFITI", 40f, 58f, paint)

            val tagText = "TAG: " + if (config.text.trim().isEmpty()) "GRAFFITI" else config.text.trim().uppercase()
            paint.color = AndroidColor.rgb(255, 220, 0)
            paint.textSize = 14f
            canvas.drawText(tagText, 40f, 95f, paint)

            val frameLeft = 40f
            val frameTop = 115f
            val frameWidth = pageWidth - 80f
            val frameHeight = 386f
            val artBitmap = createBitmap(config, width = 1200, height = 900, isTransparent = false, context = context)
            canvas.drawBitmap(artBitmap, Rect(0, 0, artBitmap.width, artBitmap.height), Rect(frameLeft.toInt(), frameTop.toInt(), (frameLeft + frameWidth).toInt(), (frameTop + frameHeight).toInt()), paint)

            val cardTop = frameTop + frameHeight + 25f
            paint.color = AndroidColor.rgb(27, 29, 40)
            canvas.drawRoundRect(frameLeft, cardTop, frameLeft + frameWidth, cardTop + 120f, 12f, 12f, paint)

            paint.color = AndroidColor.rgb(0, 220, 255)
            paint.textSize = 13f
            paint.isFakeBoldText = true
            canvas.drawText("STREET ART SPECS", frameLeft + 20f, cardTop + 30f, paint)

            paint.color = AndroidColor.rgb(215, 220, 235)
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Letter Style: ${config.fontStyle.displayName}", frameLeft + 20f, cardTop + 55f, paint)
            canvas.drawText("Outline: ${config.outlineThickness.toInt()}px • 3D Block: ${config.shadowDepth.toInt()}px", frameLeft + 20f, cardTop + 75f, paint)
            canvas.drawText("Wall: ${GraffitiPalettes.WALLS.getOrElse(config.wallIndex) { GraffitiPalettes.WALLS[0] }.name}", frameLeft + 20f, cardTop + 95f, paint)

            pdfDoc.finishPage(page)

            val cacheDir = File(context.cacheDir, "documents").apply { mkdirs() }
            val cleanWord = (if (config.text.trim().isEmpty()) "graffiti" else config.text.trim())
                .replace(Regex("[^a-zA-Z0-9]"), "_")
            val pdfFile = File(cacheDir, "graffiti_${cleanWord}.pdf")
            FileOutputStream(pdfFile).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // =========================================================================
    // STYLE-SPECIFIC SPECIAL EFFECTS (Matching reference image)
    // =========================================================================
    private fun drawSpecialStyleEffects(
        canvas: Canvas,
        textX: Float,
        textY: Float,
        textWidth: Float,
        textHeight: Float,
        textSize: Float,
        cleanText: String,
        config: GraffitiConfig,
        textPaint: Paint
    ) {
        when (config.masterStyleId) {
            "purple_chrome" -> {
                // Metallic Specular Chrome Band across letters
                val chromePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = textPaint.typeface
                    isFakeBoldText = textPaint.isFakeBoldText
                    textSkewX = textPaint.textSkewX
                    this.textSize = textPaint.textSize
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                    color = AndroidColor.argb(190, 255, 255, 255)
                }
                // Draw specular chrome line across the upper 3rd
                canvas.drawText(cleanText, textX, textY - textHeight * 0.28f, chromePaint)

                // Toxic lime droplet accents
                val limePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.rgb(140, 255, 80)
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(textX + textWidth * 0.15f, textY + 12f, 4f, limePaint)
                canvas.drawCircle(textX + textWidth * 0.45f, textY + 16f, 3f, limePaint)
                canvas.drawCircle(textX + textWidth * 0.78f, textY + 14f, 4.5f, limePaint)
            }
            "bubble_pop" -> {
                // Playful bubble dots inside letters
                val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = AndroidColor.argb(160, 255, 240, 100) // Sunny bubble dots
                }
                val rng = Random(config.splatSeed.toLong())
                val dotCount = (cleanText.length * 4).coerceIn(8, 30)
                for (i in 0 until dotCount) {
                    val bx = textX + rng.nextFloat() * textWidth * 0.9f
                    val by = textY - rng.nextFloat() * textHeight * 0.75f
                    val br = rng.nextFloat() * 6f + 3f
                    canvas.drawCircle(bx, by, br, bubblePaint)
                }

                // Electric Cyan Aerosol Overspray Halo
                val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = textPaint.typeface
                    isFakeBoldText = textPaint.isFakeBoldText
                    textSkewX = textPaint.textSkewX
                    this.textSize = textPaint.textSize
                    style = Paint.Style.STROKE
                    strokeWidth = config.outlineThickness * 1.6f
                    color = AndroidColor.argb(70, 0, 220, 255)
                }
                canvas.drawText(cleanText, textX, textY, auraPaint)
            }
            "rust_freight" -> {
                // Distressed Weathered Scratches
                val scratchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = AndroidColor.argb(140, 40, 50, 60)
                    strokeWidth = 2.5f
                }
                val rng = Random((config.splatSeed + 12).toLong())
                for (i in 0 until 12) {
                    val sx = textX + rng.nextFloat() * textWidth
                    val sy = textY - rng.nextFloat() * textHeight
                    canvas.drawLine(sx, sy, sx + 18f, sy - 8f, scratchPaint)
                }
                // Industrial Bolts
                val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = AndroidColor.rgb(180, 190, 205)
                }
                canvas.drawCircle(textX + 10f, textY - textHeight * 0.8f, 3.5f, boltPaint)
                canvas.drawCircle(textX + textWidth - 10f, textY - textHeight * 0.8f, 3.5f, boltPaint)
            }
            "cosmic_galaxy" -> {
                // Twinkling Star Clusters & Cosmic Nebulae
                val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = AndroidColor.WHITE
                }
                val rng = Random((config.splatSeed + 99).toLong())
                for (i in 0 until 24) {
                    val sx = textX + rng.nextFloat() * textWidth
                    val sy = textY - rng.nextFloat() * textHeight * 0.9f
                    val size = rng.nextFloat() * 3.5f + 1.5f
                    canvas.drawCircle(sx, sy, size, starPaint)
                    if (i % 4 == 0) {
                        // 4-point star glint
                        canvas.drawLine(sx - 8f, sy, sx + 8f, sy, starPaint)
                        canvas.drawLine(sx, sy - 8f, sx, sy + 8f, starPaint)
                    }
                }
            }
            "emerald_gold" -> {
                // Polished 3D Gold Corner Flares
                val flarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = AndroidColor.rgb(255, 235, 140)
                }
                val corners = listOf(
                    Pair(textX + 15f, textY - textHeight * 0.85f),
                    Pair(textX + textWidth * 0.48f, textY - textHeight * 0.9f),
                    Pair(textX + textWidth - 12f, textY - textHeight * 0.85f),
                    Pair(textX + textWidth * 0.82f, textY + 6f)
                )
                for ((fx, fy) in corners) {
                    canvas.drawCircle(fx, fy, 4f, flarePaint)
                    val glintStroke = Paint(flarePaint).apply { strokeWidth = 2.5f }
                    canvas.drawLine(fx - 14f, fy, fx + 14f, fy, glintStroke)
                    canvas.drawLine(fx, fy - 14f, fx, fy + 14f, glintStroke)
                }
            }
            "times_square_neon", "TIMES_SQUARE_NEON" -> {
                // Multi-layered Electric Neon Glow Halo
                val neonCyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = textPaint.typeface
                    isFakeBoldText = textPaint.isFakeBoldText
                    textSkewX = textPaint.textSkewX
                    this.textSize = textPaint.textSize
                    style = Paint.Style.STROKE
                    strokeWidth = config.outlineThickness * 2.2f
                    color = AndroidColor.argb(55, 0, 220, 255)
                }
                canvas.drawText(cleanText, textX, textY, neonCyanPaint)

                val neonPinkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = textPaint.typeface
                    isFakeBoldText = textPaint.isFakeBoldText
                    textSkewX = textPaint.textSkewX
                    this.textSize = textPaint.textSize
                    style = Paint.Style.STROKE
                    strokeWidth = config.outlineThickness * 1.5f
                    color = AndroidColor.argb(80, 255, 80, 180)
                }
                canvas.drawText(cleanText, textX, textY, neonPinkPaint)

                // Downtown neon starbursts
                val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.WHITE
                    style = Paint.Style.FILL
                }
                drawSparkleStar(canvas, textX + textWidth * 0.2f, textY - textHeight * 0.95f, textSize * 0.14f, starPaint)
                drawSparkleStar(canvas, textX + textWidth * 0.82f, textY - textHeight * 0.9f, textSize * 0.14f, starPaint)
            }
            "brooklyn_bubble", "BROOKLYN_BUBBLE" -> {
                // Street King Gold Crown sitting atop the first letter
                val crownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.rgb(255, 220, 0)
                    style = Paint.Style.FILL
                }
                val crownStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.BLACK
                    style = Paint.Style.STROKE
                    strokeWidth = 3.5f
                }
                val cx = textX + textSize * 0.45f
                val cy = textY - textHeight * 0.98f
                val cw = textSize * 0.35f
                val ch = textSize * 0.25f

                val crownPath = Path().apply {
                    moveTo(cx - cw, cy + ch * 0.4f)
                    lineTo(cx + cw, cy + ch * 0.4f)
                    lineTo(cx + cw * 0.8f, cy - ch * 0.4f)
                    lineTo(cx + cw * 0.3f, cy)
                    lineTo(cx, cy - ch * 0.8f)
                    lineTo(cx - cw * 0.3f, cy)
                    lineTo(cx - cw * 0.8f, cy - ch * 0.4f)
                    close()
                }
                canvas.drawPath(crownPath, crownPaint)
                canvas.drawPath(crownPath, crownStroke)
                canvas.drawCircle(cx, cy - ch * 0.8f, 4f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.WHITE })
            }
            "soho_stencil", "SOHO_STENCIL" -> {
                // Aerosol Stencil cuts & High-velocity paint splatter
                val cutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = AndroidColor.argb(180, 20, 20, 28)
                    strokeWidth = 4f
                }
                // Horizontal stencil bridge cuts through text
                canvas.drawLine(textX, textY - textHeight * 0.5f, textX + textWidth, textY - textHeight * 0.5f, cutPaint)

                // High-velocity splatter flecks
                val splatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.rgb(255, 45, 85)
                    style = Paint.Style.FILL
                }
                val rng = Random((config.splatSeed + 77).toLong())
                for (i in 0 until 18) {
                    val sx = textX + rng.nextFloat() * textWidth
                    val sy = textY - rng.nextFloat() * textHeight
                    canvas.drawCircle(sx, sy, rng.nextFloat() * 4f + 1.5f, splatPaint)
                }
            }
            "harlem_sunset", "HARLEM_SUNSET" -> {
                // Golden Hour Sunset Rim Highlight & Amber Flare
                val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = textPaint.typeface
                    isFakeBoldText = textPaint.isFakeBoldText
                    textSkewX = textPaint.textSkewX
                    this.textSize = textPaint.textSize
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    color = AndroidColor.argb(200, 255, 210, 100)
                }
                canvas.drawText(cleanText, textX - 2f, textY - 4f, rimPaint)

                val amberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.rgb(255, 160, 40)
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(textX + textWidth * 0.5f, textY - textHeight * 1.05f, 6f, amberPaint)
                canvas.drawCircle(textX + textWidth * 0.5f, textY - textHeight * 1.05f, 2.5f, Paint().apply { color = AndroidColor.WHITE })
            }
            "subway_chrome", "SUBWAY_CHROME" -> {
                // 3D Specular Chrome Horizon Band & Cross Sparkles
                val chromePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = textPaint.typeface
                    isFakeBoldText = textPaint.isFakeBoldText
                    textSkewX = textPaint.textSkewX
                    this.textSize = textPaint.textSize
                    style = Paint.Style.STROKE
                    strokeWidth = 5.5f
                    color = AndroidColor.argb(210, 255, 255, 255)
                }
                canvas.drawText(cleanText, textX, textY - textHeight * 0.35f, chromePaint)

                // Brilliant 4-point chrome star flares
                val glintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.WHITE
                    style = Paint.Style.FILL
                }
                drawSparkleStar(canvas, textX + textWidth * 0.12f, textY - textHeight * 0.88f, textSize * 0.16f, glintPaint)
                drawSparkleStar(canvas, textX + textWidth * 0.52f, textY - textHeight * 0.95f, textSize * 0.18f, glintPaint)
                drawSparkleStar(canvas, textX + textWidth * 0.88f, textY - textHeight * 0.82f, textSize * 0.15f, glintPaint)
            }
        }
    }

    // =========================================================================
    // URBAN WALL BACKDROPS (Rooftop, Overpass, Train Yard)
    // =========================================================================
    private fun drawRooftopSkyline(canvas: Canvas, vw: Float, vh: Float) {
        // Sunset twilight gradient
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, vh,
                intArrayOf(
                    AndroidColor.rgb(24, 16, 42),   // Deep indigo
                    AndroidColor.rgb(78, 28, 64),   // Twilight magenta
                    AndroidColor.rgb(150, 60, 48),  // Amber horizon
                    AndroidColor.rgb(40, 30, 40)    // Rooftop deck
                ),
                floatArrayOf(0f, 0.45f, 0.75f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, vw, vh, skyPaint)

        // Skyscraper silhouettes in background
        val bldgPaint = Paint().apply {
            color = AndroidColor.argb(160, 15, 12, 28)
            style = Paint.Style.FILL
        }
        val windowPaint = Paint().apply {
            color = AndroidColor.argb(120, 255, 220, 140)
            style = Paint.Style.FILL
        }
        val bldgWidths = listOf(50f, 75f, 60f, 90f, 55f, 80f, 65f, 70f, 85f)
        var bx = 0f
        var bi = 0
        while (bx < vw) {
            val bw = bldgWidths[bi % bldgWidths.size]
            val bh = 80f + (bi * 27 % 140)
            val by = vh * 0.72f - bh
            canvas.drawRect(bx, by, bx + bw, vh * 0.72f, bldgPaint)
            // Yellow window dots
            for (wy in by.toInt() + 10 until (vh * 0.72f).toInt() - 10 step 18) {
                for (wx in bx.toInt() + 8 until (bx + bw).toInt() - 8 step 14) {
                    if ((wx + wy) % 3 == 0) {
                        canvas.drawRect(wx.toFloat(), wy.toFloat(), wx + 4f, wy + 6f, windowPaint)
                    }
                }
            }
            bx += bw + 8f
            bi++
        }

        // Rooftop brick parapet ledge
        val ledgePaint = Paint().apply {
            color = AndroidColor.rgb(45, 32, 38)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, vh * 0.72f, vw, vh, ledgePaint)
    }

    private fun drawHighwayOverpass(canvas: Canvas, vw: Float, vh: Float) {
        // Concrete pillar background
        val concPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, vw, vh,
                intArrayOf(AndroidColor.rgb(48, 52, 58), AndroidColor.rgb(36, 40, 44), AndroidColor.rgb(24, 26, 30)),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, vw, vh, concPaint)

        // Concrete texture speckling & formwork lines
        val seamPaint = Paint().apply {
            color = AndroidColor.rgb(20, 22, 26)
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(0f, vh * 0.33f, vw, vh * 0.33f, seamPaint)
        canvas.drawLine(0f, vh * 0.66f, vw, vh * 0.66f, seamPaint)
        canvas.drawLine(vw * 0.5f, 0f, vw * 0.5f, vh, seamPaint)

        // Overpass concrete header shadow at the top
        val shadowPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, vh * 0.25f,
                intArrayOf(AndroidColor.argb(220, 10, 10, 15), AndroidColor.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, vw, vh * 0.25f, shadowPaint)
    }

    private fun drawFreightYard(canvas: Canvas, vw: Float, vh: Float) {
        // Dusk rail yard sky
        val yardSky = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, vh,
                intArrayOf(
                    AndroidColor.rgb(42, 36, 48),
                    AndroidColor.rgb(65, 48, 42),
                    AndroidColor.rgb(32, 28, 26)
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, vw, vh, yardSky)

        // Steel rail lines running across perspective
        val railPaint = Paint().apply {
            color = AndroidColor.rgb(180, 170, 160)
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
        val tiePaint = Paint().apply {
            color = AndroidColor.rgb(40, 32, 28)
            strokeWidth = 8f
            style = Paint.Style.STROKE
        }
        // Ballast gravel ground at bottom
        val groundY = vh * 0.78f
        val ballastPaint = Paint().apply { color = AndroidColor.rgb(38, 34, 30) }
        canvas.drawRect(0f, groundY, vw, vh, ballastPaint)

        // Wooden ties
        for (tx in 0 until vw.toInt() step 30) {
            canvas.drawLine(tx.toFloat(), groundY + 10f, tx.toFloat() + 15f, vh, tiePaint)
        }
        // Twin steel rails
        canvas.drawLine(0f, groundY + 25f, vw, groundY + 25f, railPaint)
        canvas.drawLine(0f, groundY + 55f, vw, groundY + 55f, railPaint)
    }
}
