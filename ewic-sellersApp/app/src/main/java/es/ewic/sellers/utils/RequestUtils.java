package es.ewic.sellers.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestUtils {

    //Error messages
    public static final String SHOP_NOT_OPENED = "Shop not opened";
    public static final String CLIENT_ALREADY_ENTERED = "Client already entered";
    public static final String EXIT_ALREADY_REGISTERED = "Exit already registered";

    public static final String RESERVATION_WHEN_SHOP_FULL = "Reservation when shop full";
    public static final String MOVE_RESERVATION_TO_PAST = "Move reservation to past";

    public static void sendJsonArrayRequest(Context context, int mehod, String url, JSONArray jsonRequest,
                                            Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(mehod, url, jsonRequest, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        queue.add(jsonArrayRequest);
    }

    public static void sendJsonObjectRequest(Context context, int method, String url, JSONObject jsonRequest,
                                             Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public static void sendStringRequest(Context context, int method, String url, Response.Listener<String> listener,
                                         Response.ErrorListener errorListener) {

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(method, url, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public static String getErrorMessageRequest(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            JSONObject data = new JSONObject(responseBody);
            return data.getString("message");
        } catch (JSONException e) {
            return null;
        }
    }

    public static int getErrorCodeRequest(VolleyError error) {
        return error.networkResponse.statusCode;
    }
}
