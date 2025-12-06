package com.avnsoft.photoeditor.photocollage.data.model.pattern

import com.basesource.base.ui.base.IScreenData
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PatternModel(
    @SerializedName("eventId")
    val eventId: Long,
    @SerializedName("urlThumb")
    val urlThumb: String?,
    @SerializedName("tabName")
    val tabName: String,
    @SerializedName("content")
    val content: List<PatternContentModel>,
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

@Serializable
data class PatternContentModel(
    @SerializedName("title")
    val title: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("urlThumb")
    val urlThumb: String,
)
