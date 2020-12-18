package es.ewic.sellers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;

import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.FormUtils;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopCapacity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopCapacity extends Fragment {

    private static final String ARG_SHOP = "shop";

    private Shop shopData;
    OnShopCapacityListener mCallback;

    public interface OnShopCapacityListener {
        public void shopClosed();
    }

    public ShopCapacity() {
        // Required empty public constructor
    }

    public static ShopCapacity newInstance(Shop shop) {
        ShopCapacity fragment = new ShopCapacity();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SHOP, shop);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shopData = (Shop) getArguments().getSerializable(ARG_SHOP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_shop_capacity, container, false);

        TextView shop_name = parent.findViewById(R.id.shop_name);
        shop_name.setText(shopData.getName());

        Button close_shop_button = parent.findViewById(R.id.close_shop_button);
        close_shop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreCloseDialog(parent);
            }
        });

        openShop();

        return parent;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (OnShopCapacityListener) getActivity();
    }

    private void showPreCloseDialog(ConstraintLayout parent) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.warning).setMessage(R.string.pre_close_shop);

        builder.setPositiveButton(R.string.close, (dialog, which) -> closeShop());

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            // dialog cancelled;
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.semaphore_red));
            }
        });
        dialog.show();
    }

    private void openShop() {
        String url = BackEndEndpoints.SHOP_BASE + "/" + shopData.getIdShop() + BackEndEndpoints.SHOP_OPEN;

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);
        RequestUtils.sendStringRequest(getContext(), Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("HTTP", "open " + response);
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
                            openShop();
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
                            openShop();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }

    private void closeShop() {
        String url = BackEndEndpoints.SHOP_BASE + "/" + shopData.getIdShop() + BackEndEndpoints.SHOP_CLOSE;

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);
        RequestUtils.sendStringRequest(getContext(), Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("HTTP", "close " + response);
                pd.hide();
                mCallback.shopClosed();
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
                            closeShop();
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
                            closeShop();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }
}