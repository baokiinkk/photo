package com.avnsoft.photoeditor.photocollage.data.model.remove_object

import com.google.gson.annotations.SerializedName

data class GenRemoveObjectResponse(
    @SerializedName("_id")
    val id: String,
    @SerializedName("success")
    val success: Boolean
)