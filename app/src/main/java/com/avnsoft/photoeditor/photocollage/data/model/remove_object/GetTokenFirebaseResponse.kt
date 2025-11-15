package com.avnsoft.photoeditor.photocollage.data.model.remove_object

import com.google.gson.annotations.SerializedName


data class GetTokenFirebaseResponse(
    @SerializedName("time")
    val time: Long,
    @SerializedName("token")
    val token: String
)