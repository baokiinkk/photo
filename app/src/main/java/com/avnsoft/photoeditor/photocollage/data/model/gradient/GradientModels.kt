package com.avnsoft.photoeditor.photocollage.data.model.gradient

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class GradientResponse(
    @SerializedName("version") val version: String,
    @SerializedName("lastModified") val lastModified: String,
    @SerializedName("data") val data: List<GradientGroup>,
    @SerializedName("urlRoot") val urlRoot: String
)

@Serializable
data class GradientGroup(
    @SerializedName("eventName") val eventName: String? = null,
    @SerializedName("eventId") val eventId: Int? = null,
    @SerializedName("content") val content: List<GradientItem>? = null,
    @SerializedName("urlZip") val urlZip: String? = null,
    @SerializedName("isPro") val isPro: Boolean? = null,
    @SerializedName("isReward") val isReward: Boolean? = null,
    @SerializedName("isFree") val isFree: Boolean? = null
)

@Serializable
data class GradientItem(
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null, // File name like "gradient_sunset.jpg"
    @SerializedName("urlThumb") val urlThumb: String? = null, // Relative URL path
    @SerializedName("colors") val colors: List<String>, // List of color hex codes like ["#FFB347", "#FF6B9D"]
    @SerializedName("isPro") val isPro: Boolean? = null // Optional PRO flag for individual items
)

