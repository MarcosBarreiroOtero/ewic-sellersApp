package es.ewic.sellers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ramijemli.percentagechartview.PercentageChartView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import es.ewic.sellers.model.ManualEntry;
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
    private PercentageChartView percentageChartView;
    private TextView shop_capacity;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mBluetoothServerSocket;
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BroadcastReceiver mBroadcastReceiver;

    private boolean bluetoothActivate = false;

    private List<ManualEntry> manualEntries = new ArrayList<>();
    private Button manual_entry_button;

    private AcceptThread thread;

    public interface OnShopCapacityListener {
        public void shopClosed();

        public void bluetoothDisconnected();
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

        Button close_shop_button = parent.findViewById(R.id.close_shop_button);
        close_shop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreCloseDialog(parent);
            }
        });

        TextView shop_name_text = parent.findViewById(R.id.shop_bluetooth_name);
        shop_name_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothActivate) {
                    requestActivateBluetooth();
                }
            }
        });

        percentageChartView = parent.findViewById(R.id.shop_percentage);
        float percentage = ((float) shopData.getActualCapacity() / shopData.getMaxCapacity()) * 100;
        FormUtils.configureSemaphorePercentageChartView(getResources(), percentageChartView, percentage);

        shop_capacity = parent.findViewById(R.id.shop_capacity);
        updateShopCapacityText();

        if (!shopData.isAllowEntries()) {
            openShop();
        }

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        shop_bluetooth_name = parent.findViewById(R.id.shop_bluetooth_name);
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                shop_bluetooth_name.setText(Html.fromHtml(getString(R.string.shop_connect_message) + "<br> <strong>" + getLocalBluetoothName() + "</strong>"));
                requestBluetoothDiscoverable();
            } else {
                requestActivateBluetooth();
            }
        }

        //Manual entries

        manual_entry_button = parent.findViewById(R.id.manual_entry_button);
        manual_entry_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showManuelEntryDialog();
            }
        });

        Button manual_exit = parent.findViewById(R.id.manual_exit_entry);
        manual_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showManualExitDialog();
            }
        });

        getManualEntries();

        return parent;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBroadcastReceiver != null) {
            requireActivity().unregisterReceiver(mBroadcastReceiver);
        }
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
                shop_bluetooth_name.setText(Html.fromHtml(getString(R.string.shop_connect_message) + "<br> <strong>" + getLocalBluetoothName() + "</strong>"));
                requestBluetoothDiscoverable();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.bluetooth_needed_message), Snackbar.LENGTH_LONG);
                snackbar.show();
                shop_bluetooth_name.setText(getString(R.string.automatic_entry_not_activate_1) + "\n" + getString(R.string.automatic_entry_not_activate_2));
                bluetoothActivate = false;
            }
        } else if (requestCode == BLUETOOTH_REQUEST_DISCOVERABLE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.bluetooth_discoverable_message), Snackbar.LENGTH_LONG);
                snackbar.show();
                shop_bluetooth_name.setText(getString(R.string.automatic_entry_not_activate_1) + "\n" + getString(R.string.automatic_entry_not_activate_2));
                bluetoothActivate = false;
            } else {
                startBluetoothSever();
                bluetoothActivate = true;
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
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
        startActivityForResult(discoverableIntent, BLUETOOTH_REQUEST_DISCOVERABLE);
    }

    private String getLocalBluetoothName() {

        String name = mBluetoothAdapter.getName();
        if (name == null) {
            name = mBluetoothAdapter.getAddress();
        }
        return name;
    }

    private void startBluetoothSever() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                thread = new AcceptThread();
                thread.run();
            }
        }).start();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.e("BLUETOOTH", "Disconnect : " + device.getName());
                    thread.registerDisconnect(device);
                } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                    Log.e("BLUETOOTH", "Request pairing");
                    //TODO revisar obtener permisos bluetooth privileges
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    int pin = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, 0);
//                    Log.d("BLUETOOTH", "PIN" + pin);
//                    byte[] pinBytes;
//                    pinBytes = ("" + pin).getBytes();
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        device.setPin(pinBytes);
//                        device.setPairingConfirmation(true);
//                    }

                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
    }

    private void updateShopCapacityText() {
        float percentage = ((float) shopData.getActualCapacity() / shopData.getMaxCapacity()) * 100;
        shop_capacity.setText(shopData.getActualCapacity() + "/" + shopData.getMaxCapacity());
        if (percentage < 75) {
            shop_capacity.setTextColor(getResources().getColor(R.color.semaphore_green));
        } else if (percentage < 100) {
            shop_capacity.setTextColor(getResources().getColor(R.color.semaphore_ambar));
        } else {
            shop_capacity.setTextColor(getResources().getColor(R.color.semaphore_red));
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
        String url = BackEndEndpoints.SHOP_OPEN(shopData.getIdShop());
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

    private void getManualEntries() {

        String url = BackEndEndpoints.MANUAL_ENTRIES(shopData.getIdShop(), Calendar.getInstance());

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject manualEntryJSON = response.getJSONObject(i);
                        ManualEntry manualEntry = new ManualEntry(manualEntryJSON.getInt("entryNumber"), manualEntryJSON.getString("description"));
                        manualEntries.add(manualEntry);
                    } catch (JSONException e) {
                        // omit entry
                    }
                }
                manual_entry_button.setText(getString(R.string.manual_entry) + " (" + manualEntries.size() + ")");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error manual entries");
            }
        });
    }

    private void closeShop() {
        String url = BackEndEndpoints.SHOP_CLOSE(shopData.getIdShop());
        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.connecting_server, R.string.please_wait);
        RequestUtils.sendStringRequest(getContext(), Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pd.dismiss();
                if (thread != null) {
                    thread.cancel();
                }
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

    private void showManuelEntryDialog() {

        View manualEntry_View = LayoutInflater.from(getContext()).inflate(R.layout.manual_entry_dialog, (ViewGroup) getView(), false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.register_manual_entry);
        builder.setView(manualEntry_View);
        builder.setPositiveButton(R.string.accept, ((dialog, which) -> {
        }));

        builder.setNegativeButton(R.string.cancel, (dialog, which) ->
        {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.semaphore_red));
            }
        });
        dialog.show();

        TextInputLayout til_description = manualEntry_View.findViewById(R.id.manual_entry_description_label);
        TextInputEditText tiet_description = manualEntry_View.findViewById(R.id.manual_entry_description_input);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = tiet_description.getText().toString().trim();
                til_description.setError(null);
                if (description.isEmpty()) {
                    til_description.setError(getString(R.string.error_empty_field));
                    return;
                }

                registerManualEntryClient(description);
                dialog.dismiss();

            }
        });
    }

    private void registerManualEntryClient(String description) {

        String url = BackEndEndpoints.MANUAL_ENTRY(shopData.getIdShop(), description);

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.registering_entry, R.string.please_wait);

        RequestUtils.sendStringRequest(getContext(), Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.contains("@#")) {
                    Snackbar.make(getView(), getString(R.string.max_capacity_exceeded_message), Snackbar.LENGTH_LONG).show();
                } else {
                    String entryNumber = response.split("@#")[0];
                    Integer actualCapacity = Integer.parseInt(response.split("@#")[1]);

                    Log.e("BLUETOOTH", "Entrada registrada:" + response);
                    Snackbar.make(getView(), getString(R.string.new_entry), Snackbar.LENGTH_LONG).show();

                    shopData.setActualCapacity(actualCapacity);
                    float percentage = ((float) shopData.getActualCapacity() / shopData.getMaxCapacity()) * 100;
                    percentageChartView.setProgress(percentage, true);
                    updateShopCapacityText();

                    manualEntries.add(new ManualEntry(Integer.parseInt(entryNumber), description));
                    manual_entry_button.setText(getString(R.string.manual_entry) + " (" + manualEntries.size() + ")");
                }

                pd.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                if (error instanceof TimeoutError) {

                } else {
                }
            }

        });


    }

    private void showManualExitDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        List<String> descriptions = new ArrayList<>();
        for (ManualEntry manualEntry : manualEntries) {
            descriptions.add(manualEntry.getDescription());
        }
        builder.setTitle(R.string.choose_entry_exit).setItems(descriptions.toArray(new CharSequence[descriptions.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ManualEntry manualEntry = manualEntries.get(which);
                registerExitClient(manualEntry.getEntryNumber(), true);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void registerExitClient(Integer idEntry, boolean manual) {

        ProgressDialog pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.registering_exit, R.string.please_wait);

        String url = BackEndEndpoints.EXIT_CLIENT(shopData.getIdShop(), idEntry);

        RequestUtils.sendStringRequest(getContext(), Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                int actualCapacity = Integer.parseInt(response);
                shopData.setActualCapacity(actualCapacity);
                float percentage = ((float) shopData.getActualCapacity() / shopData.getMaxCapacity()) * 100;
                percentageChartView.setProgress(percentage, true);
                updateShopCapacityText();

                if (manual) {
                    for (int i = 0; i < manualEntries.size(); i++) {
                        ManualEntry manualEntry = manualEntries.get(i);
                        if (manualEntry.getEntryNumber() == idEntry) {
                            manualEntries.remove(manualEntry);
                            break;
                        }
                    }
                    manual_entry_button.setText(getString(R.string.manual_entry) + " (" + manualEntries.size() + ")");
                }

                pd.dismiss();
                Snackbar.make(getView(), getString(R.string.exit_register_successfully), Snackbar.LENGTH_LONG).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("BLUETOOTH", "Http error");
                pd.dismiss();

                if (error instanceof TimeoutError) {
                    // do nothing
                } else {
                    int responseCode = RequestUtils.getErrorCodeRequest(error);
                    // 404 not found entry, do nothing ???
                    // 401 shop not open, do nothing ???
                    // 401 client already exit, do nothing ???
                    // unknow code close socket
                }
            }
        });
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;
        private ProgressDialog mRegisteringEntryDialog;
        private HashMap<String, Integer> socketConnections;
        private List<BluetoothSocket> socketsOpened;
        private boolean closeShop = false;


        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MYYAPP", MY_UUID_SECURE);
            } catch (IOException e) {
                Log.e("BLUETOOTH", "Socket's listen() method failed", e);
            }
            mServerSocket = tmp;
            socketsOpened = new ArrayList<>();
            socketConnections = new HashMap<>();
        }

        private String readIdGoogleLoginFromClient(BluetoothSocket socket) {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] mBuffer = new byte[1024];
                int numBytes;

                while (true) {
                    numBytes = inputStream.read(mBuffer);
                    String bufferContent = new String(mBuffer).trim();

                    if (!bufferContent.isEmpty()) {
                        return bufferContent;
                    }
                }
            } catch (IOException e) {
                Log.e("BLUETOOTH", "Can't read input stream for idGoogleLogin", e);
                return null;
            }

        }

        private void writeShopNameAndEntryNumber(BluetoothSocket socket, String response) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                JSONObject shopJson = new JSONObject();
                try {
                    shopJson.putOpt("name", shopData.getName());
                    if (response != null) {
                        shopJson.putOpt("nEntry", response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                outputStream.write(shopJson.toString().getBytes());
            } catch (IOException e) {
                Log.e("BLUETOOTH", "Error output stream", e);
                e.printStackTrace();
            }
        }

        private void storeSocketInMap(BluetoothSocket sokect, int idEntry) {
            String address = sokect.getRemoteDevice().getAddress();
            socketConnections.put(address, idEntry);
            socketsOpened.add(sokect);
        }

        private void closeSocket(BluetoothSocket socket) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("BLUETOOTH", "Can't close socket error 404");
            }
        }

        private void registerEntryClient(String idGoogleLogin, BluetoothSocket socket) {
            String url = BackEndEndpoints.ENTRY_CLIENT(shopData.getIdShop(), idGoogleLogin);
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRegisteringEntryDialog = FormUtils.showProgressDialog(getContext(), getResources(), R.string.registering_entry, R.string.please_wait);
                }
            });

            RequestUtils.sendStringRequest(getContext(), Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (!response.contains("@#")) {
                        Log.e("BLUETOOTH", "Max capacity");
                        closeSocket(socket);
                        Snackbar.make(getView(), getString(R.string.max_capacity_exceeded_message), Snackbar.LENGTH_LONG).show();
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRegisteringEntryDialog.dismiss();
                            }
                        });
                    } else {
                        String entryNumber = response.split("@#")[0];
                        Integer actualCapacity = Integer.parseInt(response.split("@#")[1]);
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("BLUETOOTH", "Entrada registrada:" + response);
                                Snackbar.make(getView(), getString(R.string.new_entry), Snackbar.LENGTH_LONG).show();
                                shopData.setActualCapacity(actualCapacity);
                                float percentage = ((float) shopData.getActualCapacity() / shopData.getMaxCapacity()) * 100;
                                percentageChartView.setProgress(percentage, true);
                                updateShopCapacityText();
                                mRegisteringEntryDialog.dismiss();
                            }
                        });
                        writeShopNameAndEntryNumber(socket, entryNumber);
                        storeSocketInMap(socket, Integer.parseInt(entryNumber));
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("BLUETOOTH", "Http error");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRegisteringEntryDialog.dismiss();
                        }
                    });

                    if (error instanceof TimeoutError) {
                        Log.e("BLUETOOTH", "Http error timetout");
                        closeSocket(socket);
                    } else {
                        int responseCode = RequestUtils.getErrorCodeRequest(error);
                        // 404 not found shop or client  (should not happen)
                        // 400 entry duplicate, send shop name and keep socket
                        // 202 max capacity, close socket
                        // 401 shop not open, close socket (should not happen)
                        // 401 client already entered, send shop name and keep socket
                        // unknow code close socket
                        Log.e("BLUETOOTH", "Http error " + responseCode);
                        switch (responseCode) {
                            case 404:
                                closeSocket(socket);
                                break;
                            case 400:
                                Log.e("BLUETOOTH", "Entry duplicated");
                                writeShopNameAndEntryNumber(socket, null);
                                break;
                            case 202:
                                Log.e("BLUETOOTH", "Max capacity");
                                closeSocket(socket);
                                break;
                            case 401:
                                String errorMessage = RequestUtils.getErrorMessageRequest(error);
                                Log.e("BLUETOOTH", "Error: " + errorMessage);
                                if (errorMessage.contains(RequestUtils.CLIENT_ALREADY_ENTERED)) {
                                    Log.e("BLUETOOTH", "Already entered");
                                    writeShopNameAndEntryNumber(socket, null);
                                } else {
                                    //Shop closed
                                    Log.e("BLUETOOTH", "Shop not opened");
                                    closeSocket(socket);
                                }
                                break;
                            default:
                                closeSocket(socket);
                                break;
                        }
                    }
                }
            });
        }

        private void catchDisconnectedBluetooth() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(getView(), getString(R.string.bluetooth_disconnected), Snackbar.LENGTH_LONG).show();
                    mCallback.bluetoothDisconnected();
                }
            });
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    Log.e("BLUETOOTH", "Socket's accept() method failed " + closeShop, e);
                    if (!closeShop) {
                        //Buetooth disconnected
                        catchDisconnectedBluetooth();
                    }
                    break;
                }

                if (socket != null) {
                    Log.e("BLUETOOTH", "Nueva conexión");
                    final BluetoothSocket socketFinal = socket;

                    //Read idGoogleLogin
                    String idGoogleLogin = readIdGoogleLoginFromClient(socketFinal);

                    if (idGoogleLogin != null) {
                        //Try to make entry
                        registerEntryClient(idGoogleLogin, socketFinal);
                    }
                }
            }
        }

        public void registerDisconnect(BluetoothDevice device) {
            String address = device.getAddress();
            Integer idEntry = socketConnections.get(address);
            if (idEntry != null) {
                Log.e("BLUETOOTH", "Detectando salida (entrada): " + idEntry);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        registerExitClient(idEntry, false);
                    }
                });

            }
        }

        public void cancel() {
            closeShop = true;
            try {
                Log.e("BLUETOOTH", "Closing server socket");
                for (BluetoothSocket socket : socketsOpened) {
                    try {
                        Log.e("BLUETOOTH", "Closing individual socket");
                        socket.getInputStream().close();
                        socket.getOutputStream().close();
                        socket.close();
                    } catch (IOException e) {
                        Log.e("BLUETOOTH", "Could not close the individual socket", e);
                    }
                }
                mServerSocket.close();
            } catch (IOException e) {
                Log.e("BLUETOOTH", "Could not close the connect socket", e);
            }
        }

    }
}