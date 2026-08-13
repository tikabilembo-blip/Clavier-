package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.ShortcutEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getInstance(context).keyboardDao() }

    val shortcutsList by dao.getAllShortcuts().collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var shortcutToEdit by remember { mutableStateOf<ShortcutEntity?>(null) }

    var inputTrigger by remember { mutableStateOf("") }
    var inputExpansion by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("Général") }

    val filteredShortcuts = shortcutsList.filter {
        it.trigger.contains(searchQuery, ignoreCase = true) ||
        it.expansion.contains(searchQuery, ignoreCase = true)
    }

    // Insert Default Presets on First Run if empty
    LaunchedEffect(shortcutsList) {
        if (shortcutsList.isEmpty()) {
            val defaultPresets = listOf(
                ShortcutEntity(trigger = "omw", expansion = "En chemin ! 🚴‍♀️", category = "Quotidien"),
                ShortcutEntity(trigger = "fem", expansion = "Égalité, Sororité & Liberté ✨", category = "Inclusif"),
                ShortcutEntity(trigger = "adr", expansion = "123 Rue de l'Égalité, 75000 Paris", category = "Quotidien"),
                ShortcutEntity(trigger = "inc", expansion = "lecteur·ice·s & citoyen·ne·s", category = "Inclusif"),
                ShortcutEntity(trigger = "@e", expansion = "contact@exemple.org", category = "Quotidien")
            )
            defaultPresets.forEach { dao.insertShortcut(it) }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Gestion Sécurisée des Raccourcis 🔒",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Vos extensions de texte personnelles sont chiffrées et stockées localement sur votre appareil.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher un raccourci...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    inputTrigger = ""
                    inputExpansion = ""
                    inputCategory = "Général"
                    shortcutToEdit = null
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouveau Raccourci") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredShortcuts, key = { it.id }) { shortcut ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = shortcut.trigger,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shortcut.expansion,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Catégorie : ${shortcut.category}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = {
                            shortcutToEdit = shortcut
                            inputTrigger = shortcut.trigger
                            inputExpansion = shortcut.expansion
                            inputCategory = shortcut.category
                            showAddDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Éditer", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = {
                            scope.launch { dao.deleteShortcut(shortcut) }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // ADD / EDIT DIALOG
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (shortcutToEdit == null) "Ajouter un Raccourci" else "Modifier le Raccourci",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputTrigger,
                        onValueChange = { inputTrigger = it.trim() },
                        label = { Text("Mot Déclencheur (ex: omw)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputExpansion,
                        onValueChange = { inputExpansion = it },
                        label = { Text("Texte Développé (ex: En chemin !)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputCategory,
                        onValueChange = { inputCategory = it },
                        label = { Text("Catégorie (ex: Général, Inclusif)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputTrigger.isNotEmpty() && inputExpansion.isNotEmpty()) {
                            scope.launch {
                                if (shortcutToEdit == null) {
                                    dao.insertShortcut(
                                        ShortcutEntity(
                                            trigger = inputTrigger,
                                            expansion = inputExpansion,
                                            category = inputCategory
                                        )
                                    )
                                } else {
                                    dao.updateShortcut(
                                        shortcutToEdit!!.copy(
                                            trigger = inputTrigger,
                                            expansion = inputExpansion,
                                            category = inputCategory
                                        )
                                    )
                                }
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
