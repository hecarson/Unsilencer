package com.example.unsilencer;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ActionsViewModel extends ViewModel {

    private List<Action> actions = new ArrayList<>();
    private final MutableLiveData<List<Action>> actionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Action>> addedActionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Action>> removedActionsLiveData = new MutableLiveData<>();
    private final File actionsFile;
    private int uniqueRequestCode = 0;

    public ActionsViewModel(String filesDir) {
        actionsFile = new File(filesDir, "actions.json");
        readActionsFromFile();
        // observers registered later will observe this setValue
        actionsLiveData.setValue(actions);
    }

    public void addAction(int hour, int minute, int ringerMode) {
        Action action = new Action(hour, minute, ringerMode, uniqueRequestCode++);
        actions.add(action);
        Collections.sort(actions, this::compareActionsByTime);

        actionsLiveData.setValue(actions);
        addedActionsLiveData.setValue(Arrays.asList(action));
        saveActionsToFile();
    }

    public void setAction(int index, int hour, int minute, int ringerMode) {
        Action oldAction = actions.get(index);
        Action newAction = new Action(hour, minute, ringerMode, oldAction.requestCode());
        actions.set(index, newAction);

        Collections.sort(actions, this::compareActionsByTime);

        actionsLiveData.setValue(actions);
        removedActionsLiveData.setValue(Arrays.asList(oldAction));
        addedActionsLiveData.setValue(Arrays.asList(newAction));
        saveActionsToFile();
    }

    public Action getAction(int index) {
        return actions.get(index);
    }

    public void removeActions(List<Integer> indices) {
        ArrayList<Action> removedActions = new ArrayList<>();

        for (int index : indices) {
            removedActions.add(actions.get(index));
            actions.set(index, null);
        }

        ArrayList<Action> newActions = new ArrayList<>();

        for (Action action : actions) {
            if (action != null)
                newActions.add(action);
        }

        actions = newActions;

        removedActionsLiveData.setValue(removedActions);
        actionsLiveData.setValue(actions);
        saveActionsToFile();
    }

    public LiveData<List<Action>> getActionsLiveData() {
        return actionsLiveData;
    }

    public LiveData<List<Action>> getAddedActionsLiveData() {
        return addedActionsLiveData;
    }

    public LiveData<List<Action>> getRemovedActionRequestCodesLiveData() {
        return removedActionsLiveData;
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

        // find new unique request code
        for (Action action : actions) {
            if (action.requestCode() > uniqueRequestCode)
                uniqueRequestCode = action.requestCode();
        }
        ++uniqueRequestCode;
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

    private int compareActionsByTime(Action a1, Action a2) {
        return (a1.hour() * 60 + a1.minute()) - (a2.hour() * 60 + a2.minute());
    }

}