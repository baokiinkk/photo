package com.avnsoft.photoeditor.photocollage.data.model.template

import com.google.gson.annotations.SerializedName

data class TemplateResponse(
    @SerializedName("version") val version: String?,
    @SerializedName("lastModified") val lastModified: String?,
    @SerializedName("data") val data: List<DataTemplate>,
)

data class DataTemplate(
    @SerializedName("category")
    val category: String?,
    @SerializedName("content")
    val content: List<TemplateData>?,

    )

data class TemplateData(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("bannerUrl")
    val bannerUrl: String?,
    @SerializedName("previewUrl")
    val previewUrl: String? = null,
    @SerializedName("frameUrl")
    val frameUrl: String?,
    @SerializedName("content")
    val content: List<TemplateContent>?,
    @SerializedName("isPro")
    val isPro: Boolean? = false,
    @SerializedName("isReward")
    val isReward: Boolean? = false,
    @SerializedName("isFree")
    val isFree: Boolean? = false,
    @SerializedName("isUsed")
    val isUsed: Boolean? = false,
)

data class TemplateContent(
    @SerializedName("urlThumb") val urlThumb: String,
    @SerializedName("x") val x: Float,
    @SerializedName("y") val y: Float,
    @SerializedName("width") val width: Float,
    @SerializedName("height") val height: Float,
    @SerializedName("rotate") val rotate: Int? = null,
)