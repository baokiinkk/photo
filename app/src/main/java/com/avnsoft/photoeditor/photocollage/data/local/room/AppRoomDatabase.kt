package com.avnsoft.photoeditor.photocollage.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternContentConverter
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternRoomModel
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerConverter
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerRoomModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DatabaseInfo {
    const val DATABASE_NAME = "photo_database_2025.sqlite"
    const val DATABASE_VERSION = 1
}

@Database(
    entities = [
        StickerRoomModel::class,
        PatternRoomModel::class
    ],
    version = DatabaseInfo.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class, StickerConverter::class, PatternContentConverter::class)
abstract class AppRoomDatabase : RoomDatabase() {

    abstract fun appDataDao(): AppDataDao
}

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return Gson().toJson(list ?: emptyList<String>())
    }
}