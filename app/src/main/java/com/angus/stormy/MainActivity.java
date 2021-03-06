package com.angus.stormy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;

import com.angus.stormy.R;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;

    @BindView(R.id.base) RelativeLayout mLayout;
    @BindView(R.id.locationLabel) TextView mLocationLabel;
    @BindView(R.id.countryLabel) TextView mCountryLabel;
    @BindView(R.id.timeLabel) TextView mTimeLabel;
    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @BindView(R.id.humidityValue) TextView mHumidityValue;
    @BindView(R.id.precipValue) TextView mPrecipValue;
    @BindView(R.id.summaryLabel) TextView mSummaryLabel;
    @BindView(R.id.iconImageView) ImageView mIconImageView;
    @BindView(R.id.refreshImageView) ImageView mRefreshImageView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        final double latitude = 37.8267;
        final double longitude = -122.4233;

        getForecast(latitude, longitude);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getForecast(latitude, longitude);
            }
        });
    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = "0ebb9731ed62a65ff62287e2dd8eddfd";

        String forecastURL = "https://api.darksky.net/forecast/" + apiKey + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {

            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, response.body().toString());
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e){
                        Log.e(TAG, "JSON Exception caught: ", e);
                    }
                }
            });

            Log.d(TAG, "Main UI Thread here!");
        } else {
            Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }

    }

    private void updateDisplay() {
        mLocationLabel.setText(mCurrentWeather.getCity());
        mCountryLabel.setText(mCurrentWeather.getCountry());
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());

        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);

        setWeatherTheme(mCurrentWeather.getIcon());

    }

    private void setWeatherTheme(String icon) {
        Log.d(TAG, icon);

        if (icon.equals("clear-day"))
            setColorScheme(R.color.colorPrimaryClearDay);
        else if (icon.equals("clear-night"))
            setColorScheme(getResources().getColor(R.color.colorPrimaryClearNight));
        else if (icon.equals("partly-cloudy-day"))
            setColorScheme(getResources().getColor(R.color.colorPrimaryPartlyCloudyDay));
        else if (icon.equals("partly-cloudy-night"))
            setColorScheme(getResources().getColor(R.color.colorPrimaryPartlyCloudyNight));
        else if (icon.equals("rain"))
            setColorScheme(getResources().getColor(R.color.colorPrimaryRain));
        else if (icon.equals("snow"))
            setColorScheme(getResources().getColor(R.color.colorPrimarySnow));
        else if (icon.equals("snow"))
            setColorScheme(getResources().getColor(R.color.colorPrimarySleet));
        else if (icon.equals("wind"))
            setColorScheme(getResources().getColor(R.color.colorPrimaryWind));
        else if (icon.equals("fog"))
            setColorScheme(getResources().getColor(R.color.colorPrimaryFog));
        else if (icon.equals("cloudy"))
            setColorScheme(getResources().getColor(R.color.colorPrimaryCloudy));
        else
            setColorScheme(R.color.colorPrimaryWind);

    }

    private void setColorScheme(int color) {
        ColorWheel colorWheel = new ColorWheel();
        int mColor = colorWheel.makeDarkenColor(color);
        mLayout.setBackgroundColor(color);

        // change status bar color
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(mColor);
        }
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);
        currentWeather.setCity(timezone);
        currentWeather.setCountry(timezone);

        Log.d(TAG, currentWeather.getFormattedTime());

        return currentWeather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }
}
