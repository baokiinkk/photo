package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import com.avnsoft.photoeditor.photocollage.data.model.frame.FrameCategory
import com.avnsoft.photoeditor.photocollage.data.model.frame.FrameResponse
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.map
import com.basesource.base.network.safeApiCall
import com.basesource.base.result.Result
import org.koin.core.annotation.Single

@Single
class FrameRepository(
    private val context: Context,
    private val api: CollageApiService
) {
    suspend fun getFrames(): Result<FrameResponse> {
        return safeApiCall<FrameResponse>(
            context = context,
            apiCallMock = { api.getFrames() },
            apiCall = { api.getFrames() }
        )
    }
}

