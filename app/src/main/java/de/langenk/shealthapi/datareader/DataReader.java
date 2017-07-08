package de.langenk.shealthapi.datareader;


import android.database.Cursor;
import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import static de.langenk.shealthapi.Constants.APP_TAG;

public abstract class DataReader {
    protected Filter filter;
    protected String[] properties;
    private HealthDataStore dataStore;
    private String dataType;

    public DataReader(HealthDataStore dataStore, String dataType, Filter filter, String[] properties) {
        this.filter = filter;
        this.properties = properties;
        this.dataStore = dataStore;
        this.dataType = dataType;
    }

    protected void initialize() {

    }
    protected abstract void handleEntry(Cursor c);
    protected abstract void handleFinished();

    public void readData() {
        HealthDataResolver resolver = new HealthDataResolver(this.dataStore, null);

        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(this.dataType)
                .setProperties(this.properties)
                .setFilter(this.filter)
                .build();

        this.initialize();
        try {
            resolver.read(request).setResultListener(new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>() {

                @Override
                public void onResult(HealthDataResolver.ReadResult result) {
                    Cursor c = null;
                    try {
                        c = result.getResultCursor();
                        if (c != null) {
                            while (c.moveToNext()) {
                                DataReader.this.handleEntry(c);
                            }
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }
                    DataReader.this.handleFinished();
                }
            });
        } catch (Exception e) {
            Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
            Log.e(APP_TAG, "Getting step count fails.");
        }
    }

}
