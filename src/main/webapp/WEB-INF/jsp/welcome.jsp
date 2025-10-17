<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Лесное приключение</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
</head>
<body>
    <header class="header">
        <h1>Лесное приключение</h1>
        <nav class="main-nav">
            <a href="${pageContext.request.contextPath}/editor" class="nav-link">Редактор квестов</a>
        </nav>
    </header>

    <main class="container">
        <div class="game-container fade-in">
            <div class="game-content">
                <h2 class="text-center">Добро пожаловать в игру!</h2>
                <div class="story">
                    <p class="game-text">
                        Вы очнулись на опушке таинственного леса. Вокруг ни души, только шелест листьев и далёкие крики птиц. 
                        Впереди расходятся две тропинки. Кажется, пришло время сделать выбор...
                    </p>
                    <p class="game-text">
                        В этой игре вы окажетесь в загадочном лесу, полном тайн и опасностей. 
                        Каждое ваше решение будет влиять на ход событий и вашу судьбу.
                    </p>
                    <p class="game-text">
                        Введите ваше имя, чтобы начать игру:
                    </p>
                </div>

                <form action="${pageContext.request.contextPath}/welcome" method="post" class="mt-3">
                    <div class="form-group">
                        <input type="text" 
                               id="playerName" 
                               name="playerName" 
                               class="form-control" 
                               placeholder="Введите ваше имя" 
                               required 
                               value="${not empty playerStats.playerName ? playerStats.playerName : ''}">
                    </div>
                    <button type="submit" class="btn btn-block mt-3">Начать приключение</button>
                </form>
            </div>

            <c:if test="${playerStats.gamesPlayed > 0}">
                <div class="stats mt-4">
                    <h3>Ваша статистика</h3>
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
            </c:if>
        </div>
    </main>
</body>
</html>
