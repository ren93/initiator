package com.renny.init;


import android.app.Application;
import android.util.Log;

import com.renny.libcore.AppInit;
import com.renny.mylibrary.IAppInit;

/**
 * Created by renjianan on 2018/7/23.
 * <p>
 * Describe:
 */
@AppInit(priority = 22, delay = 1740, onlyInDebug = true)
public class PushInit implements IAppInit {


    @Override
    public void init(Application application) {
        Log.d("init==", "PushInit");
    }
}
