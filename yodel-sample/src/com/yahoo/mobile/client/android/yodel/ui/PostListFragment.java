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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.yahoo.mobile.client.android.yodel.R;
import com.yahoo.mobile.client.android.yodel.ui.widgets.adapters.PostSearchListAdapter;
import com.yahoo.mobile.client.android.yodel.utils.AnalyticsHelper;
import com.yahoo.mobile.client.android.yodel.utils.PostDataLoader;
import com.yahoo.mobile.client.android.yodel.ui.widgets.adapters.PostListAdapter;
import com.tumblr.jumblr.types.Post;
import com.yahoo.mobile.client.share.search.ui.activity.SearchActivity;

import java.util.HashMap;
import java.util.List;

/**
 * The list fragment containing Tumblr blog posts.
 */
public class PostListFragment extends ListFragment 
        implements LoaderManager.LoaderCallbacks<List<Post>> {
    
    private final static String LOG_TAG = PostListFragment.class.getSimpleName();

    private List<Post> mBlogPostList = null;
    private PostListAdapter mPostListAdapter = null;
    private PostSearchListAdapter mPostSearchListAdapter = null;
    private SwipeRefreshLayout mSwipeView = null;
    private AlertDialog mAlertDialog = null;
    private Callbacks mCallbackHandler;
    private String mTagQuery;

    private ProgressDialog mProgressDialog;

    private final static int LOADER_ID_LOAD_RECENT_POSTS = 0x1;
    private final static int LOADER_ID_LOAD_POSTS_WITH_TAGS = 0x2;
    private final static String EXTRA_TAG_QUERY = "com.yahoo.mobile.sample.extra.tagquery";

    static PostListFragment newInstance() {
        return new PostListFragment();
    }

    static PostListFragment newInstance(String tagQuery) {
        PostListFragment fragment = new PostListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TAG_QUERY, tagQuery);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            if (args != null) {
                mTagQuery = args.getString(EXTRA_TAG_QUERY);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mSwipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);

        mSwipeView.setColorSchemeResources(R.color.accent_color, R.color.red_transparent,
                R.color.purple_transparent, R.color.y_blue);
        mSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Log a timed event
                AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_STREAM_PULL_REFRESH, null, true);
                
                mSwipeView.setRefreshing(true);
                refreshPosts();
            }
        });
        // Create a reusable progress dialog
        createProgressDialog();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mTagQuery == null) {
            mPostListAdapter = new PostListAdapter(getActivity());
            setListAdapter(mPostListAdapter);
        } else {
            View footer = getActivity().getLayoutInflater()
                    .inflate(R.layout.view_more_web_footer, getListView(), false);
            mPostSearchListAdapter = new PostSearchListAdapter(getActivity());
            getListView().addFooterView(footer);
            setListAdapter(mPostSearchListAdapter);
        }

        // If blog post list wasn't persisted during config change or is just empty...
        if (mBlogPostList == null || mBlogPostList.size() > 0) {
            refreshPosts();
        }

        /*
        Account for the overlay Toolbar. Warning: makes it hard to re-use this fragment
        (which is the whole point of fragments, isn't it?)
        */
        final TypedArray styledAttributes = getActivity().getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        int mediumViewMargin = getResources().getDimensionPixelSize(R.dimen.view_margin_medium);
        int lvTopPadding = mediumViewMargin +
                actionBarSize;
        getListView().setPadding(0, lvTopPadding, 0, mediumViewMargin);
        mSwipeView.setProgressViewOffset(true, mediumViewMargin, lvTopPadding);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity "
                    + activity.getClass().getSimpleName()
                    + " must implement PostListFragment.Callbacks");
        } else {
            mCallbackHandler = (Callbacks)activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbackHandler = null;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        setListAdapter(null);

        if (mPostListAdapter != null) {
            mPostListAdapter.destroy();
            mPostListAdapter = null;
        }

        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    @Override
    public void onListItemClick(ListView listView, View clickedView, int position, long id) {
        if (mCallbackHandler != null) {
            if (mTagQuery == null) {
                mCallbackHandler.onPostSelected(
                        (Post) mPostListAdapter.getItem(position), position, clickedView);
            } else {
                if (position + 1 > mPostSearchListAdapter.getCount()) {
                    // The search footer was clicked. Log the event and open the Search SDK
                    HashMap<String, String> eventParam = new HashMap<>(1);
                    eventParam.put(AnalyticsHelper.PARAM_SEARCH_TERM, mTagQuery);
                    AnalyticsHelper.logEvent(
                            AnalyticsHelper.EVENT_SEARCH_MOREONWEB_CLICK, eventParam, false);
                    
                    Intent i = new Intent(getActivity(), SearchActivity.class);
                    i.putExtra(SearchActivity.QUERY_STRING, mTagQuery);
                    i.putExtra(SearchActivity.HEADER_RESOURCE_KEY, R.layout.view_ysearch_header);
                    getActivity().startActivity(i);
                } else {
                    mCallbackHandler.onPostSelected(
                            (Post) mPostSearchListAdapter.getItem(position), position, clickedView);
                }
            }
        }
    }

    @Override
    public Loader<List<Post>> onCreateLoader(int id, Bundle args) {
        showProgressDialog();
        switch (id) {
            case LOADER_ID_LOAD_RECENT_POSTS:
                return new PostDataLoader(getActivity());
            case LOADER_ID_LOAD_POSTS_WITH_TAGS:
                return new PostDataLoader(getActivity(), mTagQuery);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Post>> loader, List<Post> data) {
        hideProgressDialog();
        mBlogPostList = data;
        boolean contentLoaded = false;

        switch (loader.getId()) {
            case LOADER_ID_LOAD_RECENT_POSTS:
                if (data != null && data.size() > 0) {
                    contentLoaded = true;
                    mPostListAdapter.setBlogPosts(mBlogPostList);
                } else if (data == null) {
                    // Network is unavailable
                    showAlertDialog();
                }
                break;
            case LOADER_ID_LOAD_POSTS_WITH_TAGS:
                if (data != null && data.size() > 0) {
                    contentLoaded = true;
                    mPostSearchListAdapter.setBlogPosts(mBlogPostList);
                } else if (data == null) {
                    // Network is unavailable
                    showAlertDialog();
                }
                break;
        }
        if (mSwipeView.isRefreshing()) {
            HashMap<String, String> eventParams = new HashMap<>(1);
            eventParams.put(AnalyticsHelper.PARAM_CONTENT_LOADED, String.valueOf(contentLoaded));
            AnalyticsHelper.endTimedEvent(AnalyticsHelper.EVENT_STREAM_PULL_REFRESH, eventParams);
            mSwipeView.setRefreshing(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Post>> loader) {
        switch (loader.getId()) {
            case LOADER_ID_LOAD_RECENT_POSTS:
                if (mPostListAdapter != null) {
                    mPostListAdapter.setBlogPosts(null);
                }
                break;
            case LOADER_ID_LOAD_POSTS_WITH_TAGS:
                if (mPostSearchListAdapter != null) {
                    mPostSearchListAdapter.setBlogPosts(null);
                }
        }
    }

    private void refreshPosts() {
        Log.d(LOG_TAG, "Refreshing post list...");
        if (mTagQuery == null) {
            getActivity().getSupportLoaderManager()
                    .restartLoader(LOADER_ID_LOAD_RECENT_POSTS, null, this);
        } else {
            getActivity().getSupportLoaderManager()
                    .restartLoader(LOADER_ID_LOAD_POSTS_WITH_TAGS, null, this);
        }
    }

    public void createProgressDialog() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(R.string.fetching_posts);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);
    }

    public void showProgressDialog(){
        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void hideProgressDialog(){
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public void showAlertDialog(){
        MainActivity activity =  ((MainActivity) getActivity());
        if (activity == null) {
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setMessage(R.string.network_not_available);
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton(R.string.retry_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        refreshPosts();
                        dialog.dismiss();
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        mAlertDialog = alertDialog.create();
        mAlertDialog.show();
    }

    public interface Callbacks {
        void onPostSelected(Post post, int positionId, View clickedView);
    }
}
