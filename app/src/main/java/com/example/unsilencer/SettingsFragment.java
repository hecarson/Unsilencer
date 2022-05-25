package com.example.unsilencer;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragmentCompat {

    private NotificationManager notifManager;
    private AlarmManager alarmManager;
    private ActionsViewModel actionsViewModel;
    private SwitchPreference notifPolicyPref;
    private SwitchPreference exactAlarmPref;
    private ActivityResultLauncher<Intent> notifPolicySettingsLauncher;
    private ActivityResultLauncher<Intent> exactAlarmSettingsLauncher;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        notifManager = requireContext().getSystemService(NotificationManager.class);
        alarmManager = requireContext().getSystemService(AlarmManager.class);
        actionsViewModel = new ViewModelProvider(requireActivity()).get(ActionsViewModel.class);

        notifPolicyPref = findPreference("notifPolicyPerm");
        exactAlarmPref = findPreference("exactAlarmPerm");
        assert notifPolicyPref != null && exactAlarmPref != null;

        // set checked states according to permissions
        notifPolicyPref.setChecked(notifManager.isNotificationPolicyAccessGranted());
        if (Build.VERSION.SDK_INT < 31)
            exactAlarmPref.setChecked(true);
        else
            exactAlarmPref.setChecked(alarmManager.canScheduleExactAlarms());

        // register click and change listeners
        notifPolicyPref.setOnPreferenceClickListener(this::onNotifPolicyPrefClicked);
        exactAlarmPref.setOnPreferenceClickListener(this::onExactAlarmPrefClicked);

        // make settings launchers here since registerForActivityResult can only be called during initialization
        notifPolicySettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onNotifPolicySettingActivityResult
        );
        exactAlarmSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onExactAlarmSettingActivityResult
        );

        // TODO: temporary debug button
        Preference debugPref = findPreference("debugPref");
        assert debugPref != null;
        debugPref.setOnPreferenceClickListener((preference) -> {

            return true;
        });
    }

    private boolean onNotifPolicyPrefClicked(Preference preference) {
        Intent changeNotifPolicyPermIntent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        notifPolicySettingsLauncher.launch(changeNotifPolicyPermIntent);
        return true;
    }

    private boolean onExactAlarmPrefClicked(Preference preference) {
        // switch for exact alarm pref will already be checked
        if (Build.VERSION.SDK_INT < 31)
            return true;

        Intent changeExactAlarmPermIntent = new Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:" + requireContext().getPackageName())
        );
        exactAlarmSettingsLauncher.launch(changeExactAlarmPermIntent);
        return true;
    }

    private void onNotifPolicySettingActivityResult(ActivityResult activityResult) {
        notifPolicyPref.setChecked(notifManager.isNotificationPolicyAccessGranted());
    }

    private void onExactAlarmSettingActivityResult(ActivityResult activityResult) {
        // this shouldn't be called if API level < 31, so this check is to remove the lint warning
        if (Build.VERSION.SDK_INT < 31)
            return;

        if (alarmManager.canScheduleExactAlarms()) {
            exactAlarmPref.setChecked(true);
            // ensure all actions are scheduled
            actionsViewModel.removeAndReaddAllActions();
        }
    }

}