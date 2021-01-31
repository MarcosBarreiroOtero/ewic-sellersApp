package es.ewic.sellers.utils;

public class BackEndEndpoints {

    //Coruña
    public static String BASE_ENDPOINT = "http://192.168.1.44:8080/ewic";

    //Seller
    public static String SELLER_BASE = BASE_ENDPOINT + "/seller";
    public static String SELLER_LOGIN = SELLER_BASE + "/login";
    public static String SELLER_CHANGE_PASSWORD = SELLER_BASE + "/password";

    //SHOP
    public static String SHOP_BASE = BASE_ENDPOINT + "/shop";
    public static String SELLER_SHOPS = SHOP_BASE + "/seller";
    public static String SHOP_TYPES = SHOP_BASE + "/types";

    public static String SHOP_OPEN(int idShop) {
        return SHOP_BASE + "/" + idShop + "/open";
    }

    public static String SHOP_CLOSE(int idShop) {
        return SHOP_BASE + "/" + idShop + "/close";
    }

    //Entry
    public static String ENTRY_CLIENT(int idShop, String idGoogleLogin) {
        return SHOP_BASE + "/" + idShop + "/entry?idGoogleLogin=" + idGoogleLogin;
    }

    public static String EXIT_CLIENT(int idShop, int idEntry) {
        return SHOP_BASE + "/" + idShop + "/exit?entryNumber=" + idEntry;
    }

    //RESERVATION
    public static String RESERVATION_BASE = BASE_ENDPOINT + "/reservation/seller";

    //CLIENT
    public static String CLIENT_BASE = BASE_ENDPOINT + "/client";

    //CONFIGURATION
    public static String CONFIGURATION_BASE = BASE_ENDPOINT + "/configuration";

    public static String CONFIGURATION_BATCH(int idShop) {
        return CONFIGURATION_BASE + "/" + idShop + "/batch";
    }

    public static String CONFIGURATION_RESERVATION(int idShop) {
        return CONFIGURATION_BASE + "/" + idShop + "/reservation";
    }


}
