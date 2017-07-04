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

        healthData = new HealthData(this);

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


// listen on port 5000
        server.listen(5000);
// browsing http://localhost:5000 will return Hello!!!
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
            healthData.requestPermission();
        }
        return true;
    }


}
