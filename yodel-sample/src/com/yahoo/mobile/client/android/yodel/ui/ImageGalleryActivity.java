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

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.yahoo.mobile.client.android.yodel.R;
import com.yahoo.mobile.client.android.yodel.ui.widgets.CaptionViewPagerIndicator;
import com.yahoo.mobile.client.android.yodel.ui.widgets.adapters.GalleryPagerAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tumblr.jumblr.types.Photo;

import java.util.List;

public class ImageGalleryActivity extends ActionBarActivity {
    public static final String EXTRA_PHOTO_LIST = "com.yahoo.mobile.sample.extra.photolist";

    private final static int HIDE_DELAY = 3000; // 3 seconds
    private GalleryPagerAdapter mGalleryPagerAdapter;
    private final Handler mHideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideSystemUi();
        }
    };

    private CaptionViewPagerIndicator mCaptionPagerIndicator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);
        setTitle("");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.black));
        }

        final List<Photo> photos = new Gson().fromJson(getIntent().getStringExtra(EXTRA_PHOTO_LIST),
                new TypeToken<List<Photo>>() {
                }.getType());
        mGalleryPagerAdapter = new GalleryPagerAdapter(getSupportFragmentManager(), photos);
        mCaptionPagerIndicator = (CaptionViewPagerIndicator)findViewById(R.id.caption_pager_indicator);
        mCaptionPagerIndicator.setCurrentItem(0, mGalleryPagerAdapter.getCount());
        mCaptionPagerIndicator.setCaption(photos.get(0).getCaption());

        final ViewPager galleryPager = (ViewPager) findViewById(R.id.image_view_pager);
        galleryPager.setAdapter(mGalleryPagerAdapter);
        galleryPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCaptionPagerIndicator.setCurrentItem(position, mGalleryPagerAdapter.getCount());
                mCaptionPagerIndicator.setCaption(photos.get(position).getCaption());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        // Delay any activity transition until the ViewPager is ready to be drawn
        supportPostponeEnterTransition();
        galleryPager.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        galleryPager.getViewTreeObserver().removeOnPreDrawListener(this);
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                });
        final GestureDetector clickDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        toggleSystemUiVisibility();
                        return true;
                    }
                });

        galleryPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return clickDetector.onTouchEvent(event);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        startUiDelayedHide();
    }

    @Override
    protected void onPause() {
        removePendingHides();

        super.onPause();
    }

    @SuppressLint("NewApi")
    public void hideSystemUi() {

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Animator anim = AnimatorInflater.loadAnimator(this, R.animator.fade_hide);
        anim.setTarget(mCaptionPagerIndicator);
        anim.start();
        
        getSupportActionBar().hide();
    }

    @SuppressLint("NewApi")
    public void showSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // keep status bar hidden
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Animator anim = AnimatorInflater.loadAnimator(this, R.animator.fade_show);
        anim.setTarget(mCaptionPagerIndicator);
        anim.start();

        getSupportActionBar().show();
    }

    protected void toggleSystemUiVisibility() {
        boolean visible = isSystemUiVisible();
        if (visible) {
            hideSystemUi();
        } else {
            showSystemUi();
        }
    }

    protected void startUiDelayedHide() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int flags) {
                        boolean visible = (flags & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
                        if (visible) {
                            delayedHide(HIDE_DELAY);
                        }
                    }
                });
        // If system UI is visible, start hiding it after a short delay
        int visibilityFlags = getWindow().getDecorView().getSystemUiVisibility();
        if ((visibilityFlags & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            delayedHide(HIDE_DELAY / 6);
        }
    }

    protected void removePendingHides() {
        mHideHandler.removeMessages(0);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(null);
        resetSystemUi();
    }

    public void resetSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(0);
    }

    protected boolean isSystemUiVisible() {
        return (getWindow().getDecorView().getSystemUiVisibility()
                & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
    }

    private void delayedHide(int delayMillis) {
        // If less than KitKat, don't worry about setting visibility timer.
        // Not that it won't work, but my current implementation is strange on
        // API < 18
        if (android.os.Build.VERSION.SDK_INT < 18) { return; }

        mHideHandler.removeMessages(0);
        mHideHandler.sendEmptyMessageDelayed(0, delayMillis);
    }
}
