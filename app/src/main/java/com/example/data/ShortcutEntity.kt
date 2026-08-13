package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trigger: String,
    val expansion: String,
    val category: String = "Général",
    val usageCount: Int = 0,
    val createdTimestamp: Long = System.currentTimeMillis()
)
