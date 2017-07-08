package de.langenk.shealthapi;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import de.langenk.shealthapi.model.Device;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthDevice;
import com.samsung.android.sdk.healthdata.HealthDeviceManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;
import com.samsung.android.sdk.healthdata.HealthResultHolder;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.langenk.shealthapi.Constants.APP_TAG;

public class HealthData {

    private Set<PermissionKey> mKeySet;
    private HealthDataStore mStore;
    private StepCountReporter mReporter;


    public HealthData() {
        mKeySet = new HashSet<PermissionKey>();
        mKeySet.add(new PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, PermissionType.READ));
        mKeySet.add(new PermissionKey(HealthConstants.Sleep.HEALTH_DATA_TYPE, PermissionType.READ));
    }

    public interface ConnectionListener extends HealthDataStore.ConnectionListener {
        void onPermissionMissing();
    }

    public interface PermissionListener {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    public void connect(Context context, final HealthData.ConnectionListener connectionListener) {
        HealthDataService healthDataService = new HealthDataService();
        try {
            healthDataService.initialize(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create a HealthDataStore instance and set its listener
        mStore = new HealthDataStore(context, new HealthDataStore.ConnectionListener() {

            @Override
            public void onConnected() {
                Log.d(Constants.APP_TAG, "Health data service is connected.");
                HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
                mReporter = new StepCountReporter(mStore);

                try {
                    // Check whether the permissions that this application needs are acquired
                    Map<PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeySet);

                    if (resultMap.containsValue(Boolean.FALSE)) {
                        connectionListener.onPermissionMissing();
                    } else {
                        connectionListener.onConnected();
                    }
                } catch (Exception e) {
                    Log.e(Constants.APP_TAG, e.getClass().getName() + " - " + e.getMessage());
                    Log.e(Constants.APP_TAG, "Permission setting fails.");
                    connectionListener.onPermissionMissing();
                }
            }

            @Override
            public void onConnectionFailed(HealthConnectionErrorResult error) {
                Log.d(Constants.APP_TAG, "Health data service is not available.");
                connectionListener.onConnectionFailed(error);
            }

            @Override
            public void onDisconnected() {
                Log.d(Constants.APP_TAG, "Health data service is disconnected.");
                connectionListener.onDisconnected();
            }
        });
        // Request the connection to the health data store
        mStore.connectService();
    }

    public void requestPermission(Activity activity, final PermissionListener listener) {
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            // Show user permission UI for allowing user to change options
            pmsManager.requestPermissions(mKeySet, activity).setResultListener(new HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult>() {

                @Override
                public void onResult(HealthPermissionManager.PermissionResult result) {
                    Log.d(Constants.APP_TAG, "Permission callback is received.");
                    Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();

                    if (resultMap.containsValue(Boolean.FALSE)) {
                        //drawStepCount("");
                        listener.onPermissionDenied();
                        return;
                    }
                    listener.onPermissionGranted();
                }
            });
        } catch (Exception e) {
            Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
            Log.e(APP_TAG, "Permission setting fails.");
        }
    }

    public void readStepCount(Date from, Date to, String deviceUid, StepCountReporter.ResultListener listener) {
        mReporter.readStepCount(from, to, deviceUid, listener);
    }

    public void readSleep(Date from, Date to, String deviceUid, StepCountReporter.ResultListener listener) {
        mReporter.readSleep(from, to, deviceUid, listener);
    }


    public void getDevices(StepCountReporter.ResultListener listener) {
        HealthDeviceManager deviceManager = new HealthDeviceManager(mStore);
        ArrayList<Device> list = new ArrayList<Device>();
        for(HealthDevice device : deviceManager.getAllDevices()) {
            list.add(new Device(device));
        }
        listener.onSuccess(list);
    }

}
