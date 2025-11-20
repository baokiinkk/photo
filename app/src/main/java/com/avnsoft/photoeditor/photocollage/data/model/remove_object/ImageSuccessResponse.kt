package com.avnsoft.photoeditor.photocollage.data.model.remove_object


import com.google.gson.annotations.SerializedName

data class ImageSuccessResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val result: ResultImage,
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class ResultImage(
    @SerializedName("url")
    val url: String
)