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

import android.content.Context;
import android.text.format.DateFormat;

import com.yahoo.mobile.client.android.yodel.R;

import java.util.Date;

public final class DateTimeUtil {

    /**
     * Gets the current time in epoch time milliseconds.
     * @return the current time in epoch time milliseconds.
     */
    public static long getNow() {
        return new Date().getTime();
    }

    /**
     * Gets the local time as a Date object.
     *
     * @param unixTimeStampMillis the epoch time in milliseconds.
     * @return the {@link java.util.Date} representing the local time.
     */
    public static Date getLocalDateFromTicks(long unixTimeStampMillis) {
        return new Date(unixTimeStampMillis);
    }

    /**
     * <p>Gets a simple representation of the time at time in which something occurred relative
     * to now.</p>
     *
     * <p>E.g. "1 day ago", "3 hours ago", "Just now"</p>
     * @param unixTimeStampMillis the epoch time in the past in milliseconds.
     * @param context the current application context.
     * @return a string representation of the time.
     */
    public static String getFriendlyDateString(long unixTimeStampMillis, Context context) {
        // Don't trust this method in prod. Currently, 1.4 days ago is "yesterday"
        long timeDiffMs = getNow() - unixTimeStampMillis;
        long timeDiffMinutes = timeDiffMs / (1000 * 60);
        long timeDiffHours = timeDiffMinutes / 60;
        long timeDiffDays = timeDiffHours / 24;

        if (timeDiffDays > 7) {
            return DateFormat.getMediumDateFormat(context)
                    .format(getLocalDateFromTicks(unixTimeStampMillis));
        } else if (timeDiffDays > 0) {
            return context.getResources()
                    .getQuantityString(R.plurals.friendly_date_day_ago,
                            (int)timeDiffDays, (int)timeDiffDays);
        } else if (timeDiffHours > 0) {
            return context.getResources()
                    .getQuantityString(R.plurals.friendly_date_hour_ago,
                            (int)timeDiffHours, (int)timeDiffHours);
        } else if (timeDiffMinutes > 0) {
            return context.getResources()
                    .getQuantityString(R.plurals.friendly_date_minute_ago,
                            (int)timeDiffMinutes, (int)timeDiffMinutes);
        } else {
            return context.getResources().getString(R.string.friendly_date_now);
        }
    }
}
