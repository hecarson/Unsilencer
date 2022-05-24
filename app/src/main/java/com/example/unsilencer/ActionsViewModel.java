package com.example.unsilencer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ActionsViewModel extends ViewModel {

    private List<Action> actions = new ArrayList<>();
    private final MutableLiveData<List<Action>> actionsLiveData = new MutableLiveData<>();
    private final File actionsFile;

    public ActionsViewModel(String filesDir) {
        actionsLiveData.setValue(actions);
        actionsFile = new File(filesDir, "actions.json");
        readActionsFromFile();
    }

    public void addAction(int hour, int minute, int ringerMode) {
        Action newAction = new Action(hour, minute, ringerMode);
        actions.add(newAction);
        Collections.sort(actions);

        actionsLiveData.setValue(actions);
        saveActionsToFile();
    }

    public void setAction(int index, int hour, int minute, int ringerMode) {
        Action action = actions.get(index);
        action.hour = hour;
        action.minute = minute;
        action.ringerMode = ringerMode;

        Collections.sort(actions);

        actionsLiveData.setValue(actions);
        saveActionsToFile();
    }

    public Action getAction(int index) {
        return actions.get(index);
    }

    public void removeActions(List<Integer> indices) {
        for (int index : indices)
            actions.set(index, null);

        ArrayList<Action> newActions = new ArrayList<>();

        for (Action action : actions) {
            if (action != null)
                newActions.add(action);
        }

        actions = newActions;

        actionsLiveData.setValue(actions);
        saveActionsToFile();
    }

    public LiveData<List<Action>> getActionsLiveData() {
        return actionsLiveData;
    }

    private void readActionsFromFile() {
        if (!actionsFile.exists())
            return;

        try (JsonReader reader = new JsonReader(new FileReader(actionsFile))) {
            Gson gson = new Gson();

            reader.beginArray();

            while (reader.hasNext())
                actions.add(gson.fromJson(reader, Action.class));

            reader.endArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void saveActionsToFile() {
        if (actionsFile.exists() && !actionsFile.delete())
            throw new RuntimeException("could not delete actions.json file");

        try (JsonWriter writer = new JsonWriter(new FileWriter(actionsFile))) {
            Gson gson = new Gson();

            writer.beginArray();

            for (Action action : actions)
                gson.toJson(action, Action.class, writer);

            writer.endArray();
            writer.flush();
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

//    private void scheduleNewAction(Action action) {
//        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms())
//            return;
//
//        GregorianCalendar nowTime = new GregorianCalendar();
//        nowTime.setTime(new Date(System.currentTimeMillis()));
//        GregorianCalendar actionTime = (GregorianCalendar)nowTime.clone();
//        actionTime.set(Calendar.HOUR_OF_DAY, action.hour);
//        actionTime.set(Calendar.MINUTE, action.minute);
//
//        if (actionTime.before(nowTime))
//            actionTime.add(Calendar.DAY_OF_MONTH, 1);
//    }

}