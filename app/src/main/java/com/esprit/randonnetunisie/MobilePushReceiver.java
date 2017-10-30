package com.esprit.randonnetunisie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

/**
 * Created by youss on 23/11/2016.
 */

public class MobilePushReceiver extends BroadcastReceiver {

    private static boolean firstConnect = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null) {
            if(firstConnect) {
                // do subroutines here
                Bundle extras = intent.getExtras();
                Intent i = new Intent("broadCastName");
                // Data you need to pass to activity
                i.putExtras(extras);
                context.sendBroadcast(i);
                firstConnect = false;
            }
        }
        else {
            firstConnect= true;
        }
    }
}
