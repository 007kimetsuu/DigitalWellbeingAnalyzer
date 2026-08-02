package com.example.digitalwellbeinganalyzer.views;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.example.digitalwellbeinganalyzer.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText resetEmailEditText;
    private Button resetPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        resetEmailEditText = findViewById(R.id.resetEmailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        mAuth = FirebaseAuth.getInstance();

        TextView backToLoginText = findViewById(R.id.backToLoginText);
        resetPasswordButton.setOnClickListener(v -> {

            String email = resetEmailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                resetEmailEditText.setError("Please enter your email");
                resetEmailEditText.requestFocus();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    "Password reset link sent to your email.",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    });

        });
        backToLoginText.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}