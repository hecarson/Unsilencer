package com.example.unsilencer;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

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
    private PowerManager powerManager;
    private RingerModeSettingViewModel ringerModeSettingViewModel;
    private SwitchPreference notifPolicyPref;
    private SwitchPreference exactAlarmPref;
    private SwitchPreference ignoreOptimizationsPref;
    private ActivityResultLauncher<Intent> notifPolicySettingsLauncher;
    private ActivityResultLauncher<Intent> exactAlarmSettingsLauncher;
    private ActivityResultLauncher<Intent> ignoreOptimizationsSettingsLauncher;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        notifManager = requireContext().getSystemService(NotificationManager.class);
        alarmManager = requireContext().getSystemService(AlarmManager.class);
        powerManager = requireContext().getSystemService(PowerManager.class);
        ringerModeSettingViewModel = new ViewModelProvider(requireActivity()).get(RingerModeSettingViewModel.class);

        notifPolicyPref = findPreference("notifPolicyPerm");
        exactAlarmPref = findPreference("exactAlarmPerm");
        ignoreOptimizationsPref = findPreference("ignoreOptimizationsPerm");
        assert notifPolicyPref != null && exactAlarmPref != null && ignoreOptimizationsPref != null;

        // set checked states according to permissions
        notifPolicyPref.setChecked(notifManager.isNotificationPolicyAccessGranted());
        if (Build.VERSION.SDK_INT < 31)
            exactAlarmPref.setChecked(true);
        else
            exactAlarmPref.setChecked(alarmManager.canScheduleExactAlarms());
        ignoreOptimizationsPref.setChecked(powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName()));

        // register click and change listeners
        notifPolicyPref.setOnPreferenceClickListener(this::onNotifPolicyPrefClicked);
        exactAlarmPref.setOnPreferenceClickListener(this::onExactAlarmPrefClicked);
        ignoreOptimizationsPref.setOnPreferenceClickListener(this::onIgnoreOptimizationsPrefClicked);

        // make settings launchers here since registerForActivityResult can only be called during initialization
        notifPolicySettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onNotifPolicySettingActivityResult
        );
        exactAlarmSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onExactAlarmSettingActivityResult
        );
        ignoreOptimizationsSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onIgnoreOptimizationsSettingActivityResult
        );

        Preference debugPref = findPreference("debugPref");
        assert debugPref != null;
        debugPref.setOnPreferenceClickListener(this::onDebugPrefClicked);
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

    private boolean onIgnoreOptimizationsPrefClicked(Preference preference) {
        Intent ignoreOptimizationsIntent = new Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:" + requireContext().getPackageName())
        );
        ignoreOptimizationsSettingsLauncher.launch(ignoreOptimizationsIntent);

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
            ringerModeSettingViewModel.removeAndReaddAllActions();
        }
        else
            exactAlarmPref.setChecked(false);
    }

    private void onIgnoreOptimizationsSettingActivityResult(ActivityResult activityResult) {
        ignoreOptimizationsPref.setChecked(
                powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName())
        );
    }

    private boolean onDebugPrefClicked(Preference preference) {
        PowerManager powerManager = requireContext().getSystemService(PowerManager.class);
        Log.d(MainActivity.LOG_TAG, "power manager ignoring optimizations: " +
                powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName())
        );

        return true;
    }

}