package com.example.livetv;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import android.widget.ProgressBar;



public class MainActivity extends AppCompatActivity {

    private List<Channel> channelList;
    private ProgressBar progressBar;
    private String token;
    private Button logout_btn;
    private TextView username;
    private JSONArray channelJsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        token = getIntent().getStringExtra("jwt_token");
        // Initialize channelList
        channelList = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);

        logout_btn = findViewById(R.id.logout_btn);
        username = findViewById(R.id.username);

        if (token == null || token.isEmpty()) {
            logout_btn.setText("LOGIN");
        }


        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("accessToken", "");
                editor.apply();

                if(token != null && !token.isEmpty()){
                    Toast.makeText(MainActivity.this, "You have been logged out!", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });


        // Execute AsyncTask to fetch JSON data
        new FetchDataAsyncTask().execute();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class FetchDataAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show progress bar
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(Void... voids) {
            if (!isNetworkAvailable()) {
                return null; // No internet connection
            }
            return endpoint();
        }

        @Override
        protected void onPostExecute(String jsonData) {
            progressBar.setVisibility(View.GONE);
            if (jsonData != null) {
                Log.d("MainActivity", "JSON data received: " + jsonData);
                parseJsonData(jsonData);
                populateCards();
            } else {
                if (!isNetworkAvailable()) {
                    Toast.makeText(MainActivity.this, "Sorry, something went wrong. Please check your internet & try again", Toast.LENGTH_LONG).show();
                    Intent tmp = new Intent(MainActivity.this, ErrorActivity.class);
                    tmp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(tmp);
                }
                else{
                    Log.e("MainActivity", "Failed to fetch JSON data");
                }
            }
        }
    }



    public String endpoint() {
        OkHttpClient client = new OkHttpClient();

        // Replace the URL with your API endpoint
        String url = "https://livetv-njf6.onrender.com/tv";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    return responseBody.string();
                } else {
                    Log.d("MainActivity", "Response body is empty");
                }
            } else {
                Log.d("MainActivity", "Request failed with code: " + response.code());
            }
        } catch (IOException e) {
            Log.e("MainActivity", "IOException occurred: " + e.getMessage());
        }
        return null;
    }

    private void parseJsonData(String jsonData) {
        if (jsonData == null) {
            Log.d("MainActivity", "JSON data is null");
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            JSONObject dataObject = jsonArray.getJSONObject(0); // Assuming there's only one object in the array
            String msg = dataObject.getString("message");
            String user = dataObject.getString("user").toString();
            if(!user.equals("NO USER")){
                JSONObject fulluser = dataObject.getJSONObject("user");
                username.setText("Hello, "+fulluser.getString("fullName"));
            }
            else{
                username.setText("Please login to access all channels");
            }
            if(!msg.equals("success")){
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
            JSONArray videosArray = dataObject.getJSONArray("videos");
            channelJsonArray = videosArray;
            for (int i = 0; i < videosArray.length(); i++) {
                JSONObject videoObject = videosArray.getJSONObject(i);
                String channelName = videoObject.getString("channelName");
                String liveURL = videoObject.getString("url");
                String thumbnail = videoObject.optString("channelLogo", ""); // Use optString to handle optional fields
                Channel channel = new Channel(i, channelName, liveURL, thumbnail);
                channelList.add(channel);
            }
        } catch (JSONException e) {
            Log.e("MainActivity", "Error parsing JSON: " + e.getMessage());
        }
    }

    private void populateCards() {
        ViewGroup container = findViewById(R.id.container);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            View firstCardView = null;
            boolean firstViewTaken = false;
            for (Channel channel : channelList) {
                View cardView = inflater.inflate(R.layout.card_layout, container, false);

                TextView nameTextView = cardView.findViewById(R.id.nameTextView);
                ImageView thumbnailImageView = cardView.findViewById(R.id.thumbnailImageView);

                nameTextView.setText(channel.getChannelName());

                // Check if thumbnail URL is not empty before loading with Picasso
                if (!TextUtils.isEmpty(channel.getThumbnail())) {
                    Picasso.get().load(channel.getThumbnail()).into(thumbnailImageView);
                }

                container.addView(cardView);
                // Set focus change listener for each card
                cardView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus) {
                            // Set focused state background
                            cardView.setBackgroundResource(R.drawable.card_background_focused);
                        } else {
                            // Set unfocused state background
                            cardView.setBackgroundResource(R.drawable.card_background_unfocused);
                        }
                    }
                });

                // Set click listener for each card
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                        intent.putExtra("channelJsonArray", channelJsonArray.toString());
                        intent.putExtra("channelIndex", channel.getIndex());
                        startActivity(intent);
                    }
                });

                // Ensure card view is clickable
                cardView.setClickable(true);

                if(!firstViewTaken){
                    firstCardView = cardView;
                    firstViewTaken = true;
                }
            }
            if (firstCardView != null) {
                firstCardView.requestFocus();
            }
        }
    }


}

class Channel {
    private String channelName;
    private String liveURL;
    private String thumbnail;
    private int idx;

    public Channel(int idx, String channelName, String liveURL, String thumbnail) {
        this.channelName = channelName;
        this.liveURL = liveURL;
        this.thumbnail = thumbnail;
        this.idx = idx;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getLiveURL() {
        return liveURL;
    }

    public String getThumbnail() {
        return thumbnail;
    }
    public int getIndex(){return idx;}
}