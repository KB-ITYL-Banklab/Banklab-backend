<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>거래 내역 테스트</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f4; color: #333; }
        .container { background-color: #fff; padding: 25px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); max-width: 600px; margin: 30px auto; }
        h2 { color: #0056b3; text-align: center; margin-bottom: 25px; }
        label { display: block; margin-bottom: 8px; font-weight: bold; }
        input[type="text"], input[type="date"] { width: calc(100% - 20px); padding: 10px; margin-bottom: 15px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
        button { background-color: #007bff; color: white; padding: 12px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; width: 100%; transition: background-color 0.2s ease; }
        button:hover { background-color: #0056b3; }
        #responseMessage { margin-top: 20px; padding: 15px; border-radius: 4px; background-color: #e9ecef; color: #333; text-align: center; min-height: 20px; }
        .success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
    </style>
</head>
<body>
<div class="container">
    <h2>BankLab 거래 내역 테스트 (JSP)</h2>
    <form id="transactionForm">
        <label for="connectedId">Connected ID:</label>
        <input type="text" id="connectedId" name="connectedId" value="6KluL2UakbAau6ATaNFKKn" required>

        <label for="organization">기관 코드 (예: 국민은행 0004):</label>
        <input type="text" id="organization" name="organization" value="0088" required>

        <label for="account">계좌 번호:</label>
        <input type="text" id="account" name="account" value="110568497184" required>


        <label for="orderBy">최신순:</label>
        <input type="text" id="orderBy" name="orderBy" value="0" required>

        <label for="startDate">시작일 (YYYYMMDD):</label>
        <input type="date" id="startDate" name="startDate" required>

        <label for="endDate">종료일 (YYYYMMDD):</label>
        <input type="date" id="endDate" name="endDate" required>



        <button type="submit">거래 내역 불러와 저장하기</button>
    </form>
    <div id="responseMessage"></div>
</div>

<script>
    document.getElementById('transactionForm').addEventListener('submit', async function(event) {
        event.preventDefault(); // 폼 기본 제출 동작 방지

        const responseMessageDiv = document.getElementById('responseMessage');
        responseMessageDiv.textContent = '불러오는 중...';
        responseMessageDiv.className = ''; // 이전 스타일 제거

        // 날짜 형식 YYYYMMDD로 변환
        const startDate = document.getElementById('startDate').value.replace(/-/g, '');
        const endDate = document.getElementById('endDate').value.replace(/-/g, '');

        const formData = {
            connectedId: document.getElementById('connectedId').value,
            organization: document.getElementById('organization').value,
            account: document.getElementById('account').value,
            startDate: startDate,
            endDate: endDate,
            orderBy: document.getElementById('orderBy').value
        };

        try {
            const response = await fetch('/api/codef/transaction-list', {
                method: 'POST',
                headers: {

                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const result = await response; // 응답을 텍스트로 받음

            if (response.ok) {
                responseMessageDiv.textContent = '성공: ' + result;
                responseMessageDiv.classList.add('success');
            } else {
                responseMessageDiv.textContent = '오류: ' + result;
                responseMessageDiv.classList.add('error');
            }
        } catch (error) {
            responseMessageDiv.textContent = '네트워크 오류: ' + error.message;
            responseMessageDiv.classList.add('error');
            console.error('Fetch 오류:', error);
        }
    });

    // 편의를 위해 기본 날짜 설정 (페이지 로드 시)
    document.addEventListener('DOMContentLoaded', () => {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');

        document.getElementById('endDate').value = year + '-' + month + '-' + day;


        const threeMonthsAgo = new Date(today);
        threeMonthsAgo.setMonth(today.getMonth() - 3);
        const startYear = threeMonthsAgo.getFullYear();
        const startMonth = String(threeMonthsAgo.getMonth() + 1).padStart(2, '0');
        const startDay = String(threeMonthsAgo.getDate()).padStart(2, '0');
        document.getElementById('startDate').value = startYear + '-' + startMonth + '-' + startDay;

    });

</script>
</body>
</html>