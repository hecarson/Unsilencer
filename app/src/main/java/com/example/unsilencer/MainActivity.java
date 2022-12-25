package com.example.unsilencer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements EditorFragment.SettingsNavigator {

    private RingerModeSettingViewModel ringerModeSettingViewModel;
    private RingerModeSettingScheduler actionScheduler;
    private NavController navController;

    public static final String LOG_TAG = "Unsilencer";

    // --- Initialization ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "activity started");

        actionScheduler = new RingerModeSettingScheduler(this);

        // initialize the ActionsViewModel
        //
        // the ActionsViewModel needs to be initialized before inflating layouts, since inflating EditorFragment needs
        // the ActionsViewModel initialized beforehand
        ringerModeSettingViewModel = new ViewModelProvider(this, new ActionsViewModelFactory()).get(RingerModeSettingViewModel.class);
        ringerModeSettingViewModel.getRemovedActionRequestCodesLiveData().observe(this, actionScheduler::cancelRemovedRingerModeSettings);
        ringerModeSettingViewModel.getAddedSettingsLiveData().observe(this, actionScheduler::addAlarmsForRingerModeSettings);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHost = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        assert navHost != null;
        navController = navHost.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    /**
     * Factory is needed, because ActionsViewModel has no default constructor
     */
    @SuppressWarnings("unchecked")
    private class ActionsViewModelFactory implements ViewModelProvider.Factory {
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new RingerModeSettingViewModel(getFilesDir().getAbsolutePath());
        }
    }

    // --- Logic ---

    @Override
    public void navigateToSettings() {
        navController.navigate(R.id.action_editorFragment_to_settingsFragment);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            navController.navigateUp();

        return super.onOptionsItemSelected(item);
    }

}