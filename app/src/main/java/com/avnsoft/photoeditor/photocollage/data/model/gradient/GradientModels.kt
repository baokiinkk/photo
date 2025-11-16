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
    @SerializedName("eventName") val eventName: String,
    @SerializedName("eventId") val eventId: Int,
    @SerializedName("content") val content: List<GradientItem>,
    @SerializedName("urlZip") val urlZip: String,
    @SerializedName("isPro") val isPro: Boolean,
    @SerializedName("isReward") val isReward: Boolean,
    @SerializedName("isFree") val isFree: Boolean
)

@Serializable
data class GradientItem(
    @SerializedName("title") val title: String,
    @SerializedName("name") val name: String, // File name like "gradient_sunset.jpg"
    @SerializedName("urlThumb") val urlThumb: String, // Relative URL path
    @SerializedName("colors") val colors: List<String>, // List of color hex codes like ["#FFB347", "#FF6B9D"]
    @SerializedName("isPro") val isPro: Boolean? = null // Optional PRO flag for individual items
)

