package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.digitalwellbeinganalyzer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private android.widget.TextView profileGreetingText;
    private android.widget.TextView profileNameText;
    private android.widget.TextView profileEmailText;
    private android.widget.TextView profileOccupationText;

    private SwitchCompat darkModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();

        loadUserData();
        setupBottomNavigation();
        setupProfileActions();
    }

    // =========================================================
    // USER DATA
    // =========================================================

    private void loadUserData() {

        profileGreetingText =
                findViewById(R.id.profileGreetingText);

        profileNameText =
                findViewById(R.id.profileNameText);

        profileEmailText =
                findViewById(R.id.profileEmailText);

        profileOccupationText =
                findViewById(R.id.profileOccupationText);

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            profileGreetingText.setText("Good morning, Student");
            profileNameText.setText("Student Name");
            profileEmailText.setText("student@email.com");
            profileOccupationText.setText("Occupation: Student");
            return;
        }

        String name = currentUser.getDisplayName();
        String email = currentUser.getEmail();

        if (name != null && !name.trim().isEmpty()) {

            profileNameText.setText(name);
            profileGreetingText.setText(
                    "Good morning, " + name
            );

        } else {

            profileNameText.setText("Student");
            profileGreetingText.setText(
                    "Good morning, Student"
            );
        }

        if (email != null && !email.trim().isEmpty()) {

            profileEmailText.setText(email);

        } else {

            profileEmailText.setText(
                    "student@email.com"
            );
        }

        profileOccupationText.setText(
                "Occupation: Student"
        );
    }


    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void setupBottomNavigation() {

        findViewById(R.id.navHome).setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProfileActivity.this,
                    DashboardActivity.class
            );

            startActivity(intent);
            finish();
        });


        findViewById(R.id.navAnalytics).setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProfileActivity.this,
                    M2AnalyticsActivity.class
            );

            startActivity(intent);
            finish();
        });


        findViewById(R.id.navGuides).setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProfileActivity.this,
                    GuideActivity.class
            );

            startActivity(intent);
            finish();
        });


        // Already on Profile
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            // Do nothing
        });
    }


    // =========================================================
    // PROFILE ACTIONS
    // =========================================================

    private void setupProfileActions() {

        // Edit profile

        findViewById(R.id.editProfileIcon)
                .setOnClickListener(v -> {

                    Intent intent = new Intent(
                            ProfileActivity.this,
                            EditProfileActivity.class
                    );

                    startActivity(intent);
                });


        // Notification settings

        findViewById(R.id.notificationSettingsRow)
                .setOnClickListener(v -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Notification settings coming soon",
                            Toast.LENGTH_SHORT
                    ).show();
                });


        // Reminder frequency

        findViewById(R.id.reminderFrequencyRow)
                .setOnClickListener(v -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Reminder frequency coming soon",
                            Toast.LENGTH_SHORT
                    ).show();
                });


        // Break duration

        findViewById(R.id.breakDurationRow)
                .setOnClickListener(v -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Break duration coming soon",
                            Toast.LENGTH_SHORT
                    ).show();
                });


        // Dark mode

        darkModeSwitch =
                findViewById(R.id.darkModeSwitch);

        darkModeSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Dark mode coming soon",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Light mode selected",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );


        // Privacy

        findViewById(R.id.privacyControlsRow)
                .setOnClickListener(v -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Privacy controls coming soon",
                            Toast.LENGTH_SHORT
                    ).show();
                });


        // Export

        findViewById(R.id.exportDataRow)
                .setOnClickListener(v -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Data export coming soon",
                            Toast.LENGTH_SHORT
                    ).show();
                });


        // Logout

        findViewById(R.id.logoutButton)
                .setOnClickListener(v -> {

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