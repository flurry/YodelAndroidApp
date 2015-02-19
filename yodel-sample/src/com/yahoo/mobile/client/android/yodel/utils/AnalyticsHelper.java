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

package com.yahoo.mobile.client.android.yodel.utils;

import com.flurry.android.FlurryAgent;

import java.util.Map;

/**
 * Helps with logging custom events and errors to Flurry
 */
public class AnalyticsHelper {
    public static final String EVENT_STREAM_ARTICLE_CLICK = "stream_article_click";
    public static final String EVENT_STREAM_AD_CLICK = "stream_ad_click";
    public static final String EVENT_STREAM_SEARCH_CLICK = "stream_search_click";
    public static final String EVENT_STREAM_PULL_REFRESH = "stream_pullto_refresh";
    public static final String EVENT_CAR_MOREIMG_CLICK = "carousel_moreimages_click";
    public static final String EVENT_CAR_LEARNMORE_CLICK = "carousel_learnmore_click";
    public static final String EVENT_CAR_CONTENT_SWIPE = "carousel_content_swipe";
    public static final String EVENT_AD_CLOSEBUTTON_CLICK = "ad_closebutton_click";
    public static final String EVENT_SEARCH_STARTED = "search_term_started";
    public static final String EVENT_SEARCH_MOREONWEB_CLICK = "search_moreonweb_click";
    public static final String EVENT_SEARCH_RESULT_CLICK = "search_result_click";

    public static final String PARAM_ARTICLE_ORIGIN = "article_origin";
    public static final String PARAM_ARTICLE_TYPE = "article_type";
    public static final String PARAM_CONTENT_LOADED = "content_loaded";
    public static final String PARAM_SEARCH_TERM = "search_term";

    /**
     * Logs an event for analytics.
     *
     * @param eventName     name of the event
     * @param eventParams   event parameters (can be null)
     * @param timed         <code>true</code> if the event should be timed, false otherwise
     */
    public static void logEvent(String eventName, Map<String, String> eventParams, boolean timed) {
        FlurryAgent.logEvent(eventName, eventParams, timed);
    }

    /**
     * Ends a timed event that was previously started.
     *
     * @param eventName     name of the event
     * @param eventParams   event parameters (can be null)
     */
    public static void endTimedEvent(String eventName, Map<String, String> eventParams) {
        FlurryAgent.endTimedEvent(eventName, eventParams);
    }

    /**
     * Logs an error.
     *
     * @param errorId           error ID
     * @param errorDescription  error description
     * @param throwable         a {@link Throwable} that describes the error
     */
    public static void logError(String errorId, String errorDescription, Throwable throwable) {
        FlurryAgent.onError(errorId, errorDescription, throwable);
    }
}
