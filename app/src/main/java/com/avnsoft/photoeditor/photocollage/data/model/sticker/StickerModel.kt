package com.avnsoft.photoeditor.photocollage.data.model.sticker

import com.basesource.base.ui.base.IScreenData

data class StickerModel(
    val eventId: Long,
    val iconTabUrl: String?,
    val tabName: String,
    val content: List<StickerContentModel>,
    val isPro: Boolean,
    val isFree: Boolean,
    val isReward: Boolean,
    val timeCreate: String = System.currentTimeMillis().toString(),
    val isUsed: Boolean,
    val total: String,
    val bannerUrl: String?
) : IScreenData

data class StickerContentModel(
    val title: String,
    val name: String,
    val urlThumb: String,
)