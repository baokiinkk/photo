package com.basesource.base.network

import com.basesource.base.network.model.DataEncrypt
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface CollageApiService {
    @GET("mock/collage_templates")
    suspend fun getCollageTemplates(): Response<ResponseBody>

    @Multipart
    @POST("v3/tools/remove-object-auto-detect")
    fun genAutoDetect(
        @Part file: MultipartBody.Part?,
        @Part data: MultipartBody.Part?,
        @Header("Authorization") token: String?
    ): Response<ResponseBody>

    @GET("v3/tools/get-by-id/{id}")
    fun getProgress(
        @Path("id") id: String?,
        @Header("Authorization") token: String?
    ): Response<ResponseBody>

    @POST("v4/account/get-token")
    fun getTokenFirebase(@Body requestBody: DataEncrypt): Response<ResponseBody>
}


