package com.eveningoutpost.dexdrip.reports;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.R;
import com.eveningoutpost.dexdrip.profileeditor.DatePickerFragment;
import com.eveningoutpost.dexdrip.utils.ActivityWithMenu;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends ActivityWithMenu {
    private Spinner periodSpinner;
    private LinearLayout periodContainer;
    private Button periodStartBtn;
    private Button periodEndBtn;
    private EditText cautions;
    private Spinner eventTypesSpinner;
    private EditText glucoseRangeDown;
    private EditText glucoseRangeUp;
    private Spinner sortOrderSpinner;
    private CheckBox mondayCB;
    private CheckBox tuesdayCB;
    private CheckBox wednesdayCB;
    private CheckBox thursdayCB;
    private CheckBox fridayCB;
    private CheckBox saturdayCB;
    private CheckBox sundayCB;
    private static SharedPreferences prefs;


    private ArrayAdapter<CharSequence> periodList;
    private Calendar startDate;
    private Calendar endDate;

    @Override
    public String getMenuName() {
        return getString(R.string.reports);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setContentView(R.layout.activity_report);
        setupControls();
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        setupDates(0);
        updateBtnText(periodStartBtn, startDate);
        updateBtnText(periodEndBtn, endDate);
        glucoseRangeDown.setText(prefs.getString("lowValue", "70"));
        glucoseRangeUp.setText(prefs.getString("highValue", "70"));
    }

    private void setupControls() {
        periodSpinner = findViewById(R.id.period_spinner);
        periodContainer = findViewById(R.id.report_period_container);
        periodStartBtn = findViewById(R.id.report_period_start_btn);
        periodEndBtn = findViewById(R.id.report_period_end_btn);
        cautions = findViewById(R.id.report_cautions_et);
        eventTypesSpinner = findViewById(R.id.event_type_spinner);
        glucoseRangeDown = findViewById(R.id.report_glucose_alert_down);
        glucoseRangeUp = findViewById(R.id.report_glucose_alert_up);
        sortOrderSpinner = findViewById(R.id.report_sort_order_spinner);
        mondayCB = findViewById(R.id.mondayCB);
        tuesdayCB = findViewById(R.id.tuesdayCB);
        wednesdayCB = findViewById(R.id.wednesdayCB);
        thursdayCB = findViewById(R.id.thursdayCB);
        fridayCB = findViewById(R.id.fridayCB);
        saturdayCB = findViewById(R.id.saturdayCB);
        sundayCB = findViewById(R.id.sundayCB);
        setupPeriodSpinner();
        setupSortOrderSpinner();
        setupEventTypeSpinner();
    }

    private void setupEventTypeSpinner() {
        ArrayAdapter<CharSequence> eventTypes = ArrayAdapter.createFromResource(this,
                R.array.reportEventTypes, android.R.layout.simple_spinner_item);
        eventTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypesSpinner.setAdapter(eventTypes);
        eventTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupSortOrderSpinner() {
        ArrayAdapter<CharSequence> sortOrder = ArrayAdapter.createFromResource(this,
                R.array.reportSortOrder, android.R.layout.simple_spinner_item);
        sortOrder.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortOrderSpinner.setAdapter(sortOrder);
        sortOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupPeriodSpinner() {
        periodList = ArrayAdapter.createFromResource(this,
                R.array.reportPeriods, android.R.layout.simple_spinner_item);
        periodList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(periodList);
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                if (index < (periodList.getCount() - 1)) {
                    periodContainer.setVisibility(View.GONE);
                } else {
                    periodContainer.setVisibility(View.VISIBLE);
                }
                setupDates(index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupDates(int index) {
        startDate.setTime(new Date());
        endDate.setTime(new Date());
        switch (index) {
            case 1:
                startDate.add(Calendar.DAY_OF_MONTH, -2);
                break;
            case 2:
                startDate.add(Calendar.DAY_OF_MONTH, -3);
                break;
            case 3:
                startDate.add(Calendar.WEEK_OF_MONTH, -1);
                break;
            case 4:
                startDate.add(Calendar.WEEK_OF_MONTH, -2);
                break;
            case 5:
                startDate.add(Calendar.MONTH, -1);
                break;
            case 6:
                startDate.add(Calendar.MONTH, -3);
                break;
            default:
                startDate.setTime(new Date());
                break;
        }
    }

    private void trimToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void trimToEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    private void updateBtnText(Button button, Calendar calendar) {
        button.setText(getString(R.string.date_format, extendDate(calendar.get(Calendar.YEAR)), extendDate(calendar.get(Calendar.MONTH) + 1), extendDate(calendar.get(Calendar.DAY_OF_MONTH))));
    }

    private String extendDate(int date) {
        return date < 10 ? "0" + date : "" + date;
    }

    public void showStartPeriodPicker(View view) {
        final DatePickerFragment datePickerFragment = new DatePickerFragment();

        datePickerFragment.setAllowFuture(false);
        datePickerFragment.setInitiallySelectedDate(startDate.getTimeInMillis());
        datePickerFragment.setTitle(getString(R.string.report_period_start_title));
        datePickerFragment.setDateCallback((year, month, day) -> {
            startDate.set(year, month, day);
            updateBtnText(periodStartBtn, startDate);
        });
        datePickerFragment.show(this.getFragmentManager(), "DatePickerStart");
    }


    public void showEndPeriodPicker(View view) {
        final DatePickerFragment datePickerFragment = new DatePickerFragment();

        datePickerFragment.setAllowFuture(false);
        datePickerFragment.setInitiallySelectedDate(endDate.getTimeInMillis());
        datePickerFragment.setTitle(getString(R.string.report_period_end_title));
        datePickerFragment.setDateCallback((year, month, day) -> {
            endDate.set(year, month, day);
            updateBtnText(periodEndBtn, endDate);
        });
        datePickerFragment.show(this.getFragmentManager(), "DatePickerEnd");
    }

    public void generateReport(View view) {

    }
}
