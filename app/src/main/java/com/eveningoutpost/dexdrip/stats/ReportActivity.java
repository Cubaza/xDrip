package com.eveningoutpost.dexdrip.stats;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.Models.UserError;
import com.eveningoutpost.dexdrip.R;
import com.eveningoutpost.dexdrip.profileeditor.DatePickerFragment;
import com.eveningoutpost.dexdrip.stats.BgReadingStats;
import com.eveningoutpost.dexdrip.stats.DBSearchUtil;
import com.eveningoutpost.dexdrip.stats.StatsResult;
import com.eveningoutpost.dexdrip.utils.ActivityWithMenu;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ReportActivity extends ActivityWithMenu {
    private static final String TAG = "ReportActivity";


    private Spinner periodSpinner;
    private LinearLayout periodContainer;
    private Button periodStartBtn;
    private Button periodEndBtn;


    private ArrayAdapter<CharSequence> periodList;
    private GregorianCalendar startDate;
    private GregorianCalendar endDate;
    private SharedPreferences prefs;
    private String fileName;
    private String targetFile;
    private final static int MY_PERMISSIONS_REQUEST_STORAGE_SCREENSHOT = 106;

    @Override
    public String getMenuName() {
        return getString(R.string.reports);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setupControls();
        startDate = new GregorianCalendar();
        endDate = new GregorianCalendar();
        setupDates(0);
        updateBtnText(periodStartBtn, startDate);
        updateBtnText(periodEndBtn, endDate);
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

    }

    private void setupControls() {
        periodSpinner = findViewById(R.id.period_spinner);
        periodContainer = findViewById(R.id.report_period_container);
        periodStartBtn = findViewById(R.id.report_period_start_btn);
        periodEndBtn = findViewById(R.id.report_period_end_btn);
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
        button.setText(setDate(calendar));
    }

    private String setDate(Calendar calendar) {
        return getString(R.string.date_format, extendDate(calendar.get(Calendar.YEAR)), extendDate(calendar.get(Calendar.MONTH) + 1), extendDate(calendar.get(Calendar.DAY_OF_MONTH)));
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
        if(checkPermissions()){
            final Activity context = this;
            JoH.runOnUiThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    trimToMidnight(startDate);
                    trimToEndOfDay(endDate);
                    generateFilePath();
                    StatsResult stats = new StatsResult(prefs, startDate.getTimeInMillis(), endDate.getTimeInMillis());
                    generateResultPDF(stats);
                    JoH.sharePDFFile(context, new File(targetFile));
                }
            }, 250);
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE_SCREENSHOT);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE_SCREENSHOT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateReport(null);
            } else {
                JoH.static_toast_long(this, "Cannot save screenshot without permission");
            }
        }
    }

    private String generateFilePath() {
        targetFile =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/";
        fileName = "xDrip-report-"
                + JoH.dateTimeText(JoH.tsl()).replace(" ", "-").replace(":", "-").replace(".", "-")
                + ".pdf";
        targetFile+=fileName;
        return targetFile;
    }

    private void generateResultPDF(StatsResult stats){
        Document document = new Document(PageSize.A4);
        try {

            PdfWriter.getInstance(document, new FileOutputStream(targetFile));
            document.open();

            Font titleHeader = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD);
            Paragraph paragraph = new Paragraph(getPDFTitle(), titleHeader);
            paragraph.setSpacingAfter(20f);
            document.add(paragraph);
            if(stats.getTotalReadings() > 0 ) {
                Font fontHeader = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
                String[] headers = setHeaders();
                PdfPTable table = new PdfPTable(headers.length);
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell();
                    cell.setGrayFill(0.9f);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPhrase(new Phrase(header.toUpperCase(), fontHeader));
                    table.addCell(cell);
                }
                table.completeRow();
                addLowRow(stats, table);
                addInRange(stats, table);
                addAboveRange(stats, table);
                addOverall(stats, table);
                document.add(table);
                addStatsValue(stats, document);
                addChart(stats, document);
            }


            document.addTitle(fileName);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    private void addStatsValue(StatsResult stats, Document document) throws DocumentException{
        String gvi = stats.getGVI();
        String[] gviSplit = gvi.split(" ");
        String gviValue = gviSplit[0].substring("gvi:".length());
        String pgsValue = gviSplit[1].substring("pgs:".length());
        Font titleHeader = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
        Paragraph paragraph = new Paragraph(getString(R.string.report_gvi_value, gviValue));
        paragraph.setSpacingBefore(12f);
        paragraph.setFont(titleHeader);
        document.add(paragraph);
        paragraph = new Paragraph(getString(R.string.report_pgs_value, pgsValue));
        paragraph.setSpacingBefore(4f);
        paragraph.setFont(titleHeader);
        document.add(paragraph);
    }

    private void addChart(StatsResult stats, Document document) throws DocumentException, IOException {
        Bitmap bitmap = Bitmap.createBitmap( 300, 300, Bitmap.Config.ARGB_4444 );
        bitmap.setHasAlpha(true);
        Canvas canvas = new Canvas(bitmap);
        RectF rect = new RectF(10, 10, 290, 290);

        Paint myPaint = new Paint();
        myPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        myPaint.setAntiAlias(true);


        float inDeg = stats.getIn() * 360f / (stats.getTotalReadings());
        float lowDeg = stats.getBelow() * 360f / (stats.getTotalReadings());
        float highDeg = stats.getAbove() * 360f / (stats.getTotalReadings());

        myPaint.setColor(android.graphics.Color.RED);
        canvas.drawArc(rect, -90, lowDeg, true, myPaint);
        myPaint.setColor(Color.GREEN);
        canvas.drawArc(rect, -90 + lowDeg, inDeg, true, myPaint);
        myPaint.setColor(Color.YELLOW);
        canvas.drawArc(rect, -90 + lowDeg + inDeg, highDeg, true, myPaint);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image myImg = Image.getInstance(stream.toByteArray());
        myImg.setAlignment(Image.MIDDLE);
        document.add(myImg);
    }

    private void addStatsToTable(int value, int total, List<BgReadingStats> stats, PdfPTable table) {
        if(value > 0) {
            double lowResults = ( (double)value / (double)total) * 100;
            addCell(parseValue(lowResults)+"%", table);
            addCell(""+value, table);
            addCell(""+parseValue(calculateAverage(stats)), table);
            addCell(""+parseValue(calculateMedian(stats)), table);
            addCell(""+parseValue(calculateStdDev(stats)), table);
        }else {
            addCell("0", table);
            addCell("0", table);
            addCell("N/A", table);
            addCell("N/A", table);
            addCell("N/A", table);
        }

        table.completeRow();
    }

    private void addLowRow(StatsResult stats, PdfPTable table) {
        addCell(getString(R.string.report_low, prefs.getString("lowValue", "70")), table, BaseColor.RED);
        List<BgReadingStats> statsBelowRange = DBSearchUtil.getReadingsBelowRange(
                getApplicationContext(),
                new DBSearchUtil.Bounds(startDate.getTimeInMillis(), endDate.getTimeInMillis())
        );
        addStatsToTable(stats.getBelow(), stats.getTotalReadings(), statsBelowRange, table);
    }
    private void addInRange(StatsResult stats, PdfPTable table) {
        addCell(getString(R.string.report_range), table, BaseColor.GREEN);
        List<BgReadingStats> inRange = DBSearchUtil.getReadingsInRange(
                getApplicationContext(),
                new DBSearchUtil.Bounds(startDate.getTimeInMillis(), endDate.getTimeInMillis())
        );
        addStatsToTable(stats.getIn(), stats.getTotalReadings(), inRange, table);
    }
    private void addAboveRange(StatsResult stats, PdfPTable table) {
        addCell(getString(R.string.report_high, prefs.getString("highValue", "170")), table, BaseColor.YELLOW);
        List<BgReadingStats> inRange = DBSearchUtil.getReadingsAboveRange(
                getApplicationContext(),
                new DBSearchUtil.Bounds(startDate.getTimeInMillis(), endDate.getTimeInMillis())
        );
        addStatsToTable(stats.getAbove(), stats.getTotalReadings(), inRange, table);
    }

    private void addOverall(StatsResult stats, PdfPTable table) {
        addCell(getString(R.string.report_overall), table, BaseColor.WHITE);
        List<BgReadingStats> statsOverall = DBSearchUtil.getReadings(
                true,
                new DBSearchUtil.Bounds(startDate.getTimeInMillis(), endDate.getTimeInMillis())
        );
        addStatsToTable(stats.getTotalReadings(), stats.getTotalReadings(), statsOverall, table);
    }
    private double calculateStdDev(List<BgReadingStats> statsBelowRange) {
        double sumDoubled = 0;
        double sum = 0;
        for(BgReadingStats stat : statsBelowRange) {
            sumDoubled += (stat.calculated_value * stat.calculated_value);
            sum += stat.calculated_value;
        }
        double top = (statsBelowRange.size()*sumDoubled) - (sum*sum);
        double bottom = (statsBelowRange.size() - 1) * statsBelowRange.size();
        if(top > 0 && bottom > 0) {
            return parseValue(Math.sqrt(top/bottom));
        }
        return parseValue(0);
    }

    private void addCell(String value, PdfPTable table) {
        addCell(value, table, BaseColor.WHITE);
    }

    private void addCell(String value, PdfPTable table, BaseColor baseColor) {
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
        Phrase cellPhrase = new Phrase(value, font);
        PdfPCell cell = new PdfPCell(cellPhrase);
        cell.setBackgroundColor(baseColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
    private double calculateAverage(List<BgReadingStats> statsBelowRange) {
        double average = 0d;
        for(BgReadingStats stat: statsBelowRange){
            average += stat.calculated_value;
        }
        return average / statsBelowRange.size();
    }
    private double calculateMedian(List<BgReadingStats> statsBelowRange) {
        Collections.sort(statsBelowRange, (o1, o2) -> ((Double) o1.calculated_value).compareTo(((Double) o2.calculated_value)));
        if(statsBelowRange.size() %2 == 0) {
            return statsBelowRange.get((statsBelowRange.size() / 2)).calculated_value;
        }else {
            if(statsBelowRange.size() > 1) {
                 double first = statsBelowRange.get(statsBelowRange.size() / 2).calculated_value;
                 double second = statsBelowRange.get((statsBelowRange.size() / 2) + 1).calculated_value;
                 return (double)((first / second) / 2);
            }else {
                return statsBelowRange.get(0).calculated_value;
            }

        }
    }

    private String[] setHeaders() {
        return new String[]{getString(R.string.report_range),
                getString(R.string.report_percentage_of_readings),
                getString(R.string.report_no_of_readings),
                getString(R.string.report_average),
                getString(R.string.report_median),
                getString(R.string.report_standard_deviation)
        };
    }
    private String getPDFTitle() {
        return getString(R.string.report_pdf_range_title, setDate(startDate), setDate(endDate));
    }

    private double parseValue(double value){
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
