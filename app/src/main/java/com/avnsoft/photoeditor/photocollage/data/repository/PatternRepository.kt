package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import com.avnsoft.photoeditor.photocollage.data.local.room.AppDataDao
import com.avnsoft.photoeditor.photocollage.data.local.sharedPref.EditorSharedPref
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternContentModel
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternContentRoom
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternGroup
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternModel
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternResponse
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternRoomModel
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerContentModel
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerModel
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.map
import com.basesource.base.network.safeApiCall
import com.basesource.base.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.collections.map

data class PatternsResult(
    val groups: List<PatternGroup>,
    val urlRoot: String
)

@Single
class PatternRepository(
    private val context: Context,
    private val api: CollageApiService,
    private val editorSharedPref: EditorSharedPref,
    private val appDataDao: AppDataDao,
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

    suspend fun getNewPatterns(): Result<List<PatternModel>> {
        syncPatterns()
        val response = appDataDao.getPatterns()
        val data = response.mapIndexed { index, model ->
            val contents = model.content.map { item ->
                PatternContentModel(
                    title = item.title,
                    name = item.name,
                    urlThumb = item.urlThumb
                )
            }
            PatternModel(
                eventId = model.eventId,
                urlThumb = model.urlThumb,
                content = contents,
                isPro = model.isPro,
                isFree = model.isFree,
                isReward = model.isReward,
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


    suspend fun syncPatterns() {
        if (editorSharedPref.getIsSyncPattern()) return

        val response = safeApiCall<PatternResponse>(
            context = context,
            apiCallMock = { api.getPatterns() },
            apiCall = { api.getPatterns() }
        )
        when (response) {
            is Result.Success -> {
                val data = response.data.data.mapIndexed { index, model ->
                    val contents = model.content.map { item ->
                        val urlThumb = if (index == 0) {
                            "file:///android_asset/${item.urlThumb}"
                        } else {
                            "${response.data.urlRoot}${item.urlThumb}"
                        }
                        PatternContentRoom(
                            title = item.title,
                            name = item.name,
                            urlThumb = urlThumb
                        )
                    }
                    PatternRoomModel(
                        eventId = model.eventId,
                        eventName = model.eventName,
                        description = "",
                        urlThumb = contents.first().urlThumb,
                        content = contents,
                        urlZip = model.urlZip,
                        isPro = model.isPro,
                        isFree = model.isFree,
                        isReward = model.isReward,
                        timeCreate = System.currentTimeMillis().toString(),
                        isUsed = model.isUsed,
                        bannerUrl = contents.first().urlThumb
                    )
                }
                appDataDao.insertAllPattern(data)
                editorSharedPref.setIsSyncPattern(true)
            }

            else -> {

            }
        }
    }

    suspend fun getPreviewPatterns(): Flow<List<PatternModel>> {
        syncPatterns()
        val response = appDataDao.getPreviewPatterns()

        val data = response.map { models ->
            models.map { model ->
                val contents = model.content.map { item ->
                    PatternContentModel(
                        title = item.title,
                        name = item.name,
                        urlThumb = item.urlThumb
                    )
                }
                PatternModel(
                    eventId = model.eventId,
                    urlThumb = model.urlThumb,
                    content = contents,
                    isPro = model.isPro,
                    isFree = model.isFree,
                    isReward = model.isReward,
                    isUsed = model.isUsed,
                    tabName = model.eventName,
                    total = contents.size.toString(),
                    bannerUrl = model.bannerUrl
                )
            }
        }
        return data
    }

    suspend fun updateIsUsedPatternById(eventId: Long, isUsed: Boolean): Boolean {
        appDataDao.updateIsUsedPatternById(eventId, isUsed)
        return true
    }
}

