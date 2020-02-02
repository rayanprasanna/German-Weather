package com.universl.kalagune.germanweather.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.universl.kalagune.germanweather.Constants;
import com.universl.kalagune.germanweather.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CommonUtils {

    public final static String THEME_DARK = "dark";
    public final static String THEME_CLASSIC_DARK = "classicdark";
    public final static String THEME_CLASSIC_BLACK = "classicblack";
    public final static String THEME_CLASSIC = "classic";
    public final static String THEME_BLACK = "black";
    public final static String THEME_CLASSIC_SKY = "classicsky";

    public static boolean checkNetwork(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean dataEnabled(Context context, boolean enabled) {
        try {
            final ConnectivityManager conman = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass
                    .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isGpsEnabled(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        return Settings.Secure.isLocationProviderEnabled(contentResolver, Constants.GPS_PROVIDER);

    }

    public static int getStatusImage(Context context,String status){

        if(status!=null || !status.isEmpty()){

            if( context.getString(R.string.weather_cloudy).equalsIgnoreCase(status)){
                return R.drawable.status_cloudy;
            }else if( context.getString(R.string.weather_clear_night).equalsIgnoreCase(status)){
                return R.drawable.status_clear_night;
            }else if( context.getString(R.string.weather_sunny).equalsIgnoreCase(status)){
                return R.drawable.status_clear;
            } else if(context.getString(R.string.weather_drizzle).equalsIgnoreCase(status)){
                return R.drawable.status_drizzle;
            } else if(context.getString(R.string.weather_foggy).equalsIgnoreCase(status)){
                return R.drawable.status_foggy;
            } else if(context.getString(R.string.weather_rainy).equalsIgnoreCase(status)){
                return R.drawable.status_rain;
            } else if(context.getString(R.string.weather_thunder).equalsIgnoreCase(status)){
                return R.drawable.status_thunder;
            } else if(context.getString(R.string.weather_snowy).equalsIgnoreCase(status)){
                return R.drawable.status_snow;
            }
        }
        return R.drawable.status_normal;
    }

    public static String getStatusTheme(Context context,String status){
        if(status!=null || !status.isEmpty()){
            if( context.getString(R.string.weather_cloudy).equalsIgnoreCase(status)){
                return CommonUtils.THEME_CLASSIC_DARK;
            }else if( context.getString(R.string.weather_clear_night).equalsIgnoreCase(status)){
                return CommonUtils.THEME_DARK;
            }else if( context.getString(R.string.weather_sunny).equalsIgnoreCase(status)){
                return CommonUtils.THEME_CLASSIC_SKY;
            } else if(context.getString(R.string.weather_drizzle).equalsIgnoreCase(status)){
                return CommonUtils.THEME_CLASSIC;
            } else if(context.getString(R.string.weather_foggy).equalsIgnoreCase(status)){
                return CommonUtils.THEME_DARK;
            } else if(context.getString(R.string.weather_rainy).equalsIgnoreCase(status)){
                return CommonUtils.THEME_CLASSIC_DARK;
            } else if(context.getString(R.string.weather_thunder).equalsIgnoreCase(status)){
                return CommonUtils.THEME_CLASSIC_DARK;
            } else if(context.getString(R.string.weather_snowy).equalsIgnoreCase(status)){
                return CommonUtils.THEME_CLASSIC_SKY;
            }
        }

        return "";

    }
}
