package com.avnsoft.photoeditor.photocollage.data.model.remove_object

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ResponseObjAuto(
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<ResultObjAuto>,
    @SerializedName("status_code") val status_code: Int
)

@Keep
data class ResultObjAuto(
    @SerializedName("object_type")   val objName: String,
    @SerializedName("box")  val boxRect: List<Int>,
    @SerializedName("url")  val maskImg: String
)