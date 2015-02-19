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

package com.yahoo.mobile.client.android.yodel.feed;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.LinkPost;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.QuotePost;
import com.tumblr.jumblr.types.TextPost;
import com.tumblr.jumblr.types.VideoPost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that manages fetching of Tumblr blog posts.
 */
public final class TumblrFeedManager {
    private final static String TUMBLR_CONSUMER_KEY = "xz2gUnYX7axNd9XsVFIjUyhVElCPxU5pQVpfSV1qZgYxaiz29f";
    private final static String TUMBLR_CONSUMER_SECRET = "DqFvBW162BYUF92jCQgrLBzVDngxKy8TAZ3tDBspSE7mz4tqTw";
    // Only a limited set of the possible Tumblr post types will be supported
    public final static String POST_TYPE_TEXT = "text";
    public final static String POST_TYPE_QUOTE = "quote";
    public final static String POST_TYPE_PHOTO = "photo";
    public final static String POST_TYPE_VIDEO = "video";
    public final static String POST_TYPE_LINK = "link";

    private static JumblrClient sTumblrClient;
    private final static Blog BLOG_FEED_1;
    private final static Blog BLOG_FEED_2;
    private final static Blog BLOG_FEED_3;

    private final static int MAX_POSTS_TO_FETCH = 20;

    static {
        sTumblrClient = new JumblrClient(TUMBLR_CONSUMER_KEY,
                TUMBLR_CONSUMER_SECRET);

        BLOG_FEED_1 = sTumblrClient.blogInfo("archaicwonder.tumblr.com");
        BLOG_FEED_2 = sTumblrClient.blogInfo("yahoo.tumblr.com");
        BLOG_FEED_3 = sTumblrClient.blogInfo("natgeotravel.tumblr.com");
    }

    public static List<Post> getRecentPosts() {
        HashMap<String, String> optionsMap = new HashMap<>(1);
        optionsMap.put("filter", "text");
        optionsMap.put("limit", "20");
        optionsMap.put("reblog_info", "true");
        List<Post> recentPosts = new ArrayList<>(MAX_POSTS_TO_FETCH);
        List<Post> blog1Posts = BLOG_FEED_1.posts(optionsMap);
        List<Post> blog2Posts = BLOG_FEED_2.posts(optionsMap);
        List<Post> blog3Posts = BLOG_FEED_3.posts(optionsMap);
        int blog1Counter, blog2Counter, blog3Counter;
        blog1Counter = blog2Counter = blog3Counter = 0;


        while (recentPosts.size() < 20) {
            Post nextBlogPost;
            if (blog1Posts.get(blog1Counter).getTimestamp() >
                    blog2Posts.get(blog2Counter).getTimestamp() &&
                    blog1Posts.get(blog1Counter).getTimestamp() >
                    blog3Posts.get(blog3Counter).getTimestamp()) {
                // If the next post from blog1 is the latest, add it to list
                nextBlogPost = blog1Posts.get(blog1Counter++);
                /*
                 * Set the post name to a human-readable title instead of the third-level domain name
                 * it is currently set from.
                 */
                nextBlogPost.setBlogName(BLOG_FEED_1.getTitle());
            } else if (blog2Posts.get(blog2Counter).getTimestamp() >
                    blog3Posts.get(blog3Counter).getTimestamp()) {
                // Else, if the next post from blog2 is the latest, add it to list
                nextBlogPost = blog2Posts.get(blog2Counter++);
                nextBlogPost.setBlogName(BLOG_FEED_2.getTitle());
            } else {
                // Else, add the next post from blog3 to the list.
                nextBlogPost = blog3Posts.get(blog3Counter++);
                nextBlogPost.setBlogName(BLOG_FEED_3.getTitle());
            }

            /*
            Remove JumblrClient since we are not doing any write operations.
            This makes serialization easier.
             */
            nextBlogPost.setClient(null);
            recentPosts.add(nextBlogPost);
        }
        return recentPosts;
    }

    public static List<Post> getPostsWithTag(String tag) {
        HashMap<String, String> optionsMap = new HashMap<>(1);
        optionsMap.put("filter", "text");
        optionsMap.put("type", "photo");    // Limit to only photo posts
        optionsMap.put("limit", "5");

        return sTumblrClient.tagged(tag, optionsMap);
    }

    public static Post deserializePostJson(String postType, String postJsonString) {
        Post post;
        switch (postType) {
            case POST_TYPE_PHOTO:
                post = new Gson().fromJson(postJsonString,
                        new TypeToken<PhotoPost>() {
                        }.getType());
                break;
            case POST_TYPE_LINK:
                post = new Gson().fromJson(postJsonString,
                        new TypeToken<LinkPost>() {
                        }.getType());
                break;
            case POST_TYPE_VIDEO:
                post = new Gson().fromJson(postJsonString,
                        new TypeToken<VideoPost>() {
                        }.getType());
                break;
            case POST_TYPE_TEXT:
                post = new Gson().fromJson(postJsonString,
                        new TypeToken<TextPost>() {
                        }.getType());
                break;
            case POST_TYPE_QUOTE:
                post = new Gson().fromJson(postJsonString,
                        new TypeToken<QuotePost>() {
                        }.getType());
                break;
            default:
                post = new Gson().fromJson(postJsonString,
                        new TypeToken<Post>() {
                        }.getType());
        }
        return post;
    }
}
