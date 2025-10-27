<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🔑 Вход - Text Quest Game</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/modern.css?v=2">
    <style>
        .auth-page {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-primary) 100%);
            padding: 2rem 1rem;
        }
        
        .auth-container {
            width: 100%;
            max-width: 400px;
            background: var(--bg-primary);
            border: 1px solid var(--border);
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-lg);
            padding: 2.5rem;
            position: relative;
        }
        
        .auth-header {
            text-align: center;
            margin-bottom: 2rem;
        }
        
        .auth-header h1 {
            font-size: 1.875rem;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 0.5rem;
        }
        
        .auth-header p {
            color: var(--text-secondary);
            font-size: 0.875rem;
        }
        
        .form-group {
            margin-bottom: 1.5rem;
        }
        
        .form-label {
            display: block;
            margin-bottom: 0.5rem;
            color: var(--text-primary);
            font-weight: 500;
            font-size: 0.875rem;
        }
        
        .form-input {
            width: 100%;
            padding: 0.75rem 1rem;
            border: 1px solid var(--border);
            border-radius: var(--radius);
            font-size: 1rem;
            transition: var(--transition);
            background: var(--bg-primary);
        }
        
        .form-input:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgb(37 99 235 / 0.1);
        }
        
        .form-input::placeholder {
            color: var(--text-light);
        }
        
        .error-message {
            background: #fef2f2;
            color: var(--danger);
            padding: 0.75rem 1rem;
            border-radius: var(--radius);
            margin-bottom: 1.5rem;
            border: 1px solid #fecaca;
            font-size: 0.875rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .error-message::before {
            content: '❌';
        }
        
        .btn-submit {
            width: 100%;
            padding: 0.875rem;
            font-size: 1rem;
            font-weight: 500;
            margin-bottom: 1.5rem;
        }
        
        .auth-footer {
            text-align: center;
            padding-top: 1.5rem;
            border-top: 1px solid var(--border);
        }
        
        .auth-footer p {
            color: var(--text-secondary);
            font-size: 0.875rem;
            margin: 0;
        }
        
        .auth-footer a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 500;
        }
        
        .auth-footer a:hover {
            text-decoration: underline;
        }
        
        .back-link {
            position: absolute;
            top: 1.5rem;
            left: 1.5rem;
            color: var(--text-muted);
            text-decoration: none;
            font-size: 0.875rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            transition: var(--transition);
        }
        
        .back-link:hover {
            color: var(--primary);
        }
        
        @media (max-width: 480px) {
            .auth-page {
                padding: 1rem;
            }
            
            .auth-container {
                padding: 1.5rem;
            }
            
            .auth-header h1 {
                font-size: 1.5rem;
            }
        }
    </style>
</head>
<body>
    <div class="auth-page">
        <div class="auth-container">
            <a href="${pageContext.request.contextPath}/welcome" class="back-link">
                ← На главную
            </a>
            
            <div class="auth-header">
                <h1>🔑 Вход в систему</h1>
                <p>Войдите, чтобы продолжить игру</p>
            </div>
            
            <c:if test="${not empty error}">
                <div class="error-message">
                    ${error}
                </div>
            </c:if>
            
            <form action="${pageContext.request.contextPath}/auth/login" method="post">
                <div class="form-group">
                    <label for="username" class="form-label">👤 Имя пользователя или email</label>
                    <input type="text" 
                           id="username" 
                           name="username" 
                           class="form-input" 
                           placeholder="Введите имя пользователя или email"
                           required 
                           value="${param.username}">
                </div>
                
                <div class="form-group">
                    <label for="password" class="form-label">🔒 Пароль</label>
                    <input type="password" 
                           id="password" 
                           name="password" 
                           class="form-input" 
                           placeholder="Введите пароль"
                           required>
                </div>
                
                <button type="submit" class="btn btn-primary btn-submit">
                    🔑 Войти
                </button>
            </form>
            
            <div class="auth-footer">
                <p>Еще нет аккаунта? <a href="${pageContext.request.contextPath}/auth/register">📝 Зарегистрироваться</a></p>
            </div>
        </div>
    </div>
</body>
</html>
