package com.avnsoft.photoeditor.photocollage.data.model.remove_object

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RemoveObjRequestBody(
    @SerializedName("tier") val tier : String,
)