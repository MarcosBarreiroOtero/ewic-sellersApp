package es.ewic.sellers;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateShopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateShopFragment extends Fragment {

    private static final String ARG_SHOP = "shop";

    private Shop shop;

    private boolean showing_general = true;
    private boolean showing_location = true;
    private boolean showing_capacity = true;
    private boolean showing_timetable = false;

    private JSONArray shop_types;

    public CreateShopFragment() {
        // Required empty public constructor
    }

    public static CreateShopFragment newInstance(Shop shopData) {
        CreateShopFragment fragment = new CreateShopFragment();
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

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.create_shop);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_create_shop, container, false);

        getShopTypes(parent);

        ScrollView sv = parent.findViewById(R.id.create_shop_scrollView);

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
                toggleVisibilityTimetable(parent);

                if (showing_timetable) {
                    //TODO mover scroll hasta el final de la scrollView (o hacer focus sobre algún input)
                    //extView sunday = parent.findViewById(R.id.create_shop_sunday_text);
                    //sv.scrollTo(0, sunday.getBottom());
                }

            }
        });

        initTimetable(parent);

        return parent;
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
            }
        });
    }

    private void handleTimetableClick(TextInputEditText tiet) {
        tiet.setInputType(InputType.TYPE_NULL);
        tiet.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String value = tiet.getText().toString().trim();
                    int hour = value.equals("") ? 0 : Integer.parseInt(value.split(":")[0]);
                    int minute = value.equals("") ? 0 : Integer.parseInt(value.split(":")[1]);

                    TimePickerDialog picker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            tiet.setText((hourOfDay < 10 ? "0" : "") + hourOfDay + ":" + (minute < 10 ? "0" : "") + minute);
                            tiet.clearFocus();
                        }
                    }, hour, minute, true);
                    picker.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            tiet.clearFocus();
                        }
                    });
                    picker.show();
                }
                ;
            }
        });
    }

    private void initTimetable(ConstraintLayout parent) {
        TextInputEditText tiet_monday_start_morning = parent.findViewById(R.id.create_shop_monday_start_morning_input);
        handleTimetableClick(tiet_monday_start_morning);
        tiet_monday_start_morning.setText("09:30");
        TextInputEditText tiet_monday_end_morning = parent.findViewById(R.id.create_shop_monday_end_morning_input);
        handleTimetableClick(tiet_monday_end_morning);
        tiet_monday_end_morning.setText("13:30");
        TextInputEditText tiet_monday_start_afternoon = parent.findViewById(R.id.create_shop_monday_start_afternoon_input);
        handleTimetableClick(tiet_monday_start_afternoon);
        tiet_monday_start_afternoon.setText("16:00");
        TextInputEditText tiet_monday_end_afternoon = parent.findViewById(R.id.create_shop_monday_end_afternoon_input);
        handleTimetableClick(tiet_monday_end_afternoon);
        tiet_monday_end_afternoon.setText("20:00");

        TextInputEditText tiet_tuesday_start_morning = parent.findViewById(R.id.create_shop_tuesday_start_morning_input);
        handleTimetableClick(tiet_tuesday_start_morning);
        tiet_tuesday_start_morning.setText("09:30");
        TextInputEditText tiet_tuesday_end_morning = parent.findViewById(R.id.create_shop_tuesday_end_morning_input);
        handleTimetableClick(tiet_tuesday_end_morning);
        tiet_tuesday_end_morning.setText("13:30");
        TextInputEditText tiet_tuesday_start_afternoon = parent.findViewById(R.id.create_shop_tuesday_start_afternoon_input);
        handleTimetableClick(tiet_tuesday_start_afternoon);
        tiet_tuesday_start_afternoon.setText("16:00");
        TextInputEditText tiet_tuesday_end_afternoon = parent.findViewById(R.id.create_shop_tuesday_end_afternoon_input);
        handleTimetableClick(tiet_tuesday_end_afternoon);
        tiet_tuesday_end_afternoon.setText("20:00");

        TextInputEditText tiet_wednesday_start_morning = parent.findViewById(R.id.create_shop_wednesday_start_morning_input);
        handleTimetableClick(tiet_wednesday_start_morning);
        tiet_wednesday_start_morning.setText("09:30");
        TextInputEditText tiet_wednesday_end_morning = parent.findViewById(R.id.create_shop_wednesday_end_morning_input);
        handleTimetableClick(tiet_wednesday_end_morning);
        tiet_wednesday_end_morning.setText("13:30");
        TextInputEditText tiet_wednesday_start_afternoon = parent.findViewById(R.id.create_shop_wednesday_start_afternoon_input);
        handleTimetableClick(tiet_wednesday_start_afternoon);
        tiet_wednesday_start_afternoon.setText("16:00");
        TextInputEditText tiet_wednesday_end_afternoon = parent.findViewById(R.id.create_shop_wednesday_end_afternoon_input);
        handleTimetableClick(tiet_wednesday_end_afternoon);
        tiet_wednesday_end_afternoon.setText("20:00");

        TextInputEditText tiet_thursday_start_morning = parent.findViewById(R.id.create_shop_thursday_start_morning_input);
        handleTimetableClick(tiet_thursday_start_morning);
        tiet_thursday_start_morning.setText("09:30");
        TextInputEditText tiet_thursday_end_morning = parent.findViewById(R.id.create_shop_thursday_end_morning_input);
        handleTimetableClick(tiet_thursday_end_morning);
        tiet_thursday_end_morning.setText("13:30");
        TextInputEditText tiet_thursday_start_afternoon = parent.findViewById(R.id.create_shop_thursday_start_afternoon_input);
        handleTimetableClick(tiet_thursday_start_afternoon);
        tiet_thursday_start_afternoon.setText("16:00");
        TextInputEditText tiet_thursday_end_afternoon = parent.findViewById(R.id.create_shop_thursday_end_afternoon_input);
        handleTimetableClick(tiet_thursday_end_afternoon);
        tiet_thursday_end_afternoon.setText("20:00");

        TextInputEditText tiet_friday_start_morning = parent.findViewById(R.id.create_shop_friday_start_morning_input);
        handleTimetableClick(tiet_friday_start_morning);
        tiet_friday_start_morning.setText("09:30");
        TextInputEditText tiet_friday_end_morning = parent.findViewById(R.id.create_shop_friday_end_morning_input);
        handleTimetableClick(tiet_friday_end_morning);
        tiet_friday_end_morning.setText("13:30");
        TextInputEditText tiet_friday_start_afternoon = parent.findViewById(R.id.create_shop_friday_start_afternoon_input);
        handleTimetableClick(tiet_friday_start_afternoon);
        tiet_friday_start_afternoon.setText("16:00");
        TextInputEditText tiet_friday_end_afternoon = parent.findViewById(R.id.create_shop_friday_end_afternoon_input);
        handleTimetableClick(tiet_friday_end_afternoon);
        tiet_friday_end_afternoon.setText("20:00");

        TextInputEditText tiet_saturday_start_morning = parent.findViewById(R.id.create_shop_saturday_start_morning_input);
        handleTimetableClick(tiet_saturday_start_morning);
        tiet_saturday_start_morning.setText("09:30");
        TextInputEditText tiet_saturday_end_morning = parent.findViewById(R.id.create_shop_saturday_end_morning_input);
        handleTimetableClick(tiet_saturday_end_morning);
        tiet_saturday_end_morning.setText("13:30");
        TextInputEditText tiet_saturday_start_afternoon = parent.findViewById(R.id.create_shop_saturday_start_afternoon_input);
        handleTimetableClick(tiet_saturday_start_afternoon);
        TextInputEditText tiet_saturday_end_afternoon = parent.findViewById(R.id.create_shop_saturday_end_afternoon_input);
        handleTimetableClick(tiet_saturday_end_afternoon);

        TextInputEditText tiet_sunday_start_morning = parent.findViewById(R.id.create_shop_sunday_start_morning_input);
        handleTimetableClick(tiet_sunday_start_morning);
        TextInputEditText tiet_sunday_end_morning = parent.findViewById(R.id.create_shop_sunday_end_morning_input);
        handleTimetableClick(tiet_sunday_end_morning);
        TextInputEditText tiet_sunday_start_afternoon = parent.findViewById(R.id.create_shop_sunday_start_afternoon_input);
        handleTimetableClick(tiet_sunday_start_afternoon);
        TextInputEditText tiet_sunday_end_afternoon = parent.findViewById(R.id.create_shop_sunday_end_afternoon_input);
        handleTimetableClick(tiet_sunday_end_afternoon);
    }

    private void toggleVisibilityTimetable(ConstraintLayout parent) {
        parent.findViewById(R.id.create_shop_morning_text).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_afternoon_text).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);

        parent.findViewById(R.id.opening_morning).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.closing_morning).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);

        parent.findViewById(R.id.opening_afternoon).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.closing_afternoon).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);

        parent.findViewById(R.id.create_shop_monday_text).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_monday_start_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_monday_end_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_monday_start_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_monday_end_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);

        parent.findViewById(R.id.create_shop_tuesday_text).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_tuesday_start_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_tuesday_end_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_tuesday_start_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_tuesday_end_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);

        parent.findViewById(R.id.create_shop_wednesday_text).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_wednesday_start_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_wednesday_end_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_wednesday_start_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_wednesday_end_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);

        parent.findViewById(R.id.create_shop_thursday_text).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_thursday_start_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_thursday_end_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_thursday_start_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_thursday_end_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);

        parent.findViewById(R.id.create_shop_friday_text).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_friday_start_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_friday_end_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_friday_start_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_friday_end_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);

        parent.findViewById(R.id.create_shop_saturday_text).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_saturday_start_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_saturday_end_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_saturday_start_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_saturday_end_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);

        parent.findViewById(R.id.create_shop_sunday_text).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_sunday_start_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_sunday_end_morning_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_sunday_start_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
        parent.findViewById(R.id.create_shop_sunday_end_afternoon_label).setVisibility(showing_timetable ? View.VISIBLE : View.GONE);
    }
}