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
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.yahoo.mobile.client.android.yodel.R;
import com.yahoo.mobile.client.share.search.interfaces.ISearchController;
import com.yahoo.mobile.client.share.search.interfaces.ISearchViewHolder;

/**
 * <p>A custom search view holder to be used as header in Yahoo Search SDK.</p>
 * <p>See Yahoo Search SDK documentation for more.</p>
 */
public class CustomSearchViewHolder extends LinearLayout implements ISearchViewHolder {

    public CustomSearchViewHolder(Context context) {
        super(context);
    }

    public CustomSearchViewHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public CustomSearchViewHolder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSearchController(ISearchController iSearchController) {
        
    }

    @Override
    public EditText getSearchEditText() {
        return (EditText)findViewById(R.id.search_bar_edit_text);
    }

    @Override
    public View getVoiceSearchButton() {
        return null;
    }

    @Override
    public View getClearTextButton() {
        return findViewById(R.id.search_bar_clear_icon);
    }

    @Override
    public int getSearchViewHeightOffset() {
        return 0;
    }

    @Override
    public void onVoiceSearchAvailabilityChanged(boolean b) {

    }
}
