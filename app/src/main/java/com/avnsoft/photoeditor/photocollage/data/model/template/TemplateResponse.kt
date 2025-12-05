package com.avnsoft.photoeditor.photocollage.data.model.template

import com.google.gson.annotations.SerializedName

data class TemplateResponse(
    @SerializedName("version") val version: String?,
    @SerializedName("lastModified") val lastModified: String?,
    @SerializedName("urlRoot") val urlRoot: String?,
    @SerializedName("server") val server: String?,
    @SerializedName("data") val data: List<DataTemplate>,
)

data class DataTemplate(
    @SerializedName("categoryId")
    val categoryId: Int?,
    @SerializedName("categoryName")
    val categoryName: String?,
    @SerializedName("content")
    val content: List<TemplateData>?,
)

data class TemplateData(
    @SerializedName("itemId")
    val itemId: Long?,
    @SerializedName("bannerUrl")
    val bannerUrl: String?,
    @SerializedName("previewUrl")
    val previewUrl: String? = null,
    @SerializedName("frameUrl")
    val frameUrl: String?,
    @SerializedName("width")
    val width: Int?,
    @SerializedName("height")
    val height: Int?,
    @SerializedName("layer")
    val layer: List<TemplateLayer>?,
    @SerializedName("placeholder")
    val placeholder: List<TemplatePlaceholder>?,
    @SerializedName("isPro")
    val isPro: Boolean? = false,
    @SerializedName("isReward")
    val isReward: Boolean? = false,
    @SerializedName("isFree")
    val isFree: Boolean? = false,
    @SerializedName("isUsed")
    val isUsed: Boolean? = false,
)

data class TemplateLayer(
    @SerializedName("urlThumb") val urlThumb: String?,
    @SerializedName("x") val x: Float?,
    @SerializedName("y") val y: Float?,
    @SerializedName("width") val width: Float?,
    @SerializedName("height") val height: Float?,
    @SerializedName("rotate") val rotate: Int? = null,
)

data class TemplatePlaceholder(
    @SerializedName("x") val x: Float?,
    @SerializedName("y") val y: Float?,
    @SerializedName("width") val width: Float?,
    @SerializedName("height") val height: Float?,
    @SerializedName("rotate") val rotate: Int? = null,
)