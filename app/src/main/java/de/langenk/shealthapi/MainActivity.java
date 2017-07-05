package de.langenk.shealthapi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import static de.langenk.shealthapi.Constants.APP_TAG;

public class MainActivity extends AppCompatActivity {
    private final int MENU_ITEM_PERMISSION_SETTING = 1;
    private HealthData healthData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        healthData = new HealthData();
        healthData.connect(this, new HealthData.ConnectionListener() {

            @Override
            public void onPermissionMissing() {
                MainActivity.this.requestHealthDataPermission();
            }

            @Override
            public void onConnected() {}

            @Override
            public void onConnectionFailed(HealthConnectionErrorResult error) {
                MainActivity.this.showConnectionFailureDialog(error);
            }

            @Override
            public void onDisconnected() {}
        });

        AsyncHttpServer server = new AsyncHttpServer();
        List<WebSocket> _sockets = new ArrayList<WebSocket>();
        /**/

        server.get("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        healthData.readTodayStepCount(new StepCountReporter.StepCountListener() {
                            @Override
                            public void onSuccess(int result) {
                                response.send("Count: " + result);
                            }
                        });
                    }
                });

            }
        });
        server.listen(5000);
    }

    void requestHealthDataPermission() {
        healthData.requestPermission(this, new HealthData.PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied() {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(1, MENU_ITEM_PERMISSION_SETTING, 0, "Connect to Samsung Health");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if(item.getItemId() == (MENU_ITEM_PERMISSION_SETTING)) {
            this.requestHealthDataPermission();
        }
        return true;
    }

    private void showConnectionFailureDialog(final HealthConnectionErrorResult error) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        //mConnError = error;
        String message = "Connection with Samsung Health is not available";

        if (error.hasResolution()) {
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
                if (error.hasResolution()) {
                    error.resolve(MainActivity.this);
                }
            }
        });

        if (error.hasResolution()) {
            alert.setNegativeButton("Cancel", null);
        }

        alert.show();
    }
}
