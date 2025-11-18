package com.avnsoft.photoeditor.photocollage.data.model.template

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@Entity(tableName = "template_table")
@TypeConverters(TemplateConverter::class)
data class TemplateRoomModel(
    @PrimaryKey
    val eventId: Long,
    val eventName: String,
    val urlThumb: String,
    val content: List<TemplateContentRoom> = emptyList(),
    val timeCreate: String = System.currentTimeMillis().toString(),
    val isUsed: Boolean,
    val bannerUrl: String
)

data class TemplateContentRoom(
    val title: String,
    val name: String,
    val urlThumb: String,
)

class TemplateConverter {

    @TypeConverter
    fun fromContent(list: List<TemplateContentRoom>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toContent(json: String?): List<TemplateContentRoom> {
        if (json == null) return emptyList()
        val type = object : TypeToken<List<TemplateContentRoom>>() {}.type
        return Gson().fromJson(json, type)
    }
}