package com.example.unsilencer;

import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Action {

    private final int hour;
    private final int minute;
    /**
     * Value should come from AudioManager (e.g., AudioManager.RINGER_MODE_SILENT).
     */
    private final int ringerMode;
    /**
     * This is used to make PendingIntents with the same Intent unique, in ActionScheduler.
     */
    private final int requestCode;

    public Action(int hour, int minute, int ringerMode, int requestCode) {
        this.hour = hour;
        this.minute = minute;
        this.ringerMode = ringerMode;
        this.requestCode = requestCode;
    }

    public int hour() {
        return hour;
    }

    public int minute() {
        return minute;
    }

    public int ringerMode() {
        return ringerMode;
    }

    public int requestCode() {
        return requestCode;
    }

}