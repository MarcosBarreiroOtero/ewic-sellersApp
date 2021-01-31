package es.ewic.sellers.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.ewic.sellers.model.Client;
import es.ewic.sellers.model.Entry;
import es.ewic.sellers.model.Reservation;
import es.ewic.sellers.model.Seller;
import es.ewic.sellers.model.Shop;

public class ModelConverter {

    //SELLER
    public static Seller jsonObjectToSeller(JSONObject sellerData) {
        try {
            return new Seller(sellerData.getInt("idSeller"),
                    sellerData.getString("loginName"),
                    sellerData.getString("firstName"),
                    sellerData.getString("lastName"),
                    sellerData.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject sellerToJsonObject(Seller seller) {
        try {
            return new JSONObject().put("loginName", seller.getLoginName())
                    .put("password", seller.getPassword())
                    .put("firstName", seller.getFirstName())
                    .put("lastName", seller.getLastName())
                    .put("email", seller.getEmail());
        } catch (JSONException e) {
            return null;
        }
    }

    //SHOP
    public static JSONObject shopToJsonObject(Shop shop) {
        try {
            return new JSONObject()
                    .put("name", shop.getName())
                    .put("latitude", shop.getLatitude())
                    .put("longitude", shop.getLongitude())
                    .put("location", shop.getLocation())
                    .put("maxCapacity", shop.getMaxCapacity())
                    .put("type", shop.getType())
                    .put("idSeller", shop.getIdSeller())
                    .put("timetable", shop.getTimetable());
        } catch (JSONException e) {
            return null;
        }
    }

    public static Shop jsonObjectToShop(JSONObject shopData) {
        try {
            String timetableValue = shopData.optString("timetable");
            JSONArray timetable = new JSONArray();
            if (timetableValue != null && !timetableValue.toLowerCase().equals("null")) {
                timetable = new JSONArray(timetableValue);
            }
            return new Shop(shopData.getInt("idShop"),
                    shopData.getString("name"),
                    shopData.getDouble("latitude"),
                    shopData.getDouble("longitude"),
                    shopData.getString("location"),
                    shopData.getInt("maxCapacity"),
                    shopData.getInt("actualCapacity"),
                    shopData.getString("type"),
                    shopData.getBoolean("allowEntries"),
                    shopData.getInt("idSeller"),
                    timetable.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("SHOP_ERROR", "Error", e);
            return null;
        }
    }

    public static List<Shop> jsonArrayToShopList(JSONArray shopsData) {
        ArrayList<Shop> shops = new ArrayList<>();
        for (int i = 0; i < shopsData.length(); i++) {
            JSONObject shopData = shopsData.optJSONObject(i);
            shops.add(jsonObjectToShop(shopData));
        }
        return shops;
    }

    //RESERVATION
    public static JSONObject reservationToJsonObject(Reservation reservation) {
        Calendar reservationDate = reservation.getDate();
        Calendar reservationDateFormatted = DateUtils.changeCalendarTimezoneFromDefaultToUTC(reservationDate);
        try {
            return new JSONObject().put("date", DateUtils.formatDateLong(reservationDateFormatted))
                    .put("remarks", reservation.getRemarks())
                    .put("nClients", reservation.getnClients())
                    .put("idGoogleLoginClient", reservation.getIdGoogleLoginClient())
                    .put("idShop", reservation.getIdShop());
        } catch (JSONException e) {
            return null;
        }
    }

    public static Reservation jsonObjectToReservation(JSONObject reservationData) {
        try {
            // UTC date
            Calendar reservationDate = DateUtils.parseDateLong(reservationData.getString("date"));
            reservationDate = DateUtils.changeCalendarTimezoneFromUTCToDefault(reservationDate);
            return new Reservation(reservationData.getInt("idReservation"),
                    reservationDate,
                    reservationData.getString("state"),
                    reservationData.getString("remarks"),
                    reservationData.getInt("nClients"),
                    reservationData.getString("idGoogleLoginClient"),
                    reservationData.getInt("idShop"),
                    reservationData.getString("clientName"));
        } catch (JSONException e) {
            return null;
        }

    }

    public static List<Reservation> jsonArrayToReservationList(JSONArray reservationsData) {
        ArrayList<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < reservationsData.length(); i++) {
            JSONObject reservationData = reservationsData.optJSONObject(i);
            reservations.add(jsonObjectToReservation(reservationData));
        }
        return reservations;
    }

    //Client
    public static Client jsonObjectToClient(JSONObject clientData) {
        try {
            return new Client(clientData.getInt("idClient"),
                    clientData.getString("idGoogleLogin"),
                    clientData.getString("firstName"),
                    clientData.getString("lastName"),
                    clientData.getString("email"));
        } catch (JSONException e) {
            return null;
        }
    }

    public static List<Client> jsonArrayToClientList(JSONArray clientsData) {
        ArrayList<Client> clients = new ArrayList<>();
        for (int i = 0; i < clientsData.length(); i++) {
            JSONObject clientData = clientsData.optJSONObject(i);
            clients.add(jsonObjectToClient(clientData));
        }
        return clients;
    }

    //Entry
    public static Entry jsonObjectToEntry(JSONObject entryData) {
        try {

            Calendar startDate = DateUtils.parseDateLong(entryData.getString("start"));
            startDate = DateUtils.changeCalendarTimezoneFromUTCToDefault(startDate);

            Calendar endDate = null;
            if (entryData.getString("end") != null) {
                endDate = DateUtils.parseDateLong(entryData.getString("end"));
                endDate = DateUtils.changeCalendarTimezoneFromUTCToDefault(endDate);
            }

            return new Entry(entryData.getInt("entryNumber"),
                    startDate, endDate,
                    entryData.getLong("duration"),
                    entryData.getString("description"),
                    entryData.getString("shopName"),
                    entryData.getString("clientName"));
        } catch (JSONException e) {
            return null;
        }
    }

    public static List<Entry> jsonArrayToEntriList(JSONArray entriesData) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < entriesData.length(); i++) {
            JSONObject entryData = entriesData.optJSONObject(i);
            entries.add(jsonObjectToEntry(entryData));
        }
        return entries;
    }

}
