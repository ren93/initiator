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
@AppInit(priority = 12, delay = 3440)
public class ShareInit implements IAppInit {


    @Override
    public void init(Application application) {
        Log.d("init==", "ShareInit");
    }
}
