package de.langenk.shealthapi;


import android.database.Cursor;
import android.util.Log;

import de.langenk.shealthapi.datareader.TimeRangeDataReader;
import de.langenk.shealthapi.datareader.TimeRangeGroupedDataReader;
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
        new TimeRangeGroupedDataReader(
                mStore,
                HealthConstants.StepCount.HEALTH_DATA_TYPE,
                HealthConstants.StepCount.COUNT,
                from,
                to,
                deviceUid
        ) {

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

    public void readCalories(final Date from, final Date to, String deviceUid, final ResultListener listener) {
        new TimeRangeGroupedDataReader(
                mStore,
                HealthConstants.Exercise.HEALTH_DATA_TYPE,
                HealthConstants.Exercise.CALORIE,
                from,
                to,
                deviceUid
        ) {

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
