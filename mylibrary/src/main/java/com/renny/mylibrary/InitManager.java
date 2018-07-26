package com.renny.mylibrary;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by rjn on 2018/7/23.
 */
public class InitManager {

    private static List<InitItem> pathList;

    public static void addPath(String path) {
        try {
            pathList = JSON.parseArray(path, InitItem.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doInit(final Application application) {
        if (application == null)
            return;
        if (pathList == null || pathList.isEmpty())
            return;
        if (pathList.size() > 1) {
            Collections.sort(pathList, new Comparator<InitItem>() {
                @Override
                public int compare(InitItem o1, InitItem o2) {
                    return o2.getPriority() - o1.getPriority();
                }
            });
        }
        HandlerThread handlerThread = new HandlerThread("initiator");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        for (InitItem initItem : pathList) {
            //  Log.d("initiator", "名称：" + initItem.getPath() + " | " + " 优先级：" + initItem.getPriority());
            final IAppInit appInit = newClassInstance(initItem.getPath());
            if (appInit == null) {
                continue;
            }
            Runnable initRunnable = new Runnable() {
                @Override
                public void run() {
                    long startTime = System.currentTimeMillis();
                    appInit.init(application);
                    long endTime = System.currentTimeMillis() - startTime;
                    Log.d("initiator", "初始化" + appInit.getClass().getSimpleName() + "耗时" + endTime + "毫秒");
                }
            };
            if (!BuildHelper.isDebug(application) && initItem.isOnlyInDebug())
                continue;
            if (!initItem.isInChildProcess() && ThreadUtil.isChildProgress(application))
                continue;
            long delay = initItem.getDelay();
            if (initItem.getDelay() > 0) {
                if (initItem.isBackground()) {
                    handler.postDelayed(initRunnable, delay);
                } else {
                    ThreadUtil.postDelayed(initRunnable, delay);
                }
            } else {
                if (initItem.isBackground()) {
                    handler.post(initRunnable);
                } else {
                    ThreadUtil.postMain(initRunnable);
                }
            }

        }
    }


    private static IAppInit newClassInstance(String className) {
        IAppInit result = null;
        try {
            result = (IAppInit) Class.forName(className).getConstructor().newInstance();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }
}
