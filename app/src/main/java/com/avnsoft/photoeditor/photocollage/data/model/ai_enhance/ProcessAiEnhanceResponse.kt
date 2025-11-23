package com.avnsoft.photoeditor.photocollage.data.model.ai_enhance

import com.google.gson.annotations.SerializedName


data class ProcessAiEnhanceResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: ResultX,
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class ResultX(
    @SerializedName("origin")
    val origin: String,
    @SerializedName("Ashby")
    val ashby: String,
    @SerializedName("Gingham")
    val gingham: String,
    @SerializedName("Neyu")
    val neyu: String
)