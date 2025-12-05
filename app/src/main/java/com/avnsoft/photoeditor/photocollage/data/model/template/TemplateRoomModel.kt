package com.avnsoft.photoeditor.photocollage.data.model.template

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Category model - contains category name and list of templates
@Entity(tableName = "template_table")
@TypeConverters(TemplateCategoryConverter::class)
data class TemplateCategoryRoomModel(
    @PrimaryKey
    val id: String,
    val category: String?,
    val templates: List<TemplateRoomModel>?
)

data class TemplateRoomModel(
    val bannerUrl: String?,
    val previewUrl: String? = null, // From TemplateData.previewUrl
    val frameUrl: String?, // From TemplateData.frameUrl
    val content: List<TemplateContentRoom>?, // From TemplateData.content (cells)
    val layerContents: List<TemplateContentRoom>?, // From TemplateData.layer
    val timeCreate: String = System.currentTimeMillis().toString(),
    val isUsed: Boolean?, // From TemplateData.isUsed
    val isPro: Boolean? = false, // From TemplateData.isPro
    val isReward: Boolean? = false, // From TemplateData.isReward
    val isFree: Boolean? = false, // From TemplateData.isFree
    val width: Int? = null, // From TemplateData.width
    val height: Int? = null // From TemplateData.height
)

data class TemplateContentRoom(
    val urlThumb: String?,
    val x: Float?,
    val y: Float?,
    val width: Float?,
    val height: Float?,
    val rotate: Int? = null,
)

class TemplateCategoryConverter {

    @TypeConverter
    fun fromTemplates(list: List<TemplateRoomModel>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toTemplates(json: String?): List<TemplateRoomModel> {
        if (json == null) return emptyList()
        val type = object : TypeToken<List<TemplateRoomModel>>() {}.type
        return Gson().fromJson(json, type)
    }
}