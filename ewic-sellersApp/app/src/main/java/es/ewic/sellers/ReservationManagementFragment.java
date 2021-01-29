package es.ewic.sellers;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.util.List;

import es.ewic.sellers.adapters.ReservationRowAdapter;
import es.ewic.sellers.model.Reservation;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.BackEndEndpoints;
import es.ewic.sellers.utils.ModelConverter;
import es.ewic.sellers.utils.RequestUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReservationManagementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReservationManagementFragment extends Fragment {

    private static final String ARG_SHOP = "shop";

    private Shop shop;
    private List<Reservation> reservations;

    OnReservationManagementFragment mCallback;

    public interface OnReservationManagementFragment {
        void onCreateNewRsv(Shop shop);
    }

    public ReservationManagementFragment() {
        // Required empty public constructor
    }

    public static ReservationManagementFragment newInstance(Shop shop) {
        ReservationManagementFragment fragment = new ReservationManagementFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SHOP, shop);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallback = (ReservationManagementFragment.OnReservationManagementFragment) getActivity();
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.reservation_management);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_reservation_management, container, false);

        // Reload list when refresh
        SwipeRefreshLayout swipeRefreshLayout = parent.findViewById(R.id.swipeRefreshLayoutReservatios);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getReservations(parent, swipeRefreshLayout);
        });

        getReservations(parent, swipeRefreshLayout);

        FloatingActionButton add_reservation = parent.findViewById(R.id.add_reservation);
        add_reservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onCreateNewRsv(shop);
            }
        });

        return parent;
    }


    private void getReservations(ConstraintLayout parent, SwipeRefreshLayout swipe) {

        String url = BackEndEndpoints.RESERVATION_BASE + "/" + shop.getIdShop();

        RequestUtils.sendJsonArrayRequest(getContext(), Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                reservations = ModelConverter.jsonArrayToReservationList(response);
                ListView reservationsList = parent.findViewById(R.id.reservations_list);
                ReservationRowAdapter reservationRowAdapter = new ReservationRowAdapter(reservations, shop, ReservationManagementFragment.this, getResources(), getActivity().getPackageName());
                reservationsList.setAdapter(reservationRowAdapter);
                swipe.setRefreshing(false);
                TextView reservations_not_found = parent.findViewById(R.id.reservations_not_found);
                if (reservations.isEmpty()) {
                    reservations_not_found.setVisibility(View.VISIBLE);
                    reservationsList.setVisibility(View.GONE);
                } else {
                    reservations_not_found.setVisibility(View.GONE);
                    reservationsList.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("HTTP", "error");
                swipe.setRefreshing(false);
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.error_connect_server), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        swipe.setRefreshing(true);
                        getReservations(parent, swipe);
                    }
                });
                snackbar.show();
            }
        });
    }
}