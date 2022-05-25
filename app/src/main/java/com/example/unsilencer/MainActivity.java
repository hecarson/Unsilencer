package com.example.unsilencer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements EditorFragment.SettingsNavigator {

    private ActionsViewModel actionsViewModel;
    private ActionScheduler actionScheduler;
    private NavController navController;

    // --- Initialization ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        actionScheduler = new ActionScheduler(this);

        // initialize the ActionsViewModel
        //
        // the ActionsViewModel needs to be initialized before inflating layouts, since inflating EditorFragment needs
        // the ActionsViewModel initialized beforehand
        actionsViewModel = new ViewModelProvider(this, new ActionsViewModelFactory()).get(ActionsViewModel.class);
        actionsViewModel.getRemovedActionRequestCodesLiveData().observe(this, actionScheduler::cancelRemovedActions);
        actionsViewModel.getAddedActionsLiveData().observe(this, actionScheduler::addAlarmsForActions);

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
            return (T) new ActionsViewModel(getFilesDir().getAbsolutePath());
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