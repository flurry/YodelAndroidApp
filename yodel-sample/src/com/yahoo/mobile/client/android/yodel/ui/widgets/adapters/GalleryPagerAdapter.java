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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.yahoo.mobile.client.android.yodel.ui.FullImageFragment;
import com.tumblr.jumblr.types.Photo;

import java.util.List;

public class GalleryPagerAdapter extends FragmentStatePagerAdapter {

    private List<Photo> mPhotos;

    public GalleryPagerAdapter(FragmentManager fm, List<Photo> photos) {
        super(fm);

        this.mPhotos = photos;
    }

    @Override
    public Fragment getItem(int position) {
        return FullImageFragment.newInstance(mPhotos.get(position).getOriginalSize().getUrl());
    }

    @Override
    public int getCount() {
        return mPhotos != null ? mPhotos.size() : 0;
    }
}
