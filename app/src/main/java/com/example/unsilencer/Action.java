package com.example.unsilencer;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;

public class Action implements Comparable<Action> {
    public int hour;
    public int minute;
    /**
     * Value should come from AudioManager (e.g., AudioManager.RINGER_MODE_SILENT).
     */
    public int ringerMode;

    public Action(int hour, int minute, int ringerMode) {
        this.hour = hour;
        this.minute = minute;
        this.ringerMode = ringerMode;
    }

    @Override
    public int compareTo(Action action) {
        return (this.hour * 60 + this.minute) - (action.hour * 60 + action.minute);
    }
}
