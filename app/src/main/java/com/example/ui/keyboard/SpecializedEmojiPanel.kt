package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KeyboardSettings

data class EmojiCategoryData(
    val id: String,
    val title: String,
    val icon: String,
    val items: List<String>
)

data class QuickShortcutData(
    val label: String,
    val expansion: String,
    val description: String = ""
)

object SpecializedEmojiData {
    val categories = listOf(
        EmojiCategoryData(
            id = "feminism",
            title = "Féminisme & Égalité",
            icon = "♀️",
            items = listOf(
                "♀️", "✊🏿", "✊🏻", "✊🏽", "💜", "👑", "♀️‍🌈", "👭", 
                "👩‍🔬", "👩‍💻", "👩‍🎨", "👩‍⚖️", "👩‍🚀", "⚡", "💖", "💐", 
                "🌸", "🌺", "🕊️", "🌹", "🦸‍♀️", "👩‍🏫", "👩‍⚕️", "✊🏼"
            )
        ),
        EmojiCategoryData(
            id = "inclusion",
            title = "Inclusivité & Diversité",
            icon = "🌈",
            items = listOf(
                "🌈", "🏳️‍🌈", "🏳️‍⚧️", "🧑", "🧑‍🤝‍🧑", "🧑‍🦽", "🧑‍🦯", "🫶", 
                "🤝", "🌐", "♾️", "🖐️", "🤍", "🪪", "🫂", "💛", 
                "🟣", "🟢", "✨", "🧬", "🤟", "🦻", "🤝🏿", "🫂"
            )
        ),
        EmojiCategoryData(
            id = "justice",
            title = "Justice Sociale",
            icon = "⚖️",
            items = listOf(
                "⚖️", "📢", "📣", "✊", "🕊️", "🕯️", "📜", "🛡️", 
                "🌱", "🪴", "🌿", "💡", "🌍", "🔥", "🤝", "✊🏿", 
                "🏛️", "🗳️", "🕊️", "💬", "⚖️", "✊🏻", "✊🏽", "📜"
            )
        ),
        EmojiCategoryData(
            id = "symbols",
            title = "Grammaire Inclusive",
            icon = "·",
            items = listOf(
                "·", "•", "⁘", "♀", "♂", "⚧", "⚢", "⚣", 
                "⚥", "⚩", "⚪", "🟣", "🟢", "💛", "💜", "🤍", 
                "✨", "⚡", "▪️", "▫️", "✦", "✧", "★", "☆"
            )
        )
    )

    val quickShortcuts = listOf(
        QuickShortcutData("Point médian", "·", "Caractère inclusif principal"),
        QuickShortcutData("Pluriel inclusif", "·e·s", "Extension pluriel"),
        QuickShortcutData("Féminin inclusif", "·e", "Extension féminin"),
        QuickShortcutData("toustes", "toustes", "Pronom inclusif"),
        QuickShortcutData("tous·tes", "tous·tes", "Pronom avec point médian"),
        QuickShortcutData("égalité", "égalité", "Valeur fondamentale"),
        QuickShortcutData("sororité", "sororité", "Solidarité féminine"),
        QuickShortcutData("inclusif·ve", "inclusif·ve", "Adjectif inclusif"),
        QuickShortcutData("citoyen·ne·s", "citoyen·ne·s", "Nom inclusif"),
        QuickShortcutData("lecteur·ice·s", "lecteur·ice·s", "Nom inclusif"),
        QuickShortcutData("non-binaire", "non-binaire", "Identité de genre"),
        QuickShortcutData("Droits Humains", "Droits Humains 📜", "Liberté & Justice"),
        QuickShortcutData("Justice Sociale", "Justice Sociale ⚖️", "Équité sociale"),
        QuickShortcutData("Sororité & Liberté", "Égalité, Sororité & Liberté ✨", "Devise engagée")
    )
}

@Composable
fun SpecializedEmojiPanel(
    settings: KeyboardSettings,
    height: Dp,
    cornerRadius: Dp,
    listener: KeyboardActionListener,
    triggerHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = settings.getColors()
    var selectedTabId by remember { mutableStateOf("feminism") }
    var selectedSubCategory by remember { mutableStateOf("emojis") } // "emojis" or "shortcuts"

    val activeCategory = remember(selectedTabId) {
        SpecializedEmojiData.categories.find { it.id == selectedTabId }
            ?: SpecializedEmojiData.categories.first()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        // --- 1. TOP CATEGORY TABS ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(SpecializedEmojiData.categories) { category ->
                val isSelected = selectedTabId == category.id && selectedSubCategory == "emojis"
                val chipBg by animateColorAsState(
                    targetValue = if (isSelected) Color(colors.accentGlow) else Color(colors.keyBackground).copy(alpha = 0.85f),
                    label = "tabBg"
                )
                val chipTextColor = if (isSelected) Color.Black else Color(colors.keyText)

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            triggerHaptic()
                            selectedTabId = category.id
                            selectedSubCategory = "emojis"
                        },
                    color = chipBg,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = if (isSelected) 4.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = category.icon, fontSize = 15.sp)
                        Text(
                            text = category.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = chipTextColor
                        )
                    }
                }
            }

            // Shortcut expressions Tab
            item {
                val isShortcutSelected = selectedSubCategory == "shortcuts"
                val chipBg by animateColorAsState(
                    targetValue = if (isShortcutSelected) Color(colors.accentGlow) else Color(colors.keyBackground).copy(alpha = 0.85f),
                    label = "shortcutTabBg"
                )
                val chipTextColor = if (isShortcutSelected) Color.Black else Color(colors.keyText)

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            triggerHaptic()
                            selectedSubCategory = "shortcuts"
                        },
                    color = chipBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "⚡", fontSize = 15.sp)
                        Text(
                            text = "Raccourcis Engagés",
                            fontSize = 12.sp,
                            fontWeight = if (isShortcutSelected) FontWeight.Bold else FontWeight.Medium,
                            color = chipTextColor
                        )
                    }
                }
            }
        }

        // --- 2. MAIN GRID CONTENT AREA ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(colors.keyBackground).copy(alpha = 0.35f))
                .padding(4.dp)
        ) {
            if (selectedSubCategory == "shortcuts") {
                // Quick Expressions List
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    items(SpecializedEmojiData.quickShortcuts) { shortcut ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    triggerHaptic()
                                    listener.onKeyText(shortcut.expansion)
                                },
                            color = Color(colors.keyBackground),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(colors.accentGlow).copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = shortcut.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(colors.keyText)
                                )
                                Text(
                                    text = shortcut.expansion,
                                    fontSize = 11.sp,
                                    color = Color(colors.keyText).copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            } else {
                // Emojis 2-Row Horizontal Grid
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(activeCategory.items) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(colors.keyBackground))
                                .clickable {
                                    triggerHaptic()
                                    listener.onKeyText(emoji)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // --- 3. QUICK PANEL CONTROL STRIP ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Dot Key
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        triggerHaptic()
                        listener.onKeyText("·")
                    },
                color = Color(colors.accentGlow).copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "· Point Médian",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(colors.keyText)
                    )
                }
            }

            // Quick Space Key
            Surface(
                modifier = Modifier
                    .weight(1.5f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        triggerHaptic()
                        listener.onSpace()
                    },
                color = Color(colors.keyBackground)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Espace",
                        fontSize = 11.sp,
                        color = Color(colors.keyText).copy(alpha = 0.8f)
                    )
                }
            }

            // Quick Backspace Key
            RepeatableKeyButton(
                icon = Icons.Default.Backspace,
                modifier = Modifier
                    .weight(0.8f)
                    .height(32.dp),
                bgColor = Color(colors.keyBackground).copy(alpha = 0.8f),
                tint = Color(colors.keyText),
                cornerRadius = 6.dp,
                onClick = {
                    triggerHaptic()
                    listener.onDelete()
                }
            )
        }
    }
}
