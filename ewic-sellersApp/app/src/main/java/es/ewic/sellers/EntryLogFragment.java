package es.ewic.sellers;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ewic.sellers.model.Entry;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.DateUtils;
import es.ewic.sellers.utils.FormUtils;
import es.ewic.sellers.utils.ModelConverter;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EntryLogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EntryLogFragment extends Fragment {

    private static final String ARG_SHOP = "shop";

    ProgressDialog pd;
    private Shop shop;

    private List<Entry> dailyEntries = new ArrayList<>();

    public EntryLogFragment() {
        // Required empty public constructor
    }

    public static EntryLogFragment newInstance(Shop shopData) {
        EntryLogFragment fragment = new EntryLogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SHOP, shopData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shop = (Shop) getArguments().getSerializable(ARG_SHOP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.entry_log);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_entry_log, container, false);

        Calendar now = Calendar.getInstance();
        getDailyEntries(parent, now);

        TextInputEditText tiet_date = parent.findViewById(R.id.entry_log_date_input);
        tiet_date.setInputType(InputType.TYPE_NULL);
        tiet_date.setText(DateUtils.formatDate(now));

        tiet_date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerListener(tiet_date, parent);
                }
            }
        });
        tiet_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerListener(tiet_date, parent);
            }
        });

        TextInputEditText tiet_nEntries = parent.findViewById(R.id.entry_log_nEntries_input);
        tiet_nEntries.setInputType(InputType.TYPE_NULL);

        TextInputEditText tiet_avg = parent.findViewById(R.id.entry_log_avg_input);
        tiet_avg.setInputType(InputType.TYPE_NULL);

        return parent;
    }

    private void showDatePickerListener(TextInputEditText tiet_date, ConstraintLayout parent) {

        final Calendar date = DateUtils.parseDateDate(tiet_date.getText().toString().trim());
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                tiet_date.clearFocus();
                tiet_date.setText(DateUtils.formatDate(cal));

                getDailyEntries(parent, cal);
            }
        }, year, month, day);
        datePicker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
        datePicker.show();
    }

    private void getDailyEntries(ConstraintLayout parent, Calendar date) {
        pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);

        String url = BackEndEndpoints.DAILY_ENTRIES(shop.getIdShop(), date);

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                dailyEntries = ModelConverter.jsonArrayToEntriList(response);
                loadData(parent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                Log.e("HTTP", "error");
            }
        });
    }


    private void loadData(ConstraintLayout parent) {

        TextView entries_not_found = parent.findViewById(R.id.entries_not_found);
        entries_not_found.setVisibility(dailyEntries.isEmpty() ? View.VISIBLE : View.GONE);
        Group entry_log_data = parent.findViewById(R.id.entry_log_data_group);
        entry_log_data.setVisibility(dailyEntries.isEmpty() ? View.GONE : View.VISIBLE);

        if (!dailyEntries.isEmpty()) {
            TextInputEditText tiet_nEntries = parent.findViewById(R.id.entry_log_nEntries_input);
            tiet_nEntries.setText(Integer.toString(dailyEntries.size()));


            TextInputEditText tiet_avg = parent.findViewById(R.id.entry_log_avg_input);
            int avg = 0;
            int max = 0;
            //Init map
            HashMap<Integer, Integer> entries = new HashMap<>();
            for (int i = 0; i < 24; i++) {
                entries.put(i, 0);
            }
            for (Entry entry : dailyEntries) {
                avg += entry.getDuration();
                int hour = entry.getStart().get(Calendar.HOUR_OF_DAY);
                Integer amount = entries.get(hour);
                amount = amount == null ? 1 : amount + 1;
                entries.put(hour, amount);
                if (amount > max) {
                    max = amount;
                }
            }

            float avgDuration = (float) avg / dailyEntries.size();
            avg = Math.round(avgDuration);
            tiet_avg.setText(avg == 0 ? getString(R.string.less_1_minute) : (Integer.toString(avg)) + " m");

            BarChart barChart = parent.findViewById(R.id.entry_log_area_chart);
            barChart.getAxisRight().setEnabled(false);
            barChart.getDescription().setEnabled(false);
            barChart.setScaleEnabled(false);


            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.disableAxisLineDashedLine();
            xAxis.disableGridDashedLine();
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return (int) value + ":00";
                }
            });

            YAxis yAxis = barChart.getAxisLeft();
            yAxis.disableAxisLineDashedLine();
            yAxis.disableGridDashedLine();
            yAxis.setDrawGridLines(false);
            barChart.getAxisRight().setEnabled(false);

            ArrayList<BarEntry> dataValues = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : entries.entrySet()) {
                if (entry.getValue() != 0) {
                    dataValues.add(new BarEntry(entry.getKey(), entry.getValue()));
                }

            }
            BarDataSet barDataSet = new BarDataSet(dataValues, getString(R.string.entry_log));
            barDataSet.setColor(R.color.design_default_color_primary);
            barDataSet.setDrawValues(true);

            BarData barData = new BarData();
            barData.addDataSet(barDataSet);

            barChart.setData(barData);

            barChart.animateXY(1500, 1500);
            barChart.getLegend().setEnabled(false);
            barChart.getData().setHighlightEnabled(false);
            barChart.invalidate();
        }
        pd.dismiss();
    }

}