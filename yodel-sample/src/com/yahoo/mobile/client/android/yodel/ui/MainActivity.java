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

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.yahoo.mobile.client.android.yodel.R;
import com.tumblr.jumblr.types.Post;
import com.yahoo.mobile.client.android.yodel.utils.AnalyticsHelper;

import java.util.HashMap;

public class MainActivity extends ActionBarActivity implements PostListFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, PostListFragment.newInstance())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Uncomment the line below if targeting any API level less than API 14
        // FlurryAgent.onStartSession(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // HTTP response cache
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }

        // Uncomment the line below if targeting any API level less than API 14
        // FlurryAgent.onEndSession(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager)getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(
                    new ComponentName(this, PostSearchActivity.class)));
            searchView.setQueryRefinementEnabled(true);
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnalyticsHelper.logEvent(
                            AnalyticsHelper.EVENT_STREAM_SEARCH_CLICK, null, false);
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPostSelected(Post post, int positionId, View clickedView) {
        // Log the event
        HashMap<String, String> eventParams = new HashMap<>(2);
        eventParams.put(AnalyticsHelper.PARAM_ARTICLE_ORIGIN, post.getBlogName());
        eventParams.put(AnalyticsHelper.PARAM_ARTICLE_TYPE, post.getType());
        AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_STREAM_ARTICLE_CLICK, eventParams, false);

        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_CURRENT_PAGE_INDEX, positionId);
        startActivity(intent);
    }


}
