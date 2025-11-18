package com.avnsoft.photoeditor.photocollage.data.model.template

import com.google.gson.annotations.SerializedName

data class TemplateResponse(
    @SerializedName("version") val version: String?,
    @SerializedName("lastModified") val lastModified: String?,
    @SerializedName("data") val data: List<TemplateData>,
    @SerializedName("urlRoot") val urlRoot: String
)

data class TemplateData(
    @SerializedName("eventName")
    val eventName: String,
    @SerializedName("eventId")
    val eventId: Long,
    @SerializedName("urlThumb")
    val urlThumb: String,
    @SerializedName("isUsed")
    val isUsed: Boolean,
    @SerializedName("content")
    val content: List<TemplateContentResponse>,
    @SerializedName("bannerUrl")
    val bannerUrl: String
)

data class TemplateContentResponse(
    @SerializedName("title")
    val title: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("urlThumb")
    val urlThumb: String,
)