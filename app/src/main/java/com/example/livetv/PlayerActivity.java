package com.example.livetv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PlayerActivity extends AppCompatActivity {
    private static final int MIN_DISTANCE_DP = 150; // Minimum swipe distance in dp
    private float MIN_DISTANCE; // Minimum swipe distance in pixels
    private float initialTouchX;

    private FrameLayout overlayLayout;

    //---------------------------------

    private WebView webView;
    private int channelIndex;
    private JSONArray channelJsonArray;
    private boolean isLoadedScreenHided = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);

        if(!NetworkUtils.isInternetConnected(this)){
            Intent tmp = new Intent(PlayerActivity.this, ErrorActivity.class);
            tmp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(tmp);
            return;
        }

        MIN_DISTANCE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                MIN_DISTANCE_DP, getResources().getDisplayMetrics());

        String receivedChannels = getIntent().getStringExtra("channelJsonArray");
        channelIndex = getIntent().getIntExtra("channelIndex", -1);

        try {
            channelJsonArray = new JSONArray(receivedChannels);
            JSONObject videoObject = channelJsonArray.getJSONObject(channelIndex);
            String channelURL = videoObject.getString("url");

            //Log.d("player", channelJsonArray.toString());
            //Log.d("player", "channel idx = "+channelIndex);

            webView = findViewById(R.id.webview);

            // Enable JavaScript and other necessary settings
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            webSettings.setMediaPlaybackRequiresUserGesture(false); // Important for autoplay


            overlayLayout = findViewById(R.id.overlayLayout);
            hideOverlay();

            LoadChannel(channelURL);

        } catch (JSONException e) {
            Log.d("exception", e.getMessage());
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void LoadChannel(String channelURL){
        // Load the URL with an HTML wrapper
        String videoHtml = "<html>" +
                "<head><style>body {margin: 0; padding: 0;}</style><script src=\"https://www.youtube.com/iframe_api\"></script></head>" +
                "<body><iframe width=\"100%\" height=\"100%\" src=\"" + channelURL + "?autoplay=1&mute=0\" frameborder=\"0\" allow=\"autoplay; encrypted-media\" allowfullscreen></iframe>" +
                "</body></html>";
        webView.loadDataWithBaseURL("https://www.youtube.com", videoHtml, "text/html", "utf-8", null);
    }

    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) { //for physical keybaord
        int keyCode = event.getKeyCode();
        if(isLoadedScreenHided){
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_Q) {
                    Log.d("TAG", "A key pressed");

                    playNextChannel();

                    return true; // Consume the event
                } else if (keyCode == KeyEvent.KEYCODE_A) {
                    Log.d("TAG", "A key pressed");

                    playPreviousChannel();

                    return true; // Consume the event
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }*/


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) { //for TV remote
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        //Toast.makeText(this, "clicked keycode = "+keyCode, Toast.LENGTH_SHORT).show();
        if(isLoadedScreenHided) {
            if (action == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_CHANNEL_UP:
                        //Log.d("TAG", "Channel Up button pressed");
                        //Toast.makeText(this, "Channel Up button pressed", Toast.LENGTH_SHORT).show();
                        playNextChannel();
                        return true;

                    case KeyEvent.KEYCODE_CHANNEL_DOWN:
                        //Log.d("TAG", "Channel Down button pressed");
                        //Toast.makeText(this, "Channel Down button pressed", Toast.LENGTH_SHORT).show();
                        playPreviousChannel();
                        return true;

                    default:
                        return super.dispatchKeyEvent(event);
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }



    private void playNextChannel(){
        if(channelIndex + 1 >= channelJsonArray.length()){
            Toast.makeText(PlayerActivity.this, "No next channel found!", Toast.LENGTH_SHORT).show();
        }
        else{
            channelIndex++;
            showOverlay();
            JSONObject videoObject = null;
            try {
                videoObject = channelJsonArray.getJSONObject(channelIndex);
                String channelURL = videoObject.getString("url");
                LoadChannel(channelURL);
            } catch (JSONException e) {
                Log.d("exception", e.getMessage());
            }
        }
    }

    private void playPreviousChannel(){
        if(channelIndex - 1 < 0){
            Toast.makeText(PlayerActivity.this, "No previous channel found!", Toast.LENGTH_SHORT).show();
        }
        else{
            channelIndex--;
            showOverlay();
            JSONObject videoObject = null;
            try {
                videoObject = channelJsonArray.getJSONObject(channelIndex);
                String channelURL = videoObject.getString("url");
                LoadChannel(channelURL);
            } catch (JSONException e) {
                Log.d("exception", e.getMessage());
            }
        }
    }

    private void showOverlay() {
        try {
            JSONObject videoObject = channelJsonArray.getJSONObject(channelIndex);
            String channelLogoURL = videoObject.getString("channelLogo");
            Log.d("logo orl", channelLogoURL);
            ImageView channelLogo = findViewById(R.id.channelLogo);

            TextView channelToLoad = findViewById(R.id.channelToLoad);

            channelToLoad.setText(videoObject.getString("channelName"));

            isLoadedScreenHided = false;
            overlayLayout.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            Log.d("exception", e.getMessage());
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Call your function here
                hideOverlay();
            }
        }, 5000); // 2000 milliseconds = 2 seconds
    }

    private void hideOverlay() {
        // Remove the overlay layout
        //overlayLayout.removeAllViews();
        //overlayLayout.setBackgroundColor(Color.parseColor("#00000000"));
        isLoadedScreenHided = true;
        overlayLayout.setVisibility(View.GONE);
    }
}

