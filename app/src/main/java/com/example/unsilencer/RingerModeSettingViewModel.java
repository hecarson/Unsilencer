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

public class RingerModeSettingViewModel extends ViewModel {

    private List<RingerModeSetting> ringerModeSettings = new ArrayList<>();
    private final MutableLiveData<List<RingerModeSetting>> settingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<RingerModeSetting>> addedSettingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<RingerModeSetting>> removedSettingsLiveData = new MutableLiveData<>();
    private final File settingsFile;

    public RingerModeSettingViewModel(String filesDir) {
        settingsFile = new File(filesDir, "actions.json");
        readActionsFromFile();
        // observers registered later will observe this setValue
        settingsLiveData.setValue(ringerModeSettings);
    }

    public void addAction(int hour, int minute, int ringerMode) {
        RingerModeSetting ringerModeSetting = new RingerModeSetting(hour, minute, ringerMode);
        ringerModeSettings.add(ringerModeSetting);
        Collections.sort(ringerModeSettings, this::compareActionsByTime);

        settingsLiveData.setValue(ringerModeSettings);
        addedSettingsLiveData.setValue(Arrays.asList(ringerModeSetting));
        saveActionsToFile();
    }

    public void setAction(int index, int hour, int minute, int ringerMode) {
        RingerModeSetting oldRingerModeSetting = ringerModeSettings.get(index);
        RingerModeSetting newRingerModeSetting = new RingerModeSetting(hour, minute, ringerMode, oldRingerModeSetting.requestCode());
        ringerModeSettings.set(index, newRingerModeSetting);

        Collections.sort(ringerModeSettings, this::compareActionsByTime);

        settingsLiveData.setValue(ringerModeSettings);
        removedSettingsLiveData.setValue(Arrays.asList(oldRingerModeSetting));
        addedSettingsLiveData.setValue(Arrays.asList(newRingerModeSetting));
        saveActionsToFile();
    }

    public RingerModeSetting getAction(int index) {
        return ringerModeSettings.get(index);
    }

    public void removeActions(List<Integer> indices) {
        ArrayList<RingerModeSetting> removedRingerModeSettings = new ArrayList<>();

        for (int index : indices) {
            removedRingerModeSettings.add(ringerModeSettings.get(index));
            ringerModeSettings.set(index, null);
        }

        ArrayList<RingerModeSetting> newRingerModeSettings = new ArrayList<>();

        for (RingerModeSetting ringerModeSetting : ringerModeSettings) {
            if (ringerModeSetting != null)
                newRingerModeSettings.add(ringerModeSetting);
        }

        ringerModeSettings = newRingerModeSettings;

        removedSettingsLiveData.setValue(removedRingerModeSettings);
        settingsLiveData.setValue(ringerModeSettings);
        saveActionsToFile();
    }

    /**
     * This is useful to cancel all action alarms to cancel and set them again, which ensures that
     * all actions are actually scheduled.
     */
    public void removeAndReaddAllActions() {
        removedSettingsLiveData.setValue(ringerModeSettings);
        addedSettingsLiveData.setValue(ringerModeSettings);
    }

    public LiveData<List<RingerModeSetting>> getSettingsLiveData() {
        return settingsLiveData;
    }

    public LiveData<List<RingerModeSetting>> getAddedSettingsLiveData() {
        return addedSettingsLiveData;
    }

    public LiveData<List<RingerModeSetting>> getRemovedActionRequestCodesLiveData() {
        return removedSettingsLiveData;
    }

    private void readActionsFromFile() {
        if (!settingsFile.exists())
            return;

        try (JsonReader reader = new JsonReader(new FileReader(settingsFile))) {
            Gson gson = new Gson();

            reader.beginArray();

            while (reader.hasNext())
                ringerModeSettings.add(gson.fromJson(reader, RingerModeSetting.class));

            reader.endArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void saveActionsToFile() {
        if (settingsFile.exists() && !settingsFile.delete())
            throw new RuntimeException("could not delete actions.json file");

        try (JsonWriter writer = new JsonWriter(new FileWriter(settingsFile))) {
            Gson gson = new Gson();

            writer.beginArray();

            for (RingerModeSetting ringerModeSetting : ringerModeSettings)
                gson.toJson(ringerModeSetting, RingerModeSetting.class, writer);

            writer.endArray();
            writer.flush();
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private int compareActionsByTime(RingerModeSetting a1, RingerModeSetting a2) {
        return (a1.hour() * 60 + a1.minute()) - (a2.hour() * 60 + a2.minute());
    }

}