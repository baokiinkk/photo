package com.amb.photo.data.repository

import android.content.Context
import com.amb.photo.data.model.collage.CollageTemplate
import com.amb.photo.data.model.collage.CollageTemplatesResponse
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.map
import com.basesource.base.network.safeApiCall
import com.basesource.base.result.Result
import org.koin.core.annotation.Single

@Single
class CollageTemplateRepository(
    private val context: Context,
    private val api: CollageApiService
) {
    suspend fun getTemplates(): Result<List<CollageTemplate>> {
        return safeApiCall<CollageTemplatesResponse>(
            context = context,
            apiCallMock = { api.getCollageTemplates() },
            apiCall = { api.getCollageTemplates() }
        ).map { resp -> resp.data.templates }
    }
}

// --- DTOs ---



