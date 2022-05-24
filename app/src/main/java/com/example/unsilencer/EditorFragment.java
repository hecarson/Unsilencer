package com.example.unsilencer;

import android.app.TimePickerDialog;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class EditorFragment extends Fragment {

    public interface SettingsNavigator {
        void navigateToSettings();
    }

    private ActionsViewModel actionsViewModel;
    private Menu optionsMenu;

    // --- Initialization ---

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set callback for add action button
        View addActionButton = requireActivity().findViewById(R.id.addActionButton);
        addActionButton.setOnClickListener(this::onAddActionButtonClicked);

        // register updateActionRow to listen to the actions LiveData because updateActionRows needs to have view initialized first
        // registering updateActionRows seems to also call it, so this will also fill the view with the actions in the ViewModel
        actionsViewModel = new ViewModelProvider(requireActivity()).get(ActionsViewModel.class);
        actionsViewModel.getActionsLiveData().observe(requireActivity(), this::updateActionRows);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        optionsMenu = menu;
        setUpOptionsMenuNormal();
    }

    private void setUpOptionsMenuNormal() {
        optionsMenu.clear();
        MenuInflater menuInflater = requireActivity().getMenuInflater();
        menuInflater.inflate(R.menu.editor_menu, optionsMenu);

        optionsMenu.findItem(R.id.settingsMenuItem).setOnMenuItemClickListener((item) -> {
            ( (SettingsNavigator)requireActivity() ).navigateToSettings();
            return false;
        });
    }

    private void setUpOptionsMenuSelect() {
        optionsMenu.clear();
        MenuInflater menuInflater = requireActivity().getMenuInflater();
        menuInflater.inflate(R.menu.editor_menu_select, optionsMenu);

        optionsMenu.findItem(R.id.deleteMenuItem).setOnMenuItemClickListener((menuItem) -> {
            actionsViewModel.removeActions(getCheckedActionIndices());
            setUpOptionsMenuNormal();
            return false;
        });
    }

    // --- Logic ---

    private void updateActionRows(List<Action> actions) {
        // clear the actions layout
        LinearLayout actionsLinearLayout = requireActivity().findViewById(R.id.actionsLinearLayout);
        actionsLinearLayout.removeAllViews();
        LayoutInflater layoutInflater = getLayoutInflater();

        // add new rows according to the actions list
        for (Action action : actions) {
            // make a new row
            View actionRowLayout = layoutInflater.inflate(R.layout.action_row, actionsLinearLayout, false);
            actionsLinearLayout.addView(actionRowLayout);

            CheckBox checkbox = actionRowLayout.findViewById(R.id.checkbox);
            EditText timeLabel = actionRowLayout.findViewById(R.id.timeLabel);
            ImageButton ringerModeButton = actionRowLayout.findViewById(R.id.ringerModeButton);

            // register callbacks
            checkbox.setOnCheckedChangeListener(this::onCheckboxChanged);
            timeLabel.setOnClickListener(this::onTimeLabelClicked);
            ringerModeButton.setOnClickListener(this::onRingerModeButtonClicked);

            // set data in row
            timeLabel.setText(hourAndMinuteTo12HourStr(action.hour, action.minute));
            ringerModeButton.setImageResource(getImageIdFromRingerMode(action.ringerMode));
        }
    }

    private String hourAndMinuteTo12HourStr(int hour, int minute) {
        boolean isAm = hour <= 11;
        String hourStr;

        if (hour == 0 || hour == 12)
            hourStr = "12";
        else if (!isAm)
            hourStr = Integer.toString(hour - 12);
        else
            hourStr = Integer.toString(hour);

        String minuteStr = String.format(Locale.ENGLISH, "%02d", minute);

        return hourStr + ":" + minuteStr + " " + (isAm ? "am" : "pm");
    }

    private int getImageIdFromRingerMode(int ringerMode) {
        switch (ringerMode) {
            case AudioManager.RINGER_MODE_SILENT:
                return R.drawable.ic_baseline_volume_off_24;
            case AudioManager.RINGER_MODE_VIBRATE:
                return R.drawable.ic_baseline_vibration_24;
            case AudioManager.RINGER_MODE_NORMAL:
                return R.drawable.ic_baseline_volume_up_24;
        }

        return -1;
    }

    private void onCheckboxChanged(CompoundButton checkbox, boolean isChecked) {
        int numCheckedBoxes = getCheckedActionIndices().size();

        if (numCheckedBoxes == 0)
            setUpOptionsMenuNormal();
        else if (numCheckedBoxes == 1)
            setUpOptionsMenuSelect();
    }

    private List<Integer> getCheckedActionIndices() {
        ArrayList<Integer> checkedActionIndices = new ArrayList<>();
        LinearLayout actionsLinearLayout = requireActivity().findViewById(R.id.actionsLinearLayout);

        for (int i = 0; i < actionsLinearLayout.getChildCount(); ++i) {
            View actionRowLayout = actionsLinearLayout.getChildAt(i);
            CheckBox curCheckbox = actionRowLayout.findViewById(R.id.checkbox);

            if (curCheckbox.isChecked())
                checkedActionIndices.add(i);
        }

        return checkedActionIndices;
    }

    private void onAddActionButtonClicked(View addActionButton) {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.setTimeInMillis(System.currentTimeMillis());

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (timePickerView, hour, minute) ->
                        actionsViewModel.addAction(hour, minute, AudioManager.RINGER_MODE_SILENT),
                nowCalendar.get(Calendar.HOUR_OF_DAY), nowCalendar.get(Calendar.MINUTE), false
        );
        timePickerDialog.show();
    }

    private void onTimeLabelClicked(View timeLabel) {
        ViewParent actionRowLayout = timeLabel.getParent();
        LinearLayout actionsLinearLayout = requireActivity().findViewById(R.id.actionsLinearLayout);
        int actionIndex = actionsLinearLayout.indexOfChild((View)actionRowLayout);
        Action action = actionsViewModel.getAction(actionIndex);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (timePickerView, hour, minute) ->
                        actionsViewModel.setAction(actionIndex, hour, minute, action.ringerMode),
                action.hour, action.minute, false
        );
        timePickerDialog.show();
    }

    private void onRingerModeButtonClicked(View actionButton) {
        ViewParent actionRowLayout = actionButton.getParent();
        LinearLayout actionsLinearLayout = requireActivity().findViewById(R.id.actionsLinearLayout);
        int actionIndex = actionsLinearLayout.indexOfChild((View)actionRowLayout);
        Action action = actionsViewModel.getAction(actionIndex);

        int newRingerMode = -1;

        // toggle ringer mode
        switch (action.ringerMode) {
            case AudioManager.RINGER_MODE_SILENT:
                newRingerMode = AudioManager.RINGER_MODE_VIBRATE;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                newRingerMode = AudioManager.RINGER_MODE_NORMAL;
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                newRingerMode = AudioManager.RINGER_MODE_SILENT;
                break;
        }

        actionsViewModel.setAction(actionIndex, action.hour, action.minute, newRingerMode);
    }

}