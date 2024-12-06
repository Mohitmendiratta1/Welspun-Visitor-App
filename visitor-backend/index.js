const express = require('express');
const admin = require('firebase-admin');
const serviceAccount = require('./welspun-visitor-app-600ba-firebase-adminsdk-xremj-109d3d9ac6.json');
const cors = require('cors');

// Initialize Firebase Admin SDK
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://welspun-visitor-app-600ba-default-rtdb.firebaseio.com"
});

const db = admin.database();
const employeesRef = db.ref('employees');
const visitsRef = db.ref('visits');
const visitIdCounterRef = db.ref('visitIdCounter'); // Reference for visit ID counter

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

    // Query Firebase for the employee with the matching code
    employeesRef.orderByChild('code').equalTo(employeeCode).once('value', (snapshot) => {
        if (snapshot.exists()) {
            const employeeData = snapshot.val();
            const employeeDetails = Object.values(employeeData)[0]; // Retrieve the first matching employee

            // Check if the employee is flagged
            if (employeeDetails.flag === true) {
                return res.status(403).json({
                    status: 'error',
                    message: 'Employee is flagged and cannot be validated.'
                });
            }

            // Return employee details if validation passes
            return res.json({
                status: 'success',
                name: employeeDetails.name,
                department: employeeDetails.department,
                dob: employeeDetails.dob,
                flag: employeeDetails.flag
            });
        } else {
            return res.status(404).json({ status: 'error', message: 'Employee not found' });
        }
    }, (error) => {
        return res.status(500).json({ status: 'error', message: 'Error fetching employee details: ' + error.message });
    });
});


// API to save visit details
app.post('/add-visit', async (req, res) => {
    const { employeeCode, visitDepartment, purpose, date, checkInTime, checkOutTime, meetName } = req.body;

    // Validate input
    if (!employeeCode || !visitDepartment || !purpose || !date || !checkInTime || !checkOutTime || !meetName) {
        return res.status(400).json({ status: 'error', message: 'All fields are required' });
    }

    try {
        // Get the current visit ID counter
        const visitIdSnapshot = await visitIdCounterRef.once('value');
        let visitId = visitIdSnapshot.val() || 0; // Default to 0 if counter is not set

        // Increment the visit ID, wrap it around at 1000
        visitId = (visitId + 1) % 1001;

        // Save the new visit ID counter back to Firebase
        await visitIdCounterRef.set(visitId);

        // Add the visit details
        const newVisitRef = visitsRef.push();
        newVisitRef.set({
            visitorId: visitId, // Add the generated visitor ID
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
                res.status(201).json({
                    status: 'success',
                    message: 'Visit details saved successfully',
                    visitorId: visitId // Send back the visitorId to confirm it was generated and saved
                });
            }
        });

    } catch (error) {
        res.status(500).json({ status: 'error', message: 'Error generating visit ID: ' + error.message });
    }
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

                    // Attach employee name and visitorId to each visit detail
                    const updatedVisitDetails = visitDetails.map(visit => {
                        return {
                            visitorId: visit.visitorId,  // This should be included if saved properly
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

                    res.json(updatedVisitDetails); // Send back the updated visit details with the employee name and visitorId
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

// API to update visit details
app.put('/update-visit-details', (req, res) => {
    console.log('Received request:', req.body);
    const { visitorId, employeeCode, visitDepartment, purpose, date, checkInTime, checkOutTime, meetName } = req.body;

    if (!visitorId || !employeeCode) {
        return res.status(400).json({ status: 'error', message: 'Visitor ID and Employee Code are required' });
    }

    const visitsRef = admin.database().ref('visits');

    // Debugging: log the received visitorId and employeeCode
    console.log('Received visitorId:', visitorId);
    console.log('Received employeeCode:', employeeCode);

    // Query to find the visit with the matching visitorId
    visitsRef.orderByChild('visitorId').equalTo(parseInt(visitorId)).once('value', (snapshot) => {
        console.log('Snapshot data:', snapshot.val());  // Debugging: log the fetched data

        // If the snapshot data is null, log and return an error
        if (!snapshot.exists()) {
            console.log(`No visits found for Visitor ID: ${visitorId}`);
            return res.status(404).json({ status: 'error', message: `No visits found for Visitor ID: ${visitorId}` });
        }

        let visitData = snapshot.val();
        let visitKey;

        // Debugging: log the visitData object to understand the structure
        console.log('Visit data from Firebase:', visitData);

        // Find the specific visit by visitorId and check for employeeCode match
        for (const key in visitData) {
            const currentVisit = visitData[key];
            console.log(`Checking visit ${key}:`, currentVisit);
            console.log(`Comparing visitorId: ${currentVisit.visitorId} with ${visitorId}`);
            console.log(`Comparing employeeCode: ${currentVisit.employeeCode} with ${employeeCode}`);

            // Adjust comparison to handle string/number type inconsistencies
            if (currentVisit.visitorId === parseInt(visitorId) && currentVisit.employeeCode === employeeCode) {
                visitKey = key;
                break;
            }
        }

        // If visitKey is not found, log that the visit was not found
        if (!visitKey) {
            console.log(`No matching visit found for visitorId ${visitorId} and employeeCode ${employeeCode}`);
            return res.status(404).json({ status: 'error', message: `Visit with Visitor ID ${visitorId} and Employee Code ${employeeCode} not found` });
        }

        // Debugging: log the visitKey that was found
        console.log('Found matching visitKey:', visitKey);

        // Prepare the update data
        const updateData = {};
        if (visitDepartment) updateData.visitDepartment = visitDepartment;
        if (purpose) updateData.purpose = purpose;
        if (date) updateData.date = date;
        if (checkInTime) updateData.checkInTime = checkInTime;
        if (checkOutTime) updateData.checkOutTime = checkOutTime;
        if (meetName) updateData.meetName = meetName;

        // Debugging: Check what data is being updated
        console.log('Update Data:', updateData);

        const visitRefToUpdate = visitsRef.child(visitKey);
        visitRefToUpdate.update(updateData, (error) => {
            if (error) {
                console.error('Error updating visit:', error);
                return res.status(500).json({ status: 'error', message: 'Error updating visit details: ' + error.message });
            } else {
                console.log('Successfully updated visit');
                return res.status(200).json({
                    status: 'success',
                    message: 'Visit details updated successfully',
                    updatedData: updateData
                });
            }
        });
    });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
