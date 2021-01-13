package es.ewic.sellers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import es.ewic.sellers.model.Seller;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.FragmentUtils;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginListener, MyShopsFragment.OnMyShopsListener, ShopCapacityFragment.OnShopCapacityListener {

    private Seller seller;
    private Shop openShop;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(myToolbar);

        FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), LoginFragment.newInstance(), false);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.BLUETOOTH_PRIVILEGED},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public void onLoadSellerData(Seller sellerData) {
        seller = sellerData;
        FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), MyShopsFragment.newInstance(seller), false);

    }

    @Override
    public void onShopClick(Shop shop) {
        if (seller != null) {
            openShop = shop;
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), ShopCapacityFragment.newInstance(openShop), true);
        }
    }

    @Override
    public void shopClosed() {
        if (seller != null) {
            openShop = null;
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), MyShopsFragment.newInstance(seller), false);
            }
        }
    }

    @Override
    public void bluetoothDisconnected() {
        if (seller != null) {
            openShop = null;
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), MyShopsFragment.newInstance(seller), false);
            }
        }
    }
}