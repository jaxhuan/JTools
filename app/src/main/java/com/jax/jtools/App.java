package com.jax.jtools;

import android.app.Application;
import android.content.Context;

/**
 * Created by userdev1 on 3/24/2017.
 */

public class App extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        this.mContext = getApplicationContext();
    }
}
