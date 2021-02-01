package es.ewic.sellers;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ewic.sellers.model.Reservation;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.DateUtils;
import es.ewic.sellers.utils.FormUtils;
import es.ewic.sellers.utils.ModelConverter;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReservationLogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReservationLogFragment extends Fragment {

    private static final String ARG_SHOP = "ahop";

    ProgressDialog pd;
    private Shop shop;

    private List<Reservation> dailyReservations = new ArrayList<>();

    public ReservationLogFragment() {
        // Required empty public constructor
    }

    public static ReservationLogFragment newInstance(Shop shop) {
        ReservationLogFragment fragment = new ReservationLogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SHOP, shop);
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

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.reservation_log);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_reservation_log, container, false);

        Calendar now = Calendar.getInstance();
        getDailyReservations(parent, now);

        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_log_date_input);
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

                getDailyReservations(parent, cal);
            }
        }, year, month, day);
        datePicker.show();
    }

    private void getDailyReservations(ConstraintLayout parent, Calendar date) {
        pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);

        String url = BackEndEndpoints.DAILY_RESERVATION(shop.getIdShop(), date);

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                dailyReservations = ModelConverter.jsonArrayToReservationList(response);
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

        TextView reservations_not_found = parent.findViewById(R.id.reservation_log_reservations_not_found);
        reservations_not_found.setVisibility(dailyReservations.isEmpty() ? View.VISIBLE : View.GONE);
        Group reservation_log_data = parent.findViewById(R.id.reservation_log_data_group);
        reservation_log_data.setVisibility(dailyReservations.isEmpty() ? View.GONE : View.VISIBLE);

        if (!dailyReservations.isEmpty()) {

            HashMap<Integer, Integer> reservationsAmount = new HashMap<>();
            HashMap<String, Integer> reservationsType = new HashMap<>();
            reservationsType.put("ACTIVE", 0);
            reservationsType.put("WAITING", 0);
            reservationsType.put("COMPLETED", 0);
            reservationsType.put("NOT_APPEAR", 0);
            reservationsType.put("CANCELLED", 0);

            for (Reservation rsv : dailyReservations) {
                int hour = rsv.getDate().get(Calendar.HOUR_OF_DAY);
                Integer amount = reservationsAmount.get(hour);
                amount = amount == null ? 1 : amount + 1;
                reservationsAmount.put(hour, amount);

                Integer typeAmount = reservationsType.get(rsv.getState());
                typeAmount = typeAmount == null ? 1 : typeAmount + 1;
                reservationsType.put(rsv.getState(), typeAmount);
            }

            BarChart barChart = parent.findViewById(R.id.reservation_log_bar_chart);
            loadBarChart(barChart, reservationsAmount);

            PieChart pieChart = parent.findViewById(R.id.reservation_log_pie_chart);
            loadPieChart(pieChart, reservationsType);

        }

        pd.dismiss();
    }

    private void loadBarChart(BarChart barChart, HashMap<Integer, Integer> reservationsAmount) {

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setScaleEnabled(false);


        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.disableAxisLineDashedLine();
        xAxis.disableGridDashedLine();
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(12F);
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
        yAxis.setGranularity(1f);
        yAxis.setTextSize(12F);
        barChart.getAxisRight().setEnabled(false);

        ArrayList<BarEntry> dataValues = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : reservationsAmount.entrySet()) {
            if (entry.getValue() != 0) {
                dataValues.add(new BarEntry(entry.getKey(), entry.getValue()));
            }

        }
        BarDataSet barDataSet = new BarDataSet(dataValues, getString(R.string.entry_log));
        barDataSet.setColor(R.color.design_default_color_primary);
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(18f);
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Integer.toString((int) value);
            }
        });

        BarData barData = new BarData();
        barData.addDataSet(barDataSet);

        barChart.setData(barData);

        barChart.animateXY(1500, 1500);
        barChart.getLegend().setEnabled(false);
        barChart.getData().setHighlightEnabled(false);
        barChart.invalidate();
    }

    private void loadPieChart(PieChart pieChart, HashMap<String, Integer> reservationsTypeAmount) {

        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(false);
        pieChart.setCenterText(getString(R.string.reservation_state));
        pieChart.setCenterTextSize(18);
//        pieChart.setCenterTextRadiusPercent(85);
        pieChart.setHoleRadius(70);
        pieChart.setDrawEntryLabels(false);
        pieChart.setHighlightPerTapEnabled(false);

        ArrayList<PieEntry> dataValues = new ArrayList<>();
        dataValues.add(new PieEntry(reservationsTypeAmount.get("ACTIVE"), getString(getResources().getIdentifier("ACTIVE", "string", getActivity().getPackageName()))));
        dataValues.add(new PieEntry(reservationsTypeAmount.get("WAITING"), getString(getResources().getIdentifier("WAITING", "string", getActivity().getPackageName()))));
        dataValues.add(new PieEntry(reservationsTypeAmount.get("COMPLETED"), getString(getResources().getIdentifier("COMPLETED", "string", getActivity().getPackageName()))));
        dataValues.add(new PieEntry(reservationsTypeAmount.get("NOT_APPEAR"), getString(getResources().getIdentifier("NOT_APPEAR", "string", getActivity().getPackageName()))));
        dataValues.add(new PieEntry(reservationsTypeAmount.get("CANCELLED"), getString(getResources().getIdentifier("CANCELLED", "string", getActivity().getPackageName()))));

        int[] colorClassArray = new int[]{
                Color.GREEN, Color.YELLOW, Color.CYAN, Color.RED, Color.LTGRAY};

        PieDataSet pieDataSet = new PieDataSet(dataValues, "");
        pieDataSet.setColors(colorClassArray);
        pieDataSet.setValueTextSize(18f);

        pieDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == 0 ? " " : Integer.toString((int) value);
            }
        });

        Legend l = pieChart.getLegend();
        l.setTextSize(14);
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);

        pieChart.animateXY(1500, 1500);


        pieChart.invalidate();


    }
}