package com.app.solarcalculator;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

public class App extends MultiDexApplication {

    Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        this.application = this;
    }

    public Application getApplication() {
        return application;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}