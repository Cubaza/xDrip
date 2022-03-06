package com.eveningoutpost.dexdrip.reports;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.eveningoutpost.dexdrip.R;
import com.eveningoutpost.dexdrip.profileeditor.DatePickerFragment;
import com.eveningoutpost.dexdrip.stats.BgReadingStats;
import com.eveningoutpost.dexdrip.stats.DBSearchUtil;
import com.eveningoutpost.dexdrip.utils.ActivityWithMenu;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ReportActivity extends ActivityWithMenu {
    private static final String TAG = "ReportActivity";
    private Intent mShareIntent;
    private OutputStream os;

    private Spinner periodSpinner;
    private LinearLayout periodContainer;
    private Button periodStartBtn;
    private Button periodEndBtn;
    private CheckBox mondayCB;
    private CheckBox tuesdayCB;
    private CheckBox wednesdayCB;
    private CheckBox thursdayCB;
    private CheckBox fridayCB;
    private CheckBox saturdayCB;
    private CheckBox sundayCB;
    private TextView reportResultTV;
    private Context context;


    private ArrayAdapter<CharSequence> periodList;
    private GregorianCalendar startDate;
    private GregorianCalendar endDate;

    @Override
    public String getMenuName() {
        return getString(R.string.reports);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_report);
        setupControls();
        startDate = new GregorianCalendar();
        endDate = new GregorianCalendar();
        setupDates(0);
        updateBtnText(periodStartBtn, startDate);
        updateBtnText(periodEndBtn, endDate);

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
        mondayCB = findViewById(R.id.report_monday_cb);
        tuesdayCB = findViewById(R.id.report_tuesday_cb);
        wednesdayCB = findViewById(R.id.report_wednesday_cb);
        thursdayCB = findViewById(R.id.report_thursday_cb);
        fridayCB = findViewById(R.id.report_friday_cb);
        saturdayCB = findViewById(R.id.report_saturday_cb);
        sundayCB = findViewById(R.id.report_sunday_cb);
        reportResultTV = findViewById(R.id.report_result_tv);
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
        double minValue = 90000000d;
        double maxValue = 0d;
        DBSearchUtil.Bounds bounds = new DBSearchUtil.Bounds(startDate.getTimeInMillis(), endDate.getTimeInMillis());
        List<BgReadingStats> readingsInRange = filterStatsByDay(DBSearchUtil.getReadingsInRange(context, bounds));
        List<BgReadingStats> readingsBelowRange = filterStatsByDay(DBSearchUtil.getReadingsBelowRange(context, bounds));
        List<BgReadingStats> readingsAboveRange = filterStatsByDay(DBSearchUtil.getReadingsAboveRange(context, bounds));
        List<BgReadingStats> allReadings = DBSearchUtil.getReadings(true, bounds);
        if(allReadings.size() > 0) {
            long allCount = readingsInRange.size() + readingsBelowRange.size() + readingsAboveRange.size();
            StringBuilder builder = new StringBuilder();
            builder.append("Number of results: ");
            builder.append(allCount);
            builder.append("\n");
            builder.append("Reading number below range: ");
            builder.append(readingsBelowRange.size());
            builder.append("\n");
            builder.append("Reading number in range: ");
            builder.append(readingsInRange.size());
            builder.append("\n");
            builder.append("Reading number above range: ");
            builder.append(readingsAboveRange.size());
            builder.append("\n");
            builder.append("Percentage in range: ");
            builder.append(parseValue((((double)readingsInRange.size()/(double)allCount)*100), 1));
            builder.append("%");
            builder.append("\n");
            builder.append("Percentage above range: ");
            builder.append(parseValue((((double)readingsAboveRange.size()/(double)allCount)*100), 1));
            builder.append("%");
            builder.append("\n");
            builder.append("Percentage below range: ");
            builder.append(parseValue((((double)readingsBelowRange.size()/(double)allCount)*100), 1));
            builder.append("%");
            for(BgReadingStats stat : allReadings) {
                if(Double.compare(minValue, stat.calculated_value) > 0) {
                    minValue = stat.calculated_value;
                }
                if(Double.compare(maxValue, stat.calculated_value) < 0) {
                    maxValue = stat.calculated_value;
                }
            }
            builder.append("\n");
            builder.append("Min value: ");
            builder.append(parseValue(minValue, 2));
            builder.append("\n");
            builder.append("Max value: ");
            builder.append(parseValue(maxValue, 2));
            double meanBelowRange = calculateMean(readingsBelowRange);
            double meanInRange = calculateMean(readingsInRange);
            double meanAboveRange = calculateMean(readingsAboveRange);
            double meanWholeStats = calculateMean(allReadings);
            builder.append("\n");
            builder.append("Mean value below range: ");
            builder.append(meanBelowRange);
            builder.append("\n");
            builder.append("Mean value in range: ");
            builder.append(meanInRange);
            builder.append("\n");
            builder.append("Mean value above range: ");
            builder.append(meanAboveRange);
            builder.append("\n");
            builder.append("Mean Overall: ");
            builder.append(meanWholeStats);
            double medianBelowRange = calculateMedian(readingsBelowRange);
            double medianInRange = calculateMedian(readingsInRange);
            double medianAboveRange = calculateMedian(readingsAboveRange);
            double medianWholeRange = calculateMedian(allReadings);
            builder.append("\n");
            builder.append("Median value below range: ");
            builder.append(medianBelowRange);
            builder.append("\n");
            builder.append("Median value in range: ");
            builder.append(medianInRange);
            builder.append("\n");
            builder.append("Median value above range: ");
            builder.append(medianAboveRange);
            builder.append("\n");
            builder.append("Median Overall: ");
            builder.append(medianWholeRange);

            double stdDevBelowRange = calculateStdDev(readingsBelowRange);
            double stdDevInRange = calculateStdDev(readingsInRange);
            double stdDevAboveRange = calculateStdDev(readingsAboveRange);
            double stdDevAllRange = calculateStdDev(allReadings);
            builder.append("\n");
            builder.append("StdDev value below range: ");
            builder.append(stdDevBelowRange);
            builder.append("\n");
            builder.append("StdDev value in range: ");
            builder.append(stdDevInRange);
            builder.append("\n");
            builder.append("StdDev value above range: ");
            builder.append(stdDevAboveRange);
            builder.append("\n");
            builder.append("StdDev Overall: ");
            builder.append(stdDevAllRange);
            double meanTotalDailyChange = calculateMeanTotalDailyChange(allReadings);
            double meanTotalHourlyChange = calculateMeanTotalHourlyChange(allReadings);
            builder.append("\n");
            builder.append("Mean daily change: ");
            builder.append(meanTotalDailyChange);
            builder.append("\n");
            builder.append("Mean hourly change: ");
            builder.append(meanTotalHourlyChange);
            reportResultTV.setText(builder.toString());
        }else {
            reportResultTV.setText("No results in this range");
        }

    }

    private double calculateMeanTotalDailyChange(List<BgReadingStats> allReadings) {
        long daysBetweenDates = calculateDaysBetweenDates();
        if(allReadings.size() > 0) {
            double sum = 0;
            for(BgReadingStats stat : allReadings) {
                sum += stat.calculated_value;
            }
            return sum / daysBetweenDates;
        }
        return parseValue(0d, 2);
    }

    private double calculateMeanTotalHourlyChange(List<BgReadingStats> allReadings) {
        if(allReadings.size() > 0) {
            double sum = 0;
            for(BgReadingStats stat : allReadings) {
                sum += stat.calculated_value;
            }
            return sum / 24;
        }
        return parseValue(0d, 2);
    }

    private long calculateDaysBetweenDates() {
        int daysCount = (int) ((endDate.getTimeInMillis() - startDate.getTimeInMillis()) / (1000 * 60 * 60 * 24));
        return daysCount <= 0 ? 1 : daysCount;
    }

    private double calculateMean(List<BgReadingStats> stats) {
        double mean = 0;
        if(stats.size() > 0) {
            double wholeStats = 0;
            for(BgReadingStats stat : stats) {
                wholeStats += stat.calculated_value;
            }
            mean = wholeStats/stats.size();
        }
        return parseValue(mean, 2);
    }

    private double calculateStdDev(List<BgReadingStats> stats) {
        if(stats.size() > 0) {
            double stdDev = 0;
            double sumDoubled = 0;
            double sum = 0;
            for(BgReadingStats stat : stats) {
                sumDoubled += stat.calculated_value * stat.calculated_value;
                sum += stat.calculated_value;
            }
            double top = (stats.size()*sumDoubled) - (sum*sum);
            double bottom = (stats.size() - 1) * stats.size();
            return parseValue(Math.sqrt(top/bottom), 2);
        }
        return parseValue(0d, 2);
    }
    private double calculateMedian(List<BgReadingStats> stats) {
        if(stats.size() > 0){
            return parseValue(stats.get(stats.size() / 2).calculated_value, 2);
        }
        return parseValue(0d, 2);
    }
    private double parseValue(double value, int scale) {
        return BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }
    private List<BgReadingStats> filterStatsByDay(List<BgReadingStats> statsList) {
        List<BgReadingStats> statsFiltered = new ArrayList<>();
        for(BgReadingStats stat : statsList) {
            Calendar now = new GregorianCalendar();
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
}
