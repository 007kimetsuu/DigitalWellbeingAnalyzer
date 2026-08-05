package com.example.digitalwellbeinganalyzer.views;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName, profileEmail;
    private Button logoutButton;
    private ImageButton editProfileButton, settingsButton;
    private ImageView profileImage;
    private Switch darkModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        logoutButton = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        settingsButton = findViewById(R.id.settingsButton);
        profileImage = findViewById(R.id.profileImage);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);

        TextView notificationSettings = findViewById(R.id.notificationSettings);
        TextView reminderFrequency = findViewById(R.id.reminderFrequency);
        TextView breakDuration = findViewById(R.id.breakDuration);
        TextView privacyControls = findViewById(R.id.privacyControls);
        TextView exportData = findViewById(R.id.exportData);

        loadUserDetails();

        editProfileButton.setOnClickListener(v -> {

            Intent intent = new Intent(ProfileActivity.this,
                    EditProfileActivity.class);

            startActivity(intent);

        });
        settingsButton.setOnClickListener(v -> {

            String[] settings = {
                    "Account",
                    "Help & Support",
                    "About MindTrack",
                    "Version 1.0"
            };

            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Settings")
                    .setItems(settings, (dialog, which) -> {

                        switch (which) {

                            case 0:
                                Toast.makeText(ProfileActivity.this,
                                        "Account Settings Coming Soon",
                                        Toast.LENGTH_SHORT).show();
                                break;

                            case 1:
                                Toast.makeText(ProfileActivity.this,
                                        "Help & Support Coming Soon",
                                        Toast.LENGTH_SHORT).show();
                                break;

                            case 2:
                                new AlertDialog.Builder(ProfileActivity.this)
                                        .setTitle("About MindTrack")
                                        .setMessage("MindTrack\n\nVersion 1.0\n\nA Context-Aware Digital Wellbeing System developed to help users monitor and improve healthy smartphone usage.")
                                        .setPositiveButton("OK", null)
                                        .show();
                                break;

                            case 3:
                                Toast.makeText(ProfileActivity.this,
                                        "MindTrack Version 1.0",
                                        Toast.LENGTH_SHORT).show();
                                break;

                        }

                    })
                    .show();

        });

        logoutButton.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Intent intent =
                    new Intent(ProfileActivity.this,
                            LoginActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            finish();

        });

        notificationSettings.setOnClickListener(v -> {

            String[] options = {
                    "Daily Reminder",
                    "Weekly Report",
                    "Focus Alerts"
            };

            boolean[] checked = {
                    true,
                    true,
                    false
            };

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(ProfileActivity.this);

            builder.setTitle("Notification Settings");

            builder.setMultiChoiceItems(options,
                    checked,
                    (dialog, which, isChecked) -> {
                    });

            builder.setPositiveButton("Save",
                    (dialog, which) ->

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Notification Settings Saved",
                                    Toast.LENGTH_SHORT).show());

            builder.setNegativeButton("Cancel", null);

            builder.show();

        });

        reminderFrequency.setOnClickListener(v -> {

            String[] frequency = {
                    "15 Minutes",
                    "30 Minutes",
                    "1 Hour",
                    "2 Hours"
            };

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(ProfileActivity.this);

            builder.setTitle("Reminder Frequency");

            builder.setSingleChoiceItems(frequency,
                    1,
                    (dialog, which) -> {
                    });

            builder.setPositiveButton("Save",
                    (dialog, which) ->

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Reminder Updated",
                                    Toast.LENGTH_SHORT).show());

            builder.show();

        });
        breakDuration.setOnClickListener(v -> {

            String[] duration = {
                    "5 Minutes",
                    "10 Minutes",
                    "15 Minutes",
                    "20 Minutes"
            };

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(ProfileActivity.this);

            builder.setTitle("Break Duration");

            builder.setSingleChoiceItems(duration,
                    1,
                    (dialog, which) -> {
                    });

            builder.setPositiveButton("Save",
                    (dialog, which) ->

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Break Duration Updated",
                                    Toast.LENGTH_SHORT).show());

            builder.show();

        });

        privacyControls.setOnClickListener(v -> {

            String[] options = {
                    "Collect App Usage",
                    "Collect Screen Time",
                    "Anonymous Analytics"
            };

            boolean[] checked = {
                    true,
                    true,
                    false
            };

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(ProfileActivity.this);

            builder.setTitle("Privacy Controls");

            builder.setMultiChoiceItems(options,
                    checked,
                    (dialog, which, isChecked) -> {
                    });

            builder.setPositiveButton("Save",
                    (dialog, which) ->

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Privacy Settings Saved",
                                    Toast.LENGTH_SHORT).show());

            builder.show();

        });

        exportData.setOnClickListener(v -> {

            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Export Data")
                    .setMessage("Your wellbeing report export feature will be available in a future update.")
                    .setPositiveButton("OK", null)
                    .show();

        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                Toast.makeText(ProfileActivity.this,
                        "Dark Mode Coming Soon",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(ProfileActivity.this,
                        "Light Mode Enabled",
                        Toast.LENGTH_SHORT).show();

            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserDetails();
    }

    private void loadUserDetails() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            profileEmail.setText(user.getEmail());

            TextView userNameText = findViewById(R.id.userNameText);

            if (user.getDisplayName() != null &&
                    !user.getDisplayName().trim().isEmpty()) {

                profileName.setText(user.getDisplayName());
                userNameText.setText(user.getDisplayName());

            } else {

                profileName.setText("MindTrack User");
                userNameText.setText("MindTrack User");

            }

        } else {

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();

        }

    }
}