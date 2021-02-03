package es.ewic.sellers;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.NoConnectionError;
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
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import es.ewic.sellers.model.Seller;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.FormUtils;
import es.ewic.sellers.utils.ModelConverter;
import es.ewic.sellers.utils.RequestUtils;
import es.ewic.sellers.utils.TimetableUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateShopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateShopFragment extends Fragment {

    private static final String ARG_SHOP = "shop";
    private static final String ARG_SELLER = "seller";

    OnCreateShopListener mCallback;

    private Shop shop;
    private Seller seller;

    private boolean showing_general = true;
    private boolean showing_location = true;
    private boolean showing_capacity = true;
    private boolean showing_timetable = false;

    private JSONArray shop_types;

    //Timetable values
    TextInputLayout til_monday_morning_opening;
    TextInputEditText tiet_monday_morning_opening;
    TextInputLayout til_monday_morning_closing;
    TextInputEditText tiet_monday_morning_closing;
    TextInputLayout til_monday_afternoon_opening;
    TextInputEditText tiet_monday_afternoon_opening;
    TextInputLayout til_monday_afternoon_closing;
    TextInputEditText tiet_monday_afternoon_closing;

    //Tuesday
    TextInputLayout til_tuesday_morning_opening;
    TextInputEditText tiet_tuesday_morning_opening;
    TextInputLayout til_tuesday_morning_closing;
    TextInputEditText tiet_tuesday_morning_closing;
    TextInputLayout til_tuesday_afternoon_opening;
    TextInputEditText tiet_tuesday_afternoon_opening;
    TextInputLayout til_tuesday_afternoon_closing;
    TextInputEditText tiet_tuesday_afternoon_closing;

    //Wednesday
    TextInputLayout til_wednesday_morning_opening;
    TextInputEditText tiet_wednesday_morning_opening;
    TextInputLayout til_wednesday_morning_closing;
    TextInputEditText tiet_wednesday_morning_closing;
    TextInputLayout til_wednesday_afternoon_opening;
    TextInputEditText tiet_wednesday_afternoon_opening;
    TextInputLayout til_wednesday_afternoon_closing;
    TextInputEditText tiet_wednesday_afternoon_closing;

    //Thursday
    TextInputLayout til_thursday_morning_opening;
    TextInputEditText tiet_thursday_morning_opening;
    TextInputLayout til_thursday_morning_closing;
    TextInputEditText tiet_thursday_morning_closing;
    TextInputLayout til_thursday_afternoon_opening;
    TextInputEditText tiet_thursday_afternoon_opening;
    TextInputLayout til_thursday_afternoon_closing;
    TextInputEditText tiet_thursday_afternoon_closing;

    //Friday
    TextInputLayout til_friday_morning_opening;
    TextInputEditText tiet_friday_morning_opening;
    TextInputLayout til_friday_morning_closing;
    TextInputEditText tiet_friday_morning_closing;
    TextInputLayout til_friday_afternoon_opening;
    TextInputEditText tiet_friday_afternoon_opening;
    TextInputLayout til_friday_afternoon_closing;
    TextInputEditText tiet_friday_afternoon_closing;

    //Saturday
    TextInputLayout til_saturday_morning_opening;
    TextInputEditText tiet_saturday_morning_opening;
    TextInputLayout til_saturday_morning_closing;
    TextInputEditText tiet_saturday_morning_closing;
    TextInputLayout til_saturday_afternoon_opening;
    TextInputEditText tiet_saturday_afternoon_opening;
    TextInputLayout til_saturday_afternoon_closing;
    TextInputEditText tiet_saturday_afternoon_closing;

    //Sunday
    TextInputLayout til_sunday_morning_opening;
    TextInputEditText tiet_sunday_morning_opening;
    TextInputLayout til_sunday_morning_closing;
    TextInputEditText tiet_sunday_morning_closing;
    TextInputLayout til_sunday_afternoon_opening;
    TextInputEditText tiet_sunday_afternoon_opening;
    TextInputLayout til_sunday_afternoon_closing;
    TextInputEditText tiet_sunday_afternoon_closing;


    public interface OnCreateShopListener {
        public void shopCreated();

        public void shopUpdated(Shop shop);

    }

    public CreateShopFragment() {
        // Required empty public constructor
    }

    public static CreateShopFragment newInstance(Seller seller, Shop shopData) {
        CreateShopFragment fragment = new CreateShopFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SELLER, seller);
        args.putSerializable(ARG_SHOP, shopData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            seller = (Seller) getArguments().getSerializable(ARG_SELLER);
            shop = (Shop) getArguments().getSerializable(ARG_SHOP);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (CreateShopFragment.OnCreateShopListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (shop == null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.create_shop);
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.update_shop);
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_create_shop, container, false);

        linkTimetableValues(parent);

        getShopTypes(parent);

        ScrollView sv = parent.findViewById(R.id.create_shop_scrollView);

        Button create_shop_button = parent.findViewById(R.id.create_shop_button);
        create_shop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showing_timetable = true;
                TimetableUtils.toogleTimetableVisibility(parent, showing_timetable);

                createShop(parent);

            }
        });

        TextView create_shop_general_text = parent.findViewById(R.id.create_shop_general_text);
        TextInputLayout til_name = parent.findViewById(R.id.create_shop_name_label);
        TextInputLayout til_type = parent.findViewById(R.id.create_shop_type_label);
        create_shop_general_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showing_general = !showing_general;
                create_shop_general_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, showing_general ? R.drawable.ic_baseline_keyboard_arrow_down_24 : R.drawable.ic_baseline_keyboard_arrow_up_24, 0);

                til_name.setVisibility(showing_general ? View.VISIBLE : View.GONE);
                til_type.setVisibility(showing_general ? View.VISIBLE : View.GONE);

                if (showing_general) {
                    til_type.requestFocus();
                    til_type.clearFocus();
                }
            }
        });

        TextView create_shop_location_text = parent.findViewById(R.id.create_shop_location_text);
        TextInputLayout til_location = parent.findViewById(R.id.create_shop_location_label);
        TextView create_shop_coordinates_text = parent.findViewById(R.id.create_shop_coordinates);
        TextInputLayout til_latitude = parent.findViewById(R.id.create_shop_latitude_label);
        TextInputLayout til_longitude = parent.findViewById(R.id.create_shop_longitude_label);
        create_shop_location_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showing_location = !showing_location;
                create_shop_location_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, showing_location ? R.drawable.ic_baseline_keyboard_arrow_down_24 : R.drawable.ic_baseline_keyboard_arrow_up_24, 0);

                til_location.setVisibility(showing_location ? View.VISIBLE : View.GONE);
                create_shop_coordinates_text.setVisibility(showing_location ? View.VISIBLE : View.GONE);
                til_latitude.setVisibility(showing_location ? View.VISIBLE : View.GONE);
                til_longitude.setVisibility(showing_location ? View.VISIBLE : View.GONE);

                if (showing_location) {
                    til_longitude.requestFocus();
                    til_longitude.clearFocus();
                }

            }
        });

        TextView create_shop_capacity_text = parent.findViewById(R.id.create_shop_capacity_text);
        TextInputLayout til_maxCapacity = parent.findViewById(R.id.create_shop_maxCapacity_label);
        create_shop_capacity_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showing_capacity = !showing_capacity;
                create_shop_capacity_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, showing_capacity ? R.drawable.ic_baseline_keyboard_arrow_down_24 : R.drawable.ic_baseline_keyboard_arrow_up_24, 0);

                til_maxCapacity.setVisibility(showing_capacity ? View.VISIBLE : View.GONE);

                if (showing_capacity) {
                    til_maxCapacity.requestFocus();
                    til_maxCapacity.clearFocus();
                }
            }
        });

        TextView create_shop_timetable_text = parent.findViewById(R.id.create_shop_timetable_text);
        create_shop_timetable_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showing_timetable = !showing_timetable;
                create_shop_timetable_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, showing_timetable ? R.drawable.ic_baseline_keyboard_arrow_down_24 : R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
                TimetableUtils.toogleTimetableVisibility(parent, showing_timetable);

                if (showing_timetable) {
                    //TODO mover scroll hasta el final de la scrollView (o hacer focus sobre algún input)
                    //extView sunday = parent.findViewById(R.id.create_shop_sunday_text);
                    sv.scrollTo(0, parent.findViewById(R.id.constraint_layout_general).getBottom());
                }

            }
        });

        if (shop != null) {
            initTimetable(parent, false);
            create_shop_button.setText(getString(R.string.update_shop));
        } else {
            initTimetable(parent, true);
            create_shop_button.setText(getString(R.string.create_shop));
        }


        TextInputEditText tiet_maxCapacity = parent.findViewById(R.id.create_shop_maxCapacity_input);
        tiet_maxCapacity.setText("1");

        return parent;
    }

    private void linkTimetableValues(ConstraintLayout parent) {

        //Monday
        til_monday_morning_opening = parent.findViewById(R.id.create_shop_monday_start_morning_label);
        tiet_monday_morning_opening = parent.findViewById(R.id.create_shop_monday_start_morning_input);
        til_monday_morning_closing = parent.findViewById(R.id.create_shop_monday_end_morning_label);
        tiet_monday_morning_closing = parent.findViewById(R.id.create_shop_monday_end_morning_input);
        til_monday_afternoon_opening = parent.findViewById(R.id.create_shop_monday_start_afternoon_label);
        tiet_monday_afternoon_opening = parent.findViewById(R.id.create_shop_monday_start_afternoon_input);
        til_monday_afternoon_closing = parent.findViewById(R.id.create_shop_monday_end_afternoon_label);
        tiet_monday_afternoon_closing = parent.findViewById(R.id.create_shop_monday_end_afternoon_input);

        //Tuesday
        til_tuesday_morning_opening = parent.findViewById(R.id.create_shop_tuesday_start_morning_label);
        tiet_tuesday_morning_opening = parent.findViewById(R.id.create_shop_tuesday_start_morning_input);
        til_tuesday_morning_closing = parent.findViewById(R.id.create_shop_tuesday_end_morning_label);
        tiet_tuesday_morning_closing = parent.findViewById(R.id.create_shop_tuesday_end_morning_input);
        til_tuesday_afternoon_opening = parent.findViewById(R.id.create_shop_tuesday_start_afternoon_label);
        tiet_tuesday_afternoon_opening = parent.findViewById(R.id.create_shop_tuesday_start_afternoon_input);
        til_tuesday_afternoon_closing = parent.findViewById(R.id.create_shop_tuesday_end_afternoon_label);
        tiet_tuesday_afternoon_closing = parent.findViewById(R.id.create_shop_tuesday_end_afternoon_input);

        //Wednesday
        til_wednesday_morning_opening = parent.findViewById(R.id.create_shop_wednesday_start_morning_label);
        tiet_wednesday_morning_opening = parent.findViewById(R.id.create_shop_wednesday_start_morning_input);
        til_wednesday_morning_closing = parent.findViewById(R.id.create_shop_wednesday_end_morning_label);
        tiet_wednesday_morning_closing = parent.findViewById(R.id.create_shop_wednesday_end_morning_input);
        til_wednesday_afternoon_opening = parent.findViewById(R.id.create_shop_wednesday_start_afternoon_label);
        tiet_wednesday_afternoon_opening = parent.findViewById(R.id.create_shop_wednesday_start_afternoon_input);
        til_wednesday_afternoon_closing = parent.findViewById(R.id.create_shop_wednesday_end_afternoon_label);
        tiet_wednesday_afternoon_closing = parent.findViewById(R.id.create_shop_wednesday_end_afternoon_input);

        //Thursday
        til_thursday_morning_opening = parent.findViewById(R.id.create_shop_thursday_start_morning_label);
        tiet_thursday_morning_opening = parent.findViewById(R.id.create_shop_thursday_start_morning_input);
        til_thursday_morning_closing = parent.findViewById(R.id.create_shop_thursday_end_morning_label);
        tiet_thursday_morning_closing = parent.findViewById(R.id.create_shop_thursday_end_morning_input);
        til_thursday_afternoon_opening = parent.findViewById(R.id.create_shop_thursday_start_afternoon_label);
        tiet_thursday_afternoon_opening = parent.findViewById(R.id.create_shop_thursday_start_afternoon_input);
        til_thursday_afternoon_closing = parent.findViewById(R.id.create_shop_thursday_end_afternoon_label);
        tiet_thursday_afternoon_closing = parent.findViewById(R.id.create_shop_thursday_end_afternoon_input);

        //Friday
        til_friday_morning_opening = parent.findViewById(R.id.create_shop_friday_start_morning_label);
        tiet_friday_morning_opening = parent.findViewById(R.id.create_shop_friday_start_morning_input);
        til_friday_morning_closing = parent.findViewById(R.id.create_shop_friday_end_morning_label);
        tiet_friday_morning_closing = parent.findViewById(R.id.create_shop_friday_end_morning_input);
        til_friday_afternoon_opening = parent.findViewById(R.id.create_shop_friday_start_afternoon_label);
        tiet_friday_afternoon_opening = parent.findViewById(R.id.create_shop_friday_start_afternoon_input);
        til_friday_afternoon_closing = parent.findViewById(R.id.create_shop_friday_end_afternoon_label);
        tiet_friday_afternoon_closing = parent.findViewById(R.id.create_shop_friday_end_afternoon_input);

        //Saturday
        til_saturday_morning_opening = parent.findViewById(R.id.create_shop_saturday_start_morning_label);
        tiet_saturday_morning_opening = parent.findViewById(R.id.create_shop_saturday_start_morning_input);
        til_saturday_morning_closing = parent.findViewById(R.id.create_shop_saturday_end_morning_label);
        tiet_saturday_morning_closing = parent.findViewById(R.id.create_shop_saturday_end_morning_input);
        til_saturday_afternoon_opening = parent.findViewById(R.id.create_shop_saturday_start_afternoon_label);
        tiet_saturday_afternoon_opening = parent.findViewById(R.id.create_shop_saturday_start_afternoon_input);
        til_saturday_afternoon_closing = parent.findViewById(R.id.create_shop_saturday_end_afternoon_label);
        tiet_saturday_afternoon_closing = parent.findViewById(R.id.create_shop_saturday_end_afternoon_input);

        //Sunday
        til_sunday_morning_opening = parent.findViewById(R.id.create_shop_sunday_start_morning_label);
        tiet_sunday_morning_opening = parent.findViewById(R.id.create_shop_sunday_start_morning_input);
        til_sunday_morning_closing = parent.findViewById(R.id.create_shop_sunday_end_morning_label);
        tiet_sunday_morning_closing = parent.findViewById(R.id.create_shop_sunday_end_morning_input);
        til_sunday_afternoon_opening = parent.findViewById(R.id.create_shop_sunday_start_afternoon_label);
        tiet_sunday_afternoon_opening = parent.findViewById(R.id.create_shop_sunday_start_afternoon_input);
        til_sunday_afternoon_closing = parent.findViewById(R.id.create_shop_sunday_end_afternoon_label);
        tiet_sunday_afternoon_closing = parent.findViewById(R.id.create_shop_sunday_end_afternoon_input);

    }

    private void getShopTypes(ConstraintLayout parent) {
        String url = BackEndEndpoints.SHOP_TYPES;

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                List<String> shop_translations = new ArrayList<>();
                String selected = "";
                shop_types = new JSONArray();
                for (int i = 0; i < response.length(); i++) {
                    String type = response.optString(i);
                    String type_translation = getString(getResources().getIdentifier(type, "string", getActivity().getPackageName()));
                    shop_translations.add(type_translation);
                    try {
                        shop_types.put(new JSONObject().put("type", type).put("translation", type_translation));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Collections.sort(shop_translations);
                String[] types = shop_translations.toArray(new String[shop_translations.size()]);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                        android.R.layout.simple_dropdown_item_1line, types);
                AutoCompleteTextView actv_type = parent.findViewById(R.id.create_shop_type_input);
                actv_type.setAdapter(adapter);

                if (shop != null) {
                    initShopParams(parent);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
            }
        });
    }

    private void initShopParams(ConstraintLayout parent) {
        //Name
        TextInputEditText tiet_name = parent.findViewById(R.id.create_shop_name_input);
        tiet_name.setText(shop.getName());

        //Type
        AutoCompleteTextView actv_type = parent.findViewById(R.id.create_shop_type_input);
        String type = "";
        for (int i = 0; i < shop_types.length(); i++) {
            JSONObject typeData = shop_types.optJSONObject(i);
            if (shop.getType().equals(typeData.optString("type"))) {
                type = typeData.optString("translation");
                break;
            }
        }
        actv_type.setText(type);

        //Location
        TextInputEditText tiet_location = parent.findViewById(R.id.create_shop_location_input);
        tiet_location.setText(shop.getLocation());

        //Latitude
        TextInputEditText tiet_latitude = parent.findViewById(R.id.create_shop_latitude_input);
        tiet_latitude.setText(Double.toString(shop.getLatitude()));

        //Longitude
        TextInputEditText tiet_longitude = parent.findViewById(R.id.create_shop_longitude_input);
        tiet_longitude.setText(Double.toString(shop.getLongitude()));

        //Max capacity
        TextInputEditText tiet_maxCapacity = parent.findViewById(R.id.create_shop_maxCapacity_input);
        tiet_maxCapacity.setText(Integer.toString(shop.getMaxCapacity()));

        //Timetable
        try {
            JSONArray timetableArray = new JSONArray(shop.getTimetable());
            for (int i = 0; i < timetableArray.length(); i++) {
                JSONObject timetableDay = timetableArray.getJSONObject(i);
                int weekkDay = timetableDay.getInt("weekDay");
                switch (weekkDay) {
                    case 0: // monday
                        tiet_monday_morning_opening.setText(timetableDay.optString("startMorning"));
                        tiet_monday_morning_closing.setText(timetableDay.optString("endMorning"));
                        tiet_monday_afternoon_opening.setText(timetableDay.optString("startAfternoon"));
                        tiet_monday_afternoon_closing.setText(timetableDay.optString("endAfternoon"));
                        break;
                    case 1: //tuesday
                        tiet_tuesday_morning_opening.setText(timetableDay.optString("startMorning"));
                        tiet_tuesday_morning_closing.setText(timetableDay.optString("endMorning"));
                        tiet_tuesday_afternoon_opening.setText(timetableDay.optString("startAfternoon"));
                        tiet_tuesday_afternoon_closing.setText(timetableDay.optString("endAfternoon"));
                        break;
                    case 2: //wednesday
                        tiet_wednesday_morning_opening.setText(timetableDay.optString("startMorning"));
                        tiet_wednesday_morning_closing.setText(timetableDay.optString("endMorning"));
                        tiet_wednesday_afternoon_opening.setText(timetableDay.optString("startAfternoon"));
                        tiet_wednesday_afternoon_closing.setText(timetableDay.optString("endAfternoon"));
                        break;
                    case 3: //thursday
                        tiet_thursday_morning_opening.setText(timetableDay.optString("startMorning"));
                        tiet_thursday_morning_closing.setText(timetableDay.optString("endMorning"));
                        tiet_thursday_afternoon_opening.setText(timetableDay.optString("startAfternoon"));
                        tiet_thursday_afternoon_closing.setText(timetableDay.optString("endAfternoon"));
                        break;
                    case 4: //friday
                        tiet_friday_morning_opening.setText(timetableDay.optString("startMorning"));
                        tiet_friday_morning_closing.setText(timetableDay.optString("endMorning"));
                        tiet_friday_afternoon_opening.setText(timetableDay.optString("startAfternoon"));
                        tiet_friday_afternoon_closing.setText(timetableDay.optString("endAfternoon"));
                        break;
                    case 5: //saturday
                        tiet_saturday_morning_opening.setText(timetableDay.optString("startMorning"));
                        tiet_saturday_morning_closing.setText(timetableDay.optString("endMorning"));
                        tiet_saturday_afternoon_opening.setText(timetableDay.optString("startAfternoon"));
                        tiet_saturday_afternoon_closing.setText(timetableDay.optString("endAfternoon"));
                        break;
                    case 6: //sunday
                        tiet_sunday_morning_opening.setText(timetableDay.optString("startMorning"));
                        tiet_sunday_morning_closing.setText(timetableDay.optString("endMorning"));
                        tiet_sunday_afternoon_opening.setText(timetableDay.optString("startAfternoon"));
                        tiet_sunday_afternoon_closing.setText(timetableDay.optString("endAfternoon"));
                        break;


                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initTimetable(ConstraintLayout parent, boolean setDefault) {

        //Monday
        TimetableUtils.initTimetableDay(getActivity(), setDefault, true, true,
                til_monday_morning_opening, tiet_monday_morning_opening,
                til_monday_morning_closing, tiet_monday_morning_closing,
                til_monday_afternoon_opening, tiet_monday_afternoon_opening,
                til_monday_afternoon_closing, tiet_monday_afternoon_closing);

        //Tuesday
        TimetableUtils.initTimetableDay(getActivity(), setDefault, true, true,
                til_tuesday_morning_opening, tiet_tuesday_morning_opening,
                til_tuesday_morning_closing, tiet_tuesday_morning_closing,
                til_tuesday_afternoon_opening, tiet_tuesday_afternoon_opening,
                til_tuesday_afternoon_closing, tiet_tuesday_afternoon_closing);

        //Wednesday
        TimetableUtils.initTimetableDay(getActivity(), setDefault, true, true,
                til_wednesday_morning_opening, tiet_wednesday_morning_opening,
                til_wednesday_morning_closing, tiet_wednesday_morning_closing,
                til_wednesday_afternoon_opening, tiet_wednesday_afternoon_opening,
                til_wednesday_afternoon_closing, tiet_wednesday_afternoon_closing);

        //Thursday
        TimetableUtils.initTimetableDay(getActivity(), setDefault, true, true,
                til_thursday_morning_opening, tiet_thursday_morning_opening,
                til_thursday_morning_closing, tiet_thursday_morning_closing,
                til_thursday_afternoon_opening, tiet_thursday_afternoon_opening,
                til_thursday_afternoon_closing, tiet_thursday_afternoon_closing);

        //Friday
        TimetableUtils.initTimetableDay(getActivity(), setDefault, true, true,
                til_friday_morning_opening, tiet_friday_morning_opening,
                til_friday_morning_closing, tiet_friday_morning_closing,
                til_friday_afternoon_opening, tiet_friday_afternoon_opening,
                til_friday_afternoon_closing, tiet_friday_afternoon_closing);

        //Saturday
        TimetableUtils.initTimetableDay(getActivity(), setDefault, true, false,
                til_saturday_morning_opening, tiet_saturday_morning_opening,
                til_saturday_morning_closing, tiet_saturday_morning_closing,
                til_saturday_afternoon_opening, tiet_saturday_afternoon_opening,
                til_saturday_afternoon_closing, tiet_saturday_afternoon_closing);

        //Sunday
        TimetableUtils.initTimetableDay(getActivity(), setDefault, false, false,
                til_sunday_morning_opening, tiet_sunday_morning_opening,
                til_sunday_morning_closing, tiet_sunday_morning_closing,
                til_sunday_afternoon_opening, tiet_sunday_afternoon_opening,
                til_sunday_afternoon_closing, tiet_sunday_afternoon_closing);

    }

    private boolean checkShop(ConstraintLayout parent) {
        boolean hasError = false;
        //Shop name
        TextInputLayout til_name = parent.findViewById(R.id.create_shop_name_label);
        til_name.setError(null);
        TextInputEditText tiet_name = parent.findViewById(R.id.create_shop_name_input);
        String name = tiet_name.getText().toString().trim();
        if (name.isEmpty()) {
            til_name.setError(getString(R.string.error_empty_field));
            til_name.requestFocus();
            hasError = true;
        }

        //Shop type
        TextInputLayout til_type = parent.findViewById(R.id.create_shop_type_label);
        til_type.setError(null);
        AutoCompleteTextView actv_type = parent.findViewById(R.id.create_shop_type_input);
        String type = actv_type.getText().toString().trim();
        if (type.isEmpty()) {
            til_type.setError(getString(R.string.error_empty_field));
            if (!hasError) {
                til_type.requestFocus();
            }
            hasError = true;
        }

        //Location

        //Latitude
        TextInputLayout til_latitude = parent.findViewById(R.id.create_shop_latitude_label);
        til_latitude.setError(null);
        TextInputEditText tiet_latitude = parent.findViewById(R.id.create_shop_latitude_input);
        String latitudeText = tiet_latitude.getText().toString().trim();
        if (latitudeText.isEmpty()) {
            til_latitude.setError(getString(R.string.error_empty_field));
            if (!hasError) {
                til_latitude.requestFocus();
            }
            hasError = true;

        } else {
            Float latitude = Float.parseFloat(latitudeText);
            if (!FormUtils.isValidLatitude(latitude)) {
                til_latitude.setError(getString(R.string.error_invalid_latitude));
                if (!hasError) {
                    til_latitude.requestFocus();
                }
                hasError = true;
            }
        }

        //Longitude
        TextInputLayout til_longitude = parent.findViewById(R.id.create_shop_longitude_label);
        til_longitude.setError(null);
        TextInputEditText tiet_longitude = parent.findViewById(R.id.create_shop_longitude_input);
        String longitudeText = tiet_latitude.getText().toString().trim();
        if (longitudeText.isEmpty()) {
            til_longitude.setError(getString(R.string.error_empty_field));
            if (!hasError) {
                til_longitude.requestFocus();
            }
            hasError = true;
        } else {
            Float longitude = Float.parseFloat(longitudeText);
            if (!FormUtils.isValidLongitude(longitude)) {
                til_longitude.setError(getString(R.string.error_invalid_longitude));
                if (!hasError) {
                    til_longitude.requestFocus();
                }
                hasError = true;
            }
        }

        // Max Capcity
        TextInputLayout til_maxCapacity = parent.findViewById(R.id.create_shop_maxCapacity_label);
        til_maxCapacity.setError(null);
        TextInputEditText tiet_maxCapacity = parent.findViewById(R.id.create_shop_maxCapacity_input);
        String maxCapacityText = tiet_maxCapacity.getText().toString().trim();
        if (maxCapacityText.isEmpty()) {
            til_maxCapacity.setError(getString(R.string.error_empty_field));
            if (!hasError) {
                til_maxCapacity.requestFocus();
            }
            hasError = true;
        } else {
            Integer maxCapacity = Integer.parseInt(maxCapacityText);
            if (maxCapacity < 1) {
                til_maxCapacity.setError(getString(R.string.error_max_capacity_below_1));
                if (!hasError) {
                    til_maxCapacity.requestFocus();
                }
                hasError = true;
            }
        }

        //Check timetable monday
        if (!TimetableUtils.isValidDayTimetable(getResources(),
                til_monday_morning_opening, tiet_monday_morning_opening,
                til_monday_morning_closing, tiet_monday_morning_closing,
                til_monday_afternoon_opening, tiet_monday_afternoon_opening,
                til_monday_afternoon_closing, tiet_monday_afternoon_closing)) {
            hasError = true;
        }

        //Check timetable tuesday
        if (!TimetableUtils.isValidDayTimetable(getResources(),
                til_tuesday_morning_opening, tiet_tuesday_morning_opening,
                til_tuesday_morning_closing, tiet_tuesday_morning_closing,
                til_tuesday_afternoon_opening, tiet_tuesday_afternoon_opening,
                til_tuesday_afternoon_closing, tiet_tuesday_afternoon_closing)) {
            hasError = true;
        }

        //Check timetable wednesday
        if (!TimetableUtils.isValidDayTimetable(getResources(),
                til_wednesday_morning_opening, tiet_wednesday_morning_opening,
                til_wednesday_morning_closing, tiet_wednesday_morning_closing,
                til_wednesday_afternoon_opening, tiet_wednesday_afternoon_opening,
                til_wednesday_afternoon_closing, tiet_wednesday_afternoon_closing)) {
            hasError = true;
        }

        //Check timetable thursday
        if (!TimetableUtils.isValidDayTimetable(getResources(),
                til_thursday_morning_opening, tiet_thursday_morning_opening,
                til_thursday_morning_closing, tiet_thursday_morning_closing,
                til_thursday_afternoon_opening, tiet_thursday_afternoon_opening,
                til_thursday_afternoon_closing, tiet_thursday_afternoon_closing)) {
            hasError = true;
        }

        //Check timetable friday
        if (!TimetableUtils.isValidDayTimetable(getResources(),
                til_friday_morning_opening, tiet_friday_morning_opening,
                til_friday_morning_closing, tiet_friday_morning_closing,
                til_friday_afternoon_opening, tiet_friday_afternoon_opening,
                til_friday_afternoon_closing, tiet_friday_afternoon_closing)) {
            hasError = true;
        }

        //Check timetable saturday
        if (!TimetableUtils.isValidDayTimetable(getResources(),
                til_saturday_morning_opening, tiet_saturday_morning_opening,
                til_saturday_morning_closing, tiet_saturday_morning_closing,
                til_saturday_afternoon_opening, tiet_saturday_afternoon_opening,
                til_saturday_afternoon_closing, tiet_saturday_afternoon_closing)) {
            hasError = true;
        }

        //Check timetable sunday
        if (!TimetableUtils.isValidDayTimetable(getResources(),
                til_sunday_morning_opening, tiet_sunday_morning_opening,
                til_sunday_morning_closing, tiet_sunday_morning_closing,
                til_sunday_afternoon_opening, tiet_sunday_afternoon_opening,
                til_sunday_afternoon_closing, tiet_sunday_afternoon_closing)) {
            hasError = true;
        }
        return hasError;
    }

    private void createShop(ConstraintLayout parent) {
        if (!checkShop(parent)) {
            //Name
            TextInputEditText tiet_name = parent.findViewById(R.id.create_shop_name_input);
            String name = tiet_name.getText().toString().trim();

            //Type
            AutoCompleteTextView actv_type = parent.findViewById(R.id.create_shop_type_input);
            String type_translation = actv_type.getText().toString().trim();
            String type = "";
            for (int i = 0; i < shop_types.length(); i++) {
                JSONObject typeData = shop_types.optJSONObject(i);
                if (type_translation.equals(typeData.optString("translation"))) {
                    type = typeData.optString("type");
                    break;
                }
            }

            //Location
            TextInputEditText tiet_location = parent.findViewById(R.id.create_shop_location_input);
            String location = tiet_location.getText().toString().trim();

            //Latitude
            TextInputEditText tiet_latitude = parent.findViewById(R.id.create_shop_latitude_input);
            Float latitude = Float.parseFloat(tiet_latitude.getText().toString().trim());

            //Longitude
            TextInputEditText tiet_longitude = parent.findViewById(R.id.create_shop_latitude_input);
            Float longitude = Float.parseFloat(tiet_longitude.getText().toString().trim());

            //Max capacity
            TextInputEditText tiet_maxCapacity = parent.findViewById(R.id.create_shop_maxCapacity_input);
            Integer maxCapacity = Integer.parseInt(tiet_maxCapacity.getText().toString().trim());

            //Timetable
            JSONArray timetable = new JSONArray();

            //monday
            JSONObject timetable_monday = TimetableUtils.crateDayTimetableJSON(0, tiet_monday_morning_opening, tiet_monday_morning_closing, tiet_monday_afternoon_opening, tiet_monday_afternoon_closing);
            if (timetable_monday != null) {
                timetable.put(timetable_monday);
            }

            //tuesday
            JSONObject timetable_tuesday = TimetableUtils.crateDayTimetableJSON(1, tiet_tuesday_morning_opening, tiet_tuesday_morning_closing, tiet_tuesday_afternoon_opening, tiet_tuesday_afternoon_closing);
            if (timetable_tuesday != null) {
                timetable.put(timetable_tuesday);
            }

            //wednesday
            JSONObject timetable_wednesday = TimetableUtils.crateDayTimetableJSON(2, tiet_wednesday_morning_opening, tiet_wednesday_morning_closing, tiet_wednesday_afternoon_opening, tiet_wednesday_afternoon_closing);
            if (timetable_wednesday != null) {
                timetable.put(timetable_wednesday);
            }

            //thursday
            JSONObject timetable_thursday = TimetableUtils.crateDayTimetableJSON(3, tiet_thursday_morning_opening, tiet_thursday_morning_closing, tiet_thursday_afternoon_opening, tiet_thursday_afternoon_closing);
            if (timetable_thursday != null) {
                timetable.put(timetable_thursday);
            }

            //friday
            JSONObject timetable_friday = TimetableUtils.crateDayTimetableJSON(4, tiet_friday_morning_opening, tiet_friday_morning_closing, tiet_friday_afternoon_opening, tiet_friday_afternoon_closing);
            if (timetable_friday != null) {
                timetable.put(timetable_friday);
            }

            //saturday
            JSONObject timetable_saturday = TimetableUtils.crateDayTimetableJSON(5, tiet_saturday_morning_opening, tiet_saturday_morning_closing, tiet_saturday_afternoon_opening, tiet_saturday_afternoon_closing);
            if (timetable_saturday != null) {
                timetable.put(timetable_saturday);
            }

            //sunday
            JSONObject timetable_sunday = TimetableUtils.crateDayTimetableJSON(6, tiet_sunday_morning_opening, tiet_sunday_morning_closing, tiet_sunday_afternoon_opening, tiet_sunday_afternoon_closing);
            if (timetable_sunday != null) {
                timetable.put(timetable_sunday);
            }

            Shop newShop = new Shop(name, latitude, longitude, location, maxCapacity, type, seller.getIdSeller(), timetable.toString());

            String url = BackEndEndpoints.SHOP_BASE;
            if (shop != null) {
                url = BackEndEndpoints.SHOP_BASE + "/" + shop.getIdShop();
            }

            int method = Request.Method.POST;
            if (shop != null) {
                method = Request.Method.PUT;
            }

            JSONObject shopJSON = ModelConverter.shopToJsonObject(newShop);

            ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);

            RequestUtils.sendJsonObjectRequest(getContext(), method, url, shopJSON, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    pd.dismiss();
                    if (shop == null) {
                        mCallback.shopCreated();
                    } else {
                        Shop shopUpdated = ModelConverter.jsonObjectToShop(response);
                        mCallback.shopUpdated(shop);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pd.dismiss();
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                                pd.show();
                                createShop(parent);
                            }
                        });
                        snackbar.show();
                    } else {
                        int responseCode = RequestUtils.getErrorCodeRequest(error);
                        //404 seller not found (should not happen)
                        //401 maxCapacity exceeded (should not happen)
                        //400 shop duplicated
                        if (responseCode == 401) {
                            TextInputLayout til_maxCapacity = parent.findViewById(R.id.create_shop_maxCapacity_label);
                            til_maxCapacity.setError(getString(R.string.error_max_capacity_below_1));
                            til_maxCapacity.requestFocus();
                        } else if (responseCode == 400) {
                            Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_shop_duplicate), Snackbar.LENGTH_LONG);
                            snackbar.show();
                            TextInputLayout til_name = parent.findViewById(R.id.create_shop_name_label);
                            til_name.requestFocus();
                            til_name.clearFocus();
                        } else {
                            Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                    pd.show();
                                    createShop(parent);
                                }
                            });
                            snackbar.show();
                        }
                    }
                }
            });
        }
    }
}