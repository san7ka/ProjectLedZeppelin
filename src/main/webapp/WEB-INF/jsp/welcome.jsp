<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dt" uri="http://javarush.com/jsp/functions" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Text Quest Game</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/modern.css?v=2">
    <style>
        /* Critical styles to ensure they load */
        :root {
            --primary: #2563eb;
            --primary-dark: #1d4ed8;
            --secondary: #10b981;
            --danger: #ef4444;
            --bg-primary: #ffffff;
            --bg-secondary: #f8fafc;
            --text-primary: #1e293b;
            --text-secondary: #475569;
            --border: #e2e8f0;
            --radius: 0.5rem;
            --radius-lg: 0.75rem;
            --shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
            --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1);
        }
        
        body {
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: linear-gradient(to bottom, var(--bg-secondary), var(--bg-primary));
            min-height: 100vh;
        }
        
        .hero {
            background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%) !important;
            color: white !important;
            padding: 3rem 0 !important;
            text-align: center !important;
        }
        
        .hero h1 {
            font-size: 2.5rem !important;
            font-weight: 700 !important;
            margin-bottom: 0.5rem !important;
            color: white !important;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 1rem;
        }
        
        .content {
            padding: 2rem 0;
        }
        
        /* Alert / Auth Prompt */
        .alert {
            background: #fef3c7;
            border: 1px solid #fbbf24;
            border-radius: var(--radius-lg);
            padding: 1.5rem;
            margin-bottom: 2rem;
            text-align: center;
        }
        
        .alert h3 {
            color: #92400e;
            margin: 0 0 0.5rem 0;
            font-size: 1.25rem;
        }
        
        .alert p {
            color: #92400e;
            margin: 0 0 1rem 0;
        }
        
        /* Buttons */
        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 0.75rem 1.5rem;
            font-size: 0.875rem;
            font-weight: 500;
            text-decoration: none;
            border: none;
            border-radius: var(--radius);
            cursor: pointer;
            transition: all 0.2s;
            gap: 0.5rem;
        }
        
        .btn-primary {
            background: var(--primary);
            color: white;
        }
        
        .btn-primary:hover {
            background: var(--primary-dark);
            transform: translateY(-1px);
            box-shadow: var(--shadow-md);
        }
        
        .btn-success {
            background: var(--secondary);
            color: white;
        }
        
        .btn-success:hover {
            background: #059669;
            transform: translateY(-1px);
            box-shadow: var(--shadow-md);
        }
        
        .btn-danger {
            background: var(--danger);
            color: white;
        }
        
        .btn-danger:hover {
            background: #dc2626;
            transform: translateY(-1px);
            box-shadow: var(--shadow-md);
        }
        
        .btn-secondary {
            background: #f1f5f9;
            color: var(--text-primary);
            border: 1px solid var(--border);
        }
        
        .btn-secondary:hover {
            background: #e2e8f0;
            transform: translateY(-1px);
            box-shadow: var(--shadow);
        }
        
        .btn-group {
            display: flex;
            gap: 0.75rem;
            flex-wrap: wrap;
            justify-content: center;
        }
        
        /* User Info */
        .user-info {
            background: linear-gradient(135deg, #60a5fa, var(--primary));
            color: white;
            padding: 2rem;
            border-radius: var(--radius-lg);
            margin-bottom: 2rem;
            text-align: center;
            box-shadow: var(--shadow-md);
        }
        
        .user-info h3 {
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 0.5rem;
        }
        
        .user-info p {
            opacity: 0.9;
            margin-bottom: 1.5rem;
        }
        
        /* Section Title */
        .section-title {
            font-size: 1.875rem;
            font-weight: 700;
            color: var(--text-primary);
            text-align: center;
            margin-bottom: 2rem;
        }
        
        /* Quest Cards */
        .quests {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }
        
        .card {
            background: var(--bg-primary);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow);
            transition: all 0.2s;
            overflow: hidden;
        }
        
        .card:hover {
            box-shadow: var(--shadow-md);
            transform: translateY(-2px);
        }
        
        .quest-card {
            padding: 1.5rem;
        }
        
        .quest-title {
            font-size: 1.125rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 0.75rem;
            line-height: 1.3;
        }
        
        .quest-description {
            color: var(--text-secondary);
            margin-bottom: 1rem;
            line-height: 1.6;
        }
        
        .quest-meta {
            font-size: 0.875rem;
            color: #64748b;
            margin-bottom: 1rem;
            display: flex;
            flex-wrap: wrap;
            gap: 1rem;
        }
        
        .quest-meta span {
            display: flex;
            align-items: center;
            gap: 0.25rem;
        }
        
        /* Guest Form */
        .guest-form {
            background: var(--bg-primary);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 2rem;
            text-align: center;
            box-shadow: var(--shadow);
        }
        
        .guest-form h3 {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 0.75rem;
        }
        
        .guest-form p {
            color: var(--text-secondary);
            margin-bottom: 1.5rem;
        }
        
        .guest-form input {
            padding: 0.75rem 1rem;
            border: 1px solid var(--border);
            border-radius: var(--radius);
            font-size: 1rem;
            min-width: 250px;
            margin-right: 0.75rem;
            transition: all 0.2s;
        }
        
        .guest-form input:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgb(37 99 235 / 0.1);
        }
        
        .text-center {
            text-align: center;
        }
        
        /* Responsive */
        @media (max-width: 768px) {
            .hero h1 {
                font-size: 2rem !important;
            }
            
            .quests {
                grid-template-columns: 1fr;
                gap: 1rem;
            }
            
            .btn-group {
                flex-direction: column;
            }
            
            .btn {
                width: 100%;
            }
            
            .guest-form input {
                width: 100%;
                margin-right: 0;
                margin-bottom: 1rem;
                min-width: auto;
            }
        }
    </style>
</head>
<body>
    <header class="hero">
        <div class="container">
            <h1>🎮 Text Quest Game</h1>
            <p>Погрузитесь в мир текстовых приключений</p>
        </div>
    </header>

    <main class="content">
        <div class="container">
            <c:choose>
                <c:when test="${not empty user}">
                    <section class="user-info">
                        <h3>👋 Добро пожаловать, ${user.username}!</h3>
                        <p>📧 ${user.email} | 🎭 ${user.admin ? 'Администратор' : 'Игрок'}</p>
                        
                        <div class="btn-group">
                            <a href="${pageContext.request.contextPath}/user/profile" class="btn btn-primary">
                                👤 Мой профиль
                            </a>
                            <c:if test="${user.admin}">
                                <a href="${pageContext.request.contextPath}/quest-editor" class="btn btn-danger">
                                    ✏️ Редактор квестов
                                </a>
                            </c:if>
                            <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-secondary">
                                🚪 Выйти
                            </a>
                        </div>
                    </section>
                </c:when>
                <c:otherwise>
                    <section class="alert alert-warning">
                        <h3>🔐 Войдите или играйте как гость</h3>
                        <p>Зарегистрируйтесь для создания квестов и сохранения прогресса</p>
                        <div class="btn-group">
                            <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-primary">
                                🔑 Войти
                            </a>
                            <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-success">
                                📝 Зарегистрироваться
                            </a>
                        </div>
                    </section>
                </c:otherwise>
            </c:choose>

            <c:if test="${not empty availableQuests}">
                <h2 class="section-title">📚 Доступные квесты</h2>
                
                <div class="quests">
                    <c:forEach var="quest" items="${availableQuests}" begin="0" end="5">
                        <div class="card quest-card">
                            <h3 class="quest-title">${quest.title}</h3>
                            <p class="quest-description">${quest.description}</p>
                            
                            <div class="quest-meta">
                                <span>👤 ${quest.author.username}</span>
                                <span>📍 ${quest.stageCount} этапов</span>
                                <span>📅 ${dt:formatDate(quest.createdAt)}</span>
                            </div>
                            
                            <a href="${pageContext.request.contextPath}/game?questId=${quest.id}" class="btn btn-primary">
                                🎮 Играть
                            </a>
                        </div>
                    </c:forEach>
                </div>
                
                <div class="text-center">
                    <a href="${pageContext.request.contextPath}/game" class="btn btn-secondary">
                        📋 Все квесты
                    </a>
                </div>
            </c:if>

            <c:if test="${empty user}">
                <section class="card guest-form">
                    <h3>🎯 Начать игру как гость</h3>
                    <p>Введите ваше имя и начните играть прямо сейчас!</p>
                    
                    <form action="${pageContext.request.contextPath}/welcome" method="post" class="btn-group">
                        <input type="text" 
                               name="playerName" 
                               placeholder="Введите ваше имя" 
                               required 
                               value="${not empty playerStats.playerName ? playerStats.playerName : ''}">
                        <button type="submit" class="btn btn-success">
                            🚀 Начать игру
                        </button>
                    </form>
                </section>
            </c:if>
        </div>
    </main>
</body>
</html>