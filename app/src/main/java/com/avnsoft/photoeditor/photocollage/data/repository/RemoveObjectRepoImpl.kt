package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import android.util.Log
import com.android.amg.AMGUtil
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.GenAutoDetectResponse
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.RemoveObjRequestBody
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.ResponseObjAuto
import com.avnsoft.photoeditor.photocollage.data.model.remove_object.TierUtil
import com.basesource.base.network.CollageApiService
import com.basesource.base.utils.gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.core.annotation.Single
import java.io.File

@Single
class RemoveObjectRepoImpl(
    private val context: Context,
    private val api: CollageApiService
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

//        val encrypt = encyptCallBack.encryptFile(json, fileOrigin)
        val encrypt = AMGUtil.encrypt(
            context,
            json,
            "firebaseToken"
        );

        Log.i("TAG", "requestAiArtaerg: ${encrypt}")
        val data = MultipartBody.Part.createFormData(
            "data", encrypt
        )
        api.genAutoDetect(originFile, data, "firebaseToken")
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
}