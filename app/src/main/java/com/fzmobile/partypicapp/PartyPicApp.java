package com.fzmobile.partypicapp;

import android.app.Application;
import android.os.Bundle;
import android.os.StrictMode;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by Facundo on 12/06/2017.
 */

public class PartyPicApp extends Application {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
        AppEventsLogger.activateApp(this);
        TypefaceProvider.registerDefaultIconSets();

    }
}
