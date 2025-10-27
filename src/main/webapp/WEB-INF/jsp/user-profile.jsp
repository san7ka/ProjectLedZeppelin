<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="dt" uri="http://javarush.com/jsp/functions" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Мой профиль - Text Quest Game</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .container {
            max-width: 1000px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .header {
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #eee;
        }
        
        .header h1 {
            color: #333;
            margin: 0;
        }
        
        .profile-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
            margin-bottom: 30px;
        }
        
        .profile-card {
            background: #fff;
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 25px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
        
        .profile-card h3 {
            margin: 0 0 20px 0;
            color: #333;
            border-bottom: 2px solid #007bff;
            padding-bottom: 10px;
        }
        
        .profile-info {
            margin-bottom: 20px;
        }
        
        .profile-info p {
            margin: 10px 0;
            color: #666;
        }
        
        .profile-info strong {
            color: #333;
            display: inline-block;
            width: 120px;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
            gap: 12px;
            margin-top: 20px;
        }
        
        .stat-item {
            text-align: center;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 8px;
        }
        
        .stat-number {
            font-size: 24px;
            font-weight: bold;
            color: #007bff;
            display: block;
        }
        
        .stat-label {
            font-size: 12px;
            color: #666;
            margin-top: 5px;
        }
        
        .btn {
            padding: 10px 20px;
            background: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            border: none;
            cursor: pointer;
            font-size: 14px;
            transition: background-color 0.3s;
            display: inline-block;
            margin: 5px;
        }
        
        .btn:hover {
            background: #0056b3;
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
        
        .actions-section {
            text-align: center;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 2px solid #eee;
        }
        
        .role-badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: bold;
            text-transform: uppercase;
        }
        
        .role-admin {
            background: #dc3545;
            color: white;
        }
        
        .role-user {
            background: #6c757d;
            color: white;
        }
        
        .message {
            background: #d4edda;
            color: #155724;
            padding: 10px;
            border-radius: 5px;
            margin-bottom: 20px;
            border: 1px solid #c3e6cb;
        }
        
        .error-message {
            background: #f8d7da;
            color: #721c24;
            padding: 10px;
            border-radius: 5px;
            margin-bottom: 20px;
            border: 1px solid #f5c6cb;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Мой профиль</h1>
        </div>
        
        <c:if test="${not empty message}">
            <div class="message">
                ${message}
            </div>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="error-message">
                ${error}
            </div>
        </c:if>
        
        <div class="profile-grid">
            <!-- Информация о пользователе -->
            <div class="profile-card">
                <h3>Личная информация</h3>
                <div class="profile-info">
                    <p><strong>Имя пользователя:</strong> ${user.username}</p>
                    <p><strong>Email:</strong> ${user.email}</p>
                    <p><strong>Роль:</strong> 
                        <span class="role-badge ${user.admin ? 'role-admin' : 'role-user'}">
                            ${user.admin ? 'Администратор' : 'Пользователь'}
                        </span>
                    </p>
                    <p><strong>Дата регистрации:</strong> 
                        ${dt:format(user.createdAt, "dd.MM.yyyy HH:mm")}
                    </p>
                    <p><strong>Последнее обновление:</strong> 
                        ${dt:format(user.updatedAt, "dd.MM.yyyy HH:mm")}
                    </p>
                </div>
            </div>
            
            <!-- Статистика -->
            <div class="profile-card">
                <h3>Статистика</h3>
                <div class="stats-grid">
                    <div class="stat-item">
                        <span class="stat-number">${not empty statistics ? statistics.totalGames : 0}</span>
                        <div class="stat-label">Всего игр</div>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">${not empty statistics ? statistics.completedGames : 0}</span>
                        <div class="stat-label">Завершено</div>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">${not empty statistics ? statistics.wonGames : 0}</span>
                        <div class="stat-label">Побед</div>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">${not empty statistics ? fn:substring(statistics.winRate, 0, 4) : '0.0'}%</span>
                        <div class="stat-label">Win rate</div>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">${not empty statistics ? statistics.totalPlaytimeMinutes : 0}</span>
                        <div class="stat-label">Минут игры</div>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">${not empty statistics and not empty statistics.bestTimeMinutes ? statistics.bestTimeMinutes : '-'}</span>
                        <div class="stat-label">Лучшее время</div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="actions-section">
            <a href="${pageContext.request.contextPath}/user/quests" class="btn btn-success">
                Мои квесты
            </a>
            <c:if test="${user.admin}">
                <a href="${pageContext.request.contextPath}/quest-builder" class="btn btn-success">
                    Создать квест
                </a>
            </c:if>
        </div>
        
        <div style="text-align: center; margin-top: 30px;">
            <a href="${pageContext.request.contextPath}/welcome" class="btn btn-secondary">
                Главная
            </a>
            <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-secondary">
                Выйти
            </a>
        </div>
    </div>
</body>
</html>
