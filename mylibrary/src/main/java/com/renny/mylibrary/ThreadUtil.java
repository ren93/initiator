package com.renny.mylibrary;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;



public final class ThreadUtil {

    private final static Handler MAIN = new Handler(Looper.getMainLooper());

    public static boolean inMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


    public static void postMain(Runnable runnable) {
        MAIN.post(runnable);
    }

    public static void postDelayed(final Runnable runnable, long delayMillis) {
        MAIN.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable throwable) {
                    Log.e("initiator", "", throwable);
                }
            }
        }, delayMillis);
    }

    public static void removeCallbacks(Runnable runnable) {
        MAIN.removeCallbacks(runnable);
    }


    public static boolean isChildProgress(Context context) {
        boolean isChildThread = false;
        String processName = getProgressName(context);
        String packageName = context.getPackageName();
        isChildThread = !TextUtils.equals(processName, packageName);
        return isChildThread;
    }



    private static String getProgressName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        }
        return null;
    }

}
