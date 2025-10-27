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
    TextView resultText;
    Button searchButton;

    private final String API_KEY = "4bad8cca7e6892d08a06c411e42992d2"; // Get it from openweathermap.org

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.cityInput);
        resultText = findViewById(R.id.resultText);
        searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> fetchWeather());
    }

    private void fetchWeather() {
        String city = cityInput.getText().toString().trim();
        if (city.isEmpty()) {
            resultText.setText("Please enter a city name.");
            return;
        }

        WeatherService service = RetrofitClient.getClient().create(WeatherService.class);
        Call<WeatherData> call = service.getWeather(city, API_KEY, "metric");

        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherData data = response.body();
                    String info = "City: " + data.name + "\n" +
                            "Temp: " + data.main.temp + "°C\n" +
                            "Condition: " + data.weather.get(0).main + "\n" +
                            "Humidity: " + data.main.humidity + "%\n" +
                            "Wind: " + data.wind.speed + " m/s";
                    resultText.setText(info);
                } else {
                    resultText.setText("City not found.");
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                resultText.setText("Error: " + t.getMessage());
            }
        });
    }
}
