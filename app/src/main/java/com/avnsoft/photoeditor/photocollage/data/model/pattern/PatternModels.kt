package com.avnsoft.photoeditor.photocollage.data.model.pattern

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class PatternResponse(
    @SerializedName("version") val version: String,
    @SerializedName("lastModified") val lastModified: String,
    @SerializedName("data") val data: List<PatternGroup>,
    @SerializedName("urlRoot") val urlRoot: String
)

@Serializable
data class PatternGroup(
    @SerializedName("eventName") val eventName: String,
    @SerializedName("eventId") val eventId: Long,
    @SerializedName("content") val content: List<PatternItem>,
    @SerializedName("urlZip") val urlZip: String,
    @SerializedName("isPro") val isPro: Boolean,
    @SerializedName("isReward") val isReward: Boolean,
    @SerializedName("isFree") val isFree: Boolean,
    @SerializedName("isUsed") val isUsed: Boolean,
    @SerializedName("bannerUrl") val bannerUrl: String,
    @SerializedName("urlThumb") val urlThumb: String
)

@Serializable
data class PatternItem(
    @SerializedName("title") val title: String,
    @SerializedName("name") val name: String, // File name like "item_1.jpg", "item_2.png"
    @SerializedName("urlThumb") val urlThumb: String // Relative URL path
)

