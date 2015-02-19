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
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.yahoo.mobile.client.android.yodel.R;
import com.yahoo.mobile.client.android.yodel.feed.TumblrFeedManager;
import com.yahoo.mobile.client.android.yodel.utils.AnalyticsHelper;
import com.yahoo.mobile.client.android.yodel.utils.PostDataLoader;
import com.yahoo.mobile.client.android.yodel.ui.widgets.adapters.PostDetailPagerAdapter;
import com.google.gson.Gson;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.Post;

import java.util.List;

/**
 * @author ugo
 */
public class PostDetailActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<List<Post>>, PostDetailFragment.Callback {

    private ViewPager mPager;
    private PostDetailPagerAdapter mDetailPageAdapter;
    private ProgressDialog mProgressDialog;

    private int mCurrentIndexPosition;
    private ViewPager.OnPageChangeListener mPageChangeListener =
            new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) { }

                @Override
                public void onPageSelected(int position) {
                    AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_CAR_CONTENT_SWIPE, null, false);
                }

                @Override
                public void onPageScrollStateChanged(int state) { }
            };

    final static String EXTRA_CURRENT_PAGE_INDEX = "com.yahoo.mdc.extra.currentindex";
    final static String EXTRA_SINGLE_POST = "com.yahoo.mdc.extra.singlepost";
    final static String EXTRA_SINGLE_POST_TYPE = "com.yahoo.mdc.extra.singleposttype";
    private final static String STATE_CURRENT_PAGE_INDEX = "com.yahoo.mdc.state.currentindex";
    private final static int LOADER_ID_LOAD_POSTS = 0x1; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPager = (ViewPager)findViewById(R.id.post_view_pager);
        mPager.setPageTransformer(true, new ParallaxPageTransformer(new int[] { R.id.post_image }));
        mPager.setOnPageChangeListener(mPageChangeListener);

        if (savedInstanceState != null) {
            mCurrentIndexPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX);
        }
        else {
            
            if (getIntent().hasExtra(EXTRA_SINGLE_POST)) {
                String postType = getIntent().getStringExtra(EXTRA_SINGLE_POST_TYPE);
                String postJsonString = getIntent().getStringExtra(EXTRA_SINGLE_POST);
                Post post;
                post = TumblrFeedManager.deserializePostJson(postType, postJsonString);
                mPager.setVisibility(View.GONE);
                findViewById(R.id.content_frame).setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, PostDetailFragment.newInstance(post))
                        .commit();
            } else {
                // We want to load multiple posts in the viewpager
                mDetailPageAdapter = new PostDetailPagerAdapter(this, getSupportFragmentManager());
                mPager.setAdapter(mDetailPageAdapter);

                mCurrentIndexPosition = getIntent().getIntExtra(EXTRA_CURRENT_PAGE_INDEX, 0);

                mProgressDialog = new ProgressDialog(PostDetailActivity.this);
                mProgressDialog.setTitle(R.string.fetching_posts);
                mProgressDialog.setIndeterminate(false);

                getSupportLoaderManager().initLoader(LOADER_ID_LOAD_POSTS, null, this);
            }
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

        // Uncomment the line below if targeting any API level less than API 14
        // FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        mDetailPageAdapter.destroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (getIntent().hasExtra(EXTRA_SINGLE_POST)) {
                    /* 
                    This activity was not started from the declared parent
                    Do not go up to the declared parent.
                     */
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition();
                    } else {
                        finish();
                    }
                    return true;
                }
                
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<Post>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID_LOAD_POSTS:
                if (!mProgressDialog.isShowing()) {
                    mProgressDialog.show();
                }
                return new PostDataLoader(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Post>> loader, List<Post> data) {
        switch (loader.getId()) {
            case LOADER_ID_LOAD_POSTS:
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }

                mDetailPageAdapter.setBlogPosts(data);
                mPager.setCurrentItem(mCurrentIndexPosition, false);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Post>> loader) {
        if (loader.getId() == LOADER_ID_LOAD_POSTS) {
            mDetailPageAdapter.setBlogPosts(null);
        }
    }

    @Override
    public void onPostImagesSelected(List<Photo> imagesToShow, View clickedImageView) {
        Intent intent = new Intent(this, ImageGalleryActivity.class);
        // Because Photo objects are not parcelable, we serialize to JSON to pass between activities
        String photoListJson = new Gson().toJson(imagesToShow);
        intent.putExtra(ImageGalleryActivity.EXTRA_PHOTO_LIST, photoListJson);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(this, clickedImageView, "post_image");
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    /**
     * Page transformer that applies a parallax effect to specified views in a ViewPager.
     *
     */
    class ParallaxPageTransformer implements ViewPager.PageTransformer {
        private static final float PARALLAX_COEFFICIENT = -0.3f;
        private final int[] PARALLAX_LAYERS;

        ParallaxPageTransformer(int[] parallaxLayers) {
            this.PARALLAX_LAYERS = parallaxLayers;
        }

        @Override
        public void transformPage(View page, float position) {
            if (PARALLAX_LAYERS != null) {
                float coefficient = page.getWidth() * PARALLAX_COEFFICIENT;

                for (int viewId : PARALLAX_LAYERS) {
                    View v = page.findViewById(viewId);
                    if (v != null) {
                        v.setTranslationX(coefficient * position);
                    }
                }
            }
        }
    }
}
