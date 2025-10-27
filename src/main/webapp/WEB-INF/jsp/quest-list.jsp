<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dt" uri="http://javarush.com/jsp/functions" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:choose><c:when test="${isAdminView}">Все квесты (Администратор)</c:when><c:otherwise>Мои квесты</c:otherwise></c:choose> - Text Quest Game</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .quest-list-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 2rem;
        }
        
        .quest-list-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2rem;
            padding-bottom: 1.5rem;
            border-bottom: 2px solid var(--border-color);
        }
        
        .quest-list-title {
            font-size: 1.875rem;
            font-weight: 600;
            color: var(--text-primary);
            margin: 0;
        }
        
        .quest-list-actions {
            display: flex;
            gap: 1rem;
        }
        
        .quest-stats {
            background: var(--white);
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow-md);
            padding: 1.5rem;
            margin-bottom: 2rem;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 1rem;
        }
        
        .stat-item {
            text-align: center;
        }
        
        .stat-number {
            font-size: 1.5rem;
            font-weight: bold;
            color: var(--primary-color);
            display: block;
        }
        
        .stat-label {
            font-size: 0.875rem;
            color: var(--text-secondary);
            margin-top: 0.25rem;
        }
        
        .quest-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
            gap: 1.5rem;
        }
        
        .quest-card {
            background: var(--white);
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow-md);
            overflow: hidden;
            transition: var(--transition-base);
            border: 1px solid var(--border-color);
        }
        
        .quest-card:hover {
            transform: translateY(-4px);
            box-shadow: var(--shadow-lg);
        }
        
        .quest-card-header {
            padding: 1.5rem;
            border-bottom: 1px solid var(--border-color);
        }
        
        .quest-card-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-primary);
            margin: 0 0 0.5rem 0;
        }
        
        .quest-card-meta {
            font-size: 0.875rem;
            color: var(--text-secondary);
        }
        
        .quest-card-body {
            padding: 1.5rem;
        }
        
        .quest-card-description {
            color: var(--text-secondary);
            line-height: 1.6;
            margin-bottom: 1rem;
        }
        
        .quest-card-stats {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 1rem;
            margin-bottom: 1rem;
        }
        
        .quest-stat {
            text-align: center;
            padding: 0.75rem;
            background: var(--bg-secondary);
            border-radius: var(--radius-md);
        }
        
        .quest-stat-number {
            font-size: 1.125rem;
            font-weight: bold;
            color: var(--primary-color);
            display: block;
        }
        
        .quest-stat-label {
            font-size: 0.75rem;
            color: var(--text-secondary);
            margin-top: 0.25rem;
        }
        
        .quest-card-actions {
            display: flex;
            gap: 0.5rem;
        }
        
        .quest-card-actions .btn {
            flex: 1;
            text-align: center;
            padding: 0.75rem 1rem;
            font-size: 0.875rem;
        }
        
        .btn {
            padding: 0.75rem 1.5rem;
            background: var(--primary-color);
            color: var(--white);
            text-decoration: none;
            border-radius: var(--radius-md);
            border: none;
            cursor: pointer;
            font-size: 0.875rem;
            font-weight: 500;
            transition: var(--transition-fast);
            display: inline-block;
        }
        
        .btn:hover {
            background: var(--primary-dark);
        }
        
        .btn-success {
            background: var(--success-color);
        }
        
        .btn-success:hover {
            background: #218838;
        }
        
        .btn-danger {
            background: #dc3545;
        }
        
        .btn-danger:hover {
            background: #c82333;
        }
        
        .btn-secondary {
            background: #6c757d;
        }
        
        .btn-secondary:hover {
            background: #545b62;
        }
        
        .quest-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .quest-card {
            background: #fff;
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            transition: box-shadow 0.3s;
        }
        
        .quest-card:hover {
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
        }
        
        .quest-title {
            font-size: 18px;
            font-weight: bold;
            color: #333;
            margin-bottom: 10px;
        }
        
        .quest-description {
            color: #666;
            margin-bottom: 15px;
            line-height: 1.4;
        }
        
        .quest-meta {
            font-size: 12px;
            color: #999;
            margin-bottom: 15px;
        }
        
        .quest-actions {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        
        .quest-actions .btn {
            padding: 8px 12px;
            font-size: 12px;
        }
        
        .quest-actions form {
            margin: 0;
        }
        
        .quest-actions button {
            border: none;
            cursor: pointer;
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #666;
        }
        
        .empty-state h3 {
            margin-bottom: 10px;
            color: #333;
        }
        
        .user-info {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        
        .user-info h3 {
            margin: 0 0 10px 0;
            color: #333;
        }
        
        .user-info p {
            margin: 5px 0;
            color: #666;
        }
        
        .quest-author {
            font-size: 13px;
            color: #007bff;
            font-weight: 500;
            margin-bottom: 5px;
        }
        
        .admin-badge {
            display: inline-block;
            background: #ffc107;
            color: #000;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: bold;
            margin-left: 10px;
        }
    </style>
</head>
<body>
    <div class="quest-list-container">
        <div class="quest-list-header">
            <h1 class="quest-list-title">
                <c:choose>
                    <c:when test="${isAdminView}">
                        Все квесты <span class="admin-badge">ADMIN</span>
                    </c:when>
                    <c:otherwise>
                        Мои квесты
                    </c:otherwise>
                </c:choose>
            </h1>
            <c:if test="${isAdminView}">
                <div class="quest-list-actions">
                    <a href="${pageContext.request.contextPath}/quest-builder" class="btn btn-success">
                        Создать новый квест
                    </a>
                </div>
            </c:if>
        </div>
        
        <div class="quest-stats">
            <div class="stat-item">
                <span class="stat-number">${quests.size()}</span>
                <div class="stat-label">Всего квестов</div>
            </div>
            <div class="stat-item">
                <span class="stat-number">0</span>
                <div class="stat-label">Игроков</div>
            </div>
            <div class="stat-item">
                <span class="stat-number">0</span>
                <div class="stat-label">Побед</div>
            </div>
        </div>
        
        <c:choose>
            <c:when test="${not empty quests}">
                <div class="quest-grid">
                    <c:forEach var="quest" items="${quests}">
                        <div class="quest-card">
                            <div class="quest-title">${quest.title}</div>
                            <c:if test="${isAdminView}">
                                <div class="quest-author">
                                    Автор: ${quest.author.username}
                                </div>
                            </c:if>
                            <div class="quest-description">
                                ${quest.description}
                            </div>
                            <div class="quest-meta">
                                Создан: ${dt:format(quest.createdAt, "dd.MM.yyyy HH:mm")}<br>
                                Этапов: ${quest.stageCount}
                            </div>
                            <div class="quest-actions">
                                <a href="${pageContext.request.contextPath}/quest-builder?questId=${quest.id}" 
                                   class="btn">Редактировать</a>
                                <a href="${pageContext.request.contextPath}/game?questId=${quest.id}" 
                                   class="btn btn-secondary">Играть</a>
                                <form method="post" action="${pageContext.request.contextPath}/quest-editor/delete" style="display: inline;">
                                    <input type="hidden" name="id" value="${quest.id}">
                                    <button type="submit" class="btn btn-danger"
                                            onclick="return confirm('Вы уверены, что хотите удалить этот квест?')">Удалить</button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <h3>У вас пока нет квестов</h3>
                    <p>Создайте свой первый квест, чтобы начать!</p>
                    <c:if test="${isAdminView}">
                        <a href="${pageContext.request.contextPath}/quest-builder" class="btn btn-success">
                            Создать квест
                        </a>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
        
        <div style="text-align: center; margin-top: 30px;">
            <a href="${pageContext.request.contextPath}/welcome" class="btn btn-secondary">
                Вернуться на главную
            </a>
            <a href="${pageContext.request.contextPath}/user/profile" class="btn">
                Мой профиль
            </a>
        </div>
    </div>
</body>
</html>
