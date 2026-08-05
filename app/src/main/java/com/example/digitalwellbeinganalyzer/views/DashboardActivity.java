package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button logoutButton = findViewById(R.id.logoutButton);
        Button profileButton = findViewById(R.id.profileButton);
        Button appUsageButton = findViewById(R.id.appUsageButton);
        Button behaviorButton = findViewById(R.id.behaviorButton);
        appUsageButton.setOnClickListener(v -> {

            // App Usage screen will be added later

        });

        behaviorButton.setOnClickListener(v -> {

            // Behaviour Analysis screen will be added later

        });

        // Logout Button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Profile Button
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}