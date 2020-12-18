package es.ewic.sellers.utils;

public class BackEndEndpoints {

    //Coruña
    public static String BASE_ENDPOINT = "http://192.168.1.44:8080/ewic";

    //Seller
    public static String SELLER_LOGIN = BASE_ENDPOINT + "/seller/login";


    //SHOP
    public static String SHOP_BASE = BASE_ENDPOINT + "/shop";
    public static String SELLER_SHOPS = SHOP_BASE + "/seller";
    public static String SHOP_OPEN = "/open";
    public static String SHOP_CLOSE = "/close";


}
