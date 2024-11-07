const express = require('express');
const admin = require('firebase-admin');
const serviceAccount = require('./welspun-visitor-app-600ba-firebase-adminsdk-xremj-f8a234a367.json');
const cors = require('cors');

// Initialize Firebase Admin SDK
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://welspun-visitor-app-600ba-default-rtdb.firebaseio.com"
});

const db = admin.database();
const employeesRef = db.ref('employees');
const visitsRef = db.ref('visits');

const app = express();
app.use(express.json());  // To parse JSON request bodies
app.use(cors()); // Enable CORS

// API to add a new employee
app.post('/add-employee', (req, res) => {
    const { name, code, dob, department } = req.body;

    // Validate input
    if (!name || !code || !dob || !department) {
        return res.status(400).json({ status: 'error', message: 'All fields are required' });
    }

    const newEmployeeRef = employeesRef.push();
    newEmployeeRef.set({
        name: name,
        code: code,
        dob: dob,
        department: department
    }, (error) => {
        if (error) {
            res.status(500).json({ status: 'error', message: 'Error saving employee: ' + error.message });
        } else {
            res.status(201).json({ status: 'success', message: 'Employee saved successfully' });
        }
    });
});

// API to fetch employees based on department filter
app.get('/get-employees', (req, res) => {
    const departmentFilter = req.query.department || 'ALL';

    let query = employeesRef;
    if (departmentFilter !== 'ALL') {
        query = employeesRef.orderByChild('department').equalTo(departmentFilter);
    }

    query.once('value', (snapshot) => {
        if (snapshot.exists()) {
            const employeeData = snapshot.val();
            const employeeList = Object.values(employeeData);

            res.json({
                status: 'success',
                employees: employeeList
            });
        } else {
            res.status(404).json({ status: 'error', message: 'No employees found' });
        }
    }, (error) => {
        res.status(500).json({ status: 'error', message: 'Error fetching employees: ' + error.message });
    });
});

// API to validate employee code and fetch details
app.get('/get-employee', (req, res) => {
    const employeeCode = req.query.code;

    if (!employeeCode) {
        return res.status(400).json({ status: 'error', message: 'Employee code is required' });
    }

    employeesRef.orderByChild('code').equalTo(employeeCode).once('value', (snapshot) => {
        if (snapshot.exists()) {
            const employeeData = snapshot.val();
            const employeeDetails = Object.values(employeeData)[0];
            res.json({
                status: 'success',
                name: employeeDetails.name,
                department: employeeDetails.department,
                dob: employeeDetails.dob
            });
        } else {
            res.status(404).json({ status: 'error', message: 'Employee not found' });
        }
    }, (error) => {
        res.status(500).json({ status: 'error', message: 'Error fetching employee details: ' + error.message });
    });
});

// API to save visit details
app.post('/add-visit', (req, res) => {
    const { employeeCode, visitDepartment, purpose, date, checkInTime, checkOutTime, meetName } = req.body;

    // Validate input
    if (!employeeCode || !visitDepartment || !purpose || !date || !checkInTime || !checkOutTime || !meetName) {
        return res.status(400).json({ status: 'error', message: 'All fields are required' });
    }

    const newVisitRef = visitsRef.push();
    newVisitRef.set({
        employeeCode: employeeCode,
        visitDepartment: visitDepartment,
        purpose: purpose,
        date: date,
        checkInTime: checkInTime,
        checkOutTime: checkOutTime,
        meetName: meetName
    }, (error) => {
        if (error) {
            res.status(500).json({ status: 'error', message: 'Error saving visit details: ' + error.message });
        } else {
            res.status(201).json({ status: 'success', message: 'Visit details saved successfully' });
        }
    });
});

// API to fetch visit details by employee code, including 'person met'
app.get('/get-visit-details', (req, res) => {
    const employeeCode = req.query.code;

    if (!employeeCode) {
        return res.status(400).json({ status: 'error', message: 'Employee code is required' });
    }

    visitsRef.orderByChild('employeeCode').equalTo(employeeCode).once('value', (snapshot) => {
        if (snapshot.exists()) {
            const visitData = snapshot.val();
            const visitDetails = Object.values(visitData); // Get all visit details for the employee

            // For each visit, fetch the employee's name and combine it with visit details
            employeesRef.orderByChild('code').equalTo(employeeCode).once('value', (employeeSnapshot) => {
                if (employeeSnapshot.exists()) {
                    const employeeData = Object.values(employeeSnapshot.val())[0]; // Get the employee's name
                    const employeeName = employeeData.name;

                    // Attach employee name to each visit detail
                    const updatedVisitDetails = visitDetails.map(visit => {
                        return {
                            name: employeeName,
                            employeeCode: visit.employeeCode,
                            visitDepartment: visit.visitDepartment,
                            meetName: visit.meetName,  // 'person met' field
                            purpose: visit.purpose,
                            date: visit.date,
                            checkInTime: visit.checkInTime,
                            checkOutTime: visit.checkOutTime
                        };
                    });

                    res.json(updatedVisitDetails); // Send back the updated visit details with the employee name
                } else {
                    res.status(404).json({ status: 'error', message: 'Employee not found' });
                }
            });
        } else {
            res.status(404).json({ status: 'error', message: 'No visits found for this employee' });
        }
    }, (error) => {
        res.status(500).json({ status: 'error', message: 'Error fetching visit details: ' + error.message });
    });
});

// Start the server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
