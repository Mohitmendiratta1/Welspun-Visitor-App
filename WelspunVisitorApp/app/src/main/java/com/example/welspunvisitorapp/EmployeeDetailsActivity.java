package com.example.welspunvisitorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class EmployeeDetailsActivity extends AppCompatActivity {

    private Spinner departmentSpinner;
    private Button submitButton;
    private TableLayout employeeTableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_details);

        departmentSpinner = findViewById(R.id.departmentSpinner);
        submitButton = findViewById(R.id.submitButton);
        employeeTableLayout = findViewById(R.id.employeeTableLayout);

        // Set up the department filter Spinner
        String[] departments = {"ALL", "WML", "WSL", "WDI", "WIL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpinner.setAdapter(adapter);

        // Handle submit button click
        submitButton.setOnClickListener(v -> {
            String selectedDepartment = departmentSpinner.getSelectedItem().toString();
            fetchEmployees(selectedDepartment);
        });
    }

    // Fetch employees from the backend based on the selected department
    private void fetchEmployees(String department) {
        String url = "http://10.0.2.2:3000/get-employees?department=" + department; // Modify URL if needed

        // Make the API call
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getString("status").equals("success")) {
                        JSONArray employeeArray = jsonResponse.getJSONArray("employees");

                        // Clear previous table rows
                        employeeTableLayout.removeViewsInLayout(1, employeeTableLayout.getChildCount() - 1);

                        // Loop through the employee data and populate the table
                        for (int i = 0; i < employeeArray.length(); i++) {
                            JSONObject employeeData = employeeArray.getJSONObject(i);
                            String name = employeeData.getString("name");
                            String code = employeeData.getString("code");
                            String dob = employeeData.getString("dob");
                            String dept = employeeData.getString("department");

                            // Create a new TableRow
                            TableRow tableRow = new TableRow(EmployeeDetailsActivity.this);
                            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.WRAP_CONTENT));

                            // Add TextViews for each employee field
                            TextView nameTextView = new TextView(EmployeeDetailsActivity.this);
                            nameTextView.setText(name);
                            nameTextView.setPadding(8, 8, 8, 8);
                            tableRow.addView(nameTextView);

                            TextView codeTextView = new TextView(EmployeeDetailsActivity.this);
                            codeTextView.setText(code);
                            codeTextView.setPadding(8, 8, 8, 8);
                            tableRow.addView(codeTextView);

                            TextView dobTextView = new TextView(EmployeeDetailsActivity.this);
                            dobTextView.setText(dob);
                            dobTextView.setPadding(8, 8, 8, 8);
                            tableRow.addView(dobTextView);

                            TextView deptTextView = new TextView(EmployeeDetailsActivity.this);
                            deptTextView.setText(dept);
                            deptTextView.setPadding(8, 8, 8, 8);
                            tableRow.addView(deptTextView);

                            // Add the TableRow to the TableLayout
                            employeeTableLayout.addView(tableRow);
                        }
                    } else {
                        Toast.makeText(EmployeeDetailsActivity.this, "No employees found.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(EmployeeDetailsActivity.this, "Error fetching employee data", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EmployeeDetailsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(this).add(stringRequest);
    }
}