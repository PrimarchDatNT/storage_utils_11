package com.tapon.storageandroid11;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;

import androidx.annotation.Nullable;

public class App extends Application {

    private static App appContext;

    public static App getInstance() {
        return appContext;
    }

    public static ContentResolver getAppProvider() {
        return appContext.getContentResolver();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        appContext = this;
    }

    class D extends Service{

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    class A extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
