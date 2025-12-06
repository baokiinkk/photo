package com.avnsoft.photoeditor.photocollage.data.model.template

import com.basesource.base.ui.base.IScreenData
import com.google.gson.annotations.SerializedName

// Category model - contains category name and list of templates
data class TemplateCategoryModel(
    @SerializedName("category") val category: String?,
    @SerializedName("templates") val templates: List<TemplateModel>?
) : IScreenData

data class TemplateModel(
    @SerializedName("previewUrl") val previewUrl: String?,
    @SerializedName("frameUrl") val frameUrl: String?,
    @SerializedName("cells") val cells: List<TemplateContentModel>?,
    @SerializedName("layer") val layer: List<TemplateContentModel>?,
    @SerializedName("timeCreate") val timeCreate: String = System.currentTimeMillis().toString(),
    @SerializedName("isUsed") val isUsed: Boolean?,
    @SerializedName("isPro") val isPro: Boolean? = false,
    @SerializedName("isReward") val isReward: Boolean? = false,
    @SerializedName("isFree") val isFree: Boolean? = false,
    @SerializedName("bannerUrl") val bannerUrl: String?,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null
) : IScreenData

data class TemplateContentModel(
    @SerializedName("x") var x: Float?,
    @SerializedName("y") var y: Float?,
    @SerializedName("width") val width: Float?,
    @SerializedName("height") val height: Float?,
    @SerializedName("rotate") val rotate: Int? = null,
    @SerializedName("urlThumb") var urlThumb: String? = null
)