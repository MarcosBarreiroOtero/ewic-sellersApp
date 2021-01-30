package es.ewic.sellers;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.ModelConverter;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopInformationFragment extends Fragment {

    private static final String ARG_SHOP = "shop";

    private Shop shop;

    OnShopInformationListener mCallback;

    public interface OnShopInformationListener {
        void clickCapacityButton(Shop shop);

        void clickShopButton(Shop shop);

        void clickReservationManagement(Shop shop);

        void clickAddReservation(Shop shop);

        void clickConfiguration(Shop shop);
    }

    public ShopInformationFragment() {
        // Required empty public constructor
    }

    public static ShopInformationFragment newInstance(Shop shopData) {
        ShopInformationFragment fragment = new ShopInformationFragment();
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

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(shop.getName());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_shop_information, container, false);

        loadData(parent);

        Button capacity_button = parent.findViewById(R.id.shop_information_capacity_button);
        capacity_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.clickCapacityButton(shop);
            }
        });

        Button shop_button = parent.findViewById(R.id.shop_information_shop_button);
        shop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.clickShopButton(shop);
            }
        });

        Button reservation_management_button = parent.findViewById(R.id.shop_information_reservation_mangement);
        reservation_management_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.clickReservationManagement(shop);
            }
        });

        Button add_reservation_button = parent.findViewById(R.id.shop_information_add_reservation);
        add_reservation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.clickAddReservation(shop);
            }
        });

        Button configuration_button = parent.findViewById(R.id.shop_information_configutation_button);
        configuration_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.clickConfiguration(shop);
            }
        });


        getShopUpdated(parent);

        return parent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (OnShopInformationListener) getActivity();
    }

    void loadData(ConstraintLayout parent) {
        TextView title = parent.findViewById(R.id.shop_information_title);
        title.setText(shop.getName());

        Button capacity_button = parent.findViewById(R.id.shop_information_capacity_button);
        capacity_button.setText(getString(R.string.capacity_control) + "\n" + shop.getActualCapacity() + " / " + shop.getMaxCapacity());
    }

    void getShopUpdated(ConstraintLayout parent) {
        String url = BackEndEndpoints.SHOP_BASE + "/" + shop.getIdShop();

        RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                shop = ModelConverter.jsonObjectToShop(response);
                loadData(parent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
                // no update
            }
        });
    }
}