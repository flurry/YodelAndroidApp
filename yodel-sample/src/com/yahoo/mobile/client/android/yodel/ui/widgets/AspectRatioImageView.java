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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yahoo.mobile.client.android.yodel.R;


/**
 * ImageView that respects a proportional relationship between its width and its height, regardless
 * of drawable size and regardless of screen size.
 */
public class AspectRatioImageView extends ImageView {

    private float mAspectRatioWidth;
    private float mAspectRationHeight;
    private boolean mWidthAsBase;

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.AspectRatioImageView,
                0, 0);

        if (a != null) {
            // Defaults to a 4:3 ratio
            setAspectRatioWidth(a.getInt(R.styleable.AspectRatioImageView_aspectRatioWidth, 4));
            setAspectRationHeight(a.getInt(R.styleable.AspectRatioImageView_aspectRatioHeight, 3));

            setWidthAsBase(a.getBoolean(R.styleable.AspectRatioImageView_widthAsBase, true));

            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (isWidthAsBase()) {
            int newHeight = (int)(getMeasuredWidth() * getAspectRationHeight() / getAspectRatioWidth());
            setMeasuredDimension(getMeasuredWidth(), newHeight);
        } else {
            int newWidth = (int)(getMeasuredHeight() * getAspectRatioWidth() / getAspectRationHeight());
            setMeasuredDimension(newWidth, getMeasuredHeight());
        }
    }

    public float getAspectRatioWidth() {
        return mAspectRatioWidth;
    }

    public void setAspectRatioWidth(int aspectRatioWidth) {
        mAspectRatioWidth = aspectRatioWidth;
    }

    public float getAspectRationHeight() {
        return mAspectRationHeight;
    }

    public void setAspectRationHeight(int aspectRationHeight) {
        mAspectRationHeight = aspectRationHeight;
    }

    public boolean isWidthAsBase() {
        return mWidthAsBase;
    }

    public void setWidthAsBase(boolean widthAsBase) {
        mWidthAsBase = widthAsBase;
    }
}
