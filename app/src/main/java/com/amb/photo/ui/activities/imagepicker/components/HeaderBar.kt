package com.amb.photo.ui.activities.imagepicker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amb.photo.R
import com.amb.photo.ui.theme.AppStyle
import com.amb.photo.ui.theme.Primary500
import com.amb.photo.ui.theme.TextPrimary
import com.basesource.base.components.CustomButton
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun PickerHeaderBar(folderName: String = "Recent", canNext: Boolean, onBack: () -> Unit, onNext: () -> Unit, onFolderClick: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(painter = painterResource(R.drawable.ic_arrow_left), contentDescription = "Back", modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(18.dp))
        Row(Modifier.weight(1f).clickableWithAlphaEffect { onFolderClick() }, verticalAlignment = Alignment.CenterVertically) {
            Text(text = folderName, style = AppStyle.title1().bold().gray900())
            Spacer(Modifier.width(8.dp))
            Icon(painter = painterResource(R.drawable.ic_open_down), contentDescription = "folder")
        }
        if (canNext) {
            Text(
                modifier = Modifier
                    .background(Primary500, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                text = "Next",
                textAlign = TextAlign.Center,
                style = AppStyle.button().semibold().white()
            )
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
