package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.GraffitiConfig
import com.example.model.GraffitiFontStyle

@Entity(tableName = "saved_graffiti")
data class SavedGraffiti(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val text: String,
    val fillColorIndex: Int,
    val isGradient: Boolean,
    val gradientColorIndex: Int,
    val outlineColorIndex: Int,
    val wallIndex: Int,
    val lean: Boolean,
    val splats: Boolean,
    val drips: Boolean,
    val highlights: Boolean,
    val fontStyleName: String,
    val outlineThickness: Float,
    val shadowDepth: Float,
    val splatSeed: Int,
    val splatDensity: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toConfig(): GraffitiConfig {
        val style = GraffitiFontStyle.fromNameOrDefault(fontStyleName)
        return GraffitiConfig(
            text = text,
            fillColorIndex = fillColorIndex,
            isGradient = isGradient,
            gradientColorIndex = gradientColorIndex,
            outlineColorIndex = outlineColorIndex,
            wallIndex = wallIndex,
            lean = lean,
            splats = splats,
            drips = drips,
            highlights = highlights,
            fontStyle = style,
            outlineThickness = outlineThickness,
            shadowDepth = shadowDepth,
            splatSeed = splatSeed,
            splatDensity = splatDensity
        )
    }

    companion object {
        fun fromConfig(title: String, config: GraffitiConfig): SavedGraffiti {
            return SavedGraffiti(
                title = title,
                text = config.text,
                fillColorIndex = config.fillColorIndex,
                isGradient = config.isGradient,
                gradientColorIndex = config.gradientColorIndex,
                outlineColorIndex = config.outlineColorIndex,
                wallIndex = config.wallIndex,
                lean = config.lean,
                splats = config.splats,
                drips = config.drips,
                highlights = config.highlights,
                fontStyleName = config.fontStyle.name,
                outlineThickness = config.outlineThickness,
                shadowDepth = config.shadowDepth,
                splatSeed = config.splatSeed,
                splatDensity = config.splatDensity
            )
        }
    }
}
