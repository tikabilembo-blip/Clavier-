package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardPreferences

@Composable
fun AccessibilitySettingsScreen(
    preferences: KeyboardPreferences
) {
    val settings by preferences.settings.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Touches & Options d'Accessibilité ♿",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Personnalisez la taille des touches, les retours haptiques, le mode une main et les raccourcis inclusifs.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // KEY SIZING & GEOMETRY
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dimensions des Touches",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Key Height Slider
                Text(text = "Hauteur des Touches : ${settings.keyHeightDp} dp", fontSize = 13.sp)
                Slider(
                    value = settings.keyHeightDp.toFloat(),
                    onValueChange = { preferences.updateSettings(settings.copy(keyHeightDp = it.toInt())) },
                    valueRange = 44f..72f,
                    steps = 14
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Text Size Slider
                Text(text = "Taille des Caractères : ${settings.keyTextSizeSp} sp", fontSize = 13.sp)
                Slider(
                    value = settings.keyTextSizeSp.toFloat(),
                    onValueChange = { preferences.updateSettings(settings.copy(keyTextSizeSp = it.toInt())) },
                    valueRange = 14f..24f,
                    steps = 10
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Corner Radius Slider
                Text(text = "Arrondi des Touches : ${settings.keyCornerRadiusDp} dp", fontSize = 13.sp)
                Slider(
                    value = settings.keyCornerRadiusDp.toFloat(),
                    onValueChange = { preferences.updateSettings(settings.copy(keyCornerRadiusDp = it.toInt())) },
                    valueRange = 4f..24f,
                    steps = 20
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FEEDBACK & ACCESSIBILITY TOGGLES
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Retours & Ergonomie",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // High Contrast Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Mode Haut Contraste 👁️", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = "Bordures renforcées sur chaque touche", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.highContrastEnabled,
                        onCheckedChange = { preferences.updateSettings(settings.copy(highContrastEnabled = it)) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Haptic Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Vibration Haptique 📳", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = "Feedback tactile à chaque pression", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.hapticEnabled,
                        onCheckedChange = { preferences.updateSettings(settings.copy(hapticEnabled = it)) }
                    )
                }

                if (settings.hapticEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Intensité : ${when(settings.hapticStrength) { 1 -> "Faible"; 3 -> "Forte"; else -> "Moyenne" }}", fontSize = 13.sp)
                    Slider(
                        value = settings.hapticStrength.toFloat(),
                        onValueChange = { preferences.updateSettings(settings.copy(hapticStrength = it.toInt())) },
                        valueRange = 1f..3f,
                        steps = 1
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // One-Handed Mode
                Text(text = "Mode Une Main 👋", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(text = "Rapproche le clavier à gauche ou à droite de l'écran", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("off" to "Désactivé", "left" to "Gaucher 👈", "right" to "Droitier 👉").forEach { (mode, label) ->
                        FilterChip(
                            selected = settings.oneHandedMode == mode,
                            onClick = { preferences.updateSettings(settings.copy(oneHandedMode = mode)) },
                            label = { Text(label) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Inclusive Bar Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Barre Écriture Inclusive 🌸", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = "Accès rapide aux suffixes ·e, ·es, iel, toustes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.showInclusiveBar,
                        onCheckedChange = { preferences.updateSettings(settings.copy(showInclusiveBar = it)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
