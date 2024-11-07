package com.example.welspunvisitorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle save button click
                String name = employeeNameEditText.getText().toString();
                String code = employeeCodeEditText.getText().toString();
                String dob = dobEditText.getText().toString();
                String department = departmentSpinner.getSelectedItem().toString();

                // Validate input
                if (name.isEmpty() || code.isEmpty() || dob.isEmpty()) {
                    Toast.makeText(NewEmployeeActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a new thread for network operation
                new Thread(() -> {
                    try {
                        // Use your local IP address for physical devices
                        URL url = new URL("http://10.0.2.2:3000/add-employee"); // Replace with your actual IP address if needed
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoOutput(true);

                        // Create the JSON data to send
                        String jsonInputString = String.format(
                                "{\"name\": \"%s\", \"code\": \"%s\", \"dob\": \"%s\", \"department\": \"%s\"}",
                                name, code, dob, department
                        );

                        // Send the JSON data
                        try (OutputStream os = conn.getOutputStream()) {
                            byte[] input = jsonInputString.getBytes("utf-8");
                            os.write(input, 0, input.length);
                        }

                        // Check the response code
                        int responseCode = conn.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            runOnUiThread(() -> {
                                Toast.makeText(NewEmployeeActivity.this, "Employee Saved", Toast.LENGTH_SHORT).show();
                                finish(); // Close the activity
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(NewEmployeeActivity.this, "Saved", Toast.LENGTH_SHORT).show());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(NewEmployeeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        });
    }
}
