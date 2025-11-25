package com.avnsoft.photoeditor.photocollage.ui.activities.editor.background

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundLayer
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.toJson
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import kotlin.math.roundToInt

class BackgroundActivity : BaseActivity() {

    private val viewmodel: BackgroundViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.getConfigBackground(
            bitmap = screenInput?.getBitmap(this),
            isBackgroundTransparent = screenInput?.isBackgroundTransparent ?: false
        )
        enableEdgeToEdge()

        setContent {
            Scaffold(
                containerColor = if (screenInput?.isBackgroundTransparent ==true){
                    Color.White
                } else {
                    Color(0xFFF2F4F8)
                }
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                var boxBounds by remember { mutableStateOf<Rect?>(null) }
                val localView = LocalView.current


                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        )
                ) {
                    // Background layer
                    BackgroundLayer(
                        backgroundSelection = uiState.backgroundSelection,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        uiState.originBitmap?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(it.width / it.height.toFloat())
                                        .align(Alignment.Center)
                                        .graphicsLayer()
                                        .clipToBounds()
                                        .onGloballyPositioned { coords ->
                                            val position = coords.positionInRoot()
                                            val size = coords.size
                                            boxBounds = Rect(
                                                position.x.roundToInt(),
                                                position.y.roundToInt(),
                                                (position.x + size.width).roundToInt(),
                                                (position.y + size.height).roundToInt()
                                            )
                                        }
                                ) {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(36.dp))
                        BackgroundSheet(
                            isShowFooter = uiState.backgroundSelection == null,
                            selectedBackgroundSelection = uiState.backgroundSelection,
                            onBackgroundSelect = { _, selection ->
                                viewmodel.updateBackground(selection)
                            },
                            onClose = {
                                finish()
                            },
                            onConfirm = {
                                val intent = Intent()
                                intent.putExtra("backgroundSelection", Json.encodeToString(uiState.backgroundSelection))
                                setResult(RESULT_OK, intent)
                                finish()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                }
            }
        }
    }
}

class RuntimeTypeAdapterFactory<T>(
    private val baseType: Class<T>,
    private val typeFieldName: String
) : TypeAdapterFactory {

    private val labelToSubtype = mutableMapOf<String, Class<out T>>()
    private val subtypeToLabel = mutableMapOf<Class<out T>, String>()

    fun registerSubtype(subtype: Class<out T>, label: String): RuntimeTypeAdapterFactory<T> {
        labelToSubtype[label] = subtype
        subtypeToLabel[subtype] = label
        return this
    }

    override fun <R : Any> create(gson: Gson, type: TypeToken<R>): TypeAdapter<R>? {
        if (type.rawType != baseType) return null

        val jsonElementAdapter = gson.getAdapter(JsonElement::class.java)

        return object : TypeAdapter<R>() {
            override fun write(out: JsonWriter, value: R) {
                val srcType = value!!::class.java as Class<out T>
                val label = subtypeToLabel[srcType]
                    ?: throw JsonParseException("Subtype not registered for $srcType")

                val delegate = gson.getDelegateAdapter(
                    this@RuntimeTypeAdapterFactory,
                    TypeToken.get(srcType)
                ) as TypeAdapter<R>
                val element = delegate.toJsonTree(value)
                val obj = element.asJsonObject
                obj.addProperty(typeFieldName, label)
                jsonElementAdapter.write(out, obj)
            }

            override fun read(reader: JsonReader): R {
                val json = jsonElementAdapter.read(reader).asJsonObject
                val label = json.remove(typeFieldName).asString
                val subtype = labelToSubtype[label]
                    ?: throw JsonParseException("Subtype not registered for label $label")
                val delegate =
                    gson.getDelegateAdapter(this@RuntimeTypeAdapterFactory, TypeToken.get(subtype))
                @Suppress("UNCHECKED_CAST")
                return delegate.fromJsonTree(json) as R
            }
        }
    }
}