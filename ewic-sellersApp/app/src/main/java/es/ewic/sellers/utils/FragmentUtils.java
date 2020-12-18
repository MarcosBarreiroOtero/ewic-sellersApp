package es.ewic.sellers.utils;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import es.ewic.sellers.R;

public class FragmentUtils {

    private static final FragmentUtils fragmentUtils = new FragmentUtils();
    private static final String TAG = FragmentManager.class.getSimpleName();

    public static FragmentUtils getInstance() {
        return fragmentUtils;
    }

    public static void replaceFragment(FragmentManager fragmentManager, Fragment fragment, boolean addToBackStack) {
        try {
            FragmentTransaction transaction = fragmentManager.beginTransaction().replace(R.id.mainActivityLayout, fragment,
                    fragment.getClass().getName());
            if (addToBackStack) {
                transaction.setReorderingAllowed(true);
                transaction.addToBackStack(null);
            }
            transaction.commit();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }
}
