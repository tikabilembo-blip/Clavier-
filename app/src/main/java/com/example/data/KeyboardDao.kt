package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyboardDao {
    // Shortcuts
    @Query("SELECT * FROM shortcuts ORDER BY usageCount DESC, id DESC")
    fun getAllShortcuts(): Flow<List<ShortcutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: ShortcutEntity): Long

    @Update
    suspend fun updateShortcut(shortcut: ShortcutEntity)

    @Delete
    suspend fun deleteShortcut(shortcut: ShortcutEntity)

    @Query("SELECT * FROM shortcuts WHERE lower(trigger) = lower(:trigger) LIMIT 1")
    suspend fun getShortcutByTrigger(trigger: String): ShortcutEntity?

    // Learned words
    @Query("SELECT * FROM learned_words ORDER BY frequency DESC")
    fun getAllLearnedWords(): Flow<List<CustomWordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWord(word: CustomWordEntity)

    @Query("SELECT * FROM learned_words WHERE lower(word) LIKE lower(:prefix) || '%' ORDER BY frequency DESC LIMIT 5")
    suspend fun getPredictions(prefix: String): List<CustomWordEntity>
}
