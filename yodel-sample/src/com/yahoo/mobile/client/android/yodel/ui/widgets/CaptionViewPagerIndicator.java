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

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yahoo.mobile.client.android.yodel.R;

public class CaptionViewPagerIndicator extends LinearLayout {

    private TextView mCaptionTextView;
    private TextView mPageIndicatorTextView;

    public CaptionViewPagerIndicator(Context context) {
        this(context, null);
    }

    public CaptionViewPagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public CaptionViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = layoutInflater.inflate(R.layout.view_caption_pager_indicator, this);
        mCaptionTextView = (TextView)root.findViewById(R.id.image_caption_text);
        mPageIndicatorTextView = (TextView)root.findViewById(R.id.page_indicator_text);
    }

    public void setCurrentItem(int itemIndex, int total) {
        mPageIndicatorTextView.setText(getResources().getString(R.string.view_pager_indicator,
                itemIndex + 1, total));
    }

    public void setCaption(CharSequence caption) {
        mCaptionTextView.setText(caption);
    }
}
