<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>История прохождения</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .history-container {
            max-width: 900px;
            margin: 20px auto;
            padding: 20px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .history-item {
            padding: 15px;
            margin-bottom: 10px;
            background: #f8f9fa;
            border-left: 4px solid #007bff;
            border-radius: 4px;
        }
        
        .history-item:last-child {
            border-left-color: #28a745;
            background: #e8f5e9;
        }
        
        .step-number {
            font-weight: bold;
            color: #007bff;
            margin-right: 10px;
        }
        
        .node-name {
            font-size: 1.1em;
            font-weight: bold;
            margin-bottom: 5px;
        }
        
        .choice-text {
            color: #6c757d;
            font-style: italic;
            margin-top: 5px;
        }
        
        .timestamp {
            color: #999;
            font-size: 0.9em;
            margin-top: 5px;
        }
        
        .back-button {
            display: inline-block;
            padding: 10px 20px;
            background: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            margin-top: 20px;
        }
        
        .back-button:hover {
            background: #0056b3;
        }
    </style>
</head>
<body>
    <div class="history-container">
        <h1>История прохождения</h1>
        
        <c:if test="${empty history}">
            <p>История пуста</p>
        </c:if>
        
        <c:forEach var="historyItem" items="${history}" varStatus="status">
            <div class="history-item">
                <span class="step-number">Шаг ${historyItem.stepNumber + 1}:</span>
                <div class="node-name">${historyItem.node.title}</div>
                
                <c:if test="${not empty historyItem.choice}">
                    <div class="choice-text">
                        → Выбор: ${historyItem.choice.optionText}
                    </div>
                </c:if>
                
                <div class="timestamp">
                    <fmt:formatDate value="${historyItem.visitedAt}" pattern="dd.MM.yyyy HH:mm:ss"/>
                </div>
            </div>
        </c:forEach>
        
        <a href="${pageContext.request.contextPath}/game?progressId=${sessionId}" class="back-button">
            Вернуться к игре
        </a>
    </div>
</body>
</html>
