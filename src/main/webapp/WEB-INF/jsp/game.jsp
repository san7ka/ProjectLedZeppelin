<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${stage.title} - Лесное приключение</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
</head>
<body>
    <header class="header">
        <div class="container">
            <div class="header-content">
                <h1>Лесное приключение</h1>
                <div class="player-info">
                    <span class="player-name">${playerStats.playerName}</span>
                    <span class="player-stats">Победы: ${playerStats.wins} | Поражения: ${playerStats.losses}</span>
                </div>
            </div>
        </div>
    </header>

    <main class="container">
        <div class="game-container fade-in">
            <div class="game-content">
                <h2 class="stage-title">${stage.title}</h2>
                <div class="quest-description">
                    <p>${stage.description}</p>
                </div>

                <form method="post" action="${pageContext.request.contextPath}/game" class="choices">
                    <c:forEach var="option" items="${stage.options}">
                        <button type="submit" 
                                name="choice" 
                                value="${option.key}" 
                                class="choice-btn">
                            ${option.value}
                        </button>
                    </c:forEach>
                </form>
            </div>
        </div>
    </main>
</body>
</html>
