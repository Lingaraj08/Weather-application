package com.example.myweatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.*;
import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText cityInput;
    private Button searchButton;
    private ImageButton voiceButton;
    private LottieAnimationView weatherAnimation;
    private TextView temperatureText, cityNameText, conditionText;

    private static final String API_KEY = "4bad8cca7e6892d08a06c411e42992d2";
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private static final int REQUEST_RECORD_AUDIO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.cityInput);
        searchButton = findViewById(R.id.searchButton);
        voiceButton = findViewById(R.id.voiceButton);
        weatherAnimation = findViewById(R.id.weatherAnimation);
        temperatureText = findViewById(R.id.temperatureText);
        cityNameText = findViewById(R.id.cityNameText);
        conditionText = findViewById(R.id.conditionText);

        searchButton.setOnClickListener(v -> fetchWeather());
        voiceButton.setOnClickListener(v -> startVoiceInput());
    }

    /** 🎤 Start voice recognition for city name */
    private void startVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the city name...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech input not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    /** 🎧 Handle voice input result */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenCity = result.get(0);
                cityInput.setText(spokenCity);
                fetchWeather();
            }
        }
    }

    /** 🌤️ Fetch weather data using Retrofit */
    private void fetchWeather() {
        String city = cityInput.getText().toString().trim();
        if (city.isEmpty()) {
            showError("⚠️ Please enter a city name.");
            return;
        }

        showLoadingAnimation("Fetching weather for " + city + "...");

        WeatherService service = RetrofitClient.getClient().create(WeatherService.class);
        Call<WeatherData> call = service.getWeather(city, API_KEY, "metric");

        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    showError("❌ City not found. Try again!");
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                showError("🚫 Error: " + t.getMessage());
            }
        });
    }

    /** 🧊 Update UI with weather data */
    private void updateUI(WeatherData data) {
        String condition = data.weather.get(0).main.toLowerCase();
        setWeatherAnimation(condition);

        temperatureText.setText(String.format("%.1f°C", data.main.temp));
        cityNameText.setText(data.name);
        conditionText.setText("Condition: " + data.weather.get(0).description);

        Toast.makeText(this, "✅ Weather updated!", Toast.LENGTH_SHORT).show();
    }

    /** 🌈 Select weather animation based on condition */
    private void setWeatherAnimation(String condition) {
        int animationRes;
        if (condition.contains("rain")) animationRes = R.raw.rain;
        else if (condition.contains("clear")) animationRes = R.raw.sunny_weather;
        else if (condition.contains("cloud")) animationRes = R.raw.cloudy;
        else if (condition.contains("snow")) animationRes = R.raw.snow;
        else if (condition.contains("thunder")) animationRes = R.raw.thunder;
        else animationRes = R.raw.weather_default;

        weatherAnimation.setAnimation(animationRes);
        weatherAnimation.playAnimation();
    }

    /** ⚠️ Show error animation and message */
    private void showError(String message) {
        temperatureText.setText("--°C");
        cityNameText.setText("Error");
        conditionText.setText(message);
        weatherAnimation.setAnimation(R.raw.error);
        weatherAnimation.playAnimation();
    }

    /** 🔄 Show loading animation with temporary message */
    private void showLoadingAnimation(String message) {
        temperatureText.setText("--°C");
        cityNameText.setText("Loading...");
        conditionText.setText(message);
        weatherAnimation.setAnimation(R.raw.loading);
        weatherAnimation.playAnimation();
    }
}
