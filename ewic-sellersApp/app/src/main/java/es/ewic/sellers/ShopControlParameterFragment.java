package es.ewic.sellers;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.ConfigurationNames;
import es.ewic.sellers.utils.FormUtils;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopControlParameterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopControlParameterFragment extends Fragment {

    private static final String ARG_SHOP = "shop";

    private Shop shop;

    private boolean showing_email = true;
    private boolean showing_reservation = true;

    public ShopControlParameterFragment() {
        // Required empty public constructor
    }

    public static ShopControlParameterFragment newInstance(Shop shopData) {
        ShopControlParameterFragment fragment = new ShopControlParameterFragment();
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

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_shop_control_parameter, container, false);

        loadParameters(parent);

        TextView email_text = parent.findViewById(R.id.shop_parameter_email_text);
        Group email_group = parent.findViewById(R.id.shop_parameter_email_group);
        email_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showing_email = !showing_email;
                email_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, showing_email ? R.drawable.ic_baseline_keyboard_arrow_down_24 : R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
                email_group.setVisibility(showing_email ? View.VISIBLE : View.GONE);
            }
        });
        Button email_button = parent.findViewById(R.id.shop_parameter_email_button);
        email_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMailParams(parent);
            }
        });

        TextView reservation_text = parent.findViewById(R.id.shop_parameter_reservation_text);
        Group reservation_group = parent.findViewById(R.id.shop_parameter_reservation_group);
        reservation_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showing_reservation = !showing_reservation;
                reservation_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, showing_reservation ? R.drawable.ic_baseline_keyboard_arrow_down_24 : R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
                reservation_group.setVisibility(showing_reservation ? View.VISIBLE : View.GONE);
            }
        });
        Button reservation_button = parent.findViewById(R.id.shop_parameter_reservation_button);
        reservation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReservationParmas(parent);
            }
        });


        return parent;
    }

    private void updateMailParams(ConstraintLayout parent) {

        try {
            JSONArray parameters = new JSONArray();

            TextInputEditText tiet_mailHost = parent.findViewById(R.id.mailHost);
            parameters.put(new JSONObject().put("name", ConfigurationNames.MAIL_HOST).put("value", tiet_mailHost.getText().toString().trim()));

            TextInputEditText tiet_mailPort = parent.findViewById(R.id.mailPort);
            parameters.put(new JSONObject().put("name", ConfigurationNames.MAIL_PORT).put("value", tiet_mailPort.getText().toString().trim()));

            TextInputEditText tiet_mailUsername = parent.findViewById(R.id.mailUsername);
            parameters.put(new JSONObject().put("name", ConfigurationNames.MAIL_USERNAME).put("value", tiet_mailUsername.getText().toString().trim()));

            TextInputEditText tiet_mailPassword = parent.findViewById(R.id.mailPassword);
            parameters.put(new JSONObject().put("name", ConfigurationNames.MAIL_PASSWORD).put("value", tiet_mailPassword.getText().toString().trim()));

            updateParameters(parent, parameters);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateReservationParmas(ConstraintLayout parent) {
        try {
            JSONArray parameters = new JSONArray();

            TextInputEditText tiet_reservationWaitMinutes = parent.findViewById(R.id.reservationWaitMinutes);
            parameters.put(new JSONObject().put("name", ConfigurationNames.RESERVATION_WAIT_MINUTES).put("value", tiet_reservationWaitMinutes.getText().toString().trim()));

            TextInputLayout til_tiet_minutesBetweenReservations = parent.findViewById(R.id.shop_parameter_minutesBetweenReservations);
            til_tiet_minutesBetweenReservations.setError(null);
            TextInputEditText tiet_minutesBetweenReservations = parent.findViewById(R.id.minutesBetweenReservations);
            String minutesBetweenReservations = tiet_minutesBetweenReservations.getText().toString().trim();
            if (!minutesBetweenReservations.isEmpty() && Integer.parseInt(minutesBetweenReservations) < 1) {
                til_tiet_minutesBetweenReservations.setError(getString(R.string.error_min_value_1));
                return;
            }
            parameters.put(new JSONObject().put("name", ConfigurationNames.MINUTES_BETWEEN_RESERVATIONS).put("value", minutesBetweenReservations));

            TextInputEditText tiet_minutesAfterOpeningMorning = parent.findViewById(R.id.minutesAfterOpeningMorning);
            parameters.put(new JSONObject().put("name", ConfigurationNames.MINUTES_AFTER_OPENING_MORNING).put("value", tiet_minutesAfterOpeningMorning.getText().toString().trim()));

            TextInputEditText tiet_minutesBeforeClosingMorning = parent.findViewById(R.id.minutesBeforeClosingMorning);
            parameters.put(new JSONObject().put("name", ConfigurationNames.MINUTES_BEFORE_CLOSING_MORNING).put("value", tiet_minutesBeforeClosingMorning.getText().toString().trim()));

            TextInputEditText tiet_minutesAfterOpeningAfternoon = parent.findViewById(R.id.minutesAfterOpeningAfternoon);
            parameters.put(new JSONObject().put("name", ConfigurationNames.MINUTES_AFTER_OPENING_AFTERNOON).put("value", tiet_minutesAfterOpeningAfternoon.getText().toString().trim()));

            TextInputEditText tiet_minutesBeforeClosingAfternoon = parent.findViewById(R.id.minutesBeforeClosingAfternoon);
            parameters.put(new JSONObject().put("name", ConfigurationNames.MINUTES_BEFORE_CLOSING_AFTERNOON).put("value", tiet_minutesBeforeClosingAfternoon.getText().toString().trim()));

            updateParameters(parent, parameters);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void updateParameters(ConstraintLayout parent, JSONArray parameters) {
        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);

        String url = BackEndEndpoints.CONFIGURATION_BATCH(shop.getIdShop());

        Log.e("PARAMETERS", parameters.toString());

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.POST, url, parameters, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                pd.dismiss();
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.parameters_updated_successfully), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        loadParameters(parent);
                    }
                });
                snackbar.show();
            }
        });
    }


    private void loadParameters(ConstraintLayout parent) {

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);

        String url = BackEndEndpoints.CONFIGURATION_BASE + "/" + shop.getIdShop();

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONArray controlParameters = response;
                fillTextInput(parent, controlParameters, pd);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        loadParameters(parent);
                    }
                });
                snackbar.show();
            }
        });

    }

    private void fillTextInput(ConstraintLayout parent, JSONArray controlParameters, ProgressDialog pd) {

        for (int i = 0; i < controlParameters.length(); i++) {
            try {
                JSONObject parameter = controlParameters.getJSONObject(i);
                String name = parameter.getString("name");
                String value = parameter.getString("value");

                TextInputEditText tiet = parent.findViewById(getResources().getIdentifier(name, "id", getActivity().getPackageName()));
                if (tiet != null) {
                    tiet.setText(value);
                }
            } catch (JSONException e) {
                // omit parameter
            }
        }
        pd.dismiss();
    }
}