package de.langenk.shealthapi.model;


import com.samsung.android.sdk.healthdata.HealthDevice;

public class Device {

    private String uuid;
    private String name;

    public Device(HealthDevice device){
        this.uuid = device.getUuid();
        this.name = device.getCustomName();
    }
}
