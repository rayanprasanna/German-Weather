package com.universl.kalagune.germanweather.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

import com.universl.kalagune.germanweather.R;
import com.universl.kalagune.germanweather.utils.CommonUtils;

import java.util.Objects;

public class SplashActivity extends Activity {
    int theme;
    int i;
    LinearLayout appView;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(theme = getTheme(Objects.requireNonNull(prefs.getString("theme", "fresh"))));
        boolean darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark;
        boolean blackTheme = theme == R.style.AppTheme_NoActionBar_Black ||
                theme == R.style.AppTheme_NoActionBar_Classic_Black;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        setTheme();
        appView = findViewById(R.id.splashImageView);
        setTheme();
        startMain();
    }

    private void setTheme() {
        final int sdk = android.os.Build.VERSION.SDK_INT;

        if (i == 1) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(R.drawable.wallpaper_3);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            }
        } else if (i == 2) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(R.drawable.wallpaper_3);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            }
        } else if (i == 3) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(R.drawable.wallpaper_2);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            }
        } else if (i == 4) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(R.drawable.wallpaper_2);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            }
        } else if (i == 5) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(R.drawable.wallpaper_1);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            }
        } else if (i == 7) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(R.drawable.wallpaper_1);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            }
        } else if (i == 8) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(R.drawable.wallpaper_4);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            }
        } else if (i == 9) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(R.drawable.wallpaper_4);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            }
        } else if (i == 10) {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(R.drawable.wallpaper_3);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            }
        } else {
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setImageResource(R.drawable.wallpaper_3);
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.weather_german_splash));
            }
        }
    }

    int progressBarStatus;

    private void startMain() {
        if (CommonUtils.checkNetwork(SplashActivity.this)) {
            if (CommonUtils.isGpsEnabled(getApplicationContext())) {
                showMainActivity();
            } else {
                showLocationSettingsDialog();
            }
        } else {
            dataAlert();
            new Thread(new Runnable() {
                @Override
                public void run() {

                    while (progressBarStatus < 20) {

                        if (CommonUtils.checkNetwork(SplashActivity.this)) {
                            progressBarStatus++;
                        }

                        if (progressBarStatus == 20) {
                            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            SplashActivity.this.finish();
                        }
                    }

                }
            }).start();
        }

    }

    private void showMainActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, 4000);
    }

    public void dataAlert() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getResources().getString(R.string.app_name));
        dialogBuilder.setIcon(R.mipmap.ic_logo);
        dialogBuilder.setMessage(R.string.data_settings_message);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                SplashActivity.this.finish();
                System.exit(0);

            }
        });
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                boolean res = CommonUtils.dataEnabled(SplashActivity.this, true);
                if (!CommonUtils.isGpsEnabled(getApplicationContext())) {
                    showLocationSettingsDialog();
                } else {
                    showMainActivity();
                }
            }
        });

        final AlertDialog alert = dialogBuilder.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface d) {
                Button posButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                LinearLayout linearLayout = (LinearLayout) posButton.getParent();
                linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                linearLayout.setWeightSum(2);

                int btnwidth = (linearLayout.getWidth() / 2) - (linearLayout.getWidth() / 20);
                posButton.setWidth(btnwidth);
                negButton.setWidth(btnwidth);

            }
        });

        alert.show();
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.location_settings);
        alertDialog.setMessage(R.string.location_settings_message);
        alertDialog.setPositiveButton(R.string.location_settings_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                finish();
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });
        alertDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private int getTheme(String themePref) {
        switch (themePref) {
            case "dark":
                i = 1;
                return R.style.AppTheme_NoActionBar_Dark;
            case "black":
                i = 2;
                return R.style.AppTheme_NoActionBar_Black;
            case "classic":
                i = 3;
                return R.style.AppTheme_NoActionBar_Classic;
            case "classicdark":
                i = 4;
                return R.style.AppTheme_NoActionBar_Classic_Dark;
            case "classicblack":
                i = 5;
                return R.style.AppTheme_NoActionBar_Classic_Black;
            case "randomclassicdark":
                i = 7;
                return R.style.AppTheme_NoActionBar_Random_Classic_Dark;
            case "randomclassicblack":
                i = 8;
                return R.style.AppTheme_NoActionBar_Random_Classic_Black;
            case "randomfreshdark":
                i = 9;
                return R.style.AppTheme_NoActionBar_Random_Fresh_Dark;
            case "randomfreshblack":
                i = 10;
                return R.style.AppTheme_NoActionBar_Random_Fresh_Black;
            default:
                i = 6;
                return R.style.AppTheme_NoActionBar;
        }
    }

}
