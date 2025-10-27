<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Игра - Text Quest Game</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
        }
        .game-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px 20px;
            text-align: center;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        .game-header h1 {
            margin: 0 0 10px 0;
            font-size: 28px;
        }
        .game-header p {
            margin: 0;
            opacity: 0.9;
        }
        .auth-prompt {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 30px;
            text-align: center;
        }
        .auth-prompt h3 {
            margin: 0 0 10px 0;
            color: #856404;
        }
        .auth-prompt p {
            margin: 0 0 15px 0;
            color: #856404;
        }
        .auth-links {
            display: flex;
            gap: 15px;
            justify-content: center;
        }
        .btn {
            padding: 12px 24px;
            background: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 6px;
            font-size: 16px;
            font-weight: 500;
            transition: all 0.3s ease;
            border: none;
            cursor: pointer;
            display: inline-block;
        }
        .btn:hover {
            background: #0056b3;
            transform: translateY(-1px);
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
        .quest-form {
            background: #f8f9fa;
            padding: 30px;
            border-radius: 10px;
            text-align: center;
        }
        .quest-form h3 {
            margin: 0 0 15px 0;
            color: #333;
        }
        .quest-form p {
            margin: 0 0 20px 0;
            color: #666;
        }
        .player-input {
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 16px;
            margin-right: 10px;
            min-width: 200px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="game-header">
            <h1>Text Quest Game</h1>
            <p>Выберите квест и начните приключение</p>
        </div>
        
        <div class="auth-prompt">
            <h3>Войдите или играйте как гость</h3>
            <p>Зарегистрируйтесь для создания квестов и сохранения прогресса</p>
            <div class="auth-links">
                <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-primary">
                    Войти
                </a>
                <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-secondary">
                    Зарегистрироваться
                </a>
            </div>
        </div>

        <div class="quest-form">
            <h3>Начать игру как гость</h3>
            <p>Введите ваше имя для начала приключения</p>
            
            <form action="${pageContext.request.contextPath}/game" method="get">
                <c:if test="${not empty questId}">
                    <input type="hidden" name="questId" value="${questId}">
                </c:if>
                
                <div class="form-group">
                    <label for="playerName">Ваше имя:</label>
                    <input type="text" 
                           name="playerName" 
                           id="playerName"
                           class="player-input" 
                           placeholder="Введите ваше имя" 
                           required 
                           value="Гость">
                </div>
                
                <button type="submit" class="btn btn-success">
                    Начать игру
                </button>
            </form>
        </div>
        
        <div style="text-align: center; margin-top: 30px;">
            <a href="${pageContext.request.contextPath}/welcome" class="btn btn-secondary">
                Вернуться на главную
            </a>
        </div>
    </div>
</body>
</html>
