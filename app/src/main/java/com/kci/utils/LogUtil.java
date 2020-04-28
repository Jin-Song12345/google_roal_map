package com.kci.utils;

import android.content.Context;
import android.widget.Toast;

public class LogUtil {
    public static void showToast(String content, Context context){
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }
}
