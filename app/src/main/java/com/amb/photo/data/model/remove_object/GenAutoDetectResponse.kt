package com.amb.photo.data.model.remove_object

import com.google.gson.annotations.SerializedName


data class GenAutoDetectResponse(
    @SerializedName("_id")
    val id: String,
    @SerializedName("success")
    val success: Boolean
)