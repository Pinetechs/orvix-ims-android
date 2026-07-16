package com.pinetechs.orvix.ims.android;

import android.app.Application;
import android.content.Context;

import com.pinetechs.orvix.ims.android.core.util.LocaleHelper;

public class OrvixApplication extends Application {

    private static OrvixApplication instance;

    public static OrvixApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
