package com.renny.init

import android.app.Application
import android.util.Log
import com.renny.libcore.AppInit
import com.renny.mylibrary.IAppInit

/**
 * Created by renjianan on 2018/7/26.
 *
 * Describe:
 */
@AppInit(priority = 22, delay = 1740)
class H5Init : IAppInit {
    override fun init(p0: Application?) {
        Log.d("init==", "H5Init")
    }
}