package com.example.livetv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


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

    private List<WebView> webViewList = new ArrayList<>();
    private int currentChannelIndex = 0;

    private final int CHANNEL_BETWEEN_TIME = 600;

    private boolean [] flag;

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

        flag = new boolean[200];
        Arrays.fill(flag, false);

        MIN_DISTANCE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                MIN_DISTANCE_DP, getResources().getDisplayMetrics());

        String receivedChannels = getIntent().getStringExtra("channelJsonArray");
        channelIndex = getIntent().getIntExtra("channelIndex", -1);
        currentChannelIndex = channelIndex;

        try {
            channelJsonArray = new JSONArray(receivedChannels);

            preloadChannels();

            overlayLayout = findViewById(R.id.overlayLayout);

            LoadChannel();
            hideOverlay();

        } catch (Exception e) {
            Log.d("exception", e.getMessage());
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void preloadChannels() {
        for (int i = 0; i < channelJsonArray.length(); i++) {
            try {
                final int index = i;  // Create a final copy of 'i'

                JSONObject videoObject = channelJsonArray.getJSONObject(index);  // Use 'index' instead of 'i'
                String channelURL = videoObject.getString("url");

                // Create a new WebView and preload the video
                WebView webView = new WebView(this);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);

                webView.setWebViewClient(new WebViewClient());

                // Unique ID for the iframe and JavaScript functions
                String iframeId = "player" + index;  // Use 'index' instead of 'i'

                int isMute = 1;

                // Ensure that JavaScript variables and functions are unique for each WebView
                String videoHtml = "<html>" +
                        "<head><style>body {margin: 0; padding: 0;}</style></head>" +
                        "<body>" +
                        "<iframe id=\"" + iframeId + "\" width=\"100%\" height=\"90%\" src=\"" + channelURL + "?enablejsapi=1&autoplay=1&mute="+isMute+"\" frameborder=\"0\" allow=\"autoplay; encrypted-media\" allowfullscreen></iframe>" +
                        "<button onclick=\"unmuteVideo" + index + "()\">Unmute" + index + "</button>" +
                        "<button onclick=\"muteVideo" + index + "()\">Mute" + index + "</button>" +
                        "<button onclick=\"playVdo" + index + "()\">Play" + index + "</button>" +
                        "<script src=\"https://www.youtube.com/iframe_api\"></script>" +
                        "<script>" +
                        "  var player" + index + ";" + // Unique player variable per WebView
                        "  function initPlayer() {" +
                        "    player" + index + " = new YT.Player('" + iframeId + "', {" +
                        "      events: {" +
                        "        'onReady': onPlayerReady" + index + "," +
                        "        'onStateChange': onPlayerStateChange" + index +
                        "      }" +
                        "    });" +
                        "  }" +
                        "  function onPlayerReady" + index + "(event) {" +
                        "    event.target.playVideo();" +
                        "  }" +
                        "  function onPlayerStateChange" + index + "(event) {" +
                        "    if (event.data == YT.PlayerState.PLAYING) {" +
                        "      console.log('Video " + index + " is playing.');" +
                        "    }" +
                        "  }" +
                        "  function muteVideo" + index + "() {" +
                        "    if (player" + index + ") {" +
                        "      player" + index + ".mute();" +
                        "      player" + index + ".playVideo();" +
                        "    } else {" +
                        "      console.log('Player " + index + " is not ready.');" +
                        "    }" +
                        "  }" +
                        "  function unmuteVideo" + index + "() {" +
                        "    if (player" + index + ") {" +
                        "      player" + index + ".unMute();" +
                        "      player" + index + ".setVolume(100);" +
                        "      player" + index + ".playVideo();" +
                        "    } else {" +
                        "      console.log('Player " + index + " is not ready.');" +
                        "    }" +
                        "  }" +
                        "  function playVdo" + index + "() {" +
                        "      player" + index + ".playVideo();" +
                        "  }" +
                        "  initPlayer();" +  // Initialize the player when this script runs
                        "</script>" +
                        "</body></html>";

                webView.addJavascriptInterface(new Object() {
                    @JavascriptInterface
                    public void mute() {
                        webView.evaluateJavascript("muteVideo" + index + "();", null); // Call unique JS mute function
                    }

                    @JavascriptInterface
                    public void unmute() {
                        webView.evaluateJavascript("unmuteVideo" + index + "();", null); // Call unique JS unmute function
                    }
                }, "Android");

                // Preload the content in the WebView
                webView.loadDataWithBaseURL("https://www.youtube.com", videoHtml, "text/html", "utf-8", null);

                // Hide the WebView initially
                webView.setVisibility(View.GONE);

                // Add to the list of preloaded WebViews
                webViewList.add(webView);

                // Add the WebView to the layout
                ((ViewGroup) findViewById(R.id.webviewContainer)).addView(webView);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void switchToChannel(int index) {
        if (index < 0 || index >= webViewList.size()) {
            return; // Index out of bounds, do nothing
        }

        // Hide the current WebView
        WebView currentWebView = webViewList.get(currentChannelIndex);
        currentWebView.setVisibility(View.GONE);

        EnableSound(index);

        // Show the next WebView
        WebView nextWebView = webViewList.get(index);
        nextWebView.setVisibility(View.VISIBLE);

        // Update the current channel index
        currentChannelIndex = index;
    }

    private void LoadChannel() throws JSONException {
        switchToChannel(channelIndex);

        JSONObject videoObject = channelJsonArray.getJSONObject(channelIndex);
        String channelName = videoObject.getString("channelName");
        saveActivity(channelName);
    }

    private void EnableSound(int idx) {
        Handler handler = new Handler(); // Create a new Handler for managing delays
        int delay = 20; // Set your desired delay in milliseconds

        // Mute all WebViews first
        for (int i = 0; i < webViewList.size(); i++) {
            final int index = i; // Final reference for use in the Runnable
            handler.postDelayed(() -> {
                webViewList.get(index).evaluateJavascript("muteVideo" + index + "();", null);
                //webViewList.get(index).evaluateJavascript("playVdo" + index + "();", null);
            }, delay); // Delay increases with each iteration
        }
    }


    @Override
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
    }


    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) { //for TV remote
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        //Toast.makeText(this, "clicked keycode = "+keyCode, Toast.LENGTH_SHORT).show();
        if(isLoadedScreenHided) {
            if (action == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_CHANNEL_UP:
                        playNextChannel();
                        return true;

                    case KeyEvent.KEYCODE_CHANNEL_DOWN:
                        playPreviousChannel();
                        return true;

                    default:
                        return super.dispatchKeyEvent(event);
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }*/



    private void playNextChannel(){
        if(channelIndex + 1 >= channelJsonArray.length()){
            Toast.makeText(PlayerActivity.this, "No next channel found!", Toast.LENGTH_SHORT).show();
        }
        else{
            channelIndex++;
            showOverlay();
            try {
                LoadChannel();
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
            try {
                LoadChannel();
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
        }, CHANNEL_BETWEEN_TIME); // 2000 milliseconds = 2 seconds
    }

    private void hideOverlay() {
        int delay = 1500; // Set your desired delay in milliseconds

        Handler handler = new Handler(); // Create a new Handler for managing delays

        if(!flag[channelIndex]){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try{
                        LoadChannel();
                        flag[channelIndex] = true;
                    }
                    catch(Exception e){}
                }
            }, delay);

        }


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // for (int i = 0; i < 10; i++) {
                webViewList.get(channelIndex).evaluateJavascript("unmuteVideo" + channelIndex + "();", null);
                //Toast.makeText(PlayerActivity.this, "channel idx = "+channelIndex, Toast.LENGTH_SHORT).show();
                // }
            }
        }, delay);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // for (int i = 0; i < 10; i++) {
                webViewList.get(channelIndex).evaluateJavascript("unmuteVideo" + channelIndex + "();", null);
                //Toast.makeText(PlayerActivity.this, "channel idx = "+channelIndex, Toast.LENGTH_SHORT).show();
                // }
            }
        }, delay);


        // Mute all WebViews first


        isLoadedScreenHided = true;
        overlayLayout.setVisibility(View.GONE);
    }


    private void saveActivity(String channelName) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
        String jwt_token = sharedPreferences.getString("accessToken", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                String URL = "https://livetv-njf6.onrender.com/tv/checkAvailability";

                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("channelName", channelName+" (from app)");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json; charset=utf-8"));

                Request request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + jwt_token)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        Log.d("PlayerActivity", "Response received: " + responseData);
                        //runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Saved activity!", Toast.LENGTH_SHORT).show());
                    } else {
                        Log.e("PlayerActivity", "Request failed: " + response.code());
                        //runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to save activity!", Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    Log.e("PlayerActivity", "IOException occurred: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error saving activity!", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Perform any necessary cleanup here
        // For example, unregister any receivers, stop services, etc.
        // Stop video playback in WebView

        for(WebView wv : webViewList){
            if (wv != null) {
                wv.stopLoading();
                wv.onPause();
                wv.destroy();
            }
        }
    }
}
