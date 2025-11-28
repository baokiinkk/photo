package com.avnsoft.photoeditor.photocollage.data.repository

import android.util.Log
import com.avnsoft.photoeditor.photocollage.data.local.room.AppDataDao
import com.avnsoft.photoeditor.photocollage.data.model.image.ImageInfoModel
import com.avnsoft.photoeditor.photocollage.data.model.image.ImageInfoRoomModel
import com.avnsoft.photoeditor.photocollage.ui.activities.mycreate.MyCreateItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class GetImageInfoRepoImpl(
    private val appDataDao: AppDataDao
) {

    fun insertImage(imageUrl: String): Int {
        val imageInfoModel = ImageInfoRoomModel(
            imageUrl = imageUrl,
            createdAt = System.currentTimeMillis()
        )
        return appDataDao.insertImage(imageInfoModel).toInt()
    }

    fun getMyCreates(): Flow<List<MyCreateItem>> {
        return appDataDao.getImages().map { models ->
            models.map {
                MyCreateItem(
                    thumbnailPath = it.imageUrl,
                    id = it.id,
                )
            }
        }
    }

    fun deleteAllImage() {
        appDataDao.deleteAllImage()
    }

    suspend fun deleteImageById(id: Long) {
        appDataDao.deleteImageById(id)
    }

    fun updateImageById(id: Int, imageUrl: String) {
        appDataDao.updateImageById(id, imageUrl)
    }
}