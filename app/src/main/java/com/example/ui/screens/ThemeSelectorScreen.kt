package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences
import com.example.data.KeyboardThemeType
import com.example.ui.components.FrontImageBanner
import com.example.ui.components.ModifyFrontImageDialog

@Composable
fun ThemeSelectorScreen(
    preferences: KeyboardPreferences
) {
    val settings by preferences.settings.collectAsState()
    val scrollState = rememberScrollState()
    var showImageDialog by remember { mutableStateOf(false) }

    if (showImageDialog) {
        ModifyFrontImageDialog(
            settings = settings,
            preferences = preferences,
            onDismiss = { showImageDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // FRONT IMAGE BANNER WITH MODIFY ACTION
        FrontImageBanner(
            settings = settings,
            onModifyClick = { showImageDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Thèmes & Personnalisation 🎨",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Choisissez un style prédéfini inspiré de la nature et du féminisme, ou composez vos propres couleurs.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // PRESET THEMES LIST
        ThemePresetCard(
            title = "Féministe Solidarité ✊🏿 (Par Défaut)",
            description = "Couleurs vives violette et touches blanches ultra lisibles avec icônes féministes affirmées.",
            gradientColors = listOf(Color(0xFF311B92), Color(0xFF512DA8)),
            keyColor = Color(0xFFFFFFFF),
            textColor = Color(0xFF311B92),
            accentColor = Color(0xFFFF4081),
            sampleKeys = listOf("A", "Z", "E", "✊🏿", "♀️"),
            isSelected = settings.themeType == KeyboardThemeType.SOLIDARITE,
            onClick = { preferences.setTheme(KeyboardThemeType.SOLIDARITE) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemePresetCard(
            title = "Petites Fleurs 🌸",
            description = "Transforme les touches du clavier en de délicates petites fleurs aux pétales doux.",
            gradientColors = listOf(Color(0xFF2B192E), Color(0xFF421D3B)),
            keyColor = Color(0xFFFFF0F5),
            textColor = Color(0xFF880E4F),
            accentColor = Color(0xFFFF69B4),
            sampleKeys = listOf("A", "Z", "E", "🌸", "♀️"),
            themeType = KeyboardThemeType.PETITES_FLEURS,
            isSelected = settings.themeType == KeyboardThemeType.PETITES_FLEURS,
            onClick = { preferences.setTheme(KeyboardThemeType.PETITES_FLEURS) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemePresetCard(
            title = "Morceaux de Bois 🪵",
            description = "Transforme les touches du clavier en véritables pièces et blocs de bois sculptés.",
            gradientColors = listOf(Color(0xFF1A100C), Color(0xFF2A1B14)),
            keyColor = Color(0xFF8B5A2B),
            textColor = Color(0xFFFFF8E7),
            accentColor = Color(0xFFD7CCC8),
            sampleKeys = listOf("A", "Z", "E", "🪵", "✊🏿"),
            themeType = KeyboardThemeType.MORCEAUX_DE_BOIS,
            isSelected = settings.themeType == KeyboardThemeType.MORCEAUX_DE_BOIS,
            onClick = { preferences.setTheme(KeyboardThemeType.MORCEAUX_DE_BOIS) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemePresetCard(
            title = "Gouttes d'Eau 💧",
            description = "Transforme les touches du clavier en gouttes d'eau lisses, brillantes et translucides.",
            gradientColors = listOf(Color(0xFF03045E), Color(0xFF0077B6)),
            keyColor = Color(0xFF00B4D8),
            textColor = Color(0xFFFFFFFF),
            accentColor = Color(0xFF90E0EF),
            sampleKeys = listOf("A", "Z", "E", "💧", "🌊"),
            themeType = KeyboardThemeType.GOUTTES_DEAU,
            isSelected = settings.themeType == KeyboardThemeType.GOUTTES_DEAU,
            onClick = { preferences.setTheme(KeyboardThemeType.GOUTTES_DEAU) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemePresetCard(
            title = "Fleurs & Féministe 🌸",
            description = "Palette violette, lavande et rose gold féministe aux nuances douces.",
            gradientColors = listOf(Color(0xFF2A1B30), Color(0xFF3B2544)),
            keyColor = Color(0xFF4A3258),
            textColor = Color(0xFFF7EBFD),
            accentColor = Color(0xFFE082B4),
            isSelected = settings.themeType == KeyboardThemeType.FLEURS,
            onClick = { preferences.setTheme(KeyboardThemeType.FLEURS) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemePresetCard(
            title = "Eau & Océan 💧",
            description = "Style minimaliste aqua et bleu cyane apaisant.",
            gradientColors = listOf(Color(0xFF0D2533), Color(0xFF16384C)),
            keyColor = Color(0xFF1D4A63),
            textColor = Color(0xFFE0F7FA),
            accentColor = Color(0xFF4DD0E1),
            isSelected = settings.themeType == KeyboardThemeType.EAU,
            onClick = { preferences.setTheme(KeyboardThemeType.EAU) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemePresetCard(
            title = "Forêt & Nature 🌿",
            description = "Nuances vert émeraude, sauge et végétales rafraîchissantes.",
            gradientColors = listOf(Color(0xFF132A1A), Color(0xFF1C3D27)),
            keyColor = Color(0xFF275235),
            textColor = Color(0xFFE8F5E9),
            accentColor = Color(0xFF81C784),
            isSelected = settings.themeType == KeyboardThemeType.FORET,
            onClick = { preferences.setTheme(KeyboardThemeType.FORET) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemePresetCard(
            title = "Thème Personnalisé 🎨",
            description = "Ajustez vous-même la couleur des touches, le fond et la lueur.",
            gradientColors = listOf(Color(settings.customBgStart), Color(settings.customBgEnd)),
            keyColor = Color(settings.customKeyBg),
            textColor = Color(settings.customKeyText),
            accentColor = Color(settings.customAccent),
            isSelected = settings.themeType == KeyboardThemeType.CUSTOM,
            onClick = { preferences.setTheme(KeyboardThemeType.CUSTOM) }
        )

        // CUSTOM COLOR EDITOR SECTION
        AnimatedVisibility(visible = settings.themeType == KeyboardThemeType.CUSTOM) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Éditeur de Couleurs Personnalisées",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ColorPickerRow(
                        label = "Couleur des Touches",
                        selectedColor = Color(settings.customKeyBg),
                        onColorSelected = { colorHex ->
                            preferences.updateSettings(settings.copy(customKeyBg = colorHex))
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ColorPickerRow(
                        label = "Couleur du Texte",
                        selectedColor = Color(settings.customKeyText),
                        onColorSelected = { colorHex ->
                            preferences.updateSettings(settings.copy(customKeyText = colorHex))
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ColorPickerRow(
                        label = "Lueur d'Accentuation",
                        selectedColor = Color(settings.customAccent),
                        onColorSelected = { colorHex ->
                            preferences.updateSettings(settings.copy(customAccent = colorHex))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ThemePresetCard(
    title: String,
    description: String,
    gradientColors: List<Color>,
    keyColor: Color,
    textColor: Color,
    accentColor: Color,
    sampleKeys: List<String> = listOf("Q", "W", "E", "R", "🌸"),
    themeType: KeyboardThemeType? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val sampleShape = when (themeType) {
        KeyboardThemeType.PETITES_FLEURS -> RoundedCornerShape(topStartPercent = 45, topEndPercent = 15, bottomEndPercent = 45, bottomStartPercent = 15)
        KeyboardThemeType.MORCEAUX_DE_BOIS -> RoundedCornerShape(4.dp)
        KeyboardThemeType.GOUTTES_DEAU -> RoundedCornerShape(topStart = 12.dp, topEnd = 4.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
        else -> RoundedCornerShape(6.dp)
    }

    val sampleBorder = when (themeType) {
        KeyboardThemeType.PETITES_FLEURS -> Modifier.border(1.dp, Color(0xFFFFB6C1), sampleShape)
        KeyboardThemeType.MORCEAUX_DE_BOIS -> Modifier.border(1.5.dp, Color(0xFF3E2723), sampleShape)
        KeyboardThemeType.GOUTTES_DEAU -> Modifier.border(1.dp, Color(0xFF48CAE4), sampleShape)
        else -> Modifier
    }

    val sampleBgModifier = when (themeType) {
        KeyboardThemeType.MORCEAUX_DE_BOIS -> Modifier.background(
            Brush.verticalGradient(listOf(Color(0xFF8B5A2B), Color(0xFF6F4423)))
        )
        else -> Modifier.background(keyColor)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sélectionné",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // KEY PREVIEW SAMPLE WITH THEME SHAPES
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        sampleKeys.forEach { keyText ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(sampleShape)
                                    .then(sampleBgModifier)
                                    .then(sampleBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                if (themeType == KeyboardThemeType.GOUTTES_DEAU) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(0.48f)
                                            .align(Alignment.TopCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
                                                )
                                            )
                                    )
                                }
                                Text(text = keyText, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPickerRow(
    label: String,
    selectedColor: Color,
    onColorSelected: (Long) -> Unit
) {
    val presetPalette = listOf(
        0xFF3D2C42 to "Violet",
        0xFF1D4A63 to "Océan",
        0xFF275235 to "Forêt",
        0xFFE082B4 to "Rose Gold",
        0xFF4DD0E1 to "Aqua",
        0xFF81C784 to "Sauge",
        0xFFFFFFFF to "Blanc",
        0xFF212121 to "Sombre"
    )

    Column {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetPalette.forEach { (colorHex, _) ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(colorHex))
                        .clickable { onColorSelected(colorHex) }
                        .then(
                            if (Color(colorHex) == selectedColor) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            } else Modifier
                        )
                )
            }
        }
    }
}
