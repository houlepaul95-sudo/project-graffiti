package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.BrushType
import com.example.model.DrawPoint
import com.example.model.DripDrop
import com.example.model.FreehandStroke
import com.example.model.MascotCharacter
import com.example.model.StreetStencil
import com.example.model.WallType
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object GraffitiBrushEngine {

    private val solidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val sprayMistPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val eraserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    /**
     * Renders a complete freehand stroke or stencil stamp onto an Android Canvas
     */
    fun drawStroke(canvas: Canvas, stroke: FreehandStroke) {
        // If stroke is a Mascot Character stamp
        if (stroke.mascot != null && stroke.mascot != MascotCharacter.NONE && stroke.points.isNotEmpty()) {
            val pt = stroke.points[0]
            val mascotSize = (stroke.strokeWidth * 3.5f).coerceIn(120f, 360f)
            MascotRenderer.drawMascotAt(canvas, stroke.mascot, pt.x, pt.y, mascotSize)
            return
        }

        // If stroke is a Stencil stamp
        if (stroke.stencil != null && stroke.points.isNotEmpty()) {
            val pt = stroke.points[0]
            drawStencil(canvas, stroke.stencil, pt.x, pt.y, stroke.strokeWidth, stroke.color)
            return
        }

        val points = stroke.points
        if (points.isEmpty()) return

        if (points.size == 1) {
            drawDot(canvas, points[0], stroke.brushType, stroke.color, stroke.strokeWidth)
            return
        }

        for (i in 0 until points.size - 1) {
            drawSegment(canvas, points[i], points[i + 1], stroke.brushType, stroke.color, stroke.strokeWidth)
        }

        // Render drips associated with this stroke
        for (drip in stroke.drips) {
            drawDrip(canvas, drip)
        }
    }

    /**
     * Draws an isolated point/tap
     */
    fun drawDot(canvas: Canvas, point: DrawPoint, brushType: BrushType, color: Int, strokeWidth: Float) {
        when (brushType) {
            BrushType.SPRAY, BrushType.FAT_CAP -> {
                drawSprayCone(canvas, point.x, point.y, strokeWidth, color, if (brushType == BrushType.FAT_CAP) 40 else 24)
            }
            BrushType.FLARE_CAP -> {
                // Wide flared aerosol burst with high particle count and soft outer mist
                drawSprayCone(canvas, point.x, point.y, strokeWidth * 1.3f, color, 48)
            }
            BrushType.MOP -> {
                // Heavy juicy ink puddle with wet outline
                fillPaint.color = color
                canvas.drawCircle(point.x, point.y, strokeWidth * 0.55f, fillPaint)
                val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = color
                    alpha = 140
                    style = Paint.Style.STROKE
                    this.strokeWidth = 3f
                }
                canvas.drawCircle(point.x, point.y, strokeWidth * 0.58f, rimPaint)
            }
            BrushType.CHISEL -> {
                val nx = strokeWidth * 0.7071f
                val ny = -strokeWidth * 0.7071f
                fillPaint.color = color
                canvas.drawCircle(point.x, point.y, strokeWidth * 0.5f, fillPaint)
            }
            BrushType.DRIP -> {
                fillPaint.color = color
                canvas.drawCircle(point.x, point.y, strokeWidth * 0.5f, fillPaint)
            }
            BrushType.ERASER -> {
                eraserPaint.strokeWidth = strokeWidth
                canvas.drawCircle(point.x, point.y, strokeWidth * 0.5f, eraserPaint)
            }
        }
    }

    /**
     * Draws a line segment between two consecutive points
     */
    fun drawSegment(
        canvas: Canvas,
        from: DrawPoint,
        to: DrawPoint,
        brushType: BrushType,
        color: Int,
        strokeWidth: Float
    ) {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val dist = hypot(dx, dy)

        when (brushType) {
            BrushType.SPRAY -> {
                val stepSize = 3.5f
                val steps = (dist / stepSize).toInt().coerceAtLeast(1)
                for (s in 0..steps) {
                    val t = s.toFloat() / steps
                    val cx = from.x + dx * t
                    val cy = from.y + dy * t
                    drawSprayCone(canvas, cx, cy, strokeWidth, color, 14)
                }
            }

            BrushType.FLARE_CAP -> {
                // Speed-sensitive downtown aerosol flare
                val stepSize = 3.8f
                val steps = (dist / stepSize).toInt().coerceAtLeast(1)
                val flareFactor = (dist * 0.18f).coerceIn(1.0f, 1.55f)
                for (s in 0..steps) {
                    val t = s.toFloat() / steps
                    val cx = from.x + dx * t
                    val cy = from.y + dy * t
                    drawSprayCone(canvas, cx, cy, strokeWidth * flareFactor, color, 12)
                }
            }

            BrushType.FAT_CAP -> {
                // Outer soft overspray halo
                val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = color
                    alpha = 60
                    this.strokeWidth = strokeWidth * 1.35f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                }
                canvas.drawLine(from.x, from.y, to.x, to.y, haloPaint)

                // Dense center core
                solidPaint.color = color
                solidPaint.alpha = 255
                solidPaint.strokeWidth = strokeWidth
                canvas.drawLine(from.x, from.y, to.x, to.y, solidPaint)

                // Splatter particles along edge
                val stepSize = 8f
                val steps = (dist / stepSize).toInt().coerceAtLeast(1)
                for (s in 0..steps) {
                    val t = s.toFloat() / steps
                    val cx = from.x + dx * t
                    val cy = from.y + dy * t
                    drawSprayCone(canvas, cx, cy, strokeWidth * 0.8f, color, 6)
                }
            }

            BrushType.MOP -> {
                // Ultra-thick, juicy liquid paint stroke
                solidPaint.color = color
                solidPaint.alpha = 255
                solidPaint.strokeWidth = strokeWidth * 1.15f
                canvas.drawLine(from.x, from.y, to.x, to.y, solidPaint)
            }

            BrushType.CHISEL -> {
                // Authentic 45-degree flat chisel calligraphy marker polygon
                val nx = strokeWidth * 0.7071f
                val ny = -strokeWidth * 0.7071f

                val path = Path().apply {
                    moveTo(from.x - nx, from.y - ny)
                    lineTo(from.x + nx, from.y + ny)
                    lineTo(to.x + nx, to.y + ny)
                    lineTo(to.x - nx, to.y - ny)
                    close()
                }

                fillPaint.color = color
                fillPaint.alpha = 255
                canvas.drawPath(path, fillPaint)
            }

            BrushType.DRIP -> {
                // Thick liquid paint stroke
                solidPaint.color = color
                solidPaint.alpha = 255
                solidPaint.strokeWidth = strokeWidth
                canvas.drawLine(from.x, from.y, to.x, to.y, solidPaint)
            }

            BrushType.ERASER -> {
                eraserPaint.strokeWidth = strokeWidth
                canvas.drawLine(from.x, from.y, to.x, to.y, eraserPaint)
            }
        }
    }

    /**
     * Renders a realistic aerosol spray can dispersion cone
     */
    private fun drawSprayCone(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, particleCount: Int) {
        val r = radius * 0.5f

        // 1. Soft aerosol center mist
        val baseAlpha = AndroidColor.alpha(color)
        sprayMistPaint.color = color
        sprayMistPaint.alpha = (baseAlpha * 0.12f).toInt().coerceIn(10, 40)
        canvas.drawCircle(cx, cy, r * 0.6f, sprayMistPaint)

        // 2. High velocity aerosol specks
        for (i in 0 until particleCount) {
            val theta = Random.nextFloat() * (2 * PI.toFloat())
            // Non-linear falloff (denser in center, feathering out to rim)
            val distFactor = Random.nextFloat()
            val dist = r * sqrt(distFactor)
            val px = cx + cos(theta) * dist
            val py = cy + sin(theta) * dist

            val speckRadius = (Random.nextFloat() * 1.5f + 0.6f)
            val speckAlpha = (baseAlpha * (0.4f + 0.55f * (1f - dist / r))).toInt().coerceIn(40, 255)

            fillPaint.color = color
            fillPaint.alpha = speckAlpha
            canvas.drawCircle(px, py, speckRadius, fillPaint)
        }

        // 3. Occasional stray overspray splat outside main nozzle
        if (Random.nextFloat() < 0.08f) {
            val strayTheta = Random.nextFloat() * (2 * PI.toFloat())
            val strayDist = r * (1.15f + Random.nextFloat() * 0.45f)
            fillPaint.color = color
            fillPaint.alpha = (baseAlpha * 0.7f).toInt().coerceIn(30, 220)
            canvas.drawCircle(
                cx + cos(strayTheta) * strayDist,
                cy + sin(strayTheta) * strayDist,
                Random.nextFloat() * 1.8f + 0.8f,
                fillPaint
            )
        }
    }

    /**
     * Draws a single gravity paint drip with tapered trail and teardrop droplet
     */
    fun drawDrip(canvas: Canvas, drip: DripDrop) {
        val startX = drip.startX
        val startY = drip.startY
        val length = drip.length
        val endY = startY + length
        val tipR = drip.tipRadius

        val dripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = drip.color
            style = Paint.Style.FILL
        }

        // Tapered drip path
        val topWidth = tipR * 1.1f
        val bottomWidth = tipR * 0.75f
        val path = Path().apply {
            moveTo(startX - topWidth, startY)
            lineTo(startX + topWidth, startY)
            lineTo(startX + bottomWidth, endY - tipR * 0.5f)
            lineTo(startX - bottomWidth, endY - tipR * 0.5f)
            close()
        }
        canvas.drawPath(path, dripPaint)

        // Rounded teardrop bead at bottom
        canvas.drawCircle(startX, endY, tipR, dripPaint)

        // Wet glossy highlight reflection on the droplet
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            alpha = 180
            style = Paint.Style.FILL
        }
        canvas.drawCircle(startX - tipR * 0.3f, endY - tipR * 0.3f, tipR * 0.35f, highlightPaint)
    }

    /**
     * Generates a drip drop based on a touch point and brush size
     */
    fun createDripFromPoint(x: Float, y: Float, strokeWidth: Float, color: Int, dripMultiplier: Float = 1f): DripDrop {
        val baseLength = strokeWidth * (1.8f + Random.nextFloat() * 2.5f) * dripMultiplier
        val tipR = (strokeWidth * 0.28f).coerceAtLeast(2.5f)
        return DripDrop(
            startX = x,
            startY = y,
            length = baseLength,
            tipRadius = tipR,
            color = color
        )
    }

    /**
     * Composites a wall texture bitmap with the user's freehand drawing layer
     */
    fun compositeArtwork(
        width: Int,
        height: Int,
        wallType: WallType,
        drawingBitmap: Bitmap,
        context: Context
    ): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 1. Draw wall background
        val config = com.example.model.GraffitiConfig(
            wallIndex = com.example.model.GraffitiPalettes.WALLS.indexOfFirst { it.type == wallType }.coerceAtLeast(0)
        )
        GraffitiRenderer.drawWall(canvas, width.toFloat(), height.toFloat(), config, context)

        // 2. Composite drawing on top
        canvas.drawBitmap(drawingBitmap, 0f, 0f, null)

        return result
    }

    /**
     * Saves the drawn canvas artwork to device Pictures gallery
     */
    fun saveDrawnArtwork(
        context: Context,
        drawingBitmap: Bitmap,
        wallType: WallType,
        isTransparent: Boolean = false
    ): Result<Uri> {
        return try {
            val finalBitmap = if (isTransparent || wallType == WallType.TRANSPARENT) {
                drawingBitmap
            } else {
                compositeArtwork(drawingBitmap.width, drawingBitmap.height, wallType, drawingBitmap, context)
            }

            val filename = "Graffiti_Freehand_${System.currentTimeMillis()}.png"
            val uri: Uri?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/GraffitiCreator")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return Result.failure(Exception("Failed to create MediaStore entry"))

                resolver.openOutputStream(imageUri)?.use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, values, null, null)
                uri = imageUri
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "GraffitiCreator").apply { mkdirs() }
                val file = File(appDir, filename)
                FileOutputStream(file).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                uri = Uri.fromFile(file)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stencils & Slap Stickers Rack:
     * Renders authentic downtown street art stencils and slaps directly onto the canvas.
     */
    fun drawStencil(canvas: Canvas, stencil: StreetStencil, cx: Float, cy: Float, size: Float, color: Int) {
        val s = (size * 2.2f).coerceIn(40f, 160f)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4.5f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.color = AndroidColor.BLACK
        }
        val mainFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = color
        }
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = AndroidColor.WHITE
        }

        // Soft aerosol spray halo behind stencil
        drawSprayCone(canvas, cx, cy, s * 0.9f, color, 28)

        when (stencil) {
            StreetStencil.CROWN -> {
                // Classic 3-point street king crown
                val w = s * 0.55f
                val h = s * 0.42f
                val path = Path().apply {
                    moveTo(cx - w, cy + h * 0.4f)
                    lineTo(cx + w, cy + h * 0.4f)
                    lineTo(cx + w * 0.85f, cy - h * 0.45f)
                    lineTo(cx + w * 0.35f, cy)
                    lineTo(cx, cy - h * 0.85f)
                    lineTo(cx - w * 0.35f, cy)
                    lineTo(cx - w * 0.85f, cy - h * 0.45f)
                    close()
                }
                canvas.drawPath(path, mainFillPaint)
                canvas.drawPath(path, strokePaint)

                // 3 Tip jewel circles
                canvas.drawCircle(cx - w * 0.85f, cy - h * 0.45f, 5f, whitePaint)
                canvas.drawCircle(cx, cy - h * 0.85f, 6.5f, whitePaint)
                canvas.drawCircle(cx + w * 0.85f, cy - h * 0.45f, 5f, whitePaint)

                canvas.drawCircle(cx - w * 0.85f, cy - h * 0.45f, 5f, strokePaint)
                canvas.drawCircle(cx, cy - h * 0.85f, 6.5f, strokePaint)
                canvas.drawCircle(cx + w * 0.85f, cy - h * 0.45f, 5f, strokePaint)
            }

            StreetStencil.LIGHTNING -> {
                // High-voltage street shocker bolt
                val w = s * 0.35f
                val h = s * 0.75f
                val path = Path().apply {
                    moveTo(cx - w * 0.2f, cy - h * 0.5f)
                    lineTo(cx + w * 0.9f, cy - h * 0.5f)
                    lineTo(cx + w * 0.1f, cy - h * 0.05f)
                    lineTo(cx + w * 0.7f, cy - h * 0.05f)
                    lineTo(cx - w * 0.8f, cy + h * 0.55f)
                    lineTo(cx - w * 0.15f, cy + h * 0.08f)
                    lineTo(cx - w * 0.7f, cy + h * 0.08f)
                    close()
                }
                canvas.drawPath(path, mainFillPaint)
                canvas.drawPath(path, strokePaint)
                // Center inner shine
                val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = AndroidColor.WHITE
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                }
                canvas.drawLine(cx, cy - h * 0.35f, cx + w * 0.2f, cy - h * 0.1f, innerPaint)
            }

            StreetStencil.BOOMBOX -> {
                // Retro 1980s street cassette blaster
                val bw = s * 0.65f
                val bh = s * 0.42f
                val bodyRect = android.graphics.RectF(cx - bw, cy - bh * 0.5f, cx + bw, cy + bh * 0.8f)
                canvas.drawRoundRect(bodyRect, 10f, 10f, mainFillPaint)
                canvas.drawRoundRect(bodyRect, 10f, 10f, strokePaint)

                // Handle arch on top
                val handleRect = android.graphics.RectF(cx - bw * 0.45f, cy - bh * 0.9f, cx + bw * 0.45f, cy - bh * 0.35f)
                val handlePaint = Paint(strokePaint).apply { strokeWidth = 5f }
                canvas.drawRoundRect(handleRect, 6f, 6f, handlePaint)

                // Twin speaker circles
                val spkRadius = bh * 0.38f
                val leftSpkX = cx - bw * 0.52f
                val rightSpkX = cx + bw * 0.52f
                val spkY = cy + bh * 0.15f

                val spkBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = AndroidColor.BLACK
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(leftSpkX, spkY, spkRadius, spkBg)
                canvas.drawCircle(rightSpkX, spkY, spkRadius, spkBg)
                canvas.drawCircle(leftSpkX, spkY, spkRadius * 0.45f, whitePaint)
                canvas.drawCircle(rightSpkX, spkY, spkRadius * 0.45f, whitePaint)

                // Cassette door
                val deckRect = android.graphics.RectF(cx - bw * 0.2f, cy - bh * 0.15f, cx + bw * 0.2f, cy + bh * 0.45f)
                canvas.drawRoundRect(deckRect, 4f, 4f, strokePaint)
            }

            StreetStencil.STAR_FLARE -> {
                // 4-point aerosol glint star
                val r = s * 0.55f
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    quadTo(cx, cy, cx + r, cy)
                    quadTo(cx, cy, cx, cy + r)
                    quadTo(cx, cy, cx - r, cy)
                    quadTo(cx, cy, cx, cy - r)
                    close()
                }
                canvas.drawPath(path, mainFillPaint)
                canvas.drawPath(path, strokePaint)
                canvas.drawCircle(cx, cy, r * 0.22f, whitePaint)
            }

            StreetStencil.SKULL -> {
                // Downtown street skater skull
                val r = s * 0.4f
                val skullPath = Path().apply {
                    addCircle(cx, cy - r * 0.25f, r * 0.75f, Path.Direction.CW)
                }
                canvas.drawPath(skullPath, mainFillPaint)

                // Jaw
                val jawRect = android.graphics.RectF(cx - r * 0.45f, cy + r * 0.2f, cx + r * 0.45f, cy + r * 0.85f)
                canvas.drawRoundRect(jawRect, 6f, 6f, mainFillPaint)
                canvas.drawRoundRect(jawRect, 6f, 6f, strokePaint)
                canvas.drawPath(skullPath, strokePaint)

                // Eye sockets
                val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = AndroidColor.BLACK
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(cx - r * 0.32f, cy - r * 0.2f, r * 0.22f, eyePaint)
                canvas.drawCircle(cx + r * 0.32f, cy - r * 0.2f, r * 0.22f, eyePaint)
                // Nose triangle
                val nosePath = Path().apply {
                    moveTo(cx, cy + r * 0.05f)
                    lineTo(cx - r * 0.12f, cy + r * 0.22f)
                    lineTo(cx + r * 0.12f, cy + r * 0.22f)
                    close()
                }
                canvas.drawPath(nosePath, eyePaint)
                // Teeth lines
                canvas.drawLine(cx - r * 0.25f, cy + r * 0.55f, cx + r * 0.25f, cy + r * 0.55f, strokePaint)
                canvas.drawLine(cx - r * 0.12f, cy + r * 0.4f, cx - r * 0.12f, cy + r * 0.7f, strokePaint)
                canvas.drawLine(cx + r * 0.12f, cy + r * 0.4f, cx + r * 0.12f, cy + r * 0.7f, strokePaint)
            }

            StreetStencil.SLAP_STICKER -> {
                // Classic "HELLO MY NAME IS" vinyl slap sticker
                val sw = s * 0.65f
                val sh = s * 0.42f
                val stickerRect = android.graphics.RectF(cx - sw, cy - sh, cx + sw, cy + sh)
                val slapBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = AndroidColor.WHITE
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(stickerRect, 8f, 8f, slapBg)
                canvas.drawRoundRect(stickerRect, 8f, 8f, strokePaint)

                // Red top banner
                val bannerRect = android.graphics.RectF(cx - sw, cy - sh, cx + sw, cy - sh * 0.3f)
                val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = AndroidColor.rgb(235, 45, 55)
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(bannerRect, 8f, 8f, bannerPaint)

                val tagTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = AndroidColor.WHITE
                    textSize = sh * 0.4f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("HELLO", cx, cy - sh * 0.45f, tagTextPaint)

                // Subtitle
                val subPaint = Paint(tagTextPaint).apply {
                    this.color = AndroidColor.BLACK
                    textSize = sh * 0.2f
                    isFakeBoldText = true
                }
                canvas.drawText("MY NAME IS", cx, cy - sh * 0.08f, subPaint)

                // Downtown signature graffiti scribble across sticker
                val inkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = color
                    strokeWidth = 3.5f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                }
                val tagPath = Path().apply {
                    moveTo(cx - sw * 0.65f, cy + sh * 0.45f)
                    quadTo(cx - sw * 0.2f, cy + sh * 0.15f, cx, cy + sh * 0.45f)
                    quadTo(cx + sw * 0.3f, cy + sh * 0.65f, cx + sw * 0.65f, cy + sh * 0.35f)
                }
                canvas.drawPath(tagPath, inkPaint)
            }
        }
    }

    /**
     * Downtown Street FX: Detonates an aerosol splatter bomb of micro-droplets
     */
    fun drawSplatterBomb(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, count: Int = 36) {
        val splatterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        val rng = Random(System.currentTimeMillis())
        for (i in 0 until count) {
            val angle = rng.nextFloat() * 2f * PI.toFloat()
            val dist = rng.nextFloat() * radius * 1.35f
            val px = cx + cos(angle) * dist
            val py = cy + sin(angle) * dist
            val dotRadius = (rng.nextFloat() * 4.5f + 1f) * (1f - dist / (radius * 1.5f)).coerceAtLeast(0.3f)
            splatterPaint.alpha = (rng.nextFloat() * 150 + 105).toInt()
            canvas.drawCircle(px, py, dotRadius, splatterPaint)
        }
    }
}
