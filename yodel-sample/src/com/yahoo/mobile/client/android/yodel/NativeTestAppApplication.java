/*
 * Copyright 2015 Yahoo Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yahoo.mobile.client.android.yodel;

import android.app.Application;
import android.net.http.HttpResponseCache;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.yahoo.mobile.client.android.yodel.utils.AnalyticsHelper;
import com.yahoo.mobile.client.share.search.settings.SearchSDKSettings;

import java.io.File;
import java.io.IOException;

public class NativeTestAppApplication extends Application {
    private static String LOG_TAG = NativeTestAppApplication.class.getSimpleName();

    private static final String FLURRY_APIKEY = "JQVT87W7TGN5W7SWY2FH";
    public static final String FLURRY_ADSPACE = "StaticVideoNativeTest";
    public static final String YAHOO_SEARCH_APIKEY = "your_api_key";

    private static NativeTestAppApplication sApplication;

    private Handler mMainThreadHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        // Init Search SDK
        SearchSDKSettings.initializeSearchSDKSettings(
                new SearchSDKSettings.Builder(YAHOO_SEARCH_APIKEY)
                .setVoiceSearchEnabled(true));
        
        sApplication = this;

        // create handlers
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        HandlerThread backgroundHandlerThread = new HandlerThread(LOG_TAG);
        backgroundHandlerThread.start();

        // http response cache
        File httpCacheDir = new File(getCacheDir(), "http");
        long httpCacheSize = 100 * 1024 * 1024; // 100 MiB

        try {
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            AnalyticsHelper.logError(LOG_TAG, "HTTP response cache installation failed", e);
        }

        // Init Flurry
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .withCaptureUncaughtExceptions(true)
                .withContinueSessionMillis(10)
                .withLogEnabled(true)
                .withLogLevel(Log.VERBOSE)

                .build(this, FLURRY_APIKEY);
    }

    public static NativeTestAppApplication getInstance() {
        return sApplication;
    }

    public Handler getMainThreadHandler() {
        return mMainThreadHandler;
    }
}
