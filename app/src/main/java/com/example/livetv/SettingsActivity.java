package com.example.livetv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefsFile";

    private static final String SERVER_URL = "SERVER_URL";

    private EditText serverUrl;
    private Button saveSettingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.settings_page);

        serverUrl = findViewById(R.id.serverUrl);
        saveSettingsBtn = findViewById(R.id.saveSettings);

        serverUrl.setText(Server.getBackendUrl(this));
        saveSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String backendURL = serverUrl.getText().toString().trim();

                setBackendUrl(backendURL);
            }
        });

    }

    private void setBackendUrl(String _url){
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SERVER_URL", _url);
        editor.apply();

        Intent tmp = new Intent(SettingsActivity.this, SplashActivity.class);
        tmp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(tmp);
    }
}
