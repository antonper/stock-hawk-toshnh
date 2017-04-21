package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ChartActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>{

    @BindView(R.id.chart_title)
    TextView text;
    @BindView(R.id.chart)
    LineChart chart;
    @BindColor(R.color.white) public int white;
    Uri stockUri;
    String history = null;
    String symbol = null;

    private static int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        ButterKnife.bind(this);

        stockUri=getIntent().getData();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    public String plot(String symbol) {
//        String history = getHistory(symbol);


        List<String[]> lines = getLines(history);

        List<Entry> entries = new ArrayList<>();
        final List<Long> xAxisValues = new ArrayList<>();
        int xAxisPosition = 0;
        for (String[] line : lines){
            xAxisValues.add(Long.valueOf(line[0]));
            xAxisPosition++;
            Entry entry = new Entry(
                    xAxisPosition,
                    Float.valueOf(line[1])
            );
            entries.add(entry);
        }


//        chart.setBackgroundColor(Color.BLACK);
        LineDataSet  lineDataSet = new LineDataSet(entries, symbol);
//        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(ContextCompat.getColor(this,R.color.chart_line));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setDrawValues(false);

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_blue);
        lineDataSet.setFillDrawable(drawable);

        LineData lineData = new LineData(lineDataSet);
        lineData.setDrawValues(false);
        chart.setDescription(null);
        chart.setData(lineData);
        chart.getLegend().setEnabled(false);
        XAxis xAxis =  chart.getXAxis();
//        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(12f);
        xAxis.setGridColor(ContextCompat.getColor(this,R.color.chart_grid));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date(xAxisValues.get(xAxisValues.size()- (int)value - 1));
                return new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH)
                        .format(date);
            }
        });


        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setAxisLineColor(white);
        yAxis.setAxisLineWidth(1.5f);
        yAxis.setTextColor(white);
        yAxis.setTextSize(12f);


        text.setText(symbol);
        return symbol;
    }
    @Nullable
    private List<String[]> getLines(String history){
        List<String[]> lines = new ArrayList<>();
        CSVReader reader = new CSVReader(new StringReader(history));
        try {
            lines.addAll(reader.readAll());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private String getHistory(String symbol) {
        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol), null, null, null, null);
        String history = null;
        if(cursor != null){
            cursor.moveToFirst();
            history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            cursor.close();
        }
        return history;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (stockUri != null) {
            return new CursorLoader(
                    this,
                    stockUri,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()){
            history = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            symbol =  data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
            plot(symbol);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}









