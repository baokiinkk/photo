package com.avnsoft.photoeditor.photocollage.data.model.ai_enhance

import com.google.gson.annotations.SerializedName

data class AIEnhanceResponse(
    @SerializedName("_id")
    val id: String,
    @SerializedName("success")
    val success: Boolean
)