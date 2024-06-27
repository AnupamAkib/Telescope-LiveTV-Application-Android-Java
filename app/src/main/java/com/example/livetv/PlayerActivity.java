package com.example.livetv;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayerActivity extends AppCompatActivity {


    private WebView webView;
    private int channelIndex;
    private JSONArray channelJsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);

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

            //webView.setWebViewClient(new WebViewClient());
            //webView.setWebChromeClient(new WebChromeClient());

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
        String videoHtml = "<html><style>body {margin: 0; padding: 0;}</style><body><iframe width=\"100%\" height=\"100%\" src=\"" + channelURL + "?autoplay=1&mute=0\" frameborder=\"0\" allow=\"autoplay; encrypted-media\" allowfullscreen></iframe></body></html>";
        webView.loadData(videoHtml, "text/html", "utf-8");
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
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
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("key up code", String.valueOf(keyCode));

        if(keyCode == KeyEvent.KEYCODE_CHANNEL_UP){
            Log.d("TAG", "Channel Up button pressed");

            playNextChannel();

            return true;
        }
        if(keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN){
            Log.d("TAG", "Channel Down button pressed");

            playPreviousChannel();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void playNextChannel(){
        if(channelIndex + 1 >= channelJsonArray.length()){
            Toast.makeText(PlayerActivity.this, "No next channel found!", Toast.LENGTH_SHORT).show();
        }
        else{
            channelIndex++;
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


    //gesture functionality

    
}