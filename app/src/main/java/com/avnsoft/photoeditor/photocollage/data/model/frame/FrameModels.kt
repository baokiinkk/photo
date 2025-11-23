package com.avnsoft.photoeditor.photocollage.data.model.frame

import com.google.gson.annotations.SerializedName

data class FrameResponse(
    @SerializedName("version") val version: String? = null,
    @SerializedName("lastModified") val lastModified: String? = null,
    @SerializedName("data") val data: List<FrameCategory>? = null,
    @SerializedName("urlRoot") val urlRoot: String? = null
)

data class FrameCategory(
    @SerializedName("eventName") val categoryName: String? = null,
    @SerializedName("categoryId") val categoryId: Int? = null,
    @SerializedName("content") val content: List<FrameItem>? = null,
    @SerializedName("isPro") val isPro: Boolean? = null,
    @SerializedName("isReward") val isReward: Boolean? = null,
    @SerializedName("isFree") val isFree: Boolean? = null
)

data class FrameItem(
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null, // File name like "frame_valentine_1.png"
    @SerializedName("urlThumb") val urlThumb: String? = null, // Relative URL path for thumbnail
    @SerializedName("urlFrame") val urlFrame: String? = null, // Relative URL path for frame image
    @SerializedName("isPro") val isPro: Boolean? = null // Optional PRO flag for individual items
)

