package de.langenk.shealthapi;


import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StepCount implements JsonAble{
    private int count;
    private Date date;

    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy '00:00:00' 'GMT'Z '('z')'");

    public StepCount(int count){
        this.count = count;
        this.date = Calendar.getInstance().getTime();
    }

    public StepCount(int count, Date date){
        this.count = count;
        this.date = date;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try{
            obj.put("count", this.count);
            obj.put("date", formatter.format(this.date));
        } catch (Exception ex) {

        }
        return obj;
    }
}
