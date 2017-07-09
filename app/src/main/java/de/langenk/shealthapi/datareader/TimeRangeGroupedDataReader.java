package de.langenk.shealthapi.datareader;


import android.database.Cursor;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import java.util.ArrayList;
import java.util.Date;

import de.langenk.shealthapi.Constants;
import de.langenk.shealthapi.model.StepCount;

public abstract class TimeRangeGroupedDataReader extends TimeRangeDataReader {

    private Date from;
    private Date to;
    private String property;

    public TimeRangeGroupedDataReader(HealthDataStore dataStore, String dataType, String property, Date from, Date to, String deviceUid) {
        super(dataStore,
                dataType,
                new String[] {HealthConstants.DiscreteMeasurement.START_TIME, property},
                from,
                to,
                deviceUid
        );
        this.from = from;
        this.to = to;
        this.property = property;
    }

    protected final ArrayList<StepCount> list = new ArrayList<StepCount>();

    @Override
    protected void initialize() {
        for (long i = from.getTime(); i < to.getTime(); i+= Constants.MILLIS_PER_DAY) {
            list.add(new StepCount(0, new Date(i)));
        }
    }

    @Override
    protected void handleEntry(Cursor c) {
        long timeData = c.getLong(c.getColumnIndex(HealthConstants.StepCount.START_TIME));
        int day = (int) ((timeData - from.getTime()) / Constants.MILLIS_PER_DAY);
        list.get(day).count += c.getInt(c.getColumnIndex(this.property));
    }

}
