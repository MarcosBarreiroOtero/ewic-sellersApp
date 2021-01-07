package es.ewic.sellers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.text.Html;
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
import com.ramijemli.percentagechartview.PercentageChartView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.UUID;

import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.FormUtils;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopCapacityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopCapacityFragment extends Fragment {

    private static final String ARG_SHOP = "shop";
    private static final int BLUETOOTH_REQUEST_CODE = 01;
    private static final int BLUETOOTH_REQUEST_DISCOVERABLE = 02;

    private Shop shopData;
    OnShopCapacityListener mCallback;
    private TextView shop_bluetooth_name;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mBluetoothServerSocket;
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public interface OnShopCapacityListener {
        public void shopClosed();
    }

    public ShopCapacityFragment() {
        // Required empty public constructor
    }

    public static ShopCapacityFragment newInstance(Shop shop) {
        ShopCapacityFragment fragment = new ShopCapacityFragment();
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

        PercentageChartView percentageChartView = parent.findViewById(R.id.shop_percentage);
        float percentage = ((float) shopData.getActualCapacity() / shopData.getMaxCapacity()) * 100;
        FormUtils.configureSemaphorePercentageChartView(getResources(), percentageChartView, percentage);

        TextView shop_capacity = parent.findViewById(R.id.shop_capacity);
        shop_capacity.setText(shopData.getActualCapacity() + "/" + shopData.getMaxCapacity());
        if (percentage < 75) {
            shop_capacity.setTextColor(getResources().getColor(R.color.semaphore_green));
        } else if (percentage < 100) {
            shop_capacity.setTextColor(getResources().getColor(R.color.semaphore_ambar));
        } else {
            shop_capacity.setTextColor(getResources().getColor(R.color.semaphore_red));
        }

        if (!shopData.isAllowEntries()) {
            openShop();
        }

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        shop_bluetooth_name = parent.findViewById(R.id.shop_bluetooth_name);
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                shop_bluetooth_name.setText(Html.fromHtml(getString(R.string.shop_connect_message) + " <strong>" + getLocalBluetoothName() + "</strong>."));
                requestBluetoothDiscoverable();
            } else {
                requestActivateBluetooth();
            }
        }
        return parent;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (OnShopCapacityListener) getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                shop_bluetooth_name.setText(Html.fromHtml(getString(R.string.shop_connect_message) + " <strong>" + getLocalBluetoothName() + "</strong>."));
                requestBluetoothDiscoverable();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.bluetooth_needed_message), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.activate, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        requestActivateBluetooth();
                    }
                });
                snackbar.show();
            }
        } else if (requestCode == BLUETOOTH_REQUEST_DISCOVERABLE) {
            Log.e("BLUETOOTH", "Activado visisbilidad" + resultCode);
            if (resultCode == Activity.RESULT_CANCELED) {
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.bluetooth_discoverable_message), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.activate, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        requestActivateBluetooth();
                    }
                });
                snackbar.show();
            } else {
                startBluetoothSever();
            }
        }
    }

    private void requestActivateBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE);
    }

    private void requestBluetoothDiscoverable() {
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivityForResult(discoverableIntent, BLUETOOTH_REQUEST_DISCOVERABLE);
    }

    private String getLocalBluetoothName() {

        String name = mBluetoothAdapter.getName();
        if (name == null) {
            System.out.println("Name is null!");
            name = mBluetoothAdapter.getAddress();
            Log.e("BLUETOOTH", "Adress: " + name);
        }
        return name;
    }

    private void startBluetoothSever() {
        BluetoothServerSocket tmp = null;
        Log.e("BLUETOOTH", "comenzando servidor");
        try {
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MYYAPP", MY_UUID_SECURE);
        } catch (IOException e) {
            Log.e("BLUETOOTH", "Socket's listen() method failed", e);
        }
        mBluetoothServerSocket = tmp;
        run();
    }

    public void run() {
        BluetoothSocket socket = null;
        Log.e("BLUETOOTH", "Comienzo servidor");
        while (true) {
            Log.e("BLUETOOTH", "Escuchando");
            try {
                socket = mBluetoothServerSocket.accept();
            } catch (IOException e) {
                Log.e("BLUETOOTH", "Socket's accept() method failed", e);
                break;
            }
            if (socket != null) {
                Log.e("BLUETOOTH", "Nueva conexión");
                // do entry
                // mBluetoothServerSocket.close();
                break;
            }
        }
    }

    public void cancel() {
        try {
            mBluetoothServerSocket.close();
        } catch (IOException e) {
            Log.e("BLUETOOTH", "Could not close the connect socket", e);
        }
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
                pd.dismiss();
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
                pd.dismiss();
                mCallback.shopClosed();
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