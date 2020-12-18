package es.ewic.sellers.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;

public class FormUtils {

    public static ProgressDialog showProgressDialog(Context contex, Resources resources, int title, int message) {
        ProgressDialog pd = new ProgressDialog(contex);
        pd.setTitle(resources.getString(title));
        pd.setMessage(resources.getString(message));
        pd.setCancelable(false);
        pd.show();
        return pd;
    }
}
