package de.langenk.shealthapi;


import android.database.Cursor;
import android.util.Log;

import de.langenk.shealthapi.datareader.TimeRangeDataReader;
import de.langenk.shealthapi.model.Device;
import de.langenk.shealthapi.model.ListWrapper;
import de.langenk.shealthapi.model.StepCount;
import de.langenk.shealthapi.model.TimeRange;

import com.samsung.android.sdk.healthdata.HealthDevice;
import com.samsung.android.sdk.healthdata.HealthDeviceManager;
import com.samsung.android.sdk.healthdata.HealthConstants;

import java.util.ArrayList;
import java.util.Date;

import static de.langenk.shealthapi.Constants.APP_TAG;

public class HealthData extends AbstractHealthData {

    private static final long MILLIS_PER_DAY = 1000*60*60*24;



    public void readSleep(Date from, Date to, String deviceUid, final ResultListener listener) {
        new TimeRangeDataReader(
                mStore,
                HealthConstants.Sleep.HEALTH_DATA_TYPE,
                new String[] {HealthConstants.Sleep.END_TIME, HealthConstants.Sleep.START_TIME},
                from,
                to,
                deviceUid
        ) {

            private ArrayList<TimeRange> list = new ArrayList<TimeRange>();

            @Override
            protected void handleEntry(Cursor c) {
                list.add(new TimeRange(
                        c.getLong(c.getColumnIndex(HealthConstants.Sleep.START_TIME)),
                        c.getLong(c.getColumnIndex(HealthConstants.Sleep.END_TIME))
                ));
            }

            @Override
            protected void handleFinished() {
                listener.onSuccess(new ListWrapper(list));
            }
        }.readData();

    }

    public void readStepCount(final Date from, final Date to, String deviceUid, final ResultListener listener) {
        new TimeRangeDataReader(
                mStore,
                HealthConstants.StepCount.HEALTH_DATA_TYPE,
                new String[] {HealthConstants.StepCount.COUNT, HealthConstants.StepCount.START_TIME},
                from,
                to,
                deviceUid
        ) {

            final ArrayList<StepCount> list = new ArrayList<StepCount>();

            @Override
            protected void initialize() {
                for (long i = from.getTime(); i < to.getTime(); i+= MILLIS_PER_DAY) {
                    list.add(new StepCount(0, new Date(i)));
                }
            }

            @Override
            protected void handleEntry(Cursor c) {
                long timeData = c.getLong(c.getColumnIndex(HealthConstants.StepCount.START_TIME));
                int day = (int) ((timeData - from.getTime()) / MILLIS_PER_DAY);
                list.get(day).count += c.getInt(c.getColumnIndex(HealthConstants.StepCount.COUNT));
            }

            @Override
            protected void handleFinished() {
                if(list.size() == 1) {
                    listener.onSuccess(list.get(0));
                } else {
                    listener.onSuccess(new ListWrapper(list));
                }
            }
        }.readData();

    }


    public void getDevices(ResultListener listener) {
        HealthDeviceManager deviceManager = new HealthDeviceManager(mStore);
        ArrayList<Device> list = new ArrayList<Device>();
        for(HealthDevice device : deviceManager.getAllDevices()) {
            list.add(new Device(device));
        }
        listener.onSuccess(list);
    }

}
