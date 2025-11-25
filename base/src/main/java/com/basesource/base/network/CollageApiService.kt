package com.basesource.base.network

import com.basesource.base.network.model.DataEncrypt
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Url

interface CollageApiService {
    @GET("mock/collage_templates")
    suspend fun getCollageTemplates(): Response<ResponseBody>

    @GET("mock/patterns")
    suspend fun getPatterns(): Response<ResponseBody>

    @GET("mock/gradients")
    suspend fun getGradients(): Response<ResponseBody>

    @GET("mock/frames")
    suspend fun getFrames(): Response<ResponseBody>

    @POST("v5/tools/remove-object-auto-detect")
    suspend fun genAutoDetect(
        @Body data: DataEncrypt,
        @Header("Authorization") token: String?
    ): String

    @GET("https://proxy-future-self.footballtv.info/v3/tools/get-by-id/{id}")
    suspend fun getProgress(
        @Path("id") id: String?,
        @Header("Authorization") token: String?
    ): String

    @POST("v5/account/get-token")
    suspend fun getTokenFirebase(@Body requestBody: DataEncrypt): String

    @GET("mock_sticker_data")
    suspend fun getStickers(): Response<ResponseBody>

    @GET("mock_template_data")
    suspend fun getTemplates(): Response<ResponseBody>


    @POST("v5/tools/remove-object-manual")
    suspend fun genRemoveObject(
        @Body data: DataEncrypt,
        @Header("Authorization") token: String?
    ): String


    @POST("v5/tools/remove-background")
    suspend fun requestRemoveBg(
        @Body data: DataEncrypt,
        @Header("Authorization") token: String?
    ): String

    @POST("v5/tools/enhance-image")
    suspend fun genAiEnhance(
        @Body data: DataEncrypt,
        @Header("Authorization") token: String?
    ): String


    @PUT
    suspend fun uploadFileToS3(
        @Url uploadUrl: String,
        @Body filePart: RequestBody,
    ): Response<ResponseBody>

    @POST("v5/tools/remove-background/upload-image-status")
    suspend fun getImageRemoveBackgroundStatus(
        @Body data: DataEncrypt,
        @Header("Authorization") token: String?
    ): String

    @POST("v5/tools/enhance-image/upload-image-status")
    suspend fun getImageAIEnhanceStatus(
        @Body data: DataEncrypt,
        @Header("Authorization") token: String?
    ): String

    @POST("v5/tools/remove-object-auto-detect/upload-image-status")
    suspend fun getImageRemoveObjectStatus(
        @Body data: DataEncrypt,
        @Header("Authorization") token: String?
    ): String

    @POST("v5/tools/remove-object-manual/upload-image-status")
    suspend fun getImageRemoveManualObjectStatus(
        @Body data: DataEncrypt,
        @Header("Authorization") token: String?
    ): String

}


data class RemoveBackgroundRequest(
    @SerializedName("tier")
    val tier: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("original_name")
    val originalName: List<String>,
    @SerializedName("is_inpain")
    val isInpain: Int? = null,
    @SerializedName("prompt_user")
    val promptUser: String? = null
)

