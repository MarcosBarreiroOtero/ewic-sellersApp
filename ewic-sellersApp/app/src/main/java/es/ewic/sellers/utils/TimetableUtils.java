package es.ewic.sellers.utils;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.ewic.sellers.R;

public class TimetableUtils {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public static boolean isValidDayTimetable(Resources resources, TextInputLayout til_morning_opening, TextInputEditText tiet_morning_opening,
                                              TextInputLayout til_morning_closing, TextInputEditText tiet_morning_closing,
                                              TextInputLayout til_afternoon_opening, TextInputEditText tiet_afternoon_opening,
                                              TextInputLayout til_afternoon_closing, TextInputEditText tiet_afternoon_closing) {
        boolean hasError = false;

        //Morning
        til_morning_opening.setError(null);
        til_morning_closing.setError(null);

        String morning_opening = tiet_morning_opening.getText().toString().trim();
        String morning_closing = tiet_morning_closing.getText().toString().trim();

        if (!morning_opening.isEmpty() && !morning_closing.isEmpty()) {
            if (FormUtils.isValidTime(morning_opening) && FormUtils.isValidTime(morning_closing)) {
                try {
                    Date opening = sdf.parse(morning_opening);
                    Date closing = sdf.parse(morning_closing);
                    if (opening.compareTo(closing) >= 0) {
                        til_morning_opening.setError(resources.getString(R.string.error_timetable));
                        til_morning_closing.setError(resources.getString(R.string.error_timetable));
                        hasError = true;
                    }
                } catch (ParseException e) {
                    til_morning_opening.setError(resources.getString(R.string.error_time_format));
                    til_morning_closing.setError(resources.getString(R.string.error_time_format));
                    hasError = true;
                }

            } else {
                til_morning_opening.setError(FormUtils.isValidTime(morning_opening) ? resources.getString(R.string.error_time_format) : null);
                til_morning_closing.setError(FormUtils.isValidTime(morning_closing) ? resources.getString(R.string.error_time_format) : null);
                hasError = true;
            }
        } else if (!morning_opening.isEmpty() || !morning_closing.isEmpty()) {
            til_morning_opening.setError(morning_opening.isEmpty() ? resources.getString(R.string.error_empty_field) : null);
            til_morning_closing.setError(morning_closing.isEmpty() ? resources.getString(R.string.error_empty_field) : null);
            hasError = true;
        }

        //Afternoon
        til_afternoon_opening.setError(null);
        til_afternoon_closing.setError(null);

        String afternoon_opening = tiet_afternoon_opening.getText().toString().trim();
        String afternoon_closing = tiet_afternoon_closing.getText().toString().trim();

        if (!afternoon_opening.isEmpty() && !afternoon_closing.isEmpty()) {
            if (FormUtils.isValidTime(afternoon_opening) && FormUtils.isValidTime(afternoon_closing)) {
                try {
                    Date opening = sdf.parse(afternoon_opening);
                    Date closing = sdf.parse(afternoon_closing);
                    if (opening.compareTo(closing) >= 0) {
                        til_afternoon_opening.setError(resources.getString(R.string.error_timetable));
                        til_afternoon_closing.setError(resources.getString(R.string.error_timetable));
                        hasError = true;
                    }
                } catch (ParseException e) {
                    til_afternoon_opening.setError(resources.getString(R.string.error_time_format));
                    til_afternoon_closing.setError(resources.getString(R.string.error_time_format));
                    hasError = true;
                }

            } else {
                til_afternoon_opening.setError(FormUtils.isValidTime(afternoon_opening) ? resources.getString(R.string.error_time_format) : null);
                til_afternoon_closing.setError(FormUtils.isValidTime(afternoon_closing) ? resources.getString(R.string.error_time_format) : null);
                hasError = true;
            }
        } else if (!afternoon_opening.isEmpty() || !afternoon_closing.isEmpty()) {
            til_afternoon_opening.setError(afternoon_opening.isEmpty() ? resources.getString(R.string.error_empty_field) : null);
            til_afternoon_closing.setError(afternoon_closing.isEmpty() ? resources.getString(R.string.error_empty_field) : null);
            hasError = true;
        }

        //Check collisions

        if (!morning_closing.isEmpty() && !afternoon_opening.isEmpty()) {
            try {
                Date closing_morning = sdf.parse(morning_closing);
                Date opening_afternoon = sdf.parse(afternoon_opening);
                if (closing_morning.compareTo(opening_afternoon) > 0) {
                    til_morning_closing.setError(resources.getString(R.string.error_timetable));
                    til_afternoon_opening.setError(resources.getString(R.string.error_timetable));
                    hasError = true;
                }
            } catch (ParseException e) {
                til_morning_opening.setError(resources.getString(R.string.error_time_format));
                til_afternoon_opening.setError(resources.getString(R.string.error_time_format));
                hasError = true;
            }

        }
        if (til_morning_opening.getChildCount() == 2) {
            til_morning_opening.getChildAt(1).setVisibility(View.GONE);
        }
        if (til_morning_closing.getChildCount() == 2) {
            til_morning_closing.getChildAt(1).setVisibility(View.GONE);
        }
        if (til_afternoon_opening.getChildCount() == 2) {
            til_afternoon_opening.getChildAt(1).setVisibility(View.GONE);
        }
        if (til_afternoon_closing.getChildCount() == 2) {
            til_afternoon_closing.getChildAt(1).setVisibility(View.GONE);
        }
        return !hasError;
    }

    public static void initTimetableDay(Activity activity, boolean setDefault, boolean initMorning, boolean initAfternoon,
                                        TextInputLayout til_morning_opening, TextInputEditText tiet_morning_opening,
                                        TextInputLayout til_morning_closing, TextInputEditText tiet_morning_closing,
                                        TextInputLayout til_afternoon_opening, TextInputEditText tiet_afternoon_opening,
                                        TextInputLayout til_afternoon_closing, TextInputEditText tiet_afternoon_closing) {
        handleTimetableClick(activity, til_morning_opening, tiet_morning_opening);
        handleTimetableClick(activity, til_morning_closing, tiet_morning_closing);
        handleTimetableClick(activity, til_afternoon_opening, tiet_afternoon_opening);
        handleTimetableClick(activity, til_afternoon_closing, tiet_afternoon_closing);

        if (setDefault) {
            if (initMorning) {
                tiet_morning_opening.setText("09:30");
                tiet_morning_closing.setText("13:30");
            }

            if (initAfternoon) {
                tiet_afternoon_opening.setText("16:00");
                tiet_afternoon_closing.setText("20:00");
            }
        }
    }

    public static void handleTimetableClick(Activity activity, TextInputLayout til, TextInputEditText tiet) {
        tiet.setInputType(InputType.TYPE_NULL);
        tiet.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String value = tiet.getText().toString().trim();
                    int hour = value.equals("") ? 0 : Integer.parseInt(value.split(":")[0]);
                    int minute = value.equals("") ? 0 : Integer.parseInt(value.split(":")[1]);
                    til.setError(null);
                    TimePickerDialog picker = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            tiet.setText((hourOfDay < 10 ? "0" : "") + hourOfDay + ":" + (minute < 10 ? "0" : "") + minute);
                            tiet.clearFocus();
                        }
                    }, hour, minute, true);
                    picker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            tiet.setText("");
                            dialog.dismiss();
                        }
                    });
                    picker.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Log.e("TIMEPICKER", "Dismiss");
                            tiet.clearFocus();
                        }
                    });
                    picker.show();
                }
            }
        });
    }

    public static void toogleTimetableVisibility(ConstraintLayout parent, boolean showing_timetable) {
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

    public static JSONObject crateDayTimetableJSON(int weekday, TextInputEditText tiet_morning_opening, TextInputEditText tiet_morning_closing, TextInputEditText tiet_afternoon_opening, TextInputEditText tiet_afternoon_closing) {
        String morning_opening = tiet_morning_opening.getText().toString().trim();
        String morning_closing = tiet_morning_closing.getText().toString().trim();
        String afternoon_opening = tiet_afternoon_opening.getText().toString().trim();
        String afternoon_closing = tiet_afternoon_closing.getText().toString().trim();

        if (morning_opening.isEmpty() && morning_closing.isEmpty() && afternoon_opening.isEmpty() && afternoon_closing.isEmpty()) {
            return null;
        }

        try {
            JSONObject timetableJSON = new JSONObject();

            timetableJSON.putOpt("weekDay", weekday);


            if (!morning_opening.isEmpty() && !morning_closing.isEmpty()) {
                timetableJSON.putOpt("startMorning", morning_opening);
                timetableJSON.putOpt("endMorning", morning_closing);
            }

            if (!afternoon_opening.isEmpty() && !afternoon_closing.isEmpty()) {
                timetableJSON.putOpt("startAfternoon", afternoon_opening);
                timetableJSON.putOpt("endAfternoon", afternoon_closing);
            }

            return timetableJSON;
        } catch (JSONException e) {
            return null;
        }

    }
}
