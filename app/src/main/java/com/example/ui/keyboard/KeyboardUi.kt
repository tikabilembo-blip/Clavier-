package com.example.ui.keyboard

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardSettings
import com.example.data.KeyboardThemeType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class KeyboardMode {
    ALPHA_LOWER, ALPHA_UPPER, NUMERIC, SYMBOL, EMOJI
}

interface KeyboardActionListener {
    fun onKeyText(text: String)
    fun onDelete()
    fun onSpace()
    fun onEnter()
    fun onCursorMove(direction: Int) // -1 left, 1 right, -10 up, 10 down
    fun onShortcutTrigger(expansion: String)
}

@Composable
fun KeyboardView(
    settings: KeyboardSettings,
    listener: KeyboardActionListener,
    currentTypedText: String = "",
    predictiveSuggestions: List<String> = emptyList(),
    onSuggestionSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }
    val colors = settings.getColors()

    var keyboardMode by remember { mutableStateOf(KeyboardMode.ALPHA_LOWER) }
    var showTrackpad by remember { mutableStateOf(false) }
    var showInclusiveShortcuts by remember { mutableStateOf(true) }

    fun triggerHaptic() {
        if (!settings.hapticEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val ms = when (settings.hapticStrength) {
                    1 -> 10L
                    3 -> 40L
                    else -> 20L
                }
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(colors.bgGradientStart),
            Color(colors.bgGradientEnd)
        )
    )

    // One-handed layout constraint multiplier
    val horizontalPadding = when (settings.oneHandedMode) {
        "left" -> PaddingValues(start = 0.dp, end = 48.dp)
        "right" -> PaddingValues(start = 48.dp, end = 0.dp)
        else -> PaddingValues(horizontal = 4.dp)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgBrush)
            .padding(vertical = 6.dp)
            .padding(horizontalPadding)
    ) {
        // --- 1. PREDICTIVE & ACCESSIBILITY TOOLBAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(Color(colors.topBarBg).copy(alpha = 0.85f))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trackpad Toggle Button
            IconButton(
                onClick = {
                    triggerHaptic()
                    showTrackpad = !showTrackpad
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (showTrackpad) Icons.Default.KeyboardHide else Icons.Default.OpenWith,
                    contentDescription = "Trackpad curseur",
                    tint = Color(colors.accentGlow)
                )
            }

            // Inclusive Toolbar Toggle
            IconButton(
                onClick = {
                    triggerHaptic()
                    showInclusiveShortcuts = !showInclusiveShortcuts
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Accès inclusif",
                    tint = if (showInclusiveShortcuts) Color(colors.accentGlow) else Color(colors.keyText).copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Suggestions List
            if (!showTrackpad) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (predictiveSuggestions.isEmpty()) {
                        item {
                            Text(
                                text = "Saisie libre • ${settings.themeType.title}",
                                fontSize = 13.sp,
                                color = Color(colors.keyText).copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    } else {
                        items(predictiveSuggestions) { suggestion ->
                            Surface(
                                onClick = {
                                    triggerHaptic()
                                    if (suggestion.startsWith("🔑 ")) {
                                        listener.onShortcutTrigger(suggestion.removePrefix("🔑 "))
                                    } else {
                                        onSuggestionSelected(suggestion)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = Color(colors.keyBackground),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    color = Color(colors.keyText),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // CURSOR NAVIGATION TRACKPAD (ACCESSIBILITY FEATURE)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Curseur:",
                        fontSize = 12.sp,
                        color = Color(colors.keyText).copy(alpha = 0.7f)
                    )
                    IconButton(onClick = { triggerHaptic(); listener.onCursorMove(-1) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Gauche", tint = Color(colors.keyText))
                    }
                    IconButton(onClick = { triggerHaptic(); listener.onCursorMove(1) }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Droite", tint = Color(colors.keyText))
                    }
                    IconButton(onClick = { triggerHaptic(); listener.onCursorMove(-10) }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Haut", tint = Color(colors.keyText))
                    }
                    IconButton(onClick = { triggerHaptic(); listener.onCursorMove(10) }) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Bas", tint = Color(colors.keyText))
                    }
                }
            }
        }

        // --- 2. INCLUSIVE WRITING QUICK TOOLBAR (ACCESSIBILITY KEYROW) ---
        AnimatedVisibility(
            visible = settings.showInclusiveBar && showInclusiveShortcuts,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(colors.accentGlow).copy(alpha = 0.5f))
                            .border(1.dp, Color(colors.accentGlow), RoundedCornerShape(8.dp))
                            .clickable {
                                triggerHaptic()
                                keyboardMode = if (keyboardMode == KeyboardMode.EMOJI) KeyboardMode.ALPHA_LOWER else KeyboardMode.EMOJI
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "♀️ Émojis",
                            color = Color(colors.keyText),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                val inclusiveSnippets = listOf("·e", "·es", "·x", "iel", "toustes", "·a·e", "lecteur·ice·s")
                items(inclusiveSnippets) { snippet ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(colors.accentGlow).copy(alpha = 0.25f))
                            .border(1.dp, Color(colors.accentGlow).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                triggerHaptic()
                                listener.onKeyText(snippet)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = snippet,
                            color = Color(colors.keyText),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 3. MAIN KEYBOARD ROWS ---
        val keyHeight = settings.keyHeightDp.dp
        val cornerRadius = settings.keyCornerRadiusDp.dp
        val textSize = settings.keyTextSizeSp.sp

        when (keyboardMode) {
            KeyboardMode.ALPHA_LOWER -> {
                val row1 = listOf("a", "z", "e", "r", "t", "y", "u", "i", "o", "p")
                val row2 = listOf("q", "s", "d", "f", "g", "h", "j", "k", "l", "m")
                val row3 = listOf("w", "x", "c", "v", "b", "n")

                KeyRow(row1, settings, keyHeight, cornerRadius, textSize, listener, ::triggerHaptic)
                KeyRow(row2, settings, keyHeight, cornerRadius, textSize, listener, ::triggerHaptic)

                // Row 3 with Shift & Delete
                Row(
                    modifier = Modifier.fillMaxWidth().height(keyHeight).padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Shift Key
                    KeyButton(
                        text = "⇧",
                        modifier = Modifier.weight(1.5f),
                        bgColor = Color(colors.keyBackground).copy(alpha = 0.7f),
                        textColor = Color(colors.keyText),
                        cornerRadius = cornerRadius,
                        textSize = textSize,
                        onClick = {
                            triggerHaptic()
                            keyboardMode = KeyboardMode.ALPHA_UPPER
                        }
                    )

                    row3.forEach { char ->
                        KeyButton(
                            text = char,
                            modifier = Modifier.weight(1f),
                            bgColor = Color(colors.keyBackground),
                            textColor = Color(colors.keyText),
                            cornerRadius = cornerRadius,
                            textSize = textSize,
                            highContrast = settings.highContrastEnabled,
                            accentColor = Color(colors.accentGlow),
                            onClick = {
                                triggerHaptic()
                                listener.onKeyText(char)
                            }
                        )
                    }

                    // Delete Key
                    RepeatableKeyButton(
                        icon = Icons.Default.Backspace,
                        modifier = Modifier.weight(1.5f),
                        bgColor = Color(colors.keyBackground).copy(alpha = 0.7f),
                        tint = Color(colors.keyText),
                        cornerRadius = cornerRadius,
                        onClick = {
                            triggerHaptic()
                            listener.onDelete()
                        }
                    )
                }
            }

            KeyboardMode.ALPHA_UPPER -> {
                val row1 = listOf("A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P")
                val row2 = listOf("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M")
                val row3 = listOf("W", "X", "C", "V", "B", "N")

                KeyRow(row1, settings, keyHeight, cornerRadius, textSize, listener, ::triggerHaptic)
                KeyRow(row2, settings, keyHeight, cornerRadius, textSize, listener, ::triggerHaptic)

                Row(
                    modifier = Modifier.fillMaxWidth().height(keyHeight).padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KeyButton(
                        text = "⇪",
                        modifier = Modifier.weight(1.5f),
                        bgColor = Color(colors.accentGlow),
                        textColor = Color.Black,
                        cornerRadius = cornerRadius,
                        textSize = textSize,
                        onClick = {
                            triggerHaptic()
                            keyboardMode = KeyboardMode.ALPHA_LOWER
                        }
                    )

                    row3.forEach { char ->
                        KeyButton(
                            text = char,
                            modifier = Modifier.weight(1f),
                            bgColor = Color(colors.keyBackground),
                            textColor = Color(colors.keyText),
                            cornerRadius = cornerRadius,
                            textSize = textSize,
                            highContrast = settings.highContrastEnabled,
                            accentColor = Color(colors.accentGlow),
                            onClick = {
                                triggerHaptic()
                                listener.onKeyText(char)
                                keyboardMode = KeyboardMode.ALPHA_LOWER
                            }
                        )
                    }

                    RepeatableKeyButton(
                        icon = Icons.Default.Backspace,
                        modifier = Modifier.weight(1.5f),
                        bgColor = Color(colors.keyBackground).copy(alpha = 0.7f),
                        tint = Color(colors.keyText),
                        cornerRadius = cornerRadius,
                        onClick = {
                            triggerHaptic()
                            listener.onDelete()
                        }
                    )
                }
            }

            KeyboardMode.NUMERIC -> {
                val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                val row2 = listOf("@", "#", "€", "%", "&", "-", "+", "(", ")", "/")
                val row3 = listOf("*", "\"", "'", ":", ";", "!", "?")

                KeyRow(row1, settings, keyHeight, cornerRadius, textSize, listener, ::triggerHaptic)
                KeyRow(row2, settings, keyHeight, cornerRadius, textSize, listener, ::triggerHaptic)

                Row(
                    modifier = Modifier.fillMaxWidth().height(keyHeight).padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KeyButton(
                        text = "=#\\",
                        modifier = Modifier.weight(1.5f),
                        bgColor = Color(colors.keyBackground).copy(alpha = 0.7f),
                        textColor = Color(colors.keyText),
                        cornerRadius = cornerRadius,
                        textSize = 14.sp,
                        onClick = {
                            triggerHaptic()
                            keyboardMode = KeyboardMode.SYMBOL
                        }
                    )

                    row3.forEach { char ->
                        KeyButton(
                            text = char,
                            modifier = Modifier.weight(1f),
                            bgColor = Color(colors.keyBackground),
                            textColor = Color(colors.keyText),
                            cornerRadius = cornerRadius,
                            textSize = textSize,
                            onClick = {
                                triggerHaptic()
                                listener.onKeyText(char)
                            }
                        )
                    }

                    RepeatableKeyButton(
                        icon = Icons.Default.Backspace,
                        modifier = Modifier.weight(1.5f),
                        bgColor = Color(colors.keyBackground).copy(alpha = 0.7f),
                        tint = Color(colors.keyText),
                        cornerRadius = cornerRadius,
                        onClick = {
                            triggerHaptic()
                            listener.onDelete()
                        }
                    )
                }
            }

            KeyboardMode.SYMBOL -> {
                val row1 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")
                val row2 = listOf("£", "¥", "$", "¢", "^", "°", "=", "{", "}", "\\")
                val row3 = listOf("[", "]", "<", ">", "«", "»", "…")

                KeyRow(row1, settings, keyHeight, cornerRadius, textSize, listener, ::triggerHaptic)
                KeyRow(row2, settings, keyHeight, cornerRadius, textSize, listener, ::triggerHaptic)

                Row(
                    modifier = Modifier.fillMaxWidth().height(keyHeight).padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KeyButton(
                        text = "123",
                        modifier = Modifier.weight(1.5f),
                        bgColor = Color(colors.keyBackground).copy(alpha = 0.7f),
                        textColor = Color(colors.keyText),
                        cornerRadius = cornerRadius,
                        textSize = 14.sp,
                        onClick = {
                            triggerHaptic()
                            keyboardMode = KeyboardMode.NUMERIC
                        }
                    )

                    row3.forEach { char ->
                        KeyButton(
                            text = char,
                            modifier = Modifier.weight(1f),
                            bgColor = Color(colors.keyBackground),
                            textColor = Color(colors.keyText),
                            cornerRadius = cornerRadius,
                            textSize = textSize,
                            onClick = {
                                triggerHaptic()
                                listener.onKeyText(char)
                            }
                        )
                    }

                    RepeatableKeyButton(
                        icon = Icons.Default.Backspace,
                        modifier = Modifier.weight(1.5f),
                        bgColor = Color(colors.keyBackground).copy(alpha = 0.7f),
                        tint = Color(colors.keyText),
                        cornerRadius = cornerRadius,
                        onClick = {
                            triggerHaptic()
                            listener.onDelete()
                        }
                    )
                }
            }

            KeyboardMode.EMOJI -> {
                SpecializedEmojiPanel(
                    settings = settings,
                    height = keyHeight * 3.2f,
                    cornerRadius = cornerRadius,
                    listener = listener,
                    triggerHaptic = { triggerHaptic() }
                )
            }
        }

        // --- 4. BOTTOM ACTION ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyHeight)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Mode Switcher (?123 / ABC)
            KeyButton(
                text = if (keyboardMode == KeyboardMode.ALPHA_LOWER || keyboardMode == KeyboardMode.ALPHA_UPPER) "?123" else "ABC",
                modifier = Modifier.weight(1.3f),
                bgColor = Color(colors.keyBackground).copy(alpha = 0.75f),
                textColor = Color(colors.keyText),
                cornerRadius = cornerRadius,
                textSize = 14.sp,
                onClick = {
                    triggerHaptic()
                    keyboardMode = if (keyboardMode == KeyboardMode.ALPHA_LOWER || keyboardMode == KeyboardMode.ALPHA_UPPER) {
                        KeyboardMode.NUMERIC
                    } else {
                        KeyboardMode.ALPHA_LOWER
                    }
                }
            )

            // Emoji / Theme Icon
            KeyButton(
                text = settings.themeType.icon,
                modifier = Modifier.weight(1f),
                bgColor = Color(colors.keyBackground).copy(alpha = 0.75f),
                textColor = Color(colors.keyText),
                cornerRadius = cornerRadius,
                textSize = 18.sp,
                onClick = {
                    triggerHaptic()
                    keyboardMode = if (keyboardMode == KeyboardMode.EMOJI) KeyboardMode.ALPHA_LOWER else KeyboardMode.EMOJI
                }
            )

            // Inclusive Dot Key ·
            KeyButton(
                text = "·",
                modifier = Modifier.weight(0.9f),
                bgColor = Color(colors.accentGlow).copy(alpha = 0.3f),
                textColor = Color(colors.keyText),
                cornerRadius = cornerRadius,
                textSize = 20.sp,
                onClick = {
                    triggerHaptic()
                    listener.onKeyText("·")
                }
            )

            // Spacebar
            KeyButton(
                text = "Espace  ${settings.themeType.icon}",
                modifier = Modifier.weight(3.5f),
                bgColor = Color(colors.keyBackground),
                textColor = Color(colors.keyText).copy(alpha = 0.8f),
                cornerRadius = cornerRadius,
                textSize = 13.sp,
                onClick = {
                    triggerHaptic()
                    listener.onSpace()
                }
            )

            // Period .
            KeyButton(
                text = ".",
                modifier = Modifier.weight(0.9f),
                bgColor = Color(colors.keyBackground),
                textColor = Color(colors.keyText),
                cornerRadius = cornerRadius,
                textSize = 18.sp,
                onClick = {
                    triggerHaptic()
                    listener.onKeyText(".")
                }
            )

            // Enter ↵ Key
            KeyButton(
                text = "↵",
                modifier = Modifier.weight(1.3f),
                bgColor = Color(colors.accentGlow),
                textColor = Color.Black,
                cornerRadius = cornerRadius,
                textSize = 20.sp,
                onClick = {
                    triggerHaptic()
                    listener.onEnter()
                }
            )
        }
    }
}

@Composable
private fun KeyRow(
    keys: List<String>,
    settings: KeyboardSettings,
    keyHeight: Dp,
    cornerRadius: Dp,
    textSize: androidx.compose.ui.unit.TextUnit,
    listener: KeyboardActionListener,
    triggerHaptic: () -> Unit
) {
    val colors = settings.getColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyHeight)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keys.forEach { char ->
            KeyButton(
                text = char,
                modifier = Modifier.weight(1f),
                bgColor = Color(colors.keyBackground),
                textColor = Color(colors.keyText),
                cornerRadius = cornerRadius,
                textSize = textSize,
                highContrast = settings.highContrastEnabled,
                accentColor = Color(colors.accentGlow),
                themeType = settings.themeType,
                onClick = {
                    triggerHaptic()
                    listener.onKeyText(char)
                }
            )
        }
    }
}

fun getKeyShape(themeType: KeyboardThemeType, defaultRadius: Dp): androidx.compose.ui.graphics.Shape {
    return when (themeType) {
        KeyboardThemeType.PETITES_FLEURS -> RoundedCornerShape(
            topStartPercent = 45,
            topEndPercent = 15,
            bottomEndPercent = 45,
            bottomStartPercent = 15
        )
        KeyboardThemeType.MORCEAUX_DE_BOIS -> RoundedCornerShape(4.dp)
        KeyboardThemeType.GOUTTES_DEAU -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 6.dp,
            bottomEnd = 16.dp,
            bottomStart = 16.dp
        )
        else -> RoundedCornerShape(defaultRadius)
    }
}

@Composable
fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color,
    textColor: Color,
    cornerRadius: Dp,
    textSize: androidx.compose.ui.unit.TextUnit,
    highContrast: Boolean = false,
    accentColor: Color = Color.Transparent,
    themeType: KeyboardThemeType = KeyboardThemeType.SOLIDARITE,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "key_scale")
    val shape = getKeyShape(themeType, cornerRadius)

    val effectiveBgModifier = when (themeType) {
        KeyboardThemeType.MORCEAUX_DE_BOIS -> Modifier.background(
            Brush.verticalGradient(listOf(Color(0xFF8B5A2B), Color(0xFF6F4423)))
        )
        else -> Modifier.background(if (isPressed) bgColor.copy(alpha = 0.6f) else bgColor)
    }

    val themeBorderModifier = when (themeType) {
        KeyboardThemeType.PETITES_FLEURS -> Modifier.border(1.5.dp, Color(0xFFFFB6C1), shape)
        KeyboardThemeType.MORCEAUX_DE_BOIS -> Modifier.border(2.dp, Color(0xFF3E2723), shape)
        KeyboardThemeType.GOUTTES_DEAU -> Modifier.border(1.5.dp, Color(0xFF48CAE4), shape)
        else -> if (highContrast) Modifier.border(1.5.dp, accentColor, shape) else Modifier
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clip(shape)
            .then(effectiveBgModifier)
            .then(themeBorderModifier)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
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
        } else if (themeType == KeyboardThemeType.PETITES_FLEURS) {
            Text(
                text = "🌸",
                fontSize = 8.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .scale(0.85f)
            )
        }

        Text(
            text = text,
            color = textColor,
            fontSize = textSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RepeatableKeyButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    bgColor: Color,
    tint: Color,
    cornerRadius: Dp,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (isPressed) bgColor.copy(alpha = 0.5f) else bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val job = coroutineScope.launch {
                            try {
                                onClick()
                                delay(350)
                                while (isPressed) {
                                    onClick()
                                    delay(60)
                                }
                            } catch (_: Exception) {}
                        }
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                            job.cancel()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = "Delete", tint = tint)
    }
}
