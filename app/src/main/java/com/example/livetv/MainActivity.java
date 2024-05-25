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
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.widget.ProgressBar;



public class MainActivity extends AppCompatActivity {

    private List<Channel> channelList;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize channelList
        channelList = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);

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
                    Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
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

            JSONArray videosArray = dataObject.getJSONArray("videos");
            for (int i = 0; i < videosArray.length(); i++) {
                JSONObject videoObject = videosArray.getJSONObject(i);
                String channelName = videoObject.getString("channelName");
                String liveURL = videoObject.getString("url");
                String thumbnail = videoObject.optString("channelLogo", ""); // Use optString to handle optional fields
                Channel channel = new Channel(channelName, liveURL, thumbnail);
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
                        intent.putExtra("liveURL", channel.getLiveURL());
                        startActivity(intent);
                    }
                });

                // Ensure card view is clickable
                cardView.setClickable(true);
            }
        }
    }


}

class Channel {
    private String channelName;
    private String liveURL;
    private String thumbnail;

    public Channel(String channelName, String liveURL, String thumbnail) {
        this.channelName = channelName;
        this.liveURL = liveURL;
        this.thumbnail = thumbnail;
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
}