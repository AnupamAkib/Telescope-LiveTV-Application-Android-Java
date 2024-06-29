package com.example.livetv;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 2200; // 2.2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(!NetworkUtils.isInternetConnected(this)){
            Toast.makeText(SplashActivity.this, "Please check your internet & try again", Toast.LENGTH_LONG).show();
            Intent tmp = new Intent(SplashActivity.this, ErrorActivity.class);
            tmp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(tmp);
            return;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
                String jwt_token = sharedPreferences.getString("accessToken", "");


                Intent intent;
                if (!jwt_token.equals("")) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.putExtra("jwt_token", jwt_token);
                    //Toast.makeText(SplashActivity.this, jwt_token, Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

}

class NetworkUtils {
    public static boolean isInternetConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }
}