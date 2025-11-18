package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import com.avnsoft.photoeditor.photocollage.data.local.room.AppDataDao
import com.avnsoft.photoeditor.photocollage.data.local.sharedPref.EditorSharedPref
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateContentModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateContentRoom
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateResponse
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateRoomModel
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.safeApiCall
import com.basesource.base.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class TemplateRepoImpl(
    private val context: Context,
    private val api: CollageApiService,
    private val appDataDao: AppDataDao,
    private val editorSharedPref: EditorSharedPref
) {
    suspend fun syncTemplates() {
        if (editorSharedPref.getIsSyncTemplate()) return
        val response = safeApiCall<TemplateResponse>(
            context = context,
            apiCallMock = { api.getTemplates() },
            apiCall = { api.getTemplates() }
        )
        when (response) {
            is Result.Success -> {
                val data = response.data.data.mapIndexed { index, model ->
                    val contents = model.content.map { item ->
                        val urlThumb = if (index == 0 || index == 1) {
                            "file:///android_asset/${item.urlThumb}"
                        } else {
                            "${response.data.urlRoot}${item.urlThumb}"
                        }
                        TemplateContentRoom(
                            title = item.title,
                            name = item.name,
                            urlThumb = urlThumb
                        )
                    }

                    TemplateRoomModel(
                        eventId = model.eventId,
                        eventName = model.eventName,
                        urlThumb = contents.first().urlThumb,
                        content = contents,
                        timeCreate = System.currentTimeMillis().toString(),
                        isUsed = model.isUsed,
                        bannerUrl = model.bannerUrl,
                    )
                }

                appDataDao.insertAllTemplate(data)
                editorSharedPref.setIsSyncTemplate(true)
            }

            else -> {

            }
        }
    }

    suspend fun getTemplates(): Result<List<TemplateModel>> {
        if (!editorSharedPref.getIsSyncTemplate()) {
            syncTemplates()
        }
        val response = appDataDao.getTemplates()
        val data = response.mapIndexed { index, model ->
            val contents = model.content.map { item ->
                TemplateContentModel(
                    title = item.title,
                    name = item.name,
                    urlThumb = item.urlThumb
                )
            }
            TemplateModel(
                eventId = model.eventId,
                urlThumb = model.urlThumb,
                content = contents,
                isUsed = model.isUsed,
                tabName = model.eventName,
                total = contents.size.toString(),
                bannerUrl = model.bannerUrl
            )
        }.filter {
            it.isUsed
        }
        return Result.Success(data)
    }

    suspend fun getPreviewTemplates(): Flow<List<TemplateModel>> {
        if (!editorSharedPref.getIsSyncTemplate()) {
            syncTemplates()
        }
        val response = appDataDao.getPreviewTemplates()

        val data = response.map { models ->
            models.map { model ->
                val contents = model.content.map { item ->
                    TemplateContentModel(
                        title = item.title,
                        name = item.name,
                        urlThumb = item.urlThumb
                    )
                }
                TemplateModel(
                    eventId = model.eventId,
                    urlThumb = model.urlThumb,
                    content = contents,
                    isUsed = model.isUsed,
                    tabName = model.eventName,
                    total = contents.size.toString(),
                    bannerUrl = model.bannerUrl
                )
            }
        }
        return data
    }

    suspend fun updateIsUsedById(eventId: Long, isUsed: Boolean): Boolean {
        appDataDao.updateIsUsedTemplateById(eventId, isUsed)
        return true
    }
}