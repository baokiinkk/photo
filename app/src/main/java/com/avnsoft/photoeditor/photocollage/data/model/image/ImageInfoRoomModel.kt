package com.avnsoft.photoeditor.photocollage.data.model.image

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_info")
data class ImageInfoRoomModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageUrl: String,
    val createdAt: Long
)


