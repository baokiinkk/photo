package com.avnsoft.photoeditor.photocollage.data.model.pattern

import com.basesource.base.ui.base.IScreenData
import kotlinx.serialization.Serializable

@Serializable
data class PatternModel(
    val eventId: Long,
    val urlThumb: String?,
    val tabName: String,
    val content: List<PatternContentModel>,
    val isPro: Boolean,
    val isFree: Boolean,
    val isReward: Boolean,
    val timeCreate: String = System.currentTimeMillis().toString(),
    val isUsed: Boolean,
    val total: String,
    val bannerUrl: String?
) : IScreenData

@Serializable
data class PatternContentModel(
    val title: String,
    val name: String,
    val urlThumb: String,
)