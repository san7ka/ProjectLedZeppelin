<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Результат - Лесное приключение</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
</head>
<body>
    <header class="header">
        <div class="container">
            <h1>Лесное приключение</h1>
        </div>
    </header>

    <main class="container">
        <div class="game-container fade-in">
            <div class="result-screen ${isWin ? 'win' : 'lose'}">
                <c:choose>
                    <c:when test="${isWin}">
                        <div class="result-icon">🏆</div>
                        <h2>Поздравляем с победой!</h2>
                        <p class="result-text">Вы успешно прошли квест и выбрались из леса с сокровищами!</p>
                    </c:when>
                    <c:otherwise>
                        <div class="result-icon">💀</div>
                        <h2>Конец игры</h2>
                        <p class="result-text">К сожалению, ваше приключение закончилось неудачей...</p>
                    </c:otherwise>
                </c:choose>

                <div class="stats mt-4">
                    <h3>Статистика игрока</h3>
                    <div class="stat-item">
                        <span class="stat-label">Имя:</span>
                        <span class="stat-value">${playerStats.playerName}</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">Сыграно игр:</span>
                        <span class="stat-value">${playerStats.gamesPlayed}</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">Побед:</span>
                        <span class="stat-value">${playerStats.wins}</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">Поражений:</span>
                        <span class="stat-value">${playerStats.losses}</span>
                    </div>
                </div>

                <div class="action-buttons">
                    <form method="post" action="${pageContext.request.contextPath}/result" class="d-inline">
                        <button type="submit" class="btn btn-success">Играть снова</button>
                    </form>
                    <a href="${pageContext.request.contextPath}/welcome" class="btn btn-warning">На главную</a>
                </div>
            </div>
        </div>
    </main>
</body>
</html>
