<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dt" uri="http://javarush.com/jsp/functions" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Результат игры - Text Quest Game</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
    <style>
        .result-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px 0;
            text-align: center;
        }
        
        .result-title {
            font-size: 36px;
            font-weight: 700;
            margin-bottom: 10px;
        }
        
        .result-subtitle {
            font-size: 18px;
            opacity: 0.9;
        }
        
        .result-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 40px 20px;
        }
        
        .result-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
            overflow: hidden;
            margin-bottom: 30px;
        }
        
        .result-screen {
            padding: 60px 40px;
            text-align: center;
            position: relative;
        }
        
        .result-screen.win {
            background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
        }
        
        .result-screen.lose {
            background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
        }
        
        .result-icon {
            font-size: 80px;
            margin-bottom: 20px;
            animation: bounce 2s infinite;
        }
        
        @keyframes bounce {
            0%, 20%, 50%, 80%, 100% {
                transform: translateY(0);
            }
            40% {
                transform: translateY(-10px);
            }
            60% {
                transform: translateY(-5px);
            }
        }
        
        .result-title-text {
            font-size: 32px;
            font-weight: 700;
            margin-bottom: 15px;
            color: #333;
        }
        
        .result-text {
            font-size: 18px;
            color: #666;
            line-height: 1.6;
            margin-bottom: 30px;
        }
        
        .quest-info {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 30px;
            text-align: left;
        }
        
        .quest-info h3 {
            margin: 0 0 15px 0;
            color: #333;
            font-size: 20px;
        }
        
        .quest-meta {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
        }
        
        .meta-item {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #e9ecef;
        }
        
        .meta-label {
            font-weight: 500;
            color: #666;
        }
        
        .meta-value {
            color: #333;
        }
        
        .quest-author {
            color: #007bff;
            font-weight: 500;
        }
        
        .stats-section {
            background: #f8f9fa;
            padding: 30px;
            border-top: 1px solid #e9ecef;
        }
        
        .stats-title {
            font-size: 24px;
            font-weight: 600;
            margin-bottom: 20px;
            color: #333;
            text-align: center;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-item {
            background: white;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
        
        .stat-number {
            font-size: 28px;
            font-weight: 700;
            color: #007bff;
            display: block;
            margin-bottom: 5px;
        }
        
        .stat-label {
            font-size: 14px;
            color: #666;
        }
        
        .actions-section {
            padding: 30px;
            text-align: center;
            background: white;
        }
        
        .actions-title {
            font-size: 20px;
            font-weight: 600;
            margin-bottom: 20px;
            color: #333;
        }
        
        .action-buttons {
            display: flex;
            gap: 15px;
            justify-content: center;
            flex-wrap: wrap;
        }
        
        .btn {
            padding: 15px 30px;
            background: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 500;
            transition: all 0.3s ease;
            border: none;
            cursor: pointer;
        }
        
        .btn:hover {
            background: #0056b3;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }
        
        .btn-success {
            background: #28a745;
        }
        
        .btn-success:hover {
            background: #218838;
        }
        
        .btn-secondary {
            background: #6c757d;
        }
        
        .btn-secondary:hover {
            background: #545b62;
        }
        
        .btn-outline {
            background: transparent;
            border: 2px solid #007bff;
            color: #007bff;
        }
        
        .btn-outline:hover {
            background: #007bff;
            color: white;
        }
        
        .fade-in {
            animation: fadeIn 0.8s ease-in;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(30px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        .anonymous-notice {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 30px;
            text-align: center;
        }
        
        .anonymous-notice h3 {
            margin: 0 0 10px 0;
            color: #856404;
        }
        
        .anonymous-notice p {
            margin: 0;
            color: #856404;
        }
        
        .final-stage {
            background: #e3f2fd;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            border-left: 4px solid #2196f3;
        }
        
        .final-stage h4 {
            margin: 0 0 10px 0;
            color: #1976d2;
        }
        
        .final-stage p {
            margin: 0;
            color: #555;
            line-height: 1.5;
        }
    </style>
</head>
<body>
    <div class="result-header">
        <div class="container">
            <h1 class="result-title">
                <c:choose>
                    <c:when test="${isWin}">🏆 Поздравляем!</c:when>
                    <c:otherwise>💀 Игра завершена</c:otherwise>
                </c:choose>
            </h1>
            <p class="result-subtitle">
                <c:choose>
                    <c:when test="${isWin}">Вы успешно прошли квест!</c:when>
                    <c:otherwise>К сожалению, ваше приключение закончилось неудачей</c:otherwise>
                </c:choose>
            </p>
        </div>
    </div>

    <main class="result-container">
        <div class="result-card fade-in">
            <div class="result-screen ${isWin ? 'win' : 'lose'}">
                <div class="result-icon">
                    <c:choose>
                        <c:when test="${isWin}">🏆</c:when>
                        <c:otherwise>💀</c:otherwise>
                    </c:choose>
                </div>
                
                <h2 class="result-title-text">
                    <c:choose>
                        <c:when test="${isWin}">ПОБЕДА!</c:when>
                        <c:otherwise>ПОРАЖЕНИЕ</c:otherwise>
                    </c:choose>
                </h2>
                
                <p class="result-text">
                    <c:choose>
                        <c:when test="${isWin}">
                            Отличная работа! Вы успешно прошли все испытания и достигли цели.
                        </c:when>
                        <c:otherwise>
                            Не расстраивайтесь! Каждая неудача - это опыт для будущих побед.
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
            
            <c:if test="${not empty quest}">
                <div class="quest-info">
                    <h3>Информация о квесте</h3>
                    <div class="quest-meta">
                        <div class="meta-item">
                            <span class="meta-label">Название:</span>
                            <span class="meta-value">${quest.title}</span>
                        </div>
                        <c:if test="${not empty quest.author}">
                            <div class="meta-item">
                                <span class="meta-label">Автор:</span>
                                <span class="meta-value quest-author">${quest.author.username}</span>
                            </div>
                        </c:if>
                        <c:if test="${not empty quest.stages}">
                            <div class="meta-item">
                                <span class="meta-label">Этапов:</span>
                                <span class="meta-value">${quest.stageCount}</span>
                            </div>
                        </c:if>
                        <c:if test="${not empty gameDuration}">
                            <div class="meta-item">
                                <span class="meta-label">Время игры:</span>
                                <span class="meta-value">${gameDuration}</span>
                            </div>
                        </c:if>
                        <c:if test="${not empty progress}">
                            <div class="meta-item">
                                <span class="meta-label">Завершено:</span>
                                <span class="meta-value">
                                    ${progress.completedAt != null ? dt:format(progress.completedAt, "dd.MM.yyyy HH:mm") : "Не завершено"}
                                </span>
                            </div>
                        </c:if>
                    </div>
                </div>
            </c:if>
            
            <c:if test="${not empty finalStage}">
                <div class="final-stage">
                    <h4>Финальный этап: ${finalStage.title}</h4>
                    <p>${finalStage.description}</p>
                </div>
            </c:if>
            
            <c:if test="${not empty isAnonymous}">
                <div class="anonymous-notice">
                    <h3>Зарегистрируйтесь для сохранения прогресса!</h3>
                    <p>Создайте аккаунт, чтобы сохранять результаты игр и участвовать в рейтингах</p>
                </div>
            </c:if>
            
            <c:if test="${not empty user}">
                <div class="stats-section">
                    <h3 class="stats-title">Ваша статистика</h3>
                    <div class="stats-grid">
                        <div class="stat-item">
                            <span class="stat-number">${totalGames}</span>
                            <div class="stat-label">Всего игр</div>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">${completedGames}</span>
                            <div class="stat-label">Завершено</div>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">${wonGames}</span>
                            <div class="stat-label">Побед</div>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">${completedGames - wonGames}</span>
                            <div class="stat-label">Поражений</div>
                        </div>
                    </div>
                </div>
            </c:if>
            
            <c:if test="${not empty playerStats and empty user}">
                <div class="stats-section">
                    <h3 class="stats-title">Статистика игрока</h3>
                    <div class="stats-grid">
                        <div class="stat-item">
                            <span class="stat-number">${playerStats.playerName}</span>
                            <div class="stat-label">Имя игрока</div>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">${playerStats.gamesPlayed}</span>
                            <div class="stat-label">Сыграно игр</div>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">${playerStats.wins}</span>
                            <div class="stat-label">Побед</div>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">${playerStats.losses}</span>
                            <div class="stat-label">Поражений</div>
                        </div>
                    </div>
                </div>
            </c:if>
            
            <div class="actions-section">
                <h3 class="actions-title">Что дальше?</h3>
                <div class="action-buttons">
                    <c:if test="${not empty quest}">
                        <form method="post" action="${pageContext.request.contextPath}/result" style="display: inline;">
                            <input type="hidden" name="action" value="restart">
                            <input type="hidden" name="questId" value="${quest.id}">
                            <button type="submit" class="btn btn-success">
                                Играть снова
                            </button>
                        </form>
                    </c:if>
                    
                    <form method="post" action="${pageContext.request.contextPath}/result" style="display: inline;">
                        <input type="hidden" name="action" value="new_quest">
                        <button type="submit" class="btn btn-primary">
                            Выбрать другой квест
                        </button>
                    </form>
                    
                    <a href="${pageContext.request.contextPath}/welcome" class="btn btn-secondary">
                        На главную
                    </a>
                    
                    <c:if test="${not empty user}">
                        <a href="${pageContext.request.contextPath}/user/profile" class="btn btn-outline">
                            Мой профиль
                        </a>
                    </c:if>
                </div>
            </div>
        </div>
    </main>
</body>
</html>