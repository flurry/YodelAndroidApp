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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yahoo.mobile.client.android.yodel.R;
import com.yahoo.mobile.client.android.yodel.utils.ImageLoader;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;

import java.util.List;

public class PostSearchListAdapter extends BaseAdapter {

    private List<Post> mPosts;
    private Context mContext;

    public PostSearchListAdapter(Context context) {
        super();
        this.mContext = context;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = LayoutInflater
                    .from(mContext).inflate(R.layout.list_item_post_search, parent, false);

            holder.postImage = (ImageView) convertView.findViewById(R.id.post_image);
            holder.publisher = (TextView) convertView.findViewById(R.id.post_publisher);
            holder.postSummary = (TextView) convertView.findViewById(R.id.post_summary);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Post post = mPosts.get(position);
        String type = post.getType();
        String blogName, caption, photoUrl;
        blogName = post.getBlogName();

        // We are only concerned with photo posts for this scenario
        if ("photo".equals(type)) {
            caption = ((PhotoPost)post).getCaption();
            // It can't be a photo post without photos. Either way...
            photoUrl = ((PhotoPost) post).getPhotos().get(0).getOriginalSize().getUrl();

            holder.postSummary.setText(caption);
            ImageLoader.getInstance().displayImage(photoUrl, holder.postImage);
        }

        holder.publisher.setText(blogName);

        return convertView;
    }

    @Override
    public int getCount() {
        return mPosts != null ? mPosts.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mPosts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setBlogPosts(List<Post> blogPosts) {
        mPosts = blogPosts;
        notifyDataSetChanged();
    }

    public static class ViewHolder
    {
        ImageView postImage;
        TextView postSummary;
        TextView publisher;
    }
}
