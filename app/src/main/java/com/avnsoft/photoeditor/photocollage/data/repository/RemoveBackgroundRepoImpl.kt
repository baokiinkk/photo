package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import android.util.Log
import com.android.amg.AMGUtil
import com.avnsoft.photoeditor.photocollage.data.local.sharedPref.EditorSharedPref
import com.avnsoft.photoeditor.photocollage.data.model.remove_background.RemoveBackgroundResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.GetTokenFirebaseResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.ImageSuccessResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.TierUtil
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.RemoveBackgroundRequest
import com.basesource.base.network.model.DataEncrypt
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.gson
import com.basesource.base.utils.toJson
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.core.annotation.Single
import java.io.File

@Single
class RemoveBackgroundRepoImpl(
    private val context: Context,
    private val api: CollageApiService,
    private val editorSharedPref: EditorSharedPref
) {

    suspend fun requestRemoveBg(jpegFile: File): RemoveBackgroundResponse {
        return try {
            val token = "Bearer " + editorSharedPref.getAccessToken()

            val tier = Tier(TierUtil.getTearUser(context).toInt())
            val request = RemoveBackgroundRequest(
                tier = tier.tier.toString(),
                type = "1",
                originalName = listOf(jpegFile.getName())
            )
            val jsonStr = gson.toJson(request)
            Log.e("requestRemoveBg", "request : $jsonStr")

            val dataPush: String = AMGUtil.encryptFile(
                context,
                jsonStr,
                jpegFile,
                "TokenFirbaseTest"
            )
            val dataEncrypt = DataEncrypt(
                data = dataPush
            )
            Log.e("requestRemoveBg", "request to server: ${dataEncrypt.toJson()}")
            val response = api.requestRemoveBg(
                data = dataEncrypt,
                token = token
            )
            Log.e("requestRemoveBg", "response server: ${response.toJson()}")
            val cleanResponseBody: String = response.replace("\"", "")
            val decryptedResponse: String = AMGUtil.decrypt(context, cleanResponseBody)
            val responsePostAI =
                gson.fromJson(decryptedResponse, RemoveBackgroundResponse::class.java)
            Log.e("requestRemoveBg", "onSuccess: $decryptedResponse")
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

    suspend fun uploadFileToS3(
        uploadUrl: String,
        file: File,
    ): String {
        val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        val response = api.uploadFileToS3(
            uploadUrl = uploadUrl,
            filePart = requestFile
        )
        if (response.isSuccessful) {
            return uploadUrl
        } else {
            throw Exception("Failed to upload file to S3")
        }
    }

    suspend fun getImageStatus(
        id: String,
        status: UPLOAD_TYPE_STATUS
    ): ImageSuccessResponse {
        val request = ImageStatusRequest(
            id = id,
            status = status.value
        )
        Log.d("getImageStatus", "request: ${request.toJson()}")
        val dataPush: String = AMGUtil.encrypt(
            context,
            request.toJson(),
            "TokenFirbaseTest"
        )
        val dataEncrypt = DataEncrypt(
            data = dataPush
        )
        Log.d("getImageStatus", "dataEncrypt: ${dataEncrypt.toJson()}")
        val response = api.getImageStatus(
            data = dataEncrypt,
            token = "Bearer " + editorSharedPref.getAccessToken()
        )
        Log.d("getImageStatus", "response: ${response.toJson()}")
        Log.d("getImageStatus", "onViewReady: $response")
        val cleanResponseBody = response.replace("\"", "")
        val decryptedResponse = AMGUtil.decrypt(context, cleanResponseBody)
        Log.d("getImageStatus", "Decrypted response getProgress: $decryptedResponse")
        val responsePostAI = gson.fromJson(
            decryptedResponse,
            ImageSuccessResponse::class.java
        )
        return responsePostAI
    }
}

data class ImageStatusRequest(
    @SerializedName("_id")
    val id: String,
    @SerializedName("status")
    val status: Int
)

enum class UPLOAD_TYPE_STATUS(val value: Int) {
    SUCCESS(1),
    FAILED(0)
}

data class Tier(
    val tier: Int
)

