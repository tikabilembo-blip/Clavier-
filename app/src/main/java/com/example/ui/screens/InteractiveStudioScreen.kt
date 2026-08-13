package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.KeyboardPreferences
import com.example.ui.components.FrontImageBanner
import com.example.ui.components.ModifyFrontImageDialog
import com.example.ui.keyboard.KeyboardActionListener
import com.example.ui.keyboard.KeyboardView
import com.example.ui.keyboard.PredictiveEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveStudioScreen(
    preferences: KeyboardPreferences
) {
    val context = LocalContext.current
    val settings by preferences.settings.collectAsState()
    val scope = rememberCoroutineScope()

    val database = remember { AppDatabase.getInstance(context) }
    val predictiveEngine = remember { PredictiveEngine(database.keyboardDao()) }

    var testInputText by remember { mutableStateOf("Bonjour à toustes ! Testez le clavier inclusive...") }
    var currentWord by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var lastActionLog by remember { mutableStateOf("Ready to test keyboard!") }
    var showImageDialog by remember { mutableStateOf(false) }

    if (showImageDialog) {
        ModifyFrontImageDialog(
            settings = settings,
            preferences = preferences,
            onDismiss = { showImageDialog = false }
        )
    }

    fun updateWordAndPredictions(char: String) {
        if (char.length == 1 && (char[0].isLetterOrDigit() || char[0] == '·')) {
            currentWord += char
            scope.launch {
                suggestions = predictiveEngine.getSuggestions(currentWord)
            }
        } else {
            currentWord = ""
            suggestions = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP CONTROL BAR & LIVE OUTPUT BOX
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // FRONT IMAGE BANNER WITH MODIFY BUTTON
            FrontImageBanner(
                settings = settings,
                onModifyClick = { showImageDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Studio d'Essai Interactif 🌸",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = {
                    testInputText = ""
                    currentWord = ""
                    suggestions = emptyList()
                    lastActionLog = "Champ effacé"
                }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Effacer", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Text(
                text = "Thème actif: ${settings.themeType.title}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // LIVE TEXT DISPLAY AREA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Résultat de Saisie :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (testInputText.isEmpty()) "Tapez sur les touches ci-dessous..." else testInputText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (testInputText.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FEATURE TIPS CHIPS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {
                        testInputText += " omw"
                        currentWord = "omw"
                        scope.launch { suggestions = predictiveEngine.getSuggestions("omw") }
                    },
                    label = { Text("Tester raccourci 'omw'") },
                    leadingIcon = { Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = {
                        testInputText += " bonjoir"
                        currentWord = "bonjoir"
                        scope.launch { suggestions = predictiveEngine.getSuggestions("bonjoir") }
                    },
                    label = { Text("Tester correction 'bonjoir'") },
                    leadingIcon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Log d'activité : $lastActionLog",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // EMBEDDED REAL-TIME KEYBOARD PREVIEW
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                KeyboardView(
                    settings = settings,
                    currentTypedText = currentWord,
                    predictiveSuggestions = suggestions,
                    onSuggestionSelected = { replacement ->
                        val words = testInputText.split(" ").toMutableList()
                        if (words.isNotEmpty()) {
                            words.removeAt(words.size - 1)
                        }
                        words.add(replacement)
                        testInputText = words.joinToString(" ") + " "
                        currentWord = ""
                        suggestions = emptyList()
                        lastActionLog = "Suggestion insérée: $replacement"
                    },
                    listener = object : KeyboardActionListener {
                        override fun onKeyText(text: String) {
                            testInputText += text
                            updateWordAndPredictions(text)
                            lastActionLog = "Touche pressée: '$text'"
                        }

                        override fun onDelete() {
                            if (testInputText.isNotEmpty()) {
                                testInputText = testInputText.dropLast(1)
                            }
                            if (currentWord.isNotEmpty()) {
                                currentWord = currentWord.dropLast(1)
                                scope.launch { suggestions = predictiveEngine.getSuggestions(currentWord) }
                            }
                            lastActionLog = "Suppression"
                        }

                        override fun onSpace() {
                            // Check autocorrect
                            val autocorrect = predictiveEngine.getAutocorrectWord(currentWord)
                            if (autocorrect != null) {
                                val words = testInputText.split(" ").toMutableList()
                                if (words.isNotEmpty()) {
                                    words.removeAt(words.size - 1)
                                }
                                words.add(autocorrect)
                                testInputText = words.joinToString(" ") + " "
                                lastActionLog = "Autocorrect: '$currentWord' -> '$autocorrect'"
                            } else {
                                testInputText += " "
                                lastActionLog = "Espace"
                            }
                            currentWord = ""
                            suggestions = emptyList()
                        }

                        override fun onEnter() {
                            testInputText += "\n"
                            currentWord = ""
                            suggestions = emptyList()
                            lastActionLog = "Retour à la ligne"
                        }

                        override fun onCursorMove(direction: Int) {
                            lastActionLog = "Déplacement curseur ($direction)"
                        }

                        override fun onShortcutTrigger(expansion: String) {
                            val words = testInputText.split(" ").toMutableList()
                            if (words.isNotEmpty()) {
                                words.removeAt(words.size - 1)
                            }
                            words.add(expansion)
                            testInputText = words.joinToString(" ") + " "
                            currentWord = ""
                            suggestions = emptyList()
                            lastActionLog = "Raccourci exécuté: '$expansion'"
                        }
                    }
                )
            }
        }
    }
}
