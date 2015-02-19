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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.ads.FlurryAdNative;
import com.flurry.android.ads.FlurryAdNativeAsset;
import com.yahoo.mobile.client.android.yodel.R;
import com.yahoo.mobile.client.android.yodel.feed.TumblrFeedManager;
import com.yahoo.mobile.client.android.yodel.utils.AnalyticsHelper;
import com.yahoo.mobile.client.android.yodel.utils.DateTimeUtil;
import com.yahoo.mobile.client.android.yodel.utils.ImageLoader;
import com.yahoo.mobile.client.android.yodel.ui.widgets.SearchToolButton;
import com.tumblr.jumblr.types.LinkPost;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.QuotePost;
import com.tumblr.jumblr.types.TextPost;
import com.tumblr.jumblr.types.VideoPost;

import java.util.HashMap;
import java.util.List;

/**
 * @author ugo
 */
public class PostDetailFragment extends Fragment {

    private Post mPost;
    private FlurryAdNative mAdNative;
    private ImageLoader mImageLoader;
    private Callback mCallbackHandler;

    private TextView mPublisherTextView;
    private TextView mTitleTextView;
    private TextView mBodyTextView;
    private TextView mTagsTextView;
    private TextView mDateTextView;
    private ImageView mSponsoredImage;
    private ImageView mPostImageView;
    private ImageView mImagePlayButton;
    private SearchToolButton mSearchToolButton;
    private View mMediaContainer;
    private View mDividerLine;

    public static final String AD_ASSET_HEADLINE = "headline";
    public static final String AD_ASSET_SUMMARY = "summary";
    public static final String AD_ASSET_SOURCE = "source";
    public static final String AD_ASSET_SEC_HQ_BRANDING_LOGO = "secHqBrandingLogo";
    public static final String AD_ASSET_SEC_HQ_IMAGE = "secHqImage";

    public static PostDetailFragment newInstance(Post post) {
        PostDetailFragment fragment = new PostDetailFragment();
        fragment.mPost = post;
        return fragment;
    }

    public static PostDetailFragment newInstance(FlurryAdNative adNative) {
        PostDetailFragment fragment = new PostDetailFragment();
        fragment.mAdNative = adNative;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_detail, container, false);

        mPublisherTextView = (TextView)rootView.findViewById(R.id.post_publisher);
        mTitleTextView = (TextView)rootView.findViewById(R.id.post_title);
        mBodyTextView = (TextView)rootView.findViewById(R.id.post_body);
        mTagsTextView = (TextView)rootView.findViewById(R.id.post_tags);
        mDateTextView = (TextView)rootView.findViewById(R.id.date_text);
        mSponsoredImage = (ImageView)rootView.findViewById(R.id.sponsored_image);
        mPostImageView = (ImageView)rootView.findViewById(R.id.post_image);
        mImagePlayButton = (ImageButton)rootView.findViewById(R.id.image_play_button);
        mSearchToolButton = (SearchToolButton)rootView.findViewById(R.id.search_tool_button);
        mMediaContainer = rootView.findViewById(R.id.media_container);
        mDividerLine = rootView.findViewById(R.id.divider_line);
        if (mAdNative != null) {
            mAdNative.setTrackingView(rootView);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImageLoader = ImageLoader.getInstance();
        if (mPost != null) {
            loadData();
        } else if (mAdNative != null) {
            loadAd();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof Callback)) {
            throw new IllegalStateException("Activity "
                    + activity.getClass().getSimpleName()
                    + " must implement PostDetailFragment.Callbacks");
        } else {
            mCallbackHandler = (Callback)activity;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                // Log the event
                HashMap<String, String> eventParams = new HashMap<>(2);
                eventParams.put(
                        AnalyticsHelper.PARAM_ARTICLE_ORIGIN, mPost.getBlogName());
                eventParams.put(AnalyticsHelper.PARAM_ARTICLE_TYPE, mPost.getType());
                AnalyticsHelper.logEvent(
                        AnalyticsHelper.EVENT_CAR_MOREIMG_CLICK, eventParams, false);

        }
        return super.onOptionsItemSelected(item);
    }

    private void loadAd() {
        if (mAdNative != null && mAdNative.isReady()) {
            mAdNative.getAsset(AD_ASSET_SOURCE).loadAssetIntoView(mPublisherTextView);
            mAdNative.getAsset(AD_ASSET_HEADLINE).loadAssetIntoView(mTitleTextView);
            mAdNative.getAsset(AD_ASSET_SUMMARY).loadAssetIntoView(mBodyTextView);
            
            FlurryAdNativeAsset adNativeAsset = mAdNative.getAsset(AD_ASSET_SEC_HQ_BRANDING_LOGO);
            mImageLoader.displayImage(adNativeAsset.getValue(), mSponsoredImage);
            adNativeAsset = mAdNative.getAsset(AD_ASSET_SEC_HQ_IMAGE);
            mImageLoader.displayImage(adNativeAsset.getValue(), mPostImageView);
            /*
            The above lines could be replaced with the following. In that case, you would let the 
            Flurry SDK handle ad image management for your app. However, this would prevent on-device
            caching.
            
            mAdNative.getAsset(AD_ASSET_SEC_HQ_BRANDING_LOGO).loadAssetIntoView(mSponsoredImage);
            mAdNative.getAsset(AD_ASSET_SEC_HQ_IMAGE).loadAssetIntoView(mPostImageView);
             */
            
            mDateTextView.setText(R.string.sponsored_text);
            mPublisherTextView.setTextColor(getResources().getColor(R.color.sponsored_text_color));
            mDividerLine.setBackgroundResource(R.color.sponsored_text_color);

            mSearchToolButton.setVisibility(View.GONE);
        }
    }

    private void loadData() {
        if (mPost != null) {
            String type = mPost.getType();
            String blogName, title, body, photoUrl, postDate;
            int photoCount;
            blogName = mPost.getBlogName();
            title = body = photoUrl = null;

            StringBuilder tags = new StringBuilder();
            for (String tag : mPost.getTags()) {
                tags.append("#").append(tag).append(" ");
            }
            postDate = DateTimeUtil.getFriendlyDateString(mPost.getTimestamp() * 1000, getActivity());

            switch (type) {
                case TumblrFeedManager.POST_TYPE_TEXT:
                    title = ((TextPost)mPost).getTitle();
                    body = ((TextPost)mPost).getBody();
                    break;
                case TumblrFeedManager.POST_TYPE_QUOTE:
                    body = ((QuotePost)mPost).getText() + "\n - " + ((QuotePost)mPost).getSource();
                    break;
                case TumblrFeedManager.POST_TYPE_PHOTO:
                    body = ((PhotoPost)mPost).getCaption();
                    // It can't be a photo post without photos. Either way...
                    photoCount = ((PhotoPost)mPost).getPhotos().size();
                    if (photoCount > 0) {
                        photoUrl = ((PhotoPost) mPost).getPhotos().get(0).getOriginalSize().getUrl();
                    }
                    break;
                case TumblrFeedManager.POST_TYPE_VIDEO:
                    body = ((VideoPost)mPost).getCaption();
                    photoUrl = ((VideoPost)mPost).getThumbnailUrl();
                    break;
                case TumblrFeedManager.POST_TYPE_LINK:
                    title = ((LinkPost)mPost).getTitle();
                    body = ((LinkPost)mPost).getDescription() + "\n" + ((LinkPost)mPost).getLinkUrl();
                    break;
            }

            mPublisherTextView.setText(blogName);
            if (title != null) {
                mTitleTextView.setVisibility(View.VISIBLE);
                mTitleTextView.setText(title);
            } else {
                mTitleTextView.setVisibility(View.GONE);
            }
            if (body != null) {
                mBodyTextView.setVisibility(View.VISIBLE);
                mBodyTextView.setText(body);
            } else {
                mBodyTextView.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(tags)) {
                mTagsTextView.setVisibility(View.VISIBLE);
                mTagsTextView.setText(tags);
            }
            mDateTextView.setText(postDate);
            if (photoUrl != null) {
                mMediaContainer.setVisibility(View.VISIBLE);
                mPostImageView.setVisibility(View.VISIBLE);
                mImagePlayButton.setVisibility(View.VISIBLE);
                mImageLoader.displayImage(photoUrl, mPostImageView);
                if (type.equals(TumblrFeedManager.POST_TYPE_PHOTO)) {
                    mImagePlayButton.setImageResource(R.drawable.ic_gallery);
                    mImagePlayButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HashMap<String, String> eventParams = new HashMap<>(2);
                            eventParams.put(
                                    AnalyticsHelper.PARAM_ARTICLE_ORIGIN, mPost.getBlogName());
                            eventParams.put(AnalyticsHelper.PARAM_ARTICLE_TYPE, mPost.getType());
                            AnalyticsHelper.logEvent(
                                    AnalyticsHelper.EVENT_CAR_MOREIMG_CLICK, eventParams, false);
                            
                            mCallbackHandler.onPostImagesSelected(
                                    ((PhotoPost) mPost).getPhotos(), mPostImageView);

                        }
                    });
                } else {
                    mImagePlayButton.setImageResource(R.drawable.ic_video);
                }
            } else {
                mMediaContainer.setVisibility(View.GONE);
                mPostImageView.setVisibility(View.GONE);
                mImagePlayButton.setVisibility(View.GONE);
            }

            if (mPost.getTags().size() > 0) {
                mSearchToolButton.setSearchTerm(mPost.getTags().get(0));
            } else {
                mSearchToolButton.setVisibility(View.GONE);
            }
        }
    }

    public interface Callback {
        void onPostImagesSelected(List<Photo> photosToShow, View clickedImageView);
    }
}
