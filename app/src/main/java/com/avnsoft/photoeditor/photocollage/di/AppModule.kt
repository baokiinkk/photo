package com.avnsoft.photoeditor.photocollage.di

import android.app.Application
import androidx.room.Room
import com.avnsoft.photoeditor.photocollage.data.local.room.AppDataDao
import com.avnsoft.photoeditor.photocollage.data.local.room.AppRoomDatabase
import com.avnsoft.photoeditor.photocollage.data.local.room.DatabaseInfo
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module

@Module
@ComponentScan("com.avnsoft.photoeditor.photocollage")
class AppModule

val appLocalModule = module {

    single<AppRoomDatabase> {
        provideDatabase(get())
    }

    single<AppDataDao> {
        provideDatabaseDao(get())
    }
}

fun provideDatabaseDao(postDataBase: AppRoomDatabase): AppDataDao = postDataBase.appDataDao()


fun provideDatabase(application: Application): AppRoomDatabase {
    val databaseBuilder = Room.databaseBuilder(
        application,
        AppRoomDatabase::class.java,
        DatabaseInfo.DATABASE_NAME
    )
        .fallbackToDestructiveMigration(true)
    return databaseBuilder.build()
}