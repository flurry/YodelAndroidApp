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


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.yahoo.mobile.client.android.yodel.feed.TumblrFeedManager;
import com.tumblr.jumblr.types.Post;

import java.util.List;

public class PostDataLoader extends AsyncTaskLoader<List<Post>> {

    private List<Post> mPostsCache;
    private final String mTagQuery;

    public PostDataLoader(Context context) {
        this(context, null);
    }

    public PostDataLoader(Context context, String tagQuery) {
        super(context);
        this.mTagQuery = tagQuery;
    }

    @Override
    public List<Post> loadInBackground() {
        if (mTagQuery == null) {
            return TumblrFeedManager.getRecentPosts();
        } else {
            return TumblrFeedManager.getPostsWithTag(mTagQuery);
        }
    }

    /**
     * Derived classes must call through to the super class's implementation of this method, kthnx
     */
    @Override
    protected void onStartLoading() {
        if (mPostsCache != null) {
            deliverResult(mPostsCache);
        } else {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(List<Post> data) {
        if (isReset()) {
            mPostsCache = null;
            return;
        }

        mPostsCache = data;
        super.deliverResult(data);
    }
}
