package com.example.unsilencer;

import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class RingerModeSetting {

    private int hour;
    private int minute;
    /**
     * Value should come from AudioManager (e.g., AudioManager.RINGER_MODE_SILENT).
     */
    private int ringerMode;
    /**
     * This is used to make PendingIntents with the same Intent unique, in ActionScheduler.
     */
    private int requestCode;

    private static int nextRequestCode = 0;

    public RingerModeSetting(int hour, int minute, int ringerMode, int requestCode) {
        this.hour = hour;
        this.minute = minute;
        this.ringerMode = ringerMode;
        this.requestCode = requestCode;
    }

    public RingerModeSetting(int hour, int minute, int ringerMode) {
        this(hour, minute, ringerMode, nextRequestCode++);
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