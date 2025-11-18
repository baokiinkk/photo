package com.avnsoft.photoeditor.photocollage.ui.activities.collage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageScreen
import com.avnsoft.photoeditor.photocollage.ui.theme.MainTheme
import com.basesource.base.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class CollageActivity : BaseActivity() {
    private val vm: CollageViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = intent.getParcelableArrayListExtra<Uri>(EXTRA_URIS) ?: arrayListOf()
        setContent {
            MainTheme {
                CollageScreen(
                    list,
                    vm,
                    onBack = { finish() },

                    )
            }
        }
    }

    companion object {
        private const val EXTRA_URIS = "extra_uris"
        fun start(context: Context, uris: List<Uri>) {
            val i = Intent(context, CollageActivity::class.java)
            i.putParcelableArrayListExtra(EXTRA_URIS, ArrayList(uris))
            context.startActivity(i)
        }
    }
}

