package com.avnsoft.photoeditor.photocollage.data.model.sticker

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "sticker_table")
@TypeConverters(StickerConverter::class)
data class StickerRoomModel(
    @PrimaryKey
    val eventId: Long,
    val eventName: String,
    val description: String,
    val urlThumb: String,
    val content: List<StickerContentRoom> = emptyList(),
    val urlZip: String?,
    val isPro: Boolean,
    val isFree: Boolean,
    val isReward: Boolean,
    val timeCreate: String = System.currentTimeMillis().toString(),
    val isUsed: Boolean,
    val bannerUrl: String
)

data class StickerContentRoom(
    val title: String,
    val name: String,
    val urlThumb: String,
)

class StickerConverter {

    @TypeConverter
    fun fromContent(list: List<StickerContentRoom>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toContent(json: String?): List<StickerContentRoom> {
        if (json == null) return emptyList()
        val type = object : TypeToken<List<StickerContentRoom>>() {}.type
        return Gson().fromJson(json, type)
    }
}