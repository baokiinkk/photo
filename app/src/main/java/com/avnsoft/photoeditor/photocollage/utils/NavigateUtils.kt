package com.avnsoft.photoeditor.photocollage.utils

import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput.TYPE
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.onboard.OnboardRemoveBackgroundActivity
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.launchActivity

object NavigateUtils {

    fun navigateToOnboardRemoveBackground(
        activity: BaseActivity,
        pathBitmap: String?,
        type: TYPE
    ) {
        activity.launchActivity(
            toActivity = OnboardRemoveBackgroundActivity::class.java,
            input = ToolInput(pathBitmap = pathBitmap, type = type),
        )
    }
}