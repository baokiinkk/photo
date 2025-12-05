package com.avnsoft.photoeditor.photocollage.data.model.template

import com.basesource.base.ui.base.IScreenData

// Category model - contains category name and list of templates
data class TemplateCategoryModel(
    val category: String?,
    val templates: List<TemplateModel>?
) : IScreenData

data class TemplateModel(
    val previewUrl: String?,
    val frameUrl: String?,
    val cells: List<TemplateContentModel>?,
    val layer: List<TemplateContentModel>?,
    val timeCreate: String = System.currentTimeMillis().toString(),
    val isUsed: Boolean?,
    val isPro: Boolean? = false,
    val isReward: Boolean? = false,
    val isFree: Boolean? = false,
    val bannerUrl: String?,
    val width: Int? = null,
    val height: Int? = null
) : IScreenData

data class TemplateContentModel(
    val x: Float?,
    val y: Float?,
    val width: Float?,
    val height: Float?,
    val rotate: Int? = null,
    var urlThumb: String? = null
)