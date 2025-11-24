package com.avnsoft.photoeditor.photocollage.data.model.remove_background

import com.google.gson.annotations.SerializedName

data class RemoveBackgroundResponse(
    @SerializedName("_id")
    val id: String,
    @SerializedName("link_upload")
    val links: List<String>
)