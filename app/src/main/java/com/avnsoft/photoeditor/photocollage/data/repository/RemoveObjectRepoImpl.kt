package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import android.util.Log
import com.android.amg.AMGUtil
import com.avnsoft.photoeditor.photocollage.data.local.sharedPref.EditorSharedPref
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.GenAutoDetectResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.GenRemoveObjectResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.GetTokenFirebaseResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.ImageSuccessResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.RemoveObjRequestBody
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.ResponseObjAuto
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.TierUtil
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.model.DataEncrypt
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.gson
import com.basesource.base.utils.toJson
import com.google.gson.Gson
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
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

    suspend fun genAutoDetect(fileOrigin: File): GenAutoDetectResponse {
        val requestFile = fileOrigin.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val originFile = MultipartBody.Part.createFormData(
            "image_original", fileOrigin.name, requestFile
        )

        val json = gson.toJson(
            RemoveObjRequestBody(
                tier = TierUtil.getTearUser(context)
            )
        )

        val encrypt = AMGUtil.encryptFile(
            context,
            json,
            fileOrigin,
            "firebaseToken"
        )

        Log.i("TAG", "requestAiArtaerg: ${encrypt}")
        val data = MultipartBody.Part.createFormData(
            "data", encrypt
        )
        return try {
            val token = "Bearer " + editorSharedPref.getAccessToken()
            val data = api.genAutoDetect(originFile, data, token)
            val cleanResponseBody = data.replace("\"", "")
            val decryptedResponse = AMGUtil.decrypt(context, cleanResponseBody)
            Log.d("TAG", "Decrypted response genAutoDetect: $decryptedResponse")
            val responsePostAI =
                gson.fromJson(decryptedResponse, GenAutoDetectResponse::class.java)
            Log.d("TAG", "id response:  ${responsePostAI.id}")
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

    suspend fun getProgress(
        id: String
    ): ResponseObjAuto {
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
            ResponseObjAuto::class.java
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
        fileMask: File,
        fileOrigin: File
    ): GenRemoveObjectResponse {
        val requestMaskFile = fileMask.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val maskFile =
            MultipartBody.Part.createFormData("mask_file", fileMask.name, requestMaskFile)

        val requestFile = fileOrigin.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val originFile =
            MultipartBody.Part.createFormData("file", fileOrigin.name, requestFile)

        val json = gson.toJson(
            RemoveObjRequestBody(
                tier = TierUtil.getTearUser(context)
            )
        )

        val encrypt = AMGUtil.encryptFile(context, json, fileOrigin, "firebaseToken")

        val dataRequest = MultipartBody.Part.createFormData("data", encrypt)
        Log.e("", "removeObj: ")
        val data = api.genRemoveObject(
            maskFile,
            originFile, dataRequest, "Bearer " + editorSharedPref.getAccessToken()
        )

        Log.d("TAG", "onViewReady: $data")
        val cleanResponseBody = data.replace("\"", "")
        val decryptedResponse = AMGUtil.decrypt(context, cleanResponseBody)
        Log.d("TAG", "Decrypted response: $decryptedResponse")
        val responsePostAI =
            gson.fromJson(decryptedResponse, GenRemoveObjectResponse::class.java)
        Log.d("TAG", "id response:  ${responsePostAI.id}")
        return responsePostAI
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