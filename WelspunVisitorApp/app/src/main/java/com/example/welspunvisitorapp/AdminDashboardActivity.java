package com.example.welspunvisitorapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button newEmployeeButton;
    private Button employeeDetailsButton;
    private Button employeeDetailsByDepartmentButton;
    private Button editEmployeeButton;
    private Button deleteEmployeeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard); // Make sure the layout is set correctly

        // Initialize the buttons
        newEmployeeButton = findViewById(R.id.newEmployeeButton);
        employeeDetailsButton = findViewById(R.id.employeeDetailsButton);
        employeeDetailsByDepartmentButton = findViewById(R.id.employeeDetailsByDepartmentButton);
        editEmployeeButton = findViewById(R.id.editEmployeeButton);
        deleteEmployeeButton = findViewById(R.id.deleteEmployeeButton);

        // Set OnClickListener for New Employee button
        newEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, NewEmployeeActivity.class);
                startActivity(intent); // Start NewEmployeeActivity
            }
        });

        // Set OnClickListener for Employee Details button
        employeeDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, VisitorEmployeeDetailsActivity.class);
                startActivity(intent); // Start VisitorEmployeeDetailsActivity
            }
        });

        // Set OnClickListener for Employee Details by Department button
        employeeDetailsByDepartmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, EmployeeDetailsActivity.class);
                startActivity(intent); // Start EmployeeDetailsActivity
            }
        });

        // Set OnClickListener for Edit Employee button
        editEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, EditEmployeeActivity.class);
                startActivity(intent); // Start EditEmployeeActivity
            }
        });

        // Set OnClickListener for Delete Employee button
        deleteEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, DeleteEmployeeActivity.class);
                startActivity(intent);
            }
        });
    }
}
