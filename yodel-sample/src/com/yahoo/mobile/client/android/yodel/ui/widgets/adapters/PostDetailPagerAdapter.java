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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.flurry.android.ads.FlurryAdNative;
import com.yahoo.mobile.client.android.yodel.ui.PostDetailFragment;
import com.tumblr.jumblr.types.Post;

import java.util.List;

public class PostDetailPagerAdapter extends FragmentStatePagerAdapter {
    private BaseAdAdapter mAdAdapter;

    public PostDetailPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mAdAdapter = new BaseAdAdapter(context) {
            @Override
            public void onAdCountChanged() {
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public Fragment getItem(int position) {
        Object listItemObject = mAdAdapter.getItem(position);
        if (listItemObject instanceof FlurryAdNative) {
            return PostDetailFragment.newInstance((FlurryAdNative)listItemObject);
        } else {
            return PostDetailFragment.newInstance((Post)listItemObject);
        }
    }

    @Override
    public int getCount() {
        return mAdAdapter.getCount();
    }

    public void setBlogPosts(List<Post> blogPosts) {
        mAdAdapter.setData(blogPosts);
        notifyDataSetChanged();
    }

    public void destroy() {
        mAdAdapter.destroy();
    }
}
