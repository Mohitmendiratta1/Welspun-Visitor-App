package com.example.welspunvisitorapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateEmployeeActivity extends AppCompatActivity {

    private TextView visitorIdTextView, employeeCodeTextView, employeeNameTextView;
    private EditText visitDepartmentEditText, personMetEditText, dateEditText, checkInTimeEditText, checkOutTimeEditText, purposeEditText;
    private Button updateButton;
    private RequestQueue requestQueue;
    private String visitorId, employeeCode, employeeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_employee);

        // Initialize views
        visitorIdTextView = findViewById(R.id.visitorIdTextView);
        employeeCodeTextView = findViewById(R.id.employeeCodeTextView);
        employeeNameTextView = findViewById(R.id.employeeNameTextView);
        visitDepartmentEditText = findViewById(R.id.visitDepartmentEditText);
        personMetEditText = findViewById(R.id.personMetEditText);
        dateEditText = findViewById(R.id.dateEditText);
        checkInTimeEditText = findViewById(R.id.checkInTimeEditText);
        checkOutTimeEditText = findViewById(R.id.checkOutTimeEditText);
        purposeEditText = findViewById(R.id.purposeEditText);
        updateButton = findViewById(R.id.updateButton);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Retrieve data passed from previous activity
        visitorId = getIntent().getStringExtra("visitorId");
        employeeCode = getIntent().getStringExtra("employeeCode");
        employeeName = getIntent().getStringExtra("employeeName");

        // Display visitorId and employeeCode in TextViews
        if (visitorId != null) {
            visitorIdTextView.setText("Visitor ID: " + visitorId);
        } else {
            showToast("Visitor ID not found");
        }

        if (employeeCode != null) {
            employeeCodeTextView.setText("Employee Code: " + employeeCode);
        } else {
            showToast("Employee code not found");
        }

        // Populate employee name TextView
        if (employeeName != null) {
            employeeNameTextView.setText(employeeName);
        } else {
            showToast("Employee name not found");
        }

        // Populate other fields with existing data if available
        visitDepartmentEditText.setText(getIntent().getStringExtra("visitDepartment") != null ? getIntent().getStringExtra("visitDepartment") : "");
        personMetEditText.setText(getIntent().getStringExtra("meetName") != null ? getIntent().getStringExtra("meetName") : "");
        dateEditText.setText(getIntent().getStringExtra("visitDate") != null ? getIntent().getStringExtra("visitDate") : "");
        checkInTimeEditText.setText(getIntent().getStringExtra("checkInTime") != null ? getIntent().getStringExtra("checkInTime") : "");
        checkOutTimeEditText.setText(getIntent().getStringExtra("checkOutTime") != null ? getIntent().getStringExtra("checkOutTime") : "");
        purposeEditText.setText(getIntent().getStringExtra("purpose") != null ? getIntent().getStringExtra("purpose") : "");

        // Set up the button click listener to update the visit details
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateVisitDetails();
            }
        });
    }

    private void updateVisitDetails() {
        String updatedDepartment = visitDepartmentEditText.getText().toString().trim();
        String updatedPurpose = purposeEditText.getText().toString().trim();
        String updatedDate = dateEditText.getText().toString().trim();
        String updatedCheckInTime = checkInTimeEditText.getText().toString().trim();
        String updatedCheckOutTime = checkOutTimeEditText.getText().toString().trim();
        String updatedMeetName = personMetEditText.getText().toString().trim();

        if (updatedDepartment.isEmpty() && updatedPurpose.isEmpty() && updatedDate.isEmpty() && updatedCheckInTime.isEmpty() && updatedCheckOutTime.isEmpty() && updatedMeetName.isEmpty()) {
            showToast("Please update at least one field.");
            return;
        }

        JSONObject updatedVisitDetails = new JSONObject();
        try {
            updatedVisitDetails.put("visitorId", visitorId);
            updatedVisitDetails.put("employeeCode", employeeCode);

            if (!updatedDepartment.isEmpty()) {
                updatedVisitDetails.put("visitDepartment", updatedDepartment);
            }
            if (!updatedPurpose.isEmpty()) {
                updatedVisitDetails.put("purpose", updatedPurpose);
            }
            if (!updatedDate.isEmpty()) {
                updatedVisitDetails.put("date", updatedDate);
            }
            if (!updatedCheckInTime.isEmpty()) {
                updatedVisitDetails.put("checkInTime", updatedCheckInTime);
            }
            if (!updatedCheckOutTime.isEmpty()) {
                updatedVisitDetails.put("checkOutTime", updatedCheckOutTime);
            }
            if (!updatedMeetName.isEmpty()) {
                updatedVisitDetails.put("meetName", updatedMeetName);
            }

            Log.d("UpdateVisitDetails", updatedVisitDetails.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Error creating JSON object");
            return;
        }

        String url = "http://10.0.2.2:3000/update-visit-details"; // Make sure to change this for production

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, updatedVisitDetails,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if ("success".equals(status)) {
                                showToast("Visit details updated successfully");
                                resetFields();
                            } else {
                                showToast("Failed to update visit details");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showToast("Error processing response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", "Error uploading visit details: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e("VolleyError", "Error code: " + error.networkResponse.statusCode);
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                Log.e("VolleyError", "Error response: " + responseBody);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        showToast("Error uploading visit details. Please try again later.");
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void showToast(String message) {
        Toast.makeText(UpdateEmployeeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void resetFields() {
        visitDepartmentEditText.setText("");
        purposeEditText.setText("");
        dateEditText.setText("");
        checkInTimeEditText.setText("");
        checkOutTimeEditText.setText("");
        personMetEditText.setText("");
    }
}
