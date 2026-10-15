package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush as ComposeBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiGraffitiDesign
import com.example.ai.GeminiGraffitiService
import com.example.model.GraffitiConfig
import kotlinx.coroutines.launch

@Composable
fun GeminiGraffitiAiCard(
    currentConfig: GraffitiConfig,
    onApplyDesign: (GraffitiConfig) -> Unit,
    onSwitchToDrawingCanvas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var userPrompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var lastGeneratedDesign by remember { mutableStateOf<GeminiGraffitiDesign?>(null) }
    var suggestedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var isGeneratingTags by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val quickConcepts = listOf(
        "☣️ Toxic Acid Drip" to "Toxic lime green acid drip burner with neon magenta halo and radioactive mist",
        "🚇 NYC Subway Steel" to "NYC subway train wholecar burner with chrome steel reflections and white outline",
        "🤖 Cyber Steampunk Mech" to "Cyberpunk robot mech tag with electric cyan wires and burnt rust 3D block",
        "🌌 Cosmic Zero-G" to "Galactic nebula stardust letters with purple cosmic void and astronaut mascot",
        "👑 Street King Crown" to "Old school 90s hip hop gold crown lettering with heavy 3D drop shadow",
        "🧴 Aerosol Can Crew" to "Dynamic street wildstyle with animated spray can character companion"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141724)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = ComposeBrush.horizontalGradient(
                    colors = listOf(Color(0xFF00DCFF), Color(0xFFFF50B4), Color(0xFFFFDC00))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("gemini_graffiti_ai_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                ComposeBrush.linearGradient(
                                    listOf(Color(0xFF00DCFF), Color(0xFFFF50B4))
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "GEMINI AI GRAFFITI DESIGNER",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Smart Custom Graffiti Graphic & Character Generator",
                            fontSize = 9.sp,
                            color = Color(0xFF00DCFF)
                        )
                    }
                }

                Text(
                    text = if (isExpanded) "HIDE" else "SHOW",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E95AA),
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(4.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    // Prompt input
                    OutlinedTextField(
                        value = userPrompt,
                        onValueChange = { userPrompt = it },
                        placeholder = {
                            Text(
                                text = "Describe your graffiti style (e.g. 'Toxic dragon on subway' or '90s NYC block')...",
                                color = Color(0xFF72798F),
                                fontSize = 11.sp
                            )
                        },
                        trailingIcon = {
                            if (userPrompt.isNotBlank()) {
                                IconButton(onClick = { userPrompt = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF72798F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = false,
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00DCFF),
                            unfocusedBorderColor = Color(0xFF282D42),
                            cursorColor = Color(0xFF00DCFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Concept Chips
                    Text(
                        text = "QUICK AI CONCEPTS:",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E95AA),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickConcepts) { (label, promptText) ->
                            FilterChip(
                                selected = userPrompt == promptText,
                                onClick = {
                                    userPrompt = promptText
                                    // Auto generate when tapped
                                    scope.launch {
                                        isGenerating = true
                                        val result = GeminiGraffitiService.generateCustomGraffiti(promptText, currentConfig)
                                        isGenerating = false
                                        result.onSuccess { design ->
                                            lastGeneratedDesign = design
                                            onApplyDesign(design.toGraffitiConfig(currentConfig))
                                        }
                                    }
                                },
                                label = { Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF1B1E2E),
                                    labelColor = Color(0xFFCCD2E3),
                                    selectedContainerColor = Color(0xFF00DCFF),
                                    selectedLabelColor = Color.Black
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = userPrompt == promptText,
                                    borderColor = Color(0xFF2A2E44),
                                    selectedBorderColor = Color(0xFF00DCFF)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons: Generate & Suggest Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                scope.launch {
                                    isGenerating = true
                                    val prompt = userPrompt.ifBlank { "Dynamic colorful street wildstyle with aerosol spray mist and character" }
                                    val result = GeminiGraffitiService.generateCustomGraffiti(prompt, currentConfig)
                                    isGenerating = false
                                    result.onSuccess { design ->
                                        lastGeneratedDesign = design
                                        onApplyDesign(design.toGraffitiConfig(currentConfig))
                                    }
                                }
                            },
                            enabled = !isGenerating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00DCFF),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(40.dp)
                                .testTag("btn_gemini_generate")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DESIGNING...", fontSize = 10.sp, fontWeight = FontWeight.Black)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI GENERATE", fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isGeneratingTags = true
                                    val theme = userPrompt.ifBlank { "legendary street aerosol wildstyle" }
                                    val result = GeminiGraffitiService.suggestGraffitiTags(theme)
                                    isGeneratingTags = false
                                    result.onSuccess { tags ->
                                        suggestedTags = tags
                                    }
                                }
                            },
                            enabled = !isGeneratingTags,
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = ComposeBrush.horizontalGradient(
                                    listOf(Color(0xFFFF50B4), Color(0xFFFFDC00))
                                )
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("btn_gemini_suggest_tags")
                        ) {
                            if (isGeneratingTags) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFF50B4),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFFFDC00),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI TAGS",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Suggested Tags Row
                    if (suggestedTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AI SUGGESTED TAG ALIASES (TAP TO APPLY):",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFDC00)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(suggestedTags) { tag ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222638)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .clickable {
                                            onApplyDesign(currentConfig.copy(text = tag))
                                        }
                                        .border(1.dp, Color(0xFFFF50B4), RoundedCornerShape(6.dp))
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Generated Concept Feedback & Canvas Bridge
                    lastGeneratedDesign?.let { design ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2235)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF333A52), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🎨 ${design.styleTitle}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF00DCFF)
                                    )
                                    Text(
                                        text = "Tag: ${design.tagText}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF50B4)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = design.description,
                                    fontSize = 9.5.sp,
                                    color = Color(0xFFD6DBEC)
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.TipsAndUpdates,
                                        contentDescription = null,
                                        tint = Color(0xFFFFDC00),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Pro Tip: ${design.artistAdvice}",
                                        fontSize = 9.sp,
                                        color = Color(0xFFFFDC00)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Direct Bridge to Freehand Canvas with Spray/Chisel/Drip
                                Button(
                                    onClick = onSwitchToDrawingCanvas,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF50B4),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .testTag("btn_bridge_to_freehand_canvas")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Brush,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PAINT OVER IN FREEHAND CANVAS (SPRAY / CHISEL / DRIP)",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
