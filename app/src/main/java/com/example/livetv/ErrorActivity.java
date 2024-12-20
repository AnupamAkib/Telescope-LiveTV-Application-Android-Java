package com.example.livetv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ErrorActivity extends AppCompatActivity {

    private Button settingsNavigatorBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_error);

        settingsNavigatorBtn = findViewById(R.id.settings_nav_btn);

        Toast.makeText(ErrorActivity.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();

        settingsNavigatorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tmp = new Intent(ErrorActivity.this, SettingsActivity.class);
                tmp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(tmp);
            }
        });
    }
}