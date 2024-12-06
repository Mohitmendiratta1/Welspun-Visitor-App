package com.example.welspunvisitorapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EditEmployeeActivity extends AppCompatActivity {

    private EditText employeeCodeEditText;
    private Button submitButton;
    private Button editButton;
    private TableLayout visitDetailsTable;
    private RequestQueue requestQueue;
    private String selectedEmployeeCode = "";
    private String selectedVisitorId = "";  // Added to hold the visitor ID
    private String selectedEmployeeName = "";  // Added for passing employee name
    private String selectedVisitDepartment = "";
    private String selectedPurposeOfVisit = "";  // Added for Purpose of Visit
    private String selectedPersonMet = "";  // Added for Person Met
    private String selectedVisitDate = "";
    private String selectedCheckInTime = "";
    private String selectedCheckOutTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_employee);

        employeeCodeEditText = findViewById(R.id.employeeCodeEditText);
        submitButton = findViewById(R.id.submitButton);
        editButton = findViewById(R.id.editButton);
        visitDetailsTable = findViewById(R.id.visitDetailsTable);
        requestQueue = Volley.newRequestQueue(this);

        visitDetailsTable.setVisibility(View.GONE);
        editButton.setVisibility(View.GONE);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String employeeCode = employeeCodeEditText.getText().toString().trim();
                if (!employeeCode.isEmpty()) {
                    fetchVisitDetails(employeeCode);
                } else {
                    Toast.makeText(EditEmployeeActivity.this, "Please enter an employee code", Toast.LENGTH_SHORT).show();
                }
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectedEmployeeCode.isEmpty() && !selectedVisitorId.isEmpty()) {
                    // Create an Intent to pass the selected data to UpdateEmployeeActivity
                    Intent intent = new Intent(EditEmployeeActivity.this, UpdateEmployeeActivity.class);
                    // Pass the selected visit details to the next activity
                    intent.putExtra("visitorId", selectedVisitorId);  // Add visitorId here
                    intent.putExtra("employeeCode", selectedEmployeeCode);
                    intent.putExtra("employeeName", selectedEmployeeName);  // Add employee name here
                    intent.putExtra("visitDepartment", selectedVisitDepartment);
                    intent.putExtra("purpose", selectedPurposeOfVisit);  // Add purpose of visit here
                    intent.putExtra("meetName", selectedPersonMet);  // Pass meetName here
                    intent.putExtra("visitDate", selectedVisitDate);
                    intent.putExtra("checkInTime", selectedCheckInTime);
                    intent.putExtra("checkOutTime", selectedCheckOutTime);
                    // Start the activity to update the visit details
                    startActivity(intent);
                } else {
                    Toast.makeText(EditEmployeeActivity.this, "Please select a visit to edit", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchVisitDetails(String employeeCode) {
        String url = "http://10.0.2.2:3000/get-visit-details?code=" + employeeCode;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        visitDetailsTable.removeViews(1, visitDetailsTable.getChildCount() - 1);

                        if (response.length() == 0) {
                            Toast.makeText(EditEmployeeActivity.this, "No visit details found for this employee code", Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject visitDetail = response.getJSONObject(i);
                                    TableRow tableRow = new TableRow(EditEmployeeActivity.this);

                                    CheckBox selectCheckBox = new CheckBox(EditEmployeeActivity.this);
                                    selectCheckBox.setTag(visitDetail.optString("visitorId", "N/A"));
                                    tableRow.addView(selectCheckBox);

                                    TextView visitorIdTextView = createTextView(visitDetail.optString("visitorId", "No ID"));
                                    tableRow.addView(visitorIdTextView);

                                    TextView nameTextView = createTextView(visitDetail.optString("name", "N/A"));
                                    tableRow.addView(nameTextView);

                                    TextView codeTextView = createTextView(visitDetail.optString("employeeCode", "N/A"));
                                    tableRow.addView(codeTextView);

                                    TextView departmentTextView = createTextView(visitDetail.optString("visitDepartment", "N/A"));
                                    tableRow.addView(departmentTextView);

                                    TextView purposeOfVisitTextView = createTextView(visitDetail.optString("purpose", "N/A"));  // Display Purpose of Visit
                                    tableRow.addView(purposeOfVisitTextView);

                                    TextView personMetTextView = createTextView(visitDetail.optString("meetName", "N/A"));  // Display Meet Name
                                    tableRow.addView(personMetTextView);

                                    TextView dateTextView = createTextView(visitDetail.optString("date", "N/A"));
                                    tableRow.addView(dateTextView);

                                    TextView checkInTextView = createTextView(visitDetail.optString("checkInTime", "N/A"));
                                    tableRow.addView(checkInTextView);

                                    TextView checkOutTextView = createTextView(visitDetail.optString("checkOutTime", "N/A"));
                                    tableRow.addView(checkOutTextView);

                                    visitDetailsTable.addView(tableRow);

                                    selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                        if (isChecked) {
                                            selectedVisitorId = (String) buttonView.getTag();
                                            selectedEmployeeCode = visitDetail.optString("employeeCode", "N/A");
                                            selectedEmployeeName = visitDetail.optString("name", "N/A");
                                            selectedVisitDepartment = visitDetail.optString("visitDepartment", "N/A");
                                            selectedPurposeOfVisit = visitDetail.optString("purpose", "N/A");
                                            selectedPersonMet = visitDetail.optString("meetName", "N/A");  // Capture meetName
                                            selectedVisitDate = visitDetail.optString("date", "N/A");
                                            selectedCheckInTime = visitDetail.optString("checkInTime", "N/A");
                                            selectedCheckOutTime = visitDetail.optString("checkOutTime", "N/A");
                                            editButton.setVisibility(View.VISIBLE);
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(EditEmployeeActivity.this, "Error parsing visit details", Toast.LENGTH_SHORT).show();
                                }
                            }
                            visitDetailsTable.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditEmployeeActivity.this, "Error fetching visit details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        return textView;
    }
}
