package es.ewic.sellers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import es.ewic.sellers.model.Seller;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.FragmentUtils;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginListener,
        MyShopsFragment.OnMyShopsListener,
        ShopCapacityFragment.OnShopCapacityListener,
        MyDataFragment.OnMyDataListener {

    private Seller seller;
    private Shop openShop;

    private boolean enableMyData = true;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.my_toolbar).setVisibility(View.GONE);

        FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), LoginFragment.newInstance(), false);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.BLUETOOTH_PRIVILEGED},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem itemMyData = menu.findItem(R.id.action_my_data);
        itemMyData.setEnabled(enableMyData);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_my_data:
                if (seller != null) {
                    FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), MyDataFragment.newInstance(seller), true);
                    enableMyData = false;
                    invalidateOptionsMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            enableMyData = true;
            invalidateOptionsMenu();
            getSupportFragmentManager().popBackStack();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            enableMyData = true;
            invalidateOptionsMenu();
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public void onLoadSellerData(Seller sellerData) {
        seller = sellerData;

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(myToolbar);

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

    @Override
    public void onUpdateSellerAccount(Seller newSeller) {
        this.seller = newSeller;
    }
}