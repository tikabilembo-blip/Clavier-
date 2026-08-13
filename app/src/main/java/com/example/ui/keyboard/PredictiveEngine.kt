package com.example.ui.keyboard

import com.example.data.KeyboardDao
import com.example.data.ShortcutEntity

class PredictiveEngine(private val keyboardDao: KeyboardDao? = null) {

    // Common French & Inclusive dictionary
    private val dictionary = listOf(
        "bonjour", "bonsoir", "merci", "après", "être", "avoir", "faire", "pourquoi",
        "comment", "inclusif", "inclusive", "inclusivité", "égalitaire", "égalité",
        "liberté", "sororité", "respect", "dignité", "droits", "féministe", "féminisme",
        "autonomie", "puissante", "engagée", "toustes", "iel", "ielles", "lecteur·ice·s",
        "auteur·ice", "créateur·ice", "citoyen·ne·s", "étudiant·e·s", "ami·e·s",
        "collègue·s", "membre·s", "partenaire·s", "toujours", "ensemble", "avenir",
        "aujourd'hui", "demain", "monde", "vie", "amour", "paix", "justice", "solidarité",
        "france", "monde", "message", "clavier", "application", "personnalisé", "accessibilité"
    )

    // Common typos to auto-correct
    private val typoCorrections = mapOf(
        "bonjoir" to "bonjour",
        "merco" to "merci",
        "apres" to "après",
        "etre" to "être",
        "egatite" to "égalité",
        "egalite" to "égalité",
        "inclusf" to "inclusif",
        "sororite" to "sororité",
        "liberte" to "liberté",
        "foia" to "fois",
        "touste" to "toustes",
        "autonomre" to "autonomie",
        "feminite" to "féminité",
        "frapp" to "frappe"
    )

    suspend fun getSuggestions(currentPrefix: String): List<String> {
        val trimmed = currentPrefix.trim().lowercase()
        if (trimmed.isEmpty()) {
            return listOf("inclusif·ve", "toustes", "iel")
        }

        val results = mutableListOf<String>()

        // 1. Check direct typo match
        typoCorrections[trimmed]?.let { corrected ->
            results.add(corrected)
        }

        // 2. Check shortcuts from Room DB
        keyboardDao?.getShortcutByTrigger(trimmed)?.let { shortcut ->
            results.add(0, "🔑 ${shortcut.expansion}")
        }

        // 3. Search learned words from DB
        try {
            val dbWords = keyboardDao?.getPredictions(trimmed) ?: emptyList()
            dbWords.forEach { wordEntity ->
                if (!results.contains(wordEntity.word)) {
                    results.add(wordEntity.word)
                }
            }
        } catch (_: Exception) {}

        // 4. Search in-memory dictionary
        dictionary.filter { it.lowercase().startsWith(trimmed) }
            .take(5)
            .forEach { dictWord ->
                if (!results.contains(dictWord)) {
                    results.add(dictWord)
                }
            }

        // Return up to 3 distinct predictions
        return results.distinct().take(3)
    }

    fun getAutocorrectWord(currentWord: String): String? {
        val trimmed = currentWord.trim().lowercase()
        return typoCorrections[trimmed]
    }
}
