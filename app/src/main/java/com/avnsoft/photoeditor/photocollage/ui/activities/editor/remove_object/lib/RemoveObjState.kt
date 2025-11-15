package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib

import android.graphics.Bitmap
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed interface RemoveObjState {
    object None : RemoveObjState
    object ScanningObj : RemoveObjState
    object RemovingObj : RemoveObjState
    data class DoneRemoving(val bitmapResult: Bitmap) : RemoveObjState
    object DoneScanning : RemoveObjState

    data class Error(val e: Exception?) : RemoveObjState {

        fun getErrorMessage(): String {
            return when (e) {
                is SocketTimeoutException -> {
                    "SocketTimeoutException"
                }

                is retrofit2.HttpException -> {
                    val codeHTTPException = (e.cause as retrofit2.HttpException).code()
                    when (codeHTTPException) {
                        401 -> {
                            "Unauthorized"
                        }

                        404 -> {
                            "Not Found"
                        }

                        403 -> {
                            "Forbidden"
                        }

                        500 -> {
                            "Internal Server Error"
                        }

                        502 -> {
                            "Bad Gateway"
                        }

                        503 -> {
                            "Service Unavailable"
                        }
                        else -> {
                            "Internal Server Error"
                        }
                    }
                }


                is UnknownHostException, is SocketException -> {
                    "Network"
                }

                else -> {
                    "Internal Server Error"
                }
            }
        }
    }

}