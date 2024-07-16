package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xml.sax.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    EditText zipCode, countryCode;
    TextView LongitudeDisplay, LatitudeDisplay, ErrorDisplay, Hour1, Hour2, Hour3, Hour4, city, TempCur;
    Button getWeather;
    String url = "https://api.openweathermap.org/data/2.5/weather?";
    String apiKey = "a59ccb2c176a160a33e89f806d13cb7d";
    String error1 = "Must provide ZIP code";
    String error2 = "Must provide country code";
    JSONObject weatherJSON, secondCall;
    Double longitude, latitude, temp1, temp2, temp3, temp4;
    String currentDesc, hour1Desc, hour2Desc, hour3Desc, hour4Desc, currentTime, hour1, hour2, hour3, hour4;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DecimalFormat format = new DecimalFormat("#.##");
        zipCode = findViewById(R.id.editTextNumber);
        countryCode = findViewById(R.id.editTextTextPersonName);
        LongitudeDisplay = findViewById(R.id.textView3);
        LatitudeDisplay = findViewById(R.id.textView4);
        getWeather = findViewById(R.id.button);
        getWeather.setBackgroundColor(Color.BLACK);

        Hour1 = findViewById(R.id.FirstHour);
        Hour2 = findViewById(R.id.SecondHour);
        Hour3 = findViewById(R.id.ThirdHour);
        Hour4 = findViewById(R.id.FourthHour);
        TempCur = findViewById(R.id.CurrentTemp);
        imageView = findViewById(R.id.imageView);
        city = findViewById(R.id.textView16);




    }

    private class JSONClass extends AsyncTask<URL, Void, Void>{
        @Override
        protected Void doInBackground(URL... urls){
            String apiCall = "";
            String zip = zipCode.getText().toString().trim();
            String cCode = countryCode.getText().toString().trim();
            Log.d("tag_info", cCode);
            if(!zip.equals("") && !cCode.equals(""))
            {
                apiCall = url + "zip=" + zip + "," + cCode + "&appid=" + apiKey;
                String jsonTxt = "";
                StringBuilder stringBuilder = new StringBuilder();
                URL weather = null;
                try {
                    weather = new URL(apiCall);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                Log.d("Tag_URL", weather.toString());
                URLConnection conn = null;
                try {
                    conn = weather.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while((jsonTxt = reader.readLine()) != null)
                    {
                        stringBuilder.append(jsonTxt);
                    }
                    weatherJSON = new JSONObject(stringBuilder.toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                //Log.d("TAG_JSON made", weatherJSON.toString());
            }
            else
            {
               Toast.makeText(getApplicationContext(), "Must provide valid inputs!", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Log.d("TAG_INFO", "post execute reached");
            try {
                JSONObject coordinates = new JSONObject(weatherJSON.getJSONObject("coord").toString());
                latitude = coordinates.getDouble("lat");
                longitude = coordinates.getDouble("lon");
                String cityName = weatherJSON.getString("name").toString();
                Log.d("TAG_INFOD", longitude.toString());
                LatitudeDisplay.setText("Latitude\n"+latitude.toString());
                LongitudeDisplay.setText("Longitude\n"+longitude.toString());
                city.setText(cityName);


                new JSONClass2().execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    };

    private class JSONClass2 extends AsyncTask<URL, Void, Void>{
        @Override
        protected Void doInBackground(URL... urls) {
            String apiCall2 = "https://api.openweathermap.org/data/2.5/onecall?lat="+latitude+"&lon="+longitude+"&exclude=minutely,daily&appid=a59ccb2c176a160a33e89f806d13cb7d";
            String jsonTxt = "";
            StringBuilder stringBuilder = new StringBuilder();
            URL weather = null;
            try {
                weather = new URL(apiCall2);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Log.d("Tag_URLCall2", weather.toString());
            URLConnection conn = null;
            try {
                conn = weather.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while((jsonTxt = reader.readLine()) != null)
                {
                    stringBuilder.append(jsonTxt);
                }
                secondCall = new JSONObject(stringBuilder.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            Log.d("TAG_JSON 2 made", secondCall.toString());
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            try {
                //
                JSONObject current = new JSONObject(secondCall.getJSONObject("current").toString());
                JSONObject currentWeather = new JSONObject(current.getJSONArray("weather").get(0).toString());
                Log.d("TAG_JSONARRAY", currentWeather.toString());

                //getting info for current time
                currentTime = timeFormatting(current.getInt("dt"));
                currentDesc = currentWeather.getString("description");
                Double currentTemp = kelvinToFarenheit(current.getDouble("temp"));
                TempCur.setText(weatherFormat(currentTemp, currentDesc, currentTime));
                checkWeather(currentDesc);

                //getting info for hour1
                JSONObject hourOne = new JSONObject(secondCall.getJSONArray("hourly").get(0).toString());
                JSONObject hourOneWeather = new JSONObject(hourOne.getJSONArray("weather").get(0).toString());

                hour1 = timeFormatting(hourOne.getInt("dt"));
                temp1 = kelvinToFarenheit(hourOne.getDouble("temp"));
                hour1Desc = hourOneWeather.getString("description");
                Hour1.setText(weatherFormatf(temp1, hour1Desc, hour1));

                Log.d("TAG_hour1time", String.valueOf(hour1));
                //getting info for hour2
                JSONObject hourTwo = new JSONObject(secondCall.getJSONArray("hourly").get(1).toString());
                JSONObject hourTwoWeather = new JSONObject(hourTwo.getJSONArray("weather").get(0).toString());

                hour2 = timeFormatting(hourTwo.getInt("dt"));
                temp2 = kelvinToFarenheit(hourTwo.getDouble("temp"));
                hour2Desc = hourTwoWeather.getString("description");
                Hour2.setText(weatherFormatf(temp2, hour2Desc, hour2));

                //getting info for hour3
                JSONObject hourThree = new JSONObject(secondCall.getJSONArray("hourly").get(2).toString());
                JSONObject hourThreeWeather = new JSONObject(hourThree.getJSONArray("weather").get(0).toString());

                hour3 = timeFormatting(hourThree.getInt("dt"));
                temp3 = kelvinToFarenheit(hourThree.getDouble("temp"));
                hour3Desc = hourThreeWeather.getString("description");
                Hour3.setText(weatherFormatf(temp3, hour3Desc, hour3));

                //getting info for hour4
                JSONObject hourFour = new JSONObject(secondCall.getJSONArray("hourly").get(3).toString());
                JSONObject hourFourWeather = new JSONObject(hourFour.getJSONArray("weather").get(0).toString());

                hour4 = timeFormatting(hourFour.getInt("dt"));
                temp4 = kelvinToFarenheit(hourFour.getDouble("temp"));
                hour4Desc = hourFourWeather.getString("description");
                Hour4.setText(weatherFormatf(temp4, hour4Desc, hour4));




            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    public void run (View view){
        new JSONClass().execute();
    }

    public double kelvinToFarenheit(double d)
    {
        double result = d - 273.15;
        result = result * 1.8;
        result = result + 32;
        return Math.round(result * 100.0) / 100.0;
    }

    public String weatherFormat(double temperature, String description, String time)
    {
        return time+"\nThe temperature is "+temperature+"\u2109\nThe weather is "+description;
    }

    public void checkWeather(String description)
    {
        if(description.contains("rain"))
        {
            imageView.setImageResource(R.drawable.rainpng);
        }
        if(description.contains("cloud"))
        {
            imageView.setImageResource(R.drawable.cloud);
        }
        if(description.contains("sun"))
        {
            imageView.setImageResource(R.drawable.sun);
        }
    }

    public String weatherFormatf(double temperature, String description, String time)
    {
        return time+"\n"+temperature+"\u2109\n"+description;
    }

    public String timeFormatting(int unixTime)
    {
        Date t = new Date(unixTime* 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE KK:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("EST"));
        return sdf.format(t).toString();


    }



}