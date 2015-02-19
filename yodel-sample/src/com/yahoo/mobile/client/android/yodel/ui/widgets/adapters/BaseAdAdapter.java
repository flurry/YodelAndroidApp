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

package com.yahoo.mobile.client.android.yodel.ui.widgets.adapters;

import android.content.Context;
import android.view.View;

import com.flurry.android.ads.FlurryAdNative;
import com.flurry.android.ads.FlurryAdNativeAsset;
import com.yahoo.mobile.client.android.yodel.NativeAdFetcher;

import java.util.List;

/**
 * Adapter that has common functionality for any adapters that need to show ads in-between
 * other data.
 */
abstract class BaseAdAdapter implements NativeAdFetcher.AdNativeListener {

    private final static int DEFAULT_NO_OF_DATA_BETWEEN_ADS = 3;
    private int mNoOfDataBetweenAds;
    private volatile List mData = null;
    private NativeAdFetcher mAdFetcher;

    protected BaseAdAdapter(Context context) {
        setNoOfDataBetweenAds(DEFAULT_NO_OF_DATA_BETWEEN_ADS); // Default
        this.mAdFetcher = new NativeAdFetcher();
        mAdFetcher.addListener(this);
        // Start prefetching ads
        mAdFetcher.prefetchAds(context);
    }


    @Override
    public abstract void onAdCountChanged();
    
    /**
     * <p>Gets the count of all data, including interspersed ads.</p>
     *
     * <p>If data size is 10 and an ad is to be showed after every 5 items, this method
     * will return 12.</p>
     *
     * @see BaseAdAdapter#setNoOfDataBetweenAds(int)
     * @see BaseAdAdapter#getNoOfDataBetweenAds()
     * @return the total number of items this adapter can show, including ads.
     */
    protected int getCount() {
        if (mData != null) {
            /*
            No of currently fetched ads, as long as it isn't more than no of max ads that can 
            fit dataset.
             */
            int noOfAds = Math.min(mAdFetcher.getFetchedAdsCount(),
                    mData.size() / getNoOfDataBetweenAds());
            return mData.size() > 0 ? mData.size() + noOfAds : 0;
        } else {
            return 0;
        }
    }

    /**
     * Gets the item in a given position in the dataset. If an ad is to be returned,
     * a {@link com.flurry.android.ads.FlurryAdNative} object is returned.
     *
     * @param position the adapter position
     * @return the object or ad contained in this adapter position
     */
    protected Object getItem(int position) {
        if (canShowAdAtPosition(position)) {
            return mAdFetcher.getAdForIndex(getAdIndex(position));
        } else {
            return mData.get(getOriginalContentPosition(position));
        }
    }

    /**
     * Translates an adapter position to an actual position within the underlying dataset.
     *
     * @param position the adapter position
     * @return the original position that the adapter position would have been without ads
     */
    protected int getOriginalContentPosition(int position) {
        int noOfFetchedAds = mAdFetcher.getFetchedAdsCount();
        // No of spaces for ads in the dataset, according to ad placement rules
        int adSpacesCount = position / (getNoOfDataBetweenAds() + 1);
        return position - Math.min(adSpacesCount, noOfFetchedAds);
    }

    /**
     * Determines if an ad can be shown at the given position. Checks if the position is for
     * an ad, using the preconfigured ad positioning rules; and if a native ad object is 
     * available to place in that position.
     *
     * @param position the adapter position
     * @return <code>true</code> if ads can
     */
    protected boolean canShowAdAtPosition(int position) {

        // Is this a valid position for an ad?
        boolean isAdPosition = isAdPosition(position);
        // Is an ad for this position available?
        boolean isAdAvailable = isAdAvailable(position);

        return isAdPosition && isAdAvailable;
    }
    
    /**
     * Destroys all currently fetched ads
     */
    protected void destroy() {
        mAdFetcher.destroyAllAds();
    }

    /**
     * Loads an ad asset into a given view.
     *
     * @param adNative the {@link com.flurry.android.ads.FlurryAdNative} containing the ad asset
     * @param assetName the name of the asset
     * @param view the {@link View} to load the asset into
     */
    protected void loadAdAssetInView(FlurryAdNative adNative, String assetName, View view) {
        FlurryAdNativeAsset adNativeAsset = adNative.getAsset(assetName);
        if (adNativeAsset != null) {
            adNativeAsset.loadAssetIntoView(view);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * Sets the underlying dataset.
     *
     * @param data the dataset
     */
    protected void setData(List data) {
        mData = data;
    }

    /**
     * Gets the number of data items to show between ads.
     *
     * @return the number of data items to show between ads
     */
    protected int getNoOfDataBetweenAds() {
        return mNoOfDataBetweenAds;
    }

    /**
     * Sets the number of data items to show between ads.
     *
     * @param noOfDataBetweenAds the number of data items to show between ads
     */
    protected void setNoOfDataBetweenAds(int noOfDataBetweenAds) {
        mNoOfDataBetweenAds = noOfDataBetweenAds;
    }

    /**
     * Gets the ad index for this adapter position within the list of currently fetched ads.
     *
     * @param position the adapter position
     * @return the index of the ad within the list of fetched ads
     */
    private int getAdIndex(int position) {
        return (position / getNoOfDataBetweenAds()) - 1;
    }
    
    /**
     * Checks if adapter position is an ad position.
     *
     * @param position the adapter position
     * @return {@code true} if an ad position, {@code false} otherwise
     */
    private boolean isAdPosition(int position) {
        return (position + 1) % (getNoOfDataBetweenAds() + 1) == 0;
    }

    /**
     * Checks if an ad is available for this position.
     *
     * @param position the adapter position
     * @return {@code true} if an ad is available, {@code false} otherwise
     */
    private boolean isAdAvailable(int position) {
        int adIndex = getAdIndex(position);
        return position >= getNoOfDataBetweenAds()
                && adIndex >= 0 
                && mAdFetcher.getFetchedAdsCount() > adIndex;
    }
}
