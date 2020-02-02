package com.universl.kalagune.germanweather.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.LinearLayout;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.universl.kalagune.germanweather.R;
import com.universl.kalagune.germanweather.models.Weather;
import com.universl.kalagune.germanweather.tasks.ParseResult;
import com.universl.kalagune.germanweather.utils.UnitConvertor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class GraphActivity extends AppCompatActivity {

    SharedPreferences sp;
    LinearLayout appView;

    int theme;

    ArrayList<Weather> weatherList = new ArrayList<>();

    float minTemp = 100000;
    float maxTemp = 0;

    float minRain = 100000;
    float maxRain = 0;

    float minPressure = 100000;
    float maxPressure = 0;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(theme = getTheme(prefs.getString("theme", "fresh")));
        boolean darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Toolbar toolbar = (Toolbar) findViewById(R.id.graph_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (darkTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
        }

        sp = PreferenceManager.getDefaultSharedPreferences(GraphActivity.this);
        String lastLongterm = sp.getString("lastLongterm", "");
        appView=findViewById(R.id.graphLinearLayout);

        if (parseLongTermJson(lastLongterm) == ParseResult.OK) {
            temperatureGraph();
            rainGraph();
            pressureGraph();
        } else {
            Snackbar.make(findViewById(android.R.id.content), R.string.msg_err_parsing_json, Snackbar.LENGTH_LONG).show();
        }
        setTheme();
    }

    private void temperatureGraph() {
        LineChartView lineChartView = (LineChartView) findViewById(R.id.graph_temperature);

        // Data
        LineSet dataset = new LineSet();
        for (int i = 0; i < weatherList.size(); i++) {
            float temperature = UnitConvertor.convertTemperature(Float.parseFloat(weatherList.get(i).getTemperature()), sp);

            if (temperature < minTemp) {
                minTemp = temperature;
            }

            if (temperature > maxTemp) {
                maxTemp = temperature;
            }

            dataset.addPoint(getDateLabel(weatherList.get(i), i), (float) temperature);
        }
        dataset.setSmooth(false);
        dataset.setColor(Color.parseColor("#FF5722"));
        dataset.setThickness(4);

        lineChartView.addData(dataset);

        // Grid
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#333333"));
        paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        paint.setStrokeWidth(1);
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, paint);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.setAxisBorderValues((int) (Math.round(minTemp)) - 1, (int) (Math.round(maxTemp)) + 1);
        lineChartView.setStep(2);
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);

        lineChartView.show();
    }

    private void rainGraph() {
        LineChartView lineChartView = (LineChartView) findViewById(R.id.graph_rain);

        // Data
        LineSet dataset = new LineSet();
        for (int i = 0; i < weatherList.size(); i++) {
            float rain = Float.parseFloat(weatherList.get(i).getRain());

            if (rain < minRain) {
                minRain = rain;
            }

            if (rain > maxRain) {
                maxRain = rain;
            }

            dataset.addPoint(getDateLabel(weatherList.get(i), i), rain);
        }
        dataset.setSmooth(false);
        dataset.setColor(Color.parseColor("#2196F3"));
        dataset.setThickness(4);

        lineChartView.addData(dataset);

        // Grid
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#333333"));
        paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        paint.setStrokeWidth(1);
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, paint);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.setAxisBorderValues(0, (int) (Math.round(maxRain)) + 1);
        lineChartView.setStep(1);
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);

        lineChartView.show();
    }

    private void pressureGraph() {
        LineChartView lineChartView = (LineChartView) findViewById(R.id.graph_pressure);

        // Data
        LineSet dataset = new LineSet();
        for (int i = 0; i < weatherList.size(); i++) {
            float pressure = UnitConvertor.convertPressure(Float.parseFloat(weatherList.get(i).getPressure()), sp);

            if (pressure < minPressure) {
                minPressure = pressure;
            }

            if (pressure > maxPressure) {
                maxPressure = pressure;
            }

            dataset.addPoint(getDateLabel(weatherList.get(i), i), pressure);
        }
        dataset.setSmooth(true);
        dataset.setColor(Color.parseColor("#4CAF50"));
        dataset.setThickness(4);

        lineChartView.addData(dataset);

        // Grid
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#333333"));
        paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        paint.setStrokeWidth(1);
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, paint);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.setAxisBorderValues((int) minPressure - 1, (int) maxPressure + 1);
        lineChartView.setStep(2);
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);

        lineChartView.show();
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
        } else {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                appView.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.fresh));
            } else {
                appView.setBackground(ContextCompat.getDrawable(this, R.drawable.fresh));
            }
        }
    }

    public ParseResult parseLongTermJson(String result) {
        int i;

        try {
            JSONObject reader = new JSONObject(result);
            System.out.println("GrapgActivty Result :" +result.toString());
            System.out.println("GrapgActivty Reader Result :" +reader.toString());

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return ParseResult.CITY_NOT_FOUND;
            }

            JSONArray list = reader.getJSONArray("list");
            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");

                JSONObject windObj = listItem.optJSONObject("wind");
                weather.setWind(windObj.getString("speed"));

                weather.setPressure(main.getString("pressure"));
                weather.setHumidity(main.getString("humidity"));

                JSONObject rainObj = listItem.optJSONObject("rain");
                JSONObject snowObj = listItem.optJSONObject("snow");
                if (rainObj != null) {
                    System.out.println("NOOOOOOT"+"Nul");
                    weather.setRain(MainActivity.getRainString(rainObj));
                } else {
                    System.out.println("NULLLLLLLL"+"Nul");
                    weather.setRain(MainActivity.getRainString(snowObj));
                }

                weather.setDate(listItem.getString("dt"));
                weather.setTemperature(main.getString("temp"));

                weatherList.add(weather);
            }
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    String previous = "";

    public String getDateLabel(Weather weather, int i) {
        if ((i + 4) % 4 == 0) {
            SimpleDateFormat resultFormat = new SimpleDateFormat("E");
            resultFormat.setTimeZone(TimeZone.getDefault());
            String output = resultFormat.format(weather.getDate());
            if (!output.equals(previous)) {
                previous = output;
                return output;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

//    private int getTheme(String themePref) {
//        switch (themePref) {
//            case "dark":
//                return R.style.AppTheme_NoActionBar_Dark;
//            case "black":
//                return R.style.AppTheme_NoActionBar_Black;
//            case "classic":
//                return R.style.AppTheme_NoActionBar_Classic;
//            case "classicdark":
//                return R.style.AppTheme_NoActionBar_Classic_Dark;
//            case "classicblack":
//                return R.style.AppTheme_NoActionBar_Classic_Black;
//            default:
//                return R.style.AppTheme_NoActionBar;
//        }
//    }

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
