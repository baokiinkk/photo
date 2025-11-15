package com.avnsoft.photoeditor.photocollage.data.model.collage

import com.google.gson.annotations.SerializedName

data class CollageTemplatesResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: CollageData
)

data class CollageData(
    @SerializedName("templates") val templates: List<CollageTemplate>
)