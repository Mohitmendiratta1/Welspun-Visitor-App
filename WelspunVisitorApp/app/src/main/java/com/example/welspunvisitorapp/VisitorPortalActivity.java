package com.example.welspunvisitorapp;

import android.app.DatePickerDialog;
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
    private LinearLayout visitDetailsLayout; // Layout for visit details

    private List<String> departmentList = Arrays.asList("WML", "WSL", "WDI", "WIL"); // Add your actual departments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_portal);

        // Initialize UI components
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
        visitDetailsLayout = findViewById(R.id.visitDetailsLayout); // Initialize visit details layout

        // Hide visit details layout initially
        visitDetailsLayout.setVisibility(View.GONE);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Set up department spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departmentList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visitDepartmentSpinner.setAdapter(adapter);

        // Set up time spinners
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

        // Set up a date picker for dateEditText
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void setupTimeSpinners() {
        // Hours (1-12)
        String[] hours = new String[12];
        for (int i = 0; i < 12; i++) {
            hours[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hours);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        checkInHourSpinner.setAdapter(hourAdapter);
        checkOutHourSpinner.setAdapter(hourAdapter);

        // Minutes (0-59)
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format("%02d", i); // Format minutes as two digits
        }
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, minutes);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        checkInMinuteSpinner.setAdapter(minuteAdapter);
        checkOutMinuteSpinner.setAdapter(minuteAdapter);

        // AM/PM
        String[] amPmOptions = {"AM", "PM"};
        ArrayAdapter<String> amPmAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, amPmOptions);
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        checkInAmPmSpinner.setAdapter(amPmAdapter);
        checkOutAmPmSpinner.setAdapter(amPmAdapter);
    }

    private void fetchEmployeeDetails(String employeeCode) {
        String url = "http://10.0.2.2:3000/get-employee?code=" + employeeCode; // Replace with your actual server URL

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String name = response.getString("name");
                            String department = response.getString("department");
                            String dob = response.getString("dob");

                            String result = "Name: " + name + "\nDepartment: " + department + "\nDOB: " + dob;
                            validationResultTextView.setText(result);

                            // Show visit details layout after validation
                            visitDetailsLayout.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            validationResultTextView.setText("Error parsing employee details");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        validationResultTextView.setText("Error fetching details: " + error.getMessage());
                    }
                });

        // Add the request to the RequestQueue.
        requestQueue.add(jsonObjectRequest);
    }

    private void submitVisitDetails() {
        String employeeCode = employeeCodeEditText.getText().toString().trim();
        String visitDepartment = visitDepartmentSpinner.getSelectedItem().toString();
        String purpose = purposeEditText.getText().toString().trim();
        String meetName = meetNameEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();

        // Get selected time values
        String checkInHour = checkInHourSpinner.getSelectedItem().toString();
        String checkInMinute = checkInMinuteSpinner.getSelectedItem().toString();
        String checkInAmPm = checkInAmPmSpinner.getSelectedItem().toString();
        String checkOutHour = checkOutHourSpinner.getSelectedItem().toString();
        String checkOutMinute = checkOutMinuteSpinner.getSelectedItem().toString();
        String checkOutAmPm = checkOutAmPmSpinner.getSelectedItem().toString();

        // Combine to create final time strings
        String checkInTime = checkInHour + ":" + checkInMinute + " " + checkInAmPm;
        String checkOutTime = checkOutHour + ":" + checkOutMinute + " " + checkOutAmPm;

        if (employeeCode.isEmpty() || purpose.isEmpty() || meetName.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare visit data
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

        String url = "http://10.0.2.2:3000/add-visit"; // Replace with your actual server URL

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, visitData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Assuming the server responds with a success message
                            Toast.makeText(VisitorPortalActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(VisitorPortalActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue.
        requestQueue.add(jsonObjectRequest);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // Format date as needed, e.g., dd/MM/yyyy
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        dateEditText.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}
