package de.langenk.shealthapi.model;

import java.util.Date;

public class TimeRange {
    private Date from;
    private Date to;

    public TimeRange(Date from, Date to){
        this.from = from;
        this.to = to;
    }

    public TimeRange(long from, long to){
        this.from = new Date(from);
        this.to = new Date(to);
    }

}
