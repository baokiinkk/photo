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

    @GET("mock/patterns")
    suspend fun getPatterns(): Response<ResponseBody>

    @GET("mock/gradients")
    suspend fun getGradients(): Response<ResponseBody>

    @GET("mock/frames")
    suspend fun getFrames(): Response<ResponseBody>

    @Multipart
    @POST("v3/tools/remove-object-auto-detect")
    suspend fun genAutoDetect(
        @Part file: MultipartBody.Part?,
        @Part data: MultipartBody.Part?,
        @Header("Authorization") token: String?
    ): String

    @GET("https://proxy-future-self.footballtv.info/v3/tools/get-by-id/{id}")
    suspend fun getProgress(
        @Path("id") id: String?,
        @Header("Authorization") token: String?
    ): String

    @POST("https://proxy-future-self.footballtv.info/v4/account/get-token")
    suspend fun getTokenFirebase(@Body requestBody: DataEncrypt): String

    @GET("mock_sticker_data")
    suspend fun getStickers(): Response<ResponseBody>

    @GET("mock_template_data")
    suspend fun getTemplates(): Response<ResponseBody>


    @Multipart
    @POST("https://proxy-future-self.footballtv.info/v3/tools/remove-object-manual")
    suspend fun genRemoveObject(
        @Part fileMask: MultipartBody.Part?,
        @Part file: MultipartBody.Part?,
        @Part data: MultipartBody.Part?,
        @Header("Authorization") token: String?
    ): String


    @Multipart
    @POST("https://proxy-future-self.footballtv.info/v3/tools/remove-background")
    suspend fun requestRemoveBg(
        @Part file: MultipartBody.Part?,
        @Part data: MultipartBody.Part?,
        @Header("Authorization") token: String?
    ): String

    @Multipart
    @POST("https://proxy-future-self.footballtv.info/v3/tools/enhance-image")
    suspend fun genAiEnhance(
        @Part file: MultipartBody.Part?,
        @Part data: MultipartBody.Part?,
        @Header("Authorization") token: String?
    ): String
}


