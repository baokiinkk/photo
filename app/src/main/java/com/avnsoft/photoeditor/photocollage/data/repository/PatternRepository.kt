package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternGroup
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternResponse
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.map
import com.basesource.base.network.safeApiCall
import com.basesource.base.result.Result
import org.koin.core.annotation.Single

data class PatternsResult(
    val groups: List<PatternGroup>,
    val urlRoot: String
)

@Single
class PatternRepository(
    private val context: Context,
    private val api: CollageApiService
) {
    suspend fun getPatterns(): Result<PatternsResult> {
        return safeApiCall<PatternResponse>(
            context = context,
            apiCallMock = { api.getPatterns() },
            apiCall = { api.getPatterns() }
        ).map { resp -> 
            PatternsResult(
                groups = resp.data,
                urlRoot = resp.urlRoot
            )
        }
    }
}

