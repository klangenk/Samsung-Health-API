package de.langenk.shealthapi.datareader;


import android.database.Cursor;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import java.util.Date;

import de.langenk.shealthapi.datareader.DataReader;

public abstract class TimeRangeDataReader extends DataReader {

    public TimeRangeDataReader(HealthDataStore dataStore, String dataType, String[] properties, Date from, Date to, String deviceUid) {
        super(
                dataStore,
                dataType,
                Filter.and(Filter.greaterThanEquals(HealthConstants.Sleep.START_TIME, from.getTime()),
                    Filter.lessThanEquals(HealthConstants.Sleep.START_TIME, to.getTime())),
                properties);

        if(deviceUid != null){
            this.filter = Filter.and(this.filter, Filter.eq(HealthConstants.StepCount.DEVICE_UUID, deviceUid));
        }
    }

}
