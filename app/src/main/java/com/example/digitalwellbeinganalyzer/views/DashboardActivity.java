package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Connect to Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Connect dashboard UI elements
        TextView welcomeText = findViewById(R.id.welcomeText);
        Button logoutButton = findViewById(R.id.logoutButton);
        Button appUsageButton = findViewById(R.id.appUsageButton);
        Button behaviorButton = findViewById(R.id.behaviorButton);

            behaviorButton.setOnClickListener(v -> {
                Intent intent = new Intent(
                        DashboardActivity.this,
                        BehaviorAnalysisActivity.class
                );
                startActivity(intent);
            });

            appUsageButton.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, AppUsageActivity.class);
                startActivity(intent);
            });

        // Get the currently logged-in Firebase user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();

            welcomeText.setText("Welcome, " + email);
        }

        // Logout
        logoutButton.setOnClickListener(v -> {

            firebaseAuth.signOut();

            Intent intent = new Intent(
                    DashboardActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
        });
    }
}