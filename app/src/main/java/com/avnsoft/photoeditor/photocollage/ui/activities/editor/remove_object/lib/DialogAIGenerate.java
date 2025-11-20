package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.avnsoft.photoeditor.photocollage.R;


public class DialogAIGenerate extends DialogFragment {
    String content="";


    public void setContent(String content) {
        this.content = content;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_removing);



        TextView textMessage=dialog.findViewById(R.id.textMessage);

        if (content.isEmpty()){
            content=getString(R.string.content_removing_object);
        }

        textMessage.setText(content);

        // Cấu hình cho dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            dialog.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        }

        dialog.setCancelable(false); // Không cho phép đóng dialog khi bấm ra ngoài
        return dialog;
    }



}
