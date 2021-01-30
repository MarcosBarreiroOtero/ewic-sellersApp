package es.ewic.sellers;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.ewic.sellers.adapters.HourAutocompleteAdapter;
import es.ewic.sellers.model.Client;
import es.ewic.sellers.model.Reservation;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.ConfigurationNames;
import es.ewic.sellers.utils.DateUtils;
import es.ewic.sellers.utils.FormUtils;
import es.ewic.sellers.utils.ModelConverter;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateReservationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateReservationFragment extends Fragment {

    private static final String ARG_SHOP = "shop";
    private static final String ARG_RESERVATION = "reservation";

    private Shop shop;
    private Reservation reservation;
    JSONArray timetable;

    private int minutesBetweenReservation = 15;

    private List<Client> clients;

    OnCreateReservationListener mCallback;

    public interface OnCreateReservationListener {
        void onRsvCreatedOrUpdated();
    }

    public CreateReservationFragment() {
        // Required empty public constructor
    }

    public static CreateReservationFragment newInstance(Shop shopData, Reservation reservationData) {
        CreateReservationFragment fragment = new CreateReservationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SHOP, shopData);
        args.putSerializable(ARG_RESERVATION, reservationData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (OnCreateReservationListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shop = (Shop) getArguments().getSerializable(ARG_SHOP);
            reservation = (Reservation) getArguments().getSerializable(ARG_RESERVATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (reservation == null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.add_reservation));
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.update_reservation));
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);


        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_create_reservation, container, false);

        try {
            timetable = new JSONArray(shop.getTimetable());
        } catch (JSONException e) {
            timetable = new JSONArray();
        }

        Calendar now = Calendar.getInstance();
        boolean closed = true;
        while (closed) {
            now.add(Calendar.DATE, 1);
            int weekDay = now.get(Calendar.DAY_OF_WEEK);
            if (weekDay == 1) {
                weekDay = 6;
            } else {
                weekDay -= 2;
            }
            for (int i = 0; i < timetable.length(); i++) {
                JSONObject weekDayTimetable = timetable.optJSONObject(i);
                if (weekDay == weekDayTimetable.optInt("weekDay")) {
                    closed = false;
                    break;
                }
            }
        }


        // Hour input
        AutoCompleteTextView act_hour = parent.findViewById(R.id.reservation_hour_input);
        if (reservation != null) {
            getReservationParams(parent, reservation.getDate(), timetable);
        } else {
            getReservationParams(parent, now, timetable);
        }

        //Date input
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        if (reservation == null) {
            tiet_date.setText(DateUtils.formatDate(now));
        } else {
            tiet_date.setText(DateUtils.formatDate(reservation.getDate()));
        }
        tiet_date.setInputType(InputType.TYPE_NULL);
        tiet_date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerListener(parent, tiet_date);
                }
            }
        });

        //Nclients
        TextInputEditText tiet_nClients = parent.findViewById(R.id.reservation_nClients_input);
        if (reservation != null) {
            tiet_nClients.setText(Integer.toString(reservation.getnClients()));
        } else {
            tiet_nClients.setText("1");
        }

        //Client
        AutoCompleteTextView act_client = parent.findViewById(R.id.reservation_client_input);
        if (reservation == null) {
            getClientsNames(parent);
        } else {
            String[] clientNames = new String[]{reservation.getClientName()};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, clientNames);
            act_client.setAdapter(adapter);
            act_client.setText(reservation.getClientName());
            act_client.setEnabled(false);
        }

        //Button
        Button createReservationButon = parent.findViewById(R.id.create_reservation_button);
        if (reservation != null) {
            createReservationButon.setText(getString(R.string.update_reservation));
        }
        createReservationButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewReservationForm(parent);
            }
        });


        return parent;
    }

    private void showDatePickerListener(ConstraintLayout parent, TextInputEditText tiet_date) {
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
                int weekDay = cal.get(Calendar.DAY_OF_WEEK);
                if (weekDay == 1) {
                    weekDay = 6;
                } else {
                    weekDay -= 2;
                }
                boolean closed = true;
                for (int i = 0; i < timetable.length(); i++) {
                    JSONObject weekDayTimetable = timetable.optJSONObject(i);
                    if (weekDay == weekDayTimetable.optInt("weekDay")) {
                        closed = false;
                    }
                }
                if (closed) {
                    Snackbar.make(parent, getString(R.string.error_shop_closed_weekDay), Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    tiet_date.setText(DateUtils.formatDate(cal));
                    setAdapterHourInput(parent, cal, timetable);
                }
            }
        }, year, month, day);
        datePicker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        datePicker.show();
    }

    private void getClientsNames(ConstraintLayout parent) {

        String url = BackEndEndpoints.CLIENT_BASE;

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                clients = ModelConverter.jsonArrayToClientList(response);
                List<String> names = new ArrayList<>();
                for (int i = 0; i < clients.size(); i++) {
                    Client client = clients.get(i);
                    names.add(client.getFirstName() + " " + client.getLastName());
                }
                String[] clientNames = names.toArray(new String[names.size()]);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                        android.R.layout.simple_dropdown_item_1line, clientNames);
                AutoCompleteTextView act_client = parent.findViewById(R.id.reservation_client_input);
                act_client.setAdapter(adapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "Error");
            }
        });
    }

    private List<String> getHoursBetweenRanges(Calendar start, Calendar end) {
        List<String> hours = new ArrayList<>();
        while (start.before(end)) {
            hours.add(DateUtils.formatHour(start));
            start.add(Calendar.MINUTE, minutesBetweenReservation);
        }
        return hours;

    }

    private void getReservationParams(ConstraintLayout parent, Calendar date, JSONArray timetable) {

        String url = BackEndEndpoints.CONFIGURATION_RESERVATION(shop.getIdShop());

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject param = response.getJSONObject(i);
                        if (param.getString("name").equals(ConfigurationNames.MINUTES_BETWEEN_RESERVATIONS)) {
                            minutesBetweenReservation = Integer.parseInt(param.getString("value"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                setAdapterHourInput(parent, date, timetable);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error reservation params");
            }
        });
    }

    private void setAdapterHourInput(ConstraintLayout parent, Calendar date, JSONArray timetable) {
        List<String> hours = new ArrayList<>();
        int weekDay = date.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1) {
            weekDay = 6;
        } else {
            weekDay -= 2;
        }

        for (int i = 0; i < timetable.length(); i++) {
            JSONObject weekDayTimetable = timetable.optJSONObject(i);
            if (weekDay == weekDayTimetable.optInt("weekDay")) {
                try {
                    Calendar startMorning = DateUtils.parseDateHour(weekDayTimetable.getString("startMorning"));
                    Calendar endMorning = DateUtils.parseDateHour(weekDayTimetable.getString("endMorning"));
                    hours.addAll(getHoursBetweenRanges(startMorning, endMorning));
                } catch (JSONException e) {
                    // no timetable morning
                }

                try {
                    Calendar startAfternoon = DateUtils.parseDateHour(weekDayTimetable.getString("startAfternoon"));
                    Calendar endAfternoon = DateUtils.parseDateHour(weekDayTimetable.getString("endAfternoon"));
                    hours.addAll(getHoursBetweenRanges(startAfternoon, endAfternoon));
                } catch (JSONException e) {
                    // no timetable afternoon
                }
            }
        }

        String[] hoursValues = hours.toArray(new String[hours.size()]);
        AutoCompleteTextView act_hours = parent.findViewById(R.id.reservation_hour_input);
        HourAutocompleteAdapter adapter = new HourAutocompleteAdapter(hours, CreateReservationFragment.this);
        act_hours.setAdapter(adapter);
        if (reservation != null) {
            act_hours.setText(DateUtils.formatHour(reservation.getDate()));
        } else {
            act_hours.setText("");
        }
    }


    private void createNewReservationForm(ConstraintLayout parent) {

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.updating_data, R.string.please_wait);

        boolean hasError = false;

        //Client
        TextInputLayout til_client = parent.findViewById(R.id.reservation_client_label);
        AutoCompleteTextView act_client = parent.findViewById(R.id.reservation_client_input);
        til_client.setError(null);
        String name = act_client.getText().toString().trim();
        String idGoogleLogin = null;
        if (name.isEmpty()) {
            til_client.setError(getString(R.string.error_empty_field));
            hasError = true;
        } else {
            for (Client c : clients) {
                if (name.equals(c.getFirstName() + " " + c.getLastName())) {
                    idGoogleLogin = c.getIdGoogleLogin();
                    break;
                }
            }
            if (idGoogleLogin == null) {
                til_client.setError(getString(R.string.error_client_not_found));
                hasError = true;
            }
        }

        //Date
        Calendar date = Calendar.getInstance();
        TextInputLayout til_date = parent.findViewById(R.id.reservation_date_label);
        TextInputEditText tiet_date = parent.findViewById(R.id.reservation_date_input);
        til_date.setError(null);

        TextInputLayout til_hour = parent.findViewById(R.id.reservation_hour_label);
        AutoCompleteTextView act_hour = parent.findViewById(R.id.reservation_hour_input);
        act_hour.setError(null);

        String dateInput = tiet_date.getText().toString().trim();
        if (dateInput.isEmpty()) {
            til_date.setError(getString(R.string.error_empty_field));
            hasError = true;
        }
        String hourInput = act_hour.getText().toString().trim();
        if (hourInput.isEmpty()) {
            til_hour.setError(getString(R.string.error_empty_field));
            hasError = true;
        } else {
            ArrayList<String> results = new ArrayList<>();
            ListAdapter adapter = act_hour.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                results.add((String) adapter.getItem(i));
            }
            if (results.size() == 0 ||
                    results.indexOf(hourInput) == -1) {
                til_hour.setError(getString(R.string.error_hour_invalid));
                act_hour.setText("");
                act_hour.requestFocus();
                hasError = true;
            } else if (!dateInput.isEmpty()) {
                Calendar now = Calendar.getInstance();
                date = DateUtils.parseDateLong(hourInput + " " + dateInput);
                if (date == null) {
                    til_hour.setError(getString(R.string.error_bad_format_hour));
                    til_date.setError(getString(R.string.error_bad_format_date));
                    hasError = true;
                } else if (now.after(date)) {
                    til_hour.setError(getString(R.string.error_past_reservation));
                    act_hour.setText("");
                    act_hour.requestFocus();
                    hasError = true;
                }
            }
        }

        //Nclients
        TextInputLayout til_nClient = parent.findViewById(R.id.reservation_nClients_label);
        TextInputEditText tiet_nClients = parent.findViewById(R.id.reservation_nClients_input);
        til_nClient.setError(null);
        String nClientsText = tiet_nClients.getText().toString().trim();
        Integer nClients = null;
        if (nClientsText.isEmpty()) {
            til_nClient.setError(getString(R.string.error_empty_field));
            hasError = true;
        } else {
            nClients = Integer.parseInt(nClientsText);

            if (nClients < 1) {
                til_nClient.setError(getString(R.string.error_nClients_min));
                hasError = true;
            }
            if (nClients > shop.getMaxCapacity()) {
                til_nClient.setError(getString(R.string.error_nClients_max) + " (" + shop.getMaxCapacity() + ")");
                hasError = true;
            }
        }
        Log.e("ERROR", "Error " + hasError);
        if (hasError) {
            pd.dismiss();
            return;
        } else {

            TextInputEditText tiet_remarks = parent.findViewById(R.id.reservation_remarks_input);
            String remarks = tiet_remarks.getText().toString().trim();

            Reservation newReservation = new Reservation(date, remarks, nClients, idGoogleLogin, shop.getIdShop());
            JSONObject rsvJSON = ModelConverter.reservationToJsonObject(newReservation);
            String url = BackEndEndpoints.RESERVATION_BASE;
            if (reservation != null) {
                url += "/" + reservation.getIdReservation();
            }

            int method = Request.Method.POST;
            if (reservation != null) {
                method = Request.Method.PUT;
            }

            RequestUtils.sendJsonObjectRequest(getContext(), method, url, rsvJSON, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    pd.dismiss();
                    if (reservation == null) {
                        Snackbar.make(parent, getString(R.string.reservation_create_successfully), Snackbar.LENGTH_LONG)
                                .show();
                    } else {
                        Snackbar.make(parent, getString(R.string.reservation_update_successfully), Snackbar.LENGTH_LONG)
                                .show();
                    }
                    mCallback.onRsvCreatedOrUpdated();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("HTTP", "error");
                    pd.dismiss();
                    if (error instanceof TimeoutError) {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                                pd.show();
                                createNewReservationForm(parent);
                            }
                        });
                        snackbar.show();
                    } else {
                        int responseCode = RequestUtils.getErrorCodeRequest(error);
                        // 400 rsv duplicate
                        // 404 client, shop not found: should not happen
                        // 401 rsv unathorized: rsv in past or shop full
                        String message = "";
                        String errorMessage = RequestUtils.getErrorMessageRequest(error);
                        switch (responseCode) {
                            case 400:
                                message = getString(R.string.error_rsv_duplicate);
                                break;
                            case 401:
                                if (errorMessage.contains(RequestUtils.RESERVATION_WHEN_SHOP_FULL)) {
                                    message = getString(R.string.error_reservation_shop_full);
                                } else {
                                    message = getString(R.string.error_past_reservation);
                                }
                                break;
                            default:
                                break;
                        }
                        if (message.isEmpty()) {
                            Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                    pd.show();
                                    createNewReservationForm(parent);
                                }
                            });
                            snackbar.show();
                        } else {
                            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }
}