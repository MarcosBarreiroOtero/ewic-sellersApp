package es.ewic.sellers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;
import org.w3c.dom.Text;

import es.ewic.sellers.model.Seller;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.FormUtils;
import es.ewic.sellers.utils.ModelConverter;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyDataFragment extends Fragment {

    private static final String ARG_SELLER_DATA = "sellerData";

    private Seller sellerData;

    OnMyDataListener mCallback;

    public interface OnMyDataListener {
        void onUpdateSellerAccount(Seller newSeller);

        void onDeleteSellerAccount();
    }

    public MyDataFragment() {
        // Required empty public constructor
    }

    public static MyDataFragment newInstance(Seller seller) {
        MyDataFragment fragment = new MyDataFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SELLER_DATA, seller);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (MyDataFragment.OnMyDataListener) getActivity();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sellerData = (Seller) getArguments().getSerializable(ARG_SELLER_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_my_data, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.toolbar_menu_my_data);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextInputEditText til_username = parent.findViewById(R.id.my_data_username_input);
        til_username.setText(sellerData.getLoginName());
        TextInputEditText til_firstName = parent.findViewById(R.id.my_data_firstName_input);
        til_firstName.setText(sellerData.getFirstName());
        TextInputEditText til_lastName = parent.findViewById(R.id.my_data_lastName_input);
        til_lastName.setText(sellerData.getLastName());
        TextInputEditText til_email = parent.findViewById(R.id.my_data_email_input);
        til_email.setText(sellerData.getEmail());


        Button update_button = parent.findViewById(R.id.button_update_data);
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                updateSellerData(parent);
            }
        });

        Button delete_button = parent.findViewById(R.id.button_delete_account);
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreDeleteDialog(parent);
            }
        });

        Button change_password_button = parent.findViewById(R.id.button_change_password);
        change_password_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog(parent);
            }
        });

        return parent;
    }

    private void updateSellerData(ConstraintLayout parent) {

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.updating_data, R.string.please_wait);

        TextInputLayout til_username = parent.findViewById((R.id.my_data_username_label));
        TextInputLayout til_firstName = parent.findViewById((R.id.my_data_firstName_label));
        TextInputLayout til_lastName = parent.findViewById((R.id.my_data_lastName_label));
        TextInputLayout til_email = parent.findViewById((R.id.my_data_email_label));

        til_username.setError(null);
        til_firstName.setError(null);
        til_lastName.setError(null);
        til_email.setError(null);

        TextInputEditText tiet_username = parent.findViewById(R.id.my_data_username_input);
        tiet_username.clearFocus();
        TextInputEditText tiet_firstName = parent.findViewById(R.id.my_data_firstName_input);
        tiet_firstName.clearFocus();
        TextInputEditText tiet_lastName = parent.findViewById(R.id.my_data_lastName_input);
        tiet_lastName.clearFocus();
        TextInputEditText tiet_email = parent.findViewById(R.id.my_data_email_input);
        tiet_email.clearFocus();

        String username = tiet_username.getText().toString().trim();
        String firstName = tiet_firstName.getText().toString().trim();
        String lastName = tiet_lastName.getText().toString().trim();
        String email = tiet_email.getText().toString().trim();

        boolean hasError = false;

        if (username.isEmpty()) {
            til_username.setError(getString(R.string.error_empty_field));
            hasError = true;
        }

        if (firstName.isEmpty()) {
            til_firstName.setError(getString(R.string.error_empty_field));
            hasError = true;
        }

        if (!FormUtils.isValidEmail(email)) {
            til_email.setError(getString(R.string.email_invalid_format));
            hasError = true;
        }

        if (hasError) {
            pd.dismiss();
            return;
        }

        JSONObject newSellerData = ModelConverter.sellerToJsonObject(new Seller(username, null, firstName, lastName, email));

        String url = BackEndEndpoints.SELLER_BASE + "/" + sellerData.getIdSeller();

        RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.PUT, url, newSellerData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                sellerData = ModelConverter.jsonObjectToSeller(response);
                mCallback.onUpdateSellerAccount(sellerData);
                pd.dismiss();
                Snackbar.make(parent, getString(R.string.update_data_successfully), Snackbar.LENGTH_LONG)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
                pd.dismiss();
                if (error instanceof TimeoutError) {
                    Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            pd.show();
                            updateSellerData(parent);
                        }
                    });
                    snackbar.show();
                } else {
                    int responseCode = RequestUtils.getErrorCodeRequest(error);
                    //404 seller not found: should not happen
                    //400 seller duplicate: username duplicated
                    if (responseCode == 400) {
                        til_username.setError(getString(R.string.error_username_duplicate));
                    } else {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                                pd.show();
                                updateSellerData(parent);
                            }
                        });
                        snackbar.show();
                    }
                }
            }
        });
    }

    private void showPreDeleteDialog(ConstraintLayout parent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.warning).setMessage(R.string.pre_delete_message);

        View password_dialog_view = LayoutInflater.from(getContext()).inflate(R.layout.password_dialog, (ViewGroup) getView(), false);
        final TextInputEditText pwd = password_dialog_view.findViewById(R.id.delete_dialog_password_input);
        final TextInputLayout pwd_label = password_dialog_view.findViewById(R.id.delete_dialog_password_label);
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
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.semaphore_red));
            }
        });
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = pwd.getText().toString().trim();
                if (password.isEmpty()) {
                    Log.e("DELETE", "Contraseña vacia");
                    pwd_label.setError(getString(R.string.error_empty_field));
                } else {
                    deleteSellerAccount(parent, pwd.getText().toString().trim());
                    dialog.dismiss();
                }
            }
        });
    }

    private void deleteSellerAccount(ConstraintLayout parent, String password) {

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);

        String url = BackEndEndpoints.SELLER_BASE + "/" + sellerData.getIdSeller() + "?pwd=" + password;

        RequestUtils.sendStringRequest(getContext(), Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pd.dismiss();
                Snackbar.make(parent, getString(R.string.account_delete_successfully), Snackbar.LENGTH_SHORT)
                        .show();
                mCallback.onDeleteSellerAccount();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                if (error instanceof TimeoutError) {
                    Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            pd.show();
                            deleteSellerAccount(parent, password);
                        }
                    });
                    snackbar.show();
                } else {
                    int responseCode = RequestUtils.getErrorCodeRequest(error);
                    //404 seller not found: should not happen
                    //401 incorrectpassword
                    if (responseCode == 401) {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_password), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                                pd.show();
                                deleteSellerAccount(parent, password);
                            }
                        });
                        snackbar.show();
                    }
                }
            }
        });
    }

    private void showChangePasswordDialog(ConstraintLayout parent) {
        View changePassword_view = LayoutInflater.from(getContext()).inflate(R.layout.change_password_dialog, (ViewGroup) getView(), false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.change_password);
        builder.setView(changePassword_view);

        builder.setPositiveButton(R.string.save, (dialog, which) -> //delete seller account
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
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.semaphore_red));
            }
        });
        dialog.show();

        TextInputLayout til_old_password = changePassword_view.findViewById(R.id.change_password_old_label);
        TextInputLayout til_new_password = changePassword_view.findViewById(R.id.change_password_new_label);
        TextInputLayout til_re_password = changePassword_view.findViewById(R.id.change_password_re_label);

        TextInputEditText tiet_old_password = changePassword_view.findViewById(R.id.change_password_old_input);
        TextInputEditText tiet_new_password = changePassword_view.findViewById(R.id.change_password_new_input);
        TextInputEditText tiet_re_password = changePassword_view.findViewById(R.id.change_password_re_input);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = tiet_old_password.getText().toString().trim();
                String newPassword = tiet_new_password.getText().toString().trim();
                String rePassword = tiet_re_password.getText().toString().trim();

                til_old_password.setError(null);
                til_new_password.setError(null);
                til_re_password.setError(null);
                boolean hasError = false;
                if (oldPassword.isEmpty()) {
                    til_old_password.setError(getString(R.string.error_empty_field));
                    hasError = true;
                }
                if (newPassword.isEmpty()) {
                    til_new_password.setError(getString(R.string.error_empty_field));
                    hasError = true;
                }
                if (rePassword.isEmpty()) {
                    til_re_password.setError(getString(R.string.error_empty_field));
                    hasError = true;
                }
                if (!newPassword.equals(rePassword)) {
                    til_new_password.setError(getString(R.string.error_password_not_equals));
                    til_re_password.setError(getString(R.string.error_password_not_equals));
                    hasError = true;
                }
                if (hasError) {
                    return;
                }

                String url = BackEndEndpoints.SELLER_CHANGE_PASSWORD + "/" + sellerData.getIdSeller() + "?newPwd=" + newPassword + "&oldPwd=" + oldPassword;

                RequestUtils.sendStringRequest(getContext(), Request.Method.PUT, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // OK
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.password_updated_successfully), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        dialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("HTTP", "error");
                        if (error instanceof TimeoutError) {
                            Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_LONG);
                            snackbar.show();
                        } else {
                            int responseCode = RequestUtils.getErrorCodeRequest(error);
                            //404 seller not found_ should not happen
                            //401 password incorrect
                            if (responseCode == 401) {
                                til_old_password.setError(getString(R.string.error_password));
                            } else {
                                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }
                        }
                    }
                });
            }
        });
    }
}