package com.basesource.base.network.model

import com.google.gson.annotations.SerializedName

data class DataEncrypt(
    @SerializedName("data")
    var data: String
)
