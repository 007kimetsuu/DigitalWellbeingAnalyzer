package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();

        TextView profileName = findViewById(R.id.profileName);
        TextView profileEmail = findViewById(R.id.profileEmail);
        TextView profileInitial = findViewById(R.id.profileInitial);

        Button usageAccessButton =
                findViewById(R.id.usageAccessButton);

        Button logoutButton =
                findViewById(R.id.logoutButton);

        // Load Firebase user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {

            String email = currentUser.getEmail();

            if (email != null && !email.isEmpty()) {

                profileEmail.setText(email);

                String initial =
                        email.substring(0, 1).toUpperCase();

                profileInitial.setText(initial);

                profileName.setText("User");
            }
        }

        // Usage Access
        usageAccessButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);

            startActivity(intent);
        });

        // Logout
        logoutButton.setOnClickListener(v -> {

            firebaseAuth.signOut();

            Intent intent = new Intent(
                    ProfileActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }
}