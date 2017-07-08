package de.langenk.shealthapi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

import com.google.gson.Gson;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    private final int MENU_ITEM_PERMISSION_SETTING = 1;
    private HealthData healthData;
    private Gson gson = new Gson();

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
            public void onConnected() {
            }

            @Override
            public void onConnectionFailed(HealthConnectionErrorResult error) {
                MainActivity.this.showConnectionFailureDialog(error);
            }

            @Override
            public void onDisconnected() {
            }
        });

        AsyncHttpServer server = new AsyncHttpServer();

        server.get("/steps/today", new HttpServerRequestCallback() {
            @Override
            public void onRequest(final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Calendar from = Calendar.getInstance();
                        Calendar to = Calendar.getInstance();
                        setStartOfDay(from);
                        to.add(Calendar.DAY_OF_WEEK, 1);
                        setStartOfDay(to);
                        healthData.readStepCount(from.getTime(), to.getTime(), request.getQuery().getString("deviceId"), new HealthData.ResultListener() {
                            @Override
                            public void onSuccess(Object result) {

                                response.send(gson.toJson(result));
                            }

                        });

                    }
                });

            }
        });

        server.get("/devices", new HttpServerRequestCallback() {
            @Override
            public void onRequest(final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        healthData.getDevices(new HealthData.ResultListener() {
                            @Override
                            public void onSuccess(Object result) {

                                response.send(gson.toJson(result));
                            }

                        });

                    }
                });

            }
        });

        server.get("/steps/week", new HttpServerRequestCallback() {
            @Override
            public void onRequest(final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Calendar from = Calendar.getInstance();
                        Calendar to = Calendar.getInstance();
                        from.add(Calendar.DAY_OF_WEEK, -6);
                        setStartOfDay(from);
                        to.add(Calendar.DAY_OF_WEEK, 1);
                        setStartOfDay(to);
                        healthData.readStepCount(from.getTime(), to.getTime(), request.getQuery().getString("deviceId"), new HealthData.ResultListener() {
                            @Override
                            public void onSuccess(Object result) {
                                response.send(gson.toJson(result));
                            }

                        });

                    }
                });

            }
        });

        server.get("/sleep/week", new HttpServerRequestCallback() {
            @Override
            public void onRequest(final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Calendar from = Calendar.getInstance();
                        Calendar to = Calendar.getInstance();
                        from.add(Calendar.DAY_OF_WEEK, -7);
                        setMidOfDay(from);
                        to.add(Calendar.DAY_OF_WEEK, 1);
                        setMidOfDay(to);
                        healthData.readSleep(from.getTime(), to.getTime(), request.getQuery().getString("deviceId"), new HealthData.ResultListener() {
                            @Override
                            public void onSuccess(Object result) {
                                response.send(gson.toJson(result));
                            }

                        });

                    }
                });

            }
        });

        server.listen(5000);
    }

    void setStartOfDay(Calendar cal) {
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
    }
    void setMidOfDay(Calendar cal) {
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 12);
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
