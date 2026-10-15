package com.example.engine

import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import com.example.model.MascotCharacter
import com.example.model.MascotPosition
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dedicated rendering engine for the 5 street graffiti mascot companions:
 * 1. Spray Can Boy (Aerosol Homie blasting neon cyan mist)
 * 2. Alley Rat (Sewer Crew with red cap & spray can)
 * 3. Cyber Mech Bot (Steampunk robot with goggles & industrial spray gear)
 * 4. Cosmic Astronaut (Zero-G space cadet with star-spray)
 * 5. Street Bulldog (Tough guardian with spiked collar)
 */
object MascotRenderer {

    fun drawMascot(
        canvas: Canvas,
        mascot: MascotCharacter,
        textBounds: Rect,
        textX: Float,
        textY: Float,
        textWidth: Float,
        textHeight: Float,
        vw: Float,
        vh: Float,
        position: MascotPosition = MascotPosition.RIGHT,
        isVerticalLayout: Boolean = false
    ) {
        if (mascot == MascotCharacter.NONE) return

        // Compute mascot size relative to canvas
        val mascotSize = minOf(vw, vh) * 0.38f
        val half = mascotSize / 2f

        // Position computation
        val centerX: Float
        val centerY: Float

        if (isVerticalLayout) {
            // For vertical stacked text, place mascot to the right of middle letters
            centerX = (textX + textWidth + half * 0.7f).coerceAtMost(vw - half * 0.9f)
            centerY = vh * 0.52f
        } else {
            when (position) {
                MascotPosition.RIGHT -> {
                    centerX = (textX + textWidth - half * 0.2f).coerceIn(half * 1.1f, vw - half * 0.85f)
                    centerY = (textY - textHeight * 0.35f).coerceIn(half * 0.9f, vh - half * 0.9f)
                }
                MascotPosition.LEFT -> {
                    centerX = (textX - half * 0.6f).coerceIn(half * 0.85f, vw - half * 1.1f)
                    centerY = (textY - textHeight * 0.35f).coerceIn(half * 0.9f, vh - half * 0.9f)
                }
                MascotPosition.TOP -> {
                    centerX = (vw * 0.5f)
                    centerY = (textY - textHeight - half * 0.4f).coerceIn(half * 0.8f, vh - half)
                }
            }
        }

        drawMascotAt(canvas, mascot, centerX, centerY, mascotSize)
    }

    /**
     * Draws the specified mascot character at an explicit (centerX, centerY) location with custom size.
     * Perfect for direct character add-on stamps on both Freehand and Wildstyle canvases.
     */
    fun drawMascotAt(
        canvas: Canvas,
        mascot: MascotCharacter,
        centerX: Float,
        centerY: Float,
        size: Float
    ) {
        if (mascot == MascotCharacter.NONE) return

        canvas.save()
        canvas.translate(centerX, centerY)

        when (mascot) {
            MascotCharacter.SPRAY_CAN -> drawSprayCan(canvas, size)
            MascotCharacter.ALLEY_RAT -> drawAlleyRat(canvas, size)
            MascotCharacter.MECH_BOT -> drawMechBot(canvas, size)
            MascotCharacter.ASTRO_CADET -> drawAstroCadet(canvas, size)
            MascotCharacter.STREET_BULLDOG -> drawStreetBulldog(canvas, size)
            MascotCharacter.NONE -> { /* No op */ }
        }

        canvas.restore()
    }

    // =========================================================================
    // 1. SPRAY CAN BOY (Aerosol Homie)
    // =========================================================================
    private fun drawSprayCan(canvas: Canvas, size: Float) {
        val s = size / 200f // Normalization scale (200x200 canvas)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.BLACK
            strokeWidth = 6f * s
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val stickerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.WHITE
            strokeWidth = 14f * s
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        // --- A. NEON CYAN SPRAY MIST CLOUD (Shooting from nozzle) ---
        val mistPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                -45f * s, -95f * s, 60f * s,
                intArrayOf(AndroidColor.argb(220, 0, 240, 255), AndroidColor.argb(140, 0, 180, 240), AndroidColor.TRANSPARENT),
                floatArrayOf(0f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(-45f * s, -95f * s, 48f * s, mistPaint)

        // Mist droplets & splatters
        fillPaint.color = AndroidColor.rgb(0, 220, 255)
        canvas.drawCircle(-70f * s, -115f * s, 7f * s, fillPaint)
        canvas.drawCircle(-25f * s, -125f * s, 5f * s, fillPaint)
        canvas.drawCircle(-85f * s, -80f * s, 6f * s, fillPaint)
        canvas.drawCircle(-55f * s, -65f * s, 4f * s, fillPaint)

        // --- B. WHITE STICKER OUTLINE SHADOW ---
        val canRect = RectF(-42f * s, -45f * s, 42f * s, 75f * s)
        canvas.drawRoundRect(canRect, 22f * s, 22f * s, stickerGlowPaint)

        // --- C. SPRAY NOZZLE CAP ---
        // Dome / Valve base
        fillPaint.color = AndroidColor.rgb(200, 205, 215)
        val domeRect = RectF(-26f * s, -62f * s, 26f * s, -40f * s)
        canvas.drawRoundRect(domeRect, 8f * s, 8f * s, fillPaint)
        canvas.drawRoundRect(domeRect, 8f * s, 8f * s, strokePaint)

        // Nozzle actuator button
        fillPaint.color = AndroidColor.rgb(255, 210, 0) // Gold spray cap
        val nozzlePath = Path().apply {
            moveTo(-14f * s, -62f * s)
            lineTo(-18f * s, -86f * s)
            lineTo(18f * s, -86f * s)
            lineTo(14f * s, -62f * s)
            close()
        }
        canvas.drawPath(nozzlePath, fillPaint)
        canvas.drawPath(nozzlePath, strokePaint)

        // Spray hole (orifice)
        fillPaint.color = AndroidColor.BLACK
        canvas.drawCircle(-10f * s, -76f * s, 3.5f * s, fillPaint)

        // --- D. CAN BODY (Purple / Magenta Metallic Gradient) ---
        fillPaint.shader = LinearGradient(
            -42f * s, 0f, 42f * s, 0f,
            intArrayOf(
                AndroidColor.rgb(180, 50, 230),
                AndroidColor.rgb(240, 40, 160),
                AndroidColor.rgb(140, 30, 180),
                AndroidColor.rgb(80, 20, 120)
            ),
            floatArrayOf(0f, 0.45f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(canRect, 20f * s, 20f * s, fillPaint)
        fillPaint.shader = null

        // Specular highlight stripe
        fillPaint.color = AndroidColor.argb(120, 255, 255, 255)
        canvas.drawRoundRect(RectF(-32f * s, -40f * s, -22f * s, 70f * s), 6f * s, 6f * s, fillPaint)

        // Top & bottom metal rims
        fillPaint.color = AndroidColor.rgb(220, 225, 235)
        canvas.drawRoundRect(RectF(-42f * s, -45f * s, 42f * s, -33f * s), 8f * s, 8f * s, fillPaint)
        canvas.drawRoundRect(RectF(-42f * s, 63f * s, 42f * s, 75f * s), 8f * s, 8f * s, fillPaint)

        // Black outline for can
        canvas.drawRoundRect(canRect, 20f * s, 20f * s, strokePaint)

        // --- E. CARTOON FACE (Eyes & Toothy Smirk) ---
        // Left Eye (white)
        fillPaint.color = AndroidColor.WHITE
        canvas.drawOval(RectF(-26f * s, -22f * s, -4f * s, 6f * s), fillPaint)
        canvas.drawOval(RectF(-26f * s, -22f * s, -4f * s, 6f * s), strokePaint)

        // Right Eye (white)
        canvas.drawOval(RectF(4f * s, -22f * s, 26f * s, 6f * s), fillPaint)
        canvas.drawOval(RectF(4f * s, -22f * s, 26f * s, 6f * s), strokePaint)

        // Expressive Pupils looking toward the spray
        fillPaint.color = AndroidColor.BLACK
        canvas.drawCircle(-17f * s, -8f * s, 6f * s, fillPaint)
        canvas.drawCircle(13f * s, -8f * s, 6f * s, fillPaint)

        // Eye highlights (white gleam)
        fillPaint.color = AndroidColor.WHITE
        canvas.drawCircle(-19f * s, -10f * s, 2.5f * s, fillPaint)
        canvas.drawCircle(11f * s, -10f * s, 2.5f * s, fillPaint)

        // Angry / Cheeky brow lines
        val browPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.BLACK
            strokeWidth = 4.5f * s
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(-28f * s, -24f * s, -4f * s, -18f * s, browPaint)
        canvas.drawLine(28f * s, -24f * s, 4f * s, -18f * s, browPaint)

        // Mouth (Wide cheeky grin)
        val mouthPath = Path().apply {
            moveTo(-22f * s, 20f * s)
            quadTo(0f, 48f * s, 22f * s, 20f * s)
            close()
        }
        fillPaint.color = AndroidColor.rgb(40, 10, 20)
        canvas.drawPath(mouthPath, fillPaint)
        canvas.drawPath(mouthPath, strokePaint)

        // White teeth in mouth
        val teethPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(-16f * s, 20f * s, 16f * s, 27f * s, teethPaint)
        canvas.drawLine(-16f * s, 27f * s, 16f * s, 27f * s, strokePaint)

        // --- F. GLOVED CARTOON HANDS ---
        fillPaint.color = AndroidColor.WHITE
        // Left hand gesturing
        canvas.drawCircle(-48f * s, 22f * s, 11f * s, stickerGlowPaint)
        canvas.drawCircle(-48f * s, 22f * s, 10f * s, fillPaint)
        canvas.drawCircle(-48f * s, 22f * s, 10f * s, strokePaint)

        // Right hand giving thumbs up / gripping
        canvas.drawCircle(48f * s, 22f * s, 11f * s, stickerGlowPaint)
        canvas.drawCircle(48f * s, 22f * s, 10f * s, fillPaint)
        canvas.drawCircle(48f * s, 22f * s, 10f * s, strokePaint)
    }

    // =========================================================================
    // 2. ALLEY RAT (Sewer Crew with Red Cap & Aerosol Can)
    // =========================================================================
    private fun drawAlleyRat(canvas: Canvas, size: Float) {
        val s = size / 200f

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.BLACK
            strokeWidth = 6f * s
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val stickerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.WHITE
            strokeWidth = 14f * s
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        // --- A. RAT TAIL (Curving Pink Tail) ---
        val tailPath = Path().apply {
            moveTo(35f * s, 45f * s)
            cubicTo(70f * s, 60f * s, 85f * s, 10f * s, 75f * s, -20f * s)
        }
        val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.rgb(255, 170, 185)
            strokeWidth = 10f * s
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(tailPath, stickerGlowPaint)
        canvas.drawPath(tailPath, tailPaint)
        val tailStroke = Paint(strokePaint).apply { strokeWidth = 3f * s }
        canvas.drawPath(tailPath, tailStroke)

        // --- B. RAT BODY ---
        val bodyRect = RectF(-35f * s, -5f * s, 35f * s, 65f * s)
        canvas.drawRoundRect(bodyRect, 30f * s, 30f * s, stickerGlowPaint)
        fillPaint.color = AndroidColor.rgb(130, 135, 145) // Rat fur gray
        canvas.drawRoundRect(bodyRect, 30f * s, 30f * s, fillPaint)
        canvas.drawRoundRect(bodyRect, 30f * s, 30f * s, strokePaint)

        // Belly patch
        fillPaint.color = AndroidColor.rgb(190, 195, 205)
        canvas.drawRoundRect(RectF(-18f * s, 12f * s, 18f * s, 55f * s), 16f * s, 16f * s, fillPaint)

        // --- C. BIG ROUND RAT EARS ---
        // Left Ear
        fillPaint.color = AndroidColor.rgb(120, 125, 135)
        canvas.drawCircle(-42f * s, -45f * s, 22f * s, stickerGlowPaint)
        canvas.drawCircle(-42f * s, -45f * s, 20f * s, fillPaint)
        canvas.drawCircle(-42f * s, -45f * s, 20f * s, strokePaint)
        fillPaint.color = AndroidColor.rgb(255, 175, 190) // Pink inner ear
        canvas.drawCircle(-42f * s, -45f * s, 13f * s, fillPaint)

        // Right Ear
        fillPaint.color = AndroidColor.rgb(120, 125, 135)
        canvas.drawCircle(42f * s, -45f * s, 22f * s, stickerGlowPaint)
        canvas.drawCircle(42f * s, -45f * s, 20f * s, fillPaint)
        canvas.drawCircle(42f * s, -45f * s, 20f * s, strokePaint)
        fillPaint.color = AndroidColor.rgb(255, 175, 190)
        canvas.drawCircle(42f * s, -45f * s, 13f * s, fillPaint)

        // --- D. RAT HEAD & SNOUT ---
        val headRect = RectF(-32f * s, -52f * s, 32f * s, 0f)
        canvas.drawRoundRect(headRect, 22f * s, 22f * s, stickerGlowPaint)
        fillPaint.color = AndroidColor.rgb(140, 145, 155)
        canvas.drawRoundRect(headRect, 22f * s, 22f * s, fillPaint)
        canvas.drawRoundRect(headRect, 22f * s, 22f * s, strokePaint)

        // Pointy Snout / Muzzle
        val snoutPath = Path().apply {
            moveTo(-18f * s, -20f * s)
            lineTo(0f, -6f * s)
            lineTo(18f * s, -20f * s)
            close()
        }
        fillPaint.color = AndroidColor.rgb(175, 180, 190)
        canvas.drawPath(snoutPath, fillPaint)
        canvas.drawPath(snoutPath, strokePaint)

        // Black nose tip
        fillPaint.color = AndroidColor.rgb(20, 20, 25)
        canvas.drawCircle(0f, -8f * s, 6f * s, fillPaint)

        // Whiskers
        val whiskerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.WHITE
            strokeWidth = 3f * s
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(-6f * s, -9f * s, -38f * s, -14f * s, whiskerPaint)
        canvas.drawLine(-6f * s, -7f * s, -36f * s, -4f * s, whiskerPaint)
        canvas.drawLine(6f * s, -9f * s, 38f * s, -14f * s, whiskerPaint)
        canvas.drawLine(6f * s, -7f * s, 36f * s, -4f * s, whiskerPaint)

        // Cartoon Eyes (Street sly squint)
        fillPaint.color = AndroidColor.WHITE
        canvas.drawOval(RectF(-22f * s, -36f * s, -6f * s, -18f * s), fillPaint)
        canvas.drawOval(RectF(-22f * s, -36f * s, -6f * s, -18f * s), strokePaint)
        canvas.drawOval(RectF(6f * s, -36f * s, 22f * s, -18f * s), fillPaint)
        canvas.drawOval(RectF(6f * s, -36f * s, 22f * s, -18f * s), strokePaint)

        fillPaint.color = AndroidColor.BLACK
        canvas.drawCircle(-13f * s, -26f * s, 5f * s, fillPaint)
        canvas.drawCircle(15f * s, -26f * s, 5f * s, fillPaint)

        // Teeth (white buck teeth)
        fillPaint.color = AndroidColor.WHITE
        canvas.drawRect(-5f * s, -4f * s, 5f * s, 4f * s, fillPaint)
        canvas.drawRect(-5f * s, -4f * s, 5f * s, 4f * s, strokePaint)

        // --- E. RED BACKWARDS BASEBALL CAP ---
        val capRect = RectF(-30f * s, -68f * s, 30f * s, -42f * s)
        fillPaint.color = AndroidColor.rgb(230, 30, 45) // Vivid graffiti red
        canvas.drawRoundRect(capRect, 14f * s, 14f * s, stickerGlowPaint)
        canvas.drawRoundRect(capRect, 14f * s, 14f * s, fillPaint)
        canvas.drawRoundRect(capRect, 14f * s, 14f * s, strokePaint)

        // Cap bill / visor pointing backwards
        val billPath = Path().apply {
            moveTo(-15f * s, -44f * s)
            quadTo(0f, -38f * s, 44f * s, -46f * s)
            lineTo(38f * s, -52f * s)
            close()
        }
        fillPaint.color = AndroidColor.rgb(180, 20, 35)
        canvas.drawPath(billPath, fillPaint)
        canvas.drawPath(billPath, strokePaint)

        // Cap button (top)
        fillPaint.color = AndroidColor.WHITE
        canvas.drawCircle(0f, -68f * s, 4f * s, fillPaint)
        canvas.drawCircle(0f, -68f * s, 4f * s, strokePaint)

        // --- F. RAT HOLDING A MINI SPRAY CAN ---
        // Mini can
        val miniCanRect = RectF(-48f * s, 15f * s, -28f * s, 48f * s)
        fillPaint.color = AndroidColor.rgb(180, 100, 255) // Purple can
        canvas.drawRoundRect(miniCanRect, 5f * s, 5f * s, fillPaint)
        canvas.drawRoundRect(miniCanRect, 5f * s, 5f * s, strokePaint)

        // Mini gold nozzle
        fillPaint.color = AndroidColor.rgb(255, 210, 0)
        canvas.drawRect(-42f * s, 8f * s, -34f * s, 15f * s, fillPaint)
        canvas.drawRect(-42f * s, 8f * s, -34f * s, 15f * s, strokePaint)

        // Spray spray droplets
        fillPaint.color = AndroidColor.rgb(140, 255, 80) // Toxic lime spray
        canvas.drawCircle(-58f * s, 5f * s, 4f * s, fillPaint)
        canvas.drawCircle(-66f * s, 10f * s, 3f * s, fillPaint)
    }

    // =========================================================================
    // 3. CYBER MECH BOT (Steampunk Industrial Burner)
    // =========================================================================
    private fun drawMechBot(canvas: Canvas, size: Float) {
        val s = size / 200f

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.BLACK
            strokeWidth = 6f * s
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val stickerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.WHITE
            strokeWidth = 14f * s
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        // --- A. BACKPACK CYBER TANKS ---
        fillPaint.color = AndroidColor.rgb(230, 100, 30) // Burnt orange tanks
        canvas.drawRoundRect(RectF(-46f * s, -25f * s, -28f * s, 45f * s), 8f * s, 8f * s, stickerGlowPaint)
        canvas.drawRoundRect(RectF(-46f * s, -25f * s, -28f * s, 45f * s), 8f * s, 8f * s, fillPaint)
        canvas.drawRoundRect(RectF(-46f * s, -25f * s, -28f * s, 45f * s), 8f * s, 8f * s, strokePaint)

        canvas.drawRoundRect(RectF(28f * s, -25f * s, 46f * s, 45f * s), 8f * s, 8f * s, stickerGlowPaint)
        canvas.drawRoundRect(RectF(28f * s, -25f * s, 46f * s, 45f * s), 8f * s, 8f * s, fillPaint)
        canvas.drawRoundRect(RectF(28f * s, -25f * s, 46f * s, 45f * s), 8f * s, 8f * s, strokePaint)

        // --- B. INDUSTRIAL ROBOT TORSO ---
        val torsoRect = RectF(-32f * s, -10f * s, 32f * s, 55f * s)
        canvas.drawRoundRect(torsoRect, 12f * s, 12f * s, stickerGlowPaint)
        fillPaint.color = AndroidColor.rgb(65, 80, 95) // Industrial slate metal
        canvas.drawRoundRect(torsoRect, 12f * s, 12f * s, fillPaint)
        canvas.drawRoundRect(torsoRect, 12f * s, 12f * s, strokePaint)

        // Chest vent grill
        fillPaint.color = AndroidColor.rgb(35, 45, 55)
        canvas.drawRoundRect(RectF(-18f * s, 12f * s, 18f * s, 42f * s), 6f * s, 6f * s, fillPaint)
        val grillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.rgb(255, 140, 40) // Orange glowing vents
            strokeWidth = 3f * s
        }
        canvas.drawLine(-14f * s, 20f * s, 14f * s, 20f * s, grillPaint)
        canvas.drawLine(-14f * s, 27f * s, 14f * s, 27f * s, grillPaint)
        canvas.drawLine(-14f * s, 34f * s, 14f * s, 34f * s, grillPaint)

        // --- C. ROBOT HEAD & HELMET ---
        val headRect = RectF(-30f * s, -65f * s, 30f * s, -15f * s)
        canvas.drawRoundRect(headRect, 14f * s, 14f * s, stickerGlowPaint)
        fillPaint.color = AndroidColor.rgb(85, 105, 120)
        canvas.drawRoundRect(headRect, 14f * s, 14f * s, fillPaint)
        canvas.drawRoundRect(headRect, 14f * s, 14f * s, strokePaint)

        // Antenna on top
        val antennaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.BLACK
            strokeWidth = 4.5f * s
        }
        canvas.drawLine(0f, -65f * s, 0f, -85f * s, antennaPaint)
        fillPaint.color = AndroidColor.rgb(255, 140, 0) // Glowing tip
        canvas.drawCircle(0f, -87f * s, 6f * s, fillPaint)
        canvas.drawCircle(0f, -87f * s, 6f * s, strokePaint)

        // Rivets on robot brow
        fillPaint.color = AndroidColor.rgb(200, 210, 220)
        canvas.drawCircle(-22f * s, -58f * s, 2.5f * s, fillPaint)
        canvas.drawCircle(22f * s, -58f * s, 2.5f * s, fillPaint)

        // --- D. GLOWING STEAMPUNK VISOR GOGGLES ---
        val goggleRect = RectF(-26f * s, -48f * s, 26f * s, -26f * s)
        fillPaint.color = AndroidColor.rgb(30, 35, 45)
        canvas.drawRoundRect(goggleRect, 10f * s, 10f * s, fillPaint)
        canvas.drawRoundRect(goggleRect, 10f * s, 10f * s, strokePaint)

        // Glowing orange ocular lenses
        fillPaint.color = AndroidColor.rgb(255, 130, 0)
        canvas.drawCircle(-12f * s, -37f * s, 8f * s, fillPaint)
        canvas.drawCircle(12f * s, -37f * s, 8f * s, fillPaint)

        fillPaint.color = AndroidColor.rgb(255, 230, 140) // Bright core
        canvas.drawCircle(-12f * s, -37f * s, 4f * s, fillPaint)
        canvas.drawCircle(12f * s, -37f * s, 4f * s, fillPaint)

        // Mechanical neck bolts
        fillPaint.color = AndroidColor.rgb(180, 190, 200)
        canvas.drawRect(-36f * s, -14f * s, -30f * s, -6f * s, fillPaint)
        canvas.drawRect(30f * s, -14f * s, 36f * s, -6f * s, fillPaint)
    }

    // =========================================================================
    // 4. COSMIC ASTRONAUT (Zero-G Space Cadet)
    // =========================================================================
    private fun drawAstroCadet(canvas: Canvas, size: Float) {
        val s = size / 200f

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.BLACK
            strokeWidth = 6f * s
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val stickerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.WHITE
            strokeWidth = 14f * s
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        // --- A. COSMIC STARDUST TRAIL ---
        fillPaint.color = AndroidColor.rgb(220, 80, 255)
        canvas.drawCircle(46f * s, 35f * s, 7f * s, fillPaint)
        canvas.drawCircle(58f * s, 20f * s, 5f * s, fillPaint)
        fillPaint.color = AndroidColor.rgb(255, 255, 255)
        canvas.drawCircle(52f * s, 45f * s, 4f * s, fillPaint)

        // --- B. ASTRONAUT SUIT BODY ---
        val suitRect = RectF(-30f * s, -5f * s, 30f * s, 55f * s)
        canvas.drawRoundRect(suitRect, 18f * s, 18f * s, stickerGlowPaint)
        fillPaint.color = AndroidColor.rgb(235, 240, 250) // Space suit white
        canvas.drawRoundRect(suitRect, 18f * s, 18f * s, fillPaint)
        canvas.drawRoundRect(suitRect, 18f * s, 18f * s, strokePaint)

        // Chest Life-Support Module
        fillPaint.color = AndroidColor.rgb(40, 45, 65)
        canvas.drawRoundRect(RectF(-16f * s, 10f * s, 16f * s, 38f * s), 6f * s, 6f * s, fillPaint)
        canvas.drawRoundRect(RectF(-16f * s, 10f * s, 16f * s, 38f * s), 6f * s, 6f * s, strokePaint)

        // Cyan & Pink buttons
        fillPaint.color = AndroidColor.rgb(0, 220, 255)
        canvas.drawCircle(-8f * s, 24f * s, 4f * s, fillPaint)
        fillPaint.color = AndroidColor.rgb(255, 80, 180)
        canvas.drawCircle(8f * s, 24f * s, 4f * s, fillPaint)

        // --- C. BUBBLE HELMET ---
        val helmetRect = RectF(-38f * s, -68f * s, 38f * s, -2f)
        canvas.drawOval(helmetRect, stickerGlowPaint)
        fillPaint.color = AndroidColor.rgb(245, 248, 255)
        canvas.drawOval(helmetRect, fillPaint)
        canvas.drawOval(helmetRect, strokePaint)

        // --- D. DEEP PURPLE / MAGENTA REFLECTIVE VISOR ---
        val visorRect = RectF(-28f * s, -56f * s, 28f * s, -14f * s)
        fillPaint.shader = LinearGradient(
            -28f * s, -56f * s, 28f * s, -14f * s,
            intArrayOf(
                AndroidColor.rgb(180, 30, 220),
                AndroidColor.rgb(100, 15, 140),
                AndroidColor.rgb(25, 5, 45)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(visorRect, 16f * s, 16f * s, fillPaint)
        fillPaint.shader = null
        canvas.drawRoundRect(visorRect, 16f * s, 16f * s, strokePaint)

        // Visor Starlight Reflection (Four-point star flare)
        val flarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = AndroidColor.WHITE
        }
        val flareX = -12f * s
        val flareY = -42f * s
        canvas.drawCircle(flareX, flareY, 3.5f * s, flarePaint)
        canvas.drawLine(flareX - 10f * s, flareY, flareX + 10f * s, flareY, Paint(flarePaint).apply { strokeWidth = 2f * s })
        canvas.drawLine(flareX, flareY - 10f * s, flareX, flareY + 10f * s, Paint(flarePaint).apply { strokeWidth = 2f * s })
    }

    // =========================================================================
    // 5. STREET BULLDOG (Overpass Guard with Spiked Collar)
    // =========================================================================
    private fun drawStreetBulldog(canvas: Canvas, size: Float) {
        val s = size / 200f

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.BLACK
            strokeWidth = 6f * s
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val stickerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.WHITE
            strokeWidth = 14f * s
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        // --- A. BULLDOG FOLDED EARS ---
        // Left Ear
        fillPaint.color = AndroidColor.rgb(150, 115, 80)
        canvas.drawOval(RectF(-48f * s, -65f * s, -24f * s, -30f * s), stickerGlowPaint)
        canvas.drawOval(RectF(-48f * s, -65f * s, -24f * s, -30f * s), fillPaint)
        canvas.drawOval(RectF(-48f * s, -65f * s, -24f * s, -30f * s), strokePaint)

        // Right Ear
        canvas.drawOval(RectF(24f * s, -65f * s, 48f * s, -30f * s), stickerGlowPaint)
        canvas.drawOval(RectF(24f * s, -65f * s, 48f * s, -30f * s), fillPaint)
        canvas.drawOval(RectF(24f * s, -65f * s, 48f * s, -30f * s), strokePaint)

        // --- B. BULLDOG HEAD & WRINKLES ---
        val headRect = RectF(-40f * s, -55f * s, 40f * s, 15f * s)
        canvas.drawRoundRect(headRect, 28f * s, 28f * s, stickerGlowPaint)
        fillPaint.color = AndroidColor.rgb(195, 160, 120) // Bulldog tan/brown
        canvas.drawRoundRect(headRect, 28f * s, 28f * s, fillPaint)
        canvas.drawRoundRect(headRect, 28f * s, 28f * s, strokePaint)

        // Brow wrinkle lines
        val wrinklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.rgb(130, 95, 60)
            strokeWidth = 4f * s
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(-16f * s, -42f * s, 16f * s, -42f * s, wrinklePaint)
        canvas.drawLine(-22f * s, -34f * s, 22f * s, -34f * s, wrinklePaint)

        // --- C. DETERMINED EYES ---
        fillPaint.color = AndroidColor.WHITE
        canvas.drawOval(RectF(-26f * s, -28f * s, -10f * s, -14f * s), fillPaint)
        canvas.drawOval(RectF(-26f * s, -28f * s, -10f * s, -14f * s), strokePaint)
        canvas.drawOval(RectF(10f * s, -28f * s, 26f * s, -14f * s), fillPaint)
        canvas.drawOval(RectF(10f * s, -28f * s, 26f * s, -14f * s), strokePaint)

        fillPaint.color = AndroidColor.BLACK
        canvas.drawCircle(-18f * s, -21f * s, 4.5f * s, fillPaint)
        canvas.drawCircle(18f * s, -21f * s, 4.5f * s, fillPaint)

        // --- D. BROAD MUZZLE, JOWLS & UNDERBITE ---
        val muzzleRect = RectF(-28f * s, -12f * s, 28f * s, 22f * s)
        fillPaint.color = AndroidColor.rgb(65, 50, 40) // Dark muzzle
        canvas.drawRoundRect(muzzleRect, 18f * s, 18f * s, fillPaint)
        canvas.drawRoundRect(muzzleRect, 18f * s, 18f * s, strokePaint)

        // Nose
        fillPaint.color = AndroidColor.rgb(20, 15, 15)
        canvas.drawOval(RectF(-12f * s, -8f * s, 12f * s, 4f * s), fillPaint)

        // White underbite canine teeth
        fillPaint.color = AndroidColor.WHITE
        canvas.drawRect(-18f * s, 8f * s, -10f * s, 19f * s, fillPaint)
        canvas.drawRect(-18f * s, 8f * s, -10f * s, 19f * s, strokePaint)
        canvas.drawRect(10f * s, 8f * s, 18f * s, 19f * s, fillPaint)
        canvas.drawRect(10f * s, 8f * s, 18f * s, 19f * s, strokePaint)

        // --- E. RED LEATHER SPIKED COLLAR ---
        val collarRect = RectF(-38f * s, 18f * s, 38f * s, 38f * s)
        canvas.drawRoundRect(collarRect, 8f * s, 8f * s, stickerGlowPaint)
        fillPaint.color = AndroidColor.rgb(220, 30, 45) // Red collar
        canvas.drawRoundRect(collarRect, 8f * s, 8f * s, fillPaint)
        canvas.drawRoundRect(collarRect, 8f * s, 8f * s, strokePaint)

        // Silver conical spikes
        fillPaint.color = AndroidColor.rgb(235, 240, 250) // Metallic silver
        val spikePaint = Paint(strokePaint).apply { strokeWidth = 3f * s }
        // Left spike
        val spike1 = Path().apply {
            moveTo(-28f * s, 22f * s)
            lineTo(-38f * s, 28f * s)
            lineTo(-28f * s, 34f * s)
            close()
        }
        canvas.drawPath(spike1, fillPaint)
        canvas.drawPath(spike1, spikePaint)

        // Center spikes
        val spike2 = Path().apply {
            moveTo(-10f * s, 18f * s)
            lineTo(-6f * s, 6f * s)
            lineTo(-2f * s, 18f * s)
            close()
        }
        canvas.drawPath(spike2, fillPaint)
        canvas.drawPath(spike2, spikePaint)

        val spike3 = Path().apply {
            moveTo(2f * s, 18f * s)
            lineTo(6f * s, 6f * s)
            lineTo(10f * s, 18f * s)
            close()
        }
        canvas.drawPath(spike3, fillPaint)
        canvas.drawPath(spike3, spikePaint)

        // Right spike
        val spike4 = Path().apply {
            moveTo(28f * s, 22f * s)
            lineTo(38f * s, 28f * s)
            lineTo(28f * s, 34f * s)
            close()
        }
        canvas.drawPath(spike4, fillPaint)
        canvas.drawPath(spike4, spikePaint)

        // Gold collar buckle tag
        fillPaint.color = AndroidColor.rgb(255, 210, 0)
        canvas.drawCircle(0f, 32f * s, 6f * s, fillPaint)
        canvas.drawCircle(0f, 32f * s, 6f * s, strokePaint)

        // --- F. PAWS RESTING OVER LETTERS ---
        fillPaint.color = AndroidColor.rgb(195, 160, 120)
        val leftPaw = RectF(-36f * s, 36f * s, -14f * s, 54f * s)
        canvas.drawRoundRect(leftPaw, 8f * s, 8f * s, stickerGlowPaint)
        canvas.drawRoundRect(leftPaw, 8f * s, 8f * s, fillPaint)
        canvas.drawRoundRect(leftPaw, 8f * s, 8f * s, strokePaint)

        val rightPaw = RectF(14f * s, 36f * s, 36f * s, 54f * s)
        canvas.drawRoundRect(rightPaw, 8f * s, 8f * s, stickerGlowPaint)
        canvas.drawRoundRect(rightPaw, 8f * s, 8f * s, fillPaint)
        canvas.drawRoundRect(rightPaw, 8f * s, 8f * s, strokePaint)
    }
}
