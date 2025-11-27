package com.somil.dailywisdom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;
    private TextView forgotPasswordTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        loginButton.setOnClickListener(v -> handleLogin());

        forgotPasswordTextView.setOnClickListener(v -> handleForgotPassword());

        signUpTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });
    }

    private void handleForgotPassword() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Please enter your email to reset password.");
            emailEditText.requestFocus();
            return;
        }
        forgotPasswordTextView.setTextColor(Color.parseColor("#757575"));

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent. Please check your email spam folder.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to send reset email. Please check the email address.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address.");
            emailEditText.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finishAffinity();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                        Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}