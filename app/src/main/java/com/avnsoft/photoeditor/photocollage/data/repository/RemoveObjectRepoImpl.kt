package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import android.util.Log
import com.android.amg.AMGUtil
import com.avnsoft.photoeditor.photocollage.data.local.sharedPref.EditorSharedPref
import com.avnsoft.photoeditor.photocollage.data.model.remove_background.AIDetectResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.GenRemoveObjectResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.GetTokenFirebaseResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.ImageSuccessResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.RemoveObjRequestBody
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.ResponseObjAuto
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.TierUtil
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.RemoveBackgroundRequest
import com.basesource.base.network.model.DataEncrypt
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.gson
import com.basesource.base.utils.toJson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.core.annotation.Single
import java.io.File
import kotlin.text.replace

@Single
class RemoveObjectRepoImpl(
    private val context: Context,
    private val api: CollageApiService,
    private val editorSharedPref: EditorSharedPref
) {

    suspend fun genAutoDetect(jpegFile: File): AIDetectResponse {
        return try {
            val token = "Bearer " + editorSharedPref.getAccessToken()

            val tier = Tier(TierUtil.getTearUser(context).toInt())
            val request = RemoveBackgroundRequest(
                tier = tier.tier.toString(),
                type = "1",
                originalName = listOf(jpegFile.getName())
            )
            val jsonStr = gson.toJson(request)
            Log.e("genAutoDetect", "request : $jsonStr")

            val dataPush: String = AMGUtil.encryptFile(
                context,
                jsonStr,
                jpegFile,
                "TokenFirbaseTest"
            )
            val dataEncrypt = DataEncrypt(
                data = dataPush
            )
            Log.e("genAutoDetect", "request to server: ${dataEncrypt.toJson()}")
            val response = api.genAutoDetect(
                data = dataEncrypt,
                token = token
            )
            Log.e("genAutoDetect", "response server: ${response.toJson()}")
            val cleanResponseBody: String = response.replace("\"", "")
            val decryptedResponse: String = AMGUtil.decrypt(context, cleanResponseBody)
            val responsePostAI =
                gson.fromJson(decryptedResponse, AIDetectResponse::class.java)
            Log.e("genAutoDetect", "onSuccess: $decryptedResponse")
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

    suspend fun uploadFileToS3(
        uploadUrl: String,
        file: File,
    ): String {
        Log.d("uploadFileToS3", "uploadFileToS3: $uploadUrl")
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

    suspend fun getImageRemoveObjectStatus(
        id: String,
        status: UPLOAD_TYPE_STATUS
    ): ResponseObjAuto {
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
        val response = api.getImageRemoveObjectStatus(
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
            ResponseObjAuto::class.java
        )
        return responsePostAI
    }

    suspend fun getImageManualStatus(
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
        val response = api.getImageRemoveManualObjectStatus(
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

    suspend fun genRemoveObject(
        fileOrigin: File,
        fileMask: File,
    ): AIDetectResponse {
        return try {
            val token = "Bearer " + editorSharedPref.getAccessToken()

            val tier = Tier(TierUtil.getTearUser(context).toInt())
            val request = RemoveBackgroundRequest(
                tier = tier.tier.toString(),
                type = "1",
                isInpain = 0,
                promptUser = "Long Layered Waves Honey Blonde",
                originalName = listOf(fileOrigin.getName(), fileMask.getName())
            )
            val jsonStr = gson.toJson(request)
            Log.e("genAutoDetect", "request : $jsonStr")

            val dataPush: String = AMGUtil.encryptFile(
                context,
                jsonStr,
                fileOrigin,
                "TokenFirbaseTest"
            )
            val dataEncrypt = DataEncrypt(
                data = dataPush
            )
            Log.e("genAutoDetect", "request to server: ${dataEncrypt.toJson()}")
            val response = api.genRemoveObject(
                data = dataEncrypt,
                token = token
            )
            Log.e("genAutoDetect", "response server: ${response.toJson()}")
            val cleanResponseBody: String = response.replace("\"", "")
            val decryptedResponse: String = AMGUtil.decrypt(context, cleanResponseBody)
            val responsePostAI =
                gson.fromJson(decryptedResponse, AIDetectResponse::class.java)
            Log.e("genAutoDetect", "onSuccess: $decryptedResponse")
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
//        val requestMaskFile = fileMask.asRequestBody("image/jpeg".toMediaTypeOrNull())
//        val maskFile =
//            MultipartBody.Part.createFormData("mask_file", fileMask.name, requestMaskFile)
//
//        val requestFile = fileOrigin.asRequestBody("image/jpeg".toMediaTypeOrNull())
//        val originFile =
//            MultipartBody.Part.createFormData("file", fileOrigin.name, requestFile)
//
//        val json = gson.toJson(
//            RemoveObjRequestBody(
//                tier = TierUtil.getTearUser(context)
//            )
//        )
//
//        val encrypt = AMGUtil.encryptFile(context, json, fileOrigin, "firebaseToken")
//
//        val dataRequest = MultipartBody.Part.createFormData("data", encrypt)
//        Log.e("", "removeObj: ")
//        val data = api.genRemoveObject(
//            maskFile,
//            originFile, dataRequest, "Bearer " + editorSharedPref.getAccessToken()
//        )
//
//        Log.d("TAG", "onViewReady: $data")
//        val cleanResponseBody = data.replace("\"", "")
//        val decryptedResponse = AMGUtil.decrypt(context, cleanResponseBody)
//        Log.d("TAG", "Decrypted response: $decryptedResponse")
//        val responsePostAI =
//            gson.fromJson(decryptedResponse, GenRemoveObjectResponse::class.java)
//        Log.d("TAG", "id response:  ${responsePostAI.id}")
//        return responsePostAI
    }

    suspend fun getProgressRemoveObject(
        id: String
    ): ImageSuccessResponse {
        val data = api.getProgress(
            id = id,
            token = "Bearer " + editorSharedPref.getAccessToken()
        )

        Log.d("TAG", "onViewReady: $data")
        val cleanResponseBody = data.replace("\"", "")
        val decryptedResponse = AMGUtil.decrypt(context, cleanResponseBody)
        Log.d("TAG", "Decrypted response: $decryptedResponse")

//        if (decryptedResponse.equals("null")) {
//            throw Exception("Data is null")
//        }
        val responsePostAI = gson.fromJson(
            decryptedResponse,
            ImageSuccessResponse::class.java
        )

        return responsePostAI
    }
}