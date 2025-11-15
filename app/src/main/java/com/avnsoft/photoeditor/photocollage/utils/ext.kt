package com.avnsoft.photoeditor.photocollage.utils

import android.content.Intent
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.fromJson
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

inline fun <reified T> Intent.getInput(): T {
    val data = getStringExtra("arg").orEmpty()
    return data.fromJson()
}


inline fun <reified T> Intent.getOutput(): T {
    val data = getStringExtra("EXTRA_SCREEN_OUTPUT_KEY").orEmpty()
    return data.fromJson()
}

@MainThread
inline fun <reified T : ViewModel> BaseActivity.viewModelArg(): Lazy<T> = viewModel<T>(
    parameters = {
        parametersOf(intent.getInput())
    }
)