package com.example.welspunvisitorapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private Button adminLoginButton;
    private Button visitorPortalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Set the layout

        // Initialize buttons
        adminLoginButton = findViewById(R.id.adminLoginButton);
        visitorPortalButton = findViewById(R.id.visitorPortalButton);

        // Set onClick listeners for the buttons
        adminLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Admin Login Activity
                Intent intent = new Intent(LoginActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });

        visitorPortalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Visitor Portal Activity
                Intent intent = new Intent(LoginActivity.this, VisitorPortalActivity.class);
                startActivity(intent);
            }
        });
    }
}
