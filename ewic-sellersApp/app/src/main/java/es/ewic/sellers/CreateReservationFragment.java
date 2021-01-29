package es.ewic.sellers;

import android.app.DatePickerDialog;
import android.os.Bundle;

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
import android.widget.DatePicker;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.ewic.sellers.model.Client;
import es.ewic.sellers.model.Reservation;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.DateUtils;
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

    private List<Client> clients;

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

        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, 1);

        getClientsNames(parent);

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
                tiet_date.setText(DateUtils.formatDate(cal));
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
}