package com.avnsoft.photoeditor.photocollage

import android.app.Application
import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.avnsoft.photoeditor.photocollage.di.AppModule
import com.avnsoft.photoeditor.photocollage.di.appLocalModule
import com.basesource.base.di.BaseModule
import com.basesource.base.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class BaseApplication : Application(), KoinComponent {

    companion object {
        private var instance: BaseApplication? = null

        fun getInstanceApp(): BaseApplication {
            return instance as BaseApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        BaseApplication.instance = this
        initKoin(this)
        setupCoil()
    }

    private fun setupCoil() {
        val imageLoader = ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache")) // vị trí cache
                    .maxSizeBytes(512L * 1024 * 1024) // 512 MB
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .respectCacheHeaders(false) // 🔥 quan trọng — bỏ qua no-cache từ server
            .build()
        Coil.setImageLoader(imageLoader)
    }
}

fun initKoin(context: Context) {
    startKoin {
        androidContext(context)
        modules(
            BaseModule().module,
            AppModule().module,
            networkModule,
            appLocalModule
        )
    }
}