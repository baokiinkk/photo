package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import android.util.Log
import com.android.amg.AMGUtil
import com.avnsoft.photoeditor.photocollage.data.local.sharedPref.EditorSharedPref
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.GenAutoDetectResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.GetTokenFirebaseResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.RemoveObjRequestBody
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.ResponseObjAuto
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.TierUtil
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.model.DataEncrypt
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.gson
import com.basesource.base.utils.toJson
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.core.annotation.Single
import java.io.File

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

        val encrypt = AMGUtil.encrypt(
            context,
            json,
            "firebaseToken"
        )

        Log.i("TAG", "requestAiArtaerg: ${encrypt}")
        val data = MultipartBody.Part.createFormData(
            "data", encrypt
        )
        try {

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
        api.genAutoDetect(originFile, data, editorSharedPref.getAccessToken())
        return GenAutoDetectResponse(
            id = "123",
            success = true
        )
    }

    suspend fun getProgress(
        id: String
    ): ResponseObjAuto? {
        api.getProgress(
            id = id,
            token = "tokenApi"
        )
        return ResponseObjAuto(
            message = "success",
            result = emptyList(),
            status_code = 200
        )
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

//        Log.e("getAccessToken", "before encrypt: ----------------- ")

//        val dataEncrypt = DataEncrypt(
//            data = token
//        )
//        val response = safeApiCall<GetTokenFirebaseResponse>(
//            context = context,
//            apiCallMock = { api.getTokenFirebase(dataEncrypt) },
//            apiCall = { api.getTokenFirebase(dataEncrypt) }
//        )
//        when (response) {
//            is Result.Success -> {
//                editorSharedPref.setIsRequestedToken(true)
//                editorSharedPref.saveAccessToken(response.data.token)
//                Log.d("getAccessToken", "after encrypt: ${response.data.token}")
//            }
//
//            else -> {
//
//            }
//        }
    }
}