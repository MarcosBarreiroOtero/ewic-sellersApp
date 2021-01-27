package es.ewic.sellers;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import es.ewic.sellers.model.Shop;

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
        void clickShopButton(Shop shop);
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

        TextView title = parent.findViewById(R.id.shop_information_title);
        title.setText(shop.getName());

        Button shop_button = parent.findViewById(R.id.shop_information_shop_button);
        shop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.clickShopButton(shop);
            }
        });

        return parent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (OnShopInformationListener) getActivity();
    }
}