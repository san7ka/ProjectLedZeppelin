<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${stage.title} - ${quest.title}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/modern.css?v=2">
    <style>
        .game-page {
            background: linear-gradient(to bottom, var(--bg-secondary), var(--bg-primary));
            min-height: 100vh;
        }
        
        .game-header {
            background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
            color: white;
            padding: 1.5rem 0;
            margin-bottom: 2rem;
            box-shadow: var(--shadow-md);
        }
        
        .game-header-content {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 1rem;
        }
        
        .game-title {
            font-size: 1.5rem;
            font-weight: 600;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .player-info {
            display: flex;
            align-items: center;
            gap: 1rem;
            background: rgba(255, 255, 255, 0.1);
            padding: 0.5rem 1rem;
            border-radius: var(--radius-lg);
            backdrop-filter: blur(10px);
        }
        
        .player-avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.2);
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 0.875rem;
        }
        
        .player-details {
            display: flex;
            flex-direction: column;
        }
        
        .player-name {
            font-size: 0.875rem;
            font-weight: 500;
            margin-bottom: 0.125rem;
        }
        
        .player-stats {
            font-size: 0.75rem;
            opacity: 0.8;
        }
        
        .game-container {
            max-width: 800px;
            margin: 0 auto 2rem;
            background: var(--bg-primary);
            border: 1px solid var(--border);
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-lg);
            overflow: hidden;
        }
        
        .stage-header {
            background: linear-gradient(135deg, var(--bg-tertiary), var(--bg-secondary));
            padding: 2rem;
            border-bottom: 1px solid var(--border);
        }
        
        .stage-title {
            font-size: 1.75rem;
            font-weight: 700;
            color: var(--text-primary);
            margin: 0 0 1rem 0;
            line-height: 1.3;
        }
        
        .quest-info {
            display: flex;
            align-items: center;
            gap: 1rem;
            font-size: 0.875rem;
            color: var(--text-muted);
        }
        
        .quest-info span {
            display: flex;
            align-items: center;
            gap: 0.25rem;
        }
        
        .stage-content {
            padding: 2rem;
        }
        
        .story-text {
            font-size: 1.125rem;
            line-height: 1.8;
            color: var(--text-secondary);
            margin-bottom: 2rem;
            text-align: justify;
        }
        
        .choices-section {
            border-top: 1px solid var(--border);
            padding: 2rem;
            background: var(--bg-secondary);
        }
        
        .choices-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-primary);
            margin: 0 0 1.5rem 0;
            text-align: center;
        }
        
        .choices {
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }
        
        .choice-btn {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 1.25rem 1.5rem;
            background: var(--bg-primary);
            border: 2px solid var(--border);
            border-radius: var(--radius-lg);
            color: var(--text-primary);
            text-align: left;
            font-size: 1rem;
            font-weight: 500;
            cursor: pointer;
            transition: var(--transition);
            text-decoration: none;
            position: relative;
            overflow: hidden;
        }
        
        .choice-btn:hover {
            border-color: var(--primary);
            background: var(--bg-primary);
            transform: translateX(4px);
            box-shadow: var(--shadow-md);
        }
        
        .choice-btn .choice-text {
            flex: 1;
        }
        
        .choice-btn .choice-arrow {
            opacity: 0;
            transition: var(--transition);
            color: var(--primary);
        }
        
        .choice-btn:hover .choice-arrow {
            opacity: 1;
            transform: translateX(4px);
        }
        
        .game-actions {
            padding: 1.5rem 2rem;
            background: var(--bg-tertiary);
            border-top: 1px solid var(--border);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .back-btn {
            color: var(--text-muted);
            text-decoration: none;
            font-size: 0.875rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            transition: var(--transition);
        }
        
        .back-btn:hover {
            color: var(--primary);
        }
        
        .progress-info {
            font-size: 0.875rem;
            color: var(--text-muted);
        }
        
        .result-screen {
            text-align: center;
            padding: 3rem 2rem;
        }
        
        .result-icon {
            font-size: 4rem;
            margin-bottom: 1.5rem;
        }
        
        .result-title {
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 1rem;
        }
        
        .result-description {
            font-size: 1.125rem;
            color: var(--text-secondary);
            margin-bottom: 2rem;
            line-height: 1.6;
        }
        
        .result-actions {
            display: flex;
            gap: 1rem;
            justify-content: center;
            flex-wrap: wrap;
        }
        
        .win .result-icon {
            color: var(--secondary);
        }
        
        .win .result-title {
            color: var(--secondary);
        }
        
        .lose .result-icon {
            color: var(--danger);
        }
        
        .lose .result-title {
            color: var(--danger);
        }
        
        @media (max-width: 768px) {
            .game-header-content {
                flex-direction: column;
                text-align: center;
            }
            
            .player-info {
                width: 100%;
                justify-content: center;
            }
            
            .game-container {
                margin: 0 1rem 2rem;
            }
            
            .stage-header,
            .stage-content,
            .choices-section,
            .game-actions {
                padding: 1.5rem;
            }
            
            .stage-title {
                font-size: 1.5rem;
            }
            
            .story-text {
                font-size: 1rem;
            }
            
            .game-actions {
                flex-direction: column;
                gap: 1rem;
                text-align: center;
            }
        }
        
        @media (max-width: 480px) {
            .game-header {
                padding: 1rem 0;
            }
            
            .game-title {
                font-size: 1.25rem;
            }
            
            .stage-header,
            .stage-content,
            .choices-section,
            .game-actions {
                padding: 1rem;
            }
            
            .stage-title {
                font-size: 1.25rem;
            }
            
            .choice-btn {
                padding: 1rem;
                font-size: 0.875rem;
            }
        }
        
        .choice-btn:active {
            transform: translateY(0);
        }
        
        .choice-btn::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
            transition: left 0.5s;
        }
        
        .choice-btn:hover::before {
            left: 100%;
        }
        
        .game-progress {
            background: #e9ecef;
            padding: 15px 20px;
            border-top: 1px solid #dee2e6;
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 14px;
            color: #666;
        }
        
        .progress-info {
            display: flex;
            gap: 20px;
        }
        
        .back-btn {
            background: #6c757d;
            color: white;
            text-decoration: none;
            padding: 8px 16px;
            border-radius: 6px;
            font-size: 14px;
            transition: background-color 0.3s;
        }
        
        .back-btn:hover {
            background: #545b62;
            color: white;
        }
        
        .fade-in {
            animation: fadeIn 0.5s ease-in;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        .quest-meta {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 6px;
            margin-bottom: 20px;
            font-size: 14px;
            color: #666;
        }
        
        .quest-meta span {
            margin-right: 20px;
        }
        
        .quest-author {
            color: #007bff;
            font-weight: 500;
        }
    </style>
</head>
<body class="game-page">
    <header class="game-header">
        <div class="container">
            <div class="game-header-content">
                <h1 class="game-title">
                    🎮 ${quest.title}
                </h1>
                
                <div class="player-info">
                    <div class="player-avatar">
                        <c:choose>
                            <c:when test="${not empty user and not empty user.username}">
                                ${user.username.substring(0,1).toUpperCase()}
                            </c:when>
                            <c:otherwise>
                                Г
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="player-details">
                        <span class="player-name">${not empty user and not empty user.username ? user.username : 'Гость'}</span>
                        <span class="player-stats">🎯 Этап: ${stage.stageId}</span>
                    </div>
                </div>
            </div>
        </div>
    </header>

    <main class="game-container">
        <div class="stage-header">
            <h2 class="stage-title">${stage.title}</h2>
            <div class="quest-info">
                <span>📚 Квест: ${quest.title}</span>
                <c:if test="${not empty quest.author}">
                    <span>👤 Автор: ${quest.author.username}</span>
                </c:if>
                <span>📍 Этап: ${stage.stageId}</span>
            </div>
        </div>
        
        <div class="stage-content">
            <div class="story-text">${stage.description}</div>
        </div>

        <c:choose>
            <c:when test="${not empty availableOptions}">
                <div class="choices-section">
                    <h3 class="choices-title">🤔 Что вы будете делать?</h3>
                    
                    <form method="post" action="${pageContext.request.contextPath}/game" class="choices">
                        <c:choose>
                            <c:when test="${not empty progress}">
                                <input type="hidden" name="progressId" value="${progress.id}">
                            </c:when>
                            <c:otherwise>
                                <input type="hidden" name="questId" value="${quest.id}">
                                <input type="hidden" name="currentStageId" value="${stage.stageId}">
                                <input type="hidden" name="anonymousGame" value="true">
                            </c:otherwise>
                        </c:choose>
                        
                        <c:forEach var="optionKey" items="${availableOptions}">
                            <c:forEach var="option" items="${stage.options}">
                                <c:if test="${option.optionKey == optionKey}">
                                    <button type="submit" 
                                            name="choice" 
                                            value="${option.optionKey}" 
                                            class="choice-btn">
                                        <span class="choice-text">${option.optionText}</span>
                                        <span class="choice-arrow">→</span>
                                    </button>
                                </c:if>
                            </c:forEach>
                        </c:forEach>
                    </form>
                </div>
            </c:when>
            <c:otherwise>
                <div class="choices-section">
                    <div class="result-screen">
                        <div class="result-icon">🏁</div>
                        <h3 class="result-title">Игра завершена!</h3>
                        <p class="result-description">Нет доступных вариантов для выбора.</p>
                        <div class="result-actions">
                            <a href="${pageContext.request.contextPath}/result?progressId=${progress.id}" 
                               class="btn btn-primary">
                                📊 Посмотреть результат
                            </a>
                            <a href="${pageContext.request.contextPath}/game" class="btn btn-secondary">
                                🎮 Другой квест
                            </a>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
        
        <%-- Кнопки отката и истории (только для авторизованных пользователей) --%>
        <c:if test="${not empty progress and not empty user}">
            <div class="game-controls" style="margin-top: 2rem; display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;">
                <form method="post" action="${pageContext.request.contextPath}/game-action" style="display: inline;">
                    <input type="hidden" name="action" value="goBack">
                    <input type="hidden" name="sessionId" value="${progress.id}">
                    <button type="submit" class="btn btn-secondary" id="goBackBtn">
                        ↩️ Откатиться назад
                    </button>
                </form>
                
                <a href="${pageContext.request.contextPath}/game-action?action=getHistory&sessionId=${progress.id}" 
                   class="btn btn-secondary">
                    📜 История прохождения
                </a>
            </div>
            
            <script>
                // Проверка возможности отката через AJAX
                window.addEventListener('DOMContentLoaded', function() {
                    const goBackBtn = document.getElementById('goBackBtn');
                    if (goBackBtn) {
                        fetch('${pageContext.request.contextPath}/game-action?action=canGoBack&sessionId=${progress.id}')
                            .then(response => response.json())
                            .then(data => {
                                if (!data.canGoBack) {
                                    goBackBtn.disabled = true;
                                    goBackBtn.style.opacity = '0.5';
                                    goBackBtn.style.cursor = 'not-allowed';
                                    goBackBtn.title = 'Невозможно откатиться назад';
                                }
                            })
                            .catch(error => {
                                console.error('Ошибка проверки возможности отката:', error);
                            });
                    }
                });
            </script>
        </c:if>
        
        <div class="game-actions">
            <a href="${pageContext.request.contextPath}/game" class="back-btn">
                ← Выйти в меню
            </a>
            
            <div class="progress-info">
                🎮 Этап: ${stage.stageId}
            </div>
        </div>
    </main>
</body>
</html>
