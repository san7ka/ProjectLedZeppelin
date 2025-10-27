<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Создание нового квеста</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .container {
            max-width: 800px;
            margin: 2rem auto;
            padding: 2rem;
        }
        
        .form-card {
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            padding: 2rem;
        }
        
        .form-header {
            margin-bottom: 2rem;
            border-bottom: 2px solid #1a365d;
            padding-bottom: 1rem;
        }
        
        .form-header h1 {
            margin: 0;
            color: #1a365d;
        }
        
        .form-group {
            margin-bottom: 1.5rem;
        }
        
        .form-group label {
            display: block;
            font-weight: 600;
            margin-bottom: 0.5rem;
            color: #333;
        }
        
        .form-group input,
        .form-group textarea {
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 1rem;
            box-sizing: border-box;
        }
        
        .form-group textarea {
            min-height: 150px;
            resize: vertical;
        }
        
        .form-group input:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: #1a365d;
            box-shadow: 0 0 0 3px rgba(26, 54, 93, 0.1);
        }
        
        .btn-group {
            display: flex;
            gap: 1rem;
            margin-top: 2rem;
        }
        
        .btn {
            padding: 0.75rem 1.5rem;
            border: none;
            border-radius: 4px;
            font-size: 1rem;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .btn-primary {
            background: #1a365d;
            color: white;
        }
        
        .btn-primary:hover {
            background: #152a48;
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #545b62;
        }
        
        .error-message {
            background: #f8d7da;
            color: #721c24;
            padding: 1rem;
            border-radius: 4px;
            margin-bottom: 1.5rem;
            border: 1px solid #f5c6cb;
        }
        
        .help-text {
            font-size: 0.875rem;
            color: #666;
            margin-top: 0.25rem;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="form-card">
            <div class="form-header">
                <h1>Создать новый квест</h1>
            </div>
            
            <c:if test="${not empty error}">
                <div class="error-message">${error}</div>
            </c:if>
            
            <form method="post" action="${pageContext.request.contextPath}/quest-builder/">
                <input type="hidden" name="action" value="createQuest">
                
                <div class="form-group">
                    <label for="title">Название квеста *</label>
                    <input type="text" id="title" name="title" required maxlength="200" 
                           placeholder="Введите название квеста">
                    <div class="help-text">Краткое, запоминающееся название (до 200 символов)</div>
                </div>
                
                <div class="form-group">
                    <label for="description">Описание квеста *</label>
                    <textarea id="description" name="description" required 
                              placeholder="Введите описание квеста&#10;&#10;Объясните суть квеста, его тематику и цель."></textarea>
                    <div class="help-text">Подробное описание, которое увидят игроки</div>
                </div>
                
                <div class="btn-group">
                    <button type="submit" class="btn btn-primary">Создать квест</button>
                    <a href="${pageContext.request.contextPath}/quest-editor/" class="btn btn-secondary">Отмена</a>
                </div>
            </form>
            
            <div style="margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid #ddd;">
                <h3 style="color: #1a365d; margin-bottom: 1rem;">Как создать квест?</h3>
                <ol style="color: #666; line-height: 1.8;">
                    <li>Создайте квест с названием и описанием</li>
                    <li>Добавьте этапы: начальный, промежуточные, победные (WIN) и проигрышные (LOSE)</li>
                    <li>Каждый промежуточный этап должен иметь ровно <strong>2 ответа</strong></li>
                    <li>Каждый ответ должен вести к другому этапу или к WIN/LOSE</li>
                    <li>После завершения структуры нажмите "Опубликовать"</li>
                </ol>
            </div>
        </div>
    </div>
</body>
</html>
