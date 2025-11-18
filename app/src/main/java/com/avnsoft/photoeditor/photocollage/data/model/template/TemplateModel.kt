package com.avnsoft.photoeditor.photocollage.data.model.template

import com.basesource.base.ui.base.IScreenData

data class TemplateModel(
    val eventId: Long,
    val urlThumb: String?,
    val tabName: String,
    val content: List<TemplateContentModel>,
    val timeCreate: String = System.currentTimeMillis().toString(),
    val isUsed: Boolean,
    val total: String,
    val bannerUrl: String
) : IScreenData

data class TemplateContentModel(
    val title: String,
    val name: String,
    val urlThumb: String,
)