<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Transaction History Test</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: #f4f4f4;
            color: #333;
        }
        .container {
            background-color: #fff;
            padding: 25px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            max-width: 600px;
            margin: 30px auto;
        }
        h2 {
            color: #0056b3;
            text-align: center;
            margin-bottom: 25px;
        }
        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
        }
        input[type="text"], input[type="date"] {
            width: calc(100% - 20px);
            padding: 10px;
            margin-bottom: 15px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        button {
            background-color: #007bff;
            color: white;
            padding: 12px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            width: 100%;
            transition: background-color 0.2s ease;
        }
        button:hover {
            background-color: #0056b3;
        }
        #responseMessage {
            margin-top: 20px;
            padding: 15px;
            border-radius: 4px;
            background-color: #e9ecef;
            color: #333;
            text-align: center;
            min-height: 20px;
        }
        .success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>BankLab Transaction History Test (JSP)</h2>
    <form id="transactionForm">
        <label for="connectedId">Connected ID:</label>
        <input type="text" id="connectedId" name="connectedId" value="YOUR_CONNECTED_ID" required>

        <label for="organization">Organization (e.g., 0004 for Kookmin Bank):</label>
        <input type="text" id="organization" name="organization" value="0004" required>

        <label for="account">Account Number:</label>
        <input type="text" id="account" name="account" value="YOUR_ACCOUNT_NUMBER" required>

        <label for="startDate">Start Date (YYYYMMDD):</label>
        <input type="date" id="startDate" name="startDate" required>

        <label for="endDate">End Date (YYYYMMDD):</label>
        <input type="date" id="endDate" name="endDate" required>

        <button type="submit">Fetch & Save Transactions</button>
    </form>
    <div id="responseMessage"></div>
</div>

<script>
    document.getElementById('transactionForm').addEventListener('submit', async function(event) {
        event.preventDefault(); // Prevent default form submission

        const responseMessageDiv = document.getElementById('responseMessage');
        responseMessageDiv.textContent = 'Loading...';
        responseMessageDiv.className = ''; // Clear previous styles

        // Format dates to YYYYMMDD
        const startDate = document.getElementById('startDate').value.replace(/-/g, '');
        const endDate = document.getElementById('endDate').value.replace(/-/g, '');

        const formData = {
            connectedId: document.getElementById('connectedId').value,
            organization: document.getElementById('organization').value,
            account: document.getElementById('account').value,
            startDate: startDate,
            endDate: endDate
        };

        try {
            const response = await fetch('/api/codef/transaction-list', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const result = await response.text(); // Get response as text

            if (response.ok) {
                responseMessageDiv.textContent = 'Success: ' + result;
                responseMessageDiv.classList.add('success');
            } else {
                responseMessageDiv.textContent = 'Error: ' + result;
                responseMessageDiv.classList.add('error');
            }
        } catch (error) {
            responseMessageDiv.textContent = 'Network Error: ' + error.message;
            responseMessageDiv.classList.add('error');
            console.error('Fetch error:', error);
        }
    });

    // Set default dates for convenience
    document.addEventListener('DOMContentLoaded', () => {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');

        document.getElementById('endDate').value = `${year}-${month}-${day}`;

        const threeMonthsAgo = new Date(today);
        threeMonthsAgo.setMonth(today.getMonth() - 3);
        const startYear = three