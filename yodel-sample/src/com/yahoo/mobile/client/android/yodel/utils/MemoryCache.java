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

package com.yahoo.mobile.client.android.yodel.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

public class MemoryCache {
    private static String LOG_TAG = MemoryCache.class.getSimpleName();

    private final LruCache<String, Bitmap> mCache;

    public MemoryCache() {
        int cacheSize = 10 * 1024 * 1024 / 1024;  // 10 mB
        Log.i(LOG_TAG, "Image cache size: " + cacheSize + "kB");

        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    public Bitmap get(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        return mCache.get(key);
    }

    public Bitmap put(String key, Bitmap value) {
        if (TextUtils.isEmpty(key) || value == null) {
            return null;
        }

        return mCache.put(key, value);
    }
}
