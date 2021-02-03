package es.ewic.sellers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.FormUtils;
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

        void clickEntryLog(Shop shop);

        void clickReservationLog(Shop shop);

        void clickAddReservation(Shop shop);

        void clickConfiguration(Shop shop);


        void confirmDeleteShop();
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

        Button configuration_button = parent.findViewById(R.id.shop_information_configuration_button);
        configuration_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.clickConfiguration(shop);
            }
        });

        Button entry_log_button = parent.findViewById(R.id.shop_information_entry_log_button);
        entry_log_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.clickEntryLog(shop);
            }
        });

        Button reservation_log_button = parent.findViewById(R.id.shop_information_reservation_log_button);
        reservation_log_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.clickReservationLog(shop);
            }
        });

        Button delete_button = parent.findViewById(R.id.shop_information_delete_button);
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteShopDialog(parent);
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

    private void loadData(ConstraintLayout parent) {
        Button capacity_button = parent.findViewById(R.id.shop_information_capacity_button);
        capacity_button.setText(getString(R.string.capacity_control) + "\n" + shop.getActualCapacity() + " / " + shop.getMaxCapacity());

        Drawable icon = getResources().getDrawable(shop.isAllowEntries() ? R.drawable.ic_open_24 : R.drawable.ic_close_24);
        Drawable wrappedDrawable = DrawableCompat.wrap(icon);
        DrawableCompat.setTint(wrappedDrawable, getResources().getColor(shop.isAllowEntries() ? R.color.semaphore_green : R.color.semaphore_red));
        capacity_button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
    }

    private void getShopUpdated(ConstraintLayout parent) {
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

    private void showDeleteShopDialog(ConstraintLayout parent) {

        View password_dialog_view = LayoutInflater.from(getContext()).inflate(R.layout.password_dialog, (ViewGroup) getView(), false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.warning).setMessage(R.string.pre_delete_shop_message);
        TextView dialog_text = password_dialog_view.findViewById(R.id.delete_dialog_password_text);
        dialog_text.setText(getString(R.string.delete_password_needed_shop));
        builder.setView(password_dialog_view);

        builder.setPositiveButton(R.string.delete, (dialog, which) -> //delete seller account
        {
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) ->
        {
            // dialog cancelled;
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.semaphore_red));
            }
        });
        dialog.show();

        TextInputEditText pwd = password_dialog_view.findViewById(R.id.delete_dialog_password_input);
        TextInputLayout pwd_label = password_dialog_view.findViewById(R.id.delete_dialog_password_label);

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = pwd.getText().toString().trim();
                if (password.isEmpty()) {
                    pwd_label.setError(getString(R.string.error_empty_field));
                } else {
                    deleteShop(parent, pwd.getText().toString().trim());
                    dialog.dismiss();
                }
            }
        });
    }

    private void deleteShop(ConstraintLayout parent, String password) {

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);

        String url = BackEndEndpoints.SHOP_BASE + "/" + shop.getIdShop() + "?pwd=" + password;

        RequestUtils.sendStringRequest(getContext(), Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pd.dismiss();
                Snackbar.make(parent, getString(R.string.shop_delete_successfully), Snackbar.LENGTH_SHORT)
                        .show();
                mCallback.confirmDeleteShop();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                if (error instanceof TimeoutError) {
                    Snackbar snackbar = Snackbar.make(getView(), getString(R.string.delete_shop_error), Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    int responseCode = RequestUtils.getErrorCodeRequest(error);
                    //404 shop not found: should not happen
                    //401 incorrectpassword
                    if (responseCode == 401) {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_password), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.delete_shop_error), Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
            }
        });

    }
}