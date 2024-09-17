package com.sr.trackcrack.ui.analytics;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import com.sr.trackcrack.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private BarChart barChart;
    private LineChart lineChart;
    private Spinner dateRangeSpinner;
    private TextView summaryText, totalInspections, inspectionsChange, cracksDetected, cracksChange;
    private Random random = new Random();
    private String currentPeriod = "Monthly";
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_analytics, container, false);

        barChart = root.findViewById(R.id.bar_chart);
        lineChart = root.findViewById(R.id.line_chart);
        dateRangeSpinner = root.findViewById(R.id.date_range_spinner);
        summaryText = root.findViewById(R.id.summary_text);
        totalInspections = root.findViewById(R.id.total_inspections);
        inspectionsChange = root.findViewById(R.id.inspections_change);
        cracksDetected = root.findViewById(R.id.cracks_detected);
        cracksChange = root.findViewById(R.id.cracks_change);

        setupCharts();
        setupDateRangeSpinner();
        updateAnalytics(currentPeriod);

        return root;
    }

    private void setupCharts() {
        setupLineChart();
        setupBarChart();
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);

        XAxis barXAxis = barChart.getXAxis();
        barXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barXAxis.setDrawGridLines(false);
        barXAxis.setGranularity(1f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(true);
    }

    private void setupDateRangeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.date_range_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateRangeSpinner.setAdapter(adapter);

        dateRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPeriod = parent.getItemAtPosition(position).toString();
                if (selectedPeriod.equals("Custom Range")) {
                    showDateRangePicker();
                } else {
                    currentPeriod = selectedPeriod;
                    updateAnalytics(currentPeriod);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void showDateRangePicker() {
        DatePickerDialog.OnDateSetListener startDateListener = (view, year, month, dayOfMonth) -> {
            startDate.set(year, month, dayOfMonth);
            showEndDatePicker();
        };

        new DatePickerDialog(getContext(), startDateListener, startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showEndDatePicker() {
        DatePickerDialog.OnDateSetListener endDateListener = (view, year, month, dayOfMonth) -> {
            endDate.set(year, month, dayOfMonth);
            currentPeriod = "Custom";
            updateAnalytics(currentPeriod);
        };

        new DatePickerDialog(getContext(), endDateListener, endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH), endDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateAnalytics(String period) {
        updateLineChart(period);
        updateBarChart(period);
        updateSummary(period);
        updateCardData(period);
    }

    private void updateLineChart(String period) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int dataPoints = getDataPointsForPeriod(period);
        String labelPrefix = getLabelPrefixForPeriod(period);

        for (int i = 0; i < dataPoints; i++) {
            entries.add(new Entry(i, random.nextInt(50) + 1)); // Number of cracks
            labels.add(getLabelForPeriod(period, i));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Number of Cracks");
        dataSet.setColor(Color.parseColor("#009688"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.parseColor("#009688"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(Math.min(labels.size(), 6));

        lineChart.invalidate();
    }

    private void updateBarChart(String period) {
        List<BarEntry> entries = new ArrayList<>();
        final String[] crackTypes = {"Type A", "Type B", "Type C", "Type D", "Type E"};

        for (int i = 0; i < crackTypes.length; i++) {
            entries.add(new BarEntry(i, random.nextInt(20) + 5));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Crack Types");
        dataSet.setColors(Color.parseColor("#FFAB40"), // Orange 400
                Color.parseColor("#FFD54F"), // Yellow 300
                Color.parseColor("#64B5F6"), // Blue 300
                Color.parseColor("#4DB6AC"), // Teal 300
                Color.parseColor("#BA68C8")); // Purple 300
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(crackTypes));
        xAxis.setLabelCount(crackTypes.length);
        xAxis.setLabelRotationAngle(-45);

        barChart.invalidate();
    }

    private void updateSummary(String period) {
        int totalInspections = random.nextInt(100) + 50;
        int cracksDetected = random.nextInt(50) + 10;
        String mostCommonDefect = "Type " + (char)('A' + random.nextInt(5));
        int defectPercentage = random.nextInt(30) + 20;
        int efficiencyChange = random.nextInt(20) - 10;

        String summary = String.format(Locale.getDefault(),
                "During this %s period, we conducted a total of %d inspections " +
                        "and detected %d cracks. The most common defect was %s, accounting for %d%% of all detected cracks. " +
                        "We've observed a %d%% %s in inspection efficiency compared to the previous %s.",
                period.toLowerCase(), totalInspections, cracksDetected, mostCommonDefect, defectPercentage,
                Math.abs(efficiencyChange), efficiencyChange >= 0 ? "increase" : "decrease", period.toLowerCase());

        summaryText.setText(summary);
    }

    private void updateCardData(String period) {
        int inspections = random.nextInt(100) + 50;
        int cracks = random.nextInt(50) + 10;
        int inspectionChangePercent = random.nextInt(20) - 10;
        int cracksChangePercent = random.nextInt(20) - 10;

        totalInspections.setText(String.valueOf(inspections));
        cracksDetected.setText(String.valueOf(cracks));

        String comparisonPeriod = getComparisonPeriod(period);
        inspectionsChange.setText(String.format(Locale.getDefault(), "%+d%% from %s", inspectionChangePercent, comparisonPeriod));
        cracksChange.setText(String.format(Locale.getDefault(), "%+d%% from %s", cracksChangePercent, comparisonPeriod));
    }

    private int getDataPointsForPeriod(String period) {
        switch (period) {
            case "Daily": return 24;
            case "Weekly": return 7;
            case "Monthly": return 30;
            case "Yearly": return 12;
            case "Custom":
                long diff = endDate.getTimeInMillis() - startDate.getTimeInMillis();
                return (int) (diff / (24 * 60 * 60 * 1000)) + 1;
            default: return 6;
        }
    }

    private String getLabelPrefixForPeriod(String period) {
        switch (period) {
            case "Daily": return "Hour";
            case "Weekly": return "Day";
            case "Monthly": return "Day";
            case "Yearly": return "Month";
            case "Custom": return "Day";
            default: return "Period";
        }
    }

    private String getLabelForPeriod(String period, int index) {
        switch (period) {
            case "Daily":
                return String.format(Locale.getDefault(), "%02d:00", index);
            case "Weekly":
                return new SimpleDateFormat("EEE", Locale.getDefault()).format(startDate.getTime());
            case "Monthly":
                return String.format(Locale.getDefault(), "Day %d", index + 1);
            case "Yearly":
                return new SimpleDateFormat("MMM", Locale.getDefault()).format(startDate.getTime());
            case "Custom":
                Calendar calendar = (Calendar) startDate.clone();
                calendar.add(Calendar.DAY_OF_YEAR, index);
                return new SimpleDateFormat("MM/dd", Locale.getDefault()).format(calendar.getTime());
            default:
                return String.valueOf(index + 1);
        }
    }

    private String getComparisonPeriod(String currentPeriod) {
        switch (currentPeriod) {
            case "Daily": return "yesterday";
            case "Weekly": return "last week";
            case "Monthly": return "last month";
            case "Yearly": return "last year";
            case "Custom": return "previous period";
            default: return "the previous period";
        }
    }
}