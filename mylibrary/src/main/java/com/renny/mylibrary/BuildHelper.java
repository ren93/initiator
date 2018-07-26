package com.renny.mylibrary;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * Created by rjn on 2017/7/19.
 */
public class BuildHelper {
    private static Boolean isDebug;

    public static boolean isDebug(Context context) {
        if (isDebug == null) {
            isDebug = isApkDebug(context);
        }
        return isDebug;
    }


    private static boolean isApkDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
