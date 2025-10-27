<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dt" uri="http://javarush.com/jsp/functions" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Админ-панель - Text Quest Game</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .admin-container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 2rem;
        }
        
        .admin-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2rem;
            padding-bottom: 1.5rem;
            border-bottom: 2px solid #ddd;
        }
        
        .admin-title {
            font-size: 2rem;
            font-weight: 600;
            color: #333;
            margin: 0;
        }
        
        .admin-stats {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            padding: 2rem;
            margin-bottom: 2rem;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 2rem;
        }
        
        .stat-item {
            text-align: center;
        }
        
        .stat-number {
            font-size: 2.5rem;
            font-weight: bold;
            color: #007bff;
            display: block;
        }
        
        .stat-label {
            font-size: 1rem;
            color: #666;
            margin-top: 0.5rem;
        }
        
        .users-section {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            padding: 2rem;
        }
        
        .section-title {
            font-size: 1.5rem;
            font-weight: 600;
            color: #333;
            margin: 0 0 1.5rem 0;
            padding-bottom: 1rem;
            border-bottom: 1px solid #ddd;
        }
        
        .users-table {
            width: 100%;
            border-collapse: collapse;
        }
        
        .users-table th,
        .users-table td {
            padding: 1rem;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        
        .users-table th {
            background: #f8f9fa;
            font-weight: 600;
            color: #333;
        }
        
        .users-table tr:hover {
            background: #f8f9fa;
        }
        
        .user-role {
            display: inline-block;
            padding: 0.25rem 0.75rem;
            border-radius: 20px;
            font-size: 0.875rem;
            font-weight: 500;
        }
        
        .role-admin {
            background: #ffc107;
            color: #000;
        }
        
        .role-user {
            background: #28a745;
            color: white;
        }
        
        .btn {
            padding: 0.5rem 1rem;
            background: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 6px;
            border: none;
            cursor: pointer;
            font-size: 0.875rem;
            transition: background-color 0.3s;
            display: inline-block;
            margin: 0 0.25rem;
        }
        
        .btn:hover {
            background: #0056b3;
        }
        
        .btn-danger {
            background: #dc3545;
        }
        
        .btn-danger:hover {
            background: #c82333;
        }
        
        .btn-warning {
            background: #ffc107;
            color: #000;
        }
        
        .btn-warning:hover {
            background: #e0a800;
        }
        
        .btn-secondary {
            background: #6c757d;
        }
        
        .btn-secondary:hover {
            background: #545b62;
        }
        
        .error-message {
            background: #f8d7da;
            color: #721c24;
            padding: 1rem;
            border-radius: 6px;
            margin-bottom: 1.5rem;
            border: 1px solid #f5c6cb;
        }
        
        .success-message {
            background: #d4edda;
            color: #155724;
            padding: 1rem;
            border-radius: 6px;
            margin-bottom: 1.5rem;
            border: 1px solid #c3e6cb;
        }
        
        .actions-cell {
            white-space: nowrap;
        }
        
        .back-link {
            margin-top: 2rem;
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="admin-container">
        <div class="admin-header">
            <h1 class="admin-title">Админ-панель</h1>
            <div>
                <a href="${pageContext.request.contextPath}/user/profile" class="btn btn-secondary">
                    Мой профиль
                </a>
                <a href="${pageContext.request.contextPath}/welcome" class="btn btn-secondary">
                    Главная
                </a>
            </div>
        </div>
        
        <c:if test="${not empty error}">
            <div class="error-message">
                ${error}
            </div>
        </c:if>
        
        <c:if test="${not empty message}">
            <div class="success-message">
                ${message}
            </div>
        </c:if>
        
        <div class="admin-stats">
            <div class="stat-item">
                <span class="stat-number">${users.size()}</span>
                <div class="stat-label">Всего пользователей</div>
            </div>
            <div class="stat-item">
                <span class="stat-number">${totalQuests}</span>
                <div class="stat-label">Всего квестов</div>
            </div>
            <div class="stat-item">
                <span class="stat-number">
                    <c:set var="adminCount" value="0"/>
                    <c:forEach var="u" items="${users}">
                        <c:if test="${u.admin}">
                            <c:set var="adminCount" value="${adminCount + 1}"/>
                        </c:if>
                    </c:forEach>
                    ${adminCount}
                </span>
                <div class="stat-label">Администраторов</div>
            </div>
            <div class="stat-item">
                <span class="stat-number">${users.size() - adminCount}</span>
                <div class="stat-label">Обычных пользователей</div>
            </div>
        </div>
        
        <div class="users-section">
            <h2 class="section-title">Управление пользователями</h2>
            
            <c:choose>
                <c:when test="${not empty users}">
                    <table class="users-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Имя пользователя</th>
                                <th>Email</th>
                                <th>Роль</th>
                                <th>Дата регистрации</th>
                                <th>Действия</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="u" items="${users}">
                                <tr>
                                    <td>${u.id}</td>
                                    <td>${u.username}</td>
                                    <td>${u.email}</td>
                                    <td>
                                        <span class="user-role ${u.admin ? 'role-admin' : 'role-user'}">
                                            ${u.admin ? 'Администратор' : 'Пользователь'}
                                        </span>
                                    </td>
                                    <td>
                                        ${dt:format(u.createdAt, "dd.MM.yyyy HH:mm")}
                                    </td>
                                    <td class="actions-cell">
                                        <c:if test="${u.id != user.id}">
                                            <form method="post" 
                                                  action="${pageContext.request.contextPath}/user/admin/toggle-admin" 
                                                  style="display: inline;">
                                                <input type="hidden" name="userId" value="${u.id}">
                                                <button type="submit" class="btn btn-warning">
                                                    ${u.admin ? 'Снять админа' : 'Сделать админом'}
                                                </button>
                                            </form>
                                            
                                            <form method="post" 
                                                  action="${pageContext.request.contextPath}/user/admin/delete-user" 
                                                  style="display: inline;"
                                                  onsubmit="return confirm('Вы уверены, что хотите удалить пользователя ${u.username}?');">
                                                <input type="hidden" name="userId" value="${u.id}">
                                                <button type="submit" class="btn btn-danger">
                                                    Удалить
                                                </button>
                                            </form>
                                        </c:if>
                                        <c:if test="${u.id == user.id}">
                                            <span style="color: #999; font-style: italic;">Это вы</span>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p style="text-align: center; color: #666; padding: 2rem;">
                        Пользователи не найдены
                    </p>
                </c:otherwise>
            </c:choose>
        </div>
        
        <div class="back-link">
            <a href="${pageContext.request.contextPath}/welcome" class="btn btn-secondary">
                Вернуться на главную
            </a>
        </div>
    </div>
</body>
</html>
