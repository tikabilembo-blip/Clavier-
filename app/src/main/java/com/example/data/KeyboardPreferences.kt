package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class KeyboardThemeType(val key: String, val title: String, val icon: String) {
    SOLIDARITE("solidarite", "Féministe Solidarité ✊🏿", "✊🏿"),
    PETITES_FLEURS("petites_fleurs", "Petites Fleurs 🌸", "🌸"),
    MORCEAUX_DE_BOIS("morceaux_de_bois", "Morceaux de Bois 🪵", "🪵"),
    GOUTTES_DEAU("gouttes_deau", "Gouttes d'Eau 💧", "💧"),
    FLEURS("fleurs", "Fleurs & Féministe 🌸", "🌸"),
    EAU("eau", "Eau & Océan 💧", "💧"),
    FORET("foret", "Forêt & Nature 🌿", "🌿"),
    CUSTOM("custom", "Personnalisé 🎨", "🎨")
}

data class KeyboardThemeColors(
    val bgGradientStart: Long,
    val bgGradientEnd: Long,
    val keyBackground: Long,
    val keyPressedBackground: Long,
    val keyText: Long,
    val accentGlow: Long,
    val topBarBg: Long
)

data class KeyboardSettings(
    val themeType: KeyboardThemeType = KeyboardThemeType.SOLIDARITE,
    val keyHeightDp: Int = 54, // 44 to 72 dp
    val keyTextSizeSp: Int = 18, // 14 to 24 sp
    val keyCornerRadiusDp: Int = 12, // 4 to 24 dp
    val hapticEnabled: Boolean = true,
    val hapticStrength: Int = 2, // 1: light, 2: medium, 3: heavy
    val soundEnabled: Boolean = true,
    val highContrastEnabled: Boolean = false,
    val oneHandedMode: String = "off", // "off", "left", "right"
    val showInclusiveBar: Boolean = true,
    val autoCorrectEnabled: Boolean = true,
    val predictiveTextEnabled: Boolean = true,
    // Custom theme colors (HEX ARGB values)
    val customKeyBg: Long = 0xFF3D2C42,
    val customKeyText: Long = 0xFFFFFFFF,
    val customBgStart: Long = 0xFF1F1528,
    val customBgEnd: Long = 0xFF2D1E3A,
    val customAccent: Long = 0xFFE082B4,
    // Front Image / Hero Photo Preference
    val customFrontImageUri: String = "preset:img_front_banner_1"
) {
    fun getColors(): KeyboardThemeColors {
        return when (themeType) {
            KeyboardThemeType.SOLIDARITE -> KeyboardThemeColors(
                bgGradientStart = 0xFF311B92,
                bgGradientEnd = 0xFF512DA8,
                keyBackground = 0xFFFFFFFF,
                keyPressedBackground = 0xFFEDE7F6,
                keyText = 0xFF311B92,
                accentGlow = 0xFFFF4081,
                topBarBg = 0xFF281180
            )
            KeyboardThemeType.PETITES_FLEURS -> KeyboardThemeColors(
                bgGradientStart = 0xFF2B192E,
                bgGradientEnd = 0xFF421D3B,
                keyBackground = 0xFFFFF0F5,
                keyPressedBackground = 0xFFFFD1DC,
                keyText = 0xFF880E4F,
                accentGlow = 0xFFFF69B4,
                topBarBg = 0xFF201123
            )
            KeyboardThemeType.MORCEAUX_DE_BOIS -> KeyboardThemeColors(
                bgGradientStart = 0xFF1A100C,
                bgGradientEnd = 0xFF2A1B14,
                keyBackground = 0xFF8B5A2B,
                keyPressedBackground = 0xFF6F4423,
                keyText = 0xFFFFF8E7,
                accentGlow = 0xFFD7CCC8,
                topBarBg = 0xFF120A07
            )
            KeyboardThemeType.GOUTTES_DEAU -> KeyboardThemeColors(
                bgGradientStart = 0xFF03045E,
                bgGradientEnd = 0xFF0077B6,
                keyBackground = 0xFF00B4D8,
                keyPressedBackground = 0xFF0096C7,
                keyText = 0xFFFFFFFF,
                accentGlow = 0xFF90E0EF,
                topBarBg = 0xFF020340
            )
            KeyboardThemeType.FLEURS -> KeyboardThemeColors(
                bgGradientStart = 0xFF2A1B30,
                bgGradientEnd = 0xFF3B2544,
                keyBackground = 0xFF4A3258,
                keyPressedBackground = 0xFF6C4680,
                keyText = 0xFFF7EBFD,
                accentGlow = 0xFFE082B4,
                topBarBg = 0xFF23162A
            )
            KeyboardThemeType.EAU -> KeyboardThemeColors(
                bgGradientStart = 0xFF0D2533,
                bgGradientEnd = 0xFF16384C,
                keyBackground = 0xFF1D4A63,
                keyPressedBackground = 0xFF2B6B8E,
                keyText = 0xFFE0F7FA,
                accentGlow = 0xFF4DD0E1,
                topBarBg = 0xFF091B26
            )
            KeyboardThemeType.FORET -> KeyboardThemeColors(
                bgGradientStart = 0xFF132A1A,
                bgGradientEnd = 0xFF1C3D27,
                keyBackground = 0xFF275235,
                keyPressedBackground = 0xFF38734C,
                keyText = 0xFFE8F5E9,
                accentGlow = 0xFF81C784,
                topBarBg = 0xFF0C1D12
            )
            KeyboardThemeType.CUSTOM -> KeyboardThemeColors(
                bgGradientStart = customBgStart,
                bgGradientEnd = customBgEnd,
                keyBackground = customKeyBg,
                keyPressedBackground = (customKeyBg and 0x00FFFFFF) or 0xCC000000,
                keyText = customKeyText,
                accentGlow = customAccent,
                topBarBg = (customBgStart and 0x00FFFFFF) or 0xE6000000
            )
        }
    }
}

class KeyboardPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("feminist_keyboard_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<KeyboardSettings> = _settings.asStateFlow()

    private fun loadSettings(): KeyboardSettings {
        val themeStr = prefs.getString("theme", KeyboardThemeType.SOLIDARITE.key) ?: KeyboardThemeType.SOLIDARITE.key
        val theme = KeyboardThemeType.entries.find { it.key == themeStr } ?: KeyboardThemeType.SOLIDARITE

        return KeyboardSettings(
            themeType = theme,
            keyHeightDp = prefs.getInt("key_height", 54),
            keyTextSizeSp = prefs.getInt("key_text_size", 18),
            keyCornerRadiusDp = prefs.getInt("key_corner_radius", 12),
            hapticEnabled = prefs.getBoolean("haptic_enabled", true),
            hapticStrength = prefs.getInt("haptic_strength", 2),
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            highContrastEnabled = prefs.getBoolean("high_contrast", false),
            oneHandedMode = prefs.getString("one_handed", "off") ?: "off",
            showInclusiveBar = prefs.getBoolean("show_inclusive_bar", true),
            autoCorrectEnabled = prefs.getBoolean("autocorrect", true),
            predictiveTextEnabled = prefs.getBoolean("predictive", true),
            customKeyBg = prefs.getLong("custom_key_bg", 0xFF3D2C42),
            customKeyText = prefs.getLong("custom_key_text", 0xFFFFFFFF),
            customBgStart = prefs.getLong("custom_bg_start", 0xFF1F1528),
            customBgEnd = prefs.getLong("custom_bg_end", 0xFF2D1E3A),
            customAccent = prefs.getLong("custom_accent", 0xFFE082B4),
            customFrontImageUri = prefs.getString("custom_front_image_uri", "preset:img_front_banner_1") ?: "preset:img_front_banner_1"
        )
    }

    fun updateSettings(newSettings: KeyboardSettings) {
        prefs.edit().apply {
            putString("theme", newSettings.themeType.key)
            putInt("key_height", newSettings.keyHeightDp)
            putInt("key_text_size", newSettings.keyTextSizeSp)
            putInt("key_corner_radius", newSettings.keyCornerRadiusDp)
            putBoolean("haptic_enabled", newSettings.hapticEnabled)
            putInt("haptic_strength", newSettings.hapticStrength)
            putBoolean("sound_enabled", newSettings.soundEnabled)
            putBoolean("high_contrast", newSettings.highContrastEnabled)
            putString("one_handed", newSettings.oneHandedMode)
            putBoolean("show_inclusive_bar", newSettings.showInclusiveBar)
            putBoolean("autocorrect", newSettings.autoCorrectEnabled)
            putBoolean("predictive", newSettings.predictiveTextEnabled)
            putLong("custom_key_bg", newSettings.customKeyBg)
            putLong("custom_key_text", newSettings.customKeyText)
            putLong("custom_bg_start", newSettings.customBgStart)
            putLong("custom_bg_end", newSettings.customBgEnd)
            putLong("custom_accent", newSettings.customAccent)
            putString("custom_front_image_uri", newSettings.customFrontImageUri)
            apply()
        }
        _settings.value = newSettings
    }

    fun setTheme(themeType: KeyboardThemeType) {
        updateSettings(_settings.value.copy(themeType = themeType))
    }

    companion object {
        @Volatile
        private var INSTANCE: KeyboardPreferences? = null

        fun getInstance(context: Context): KeyboardPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = KeyboardPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
