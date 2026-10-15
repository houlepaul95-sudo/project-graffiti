package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.model.GraffitiFontStyle
import com.example.model.MascotCharacter
import com.example.model.WallType

enum class AppNavigationPage(val title: String, val subtitle: String) {
    FREEHAND_CANVAS("Freehand Canvas", "Draw with Spray, Chisel & Drip brushes"),
    WILDSTYLE_STUDIO("Wildstyle & Crew", "Crazy wildstyle templates & characters (VIP)")
}

enum class BrushType(
    val id: String,
    val displayName: String,
    val iconDesc: String,
    val defaultSize: Float
) {
    SPRAY("spray", "Spray Can", "Aerosol mist with particle dispersion", 32f),
    CHISEL("chisel", "Chisel Marker", "45° angled calligraphy flat marker", 24f),
    DRIP("drip", "Dripping Paint", "Wet paint with dynamic gravity drips", 22f),
    FAT_CAP("fat_cap", "Fat Cap", "High output wide circular spray", 44f),
    FLARE_CAP("flare_cap", "Flare Cap", "Speed-sensitive aerosol flare spray", 36f),
    MOP("mop", "Drip Mop", "Juicy downtown squeezer mop marker", 38f),
    ERASER("eraser", "Eraser", "Clean up and erase strokes", 30f)
}

enum class StreetStencil(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val description: String
) {
    CROWN("crown", "King Crown", "👑", "Classic 3-point downtown street crown"),
    LIGHTNING("lightning", "Electric Bolt", "⚡", "High-voltage street shocker"),
    BOOMBOX("boombox", "Boombox", "📻", "1980s street cassette blaster"),
    STAR_FLARE("star_flare", "Star Glint", "⭐", "4-point street star flare"),
    SKULL("skull", "Skater Skull", "💀", "Downtown street art skull"),
    SLAP_STICKER("slap_sticker", "Slap Tag", "🏷️", "Street graffiti slap sticker")
}

data class DripDrop(
    val startX: Float,
    val startY: Float,
    val length: Float,
    val tipRadius: Float,
    val color: Int
)

data class DrawPoint(
    val x: Float,
    val y: Float
)

data class FreehandStroke(
    val brushType: BrushType,
    val color: Int, // ARGB
    val strokeWidth: Float,
    val points: List<DrawPoint>,
    val drips: List<DripDrop> = emptyList(),
    val stencil: StreetStencil? = null,
    val mascot: MascotCharacter? = null
)

data class WildstyleTemplate(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val defaultText: String,
    val fontStyle: GraffitiFontStyle,
    val fillColorIndex: Int,
    val isGradient: Boolean,
    val gradientColorIndex: Int,
    val outlineColorIndex: Int,
    val wallType: WallType,
    val defaultMascot: MascotCharacter,
    val outlineThickness: Float = 16f,
    val shadowDepth: Float = 18f,
    val accentColor: Color = Color(0xFFFF50B4),
    val isPro: Boolean = false
)

object WildstylePresets {
    val TEMPLATES = listOf(
        // ---- 5 New Downtown City Street Styles ----
        WildstyleTemplate(
            id = "times_square_neon",
            name = "Times Square Neon Blast",
            subtitle = "Midnight Broadway Burner",
            description = "Electric cyan & hot magenta neon letters, cyberpunk downtown night backdrop, vivid aerosol glow halo.",
            defaultText = "DOWNTOWN",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 1, // Electric Cyan
            isGradient = true,
            gradientColorIndex = 0, // Neon Pink
            outlineColorIndex = 3, // Neon Yellow
            wallType = WallType.CYBER_NIGHT,
            defaultMascot = MascotCharacter.SPRAY_CAN,
            outlineThickness = 16f,
            shadowDepth = 22f,
            accentColor = Color(0xFF00DCFF),
            isPro = false // Free for all street taggers
        ),
        WildstyleTemplate(
            id = "brooklyn_bubble",
            name = "Brooklyn King Throwie",
            subtitle = "Subway Crown Bubble",
            description = "Thick toxic lime & acid yellow bubble throw-up with dripping paint runs, heavy tar outlines, and gold crown.",
            defaultText = "KINGZ",
            fontStyle = GraffitiFontStyle.SISTER_SPRAY,
            fillColorIndex = 3, // Toxic Lime
            isGradient = true,
            gradientColorIndex = 2, // Acid Yellow
            outlineColorIndex = 1, // Black
            wallType = WallType.RED_BRICK,
            defaultMascot = MascotCharacter.ALLEY_RAT,
            outlineThickness = 18f,
            shadowDepth = 20f,
            accentColor = Color(0xFFFFDC00),
            isPro = false // Free for all street taggers
        ),
        WildstyleTemplate(
            id = "soho_stencil",
            name = "SoHo Stencil Revolution",
            subtitle = "Downtown Gallery Rebel",
            description = "High-contrast pure white & fire red sharp stencil cuts, dynamic spray splatter clouds on storefront shutter.",
            defaultText = "STREET",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 9, // Pure White
            isGradient = true,
            gradientColorIndex = 6, // Fire Red
            outlineColorIndex = 1, // Black
            wallType = WallType.ROLLUP_SHUTTER,
            defaultMascot = MascotCharacter.MECH_BOT,
            outlineThickness = 15f,
            shadowDepth = 16f,
            accentColor = Color(0xFFFF2D55),
            isPro = true // 🔒 SUBSCRIPTION BLOCKED (VIP PASS)
        ),
        WildstyleTemplate(
            id = "harlem_sunset",
            name = "Harlem Rooftop Sunset",
            subtitle = "Golden Hour 3D Wildstyle",
            description = "Blazing flame orange fading into royal grape violet, gleaming pure gold 3D extrusions with NYC rooftop skyline.",
            defaultText = "HARLEM",
            fontStyle = GraffitiFontStyle.URBAN_JUNGLE,
            fillColorIndex = 4, // Flame Orange
            isGradient = true,
            gradientColorIndex = 5, // Grape Violet
            outlineColorIndex = 8, // Pure Gold
            wallType = WallType.ROOFTOP_SKYLINE,
            defaultMascot = MascotCharacter.ASTRO_CADET,
            outlineThickness = 17f,
            shadowDepth = 22f,
            accentColor = Color(0xFFFF8C28),
            isPro = true // 🔒 SUBSCRIPTION BLOCKED (VIP PASS)
        ),
        WildstyleTemplate(
            id = "subway_chrome",
            name = "Subway Station Chrome Beast",
            subtitle = "Mirrored Steel Wholecar",
            description = "Reflective chrome silver with electric violet 3D bevels, gleaming star glints and authentic NYC subway train car.",
            defaultText = "NYC84",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 10, // Chrome Silver
            isGradient = true,
            gradientColorIndex = 5, // Grape Violet
            outlineColorIndex = 0, // White
            wallType = WallType.SUBWAY_CAR,
            defaultMascot = MascotCharacter.STREET_BULLDOG,
            outlineThickness = 17f,
            shadowDepth = 24f,
            accentColor = Color(0xFFCFD8DC),
            isPro = true // 🔒 SUBSCRIPTION BLOCKED (VIP PASS)
        ),
        // ---- Classic Wildstyles ----
        WildstyleTemplate(
            id = "electric_chrome",
            name = "Electric Chrome Burner",
            subtitle = "3D Chrome & Lime Splatter",
            description = "Metallic silver chrome fill, electric purple bevels, lime drop accents and razor sharp 3D arrows.",
            defaultText = "BURNER",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 10, // Chrome Silver
            isGradient = true,
            gradientColorIndex = 5, // Grape Violet
            outlineColorIndex = 6, // Toxic Lime
            wallType = WallType.RED_BRICK,
            defaultMascot = MascotCharacter.ALLEY_RAT,
            outlineThickness = 16f,
            shadowDepth = 20f,
            accentColor = Color(0xFF00DCFF)
        ),
        WildstyleTemplate(
            id = "acid_interlock",
            name = "Acid Toxic Interlock",
            subtitle = "Biohazard Neon Drips",
            description = "Vibrant toxic lime interlocking bubble-sharp letters, neon magenta outer halo and acid spray mist.",
            defaultText = "TOXIC",
            fontStyle = GraffitiFontStyle.SISTER_SPRAY,
            fillColorIndex = 3, // Toxic Lime
            isGradient = true,
            gradientColorIndex = 2, // Acid Yellow
            outlineColorIndex = 0, // Neon Pink
            wallType = WallType.DARK_ALLEY,
            defaultMascot = MascotCharacter.SPRAY_CAN,
            outlineThickness = 15f,
            shadowDepth = 16f,
            accentColor = Color(0xFF8CFF50)
        ),
        WildstyleTemplate(
            id = "subway_wholecar",
            name = "Subway Train Wholecar",
            subtitle = "NYC Train Burner",
            description = "Authentic NYC subway car end-to-end piece with heavy outlines, steel reflections and star sparkles.",
            defaultText = "SUBWAY",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 1, // Electric Cyan
            isGradient = true,
            gradientColorIndex = 0, // Neon Pink
            outlineColorIndex = 0, // White
            wallType = WallType.SUBWAY_CAR,
            defaultMascot = MascotCharacter.SPRAY_CAN,
            outlineThickness = 18f,
            shadowDepth = 22f,
            accentColor = Color(0xFFFFDC00)
        ),
        WildstyleTemplate(
            id = "cosmic_nebula",
            name = "Cosmic Zero-G Dimension",
            subtitle = "Stardust & Void Arrows",
            description = "Deep galactic nebula gradient letters, purple cosmic stardust, star flares and astronaut companion.",
            defaultText = "COSMIC",
            fontStyle = GraffitiFontStyle.MOST_WASTED,
            fillColorIndex = 13, // Cosmic Void
            isGradient = true,
            gradientColorIndex = 11, // Hot Magenta
            outlineColorIndex = 4, // Electric Cyan
            wallType = WallType.ROOFTOP_SKYLINE,
            defaultMascot = MascotCharacter.ASTRO_CADET,
            outlineThickness = 14f,
            shadowDepth = 18f,
            accentColor = Color(0xFFB464FF)
        ),
        WildstyleTemplate(
            id = "iron_freight",
            name = "Iron Freight Rust Burner",
            subtitle = "Scorched Steel & Mech Bot",
            description = "Industrial steel plates, scorched rust texture, heavy block wildstyle and cyber steampunk mech tagger.",
            defaultText = "FREIGHT",
            fontStyle = GraffitiFontStyle.URBAN_JUNGLE,
            fillColorIndex = 12, // Burnt Rust
            isGradient = true,
            gradientColorIndex = 8, // Pure Gold
            outlineColorIndex = 1, // Black
            wallType = WallType.FREIGHT_YARD,
            defaultMascot = MascotCharacter.MECH_BOT,
            outlineThickness = 16f,
            shadowDepth = 18f,
            accentColor = Color(0xFFFF8C28)
        ),
        WildstyleTemplate(
            id = "highway_overpass",
            name = "Overpass Emerald Beast",
            subtitle = "Gold Rim & Guard Bulldog",
            description = "Deep emerald green wildstyle letters with gleaming gold rims and street bulldog mascot guarding.",
            defaultText = "GUARD",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 7, // Emerald Green
            isGradient = true,
            gradientColorIndex = 8, // Pure Gold
            outlineColorIndex = 0, // White
            wallType = WallType.HIGHWAY_OVERPASS,
            defaultMascot = MascotCharacter.STREET_BULLDOG,
            outlineThickness = 17f,
            shadowDepth = 20f,
            accentColor = Color(0xFFFFD166)
        )
    )
}
