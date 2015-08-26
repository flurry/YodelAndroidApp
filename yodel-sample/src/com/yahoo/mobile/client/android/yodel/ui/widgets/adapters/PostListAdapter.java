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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.ads.FlurryAdNative;
import com.flurry.android.ads.FlurryAdNativeAsset;
import com.yahoo.mobile.client.android.yodel.R;
import com.yahoo.mobile.client.android.yodel.feed.TumblrFeedManager;
import com.yahoo.mobile.client.android.yodel.utils.AnalyticsHelper;
import com.yahoo.mobile.client.android.yodel.utils.DateTimeUtil;
import com.yahoo.mobile.client.android.yodel.utils.ImageLoader;
import com.tumblr.jumblr.types.LinkPost;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.QuotePost;
import com.tumblr.jumblr.types.TextPost;
import com.tumblr.jumblr.types.VideoPost;

import java.util.List;

public class PostListAdapter extends BaseAdapter {

    private static final String LOG_TAG = PostListAdapter.class.getName();

    private BaseAdAdapter mAdAdapter;
    private Context mContext;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_AD = 1;

    public static final String AD_ASSET_HEADLINE = "headline";
    public static final String AD_ASSET_SUMMARY = "summary";
    public static final String AD_ASSET_SOURCE = "source";
    public static final String AD_ASSET_SEC_HQ_BRANDING_LOGO = "secHqBrandingLogo";
    public static final String AD_ASSET_SEC_HQ_IMAGE = "secHqImage";
    public static final String AD_ASSET_VIDEO = "videoUrl";

    public PostListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mImageLoader = ImageLoader.getInstance();
        this.mContext = context;
        this.mAdAdapter = new BaseAdAdapter(mContext) {
            @Override
            public void onAdCountChanged() {
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        switch (getItemViewType(position)) {
            case VIEW_TYPE_AD:
                AdViewHolder adHolder;
                FlurryAdNative adNative = (FlurryAdNative)getItem(position);
                
                if (convertView == null) {
                    adHolder = new AdViewHolder();

                    convertView = mInflater.inflate(R.layout.list_item_ad_card, parent, false);

                    adHolder.adImage = (ImageView) convertView.findViewById(R.id.ad_image);
                    adHolder.adVideo = (ViewGroup) convertView.findViewById(R.id.ad_video);
                    adHolder.adTitle = (TextView) convertView.findViewById(R.id.ad_title);
                    adHolder.adSummary = (TextView) convertView.findViewById(R.id.ad_summary);
                    adHolder.publisher = (TextView) convertView.findViewById(R.id.ad_publisher);
                    adHolder.sponsoredImage = (ImageView) convertView.findViewById(R.id.sponsored_image);

                    convertView.setTag(adHolder);
                } else {
                    adHolder = (AdViewHolder) convertView.getTag();
                    adHolder.adNative.removeTrackingView(); // Remove the old tracking view
                }

                adHolder.adNative = adNative;
                // Set this convertView as the tracking view so it could open the ad when tapped.
                adNative.setTrackingView(convertView);

                loadAdInView(adHolder, adNative);
                break;
            case VIEW_TYPE_NORMAL:
                PostViewHolder postHolder;

                if (convertView == null) {
                    postHolder = new PostViewHolder();

                    convertView = mInflater.inflate(R.layout.list_item_post_card, parent, false);

                    postHolder.postImage = (ImageView) convertView.findViewById(R.id.post_image);
                    postHolder.postTitle = (TextView) convertView.findViewById(R.id.post_title);
                    postHolder.postSummary = (TextView) convertView.findViewById(R.id.post_summary);
                    postHolder.postDate = (TextView) convertView.findViewById(R.id.date_text);
                    postHolder.publisher = (TextView) convertView.findViewById(R.id.post_publisher);
                    postHolder.retumbleIcon = (ImageView) convertView.findViewById(R.id.icon_reblog);
                    postHolder.likeIcon = (ImageView) convertView.findViewById(R.id.icon_like);

                    convertView.setTag(postHolder);
                } else {
                    postHolder = (PostViewHolder) convertView.getTag();
                }

                loadPostInView(postHolder, (Post) getItem(position));
                break;
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mAdAdapter.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return mAdAdapter.getItem(position);
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (mAdAdapter.canShowAdAtPosition(position)) {
            return VIEW_TYPE_AD;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    public void setBlogPosts(List<Post> blogPosts) {
        mAdAdapter.setData(blogPosts);
        notifyDataSetChanged();
    }

    public void destroy() {
        notifyDataSetInvalidated();
        mAdAdapter.destroy();
    }

    private void loadPostInView(PostViewHolder viewHolder, Post post) {
        viewHolder.postTitle.setText(post.getSourceTitle());

        viewHolder.postImage.setImageBitmap(null);

        String type = post.getType();
        String blogName, title, body, photoUrl, postDate;
        blogName = post.getBlogName();
        title = body = photoUrl = null;
        postDate = DateTimeUtil.getFriendlyDateString(post.getTimestamp() * 1000, mContext);

        switch (type) {
            case TumblrFeedManager.POST_TYPE_TEXT:
                title = ((TextPost)post).getTitle();
                body = ((TextPost)post).getBody();
                break;
            case TumblrFeedManager.POST_TYPE_QUOTE:
                body = ((QuotePost)post).getText() + "\n - " + ((QuotePost)post).getSource();
                break;
            case TumblrFeedManager.POST_TYPE_PHOTO:
                body = ((PhotoPost)post).getCaption();
                // It can't be a photo post without photos. Either way...
                if (((PhotoPost)post).getPhotos().size() > 0) {
                    photoUrl = ((PhotoPost) post).getPhotos().get(0).getOriginalSize().getUrl();
                }
                break;
            case TumblrFeedManager.POST_TYPE_VIDEO:
                body = ((VideoPost)post).getCaption();
                photoUrl = ((VideoPost)post).getThumbnailUrl();
                break;
            case TumblrFeedManager.POST_TYPE_LINK:
                title = ((LinkPost)post).getTitle();
                body = ((LinkPost)post).getDescription() + "\n" + ((LinkPost)post).getLinkUrl();
                break;
        }
        viewHolder.publisher.setText(blogName);
        viewHolder.publisher.setTextColor(mContext.getResources().getColor(R.color.y_blue));

        if (title != null) {
            viewHolder.postTitle.setVisibility(View.VISIBLE);
            viewHolder.postTitle.setText(title);
        } else {
            viewHolder.postTitle.setVisibility(View.GONE);
        }
        if (body != null) {
            viewHolder.postSummary.setVisibility(View.VISIBLE);
            viewHolder.postSummary.setText(body);
        } else {
            viewHolder.postSummary.setVisibility(View.VISIBLE);
        }
        if (photoUrl != null) {
            viewHolder.postImage.setVisibility(View.VISIBLE);
            mImageLoader.displayImage(photoUrl, viewHolder.postImage);
        } else {
            viewHolder.postImage.setVisibility(View.GONE);
        }

        viewHolder.postDate.setText(postDate);

    }

    public void loadAdInView(AdViewHolder viewHolder, FlurryAdNative adNative) {
        try {
            mAdAdapter.loadAdAssetInView(adNative, AD_ASSET_HEADLINE, viewHolder.adTitle);
            mAdAdapter.loadAdAssetInView(adNative, AD_ASSET_SUMMARY, viewHolder.adSummary);
            mAdAdapter.loadAdAssetInView(adNative, AD_ASSET_SOURCE, viewHolder.publisher);

            FlurryAdNativeAsset adNativeAsset = adNative.getAsset(AD_ASSET_SEC_HQ_BRANDING_LOGO);
            mImageLoader.displayImage(adNativeAsset.getValue(), viewHolder.sponsoredImage);
            if (adNative.isVideoAd()) {
                mAdAdapter.loadAdAssetInView(adNative, AD_ASSET_VIDEO, viewHolder.adVideo);
            } else {
                adNativeAsset = adNative.getAsset(AD_ASSET_SEC_HQ_IMAGE);
                mImageLoader.displayImage(adNativeAsset.getValue(), viewHolder.adImage);
            }

        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception in fetching an Ad");
            AnalyticsHelper.logError(LOG_TAG, "Exception in fetching an ad", e);
        }
    }

    public static class PostViewHolder
    {
        ImageView postImage;
        TextView postTitle;
        TextView postSummary;
        TextView postDate;
        TextView publisher;
        ImageView retumbleIcon;
        ImageView likeIcon;
    }
    
    public static class AdViewHolder
    {
        ImageView adImage;
        ViewGroup adVideo;
        TextView adTitle;
        TextView adSummary;
        TextView publisher;
        ImageView sponsoredImage;
        FlurryAdNative adNative;
    }
}
