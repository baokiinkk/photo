package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import com.avnsoft.photoeditor.photocollage.data.model.gradient.GradientGroup
import com.avnsoft.photoeditor.photocollage.data.model.gradient.GradientResponse
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.map
import com.basesource.base.network.safeApiCall
import com.basesource.base.result.Result
import org.koin.core.annotation.Single

data class GradientsResult(
    val groups: List<GradientGroup>,
    val urlRoot: String
)

@Single
class GradientRepository(
    private val context: Context,
    private val api: CollageApiService
) {
    suspend fun getGradients(): Result<GradientsResult> {
        return safeApiCall<GradientResponse>(
            context = context,
            apiCallMock = { api.getGradients() },
            apiCall = { api.getGradients() }
        ).map { resp ->
            GradientsResult(
                groups = resp.data,
                urlRoot = resp.urlRoot
            )
        }
    }
}

