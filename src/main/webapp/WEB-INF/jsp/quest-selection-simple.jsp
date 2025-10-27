<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Выбор квеста - Text Quest Game</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        .quest-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin: 20px 0;
        }
        .quest-card {
            background: white;
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
        .quest-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 10px;
            color: #333;
        }
        .quest-description {
            color: #666;
            margin-bottom: 15px;
        }
        .quest-meta {
            font-size: 13px;
            color: #999;
            margin-bottom: 15px;
        }
        .btn {
            padding: 10px 20px;
            background: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            display: inline-block;
        }
        .btn:hover {
            background: #0056b3;
        }
        h1 {
            text-align: center;
            color: #ffffff;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
            margin-bottom: 1.5rem;
        }
        
        p {
            color: rgba(255, 255, 255, 0.95);
            text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Выберите квест для игры</h1>
        
        <c:choose>
            <c:when test="${not empty user}">
                <p style="text-align: center;">Добро пожаловать, ${user.username}!</p>
            </c:when>
            <c:otherwise>
                <p style="text-align: center;">
                    <a href="${pageContext.request.contextPath}/auth/login" style="color: #ffd700; text-decoration: underline;">Войдите</a> или 
                    <a href="${pageContext.request.contextPath}/auth/register" style="color: #ffd700; text-decoration: underline;">зарегистрируйтесь</a> для сохранения прогресса
                </p>
            </c:otherwise>
        </c:choose>
        
        <c:choose>
            <c:when test="${not empty quests}">
                <div class="quest-grid">
                    <c:forEach var="quest" items="${quests}">
                        <c:if test="${quest.isPlayable()}">
                            <div class="quest-card">
                            <h3 class="quest-title">${quest.title}</h3>
                            <p class="quest-description">${quest.description}</p>
                            <div class="quest-meta">
                                Автор: ${quest.author.username} | Этапов: ${quest.stageCount}
                            </div>
                            <a href="${pageContext.request.contextPath}/game?questId=${quest.id}" class="btn">
                                Играть
                            </a>
                        </div>
                        </c:if>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p style="text-align: center;">Квестов пока нет.</p>
            </c:otherwise>
        </c:choose>
        
        <div style="text-align: center; margin-top: 30px;">
            <a href="${pageContext.request.contextPath}/welcome" class="btn" style="background: #6c757d;">
                Вернуться на главную
            </a>
        </div>
    </div>
</body>
</html>
