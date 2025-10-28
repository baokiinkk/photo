package com.basesource.base.network.mock

import android.content.res.AssetManager
import java.io.IOException

object IOUtils {

    fun getJsonStringFromFile(assets: AssetManager, fileName: String): String {
        var json = ""
        try {
            val inputStream = assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return json
    }

}
