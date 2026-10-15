package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.R

enum class GraffitiFontStyle(
    val displayName: String,
    val fontResId: Int,
    val styleCategory: String,
    val description: String,
    val defaultFillIndex: Int,
    val defaultGradientIndex: Int,
    val defaultOutlineIndex: Int,
    val defaultLean: Boolean,
    val defaultDrips: Boolean,
    val fontSkew: Float = -0.22f
) {
    SISTER_SPRAY(
        displayName = "Sister Spray",
        fontResId = R.font.sister_spray,
        styleCategory = "Bubble Throwie",
        description = "Puffy, rounded throwie alphabet",
        defaultFillIndex = 1,       // Electric Cyan
        defaultGradientIndex = 0,   // Neon Pink
        defaultOutlineIndex = 0,    // White
        defaultLean = true,
        defaultDrips = false,
        fontSkew = -0.18f
    ),
    MOST_WASTED(
        displayName = "Most Wasted",
        fontResId = R.font.most_wasted,
        styleCategory = "Throw-up",
        description = "Classic thick bubble throwie with drips",
        defaultFillIndex = 5,       // Grape Violet
        defaultGradientIndex = 2,   // Acid Yellow
        defaultOutlineIndex = 1,    // Black
        defaultLean = true,
        defaultDrips = true,
        fontSkew = -0.22f
    ),
    DON_GRAFFITI(
        displayName = "Don Graffiti",
        fontResId = R.font.don_graffiti,
        styleCategory = "Wildstyle",
        description = "Sharp, aggressive, complex letterforms",
        defaultFillIndex = 4,       // Flame Orange
        defaultGradientIndex = 10,  // Chrome Silver
        defaultOutlineIndex = 0,    // White
        defaultLean = false,
        defaultDrips = true,
        fontSkew = 0f
    ),
    URBAN_JUNGLE(
        displayName = "Urban Jungle",
        fontResId = R.font.urban_jungle,
        styleCategory = "Tag Script",
        description = "Marker/handstyle script lean",
        defaultFillIndex = 7,       // Ice Blue
        defaultGradientIndex = 9,   // Pure White
        defaultOutlineIndex = 1,    // Black
        defaultLean = true,
        defaultDrips = false,
        fontSkew = -0.25f
    );

    companion object {
        fun fromNameOrDefault(name: String): GraffitiFontStyle {
            return when (name.uppercase()) {
                "SISTER_SPRAY", "BUBBLE" -> SISTER_SPRAY
                "MOST_WASTED", "BLOCKY" -> MOST_WASTED
                "DON_GRAFFITI", "WILDSTYLE" -> DON_GRAFFITI
                "URBAN_JUNGLE", "TAG", "CHUNKY" -> URBAN_JUNGLE
                else -> SISTER_SPRAY
            }
        }
    }
}

enum class WallType(val displayName: String) {
    DARK_SLATE("Night Slate"),
    SUBWAY_PURPLE("Subway Purple"),
    RED_BRICK("Red Brick"),
    CLEAN_WHITE("White Subway"),
    DARK_ALLEY("Dark Concrete"),
    CYBER_NIGHT("Cyberpunk"),
    PHOTOREAL_ALLEY("Street Wall Photo"),
    SUBWAY_CAR("NYC Subway Train"),
    ROLLUP_SHUTTER("Storefront Shutter"),
    FREIGHT_TUNNEL("Underground Tunnel"),
    AGED_BRICK("Aged Warehouse Brick"),
    TRANSPARENT("Transparent Sticker"),
    ROOFTOP_SKYLINE("NYC Rooftop Skyline"),
    HIGHWAY_OVERPASS("Highway Overpass Concrete"),
    FREIGHT_YARD("Train Yard Tracks")
}

enum class MascotCharacter(
    val id: String,
    val displayName: String,
    val tagTitle: String,
    val description: String,
    val isPro: Boolean,
    val badge: String
) {
    NONE(
        id = "none",
        displayName = "No Mascot",
        tagTitle = "RAW LETTERS",
        description = "Clean graffiti lettering with no character",
        isPro = false,
        badge = "OFF"
    ),
    ALLEY_RAT(
        id = "alley_rat",
        displayName = "Alley Rat",
        tagTitle = "SEWER CREW",
        description = "Street-smart rat with red cap & aerosol can",
        isPro = false, // 🔓 FREE FOR ALL USERS (as requested: "except 1")
        badge = "FREE"
    ),
    SPRAY_CAN(
        id = "spray_can",
        displayName = "Spray Can Boy",
        tagTitle = "AEROSOL HOMIE",
        description = "Cheeky spray can bursting neon cyan aerosol mist",
        isPro = true, // 👑 LOCKED TO VIP SUBSCRIPTION
        badge = "VIP"
    ),
    MECH_BOT(
        id = "mech_bot",
        displayName = "Cyber Mech Bot",
        tagTitle = "STEAMPUNK CREW",
        description = "Industrial cyborg bot with spray goggles & tank",
        isPro = true, // 👑 LOCKED TO VIP SUBSCRIPTION
        badge = "VIP"
    ),
    ASTRO_CADET(
        id = "astro_cadet",
        displayName = "Cosmic Astronaut",
        tagTitle = "ZERO-G TAGGER",
        description = "Deep space cadet floating with spray canister",
        isPro = true, // 👑 LOCKED TO VIP SUBSCRIPTION
        badge = "VIP"
    ),
    STREET_BULLDOG(
        id = "street_bulldog",
        displayName = "Street Bulldog",
        tagTitle = "OVERPASS GUARD",
        description = "Tough bulldog with spiked collar guarding letters",
        isPro = true, // 👑 LOCKED TO VIP SUBSCRIPTION
        badge = "VIP"
    );

    companion object {
        fun fromIdOrDefault(id: String): MascotCharacter {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ALLEY_RAT
        }
    }
}

enum class MascotPosition(val displayName: String) {
    RIGHT("Right Side"),
    LEFT("Left Side"),
    TOP("Peeking Top")
}

data class SignatureMasterStyle(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val fontStyle: GraffitiFontStyle,
    val fillColorIndex: Int,
    val isGradient: Boolean,
    val gradientColorIndex: Int,
    val outlineColorIndex: Int,
    val wallType: WallType,
    val mascot: MascotCharacter,
    val isPro: Boolean,
    val accentColor: Color,
    val specialEffect: String
)

data class ColorOption(
    val name: String,
    val color: Color
) {
    val argb: Int get() = color.toArgb()
}

data class GraffitiConfig(
    val text: String = "GRAFFITI",
    val fillColorIndex: Int = 1,          // Electric Cyan default
    val isGradient: Boolean = true,
    val gradientColorIndex: Int = 0,      // Hot Pink default
    val outlineColorIndex: Int = 0,       // White default
    val wallIndex: Int = 0,               // Dark Slate default
    val lean: Boolean = true,
    val splats: Boolean = true,
    val drips: Boolean = true,
    val highlights: Boolean = true,
    val fontStyle: GraffitiFontStyle = GraffitiFontStyle.SISTER_SPRAY,
    val outlineThickness: Float = 14f,
    val shadowDepth: Float = 16f,
    val splatSeed: Int = 42,
    val splatDensity: Int = 18,
    val mascot: MascotCharacter = MascotCharacter.ALLEY_RAT,
    val mascotPosition: MascotPosition = MascotPosition.RIGHT,
    val masterStyleId: String = "purple_chrome",
    val isVerticalLayout: Boolean = false
)

object GraffitiPalettes {
    // Vibrant street-art fills (including the original 6 from user prompt + extra neons)
    val FILLS = listOf(
        ColorOption("Neon Pink", Color(0xFFFF50B4)),      // User's RGB(255, 80, 180)
        ColorOption("Electric Cyan", Color(0xFF00DCFF)),  // User's RGB(0, 220, 255)
        ColorOption("Acid Yellow", Color(0xFFFFDC00)),   // User's RGB(255, 220, 0)
        ColorOption("Toxic Lime", Color(0xFF8CFF50)),     // User's RGB(140, 255, 80)
        ColorOption("Flame Orange", Color(0xFFFF8C28)),   // User's RGB(255, 140, 40)
        ColorOption("Grape Violet", Color(0xFFB464FF)),   // User's RGB(180, 100, 255)
        ColorOption("Fire Red", Color(0xFFFF2D55)),
        ColorOption("Emerald Green", Color(0xFF0E7A3D)),
        ColorOption("Pure Gold", Color(0xFFFFD166)),
        ColorOption("Pure White", Color(0xFFFFFFFF)),
        ColorOption("Chrome Silver", Color(0xFFCFD8DC)),
        ColorOption("Hot Magenta", Color(0xFFFF007F)),
        ColorOption("Burnt Rust", Color(0xFFC85A17)),
        ColorOption("Cosmic Void", Color(0xFF4A148C))
    )

    // Outline colors
    val OUTLINES = listOf(
        ColorOption("White", Color.White),
        ColorOption("Black", Color.Black),
        ColorOption("Deep Purple", Color(0xFF3C1E5A)),     // User's RGB(60, 30, 90)
        ColorOption("Neon Yellow", Color(0xFFFFDC00)),    // User's RGB(255, 220, 0)
        ColorOption("Electric Cyan", Color(0xFF00DCFF)),
        ColorOption("Hot Pink", Color(0xFFFF50B4)),
        ColorOption("Toxic Lime", Color(0xFF8CFF50)),
        ColorOption("Steel Slate", Color(0xFF455A64)),
        ColorOption("Pure Gold", Color(0xFFFFD166))
    )

    // Wall / Background options (Pro VIP walls are marked isPro = true)
    val WALLS = listOf(
        WallOption("Night Slate", Color(0xFF1E2030), WallType.DARK_SLATE, isPro = false),       // User's RGB(30, 32, 48)
        WallOption("Subway Purple", Color(0xFF5A3778), WallType.SUBWAY_PURPLE, isPro = false),  // User's RGB(90, 55, 120)
        WallOption("Red Brick Wall", Color(0xFFA0553C), WallType.RED_BRICK, isPro = false),     // User's RGB(160, 85, 60)
        WallOption("White Subway Wall", Color(0xFFF0F0F0), WallType.CLEAN_WHITE, isPro = false),// User's RGB(240, 240, 240)
        WallOption("Dark Alley Concrete", Color(0xFF141418), WallType.DARK_ALLEY, isPro = false),
        WallOption("Transparent Sticker", Color.Transparent, WallType.TRANSPARENT, isPro = false),
        // VIP Street Backdrops (Subscription / Lifetime Pass Locked)
        WallOption("NYC Subway Train", Color(0xFF8E9BAE), WallType.SUBWAY_CAR, isPro = true),
        WallOption("Storefront Shutter", Color(0xFF2E3547), WallType.ROLLUP_SHUTTER, isPro = true),
        WallOption("Underground Tunnel", Color(0xFF1E212A), WallType.FREIGHT_TUNNEL, isPro = true),
        WallOption("Aged Warehouse Brick", Color(0xFF824734), WallType.AGED_BRICK, isPro = true),
        WallOption("Street Wall Photo", Color(0xFF1B192A), WallType.PHOTOREAL_ALLEY, isPro = true),
        WallOption("Cyberpunk Indigo", Color(0xFF100C2A), WallType.CYBER_NIGHT, isPro = true),
        WallOption("NYC Rooftop Skyline", Color(0xFF281E3C), WallType.ROOFTOP_SKYLINE, isPro = true),
        WallOption("Highway Overpass Concrete", Color(0xFF2C322E), WallType.HIGHWAY_OVERPASS, isPro = true),
        WallOption("Train Yard Tracks", Color(0xFF2E2620), WallType.FREIGHT_YARD, isPro = true)
    )

    // Signature Master Styles (Classic + 5 New Downtown City Street Styles)
    val SIGNATURE_MASTER_STYLES = listOf(
        // ---- 5 New Downtown City Street Styles ----
        SignatureMasterStyle(
            id = "times_square_neon",
            name = "Times Square Neon Blast",
            subtitle = "Midnight Broadway Burner",
            description = "Electric cyan & hot magenta neon letters, cyberpunk downtown night backdrop, vivid aerosol glow halo",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 1, // Electric Cyan
            isGradient = true,
            gradientColorIndex = 0, // Neon Pink
            outlineColorIndex = 3, // Neon Yellow
            wallType = WallType.CYBER_NIGHT,
            mascot = MascotCharacter.SPRAY_CAN,
            isPro = false, // 🔓 FREE FOR DOWNTOWN EXPLORATION
            accentColor = Color(0xFF00DCFF),
            specialEffect = "TIMES_SQUARE_NEON"
        ),
        SignatureMasterStyle(
            id = "brooklyn_bubble",
            name = "Brooklyn King Throwie",
            subtitle = "Subway Crown Bubble",
            description = "Thick toxic lime & acid yellow bubble throw-up with dripping paint runs, heavy tar outlines, and gold crown",
            fontStyle = GraffitiFontStyle.SISTER_SPRAY,
            fillColorIndex = 3, // Toxic Lime
            isGradient = true,
            gradientColorIndex = 2, // Acid Yellow
            outlineColorIndex = 1, // Black
            wallType = WallType.RED_BRICK,
            mascot = MascotCharacter.ALLEY_RAT,
            isPro = false,
            accentColor = Color(0xFFFFDC00),
            specialEffect = "BROOKLYN_BUBBLE"
        ),
        SignatureMasterStyle(
            id = "soho_stencil",
            name = "SoHo Stencil Revolution",
            subtitle = "Downtown Gallery Rebel",
            description = "High-contrast pure white & fire red sharp stencil cuts, dynamic spray splatter clouds on storefront shutter",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 9, // Pure White
            isGradient = true,
            gradientColorIndex = 6, // Fire Red
            outlineColorIndex = 1, // Black
            wallType = WallType.ROLLUP_SHUTTER,
            mascot = MascotCharacter.MECH_BOT,
            isPro = true, // 👑 VIP
            accentColor = Color(0xFFFF2D55),
            specialEffect = "SOHO_STENCIL"
        ),
        SignatureMasterStyle(
            id = "harlem_sunset",
            name = "Harlem Rooftop Sunset",
            subtitle = "Golden Hour 3D Wildstyle",
            description = "Blazing flame orange fading into royal grape violet, gleaming pure gold 3D extrusions with NYC rooftop skyline",
            fontStyle = GraffitiFontStyle.URBAN_JUNGLE,
            fillColorIndex = 4, // Flame Orange
            isGradient = true,
            gradientColorIndex = 5, // Grape Violet
            outlineColorIndex = 8, // Pure Gold
            wallType = WallType.ROOFTOP_SKYLINE,
            mascot = MascotCharacter.ASTRO_CADET,
            isPro = true, // 👑 VIP
            accentColor = Color(0xFFFF8C28),
            specialEffect = "HARLEM_SUNSET"
        ),
        SignatureMasterStyle(
            id = "subway_chrome",
            name = "Subway Station Chrome Beast",
            subtitle = "Mirrored Steel Wholecar",
            description = "Reflective chrome silver with electric violet 3D bevels, gleaming star glints and authentic NYC subway train car",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 10, // Chrome Silver
            isGradient = true,
            gradientColorIndex = 5, // Grape Violet
            outlineColorIndex = 0, // White
            wallType = WallType.SUBWAY_CAR,
            mascot = MascotCharacter.STREET_BULLDOG,
            isPro = true, // 👑 VIP
            accentColor = Color(0xFFCFD8DC),
            specialEffect = "SUBWAY_CHROME"
        ),
        // ---- Original Master Styles ----
        SignatureMasterStyle(
            id = "purple_chrome",
            name = "Purple Chrome",
            subtitle = "Alley Rat Wildstyle",
            description = "Metallic silver chrome fill, electric violet 3D bevels, lime drop accents & alley rat companion",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 10, // Chrome Silver
            isGradient = true,
            gradientColorIndex = 5, // Grape Violet
            outlineColorIndex = 6, // Toxic Lime
            wallType = WallType.RED_BRICK,
            mascot = MascotCharacter.ALLEY_RAT,
            isPro = false,
            accentColor = Color(0xFFB464FF),
            specialEffect = "PURPLE_CHROME"
        ),
        SignatureMasterStyle(
            id = "bubble_pop",
            name = "Bubble Pop",
            subtitle = "Aerosol Spray Can",
            description = "Hot pink to yellow bubble fade, splatter dots, electric cyan mist aura & mischievous spray can",
            fontStyle = GraffitiFontStyle.SISTER_SPRAY,
            fillColorIndex = 0, // Neon Pink
            isGradient = true,
            gradientColorIndex = 2, // Acid Yellow
            outlineColorIndex = 4, // Electric Cyan
            wallType = WallType.CLEAN_WHITE,
            mascot = MascotCharacter.SPRAY_CAN,
            isPro = true, // 👑 VIP LOCKED
            accentColor = Color(0xFFFF50B4),
            specialEffect = "BUBBLE_POP"
        ),
        SignatureMasterStyle(
            id = "rust_freight",
            name = "Rust Freight",
            subtitle = "Mech Bot Burner",
            description = "Weathered industrial burnt orange, oxidized slate blue bevels & steampunk cyborg mech bot",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 4, // Flame Orange / Rust
            isGradient = true,
            gradientColorIndex = 8, // Pure Gold / Rust
            outlineColorIndex = 7, // Steel Slate
            wallType = WallType.FREIGHT_YARD,
            mascot = MascotCharacter.MECH_BOT,
            isPro = true, // 👑 VIP LOCKED
            accentColor = Color(0xFFFF8C28),
            specialEffect = "RUST_FREIGHT"
        ),
        SignatureMasterStyle(
            id = "cosmic_galaxy",
            name = "Cosmic Galaxy",
            subtitle = "Space Cadet Wildstyle",
            description = "Deep space violet stardust nebula, shining star cluster fill, neon pink edge & floating astronaut",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 5, // Grape Violet
            isGradient = true,
            gradientColorIndex = 11, // Hot Magenta
            outlineColorIndex = 0, // White neon
            wallType = WallType.ROOFTOP_SKYLINE,
            mascot = MascotCharacter.ASTRO_CADET,
            isPro = true, // 👑 VIP LOCKED
            accentColor = Color(0xFFE040FB),
            specialEffect = "COSMIC_GALAXY"
        ),
        SignatureMasterStyle(
            id = "emerald_gold",
            name = "Emerald Gold",
            subtitle = "Overpass Bulldog",
            description = "Rich emerald green, polished 3D gold bevels with lens flares & tough street bulldog mascot",
            fontStyle = GraffitiFontStyle.DON_GRAFFITI,
            fillColorIndex = 7, // Emerald Green
            isGradient = true,
            gradientColorIndex = 3, // Toxic Lime
            outlineColorIndex = 8, // Pure Gold
            wallType = WallType.HIGHWAY_OVERPASS,
            mascot = MascotCharacter.STREET_BULLDOG,
            isPro = true, // 👑 VIP LOCKED
            accentColor = Color(0xFFFFD166),
            specialEffect = "EMERALD_GOLD"
        )
    )

    val PRESET_TAGS = listOf(
        "TESTER", "GRAFFITI", "FRESH", "WILD", "KING", "BOOM", "STREET", "PROSPER",
        "VIBE", "LEGEND", "DOPE", "NEW YORK", "URBAN", "CREW", "EDGE"
    )
}

data class WallOption(
    val name: String,
    val color: Color,
    val type: WallType,
    val isPro: Boolean = false
) {
    val argb: Int get() = color.toArgb()
}
