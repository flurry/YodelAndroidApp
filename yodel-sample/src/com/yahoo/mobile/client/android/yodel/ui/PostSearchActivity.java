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

package com.yahoo.mobile.client.android.yodel.ui;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;

import com.google.gson.Gson;
import com.yahoo.mobile.client.android.yodel.R;

import com.tumblr.jumblr.types.Post;
import com.yahoo.mobile.client.android.yodel.utils.AnalyticsHelper;

import java.util.HashMap;

public class PostSearchActivity extends ActionBarActivity implements PostListFragment.Callbacks {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);

            HashMap<String, String> eventParam = new HashMap<>(1);
            eventParam.put(AnalyticsHelper.PARAM_SEARCH_TERM, searchQuery);
            AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_SEARCH_STARTED, eventParam, false);

            doTumblrTagSearch(searchQuery);
            setTitle(Html.fromHtml(getString(R.string.title_search_query, searchQuery)));
        }
    }

    private void doTumblrTagSearch(String searchQuery) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, PostListFragment.newInstance(searchQuery))
                .commit();
    }

    @Override
    public void onPostSelected(Post post, int positionId, View clickedView) {
        // Log the event
        HashMap<String, String> eventParams = new HashMap<>(2);
        eventParams.put(AnalyticsHelper.PARAM_ARTICLE_ORIGIN, post.getBlogName());
        eventParams.put(AnalyticsHelper.PARAM_ARTICLE_TYPE, post.getType());
        AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_SEARCH_RESULT_CLICK, eventParams, false);
        
        /*
         To avoid a StackOverflowError from a circular reference during serialization, set the
         client of the post to null.
         */
        post.setClient(null);
        
        String postJson = new Gson().toJson(post);
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_SINGLE_POST, postJson);
        intent.putExtra(PostDetailActivity.EXTRA_SINGLE_POST_TYPE, post.getType());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View sharedImageElement = clickedView.findViewById(R.id.post_image);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(this, sharedImageElement, "post_image");
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }
}
