package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.GraffitiConfig
import com.example.model.GraffitiFontStyle
import com.example.model.GraffitiPalettes
import com.example.model.MascotCharacter
import com.example.model.MascotPosition
import com.example.model.WallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiGraffitiDesign(
    val tagText: String,
    val styleTitle: String,
    val description: String,
    val artistAdvice: String,
    val fontStyle: GraffitiFontStyle,
    val fillColorIndex: Int,
    val isGradient: Boolean,
    val gradientColorIndex: Int,
    val outlineColorIndex: Int,
    val wallType: WallType,
    val mascot: MascotCharacter,
    val mascotPosition: MascotPosition = MascotPosition.RIGHT,
    val outlineThickness: Float = 16f,
    val shadowDepth: Float = 18f,
    val highlights: Boolean = true,
    val drips: Boolean = true,
    val splats: Boolean = true
) {
    fun toGraffitiConfig(baseConfig: GraffitiConfig): GraffitiConfig {
        val wallIdx = GraffitiPalettes.WALLS.indexOfFirst { it.type == wallType }.coerceAtLeast(0)
        return baseConfig.copy(
            text = tagText.uppercase(),
            fontStyle = fontStyle,
            fillColorIndex = fillColorIndex.coerceIn(0, GraffitiPalettes.FILLS.lastIndex),
            isGradient = isGradient,
            gradientColorIndex = gradientColorIndex.coerceIn(0, GraffitiPalettes.FILLS.lastIndex),
            outlineColorIndex = outlineColorIndex.coerceIn(0, GraffitiPalettes.OUTLINES.lastIndex),
            wallIndex = wallIdx,
            mascot = mascot,
            mascotPosition = mascotPosition,
            outlineThickness = outlineThickness,
            shadowDepth = shadowDepth,
            highlights = highlights,
            drips = drips,
            splats = splats
        )
    }
}

object GeminiGraffitiService {
    private const val TAG = "GeminiGraffitiService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Generates custom wildstyle graffiti attributes using Gemini 3.5 Flash
     */
    suspend fun generateCustomGraffiti(
        userPrompt: String,
        currentConfig: GraffitiConfig
    ): Result<GeminiGraffitiDesign> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent fallback heuristic when API key is not configured in Secrets
            return@withContext Result.success(generateHeuristicDesign(userPrompt, currentConfig))
        }

        try {
            val systemPrompt = """
                You are an expert NYC and European legendary street graffiti artist and graphic designer.
                Given a user prompt or theme, design custom graffiti graphics and return ONLY valid JSON matching this schema:
                {
                  "tagText": "1-8 uppercase characters max without spaces, e.g. BURNER, VIBE, PSYCHO, TOXIC",
                  "styleTitle": "Creative graffiti name e.g. Toxic Subway Wholecar",
                  "description": "Short 1-2 sentence description of the artwork style and aesthetic vibe",
                  "artistAdvice": "One pro graffiti aerosol spray tip for this specific piece",
                  "fontStyle": "One of: WILDSTYLE, DON_GRAFFITI, SISTER_SPRAY, MOST_WASTED, URBAN_JUNGLE, SEMI_WILD",
                  "fillColorIndex": 0 to 14 integer,
                  "isGradient": boolean,
                  "gradientColorIndex": 0 to 14 integer,
                  "outlineColorIndex": 0 to 4 integer,
                  "wallType": "One of: SUBWAY_CAR, DARK_ALLEY, ROLLUP_SHUTTER, FREIGHT_YARD, CONCRETE_SLAB, BRICK_WAREHOUSE, ROOFTOP_SKYLINE",
                  "mascot": "One of: SPRAY_CAN, ALLEY_RAT, MECH_BOT, ASTRO_CADET, STREET_BULLDOG, NONE",
                  "mascotPosition": "One of: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT",
                  "outlineThickness": number between 12 and 22,
                  "shadowDepth": number between 14 and 26,
                  "highlights": boolean,
                  "drips": boolean,
                  "bubbles": boolean,
                  "cracks": boolean
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemPrompt\n\nUser request: $userPrompt")
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string().orEmpty()
                Log.w(TAG, "Gemini API error ($response.code): $errBody - Falling back to heuristic")
                return@withContext Result.success(generateHeuristicDesign(userPrompt, currentConfig))
            }

            val respBody = response.body?.string().orEmpty()
            val parsedDesign = parseGeminiResponse(respBody)
                ?: generateHeuristicDesign(userPrompt, currentConfig)

            Result.success(parsedDesign)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying Gemini API", e)
            Result.success(generateHeuristicDesign(userPrompt, currentConfig))
        }
    }

    /**
     * Generates a curated list of creative graffiti tags and crew aliases
     */
    suspend fun suggestGraffitiTags(theme: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(generateFallbackTags(theme))
        }

        try {
            val prompt = "Generate 6 legendary, punchy graffiti artist tag names (3-6 letters each, uppercase, no numbers) matching the theme: $theme. Return as JSON array of strings: [\"TAG1\", \"TAG2\", ...]"
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.8)
                    put("responseMimeType", "application/json")
                })
            }

            val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.success(generateFallbackTags(theme))
            }

            val respText = response.body?.string().orEmpty()
            val root = JSONObject(respText)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text").orEmpty()

            val array = JSONArray(text.trim())
            val result = mutableListOf<String>()
            for (i in 0 until array.length()) {
                val tag = array.optString(i).trim().uppercase()
                if (tag.isNotBlank()) result.add(tag)
            }
            Result.success(if (result.isNotEmpty()) result else generateFallbackTags(theme))
        } catch (e: Exception) {
            Result.success(generateFallbackTags(theme))
        }
    }

    private fun parseGeminiResponse(jsonString: String): GeminiGraffitiDesign? {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: return null

            val json = JSONObject(text.trim())
            val tag = json.optString("tagText", "WILD").trim().uppercase().take(8)
            val styleTitle = json.optString("styleTitle", "Gemini AI Wildstyle")
            val description = json.optString("description", "Custom graffiti crafted with Gemini AI design engine.")
            val artistAdvice = json.optString("artistAdvice", "Use thin chisel lines for tight negative space and fat caps for outer mists.")

            val fontStyleStr = json.optString("fontStyle", "DON_GRAFFITI")
            val fontStyle = GraffitiFontStyle.fromNameOrDefault(fontStyleStr)

            val wallStr = json.optString("wallType", "SUBWAY_CAR")
            val wallType = runCatching { WallType.valueOf(wallStr) }.getOrDefault(WallType.SUBWAY_CAR)

            val mascotStr = json.optString("mascot", "SPRAY_CAN")
            val mascot = runCatching { MascotCharacter.valueOf(mascotStr) }.getOrDefault(MascotCharacter.SPRAY_CAN)

            val posStr = json.optString("mascotPosition", "RIGHT")
            val mascotPosition = runCatching { MascotPosition.valueOf(posStr) }.getOrDefault(MascotPosition.RIGHT)

            GeminiGraffitiDesign(
                tagText = if (tag.isNotBlank()) tag else "WILD",
                styleTitle = styleTitle,
                description = description,
                artistAdvice = artistAdvice,
                fontStyle = fontStyle,
                fillColorIndex = json.optInt("fillColorIndex", 1),
                isGradient = json.optBoolean("isGradient", true),
                gradientColorIndex = json.optInt("gradientColorIndex", 0),
                outlineColorIndex = json.optInt("outlineColorIndex", 0),
                wallType = wallType,
                mascot = mascot,
                mascotPosition = mascotPosition,
                outlineThickness = json.optDouble("outlineThickness", 16.0).toFloat(),
                shadowDepth = json.optDouble("shadowDepth", 18.0).toFloat(),
                highlights = json.optBoolean("highlights", true),
                drips = json.optBoolean("drips", true),
                splats = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response JSON", e)
            null
        }
    }

    private fun generateHeuristicDesign(prompt: String, currentConfig: GraffitiConfig): GeminiGraffitiDesign {
        val lower = prompt.lowercase()
        val tagWords = prompt.trim().split(" ", "-", "_").filter { it.isNotBlank() }
        val extractedTag = tagWords.firstOrNull { it.length in 2..7 }?.uppercase() ?: currentConfig.text.ifBlank { "AERO" }

        return when {
            lower.contains("toxic") || lower.contains("acid") || lower.contains("bio") -> {
                GeminiGraffitiDesign(
                    tagText = extractedTag.ifBlank { "TOXIC" },
                    styleTitle = "Biohazard Toxic Interlock",
                    description = "Vibrant lime green interlocking lettering with neon magenta halo and radioactive drips.",
                    artistAdvice = "Spray acid mist from 30cm away to blend the toxic lime gradient into bright lemon yellow.",
                    fontStyle = GraffitiFontStyle.SISTER_SPRAY,
                    fillColorIndex = 3, // Toxic Lime
                    isGradient = true,
                    gradientColorIndex = 2, // Acid Yellow
                    outlineColorIndex = 0, // Neon Pink
                    wallType = WallType.DARK_ALLEY,
                    mascot = MascotCharacter.SPRAY_CAN,
                    mascotPosition = MascotPosition.RIGHT,
                    outlineThickness = 16f,
                    shadowDepth = 18f,
                    highlights = true,
                    drips = true,
                    splats = true
                )
            }
            lower.contains("cyber") || lower.contains("mech") || lower.contains("robot") || lower.contains("future") -> {
                GeminiGraffitiDesign(
                    tagText = extractedTag.ifBlank { "CYBER" },
                    styleTitle = "Cyberpunk Neo-Tokyo Mech",
                    description = "High-velocity electric cyan lettering with burnt rust 3D extrusions and robotic companion.",
                    artistAdvice = "Keep outlines razor sharp at 45° chisel cuts to emphasize the high-tech industrial armor plates.",
                    fontStyle = GraffitiFontStyle.URBAN_JUNGLE,
                    fillColorIndex = 1, // Cyan
                    isGradient = true,
                    gradientColorIndex = 11, // Magenta
                    outlineColorIndex = 1, // Black
                    wallType = WallType.FREIGHT_YARD,
                    mascot = MascotCharacter.MECH_BOT,
                    mascotPosition = MascotPosition.LEFT,
                    outlineThickness = 18f,
                    shadowDepth = 22f,
                    highlights = true,
                    drips = false,
                    splats = true
                )
            }
            lower.contains("subway") || lower.contains("train") || lower.contains("nyc") || lower.contains("burner") -> {
                GeminiGraffitiDesign(
                    tagText = extractedTag.ifBlank { "BURNER" },
                    styleTitle = "NYC Subway Car Wholecar",
                    description = "Classic authentic 80s NYC subway car steel burner with thick white keyline and chrome reflections.",
                    artistAdvice = "Layer chrome fills with fast sweeping horizontal passes before cutting in the crisp black 3D block.",
                    fontStyle = GraffitiFontStyle.DON_GRAFFITI,
                    fillColorIndex = 1, // Electric Cyan
                    isGradient = true,
                    gradientColorIndex = 0, // Neon Pink
                    outlineColorIndex = 0, // White
                    wallType = WallType.SUBWAY_CAR,
                    mascot = MascotCharacter.SPRAY_CAN,
                    mascotPosition = MascotPosition.TOP,
                    outlineThickness = 18f,
                    shadowDepth = 24f,
                    highlights = true,
                    drips = true,
                    splats = true
                )
            }
            lower.contains("cosmic") || lower.contains("space") || lower.contains("star") || lower.contains("astro") -> {
                GeminiGraffitiDesign(
                    tagText = extractedTag.ifBlank { "COSMIC" },
                    styleTitle = "Galactic Zero-G Nebula",
                    description = "Deep violet and electric purple wildstyle letters with stardust mist and space cadet mascot.",
                    artistAdvice = "Flick your wrist at the letter edges to create zero-gravity stardust splatter flecks.",
                    fontStyle = GraffitiFontStyle.MOST_WASTED,
                    fillColorIndex = 13, // Cosmic Void
                    isGradient = true,
                    gradientColorIndex = 11, // Magenta
                    outlineColorIndex = 4, // Cyan
                    wallType = WallType.ROOFTOP_SKYLINE,
                    mascot = MascotCharacter.ASTRO_CADET,
                    mascotPosition = MascotPosition.RIGHT,
                    outlineThickness = 15f,
                    shadowDepth = 20f,
                    highlights = true,
                    drips = true,
                    splats = true
                )
            }
            else -> {
                GeminiGraffitiDesign(
                    tagText = extractedTag.ifBlank { "VIBE" },
                    styleTitle = "Street Wildstyle Burner",
                    description = "Explosive dynamic wildstyle lettering with interlocking arrows, sharp 3D blocks and spray mist.",
                    artistAdvice = "Cut your bottom block shadows first to anchor the piece firmly against the brick texture.",
                    fontStyle = GraffitiFontStyle.DON_GRAFFITI,
                    fillColorIndex = 0, // Pink
                    isGradient = true,
                    gradientColorIndex = 2, // Yellow
                    outlineColorIndex = 1, // Black
                    wallType = WallType.DARK_ALLEY,
                    mascot = MascotCharacter.SPRAY_CAN,
                    mascotPosition = MascotPosition.RIGHT,
                    outlineThickness = 16f,
                    shadowDepth = 20f,
                    highlights = true,
                    drips = true,
                    splats = true
                )
            }
        }
    }

    private fun generateFallbackTags(theme: String): List<String> {
        val lower = theme.lowercase()
        return when {
            lower.contains("cyber") || lower.contains("future") -> listOf("CYBER", "NEXUS", "GLITCH", "MECHA", "VOLT", "SYNTH")
            lower.contains("toxic") || lower.contains("acid") -> listOf("TOXIC", "ACID", "HAZARD", "VENOM", "MUTANT", "SLIME")
            lower.contains("subway") || lower.contains("nyc") -> listOf("BURNER", "SUBWAY", "METRO", "AEROSOL", "TRACKS", "KING")
            lower.contains("space") || lower.contains("cosmic") -> listOf("COSMIC", "ASTRO", "NEBULA", "ORBIT", "VOID", "LUNAR")
            else -> listOf("AERO", "KRYLON", "FLUX", "RUSH", "BLAST", "SHOCK")
        }
    }
}
