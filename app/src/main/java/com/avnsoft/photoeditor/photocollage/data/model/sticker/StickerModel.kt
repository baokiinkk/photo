package com.avnsoft.photoeditor.photocollage.data.model.sticker

import com.basesource.base.ui.base.IScreenData
import com.google.gson.annotations.SerializedName
data class StickerModel(
    @SerializedName("eventId")
    val eventId: Long,
    @SerializedName("iconTabUrl")
    val iconTabUrl: String?,
    @SerializedName("tabName")
    val tabName: String,
    @SerializedName("content")
    val content: List<StickerContentModel>,
    @SerializedName("isPro")
    val isPro: Boolean,
    @SerializedName("isFree")
    val isFree: Boolean,
    @SerializedName("isReward")
    val isReward: Boolean,
    @SerializedName("timeCreate")
    val timeCreate: String = System.currentTimeMillis().toString(),
    @SerializedName("isUsed")
    val isUsed: Boolean,
    @SerializedName("total")
    val total: String,
    @SerializedName("bannerUrl")
    val bannerUrl: String?
) : IScreenData

data class StickerContentModel(
    @SerializedName("title")
    val title: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("urlThumb")
    val urlThumb: String,
)
