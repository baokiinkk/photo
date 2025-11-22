package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import android.util.Log
import com.android.amg.AMGUtil
import com.avnsoft.photoeditor.photocollage.data.local.sharedPref.EditorSharedPref
import com.avnsoft.photoeditor.photocollage.data.model.ai_enhance.AIEnhanceResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_background.RemoveBackgroundResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.GetTokenFirebaseResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.ImageSuccessResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.TierUtil
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.model.DataEncrypt
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.gson
import com.basesource.base.utils.toJson
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.core.annotation.Single
import java.io.File

@Single
class AIEnhanceRepoImpl(
    private val context: Context,
    private val api: CollageApiService,
    private val editorSharedPref: EditorSharedPref
) {

    suspend fun requestAIEnhance(jpegFile: File): AIEnhanceResponse {
        return try {
            val token = "Bearer " + editorSharedPref.getAccessToken()

            // Tạo đối tượng Tier
            val tier = Tier(TierUtil.getTearUser(context).toInt())

            // Chuyển đối tượng Tier thành chuỗi JSON
            val gson = Gson()
            val jsonString = gson.toJson(tier)


            // Mã hóa chuỗi JSON với AMGUtil.encryptFile
            val dataPush: String = AMGUtil.encryptFile(
                context,
                jsonString,
                jpegFile,
                "TokenFirbaseTest"
            )


            // Tạo MultipartBody.Part cho "data"
            // Tạo MultipartBody.Part cho "data"
            val data: MultipartBody.Part = MultipartBody.Part.createFormData("data", dataPush)


            // Tạo RequestBody từ tệp ảnh JPEG
            val requestFile = RequestBody.create(
                "image/jpeg".toMediaTypeOrNull(),
                jpegFile
            )
            // Tạo MultipartBody.Part cho "image_original"
            val fileImg = MultipartBody.Part.createFormData(
                "image_original",
                jpegFile.getName(),
                requestFile
            )

            val response = api.genRemoveBg(
                file = fileImg,
                data = data,
                token = token
            )
            Log.e("", "response server: ${response.toJson()}")
            val cleanResponseBody: String? = response.replace("\"", "")
            val decryptedResponse: String? = AMGUtil.decrypt(context, cleanResponseBody)

            val responsePostAI =
                gson.fromJson(decryptedResponse, AIEnhanceResponse::class.java)
            Log.e("", "onSuccess: $decryptedResponse")
            responsePostAI
        } catch (ex: Exception) {
            when (ex) {
                is retrofit2.HttpException -> {
                    val codeHTTPException = ex.code()
                    if (codeHTTPException == 401) {
                        getTokenFirebase()
                    }
                }

                else -> {

                }
            }
            throw ex
        }
    }

    suspend fun getTokenFirebase() {
        val token = AMGUtil.getToken(context, "firebaseToken")
        Log.d("getAccessToken", "before encrypt: $token")
        val dataEncrypt = DataEncrypt(
            data = token
        )
        val response = api.getTokenFirebase(dataEncrypt)
        Log.d("getAccessToken", "data from server: $response")
        val cleanResponseBody = response.replace("\"", "")
        val decryptedResponse = AMGUtil.decrypt(context, cleanResponseBody)
        val responseGetToken = decryptedResponse.fromJson<GetTokenFirebaseResponse>()
        Log.d("getAccessToken", "after encrypt: ${responseGetToken.toJson()}")
        editorSharedPref.setIsRequestedToken(true)
        editorSharedPref.saveAccessToken(responseGetToken.token)
    }

    suspend fun getProgressRemoveBg(
        id: String
    ): ImageSuccessResponse {
        try {
            val data = api.getProgress(
                id = id,
                token = "Bearer " + editorSharedPref.getAccessToken()
            )

            Log.d("TAG", "onViewReady: $data")
            val cleanResponseBody = data.replace("\"", "")
            val decryptedResponse = AMGUtil.decrypt(context, cleanResponseBody)
            Log.d("TAG", "Decrypted response getProgress: $decryptedResponse")
            if (decryptedResponse.equals("null")) {
                throw Exception("Data is null")
            }
            val responsePostAI = gson.fromJson(
                decryptedResponse,
                ImageSuccessResponse::class.java
            )
            return responsePostAI
        } catch (ex: Exception) {
            when (ex) {
                is retrofit2.HttpException -> {
                    val codeHTTPException = ex.code()
                    if (codeHTTPException == 401) {
                        getTokenFirebase()
                    }
                }

                else -> {

                }
            }
            throw ex
        }

    }
}