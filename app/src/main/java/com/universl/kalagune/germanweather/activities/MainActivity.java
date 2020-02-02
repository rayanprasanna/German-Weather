package com.universl.kalagune.germanweather.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.universl.kalagune.germanweather.AlarmReceiver;
import com.universl.kalagune.germanweather.Constants;
import com.universl.kalagune.germanweather.NowFragment;
import com.universl.kalagune.germanweather.R;
import com.universl.kalagune.germanweather.adapters.ViewPagerAdapter;
import com.universl.kalagune.germanweather.adapters.WeatherRecyclerAdapter;
import com.universl.kalagune.germanweather.fragments.RecyclerViewFragment;
import com.universl.kalagune.germanweather.models.Weather;
import com.universl.kalagune.germanweather.tasks.GenericRequestTask;
import com.universl.kalagune.germanweather.tasks.ParseResult;
import com.universl.kalagune.germanweather.tasks.TaskOutput;
import com.universl.kalagune.germanweather.utils.UnitConvertor;
import com.universl.kalagune.germanweather.widgets.AbstractWidgetProvider;
import com.universl.kalagune.germanweather.widgets.DashClockWeatherExtension;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener, NowFragment.OnFragmentInteractionListener{
    protected static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    protected static final int MY_PERMISSIONS_SEND_SMS = 2;

    // Time in milliseconds; only reload weather if last update is longer ago than this value
    private static final int NO_UPDATE_REQUIRED_THRESHOLD = 300000;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public static JSONObject object;
    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);
    private static boolean mappingsInitialised = false;
    private static boolean isAdView = false;
    public String recentCity = "";
    //private InterstitialAd interstitial;
    Typeface weatherFont;
    Weather todayWeather = new Weather();
    ViewPager viewPager;
    TabLayout tabLayout;
    View appView;
    LocationManager locationManager;
    ProgressDialog progressDialog;
    int theme;
    boolean destroyed = false;
    private Boolean mLocationPermissionsGranted = false;
    private List<Weather> longTermWeather = new ArrayList<>();
    private List<Weather> longTermTodayWeather = new ArrayList<>();
    private List<Weather> longTermTomorrowWeather = new ArrayList<>();
    private List<Weather> longTermTomorrowWeather3 = new ArrayList<>();
    private List<Weather> todayWeathera = new ArrayList<>();
    private int i;

    public static String getRainString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail");
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        return rain;
    }

    public static void initMappings() {
        if (mappingsInitialised)
            return;
        mappingsInitialised = true;
        speedUnits.put("m/s", R.string.speed_unit_mps);
        speedUnits.put("kph", R.string.speed_unit_kph);
        speedUnits.put("mph", R.string.speed_unit_mph);
        speedUnits.put("kn", R.string.speed_unit_kn);

        pressUnits.put("hPa", R.string.pressure_unit_hpa);
        pressUnits.put("kPa", R.string.pressure_unit_kpa);
        pressUnits.put("mm Hg", R.string.pressure_unit_mmhg);
    }

    public static String localize(SharedPreferences sp, Context context, String preferenceKey, String defaultValueKey) {
        String preferenceValue = sp.getString(preferenceKey, defaultValueKey);
        String result = preferenceValue;
        if ("speedUnit".equals(preferenceKey)) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue));
            }
        } else if ("pressureUnit".equals(preferenceKey)) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue));
            }
        }
        return result;
    }

    public static String getWindDirectionString(SharedPreferences sp, Context context, Weather weather) {
        try {
            if (Double.parseDouble(weather.getWind()) != 0) {
                String pref = sp.getString("windDirectionFormat", null);
                if ("arrow".equals(pref)) {
                    return weather.getWindDirection(8).getArrow(context);
                } else if ("abbr".equals(pref)) {
                    return weather.getWindDirection().getLocalizedString(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static long saveLastUpdateTime(SharedPreferences sp) {
        Calendar now = Calendar.getInstance();
        sp.edit().putLong("lastUpdate", now.getTimeInMillis()).apply();
        return now.getTimeInMillis();
    }

    public static String formatTimeWithDayIfNotToday(Context context, long timeInMillis) {
        Calendar now = Calendar.getInstance();
        Calendar lastCheckedCal = new GregorianCalendar();
        lastCheckedCal.setTimeInMillis(timeInMillis);
        Date lastCheckedDate = new Date(timeInMillis);
        String timeFormat = android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate);
        if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)) {
            // Same day, only show time
            return timeFormat;
        } else {
            return android.text.format.DateFormat.getDateFormat(context).format(lastCheckedDate) + " " + timeFormat;
        }
    }

//    public static long saveLastUpdateTime(SharedPreferences sp) {
//        Calendar now = Calendar.getInstance();
//        sp.edit().putLong("lastUpdate", now.getTimeInMillis()).apply();
//        return now.getTimeInMillis();
//    }
//
//    public static String formatTimeWithDayIfNotToday(Context context, long timeInMillis) {
//        Calendar now = Calendar.getInstance();
//        Calendar lastCheckedCal = new GregorianCalendar();
//        lastCheckedCal.setTimeInMillis(timeInMillis);
//        Date lastCheckedDate = new Date(timeInMillis);
//        String timeFormat = android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate);
//        if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
//                now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)) {
//            // Same day, only show time
//            return timeFormat;
//        } else {
//            return android.text.format.DateFormat.getDateFormat(context).format(lastCheckedDate) + " " + timeFormat;
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the associated SharedPreferences file with default values
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(theme = getTheme(prefs.getString("theme", "fresh")));
        boolean darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark;
        boolean blackTheme = theme == R.style.AppTheme_NoActionBar_Black ||
                theme == R.style.AppTheme_NoActionBar_Classic_Black;

        // Initiate activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        appView = findViewById(R.id.viewApp);

        progressDialog = new ProgressDialog(MainActivity.this);

        // Load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (darkTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
        } else if (blackTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Black);
        }


        // Initialize viewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        setTheme();

        //interstitial = new InterstitialAd(MainActivity.this);
        destroyed = false;
        getLocationPermission();
//       // Weather we=new Weather();
//        //todayWeathera.add(we);
//        initMappings();
//        getCityByLocation();
//        // Preload data from cache
//        preloadWeather();
//        updateLastUpdateTime();
//
//        // Set autoupdater
//        AlarmReceiver.setRecurringAlarm(this);
        //initAds();
        //updateTodayWeatherUI();
        //smsNotify();
    }

    private void setTheme() {
        final int sdk = android.os.Build.VERSION.SDK_INT;

        if (i == 1) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.fresh_dark2));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.fresh_dark2));
            }
        } else if (i == 2) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.fresh_black));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.fresh_black));
            }
        } else if (i == 3) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.classic_2));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.classic_2));
            }
        } else if (i == 4) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.classic_dark));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.classic_dark));
            }
        } else if (i == 5) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.classic_black));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.classic_black));
            }
        } else if (i == 7) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.crdark));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.crdark));
            }
        } else if (i == 8) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.crblack));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.crblack));
            }
        } else if (i == 9) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.frdark));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.frdark));
            }
        } else if (i == 10) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.frblack));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.frblack));
            }
        } else {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.fresh));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.fresh));
            }
        }
    }

    private void getLocationPermission() {
//        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
//                initMap();
                destroyed = false;
                initMappings();
                getCityByLocation();
                // Preload data from cache

                updateLastUpdateTime();

                // Set autoupdater
                AlarmReceiver.setRecurringAlarm(this);
                updateTodayWeatherUI();
                preloadWeather();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public WeatherRecyclerAdapter getAdapter(int id) {
        WeatherRecyclerAdapter weatherRecyclerAdapter;
        if (id == 1) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTodayWeather);
        } else if (id == 2) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTomorrowWeather);
        } else {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermWeather);
        }
        return weatherRecyclerAdapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        preloadWeather();
        updateLastUpdateTime();

        // Set autoupdater
        AlarmReceiver.setRecurringAlarm(this);
        if (getTheme(PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "fresh")) != theme) {
            // Restart activity to apply theme
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
        } else if (shouldUpdate() && isNetworkAvailable()) {
            getTodayWeather();
            getLongTermWeather();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;

        if (locationManager != null) {
            try {
                locationManager.removeUpdates(MainActivity.this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void preloadWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String lastToday = sp.getString("lastToday", "");
        if (!lastToday.isEmpty()) {
            new TodayWeatherTask(this, this, progressDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "cachedResponse", lastToday);
        }
        String lastLongterm = sp.getString("lastLongterm", "");
        if (!lastLongterm.isEmpty()) {
            new LongTermWeatherTask(this, this, progressDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "cachedResponse", lastLongterm);
        }
    }

    private void getTodayWeather() {
        new TodayWeatherTask(this, this, progressDialog).execute();
    }

    private void getLongTermWeather() {
        new LongTermWeatherTask(this, this, progressDialog).execute();
    }

    @SuppressLint("RestrictedApi")
    private void searchCities() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(this.getString(R.string.search_title));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        input.setSingleLine(true);
        alert.setView(input, 32, 0, 32, 0);
        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = input.getText().toString();
                if (!result.isEmpty()) {
                    saveLocation(result);
                }
            }
        });
        alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        alert.show();
    }

    private void saveLocation(String result) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        recentCity = preferences.getString("city", Constants.DEFAULT_CITY);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("city", result);
        editor.commit();

        if (!recentCity.equals(result)) {
            // New location, update weather
            getTodayWeather();
            getLongTermWeather();
        }
    }

    private String setWeatherIcon(int actualId, int hourOfDay) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 20) {
                icon = this.getString(R.string.weather_sunny);
            } else {
                icon = this.getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = this.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = this.getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = this.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = this.getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = this.getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = this.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }

    private ParseResult parseTodayJson(String result) {
        try {
            JSONObject reader = new JSONObject(result);
            System.out.println("RESULTSSS result " + result);
            System.out.println("RESULTSSS " + reader.toString());

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                todayWeathera = new ArrayList<>();
                return ParseResult.CITY_NOT_FOUND;
            }

            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = countryObj.getString("country");
                todayWeather.setSunrise(countryObj.getString("sunrise"));
                todayWeather.setSunset(countryObj.getString("sunset"));
            }
            todayWeather.setCity(city);
            todayWeather.setCountry(country);

            JSONObject coordinates = reader.getJSONObject("coord");
            if (coordinates != null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                sp.edit().putFloat("latitude", (float) coordinates.getDouble("lon")).putFloat("longitude", (float) coordinates.getDouble("lat")).commit();
            }

            JSONObject main = reader.getJSONObject("main");

            todayWeather.setTemperature(main.getString("temp"));
            todayWeather.setDescription(reader.getJSONArray("weather").getJSONObject(0).getString("description"));
            JSONObject windObj = reader.getJSONObject("wind");
            todayWeather.setWind(windObj.getString("speed"));
            if (windObj.has("deg")) {
                todayWeather.setWindDirectionDegree(windObj.getDouble("deg"));
            } else {
                Log.e("parseTodayJson", "No wind direction available");
                todayWeather.setWindDirectionDegree(null);
            }
            todayWeather.setPressure(main.getString("pressure"));
            todayWeather.setHumidity(main.getString("humidity"));

            JSONObject rainObj = reader.optJSONObject("rain");
            String rain;
            if (rainObj != null) {
                rain = getRainString(rainObj);
            } else {
                JSONObject snowObj = reader.optJSONObject("snow");
                if (snowObj != null) {
                    rain = getRainString(snowObj);
                } else {
                    rain = "0.0";
                }

            }
            System.out.println(rain + "RAIN");
            todayWeather.setRain(rain);

            final String idString = reader.getJSONArray("weather").getJSONObject(0).getString("id");
            todayWeather.setId(idString);
            todayWeather.setIcon(setWeatherIcon(Integer.parseInt(idString), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
            // todayWeathera.add(todayWeather);


            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastToday", result);
            editor.commit();


        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    private void updateTodayWeatherUI() {

        try {
            if (todayWeather.getCountry().isEmpty()) {
                preloadWeather();
                return;
            }
        } catch (Exception e) {
            preloadWeather();
            return;
        }
        String city = todayWeather.getCity();
        String country = todayWeather.getCountry();
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        getSupportActionBar().setTitle(city + (country.isEmpty() ? "" : ", " + country));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        // Temperature
        float temperature = UnitConvertor.convertTemperature(Float.parseFloat(todayWeather.getTemperature()), sp);
        if (sp.getBoolean("temperatureInteger", false)) {
            temperature = Math.round(temperature);
        }

        // Rain
        double rain = Double.parseDouble(todayWeather.getRain());
        String rainString = UnitConvertor.getRainString(rain, sp);

        // Wind
        double wind;
        try {
            wind = Double.parseDouble(todayWeather.getWind());
        } catch (Exception e) {
            e.printStackTrace();
            wind = 0;
        }
        wind = UnitConvertor.convertWind(wind, sp);

        // Pressure
        double pressure = UnitConvertor.convertPressure((float) Double.parseDouble(todayWeather.getPressure()), sp);
        String t = getString(R.string.temperature) + ": " + new DecimalFormat("0.#").format(temperature) + " " + sp.getString("unit", "°C");
        String d = todayWeather.getDescription().substring(0, 1).toUpperCase() +
                todayWeather.getDescription().substring(1) + rainString;
        String w = null;
        if (sp.getString("speedUnit", "m/s").equals("bft")) {
            w = getString(R.string.wind) + ": " +
                    UnitConvertor.getBeaufortName((int) wind) +
                    (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : "");
        } else {
            w = getString(R.string.wind) + ": " + new DecimalFormat("#.0").format(wind) + " " +
                    localize(sp, "speedUnit", "m/s") +
                    (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : "");
        }
        String p = getString(R.string.pressure) + ": " + new DecimalFormat("#.0").format(pressure) + " " +
                localize(sp, "pressureUnit", "hPa");
        String h = getString(R.string.varshawa_athi_weema) + ": " + todayWeather.getHumidity() + " %";
        String sr = getString(R.string.sunrise) + ": " + timeFormat.format(todayWeather.getSunrise());
        String ss = getString(R.string.sunset) + ": " + timeFormat.format(todayWeather.getSunset());
        String ti = todayWeather.getIcon();//getString(R.string.weather_sunny);
        String pre = getString(R.string.rain) + ": " + todayWeather.getRain() + "%";
        //todayWeathera.add(todayWeather);
       /* todayTemperature.setText(new DecimalFormat("0.#").format(temperature) + " " + sp.getString("unit", "°C"));
        todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() +
                todayWeather.getDescription().substring(1) + rainString);
        if (sp.getString("speedUnit", "m/s").equals("bft")) {
            todayWind.setText(getString(R.string.wind) + ": " +
                    UnitConvertor.getBeaufortName((int) wind) +
                    (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : ""));
        } else {
            todayWind.setText(getString(R.string.wind) + ": " + new DecimalFormat("#.0").format(wind) + " " +
                    localize(sp, "speedUnit", "m/s") +
                    (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : ""));
        }
        todayPressure.setText(getString(R.string.pressure) + ": " + new DecimalFormat("#.0").format(pressure) + " " +
                localize(sp, "pressureUnit", "hPa"));
        todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeather.getHumidity() + " %");
        todaySunrise.setText(getString(R.string.sunrise) + ": " + timeFormat.format(todayWeather.getSunrise()));
        todaySunset.setText(getString(R.string.sunset) + ": " + timeFormat.format(todayWeather.getSunset()));

        todayIcon.setText(todayWeather.getIcon());*/

        // NowFragment.setWeather(t,d,w,p,h,sr,ss,ti);
        Intent i = new Intent("weather updates");
        i.putExtra("temp", t);
        i.putExtra("des", d);
        i.putExtra("w", w);
        i.putExtra("p", p);
        i.putExtra("h", h);
        i.putExtra("sr", sr);
        i.putExtra("ss", ss);
        i.putExtra("ti", ti);
        i.putExtra("pre", pre);
        sendBroadcast(i);
        SharedPreferences sharedpref = getSharedPreferences("weather", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedpref.edit();
        editor1.putString("temp", t);
        editor1.putString("des", d);
        editor1.putString("w", w);
        editor1.putString("p", p);
        editor1.putString("h", h);
        editor1.putString("sr", sr);
        editor1.putString("ss", ss);
        editor1.putString("tl", ti);
        editor1.putString("pre", pre);
        //PreferenceManager.getDefaultSharedPreferences(this).edit().putString("theme", CommonUtils.getStatusTheme(this,ti)).commit();
        editor1.apply();

        /*if (getTheme(PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "fresh")) != theme) {
            // Restart activity to apply theme
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
        }*/
    }

    public ParseResult parseLongTermJson(String result) {
        int i;
        try {
            JSONObject reader = new JSONObject(result);
            System.out.println("MainActivty Result :" + result.toString());
            System.out.println("MainActivty Reader Result :" + reader.toString());

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                if (longTermWeather == null) {
                    todayWeathera = new ArrayList<>();
                    longTermWeather = new ArrayList<>();
                    longTermTodayWeather = new ArrayList<>();
                    longTermTomorrowWeather = new ArrayList<>();
                }
                return ParseResult.CITY_NOT_FOUND;
            }
            todayWeathera = new ArrayList<>();
            longTermWeather = new ArrayList<>();
            longTermTodayWeather = new ArrayList<>();
            longTermTomorrowWeather = new ArrayList<>();


            JSONArray list = reader.getJSONArray("list");
            int k = 0;
            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");

                weather.setDate(listItem.getString("dt"));
                weather.setTemperature(main.getString("temp"));
                weather.setDescription(listItem.optJSONArray("weather").getJSONObject(0).getString("description"));
                JSONObject windObj = listItem.optJSONObject("wind");
                if (windObj != null) {
                    weather.setWind(windObj.getString("speed"));
                    weather.setWindDirectionDegree(windObj.getDouble("deg"));
                }
                weather.setPressure(main.getString("pressure"));
                weather.setHumidity(main.getString("humidity"));

                JSONObject rainObj = listItem.optJSONObject("rain");
                String rain = "";
                if (rainObj != null) {
                    rain = getRainString(rainObj);
                } else {
                    JSONObject snowObj = listItem.optJSONObject("snow");
                    if (snowObj != null) {
                        rain = getRainString(snowObj);
                    } else {
                        rain = "0";
                    }
                }
                weather.setRain(rain);

                final String idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id");
                weather.setId(idString);

                final String dateMsString = listItem.getString("dt") + "000";
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(dateMsString));
                weather.setIcon(setWeatherIcon(Integer.parseInt(idString), cal.get(Calendar.HOUR_OF_DAY)));

                Calendar today = Calendar.getInstance();

                if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    longTermTodayWeather.add(weather);

                    if (k < 1) {
                        todayWeathera.add(weather);
                    }
                    k++;
                } else if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1) {
                    System.out.println("Cal.Calender " + cal.get(Calendar.DAY_OF_YEAR));
                    System.out.println("today.get " + today.get(Calendar.DAY_OF_YEAR));
                    System.out.println("today.get+1 " + today.get(Calendar.DAY_OF_YEAR) + 1);
                    longTermTomorrowWeather.add(weather);
                } /*else if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 2) {
                    longTermTomorrowWeather3.add(weather);

                } */ else if ((cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) | ((cal.get(Calendar.HOUR_OF_DAY) == today.get(Calendar.HOUR_OF_DAY)))) {
                    //todayWeathera.add(weather);
                } else {
                    longTermWeather.add(weather);
                }

            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastLongterm", result);
            editor.commit();
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    private void updateLongTermWeatherUI() {
        if (destroyed) {
            return;
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundleNow = new Bundle();
        bundleNow.putInt("day", 0);
        RecyclerViewFragment recyclerViewFragmentNow = new RecyclerViewFragment();
        recyclerViewFragmentNow.setArguments(bundleNow);
        viewPagerAdapter.addFragment(recyclerViewFragmentNow, "Jetzt");

        Bundle bundleToday = new Bundle();
        bundleToday.putInt("day", 1);
        RecyclerViewFragment recyclerViewFragmentToday = new RecyclerViewFragment();
        recyclerViewFragmentToday.setArguments(bundleToday);
        viewPagerAdapter.addFragment(recyclerViewFragmentToday, getString(R.string.today));

        Bundle bundleTomorrow = new Bundle();
        bundleTomorrow.putInt("day", 2);
        RecyclerViewFragment recyclerViewFragmentTomorrow = new RecyclerViewFragment();
        recyclerViewFragmentTomorrow.setArguments(bundleTomorrow);
        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, getString(R.string.tomorrow));

        Bundle bundle = new Bundle();
        bundle.putInt("day", 3);
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        recyclerViewFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(recyclerViewFragment, getString(R.string.later));


        int currentPage = viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (currentPage == 0 && longTermTodayWeather.isEmpty()) {
            currentPage = 1;
        }
        viewPager.setCurrentItem(currentPage, false);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean shouldUpdate() {
        long lastUpdate = PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1);
        boolean cityChanged = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("cityChanged", false);
        // Update if never checked or last update is longer ago than specified threshold
        return cityChanged || lastUpdate < 0 || (Calendar.getInstance().getTimeInMillis() - lastUpdate) > NO_UPDATE_REQUIRED_THRESHOLD;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            if (isNetworkAvailable()) {
                getCityByLocation();
                return true;
//                getTodayWeather();
//                getLongTermWeather();
            } else {
                Snackbar.make(appView, getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();

            }
            /*if(interstitial.isLoaded() && !isAdView){
                interstitial.show();
                isAdView = true;
            }*/
            return true;
        }
      /*  if (id == R.id.action_map) {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        }*/
        if (id == R.id.action_graphs) {
            Intent intent = new Intent(MainActivity.this, GraphActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_close) {
            finish();
            return true;
        }
        if (id == R.id.action_search) {
            searchCities();
            return true;
        }
        if (id == R.id.action_theme) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            intent.putExtra("test","theme_1");
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_location) {
            getCityByLocation();
            return true;
        }
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            intent.putExtra("test","theme_2");
            startActivity(intent);
            return true;
        }
//        if (id == R.id.action_about) {
//            aboutDialog();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    private String localize(SharedPreferences sp, String preferenceKey, String defaultValueKey) {
        return localize(sp, this, preferenceKey, defaultValueKey);
    }

    void getCityByLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explanation not needed, since user requests this themmself

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }

        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            progressDialog.setMessage(getString(R.string.getting_location));
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        locationManager.removeUpdates(MainActivity.this);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            });
            progressDialog.show();
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        } else {
            showLocationSettingsDialog();
        }
    }

    private void showLocationSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.location_settings);
        alertDialog.setMessage(R.string.location_settings_message);
        alertDialog.setPositiveButton(R.string.location_settings_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    //    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    getCityByLocation();
//                }
//                return;
//            }
//            case MY_PERMISSIONS_SEND_SMS: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    smsNotify();
//                }
//                return;
//            }
//        }
//    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//    Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
//                        Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
//                Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    destroyed = false;
                    initMappings();
                    getCityByLocation();
                    // Preload data from cache

                    updateLastUpdateTime();

                    // Set autoupdater
                    AlarmReceiver.setRecurringAlarm(this);
                    updateTodayWeatherUI();
                    preloadWeather();
//                initMap();
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        progressDialog.dismiss();
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e("LocationManager", "Error while trying to stop listening for location updates. This is probably a permissions issue", e);
        }
        Log.i("LOCATION (" + location.getProvider().toUpperCase() + ")", location.getLatitude() + ", " + location.getLongitude());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        new ProvideCityNameTask(this, this, progressDialog).execute("coords", Double.toString(latitude), Double.toString(longitude));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //SMS
    /*private void smsNotify() {
     *//*if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_SEND_SMS);
            }

        }*//*
        String msgContent = "දවසේ කාලගුණ වර්ථාව SMS මගින් ලබා ගනීමට කමතිද? ";
        String msg = "REG KGAPP";

        List<MessageOperator> messageOperators = new ArrayList<>();
        MessageOperator ideaMartOperator = MsgOperatorFactory.createMessageOperator("77177", com.universl.smsnotifier.Constants.SP_DIALOG1, com.universl.smsnotifier.Constants.SP_DIALOG2, com.universl.smsnotifier.Constants.SP_DIALOG3, com.universl.smsnotifier.Constants.SP_AIRTEL, com.universl.smsnotifier.Constants.SP_HUTCH);//, Constants.SP_ETISALAT
        ideaMartOperator.setSmsMsg(msg);
        ideaMartOperator.setCharge("5 LKR +Tax P/D");
        messageOperators.add(ideaMartOperator);

        Param param = new Param(getResources().getString(R.string.ow), getResources().getString(R.string.natha));
        smsSender = new AppSMSSender(this, messageOperators, param);
        smsSender.smsNotify(msgContent, getResources().getString(R.string.app_name));

    }
*/

    //Ads
    private void initAds() {
        if (!isAdView) {
           /* MobileAds.initialize(this, getResources().getString(R.string.ads_app_id));
            interstitial.setAdUnitId(getResources().getString(R.string.ads_inst_id));
            AdRequest adInsRequest = new AdRequest.Builder().build();
            interstitial.loadAd(adInsRequest);*/
        }

    }

    private void updateLastUpdateTime() {
        updateLastUpdateTime(
                PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
        );
    }

    private void updateLastUpdateTime(long timeInMillis) {
        if (timeInMillis < 0) {
            // No time
            //lastUpdate.setText("");
//            lstUpdte="";
        } else {
//            lstUpdte=formatTimeWithDayIfNotToday(this, timeInMillis);
//            NowFragment.onUpde(lstUpdte);

            //lastUpdate.setText(getString(R.string.last_update, formatTimeWithDayIfNotToday(this, timeInMillis)));
        }
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

    class TodayWeatherTask extends GenericRequestTask {
        public TodayWeatherTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

//        @Override
//        protected void onPreExecute() {
//            loading = 0;
//            super.onPreExecute();
//        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            super.onPostExecute(output);
            // Update widgets
            AbstractWidgetProvider.updateWidgets(MainActivity.this);
            DashClockWeatherExtension.updateDashClock(MainActivity.this);
        }

        @Override
        protected ParseResult parseResponse(String response) {
            return parseTodayJson(response);
        }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        protected void updateMainUI(TaskOutput output) {

            updateTodayWeatherUI();
            updateLastUpdateTime();
        }
    }

    class LongTermWeatherTask extends GenericRequestTask {
        public LongTermWeatherTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected ParseResult parseResponse(String response) {
            return parseLongTermJson(response);
        }

        @Override
        protected String getAPIName() {
            return "forecast";
        }

        @Override
        protected void updateMainUI(TaskOutput output) {
            updateLongTermWeatherUI();
        }
    }

    public class ProvideCityNameTask extends GenericRequestTask {

        public ProvideCityNameTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
            super(context, activity, progressDialog);
        }

        @Override
        protected void onPreExecute() { /*Nothing*/ }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        protected ParseResult parseResponse(String response) {
            Log.i("RESULT", response.toString());
            try {
                JSONObject reader = new JSONObject(response);

                final String code = reader.optString("cod");
                if ("404".equals(code)) {
                    Log.e("Geolocation", "No city found");
                    return ParseResult.CITY_NOT_FOUND;
                }

                String city = reader.getString("name");
                String country = "";
                JSONObject countryObj = reader.optJSONObject("sys");
                if (countryObj != null) {
                    country = ", " + countryObj.getString("country");
                }

                saveLocation(city + country);

            } catch (JSONException e) {
                Log.e("JSONException Data", response);
                e.printStackTrace();
                return ParseResult.JSON_EXCEPTION;
            }

            return ParseResult.OK;
        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            /* Handle possible errors only */
            handleTaskOutput(output);
        }
    }
}
