package com.avnsoft.photoeditor.photocollage.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternRoomModel
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerRoomModel
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDataDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun insertAllSticker(stickers: List<StickerRoomModel>): List<Long>

    @Query("SELECT * FROM sticker_table")
    fun getStickers(): List<StickerRoomModel>

    @Query("SELECT * FROM sticker_table")
    fun getPreviewStickers(): Flow<List<StickerRoomModel>>

    @Query("UPDATE sticker_table SET isUsed = :isUsed WHERE eventId = :eventId")
    fun updateIsUsedStickerById(eventId: Long, isUsed: Boolean)

    //    ============================ pattern ============================
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun insertAllPattern(stickers: List<PatternRoomModel>): List<Long>

    @Query("SELECT * FROM pattern_table")
    fun getPatterns(): List<PatternRoomModel>

    @Query("SELECT * FROM pattern_table")
    fun getPreviewPatterns(): Flow<List<PatternRoomModel>>

    @Query("UPDATE pattern_table SET isUsed = :isUsed WHERE eventId = :eventId")
    fun updateIsUsedPatternById(eventId: Long, isUsed: Boolean)
}