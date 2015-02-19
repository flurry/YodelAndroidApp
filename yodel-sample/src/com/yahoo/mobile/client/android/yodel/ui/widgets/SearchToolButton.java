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

package com.yahoo.mobile.client.android.yodel.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yahoo.mobile.client.android.yodel.R;
import com.yahoo.mobile.client.android.yodel.utils.AnalyticsHelper;
import com.yahoo.mobile.client.share.search.ui.activity.SearchActivity;

import java.util.HashMap;

public class SearchToolButton extends LinearLayout implements View.OnClickListener {

    private TextView mSearchTermTextView;
    private View mContainer;
    private CharSequence mSearchTerm;

    public SearchToolButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = layoutInflater.inflate(R.layout.view_search_tool_button, this);
        mSearchTermTextView = (TextView)root.findViewById(R.id.search_term_text);
        mContainer = root.findViewById(R.id.search_button_container);

        this.setOnClickListener(this);
    }

    public void setSearchTerm(CharSequence searchTerm) {
        mSearchTerm = searchTerm;
        mSearchTermTextView.setText(Html.fromHtml(getContext().getResources()
                .getString(R.string.search_learn_more_prompt, searchTerm)));
    }

    public CharSequence getSearchTerm() {
        return mSearchTerm;
    }

    @Override
    public void onClick(View v) {
        HashMap<String, String> eventParam = new HashMap<>(1);
        eventParam.put(AnalyticsHelper.PARAM_SEARCH_TERM, getSearchTerm().toString());
        AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_CAR_LEARNMORE_CLICK, eventParam, false);
        
        Intent i = new Intent(getContext(),
                SearchActivity.class);
        i.putExtra(SearchActivity.QUERY_STRING, getSearchTerm());
        i.putExtra(SearchActivity.HEADER_RESOURCE_KEY, R.layout.view_ysearch_header);
        getContext().startActivity(i);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        if (mContainer.getBackground() != null) {
            mContainer.getBackground().setHotspot(x, y);
        }
    }
}
