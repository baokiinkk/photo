package com.amb.photo.ui.activities.collage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amb.photo.ui.theme.MainTheme
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.viewmodel.ext.android.viewModel

class CollageActivity : BaseActivity() {
    private val vm: CollageViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = intent.getParcelableArrayListExtra<Uri>(EXTRA_URIS) ?: arrayListOf()
        setContent {
            MainTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CollageScreen(list, vm)
                }
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

@Composable
private fun CollageScreen(uris: List<Uri>, vm: CollageViewModel) {
    var gap by remember { mutableStateOf(6.dp) }
    var corner by remember { mutableStateOf(12.dp) }
    val options by vm.templates.collectAsState()
    val selected by vm.selected.collectAsState()

    LaunchedEffect(Unit) { vm.load(uris.size.coerceAtLeast(1)) }

    Column(Modifier.fillMaxSize()) {
        // Preview khu vực trên
        Box(Modifier
            .weight(1f)
            .padding(16.dp)
            .background(Color.White)) {
            CollagePreview(
                images = uris,
                template = selected ?: CollageTemplates.defaultFor(uris.size.coerceAtLeast(1)),
                gap = gap,
                corner = corner,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Thanh chọn template tối giản
        Column(Modifier.padding(bottom = 16.dp)) {
            Text("Layouts", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            LazyRow(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(options) { tpl ->
                    Box(
                        Modifier
                            .size(88.dp)
                            .background(if (tpl.id == (selected?.id)) Color(0xFFEEE1FF) else Color(0xFFF3F4F6))
                            .padding(6.dp)
                            .clickableWithAlphaEffect { vm.select(tpl) }
                    ) {
                        CollagePreview(
                            images = uris.ifEmpty { listOf() },
                            template = tpl,
                            gap = 2.dp,
                            corner = 6.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}


