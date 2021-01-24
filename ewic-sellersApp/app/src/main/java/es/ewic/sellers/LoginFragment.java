package es.ewic.sellers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
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

    TextInputLayout login_username_label;
    TextInputLayout login_password_label;

    TextInputEditText login_username_input;
    TextInputEditText login_password_input;

    TextInputLayout register_username_label;
    TextInputLayout register_password_label;
    TextInputLayout register_repassword_label;
    TextInputLayout register_firstName_label;
    TextInputLayout register_lastName_label;
    TextInputLayout register_mail_label;

    TextInputEditText register_username_input;
    TextInputEditText register_password_input;
    TextInputEditText register_repassword_input;
    TextInputEditText register_firstName_input;
    TextInputEditText register_lastName_input;
    TextInputEditText register_mail_input;

    Button pre_register_button;
    Button pre_access_button;
    Button access_button;
    Button register_button;
    Button back_button;

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

        pre_register_button = parent.findViewById(R.id.pre_register_button);
        pre_access_button = parent.findViewById(R.id.pre_access_button);
        access_button = parent.findViewById(R.id.access_button);
        register_button = parent.findViewById(R.id.register_button);
        back_button = parent.findViewById(R.id.back_button);

        login_username_label = parent.findViewById(R.id.login_username_label);
        login_password_label = parent.findViewById(R.id.login_password_label);

        login_username_input = parent.findViewById(R.id.login_username_input);
        login_password_input = parent.findViewById(R.id.login_password_input);

        register_username_label = parent.findViewById(R.id.register_username_label);
        register_password_label = parent.findViewById(R.id.register_password_label);
        register_repassword_label = parent.findViewById(R.id.register_repassword_label);
        register_firstName_label = parent.findViewById(R.id.register_firstName_label);
        register_lastName_label = parent.findViewById(R.id.register_lastName_label);
        register_mail_label = parent.findViewById(R.id.register_mail_label);

        register_username_input = parent.findViewById(R.id.register_username_input);
        register_password_input = parent.findViewById(R.id.register_password_input);
        register_repassword_input = parent.findViewById(R.id.register_repassword_input);
        register_firstName_input = parent.findViewById(R.id.register_firstName_input);
        register_lastName_input = parent.findViewById(R.id.register_lastName_input);
        register_mail_input = parent.findViewById(R.id.register_mail_input);

        //Back
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateBack(parent);
            }
        });

        //Pre access
        pre_access_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationShowServerType(parent, false);
            }
        });

        //Pre register
        pre_register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationShowServerType(parent, true);
            }

        });

        //Access
        access_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });

        //Register
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRegister();
            }
        });
        return parent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mCallback = (OnLoginListener) getActivity();
    }

    private void checkLogin() {
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
                            checkLogin();
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
                                checkLogin();
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

    private void checkRegister() {

        String username = register_username_input.getText().toString().trim();
        String password = register_password_input.getText().toString().trim();
        String repassword = register_repassword_input.getText().toString().trim();
        String firstName = register_firstName_input.getText().toString().trim();
        String lastName = register_lastName_input.getText().toString().trim();
        String mail = register_mail_input.getText().toString().trim();

        register_username_label.setError(null);
        register_password_label.setError(null);
        register_repassword_label.setError(null);
        register_firstName_label.setError(null);
        register_lastName_label.setError(null);
        register_mail_label.setError(null);

        boolean hashError = false;

        //Username
        if (username == null || username.isEmpty()) {
            register_username_label.setError(getString(R.string.error_empty_field));
            hashError = true;
        }

        //Password
        if (password == null || password.isEmpty()) {
            register_password_label.setError(getString(R.string.error_empty_field));
            hashError = true;
        }

        if (repassword == null || repassword.isEmpty()) {
            register_repassword_input.setError(getString(R.string.error_empty_field));
            hashError = true;
        }

        if (!password.equals(repassword)) {
            register_password_label.setError(getString(R.string.error_password_not_equals));
            register_repassword_input.setError(getString(R.string.error_password_not_equals));
            hashError = true;
        }

        //FirstName
        if (firstName == null || firstName.isEmpty()) {
            register_firstName_label.setError(getString(R.string.error_empty_field));
            hashError = true;
        }
        //LastName

        //Mail
        if (mail == null || mail.isEmpty()) {
            register_mail_label.setError(getString(R.string.error_empty_field));
            hashError = true;
        } else if (!FormUtils.isValidEmail(mail)) {
            register_mail_label.setError(getString(R.string.email_invalid_format));
            hashError = true;
        }

        if (hashError) {
            return;
        }

        Seller seller = new Seller(username, password, firstName, lastName, mail);

        String url = BackEndEndpoints.SELLER_BASE;
        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);

        RequestUtils.sendJsonObjectRequest(getContext(), Request.Method.POST, url, ModelConverter.sellerToJsonObject(seller), new Response.Listener<JSONObject>() {
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
                            checkRegister();
                        }
                    });
                    snackbar.show();
                } else {
                    int responseCode = RequestUtils.getErrorCodeRequest(error);
                    //400 username duplicate
                    if (responseCode == 400) {
                        register_username_label.setError(getString(R.string.error_username_duplicate));
                    } else {
                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                                pd.show();
                                checkRegister();
                            }
                        });
                        snackbar.show();
                    }
                }
            }
        });
    }

    private void animationShowServerType(ConstraintLayout parent, boolean register) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(parent);
        if (register) {
            constraintSet.connect(R.id.back_button, ConstraintSet.END, R.id.register_button, ConstraintSet.START, 24);
        } else {
            constraintSet.connect(R.id.back_button, ConstraintSet.END, R.id.access_button, ConstraintSet.START, 24);
        }
        constraintSet.applyTo(parent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = new Slide(Gravity.START);
            transition.setDuration(600);
            transition.addTarget(pre_access_button);
            transition.addTarget(pre_register_button);
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    Transition transitionShow = new Slide(Gravity.END);
                    transitionShow.setDuration(600);
                    if (register) {
                        transitionShow.addTarget(register_username_label);
                        transitionShow.addTarget(register_password_label);
                        transitionShow.addTarget(register_repassword_label);
                        transitionShow.addTarget(register_firstName_label);
                        transitionShow.addTarget(register_lastName_label);
                        transitionShow.addTarget(register_mail_label);
                        transitionShow.addTarget(register_button);
                    } else {
                        transitionShow.addTarget(login_password_label);
                        transitionShow.addTarget(login_username_label);
                        transitionShow.addTarget(access_button);
                    }

                    transitionShow.addTarget(back_button);
                    TransitionManager.beginDelayedTransition(parent, transitionShow);
                    if (register) {
                        register_username_label.setVisibility(View.VISIBLE);
                        register_password_label.setVisibility(View.VISIBLE);
                        register_repassword_label.setVisibility(View.VISIBLE);
                        register_firstName_label.setVisibility(View.VISIBLE);
                        register_lastName_label.setVisibility(View.VISIBLE);
                        register_mail_label.setVisibility(View.VISIBLE);
                        register_button.setVisibility(View.VISIBLE);
                    } else {
                        login_username_label.setVisibility(View.VISIBLE);
                        login_password_label.setVisibility(View.VISIBLE);
                        access_button.setVisibility(View.VISIBLE);
                    }
                    back_button.setVisibility(View.VISIBLE);
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
            pre_access_button.setVisibility(View.GONE);
            pre_register_button.setVisibility(View.GONE);

        } else {
            pre_access_button.setVisibility(View.GONE);
            pre_register_button.setVisibility(View.GONE);
            if (register) {
                register_username_label.setVisibility(View.VISIBLE);
                register_password_label.setVisibility(View.VISIBLE);
                register_repassword_label.setVisibility(View.VISIBLE);
                register_firstName_label.setVisibility(View.VISIBLE);
                register_lastName_label.setVisibility(View.VISIBLE);
                register_mail_label.setVisibility(View.VISIBLE);
                register_button.setVisibility(View.VISIBLE);
            } else {
                login_username_label.setVisibility(View.VISIBLE);
                login_password_label.setVisibility(View.VISIBLE);
                access_button.setVisibility(View.VISIBLE);
            }
            back_button.setVisibility(View.VISIBLE);
        }
    }

    private void animateBack(ConstraintLayout parent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = new Slide(Gravity.END);
            transition.setDuration(600);
            transition.addTarget(login_password_label);
            transition.addTarget(login_username_label);
            transition.addTarget(register_username_label);
            transition.addTarget(register_password_label);
            transition.addTarget(register_repassword_label);
            transition.addTarget(register_firstName_label);
            transition.addTarget(register_lastName_label);
            transition.addTarget(register_mail_label);
            transition.addTarget(access_button);
            transition.addTarget(back_button);
            transition.addTarget(register_button);

            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    login_username_input.setText("");
                    login_username_label.setError(null);
                    login_password_input.setText("");
                    login_password_label.setError(null);
                    register_username_input.setText("");
                    register_username_label.setError(null);
                    register_password_input.setText("");
                    register_password_label.setError(null);
                    register_repassword_input.setText("");
                    register_repassword_label.setError(null);
                    register_firstName_input.setText("");
                    register_firstName_label.setError(null);
                    register_lastName_input.setText("");
                    register_lastName_label.setError(null);
                    register_mail_input.setText("");
                    register_mail_label.setError(null);

                    Transition transitionShow = new Slide(Gravity.START);
                    transitionShow.setDuration(600);
                    transitionShow.addTarget(pre_access_button);
                    transitionShow.addTarget(pre_register_button);
                    TransitionManager.beginDelayedTransition(parent, transitionShow);
                    pre_access_button.setVisibility(View.VISIBLE);
                    pre_register_button.setVisibility(View.VISIBLE);
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
            login_username_label.setVisibility(View.GONE);
            login_password_label.setVisibility(View.GONE);
            register_username_label.setVisibility(View.GONE);
            register_password_label.setVisibility(View.GONE);
            register_repassword_label.setVisibility(View.GONE);
            register_firstName_label.setVisibility(View.GONE);
            register_lastName_label.setVisibility(View.GONE);
            register_mail_label.setVisibility(View.GONE);
            back_button.setVisibility(View.GONE);
            access_button.setVisibility(View.GONE);
            register_button.setVisibility(View.GONE);
        } else {
            pre_access_button.setVisibility(View.VISIBLE);
            pre_register_button.setVisibility(View.VISIBLE);

            login_username_label.setVisibility(View.GONE);
            login_password_label.setVisibility(View.GONE);
            access_button.setVisibility(View.GONE);

            register_username_label.setVisibility(View.GONE);
            register_password_label.setVisibility(View.GONE);
            register_repassword_label.setVisibility(View.GONE);
            register_firstName_label.setVisibility(View.GONE);
            register_lastName_label.setVisibility(View.GONE);
            register_mail_label.setVisibility(View.GONE);
            register_button.setVisibility(View.GONE);

            back_button.setVisibility(View.GONE);

            login_username_input.setText("");
            login_username_label.setError(null);
            login_password_input.setText("");
            login_password_label.setError(null);

            login_username_input.setText("");
            login_username_label.setError(null);
            login_password_input.setText("");
            login_password_label.setError(null);
            register_username_input.setText("");
            register_username_label.setError(null);
            register_password_input.setText("");
            register_password_label.setError(null);
            register_repassword_input.setText("");
            register_repassword_label.setError(null);
            register_firstName_input.setText("");
            register_firstName_label.setError(null);
            register_lastName_input.setText("");
            register_lastName_label.setError(null);
            register_mail_input.setText("");
            register_mail_label.setError(null);
        }
    }
}