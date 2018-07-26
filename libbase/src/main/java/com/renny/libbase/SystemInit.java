package com.renny.libbase;


import android.app.Application;
import android.util.Log;

import com.renny.libcore.AppInit;
import com.renny.mylibrary.IAppInit;

/**
 * Created by renjianan on 2018/7/23.
 * <p>
 * Describe:
 */
@AppInit(background = true, priority = 1, inChildProcess = true, delay = 5000)
public class SystemInit implements IAppInit {

    @Override
    public void init(Application application) {
        Log.d("init==","SystemInit");
    }
}
