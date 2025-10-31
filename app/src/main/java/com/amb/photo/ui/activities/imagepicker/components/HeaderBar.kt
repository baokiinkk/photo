package com.amb.photo.ui.activities.imagepicker.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amb.photo.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun PickerHeaderBar(folderName: String = "Recent", canNext: Boolean, onBack: () -> Unit, onNext: () -> Unit, onFolderClick: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(painter = painterResource(android.R.drawable.ic_media_previous), contentDescription = "Back")
        }
        Spacer(Modifier.width(4.dp))
        Row(Modifier.weight(1f).clickableWithAlphaEffect { onFolderClick() }, verticalAlignment = Alignment.CenterVertically) {
            Text(text = folderName, style = AppStyle.title2().semibold().black())
            Spacer(Modifier.width(4.dp))
            Icon(painter = painterResource(android.R.drawable.arrow_down_float), contentDescription = "folder")
        }
        if (canNext) {
            Button(onClick = onNext) {
                Text("Next")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PickerHeaderBarPreview() {
    Surface {
        PickerHeaderBar(folderName = "Recent", canNext = true, onBack = {}, onNext = {}, onFolderClick = {})
    }
}
