package com.example.multiversofit;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtils {

    public static void showCustomToast(Activity activity, String msg) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, activity.findViewById(android.R.id.content), false);

        TextView text = layout.findViewById(R.id.tvMessage);
        text.setText(msg);

        Toast toast = new Toast(activity);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 120);
        toast.setView(layout);
        toast.show();
    }
}
