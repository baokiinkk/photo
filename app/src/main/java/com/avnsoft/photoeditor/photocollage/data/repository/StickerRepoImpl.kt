package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import com.avnsoft.photoeditor.photocollage.data.local.room.AppDataDao
import com.avnsoft.photoeditor.photocollage.data.local.sharedPref.EditorSharedPref
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerContentModel
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerContentRoom
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerModel
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerResponse
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerRoomModel
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.safeApiCall
import com.basesource.base.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class StickerRepoImpl(
    private val context: Context,
    private val api: CollageApiService,
    private val appDataDao: AppDataDao,
    private val editorSharedPref: EditorSharedPref
) {

    suspend fun syncStickers() {
        val response = safeApiCall<StickerResponse>(
            context = context,
            apiCallMock = { api.getStickers() },
            apiCall = { api.getStickers() }
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
                        StickerContentRoom(
                            title = item.title,
                            name = item.name,
                            urlThumb = urlThumb
                        )
                    }

                    StickerRoomModel(
                        eventId = model.eventId,
                        eventName = model.eventName,
                        description = model.description,
                        urlThumb = contents.first().urlThumb,
                        content = contents,
                        urlZip = model.urlZip,
                        isPro = model.isPro,
                        isFree = model.isFree,
                        isReward = model.isReward,
                        timeCreate = System.currentTimeMillis().toString(),
                        isUsed = model.isUsed,
                        bannerUrl = model.bannerUrl
                    )
                }

                appDataDao.insertAllSticker(data)
                editorSharedPref.setIsSyncSticker(true)
            }

            else -> {

            }
        }
    }

    suspend fun getStickers(): Result<List<StickerModel>> {
        syncStickers()
        val response = appDataDao.getStickers()
        val data = response.mapIndexed { index, model ->
            val contents = model.content.map { item ->
                StickerContentModel(
                    title = item.title,
                    name = item.name,
                    urlThumb = item.urlThumb
                )
            }
            StickerModel(
                eventId = model.eventId,
                iconTabUrl = model.urlThumb,
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

    suspend fun getPreviewStickers(): Flow<List<StickerModel>> {
        syncStickers()
        val response = appDataDao.getPreviewStickers()

        val data = response.map { models ->
            models.map { model ->
                val contents = model.content.map { item ->
                    StickerContentModel(
                        title = item.title,
                        name = item.name,
                        urlThumb = item.urlThumb
                    )
                }
                StickerModel(
                    eventId = model.eventId,
                    iconTabUrl = model.urlThumb,
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

    suspend fun updateIsUsedById(eventId: Long, isUsed: Boolean): Boolean {
        appDataDao.updateIsUsedStickerById(eventId, isUsed)
        return true
    }

}