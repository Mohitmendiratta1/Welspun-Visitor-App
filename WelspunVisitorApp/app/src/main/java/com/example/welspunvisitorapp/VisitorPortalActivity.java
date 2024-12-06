package com.example.welspunvisitorapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;
import android.app.DatePickerDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class VisitorPortalActivity extends AppCompatActivity {

    private EditText employeeCodeEditText;
    private TextView validationResultTextView;
    private Spinner visitDepartmentSpinner;
    private EditText purposeEditText;
    private EditText meetNameEditText;
    private EditText dateEditText;
    private Spinner checkInHourSpinner;
    private Spinner checkInMinuteSpinner;
    private Spinner checkInAmPmSpinner;
    private Spinner checkOutHourSpinner;
    private Spinner checkOutMinuteSpinner;
    private Spinner checkOutAmPmSpinner;
    private Button validateButton;
    private Button submitVisitButton;
    private RequestQueue requestQueue;
    private LinearLayout visitDetailsLayout;

    private List<String> departmentList = Arrays.asList("WML", "WSL", "WDI", "WIL");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_portal);

        employeeCodeEditText = findViewById(R.id.employeeCodeEditText);
        validationResultTextView = findViewById(R.id.validationResultTextView);
        visitDepartmentSpinner = findViewById(R.id.visitDepartmentSpinner);
        purposeEditText = findViewById(R.id.purposeEditText);
        meetNameEditText = findViewById(R.id.meetNameEditText);
        dateEditText = findViewById(R.id.dateEditText);
        checkInHourSpinner = findViewById(R.id.checkInHourSpinner);
        checkInMinuteSpinner = findViewById(R.id.checkInMinuteSpinner);
        checkInAmPmSpinner = findViewById(R.id.checkInAmPmSpinner);
        checkOutHourSpinner = findViewById(R.id.checkOutHourSpinner);
        checkOutMinuteSpinner = findViewById(R.id.checkOutMinuteSpinner);
        checkOutAmPmSpinner = findViewById(R.id.checkOutAmPmSpinner);
        validateButton = findViewById(R.id.validateButton);
        submitVisitButton = findViewById(R.id.submitVisitButton);
        visitDetailsLayout = findViewById(R.id.visitDetailsLayout);

        visitDetailsLayout.setVisibility(View.GONE);

        requestQueue = Volley.newRequestQueue(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visitDepartmentSpinner.setAdapter(adapter);

        setupTimeSpinners();

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String employeeCode = employeeCodeEditText.getText().toString().trim();
                if (!employeeCode.isEmpty()) {
                    fetchEmployeeDetails(employeeCode);
                } else {
                    Toast.makeText(VisitorPortalActivity.this, "Please enter an employee code", Toast.LENGTH_SHORT).show();
                }
            }
        });

        submitVisitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitVisitDetails();
            }
        });

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void setupTimeSpinners() {
        String[] hours = new String[12];
        for (int i = 0; i < 12; i++) {
            hours[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hours);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        checkInHourSpinner.setAdapter(hourAdapter);
        checkOutHourSpinner.setAdapter(hourAdapter);

        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format("%02d", i);
        }
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, minutes);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        checkInMinuteSpinner.setAdapter(minuteAdapter);
        checkOutMinuteSpinner.setAdapter(minuteAdapter);

        String[] amPmOptions = {"AM", "PM"};
        ArrayAdapter<String> amPmAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, amPmOptions);
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        checkInAmPmSpinner.setAdapter(amPmAdapter);
        checkOutAmPmSpinner.setAdapter(amPmAdapter);
    }

    private void fetchEmployeeDetails(String employeeCode) {
        String url = "http://10.0.2.2:3000/get-employee?code=" + employeeCode;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Check for error response first
                            String status = response.getString("status");

                            if (status.equals("error")) {
                                String errorMessage = response.getString("message");
                                validationResultTextView.setText("Error: " + errorMessage);
                                visitDetailsLayout.setVisibility(View.GONE);
                            } else {
                                // Employee details found
                                String name = response.getString("name");
                                String department = response.getString("department");
                                String dob = response.getString("dob");

                                // Handle nested "flag" object
                                boolean isFlagged = false;
                                if (response.has("flag")) {
                                    JSONObject flagObject = response.getJSONObject("flag");
                                    if (flagObject.has("flag")) {
                                        isFlagged = flagObject.getBoolean("flag");
                                    }
                                }

                                // If flagged, show an error message
                                if (isFlagged) {
                                    validationResultTextView.setText("Employee is flagged and cannot be validated.");
                                    visitDetailsLayout.setVisibility(View.GONE);
                                    return; // Stop further processing
                                }

                                // If not flagged, display employee details
                                String result = "Name: " + name + "\nDepartment: " + department + "\nDOB: " + dob;
                                validationResultTextView.setText(result);

                                // Show the visit details section if employee is not flagged
                                visitDetailsLayout.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            validationResultTextView.setText("Error parsing employee details: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        validationResultTextView.setText("Error fetching details: " + error.getMessage());
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }





    private void submitVisitDetails() {
        String employeeCode = employeeCodeEditText.getText().toString().trim();
        String visitDepartment = visitDepartmentSpinner.getSelectedItem().toString();
        String purpose = purposeEditText.getText().toString().trim();
        String meetName = meetNameEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();

        String checkInHour = checkInHourSpinner.getSelectedItem().toString();
        String checkInMinute = checkInMinuteSpinner.getSelectedItem().toString();
        String checkInAmPm = checkInAmPmSpinner.getSelectedItem().toString();
        String checkOutHour = checkOutHourSpinner.getSelectedItem().toString();
        String checkOutMinute = checkOutMinuteSpinner.getSelectedItem().toString();
        String checkOutAmPm = checkOutAmPmSpinner.getSelectedItem().toString();

        String checkInTime = checkInHour + ":" + checkInMinute + " " + checkInAmPm;
        String checkOutTime = checkOutHour + ":" + checkOutMinute + " " + checkOutAmPm;

        if (employeeCode.isEmpty() || purpose.isEmpty() || meetName.isEmpty() || date.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        JSONObject visitData = new JSONObject();
        try {
            visitData.put("employeeCode", employeeCode);
            visitData.put("visitDepartment", visitDepartment);
            visitData.put("purpose", purpose);
            visitData.put("meetName", meetName);
            visitData.put("date", date);
            visitData.put("checkInTime", checkInTime);
            visitData.put("checkOutTime", checkOutTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "http://10.0.2.2:3000/add-visit";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, visitData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            showAlert("Success", response.getString("message"));
                        } catch (JSONException e) {
                            showAlert("Error", "Error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showAlert("Error", "Error submitting visit details: " + error.getMessage());
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(VisitorPortalActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        dateEditText.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (title.equals("Success")) {
                            resetForm();
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void resetForm() {
        employeeCodeEditText.setText("");
        purposeEditText.setText("");
        meetNameEditText.setText("");
        dateEditText.setText("");
        validationResultTextView.setText("");
        visitDetailsLayout.setVisibility(View.GONE);
    }
}
