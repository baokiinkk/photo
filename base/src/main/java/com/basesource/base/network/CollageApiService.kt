package com.basesource.base.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface CollageApiService {
    @GET("mock/collage_templates")
    suspend fun getCollageTemplates(): Response<ResponseBody>
}


