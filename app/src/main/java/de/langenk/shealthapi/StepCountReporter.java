/**
 * Copyright (C) 2014 Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Mobile Communication Division,
 * Digital Media & Communications Business, Samsung Electronics Co., Ltd.
 *
 * This software and its documentation are confidential and proprietary
 * information of Samsung Electronics Co., Ltd.  No part of the software and
 * documents may be copied, reproduced, transmitted, translated, or reduced to
 * any electronic medium or machine-readable form without the prior written
 * consent of Samsung Electronics.
 *
 * Samsung Electronics makes no representations with respect to the contents,
 * and assumes no responsibility for any errors that might appear in the
 * software and documents. This publication and the contents hereof are subject
 * to change without notice.
 */

package de.langenk.shealthapi;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadResult;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import android.database.Cursor;
import android.util.Log;

import java.util.Calendar;

import static de.langenk.shealthapi.Constants.APP_TAG;

public class StepCountReporter {
    private final HealthDataStore mStore;

    public interface StepCountListener {
        void onSuccess(int result);
    }

    public StepCountReporter(HealthDataStore store) {
        mStore = store;
    }


    // Read the today's step count on demand
    public void readTodayStepCount(final StepCountListener listener) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        // Set time range from start time of today to the current time
        long startTime = getStartTimeOfToday();
        long endTime = System.currentTimeMillis();
        Filter filter = Filter.and(Filter.greaterThanEquals(HealthConstants.StepCount.START_TIME, startTime),
                Filter.lessThanEquals(HealthConstants.StepCount.START_TIME, endTime));

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .setProperties(new String[] {HealthConstants.StepCount.COUNT})
                .setFilter(filter)
                .build();

        try {
            resolver.read(request).setResultListener(new HealthResultHolder.ResultListener<ReadResult>() {

                @Override
                public void onResult(ReadResult result) {
                    int count = 0;
                    Cursor c = null;

                    try {
                        c = result.getResultCursor();
                        if (c != null) {
                            while (c.moveToNext()) {
                                count += c.getInt(c.getColumnIndex(HealthConstants.StepCount.COUNT));
                            }
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }
                    listener.onSuccess(count);
                }
            });
        } catch (Exception e) {
            Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
            Log.e(APP_TAG, "Getting step count fails.");
        }
    }

    private long getStartTimeOfToday() {
        Calendar today = Calendar.getInstance();

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today.getTimeInMillis();
    }

}
