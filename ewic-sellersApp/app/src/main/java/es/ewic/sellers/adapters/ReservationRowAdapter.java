package es.ewic.sellers.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;

import es.ewic.sellers.R;
import es.ewic.sellers.model.Reservation;
import es.ewic.sellers.model.Seller;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.DateUtils;
import es.ewic.sellers.utils.RequestUtils;

public class ReservationRowAdapter extends BaseAdapter implements ListAdapter {

    private final List<Reservation> reservationList;
    private final Shop shop;
    private final Seller seller;
    private final Fragment fragment;
    private final Resources resources;
    private final String packageName;

    private OnEditReservationListener mCallback;

    public interface OnEditReservationListener {
        public void editReservation(Reservation reservation, Shop shop);
    }

    public ReservationRowAdapter(List<Reservation> reservationList, Shop shop, Seller seller, Fragment fragment, Resources resources, String packageName) {
        assert reservationList != null;
        assert shop != null;
        assert seller != null;
        assert fragment != null;
        assert resources != null;
        assert packageName != null;

        this.reservationList = reservationList;
        this.shop = shop;
        this.seller = seller;
        this.fragment = fragment;
        this.resources = resources;
        this.packageName = packageName;

        mCallback = (ReservationRowAdapter.OnEditReservationListener) fragment.getActivity();
    }

    @Override
    public int getCount() {
        if (reservationList == null) {
            return 0;
        } else {
            return reservationList.size();
        }
    }

    @Override
    public Reservation getItem(int position) {
        if (reservationList == null) {
            return null;
        } else {
            return reservationList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        Reservation rsv = getItem(position);
        return rsv.getIdReservation();
    }

    private void remove(int position) {
        reservationList.remove(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = fragment.getLayoutInflater().inflate(R.layout.reservation_row, null);
        }

        Reservation reservation = getItem(position);

        if (reservation != null) {
            TextView clientName = convertView.findViewById(R.id.reservation_client_name);
            clientName.setText(reservation.getClientName() + " (" + reservation.getnClients() + " " + (reservation.getnClients() == 1 ? resources.getString(R.string.client) : resources.getString(R.string.clients)) + ")");

            TextView reservationDate = convertView.findViewById(R.id.reservation_date);
            Calendar date = reservation.getDate();
            reservationDate.setText(DateUtils.formatDateLong(date));

            TextView reservationState = convertView.findViewById(R.id.reservation_state);
            reservationState.setText(resources.getIdentifier(reservation.getState(), "string", packageName));
            switch (reservation.getState()) {
                case "ACTIVE":
                    reservationState.setTextColor(resources.getColor(R.color.semaphore_green));
                    break;
                case "WAITING":
                    reservationState.setTextColor(resources.getColor(R.color.semaphore_green));
                    break;
                case "COMPLETED":
                    break;
                case "NOT_APPEAR":
                    reservationState.setTextColor(resources.getColor(R.color.semaphore_red));
                    break;
                case "CANCELLED":
                    reservationState.setTextColor(resources.getColor(R.color.semaphore_red));
                    break;
                default:
                    break;
            }

            TextView reservationRemarks = convertView.findViewById(R.id.reservation_remarks);
            reservationRemarks.setText(reservation.getRemarks());

            final View view = convertView;
            ImageView reservation_cancel_button = convertView.findViewById(R.id.reservation_cancel_button);
            reservation_cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPreCancelDialog(view, reservation, position);
                }
            });

            ImageView reservation_edit_button = convertView.findViewById(R.id.reservation_edit_button);
            reservation_edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.editReservation(reservation, shop);
                }
            });
        }
        return convertView;
    }

    private void showPreCancelDialog(View view, Reservation rsv, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext()).setTitle(R.string.warning).setMessage(R.string.pre_cancel_reservation_message);

        builder.setPositiveButton(R.string.accept, (dialog, which) -> cancelReservation(view, rsv, position));

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            // dialog cancelled;
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.semaphore_red));
            }
        });
        dialog.show();
    }


    private void cancelReservation(View view, Reservation rsv, int position) {
        String url = BackEndEndpoints.RESERVATION_BASE + "/" + rsv.getIdReservation() + "?loginName=" + seller.getLoginName();

        RequestUtils.sendStringRequest(fragment.getContext(), Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                remove(position);
                notifyDataSetChanged();
                Snackbar.make(view, resources.getString(R.string.update_data_successfully), Snackbar.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
                if (error instanceof TimeoutError) {
                    Snackbar snackbar = Snackbar.make(view, resources.getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            showPreCancelDialog(view, rsv, position);
                        }
                    });
                    snackbar.show();
                } else {
                    int responseCode = RequestUtils.getErrorCodeRequest(error);
                    // 404 rsv or client not found (should not happen)
                    // 401 rsv not mutable
                    if (responseCode == 401) {
                        remove(position);
                        notifyDataSetChanged();
                        Snackbar.make(view, resources.getString(R.string.update_data_successfully), Snackbar.LENGTH_SHORT)
                                .show();
                    } else {
                        Snackbar snackbar = Snackbar.make(view, resources.getString(R.string.error_server), Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                                showPreCancelDialog(view, rsv, position);
                            }
                        });
                        snackbar.show();
                    }

                }
            }
        });

    }
}
