package com.example.myweatherapp;

import java.util.List;

public class WeatherData {
    public String name;
    public Main main;
    public List<Weather> weather;
    public Wind wind;

    public static class Main {
        public double temp;
        public int humidity;
    }

    public static class Weather {
        public String main;
        public String description;
    }

    public static class Wind {
        public double speed;
    }
}
