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

import android.content.Context;
import android.util.Log;

import com.flurry.android.ads.FlurryAdErrorType;
import com.flurry.android.ads.FlurryAdNative;
import com.flurry.android.ads.FlurryAdNativeListener;
import com.flurry.android.ads.FlurryAdTargeting;
import com.yahoo.mobile.client.android.yodel.utils.AnalyticsHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Handles fetching, caching, and destroying of native gemini ads.</p>
 *
 */
public class NativeAdFetcher {
    private static final String LOG_TAG = NativeAdFetcher.class.getSimpleName();
    /**
     * Maximum number of ads to prefetch.
     */
    private static final int PREFETCHED_ADS_SIZE = 5;
    /**
     * Maximum number of times to try fetch an ad after failed attempts.
     */
    private static final int MAX_FETCH_ATTEMPT = 4;
    private List<AdNativeListener> mAdNativeListeners = new ArrayList<>();
    private List<FlurryAdNative> mPrefetchedAdList = new ArrayList<>();
    private List<FlurryAdNative> mFetchingAdsList = new ArrayList<>();
    private Map<Integer, FlurryAdNative> adMapAtIndex = new HashMap<>();
    private int mNoOfFetchedAds;
    private int mFetchFailCount;
    private WeakReference<Context> mContext = new WeakReference<>(null);
    private FlurryAdNativeListener mAdNativeListener = new FlurryAdNativeListener() {
        @Override
        public synchronized void onFetched(FlurryAdNative adNative) {
            Log.i(LOG_TAG, "onFetched");
            if (canUseThisAd(adNative)) {
                mPrefetchedAdList.add(adNative);
                mNoOfFetchedAds++;
            }
            if (mFetchingAdsList.contains(adNative)) {
                mFetchingAdsList.remove(adNative);
            }
            mFetchFailCount = 0;
            ensurePrefetchAmount();

            notifyObserversOfAdSizeChange();
        }

        @Override
        public void onShowFullscreen(FlurryAdNative adNative) { } // Do nothing

        @Override
        public void onCloseFullscreen(FlurryAdNative adNative) {
            AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_AD_CLOSEBUTTON_CLICK, null, false);
        }

        @Override
        public void onAppExit(FlurryAdNative adNative) { }

        @Override
        public void onClicked(FlurryAdNative adNative) {
            AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_STREAM_AD_CLICK, null, false);
        }

        @Override
        public void onImpressionLogged(FlurryAdNative adNative) { }

        @Override
        public void onError(FlurryAdNative adNative, FlurryAdErrorType adErrorType, int errorCode) {
            if (adErrorType.equals(FlurryAdErrorType.FETCH)) {
                Log.i(LOG_TAG, "onFetchFailed " + errorCode);

                if (mFetchingAdsList.contains(adNative)) {
                    adNative.destroy(); // destroy the native ad, as we are not going to render it.
                    mFetchingAdsList.remove(adNative); // Remove from the tracking list
                }
                mFetchFailCount++;
                ensurePrefetchAmount();
            }
        }
    };

    /**
     * Adds an {@link AdNativeListener} that would be notified for any changes to the native ads
     * list.
     *  
     * @param listener the listener to be notified
     */
    public synchronized void addListener(AdNativeListener listener) {
        mAdNativeListeners.add(listener);
    }

    /**
     * Gets native ad at a particular index in the fetched ads list.
     * 
     * @see #getFetchedAdsCount()
     * @param index the index of ad in the fetched ads list
     * @return the native ad in the list
     */
    public synchronized FlurryAdNative getAdForIndex(final int index) {
        FlurryAdNative adNative = adMapAtIndex.get(index);

        if (adNative == null && mPrefetchedAdList.size() > 0) {
            adNative = mPrefetchedAdList.remove(0);

            if (adNative != null) {
                adMapAtIndex.put(index, adNative);
            }
        }

        ensurePrefetchAmount(); // Make sure we have enough pre-fetched ads

        return adNative;
    }

    /**
     * Gets the number of ads that have been fetched so far.
     *
     * @return the number of ads that have been fetched
     */
    public synchronized int getFetchedAdsCount() {
        return mNoOfFetchedAds;
    }

    /**
     * Fetches a new native ad.
     * @param context the current context.
     *                
     * @see #destroyAllAds()
     */
    public synchronized void prefetchAds(Context context) {
        mContext = new WeakReference<>(context);
        fetchAd();
    }

    /**
     * Destroys ads that have been fetched, that are still being fetched and removes all resource
     * references that this instance still has. This should only be called when the Activity that
     * is showing ads is closing, preferably from the {@link android.app.Activity#onDestroy()}.
     * </p>
     * The converse of this call is {@link #prefetchAds(android.content.Context)}.
     */
    public synchronized void destroyAllAds() {
        for (FlurryAdNative flurryAdNative : adMapAtIndex.values()) {
            flurryAdNative.destroy();
        }

        mFetchFailCount = 0;
        adMapAtIndex.clear();

        for (FlurryAdNative ad : mPrefetchedAdList) {
            ad.destroy();
        }
        mPrefetchedAdList.clear();
        mNoOfFetchedAds = 0;

        for (FlurryAdNative ad : mFetchingAdsList) {
            ad.destroy();
        }
        mFetchingAdsList.clear();

        Log.i(LOG_TAG, "destroyAllAds adList " + adMapAtIndex.size() + " prefetched " +
                mPrefetchedAdList.size() + " fetched " + mFetchingAdsList.size());

        mContext.clear();

        notifyObserversOfAdSizeChange();
    }

    /**
     *  Notifies all registered {@link AdNativeListener} of a change in ad data count.
     */
    private void notifyObserversOfAdSizeChange() {
        for (AdNativeListener listener : mAdNativeListeners) {
            listener.onAdCountChanged();
        }
    }

    /**
     * Fetches a new native ad.
     */
    private synchronized void fetchAd() {
        Context context = mContext.get();

        if (context != null) {
            Log.i(LOG_TAG, "Fetching Ad now");
            FlurryAdNative nativeAd = new FlurryAdNative(
                    context, NativeTestAppApplication.FLURRY_ADSPACE);
            nativeAd.setListener(mAdNativeListener);
            mFetchingAdsList.add(nativeAd);
            nativeAd.fetchAd();
        } else {
            mFetchFailCount++;
            Log.i(LOG_TAG, "Context is null, not fetching Ad");
        }
    }

    /**
     * Ensures that the necessary amount of prefetched native ads are available.
     */
    private synchronized void ensurePrefetchAmount() {
        if (mPrefetchedAdList.size() < PREFETCHED_ADS_SIZE &&
                (mFetchFailCount < MAX_FETCH_ATTEMPT)) {
            fetchAd();
        }
    }

    /**
     * Determines if the native ad can be used.
     *
     * @param adNative  the native ad object
     * @return <code>true</code> if the ad object can be used, false otherwise
     */
    private boolean canUseThisAd(FlurryAdNative adNative) {
        return adNative != null && adNative.isReady() && !adNative.isExpired();
    }

    /**
     * Listener that is notified when changes to the list of fetched native ads are made.
     */
    public interface AdNativeListener {
        /**
         * Triggered when the number of ads have changed. Adapters that implement this class
         * should notify their data views that the dataset has changed.
         */
        void onAdCountChanged();
    }
}
