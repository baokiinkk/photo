package com.avnsoft.photoeditor.photocollage.data.model.pattern

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "pattern_table")
@TypeConverters(PatternContentConverter::class)
data class PatternRoomModel(
    @PrimaryKey
    val eventId: Long,
    val eventName: String,
    val description: String,
    val urlThumb: String,
    val content: List<PatternContentRoom> = emptyList(),
    val urlZip: String?,
    val isPro: Boolean,
    val isFree: Boolean,
    val isReward: Boolean,
    val timeCreate: String = System.currentTimeMillis().toString(),
    val isUsed: Boolean,
    val bannerUrl: String
)

data class PatternContentRoom(
    val title: String,
    val name: String,
    val urlThumb: String,
)

class PatternContentConverter {

    @TypeConverter
    fun fromContent(list: List<PatternContentRoom>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toContent(json: String?): List<PatternContentRoom> {
        if (json == null) return emptyList()
        val type = object : TypeToken<List<PatternContentRoom>>() {}.type
        return Gson().fromJson(json, type)
    }
}