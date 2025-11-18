package com.avnsoft.photoeditor.photocollage.ui.activities.store.editor

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextStickerLib
import com.avnsoft.photoeditor.photocollage.ui.activities.store.HeaderStore
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.basesource.base.ui.base.BaseActivity

class EditorStoreActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(
                containerColor = AppColor.White
            ) { inner ->
                Column(
                      modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                          .background(Color.Gray)
                ) {
                    HeaderStore(
                        title = stringResource(R.string.store),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    TextStickerLib()
                }
            }
        }
    }
}