package es.ewic.sellers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.lang.reflect.Method;

import es.ewic.sellers.model.Seller;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.FormUtils;
import es.ewic.sellers.utils.ModelConverter;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    OnLoginListener mCallback;

    public interface OnLoginListener {
        public void onLoadSellerData(Seller sellerData);
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_login, container, false);

        Button connect_local = parent.findViewById(R.id.connect_local);
        Button connect_remote = parent.findViewById(R.id.connect_remote);
        Button access_button = parent.findViewById(R.id.access_button);
        Button back_button = parent.findViewById(R.id.back_button);

        TextInputLayout login_endpoint_label = parent.findViewById(R.id.login_endpoint_label);
        TextInputLayout login_username_label = parent.findViewById(R.id.login_username_label);
        TextInputLayout login_password_label = parent.findViewById(R.id.login_password_label);

        //Back
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateBack(parent);
            }
        });

        //Remote server
        connect_remote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationShowServerType(parent, false);
            }
        });

        //Local server
        connect_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationShowServerType(parent, true);
            }

        });

        //Access
        access_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin(parent);
            }
        });
        return parent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (OnLoginListener) getActivity();
    }

    private void checkLogin(ConstraintLayout parent) {
        TextInputLayout login_username_label = parent.findViewById(R.id.login_username_label);
        TextInputLayout login_password_label = parent.findViewById(R.id.login_password_label);

        TextInputEditText login_endpoint_input = parent.findViewById(R.id.login_endpoint_input);
        TextInputEditText login_username_input = parent.findViewById(R.id.login_username_input);
        TextInputEditText login_password_input = parent.findViewById(R.id.login_password_input);

        String username = login_username_input.getText().toString().trim();
        boolean hasError = false;
        if (username == null || username.isEmpty()) {
            login_username_label.setError(getString(R.string.error_empty_field));
            hasError = true;
        } else {
            login_username_label.setError(null);
        }

        String password = login_password_input.getText().toString().trim();
        if (password == null || password.isEmpty()) {
            login_password_label.setError(getString(R.string.error_empty_field));
            hasError = true;
        } else {
            login_password_label.setError(null);
        }
        if (hasError) {
            return;
        }

        String url = BackEndEndpoints.SELLER_LOGIN + "?loginName=" + username + "&password=" + password;
        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);

        RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Seller newSeller = ModelConverter.jsonObjectToSeller(response);
                pd.dismiss();
                mCallback.onLoadSellerData(newSeller);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
                pd.dismiss();
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            pd.show();
                            checkLogin(parent);
                        }
                    });
                    snackbar.show();
                } else {
                    int responseCode = RequestUtils.getErrorCodeRequest(error);
                    //404 seller not found
                    //401 password not correct
                    String message = "";
                    switch (responseCode) {
                        case 404:
                            message = getString(R.string.error_username_login);
                            login_username_label.setError(getString(R.string.error_username_login));
                            login_password_label.setError(getString(R.string.error_username_login));
                            break;
                        case 401:
                            message = getString(R.string.error_username_login);
                            login_username_label.setError(getString(R.string.error_username_login));
                            login_password_label.setError(getString(R.string.error_username_login));
                            break;
                        default:
                            break;
                    }
                    if (message.isEmpty()) {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                                pd.show();
                                checkLogin(parent);
                            }
                        });
                        snackbar.show();
                    } else {
                        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    private void animationShowServerType(ConstraintLayout parent, boolean showEndpoint) {
        Button connect_local = parent.findViewById(R.id.connect_local);
        Button connect_remote = parent.findViewById(R.id.connect_remote);
        Button access_button = parent.findViewById(R.id.access_button);
        Button back_button = parent.findViewById(R.id.back_button);

        TextInputLayout login_endpoint_label = parent.findViewById(R.id.login_endpoint_label);
        TextInputLayout login_username_label = parent.findViewById(R.id.login_username_label);
        TextInputLayout login_password_label = parent.findViewById(R.id.login_password_label);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = new Slide(Gravity.START);
            transition.setDuration(600);
            transition.addTarget(connect_remote);
            transition.addTarget(connect_local);
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    Transition transitionShow = new Slide(Gravity.END);
                    transitionShow.setDuration(600);
                    transitionShow.addTarget(login_endpoint_label);
                    transitionShow.addTarget(login_password_label);
                    transitionShow.addTarget(login_username_label);
                    transitionShow.addTarget(access_button);
                    transitionShow.addTarget(back_button);
                    TransitionManager.beginDelayedTransition(parent, transitionShow);
                    if (showEndpoint) {
                        login_endpoint_label.setVisibility(View.VISIBLE);
                    }
                    login_username_label.setVisibility(View.VISIBLE);
                    login_password_label.setVisibility(View.VISIBLE);
                    back_button.setVisibility(View.VISIBLE);
                    access_button.setVisibility(View.VISIBLE);
                }

                @Override
                public void onTransitionStart(Transition transition) {
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                }

                @Override
                public void onTransitionPause(Transition transition) {
                }

                @Override
                public void onTransitionResume(Transition transition) {
                }
            });

            TransitionManager.beginDelayedTransition(parent, transition);
            connect_remote.setVisibility(View.GONE);
            connect_local.setVisibility(View.GONE);

        } else {
            connect_remote.setVisibility(View.GONE);
            connect_local.setVisibility(View.GONE);
            if (showEndpoint) {
                login_endpoint_label.setVisibility(View.VISIBLE);
            }
            login_username_label.setVisibility(View.VISIBLE);
            login_password_label.setVisibility(View.VISIBLE);
            back_button.setVisibility(View.VISIBLE);
            access_button.setVisibility(View.VISIBLE);
        }
    }

    private void animateBack(ConstraintLayout parent) {
        Button connect_local = parent.findViewById(R.id.connect_local);
        Button connect_remote = parent.findViewById(R.id.connect_remote);
        Button access_button = parent.findViewById(R.id.access_button);
        Button back_button = parent.findViewById(R.id.back_button);

        TextInputLayout login_endpoint_label = parent.findViewById(R.id.login_endpoint_label);
        TextInputLayout login_username_label = parent.findViewById(R.id.login_username_label);
        TextInputLayout login_password_label = parent.findViewById(R.id.login_password_label);

        TextInputEditText login_endpoint_input = parent.findViewById(R.id.login_endpoint_input);
        TextInputEditText login_username_input = parent.findViewById(R.id.login_username_input);
        TextInputEditText login_password_input = parent.findViewById(R.id.login_password_input);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = new Slide(Gravity.END);
            transition.setDuration(600);
            transition.addTarget(login_endpoint_label);
            transition.addTarget(login_password_label);
            transition.addTarget(login_username_label);
            transition.addTarget(access_button);
            transition.addTarget(back_button);

            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    login_endpoint_input.setText("");
                    login_endpoint_label.setError(null);
                    login_username_input.setText("");
                    login_username_label.setError(null);
                    login_password_input.setText("");
                    login_password_label.setError(null);

                    Transition transitionShow = new Slide(Gravity.START);
                    transitionShow.setDuration(600);
                    transitionShow.addTarget(connect_remote);
                    transitionShow.addTarget(connect_local);
                    TransitionManager.beginDelayedTransition(parent, transitionShow);
                    connect_local.setVisibility(View.VISIBLE);
                    connect_remote.setVisibility(View.VISIBLE);
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
            TransitionManager.beginDelayedTransition(parent, transition);
            login_endpoint_label.setVisibility(View.GONE);
            login_username_label.setVisibility(View.GONE);
            login_password_label.setVisibility(View.GONE);
            back_button.setVisibility(View.GONE);
            access_button.setVisibility(View.GONE);
        } else {
            connect_local.setVisibility(View.VISIBLE);
            connect_remote.setVisibility(View.VISIBLE);

            login_endpoint_label.setVisibility(View.GONE);
            login_username_label.setVisibility(View.GONE);
            login_password_label.setVisibility(View.GONE);
            access_button.setVisibility(View.GONE);
            back_button.setVisibility(View.GONE);

            login_endpoint_input.setText("");
            login_endpoint_label.setError(null);
            login_username_input.setText("");
            login_username_label.setError(null);
            login_password_input.setText("");
            login_password_label.setError(null);
        }
    }
}