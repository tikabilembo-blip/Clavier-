package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learned_words")
data class CustomWordEntity(
    @PrimaryKey
    val word: String,
    val frequency: Int = 1,
    val isUserAdded: Boolean = true
)
