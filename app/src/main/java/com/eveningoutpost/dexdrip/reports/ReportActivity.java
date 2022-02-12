package com.eveningoutpost.dexdrip.reports;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.eveningoutpost.dexdrip.BuildConfig;
import com.eveningoutpost.dexdrip.Models.BgReading;
import com.eveningoutpost.dexdrip.Models.UserError;
import com.eveningoutpost.dexdrip.R;
import com.eveningoutpost.dexdrip.profileeditor.DatePickerFragment;
import com.eveningoutpost.dexdrip.stats.BgReadingStats;
import com.eveningoutpost.dexdrip.stats.DBSearchUtil;
import com.eveningoutpost.dexdrip.utils.ActivityWithMenu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportActivity extends ActivityWithMenu {
    private static final String TAG = "ReportActivity";

    private Intent mShareIntent;
    private OutputStream os;

    private Spinner periodSpinner;
    private LinearLayout periodContainer;
    private Button periodStartBtn;
    private Button periodEndBtn;
    private EditText glucoseRangeDown;
    private EditText glucoseRangeUp;
    private CheckBox mondayCB;
    private CheckBox tuesdayCB;
    private CheckBox wednesdayCB;
    private CheckBox thursdayCB;
    private CheckBox fridayCB;
    private CheckBox saturdayCB;
    private CheckBox sundayCB;
    private static SharedPreferences prefs;
    private Context context;


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
        context = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        setContentView(R.layout.activity_report);
        setupControls();
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        setupDates(0);
        updateBtnText(periodStartBtn, startDate);
        updateBtnText(periodEndBtn, endDate);
        glucoseRangeDown.setText(prefs.getString("lowValue", "70"));
        glucoseRangeUp.setText(prefs.getString("highValue", "170"));

    }

    private void shareDocument(Uri uri) {
        mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("application/pdf");
        // Assuming it may go via eMail:
        mShareIntent.putExtra(Intent.EXTRA_SUBJECT, "Here is a PDF from PdfSend");
        // Attach the PDf as a Uri, since Android can't take it as bytes yet.
        mShareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(mShareIntent);
    }

    private void setupControls() {
        periodSpinner = findViewById(R.id.period_spinner);
        periodContainer = findViewById(R.id.report_period_container);
        periodStartBtn = findViewById(R.id.report_period_start_btn);
        periodEndBtn = findViewById(R.id.report_period_end_btn);
        glucoseRangeDown = findViewById(R.id.report_glucose_alert_down);
        glucoseRangeUp = findViewById(R.id.report_glucose_alert_up);
        mondayCB = findViewById(R.id.mondayCB);
        tuesdayCB = findViewById(R.id.tuesdayCB);
        wednesdayCB = findViewById(R.id.wednesdayCB);
        thursdayCB = findViewById(R.id.thursdayCB);
        fridayCB = findViewById(R.id.fridayCB);
        saturdayCB = findViewById(R.id.saturdayCB);
        sundayCB = findViewById(R.id.sundayCB);
        setupPeriodSpinner();

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
        trimToMidnight(startDate);
        trimToEndOfDay(endDate);
        DBSearchUtil.Bounds bounds = new DBSearchUtil.Bounds(startDate.getTimeInMillis(), endDate.getTimeInMillis());
        List<BgReadingStats> statsInRange = filterStatsByDay(DBSearchUtil.getReadingsInRange(context, bounds));
        List<BgReadingStats> statsBelowRange = filterStatsByDay(DBSearchUtil.getReadingsBelowRange(context, bounds));
        List<BgReadingStats> statsAboveRange = filterStatsByDay(DBSearchUtil.getReadingsAboveRange(context, bounds));
        int numberOfStatsBelow = DBSearchUtil.noReadingsBelowRange(context, bounds);
        int numberOfStatsInRange = DBSearchUtil.noReadingsInRange(context, bounds);
        int numberOfStatsAbove = DBSearchUtil.noReadingsAboveRange(context, bounds);
        Log.i(TAG, String.format("Number of stats in range from readings %s from count %s", statsInRange.size(), numberOfStatsInRange));
        Log.i(TAG, String.format("Number of stats below range from readings %s from count %s", statsBelowRange.size(), numberOfStatsBelow));
        Log.i(TAG, String.format("Number of stats above range from readings %s from count %s", statsAboveRange.size(), numberOfStatsAbove));
        /*long aboveRange = DBSearchUtil.noReadingsAboveRange(context, bounds);
        long belowRange = DBSearchUtil.noReadingsBelowRange(context, bounds);
        long inRange = DBSearchUtil.noReadingsInRange(context, bounds);
        long total = aboveRange + belowRange + inRange;
        if (total == 0) {
            total = Long.MAX_VALUE;
        }
        int abovePercent = (int) (aboveRange * 100.0 / total + 0.5);
        int belowPercent = (int) (belowRange * 100.0 / total + 0.5);
        int inPercent = 100 - abovePercent - belowPercent;*/
    }

    private List<BgReadingStats> filterStatsByDay(List<BgReadingStats> statsList) {
        List<BgReadingStats> statsFiltered = new ArrayList<>();
        for(BgReadingStats stat : statsList) {
            Calendar now = Calendar.getInstance();
            now.setTimeInMillis(stat.timestamp);
            if(now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && mondayCB.isChecked()){
                statsFiltered.add(stat);
            }else if(now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && mondayCB.isChecked()){
                statsFiltered.add(stat);
            }else if(now.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY && tuesdayCB.isChecked()){
                statsFiltered.add(stat);
            }else if(now.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY && wednesdayCB.isChecked()){
                statsFiltered.add(stat);
            }else if(now.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY&& thursdayCB.isChecked()){
                statsFiltered.add(stat);
            }else if(now.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && fridayCB.isChecked()){
                statsFiltered.add(stat);
            }else if(now.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && saturdayCB.isChecked()){
                statsFiltered.add(stat);
            }else if(now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && sundayCB.isChecked()){
                statsFiltered.add(stat);
            }
        }
        return statsFiltered;
    }

    private class ReportStats {
        private int totalResults;
        private double averageReading;
        private double median;
        private double standardDeviation;

        public ReportStats() {

        }

        public int getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(int totalResults) {
            this.totalResults = totalResults;
        }

        public double getAverageReading() {
            return averageReading;
        }

        public void setAverageReading(double averageReading) {
            this.averageReading = averageReading;
        }

        public double getMedian() {
            return median;
        }

        public void setMedian(double median) {
            this.median = median;
        }

        public double getStandardDeviation() {
            return standardDeviation;
        }

        public void setStandardDeviation(double standardDeviation) {
            this.standardDeviation = standardDeviation;
        }
    }
}
