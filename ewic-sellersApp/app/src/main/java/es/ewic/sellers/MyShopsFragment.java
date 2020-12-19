package es.ewic.sellers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.util.List;

import es.ewic.sellers.adapters.ShopRowAdapter;
import es.ewic.sellers.model.Seller;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.FormUtils;
import es.ewic.sellers.utils.ModelConverter;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyShopsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyShopsFragment extends Fragment {

    private static final String ARG_SELLER = "sellerData";

    private Seller sellerData;
    private List<Shop> shops;

    OnMyShopsListener mCallback;

    public interface OnMyShopsListener {
        public void onShopClick(Shop shop);
    }

    public MyShopsFragment() {
        // Required empty public constructor
    }

    public static MyShopsFragment newInstance(Seller seller) {
        MyShopsFragment fragment = new MyShopsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SELLER, seller);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sellerData = (Seller) getArguments().getSerializable(ARG_SELLER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_my_shops, container, false);

        TextView seller_welcome = parent.findViewById(R.id.seller_welcome);

        seller_welcome.setText(getString(R.string.welcome) + " " + sellerData.getLoginName() + ". " + getString(R.string.startCapacityShop));

        getShops(parent);
        ListView shopList = parent.findViewById(R.id.shop_list);
        shopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Shop selectedShop = shops.get(position);
                Log.e("SHOP", selectedShop.toString());
                mCallback.onShopClick(selectedShop);
            }
        });


        return parent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (OnMyShopsListener) getActivity();
    }

    private void getShops(ConstraintLayout parent) {

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);

        String url = BackEndEndpoints.SELLER_SHOPS + "/" + sellerData.getIdSeller();

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                shops = ModelConverter.jsonArrayToShopList(response);
                ListView shopList = parent.findViewById(R.id.shop_list);
                ShopRowAdapter shopRowAdapter = new ShopRowAdapter(MyShopsFragment.this, shops, getResources(), getActivity().getPackageName());
                shopList.setAdapter(shopRowAdapter);
                pd.hide();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.hide();
                if (error instanceof TimeoutError) {
                    Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            pd.show();
                            getShops(parent);
                        }
                    });
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            pd.show();
                            getShops(parent);
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }
}