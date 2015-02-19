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
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.yahoo.mobile.client.android.yodel.NativeTestAppApplication;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private final MemoryCache memoryCache = new MemoryCache();
    private final ExecutorService executorService;

    private static ImageLoader sInstance = null;

    private ImageLoader() {
        executorService = Executors.newFixedThreadPool(2);
    }

    public static ImageLoader getInstance() {
        synchronized (ImageLoader.class) {
            if (sInstance == null) {
                sInstance = new ImageLoader();
            }
        }

        return sInstance;
    }

    public void displayImage(String url, ImageView imageView) {
        if (TextUtils.isEmpty(url) || imageView == null) {
            return;
        }

        imageView.setTag(url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            setImageViewBitmap(imageView, bitmap);
        } else {
            executorService.submit(new PhotosLoader(url, imageView));
        }
    }

    private Bitmap getBitmap(String url) {
        if (url == null) {
            return null;
        }

        URLConnection conn = null;
        InputStream is = null;

        try {
            final URL imageUrl = new URL(url);
            conn = imageUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            is = conn.getInputStream();
            return BitmapFactory.decodeStream(is, null, null);
        } catch (Exception ex) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // Deliberately empty
                }
            }
            if (conn != null) {
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).disconnect();
                }
            }
        }
    }

    private class PhotosLoader implements Runnable {
        private String mUrl;
        private WeakReference<ImageView> mImageView;

        public PhotosLoader(final String url, final ImageView imageView) {
            mUrl = url;
            mImageView = new WeakReference<>(imageView);
        }

        @Override
        public void run() {
            ImageView imageView = mImageView.get();
            if (imageView == null || !mUrl.equals(imageView.getTag())) {
                return;
            }

            final Bitmap bmp = getBitmap(mUrl);
            if (bmp != null) {
                memoryCache.put(mUrl, bmp);

                final ImageView imageViewToLoad = mImageView.get();
                if (!mUrl.equals(imageView.getTag())) {
                    return;
                }

                NativeTestAppApplication.getInstance().getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        setImageViewBitmap(imageViewToLoad, bmp);
                    }
                });
            }
        }
    }

    private void setImageViewBitmap(ImageView image, Bitmap bitmap) {
        String deviceName = getDeviceName();
        if (!TextUtils.isEmpty(deviceName) && deviceName.toUpperCase().contains("SAMSUNG")) {
            bitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
        }
        image.setImageBitmap(bitmap);
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }
}
