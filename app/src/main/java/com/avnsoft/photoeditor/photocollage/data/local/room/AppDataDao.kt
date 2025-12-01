package com.avnsoft.photoeditor.photocollage.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.avnsoft.photoeditor.photocollage.data.model.image.ImageInfoRoomModel
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternRoomModel
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerRoomModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateCategoryRoomModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateRoomModel
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

    // =============================== template ============================

    @Query("DELETE FROM template_table")
    suspend fun deleteAllTemplate()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTemplate(list: List<TemplateCategoryRoomModel>)

    @Transaction
    suspend fun refreshAllCategories(list: List<TemplateCategoryRoomModel>) {
        deleteAllTemplate()
        insertAllTemplate(list)
    }

    @Query("SELECT * FROM template_table")
    fun getPreviewTemplateCategories(): Flow<List<TemplateCategoryRoomModel>>


    // =============================== Image info ============================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertImage(message: ImageInfoRoomModel): Long

    @Query("SELECT * FROM image_info")
    fun getImages(): Flow<List<ImageInfoRoomModel>>

    @Query("DELETE FROM image_info")
    fun deleteAllImage()

    @Query("DELETE FROM image_info WHERE id = :id")
    suspend fun deleteImageById(id: Long)

    @Query("UPDATE image_info SET imageUrl = :imageUrl WHERE id = :id")
    fun updateImageById(id: Int,imageUrl: String)

}