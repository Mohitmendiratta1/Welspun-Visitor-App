package com.example.welspunvisitorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import android.util.Log;
import java.util.Iterator;

public class DeleteEmployeeActivity extends AppCompatActivity {

    private EditText employeeCodeEditText;
    private Button submitButton, flagButton, unflagButton;
    private CardView cardViewEmployeeDetails;
    private TextView tvEmployeeName, tvEmployeeDOB, tvEmployeeDepartment;
    private ImageView imgEmployee;
    private String currentEmployeeCode; // Store current employee code for flag/unflag operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_employee);

        // Initialize views
        employeeCodeEditText = findViewById(R.id.etEmployeeCode);
        submitButton = findViewById(R.id.btnSubmit);
        cardViewEmployeeDetails = findViewById(R.id.cardViewEmployeeDetails);
        tvEmployeeName = findViewById(R.id.tvEmployeeName);
        tvEmployeeDOB = findViewById(R.id.tvEmployeeDOB);
        tvEmployeeDepartment = findViewById(R.id.tvEmployeeDepartment);
        imgEmployee = findViewById(R.id.imgEmployee);
        flagButton = findViewById(R.id.btnFlag);
        unflagButton = findViewById(R.id.btnUnflag);

        // Initially hide the card view
        cardViewEmployeeDetails.setVisibility(View.GONE);

        // Handle submit button click
        submitButton.setOnClickListener(v -> {
            String employeeCode = employeeCodeEditText.getText().toString().trim();
            if (!employeeCode.isEmpty()) {
                fetchEmployeeDetails(employeeCode);
            } else {
                Toast.makeText(DeleteEmployeeActivity.this, "Please enter an employee code", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Flag button click
        flagButton.setOnClickListener(v -> {
            if (currentEmployeeCode != null) {
                updateEmployeeFlagStatus(currentEmployeeCode, true); // Flag the employee
            } else {
                Toast.makeText(DeleteEmployeeActivity.this, "No employee selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Unflag button click
        unflagButton.setOnClickListener(v -> {
            if (currentEmployeeCode != null) {
                updateEmployeeFlagStatus(currentEmployeeCode, false); // Unflag the employee
            } else {
                Toast.makeText(DeleteEmployeeActivity.this, "No employee selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch employee details based on the employee code from Firebase
    private void fetchEmployeeDetails(String employeeCode) {
        String url = "https://welspun-visitor-app-600ba-default-rtdb.firebaseio.com/employees.json";

        // Make the API call
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // Log the response for debugging
                    Log.d("API_Response", response);

                    JSONObject jsonResponse = new JSONObject(response);
                    boolean employeeFound = false;

                    // Iterate through the employees data to find the employee with the matching code
                    for (Iterator<String> keys = jsonResponse.keys(); keys.hasNext(); ) {
                        String key = keys.next();
                        JSONObject employeeData = jsonResponse.getJSONObject(key);

                        // Check if the code matches
                        if (employeeData.getString("code").equals(employeeCode)) {
                            // Employee found, extract data
                            String name = employeeData.getString("name");
                            String dob = employeeData.getString("dob");
                            String department = employeeData.getString("department");

                            // Set data to the CardView
                            tvEmployeeName.setText("Employee Name: " + name);
                            tvEmployeeDOB.setText("DOB: " + dob);
                            tvEmployeeDepartment.setText("Department: " + department);

                            // Store the current employee code for flag/unflag actions
                            currentEmployeeCode = key;

                            // Show the CardView with employee details
                            cardViewEmployeeDetails.setVisibility(View.VISIBLE);

                            employeeFound = true;
                            break;  // Exit the loop once the employee is found
                        }
                    }

                    // If employee was not found
                    if (!employeeFound) {
                        Toast.makeText(DeleteEmployeeActivity.this, "Employee not found", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(DeleteEmployeeActivity.this, "Error processing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DeleteEmployeeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(this).add(stringRequest);
    }

    // Update the flag status of the employee in Firebase
    private void updateEmployeeFlagStatus(String employeeKey, boolean flagStatus) {
        String url = "https://welspun-visitor-app-600ba-default-rtdb.firebaseio.com/employees/" + employeeKey + "/flag.json"; // Modify URL if needed

        // Make the API call to update flag status
        JSONObject flagStatusJson = new JSONObject();
        try {
            flagStatusJson.put("flag", flagStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(DeleteEmployeeActivity.this, "Employee " + (flagStatus ? "flagged" : "unflagged") + " successfully", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DeleteEmployeeActivity.this, "Error updating flag status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public byte[] getBody() {
                return flagStatusJson.toString().getBytes();
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
