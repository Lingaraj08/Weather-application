package com.example.myweatherapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    EditText cityInput;
    TextView resultText, weatherEmoji;
    Button searchButton;

    private final String API_KEY = "4bad8cca7e6892d08a06c411e42992d2"; // OpenWeatherMap API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.cityInput);
        resultText = findViewById(R.id.resultText);
        searchButton = findViewById(R.id.searchButton);
        weatherEmoji = findViewById(R.id.weatherEmoji);

        searchButton.setOnClickListener(v -> fetchWeather());
    }

    private void fetchWeather() {
        String city = cityInput.getText().toString().trim();
        if (city.isEmpty()) {
            resultText.setText("⚠️ Please enter a city name.");
            weatherEmoji.setText("❓");
            return;
        }

        resultText.setText("Fetching weather data... 🌍");
        weatherEmoji.setText("⏳");

        WeatherService service = RetrofitClient.getClient().create(WeatherService.class);
        Call<WeatherData> call = service.getWeather(city, API_KEY, "metric");

        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherData data = response.body();
                    updateUI(data);
                } else {
                    resultText.setText("❌ City not found. Try again!");
                    weatherEmoji.setText("😕");
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                resultText.setText("🚫 Error: " + t.getMessage());
                weatherEmoji.setText("💀");
            }
        });
    }

    private void updateUI(WeatherData data) {
        String condition = data.weather.get(0).main.toLowerCase();
        String emoji = getWeatherEmoji(condition);

        String info = "📍 City: " + data.name + "\n" +
                "🌡️ Temp: " + data.main.temp + "°C\n" +
                "🌦️ Condition: " + data.weather.get(0).main + "\n" +
                "💧 Humidity: " + data.main.humidity + "%\n" +
                "🌬️ Wind: " + data.wind.speed + " m/s";

        resultText.setText(info);
        weatherEmoji.setText(emoji);
    }

    private String getWeatherEmoji(String condition) {
        if (condition.contains("rain")) return "🌧️";
        if (condition.contains("cloud")) return "☁️";
        if (condition.contains("clear")) return "☀️";
        if (condition.contains("snow")) return "❄️";
        if (condition.contains("thunder")) return "⚡";
        if (condition.contains("mist") || condition.contains("fog")) return "🌫️";
        return "🌈";
    }
}
