package com.amb.photo.data.model.remove_object

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RemoveObjRequestBody(
    @SerializedName("tier") val tier : String,
)