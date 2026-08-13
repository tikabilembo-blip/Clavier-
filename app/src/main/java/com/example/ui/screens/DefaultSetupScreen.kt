package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DefaultSetupScreen(
    onNavigateToStudio: () -> Unit
) {
    val context = LocalContext.current

    var isKeyboardEnabled by remember { mutableStateOf(checkKeyboardEnabled(context)) }
    var isKeyboardDefault by remember { mutableStateOf(checkKeyboardDefault(context)) }

    // Re-check status on resume
    LaunchedEffect(Unit) {
        isKeyboardEnabled = checkKeyboardEnabled(context)
        isKeyboardDefault = checkKeyboardDefault(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Hero Badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Activation du Clavier Inclusive",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Suivez les 2 étapes simples pour configurer votre clavier minimaliste et accessible comme clavier par défaut.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isKeyboardEnabled && isKeyboardDefault)
                    Color(0xFF2E7D32).copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isKeyboardEnabled && isKeyboardDefault)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isKeyboardEnabled && isKeyboardDefault) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isKeyboardEnabled && isKeyboardDefault)
                            "Clavier Actif & Par Défaut ! 🎉"
                        else
                            "Configuration Requise",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isKeyboardEnabled && isKeyboardDefault)
                            "Votre clavier est prêt à être utilisé dans toutes vos applications."
                        else
                            "Activez et sélectionnez le clavier dans les paramètres système.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // STEP 1 CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = if (isKeyboardEnabled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Text(text = "Étape 1", fontSize = 12.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Activer dans les Paramètres",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (isKeyboardEnabled) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "OK", tint = Color(0xFF2E7D32))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Autorisez le clavier dans la liste des méthodes de saisie Android.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isKeyboardEnabled
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isKeyboardEnabled) "Étape 1 Complétée ✓" else "1. Activer le Clavier")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STEP 2 CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = if (isKeyboardDefault) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    ) {
                        Text(text = "Étape 2", fontSize = 12.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Définir comme Par Défaut",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (isKeyboardDefault) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "OK", tint = Color(0xFF2E7D32))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sélectionnez 'Clavier Inclusive' comme méthode de saisie principale.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showInputMethodPicker()
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isKeyboardDefault) "Changer de Clavier" else "2. Sélectionner comme Par Défaut")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Test Playground Shortcut
        Button(
            onClick = onNavigateToStudio,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text(text = "Tester dans le Playground Interactif 🌸", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun checkKeyboardEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
    val enabledImes = imm.enabledInputMethodList
    val packageName = context.packageName
    return enabledImes.any { it.packageName == packageName }
}

private fun checkKeyboardDefault(context: Context): Boolean {
    val defaultIme = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.DEFAULT_INPUT_METHOD
    ) ?: return false
    return defaultIme.contains(context.packageName)
}
