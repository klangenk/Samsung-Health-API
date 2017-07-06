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
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadResult;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import android.database.Cursor;
import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static de.langenk.shealthapi.Constants.APP_TAG;

public class StepCountReporter {
    private final HealthDataStore mStore;
    private static final long MILLIS_PER_DAY = 1000*60*60*24;

    public interface ResultListener {
        void onSuccess(JsonAble result);
    }

    public StepCountReporter(HealthDataStore store) {
        mStore = store;
    }


    // Read the today's step count on demand
    public void readStepCount(Date from, Date to, String deviceUid, final ResultListener listener) {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        // Set time range from start time of today to the current time
        final long startTime = from.getTime();
        final long endTime = to.getTime();
        Filter filter = Filter.and(Filter.greaterThanEquals(HealthConstants.StepCount.START_TIME, startTime),
                Filter.lessThanEquals(HealthConstants.StepCount.START_TIME, endTime));

        if(deviceUid != null){
            filter = Filter.and(filter, Filter.eq(HealthConstants.StepCount.DEVICE_UUID, deviceUid));
        }

        HealthDataResolver.ReadRequest request = new ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .setProperties(new String[] {HealthConstants.StepCount.COUNT, HealthConstants.StepCount.START_TIME})
                .setFilter(filter)
                .build();
        
        final Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        final List<StepCount> list = new List<StepCount>();
        try {
            resolver.read(request).setResultListener(new HealthResultHolder.ResultListener<ReadResult>() {

                @Override
                public void onResult(ReadResult result) {
                    int count = 0;
                    Cursor c = null;
                    try {
                        c = result.getResultCursor();
                        Log.d(APP_TAG, Arrays.toString(c.getColumnNames()));
                        if (c != null) {
                            long timeSlot = cal.getTimeInMillis();
                            while (c.moveToNext()) {
                                long timeData = c.getLong(c.getColumnIndex(HealthConstants.StepCount.START_TIME));
                                if ( timeSlot > timeData || timeData > timeSlot + MILLIS_PER_DAY) {
                                    list.add(new StepCount(count, cal.getTime()));
                                    count = 0;
                                    cal.add(Calendar.DAY_OF_WEEK, 1);
                                    timeSlot = cal.getTimeInMillis();
                                }
                                count += c.getInt(c.getColumnIndex(HealthConstants.StepCount.COUNT));
                            }
                            while (timeSlot < endTime) {
                                timeSlot = cal.getTimeInMillis();
                                list.add(new StepCount(count, cal.getTime()));
                                count = 0;
                                cal.add(Calendar.DAY_OF_WEEK, 1);
                                timeSlot = cal.getTimeInMillis();
                            }
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }
                    if(list.size() == 1) {
                        listener.onSuccess(list.get(0));
                    } else {
                        listener.onSuccess(new ResultWrapper("collection", list));
                    }

                }
            });
        } catch (Exception e) {
            Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
            Log.e(APP_TAG, "Getting step count fails.");
        }
    }

}
