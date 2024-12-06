package com.example.welspunvisitorapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewEmployeeActivity extends AppCompatActivity {

    private EditText employeeNameEditText;
    private EditText employeeCodeEditText;
    private EditText dobEditText;
    private Spinner departmentSpinner;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_employee);

        // Initializing views
        employeeNameEditText = findViewById(R.id.employeeNameEditText);
        employeeCodeEditText = findViewById(R.id.employeeCodeEditText);
        dobEditText = findViewById(R.id.dobEditText);
        departmentSpinner = findViewById(R.id.departmentSpinner);
        saveButton = findViewById(R.id.saveButton);

        // Setting up the spinner with department options
        String[] departments = {"WML", "WSL", "WDI", "WIL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpinner.setAdapter(adapter);

        // Save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Collecting input data
                String name = employeeNameEditText.getText().toString();
                String code = employeeCodeEditText.getText().toString();
                String dob = dobEditText.getText().toString();
                String department = departmentSpinner.getSelectedItem().toString();

                // Validating input
                if (name.isEmpty() || code.isEmpty() || dob.isEmpty()) {
                    Toast.makeText(NewEmployeeActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Creating a new thread for network operation
                new Thread(() -> {
                    try {
                        // Firebase Realtime Database URL
                        URL url = new URL("https://welspun-visitor-app-600ba-default-rtdb.firebaseio.com/employees.json");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoOutput(true);

                        // Creating JSON input string
                        String jsonInputString = String.format(
                                "{\"name\": \"%s\", \"code\": \"%s\", \"dob\": \"%s\", \"department\": \"%s\"}",
                                name, code, dob, department
                        );

                        // Sending JSON data to Firebase
                        try (OutputStream os = conn.getOutputStream()) {
                            byte[] input = jsonInputString.getBytes("utf-8");
                            os.write(input, 0, input.length);
                        }

                        // Reading and logging the response from Firebase
                        int responseCode = conn.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            StringBuilder response = new StringBuilder();
                            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                                String responseLine;
                                while ((responseLine = in.readLine()) != null) {
                                    response.append(responseLine.trim());
                                }
                            }

                            Log.d("FirebaseResponse", "Response: " + response.toString()); // Log full response
                            runOnUiThread(() -> {
                                Toast.makeText(NewEmployeeActivity.this, "Employee Saved", Toast.LENGTH_SHORT).show();
                                finish(); // Close the activity
                            });
                        } else {
                            Log.e("FirebaseError", "Error code: " + responseCode);
                            runOnUiThread(() -> Toast.makeText(NewEmployeeActivity.this, "Error code: " + responseCode, Toast.LENGTH_LONG).show());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("FirebaseError", "Error: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(NewEmployeeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }).start();
            }
        });
    }
}
