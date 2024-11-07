package com.example.welspunvisitorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class VisitorEmployeeDetailsActivity extends AppCompatActivity {

    private EditText employeeCodeEditText;
    private Button submitButton;
    private TableLayout visitDetailsTable;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_employee_details);

        employeeCodeEditText = findViewById(R.id.employeeCodeEditText);
        submitButton = findViewById(R.id.submitButton);
        visitDetailsTable = findViewById(R.id.visitDetailsTable);
        requestQueue = Volley.newRequestQueue(this);

        // Initially hide the table
        visitDetailsTable.setVisibility(View.GONE);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String employeeCode = employeeCodeEditText.getText().toString().trim();
                if (!employeeCode.isEmpty()) {
                    fetchVisitDetails(employeeCode);
                } else {
                    Toast.makeText(VisitorEmployeeDetailsActivity.this, "Please enter an employee code", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchVisitDetails(String employeeCode) {
        String url = "http://10.0.2.2:3000/get-visit-details?code=" + employeeCode; // Replace with your server URL

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Clear previous rows except for the header
                        visitDetailsTable.removeViews(1, visitDetailsTable.getChildCount() - 1);

                        if (response.length() == 0) {
                            Toast.makeText(VisitorEmployeeDetailsActivity.this, "No visit details found for this employee code", Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject visitDetail = response.getJSONObject(i);
                                    TableRow tableRow = new TableRow(VisitorEmployeeDetailsActivity.this);

                                    // Create TextViews for each field and add to TableRow
                                    TextView nameTextView = createTextView(visitDetail.optString("name", "N/A"));
                                    tableRow.addView(nameTextView);

                                    TextView codeTextView = createTextView(visitDetail.optString("employeeCode", "N/A"));
                                    tableRow.addView(codeTextView);

                                    TextView departmentTextView = createTextView(visitDetail.optString("visitDepartment", "N/A"));
                                    tableRow.addView(departmentTextView);

                                    TextView personMetTextView = createTextView(visitDetail.optString("meetName", "N/A"));
                                    tableRow.addView(personMetTextView);

                                    TextView dateTextView = createTextView(visitDetail.optString("date", "N/A"));
                                    tableRow.addView(dateTextView);

                                    TextView checkInTextView = createTextView(visitDetail.optString("checkInTime", "N/A"));
                                    tableRow.addView(checkInTextView);

                                    TextView checkOutTextView = createTextView(visitDetail.optString("checkOutTime", "N/A"));
                                    tableRow.addView(checkOutTextView);

                                    // Add TableRow to TableLayout
                                    visitDetailsTable.addView(tableRow);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(VisitorEmployeeDetailsActivity.this, "Error parsing visit details", Toast.LENGTH_SHORT).show();
                                }
                            }

                            // Show the table after data is loaded
                            visitDetailsTable.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(VisitorEmployeeDetailsActivity.this, "Error fetching visit details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16); // Padding for better readability
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        return textView;
    }
}