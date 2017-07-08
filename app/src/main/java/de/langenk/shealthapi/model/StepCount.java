package de.langenk.shealthapi.model;

import java.util.Calendar;
import java.util.Date;

public class StepCount {
    public int count;
    public Date date;


    public StepCount(int count){
        this.count = count;
        this.date = (Date) Calendar.getInstance().getTime();
    }

    public StepCount(int count, Date date){
        this.count = count;
        this.date = date;
    }

}
