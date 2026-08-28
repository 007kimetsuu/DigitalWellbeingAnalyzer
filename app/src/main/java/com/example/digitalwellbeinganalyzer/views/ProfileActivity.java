package com.example.digitalwellbeinganalyzer.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.digitalwellbeinganalyzer.InterventionManager;
import com.example.digitalwellbeinganalyzer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private TextView profileGreetingText;
    private TextView profileNameText;
    private TextView profileEmailText;
    private TextView profileOccupationText;

    private TextView reminderFrequencyValueText;
    private TextView breakDurationValueText;

    private SwitchCompat darkModeSwitch;


    // =========================================================
    // ACTIVITY
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();

        bindViews();
        loadUserData();
        loadSavedSettings();
        setupBottomNavigation();
        setupProfileActions();
    }


    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Refresh values in case something changed
         * while the Activity was away.
         */
        loadUserData();
        loadSavedSettings();
    }


    // =========================================================
    // VIEW BINDING
    // =========================================================

    private void bindViews() {

        profileGreetingText =
                findViewById(R.id.profileGreetingText);

        profileNameText =
                findViewById(R.id.profileNameText);

        profileEmailText =
                findViewById(R.id.profileEmailText);

        profileOccupationText =
                findViewById(R.id.profileOccupationText);

        reminderFrequencyValueText =
                findViewById(R.id.reminderFrequencyValueText);

        breakDurationValueText =
                findViewById(R.id.breakDurationValueText);

        darkModeSwitch =
                findViewById(R.id.darkModeSwitch);
    }


    // =========================================================
    // USER DATA
    // =========================================================

    private void loadUserData() {

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();


        if (currentUser == null) {

            profileGreetingText.setText(
                    "Good morning, Student"
            );

            profileNameText.setText(
                    "Student Name"
            );

            profileEmailText.setText(
                    "student@email.com"
            );

            profileOccupationText.setText(
                    "Occupation: Student"
            );

            return;
        }


        String name =
                currentUser.getDisplayName();

        String email =
                currentUser.getEmail();


        if (name != null &&
                !name.trim().isEmpty()) {

            profileNameText.setText(
                    name
            );

            profileGreetingText.setText(
                    "Good morning, " + name
            );

        } else {

            profileNameText.setText(
                    "Student"
            );

            profileGreetingText.setText(
                    "Good morning, Student"
            );
        }


        if (email != null &&
                !email.trim().isEmpty()) {

            profileEmailText.setText(
                    email
            );

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
    // SAVED SETTINGS
    // =========================================================

    private void loadSavedSettings() {

        int reminderMinutes =
                InterventionManager
                        .getReminderFrequencyMinutes(
                                getApplicationContext()
                        );

        int breakMinutes =
                InterventionManager
                        .getBreakDurationMinutes(
                                getApplicationContext()
                        );


        updateReminderFrequencyText(
                reminderMinutes
        );

        updateBreakDurationText(
                breakMinutes
        );


        /*
         * Dark mode is not implemented yet.
         *
         * Keep the visual switch off instead of pretending
         * the setting works.
         */
        darkModeSwitch.setChecked(false);
    }


    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void setupBottomNavigation() {

        findViewById(R.id.navHome)
                .setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    ProfileActivity.this,
                                    DashboardActivity.class
                            );

                    startActivity(intent);
                    finish();
                });


        findViewById(R.id.navAnalytics)
                .setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    ProfileActivity.this,
                                    M2AnalyticsActivity.class
                            );

                    startActivity(intent);
                    finish();
                });


        findViewById(R.id.navGuides)
                .setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    ProfileActivity.this,
                                    GuideActivity.class
                            );

                    startActivity(intent);
                    finish();
                });


        // Already on Profile
        findViewById(R.id.navProfile)
                .setOnClickListener(v -> {
                    // Do nothing
                });
    }


    // =========================================================
    // PROFILE ACTIONS
    // =========================================================

    private void setupProfileActions() {

        // =====================================================
        // EDIT PROFILE
        // =====================================================

        findViewById(R.id.editProfileIcon)
                .setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    ProfileActivity.this,
                                    EditProfileActivity.class
                            );

                    startActivity(intent);
                });


        // =====================================================
        // NOTIFICATION SETTINGS
        // =====================================================

        findViewById(R.id.notificationSettingsRow)
                .setOnClickListener(v ->
                        showNotificationSettingsDialog()
                );


        // =====================================================
        // REMINDER FREQUENCY
        // =====================================================

        findViewById(R.id.reminderFrequencyRow)
                .setOnClickListener(v ->
                        showReminderFrequencyDialog()
                );


        // =====================================================
        // BREAK DURATION
        // =====================================================

        findViewById(R.id.breakDurationRow)
                .setOnClickListener(v ->
                        showBreakDurationDialog()
                );


        // =====================================================
        // DARK MODE
        // =====================================================

        darkModeSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Dark mode is not available yet",
                                Toast.LENGTH_SHORT
                        ).show();

                        /*
                         * Turn it back off because the rest of
                         * the app still uses fixed light colors.
                         */
                        buttonView.setChecked(false);
                    }
                }
        );


        // =====================================================
        // PRIVACY
        // =====================================================

        findViewById(R.id.privacyControlsRow)
                .setOnClickListener(v -> {

                    new AlertDialog.Builder(
                            ProfileActivity.this
                    )
                            .setTitle(
                                    "Privacy Controls"
                            )
                            .setMessage(
                                    "MindTrack uses Android usage access and accessibility-derived behavioral data to generate on-device wellbeing analytics.\n\nMore detailed privacy controls will be added during final app hardening."
                            )
                            .setPositiveButton(
                                    "OK",
                                    null
                            )
                            .show();
                });


        // =====================================================
        // EXPORT
        // =====================================================

        findViewById(R.id.exportDataRow)
                .setOnClickListener(v -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Data export will be added during final data handling",
                            Toast.LENGTH_SHORT
                    ).show();
                });


        // =====================================================
        // LOGOUT
        // =====================================================

        findViewById(R.id.logoutButton)
                .setOnClickListener(v ->
                        showLogoutConfirmation()
                );
    }


    // =========================================================
    // NOTIFICATION DIALOG
    // =========================================================

    private void showNotificationSettingsDialog() {

        boolean currentlyEnabled =
                InterventionManager
                        .areNotificationsEnabled(
                                getApplicationContext()
                        );


        String[] options = {
                "Enable MindTrack alerts",
                "Disable MindTrack alerts"
        };


        int selectedItem =
                currentlyEnabled
                        ? 0
                        : 1;


        new AlertDialog.Builder(
                ProfileActivity.this
        )
                .setTitle(
                        "Notification Settings"
                )
                .setSingleChoiceItems(
                        options,
                        selectedItem,
                        (dialog, which) -> {

                            boolean enabled =
                                    which == 0;


                            InterventionManager
                                    .setNotificationsEnabled(
                                            getApplicationContext(),
                                            enabled
                                    );


                            Toast.makeText(
                                    ProfileActivity.this,
                                    enabled
                                            ? "MindTrack alerts enabled"
                                            : "MindTrack alerts disabled",
                                    Toast.LENGTH_SHORT
                            ).show();


                            dialog.dismiss();
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }


    // =========================================================
    // REMINDER FREQUENCY DIALOG
    // =========================================================

    private void showReminderFrequencyDialog() {

        String[] options = {
                "Every 30 minutes",
                "Every 1 hour",
                "Every 2 hours",
                "Every 4 hours",
                "Every 6 hours"
        };


        int[] values = {
                30,
                60,
                120,
                240,
                360
        };


        int currentValue =
                InterventionManager
                        .getReminderFrequencyMinutes(
                                getApplicationContext()
                        );


        int selectedIndex =
                findMatchingIndex(
                        values,
                        currentValue
                );


        new AlertDialog.Builder(
                ProfileActivity.this
        )
                .setTitle(
                        "Reminder Frequency"
                )
                .setSingleChoiceItems(
                        options,
                        selectedIndex,
                        (dialog, which) -> {

                            int selectedMinutes =
                                    values[which];


                            InterventionManager
                                    .setReminderFrequencyMinutes(
                                            getApplicationContext(),
                                            selectedMinutes
                                    );


                            updateReminderFrequencyText(
                                    selectedMinutes
                            );


                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Reminder frequency updated",
                                    Toast.LENGTH_SHORT
                            ).show();


                            dialog.dismiss();
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }


    // =========================================================
    // BREAK DURATION DIALOG
    // =========================================================

    private void showBreakDurationDialog() {

        String[] options = {
                "5 minutes",
                "10 minutes",
                "15 minutes",
                "20 minutes"
        };


        int[] values = {
                5,
                10,
                15,
                20
        };


        int currentValue =
                InterventionManager
                        .getBreakDurationMinutes(
                                getApplicationContext()
                        );


        int selectedIndex =
                findMatchingIndex(
                        values,
                        currentValue
                );


        new AlertDialog.Builder(
                ProfileActivity.this
        )
                .setTitle(
                        "Break Duration"
                )
                .setSingleChoiceItems(
                        options,
                        selectedIndex,
                        (dialog, which) -> {

                            int selectedMinutes =
                                    values[which];


                            InterventionManager
                                    .setBreakDurationMinutes(
                                            getApplicationContext(),
                                            selectedMinutes
                                    );


                            updateBreakDurationText(
                                    selectedMinutes
                            );


                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Break duration updated",
                                    Toast.LENGTH_SHORT
                            ).show();


                            dialog.dismiss();
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }


    // =========================================================
    // DISPLAY SETTING VALUES
    // =========================================================

    private void updateReminderFrequencyText(
            int minutes
    ) {

        if (minutes < 60) {

            reminderFrequencyValueText.setText(
                    "Every " + minutes + " minutes"
            );

            return;
        }


        int hours =
                minutes / 60;


        if (hours == 1) {

            reminderFrequencyValueText.setText(
                    "Every 1 hour"
            );

        } else {

            reminderFrequencyValueText.setText(
                    "Every " + hours + " hours"
            );
        }
    }


    private void updateBreakDurationText(
            int minutes
    ) {

        if (minutes == 1) {

            breakDurationValueText.setText(
                    "1 minute break"
            );

        } else {

            breakDurationValueText.setText(
                    minutes + " minute breaks"
            );
        }
    }


    // =========================================================
    // HELPER
    // =========================================================

    private int findMatchingIndex(
            int[] values,
            int target
    ) {

        for (int i = 0;
             i < values.length;
             i++) {

            if (values[i] == target) {
                return i;
            }
        }


        return 0;
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    private void showLogoutConfirmation() {

        new AlertDialog.Builder(
                ProfileActivity.this
        )
                .setTitle(
                        "Logout"
                )
                .setMessage(
                        "Are you sure you want to log out?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Logout",
                        (dialog, which) -> {

                            firebaseAuth.signOut();


                            Intent intent =
                                    new Intent(
                                            ProfileActivity.this,
                                            LoginActivity.class
                                    );


                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );


                            startActivity(intent);
                            finish();
                        }
                )
                .show();
    }
}