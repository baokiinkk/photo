package com.amb.photo.ui.activities.editor.remove_object

import android.os.Bundle
import com.amb.photo.databinding.ActivityRemoveObjectBinding
import com.amb.photo.ui.activities.editor.crop.ToolInput
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemoveObjectActivity : BaseActivity() {

    private val viewmodel: RemoveObjectViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private val binding by lazy {
        ActivityRemoveObjectBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.setOriginalBitmap(
            bitmap = screenInput?.getBitmap(this),
            newPathBitmap = cacheDir.absolutePath + "/BitmapOriginal_For_remove_obj.jpeg"
        )
        setContentView(binding.root)
    }

}