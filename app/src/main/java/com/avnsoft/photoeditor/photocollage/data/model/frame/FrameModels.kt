package com.avnsoft.photoeditor.photocollage.data.model.frame

import com.google.gson.annotations.SerializedName

data class FrameResponse(
    @SerializedName("version") val version: String,
    @SerializedName("lastModified") val lastModified: String,
    @SerializedName("data") val data: List<FrameCategory>,
    @SerializedName("urlRoot") val urlRoot: String
)

data class FrameCategory(
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("content") val content: List<FrameItem>,
    @SerializedName("isPro") val isPro: Boolean,
    @SerializedName("isReward") val isReward: Boolean,
    @SerializedName("isFree") val isFree: Boolean
)

data class FrameItem(
    @SerializedName("title") val title: String,
    @SerializedName("name") val name: String, // File name like "frame_valentine_1.png"
    @SerializedName("urlThumb") val urlThumb: String, // Relative URL path for thumbnail
    @SerializedName("urlFrame") val urlFrame: String, // Relative URL path for frame image
    @SerializedName("isPro") val isPro: Boolean? = null // Optional PRO flag for individual items
)

