package com.example.aurorafitness;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class CheckInternet {

    //method to check if the device has an internet connection
    public static boolean isNetworkAvailable(Context con) {
        try {

            //instance of connectivity manager to get the connectivity service
            ConnectivityManager cm = (ConnectivityManager) con
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            //instance of network info
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            //checks if there is network info else returns false
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
