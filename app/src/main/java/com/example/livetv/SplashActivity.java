package com.example.livetv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 1500; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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
