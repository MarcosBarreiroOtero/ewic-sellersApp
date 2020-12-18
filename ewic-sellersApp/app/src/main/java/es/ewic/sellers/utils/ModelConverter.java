package es.ewic.sellers.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    //SHOP
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

}
