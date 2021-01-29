package es.ewic.sellers.adapters;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.List;

import es.ewic.sellers.R;
import es.ewic.sellers.model.Reservation;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.DateUtils;

public class ReservationRowAdapter extends BaseAdapter implements ListAdapter {

    private final List<Reservation> reservationList;
    private final Shop shop;
    private final Fragment fragment;
    private final Resources resources;
    private final String packageName;

//    private OnEditReservationListener mCallback;
//
//    public interface OnEditReservationListener {
//        public void editReservation(Reservation reservation);
//    }

    public ReservationRowAdapter(List<Reservation> reservationList, Shop shop, Fragment fragment, Resources resources, String packageName) {
        assert reservationList != null;
        assert shop != null;
        assert fragment != null;
        assert resources != null;
        assert packageName != null;

        this.reservationList = reservationList;
        this.shop = shop;
        this.fragment = fragment;
        this.resources = resources;
        this.packageName = packageName;

        //mCallback = (ReservationRowAdapter.OnEditReservationListener) fragment.getActivity();
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
        }
        return convertView;
    }
}
