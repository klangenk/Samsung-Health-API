package de.langenk.shealthapi;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import de.langenk.shealthapi.Constants;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionResult;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;
import com.samsung.android.sdk.healthdata.HealthResultHolder;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.langenk.shealthapi.Constants.APP_TAG;

public class HealthData {

    private Activity context;
    private Set<PermissionKey> mKeySet;
    private HealthDataStore mStore;
    private HealthConnectionErrorResult mConnError;
    private StepCountReporter mReporter;


    public HealthData(Activity context) {
        this.context = context;
        mKeySet = new HashSet<PermissionKey>();
        mKeySet.add(new PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, PermissionType.READ));
        HealthDataService healthDataService = new HealthDataService();
        try {
            healthDataService.initialize(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create a HealthDataStore instance and set its listener
        mStore = new HealthDataStore(context, mConnectionListener);
        // Request the connection to the health data store
        mStore.connectService();
    }

    public void requestPermission() {
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            // Show user permission UI for allowing user to change options
            pmsManager.requestPermissions(mKeySet, this.context).setResultListener(mPermissionListener);
        } catch (Exception e) {
            Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
            Log.e(APP_TAG, "Permission setting fails.");
        }
    }

    private void showPermissionAlarmDialog() {
        if (this.context.isFinishing()) {
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(this.context);
        alert.setTitle("Notice");
        alert.setMessage("All permissions should be acquired");
        alert.setPositiveButton("OK", null);
        alert.show();
    }

    private void showConnectionFailureDialog(HealthConnectionErrorResult error) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this.context);
        mConnError = error;
        String message = "Connection with Samsung Health is not available";

        if (mConnError.hasResolution()) {
            switch(error.getErrorCode()) {
                case HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED:
                    message = "Please install Samsung Health";
                    break;
                case HealthConnectionErrorResult.OLD_VERSION_PLATFORM:
                    message = "Please upgrade Samsung Health";
                    break;
                case HealthConnectionErrorResult.PLATFORM_DISABLED:
                    message = "Please enable Samsung Health";
                    break;
                case HealthConnectionErrorResult.USER_AGREEMENT_NEEDED:
                    message = "Please agree with Samsung Health policy";
                    break;
                default:
                    message = "Please make Samsung Health available";
                    break;
            }
        }

        alert.setMessage(message);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (mConnError.hasResolution()) {
                    mConnError.resolve(HealthData.this.context);
                }
            }
        });

        if (error.hasResolution()) {
            alert.setNegativeButton("Cancel", null);
        }

        alert.show();
    }

    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {

        @Override
        public void onConnected() {
            Log.d(Constants.APP_TAG, "Health data service is connected.");
            HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
            mReporter = new StepCountReporter(mStore);

            try {
                // Check whether the permissions that this application needs are acquired
                Map<PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeySet);

                if (resultMap.containsValue(Boolean.FALSE)) {
                    // Request the permission for reading step counts if it is not acquired
                    pmsManager.requestPermissions(mKeySet, HealthData.this.context).setResultListener(mPermissionListener);
                }
            } catch (Exception e) {
                Log.e(Constants.APP_TAG, e.getClass().getName() + " - " + e.getMessage());
                Log.e(Constants.APP_TAG, "Permission setting fails.");
            }
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            Log.d(Constants.APP_TAG, "Health data service is not available.");
            showConnectionFailureDialog(error);
        }

        @Override
        public void onDisconnected() {
            Log.d(Constants.APP_TAG, "Health data service is disconnected.");
        }
    };

    public void readTodayStepCount(StepCountReporter.StepCountListener listener) {
        mReporter.readTodayStepCount(listener);
    }

    private final HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult> mPermissionListener =
        new HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult>() {

            @Override
            public void onResult(HealthPermissionManager.PermissionResult result) {
                Log.d(Constants.APP_TAG, "Permission callback is received.");
                Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();

                if (resultMap.containsValue(Boolean.FALSE)) {
                    //drawStepCount("");
                    showPermissionAlarmDialog();
                }
            }
        };
}
