package com.avnsoft.photoeditor.photocollage.data.model.sticker

import com.google.gson.annotations.SerializedName

data class StickerResponse(
    @SerializedName("version") val version: String?,
    @SerializedName("lastModified") val lastModified: String?,
    @SerializedName("data") val data: List<StickerData>,
    @SerializedName("urlRoot") val urlRoot: String
)

data class StickerData(
    @SerializedName("eventName")
    val eventName: String,
    @SerializedName("eventId")
    val eventId: Long,
    @SerializedName("description")
    val description: String,
    @SerializedName("urlThumb")
    val urlThumb: String,
    @SerializedName("content")
    val content: List<StickerContentResponse>,
    @SerializedName("urlZip")
    val urlZip: String?,
    @SerializedName("isPro")
    val isPro: Boolean,
    @SerializedName("isFree")
    val isFree: Boolean,
    @SerializedName("isReward")
    val isReward: Boolean,
    @SerializedName("isUsed")
    val isUsed: Boolean,
    @SerializedName("bannerUrl")
    val bannerUrl: String
)

data class StickerContentResponse(
    @SerializedName("title")
    val title: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("urlThumb")
    val urlThumb: String,
)


