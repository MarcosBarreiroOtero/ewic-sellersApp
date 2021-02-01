package es.ewic.sellers.adapters;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.List;

import es.ewic.sellers.R;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.ImageUtils;
import es.ewic.sellers.utils.RequestUtils;

public class ShopRowAdapter extends BaseAdapter implements ListAdapter {

    private final List<Shop> shopList;
    private final Fragment fragment;
    private final Resources resources;
    private final String packageName;

    public ShopRowAdapter(Fragment fragment, List<Shop> shopList, Resources resources, String packageName) {
        assert fragment != null;
        assert shopList != null;
        assert resources != null;
        assert packageName != null;

        this.fragment = fragment;
        this.shopList = shopList;
        this.resources = resources;
        this.packageName = packageName;
    }

    @Override
    public int getCount() {
        if (shopList == null) {
            return 0;
        } else {
            return shopList.size();
        }
    }

    @Override
    public Shop getItem(int position) {
        if (shopList == null) {
            return null;
        } else {
            return shopList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        Shop shop = getItem(position);
        return shop.getIdShop();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = fragment.getLayoutInflater().inflate(R.layout.shop_row, null);
        }

        TextView shop_name = convertView.findViewById(R.id.shop_list_name);
        TextView shop_capacity = convertView.findViewById(R.id.shop_list_capacity);
        TextView shop_state = convertView.findViewById(R.id.shop_list_state);

        Shop shop_data = getItem(position);

        if (shop_data != null) {
            shop_name.setText(shop_data.getName());
            shop_capacity.setText(resources.getString(R.string.capacity) + ": " + shop_data.getActualCapacity() + "/" + shop_data.getMaxCapacity());

            if (shop_data.isAllowEntries()) {
                shop_state.setText(resources.getString(R.string.open));
                shop_state.setTextColor(resources.getColor(R.color.semaphore_green));
            } else {
                shop_state.setText(resources.getString(R.string.closed));
                shop_state.setTextColor(resources.getColor(R.color.semaphore_red));
            }

            ImageView image = convertView.findViewById(R.id.shop_list_image);
            getShopImage(shop_data, image);

        }

        return convertView;
    }

    private void getShopImage(Shop shopData, ImageView image) {
        String url = BackEndEndpoints.CONFIGURATION_IMAGE(shopData.getIdShop());
        RequestUtils.sendStringRequest(fragment.getContext(), Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("HTTP", "ok " + response.length());
                if (!response.isEmpty()) {
                    String base64 = response;
                    Bitmap map = ImageUtils.convert(base64);
                    image.setImageBitmap(map);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "ok");
                image.setVisibility(View.GONE);
            }
        });
    }
}
