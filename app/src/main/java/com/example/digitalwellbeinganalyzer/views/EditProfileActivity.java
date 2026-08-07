package com.example.digitalwellbeinganalyzer.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalwellbeinganalyzer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText;
    private Button saveButton, changePhotoButton;
    private ImageView editProfileImage;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Linking XML views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        saveButton = findViewById(R.id.saveButton);
        editProfileImage = findViewById(R.id.editProfileImage);
        changePhotoButton = findViewById(R.id.changePhotoButton);


        // Get current Firebase user
        user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {

            // Display existing name
            if (user.getDisplayName() != null) {
                nameEditText.setText(user.getDisplayName());
            }

            // Display email
            emailEditText.setText(user.getEmail());


            // Save Profile Changes
            saveButton.setOnClickListener(v -> {

                String newName = nameEditText.getText()
                        .toString()
                        .trim();


                if (newName.isEmpty()) {

                    nameEditText.setError("Enter your name");
                    return;

                }


                UserProfileChangeRequest profileUpdates =
                        new UserProfileChangeRequest.Builder()
                                .setDisplayName(newName)
                                .build();


                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {

                                Toast.makeText(
                                        EditProfileActivity.this,
                                        "Profile Updated Successfully",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();

                            } else {

                                Toast.makeText(
                                        EditProfileActivity.this,
                                        "Profile Update Failed",
                                        Toast.LENGTH_SHORT
                                ).show();

                            }

                        });

            });



            // Change Profile Picture (Future Firebase Storage)
            changePhotoButton.setOnClickListener(v -> {

                Toast.makeText(
                        EditProfileActivity.this,
                        "Change Profile Picture feature coming soon!",
                        Toast.LENGTH_SHORT
                ).show();

            });

        }

    }
}