package es.ewic.sellers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;

import es.ewic.sellers.model.Seller;
import es.ewic.sellers.model.Shop;
import es.ewic.sellers.utils.FragmentUtils;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginListener, MyShops.OnMyShopsListener, ShopCapacity.OnShopCapacityListener {

    private Seller seller;
    private Shop openShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(myToolbar);

        FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), LoginFragment.newInstance(), false);
    }

    @Override
    public void onLoadSellerData(Seller sellerData) {
        seller = sellerData;
        FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), MyShops.newInstance(seller), false);

    }

    @Override
    public void onShopClick(Shop shop) {
        if (seller != null) {
            openShop = shop;
            FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), ShopCapacity.newInstance(openShop), true);
        }
    }

    @Override
    public void shopClosed() {
        if (seller != null) {
            openShop = null;
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                FragmentUtils.getInstance().replaceFragment(getSupportFragmentManager(), MyShops.newInstance(seller), false);
            }
        }
    }
}