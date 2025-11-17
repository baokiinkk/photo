package com.avnsoft.photoeditor.photocollage.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerRoomModel
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDataDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    fun insertAll(stickers: List<StickerRoomModel>): List<Long>

    @Query("SELECT * FROM sticker_table")
    fun getStickers(): List<StickerRoomModel>

    @Query("SELECT * FROM sticker_table")
    fun getPreviewStickers(): Flow<List<StickerRoomModel>>

    @Query("UPDATE sticker_table SET isUsed = :isUsed WHERE eventId = :eventId")
    fun updateIsUsedById(eventId: Long, isUsed: Boolean)
}