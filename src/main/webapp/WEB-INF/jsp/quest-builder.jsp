<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Конструктор квеста: ${quest.title}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        * {
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
            background: #f5f7fa;
            margin: 0;
            padding: 0;
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 2rem;
        }
        
        .header {
            background: white;
            padding: 1.5rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .header h1 {
            margin: 0 0 0.5rem 0;
            color: #1a365d;
        }
        
        .quest-info {
            color: #666;
            font-size: 0.9rem;
        }
        
        .status-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: white;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .status-info {
            display: flex;
            gap: 2rem;
            align-items: center;
        }
        
        .status-badge {
            padding: 0.5rem 1rem;
            border-radius: 20px;
            font-weight: 600;
            font-size: 0.875rem;
        }
        
        .status-draft {
            background: #fef3c7;
            color: #92400e;
        }
        
        .status-published {
            background: #d1fae5;
            color: #065f46;
        }
        
        .validation-status {
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .validation-valid {
            color: #059669;
        }
        
        .validation-invalid {
            color: #dc2626;
        }
        
        .btn {
            padding: 0.75rem 1.5rem;
            border: none;
            border-radius: 4px;
            font-size: 0.875rem;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn-primary {
            background: #1a365d;
            color: white;
        }
        
        .btn-primary:hover:not(:disabled) {
            background: #152a48;
        }
        
        .btn-success {
            background: #059669;
            color: white;
        }
        
        .btn-success:hover:not(:disabled) {
            background: #047857;
        }
        
        .btn-danger {
            background: #dc2626;
            color: white;
        }
        
        .btn-danger:hover {
            background: #b91c1c;
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #545b62;
        }
        
        .btn:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }
        
        .actions {
            display: flex;
            gap: 1rem;
        }
        
        .main-content {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 2rem;
        }
        
        .stages-panel {
            background: white;
            border-radius: 8px;
            padding: 1.5rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            max-height: calc(100vh - 300px);
            overflow-y: auto;
        }
        
        .panel-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1.5rem;
            padding-bottom: 1rem;
            border-bottom: 2px solid #e5e7eb;
        }
        
        .panel-header h2 {
            margin: 0;
            color: #1a365d;
        }
        
        .stage-list {
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }
        
        .stage-card {
            border: 2px solid #e5e7eb;
            border-radius: 6px;
            padding: 1rem;
            transition: all 0.3s;
            cursor: pointer;
        }
        
        .stage-card:hover {
            border-color: #1a365d;
            box-shadow: 0 2px 8px rgba(26, 54, 93, 0.1);
        }
        
        .stage-card.active {
            border-color: #1a365d;
            background: #f0f4f8;
        }
        
        .stage-card-header {
            display: flex;
            justify-content: space-between;
            align-items: start;
            margin-bottom: 0.5rem;
        }
        
        .stage-title {
            font-weight: 600;
            color: #1a365d;
            margin: 0;
        }
        
        .stage-type {
            padding: 0.25rem 0.5rem;
            border-radius: 4px;
            font-size: 0.75rem;
            font-weight: 600;
        }
        
        .type-start {
            background: #dbeafe;
            color: #1e40af;
        }
        
        .type-intermediate {
            background: #e0e7ff;
            color: #4338ca;
        }
        
        .type-win {
            background: #d1fae5;
            color: #065f46;
        }
        
        .type-lose {
            background: #fee2e2;
            color: #991b1b;
        }
        
        .stage-description {
            font-size: 0.875rem;
            color: #666;
            margin: 0.5rem 0;
        }
        
        .stage-options {
            font-size: 0.75rem;
            color: #999;
            margin-top: 0.5rem;
        }
        
        .editor-panel {
            background: white;
            border-radius: 8px;
            padding: 1.5rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            max-height: calc(100vh - 300px);
            overflow-y: auto;
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
        .form-group textarea,
        .form-group select {
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 1rem;
        }
        
        .form-group textarea {
            min-height: 100px;
            resize: vertical;
        }
        
        .form-group input:focus,
        .form-group textarea:focus,
        .form-group select:focus {
            outline: none;
            border-color: #1a365d;
            box-shadow: 0 0 0 3px rgba(26, 54, 93, 0.1);
        }
        
        .help-text {
            font-size: 0.875rem;
            color: #666;
            margin-top: 0.25rem;
        }
        
        .options-section {
            background: #f9fafb;
            padding: 1rem;
            border-radius: 6px;
            margin-top: 1rem;
        }
        
        .options-section h4 {
            margin: 0 0 1rem 0;
            color: #1a365d;
        }
        
        .option-group {
            background: white;
            padding: 1rem;
            border-radius: 4px;
            margin-bottom: 1rem;
            border: 1px solid #e5e7eb;
        }
        
        .option-group:last-child {
            margin-bottom: 0;
        }
        
        .option-header {
            font-weight: 600;
            color: #1a365d;
            margin-bottom: 0.75rem;
        }
        
        .btn-group {
            display: flex;
            gap: 1rem;
            margin-top: 1.5rem;
        }
        
        .empty-state {
            text-align: center;
            padding: 3rem;
            color: #999;
        }
        
        .empty-state-icon {
            font-size: 3rem;
            margin-bottom: 1rem;
        }
        
        .validation-errors {
            background: #fee2e2;
            border: 1px solid #fecaca;
            border-radius: 6px;
            padding: 1rem;
            margin-bottom: 1.5rem;
        }
        
        .validation-errors h4 {
            margin: 0 0 0.75rem 0;
            color: #991b1b;
        }
        
        .validation-errors ul {
            margin: 0;
            padding-left: 1.5rem;
            color: #991b1b;
        }
        
        .validation-errors li {
            margin-bottom: 0.5rem;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Конструктор квеста: ${quest.title}</h1>
            <div class="quest-info">
                ${quest.description}<br>
                <strong>Статус:</strong> ${quest.status.displayName} | <strong>Этапов:</strong> ${stages.size()}
            </div>
        </div>
        
        <div class="status-bar">
            <div class="status-info">
                <span class="status-badge ${quest.status == 'DRAFT' ? 'status-draft' : 'status-published'}">
                    ${quest.status.displayName}
                </span>
                <c:if test="${validationResult != null}">
                    <div class="validation-status ${validationResult.valid ? 'validation-valid' : 'validation-invalid'}">
                        ${validationResult.valid ? '✓ Квест готов к публикации' : '✗ Квест требует доработки'}
                    </div>
                </c:if>
                <span style="color: #666; font-size: 0.875rem;">${questStatusInfo}</span>
            </div>
            <div class="actions">
                <c:choose>
                    <c:when test="${quest.status == 'DRAFT'}">
                        <button onclick="publishQuest()" class="btn btn-success" 
                                ${!validationResult.valid ? 'disabled' : ''}>
                            Опубликовать
                        </button>
                    </c:when>
                    <c:otherwise>
                        <button onclick="unpublishQuest()" class="btn btn-secondary">
                            Снять с публикации
                        </button>
                    </c:otherwise>
                </c:choose>
                <button onclick="validateQuest()" class="btn btn-primary">Проверить</button>
                <a href="${pageContext.request.contextPath}/quest-editor/" class="btn btn-secondary">Назад</a>
            </div>
        </div>
        
        <c:if test="${validationResult != null && !validationResult.valid}">
            <div class="validation-errors">
                <h4>Ошибки валидации:</h4>
                <ul>
                    <c:forEach var="error" items="${validationResult.errors}">
                        <li>${error}</li>
                    </c:forEach>
                </ul>
            </div>
        </c:if>
        
        <div class="main-content">
            <div class="stages-panel">
                <div class="panel-header">
                    <h2>Этапы квеста</h2>
                    <button onclick="showNewStageForm()" class="btn btn-primary">+ Добавить этап</button>
                </div>
                
                <c:choose>
                    <c:when test="${!empty stages}">
                        <div class="stage-list" id="stageList">
                            <c:forEach var="stage" items="${stages}">
                                <div class="stage-card" onclick="editStage('${stage.stageId}')" data-stage-id="${stage.stageId}">
                                    <div class="stage-card-header">
                                        <h3 class="stage-title">${stage.title}</h3>
                                        <span class="stage-type ${stage.isStartStage() ? 'type-start' : (stage.isEndStage() ? (stage.isWinStage() ? 'type-win' : 'type-lose') : 'type-intermediate')}">
                                            ${stage.isStartStage() ? 'СТАРТ' : (stage.isEndStage() ? (stage.isWinStage() ? 'WIN' : 'LOSE') : 'Промежуточный')}
                                        </span>
                                    </div>
                                    <p class="stage-description">${stage.description}</p>
                                    <div class="stage-options">
                                        <c:choose>
                                            <c:when test="${!empty stage.options}">
                                                Ответов: ${stage.options.size()} / 2 ${stage.options.size() == 2 ? '✓' : '⚠'}
                                            </c:when>
                                            <c:when test="${stage.isEndStage()}">
                                                Конечный этап (без ответов)
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: #dc2626;">⚠ Нет ответов</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <div class="empty-state-icon">📋</div>
                            <p>Нет этапов. Создайте первый этап квеста.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            
            <div class="editor-panel">
                <div id="editorContent">
                    <div class="empty-state">
                        <div class="empty-state-icon">👈</div>
                        <p>Выберите этап для редактирования или создайте новый</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const questId = ${quest.id};
        
        function showNewStageForm() {
            const editorContent = document.getElementById('editorContent');
            editorContent.innerHTML = `
                <h2>Создать новый этап</h2>
                <form id="stageForm" onsubmit="saveStage(event)">
                    <input type="hidden" name="stageId" value="">
                    
                    <div class="form-group">
                        <label for="title">Текст этапа *</label>
                        <textarea id="title" name="title" required maxlength="1000" style="min-height: 100px;"></textarea>
                        <div class="help-text">Текст, который увидит игрок на этом этапе</div>
                    </div>
                    
                    <div id="optionsSection" class="options-section">
                        <h4>Ответы (ровно 2)</h4>
                        
                        <div class="option-group">
                            <div class="option-header">Ответ 1</div>
                            <div class="form-group">
                                <label for="option1Text">Текст ответа *</label>
                                <input type="text" id="option1Text" name="option1Text" required maxlength="500">
                            </div>
                            <div class="form-group">
                                <label for="option1ResultType">Результат</label>
                                <select id="option1ResultType" name="option1ResultType" onchange="toggleOption1Target()">
                                    <option value="">-- Не указан --</option>
                                    <option value="NONE">Переход к этапу</option>
                                    <option value="WIN">WIN - Победа</option>
                                    <option value="LOSE">LOSE - Поражение</option>
                                </select>
                                <div class="help-text">Можно заполнить позже перед публикацией</div>
                            </div>
                            <div class="form-group" id="option1TargetGroup">
                                <label for="option1Target">Ведет к этапу</label>
                                <select id="option1Target" name="option1Target">
                                    <option value="">-- Выберите этап --</option>
                                </select>
                                <div class="help-text">Обязательно при результате "Переход к этапу"</div>
                            </div>
                        </div>
                        
                        <div class="option-group">
                            <div class="option-header">Ответ 2</div>
                            <div class="form-group">
                                <label for="option2Text">Текст ответа *</label>
                                <input type="text" id="option2Text" name="option2Text" required maxlength="500">
                            </div>
                            <div class="form-group">
                                <label for="option2ResultType">Результат</label>
                                <select id="option2ResultType" name="option2ResultType" onchange="toggleOption2Target()">
                                    <option value="">-- Не указан --</option>
                                    <option value="NONE">Переход к этапу</option>
                                    <option value="WIN">WIN - Победа</option>
                                    <option value="LOSE">LOSE - Поражение</option>
                                </select>
                                <div class="help-text">Можно заполнить позже перед публикацией</div>
                            </div>
                            <div class="form-group" id="option2TargetGroup">
                                <label for="option2Target">Ведет к этапу</label>
                                <select id="option2Target" name="option2Target">
                                    <option value="">-- Выберите этап --</option>
                                </select>
                                <div class="help-text">Обязательно при результате "Переход к этапу"</div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="btn-group">
                        <button type="submit" class="btn btn-success">Сохранить этап</button>
                        <button type="button" onclick="clearEditor()" class="btn btn-secondary">Отмена</button>
                    </div>
                </form>
            `;
            populateStageDropdowns();
            toggleOption1Target();
            toggleOption2Target();
        }
        
        function populateStageDropdowns() {
            const option1Select = document.getElementById('option1Target');
            const option2Select = document.getElementById('option2Target');
            
            if (!option1Select || !option2Select) return;
            
            // Get current stage ID to exclude it from options
            const currentStageId = document.querySelector('input[name="stageId"]')?.value;
            
            // Clear and repopulate
            option1Select.innerHTML = '<option value="">-- Выберите этап --</option>';
            option2Select.innerHTML = '<option value="">-- Выберите этап --</option>';
            
            document.querySelectorAll('.stage-card').forEach(card => {
                const stageId = card.getAttribute('data-stage-id');
                const title = card.querySelector('.stage-title').textContent;
                
                // Don't allow linking to self
                if (stageId !== currentStageId) {
                    const option1 = new Option(`${'$'}{stageId} - ${'$'}{title}`, stageId);
                    const option2 = new Option(`${'$'}{stageId} - ${'$'}{title}`, stageId);
                    option1Select.add(option1);
                    option2Select.add(option2);
                }
            });
        }
        
        function editStage(stageId) {
            // Highlight selected stage
            document.querySelectorAll('.stage-card').forEach(card => {
                card.classList.remove('active');
            });
            document.querySelector(`[data-stage-id="${'$'}{stageId}"]`).classList.add('active');
            
            // Load stage data
            fetch(`${'$'}{contextPath}/quest-builder/?questId=${'$'}{questId}&action=getStage&stageId=${'$'}{stageId}`)
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        showEditForm(data);
                    } else {
                        alert('Ошибка: ' + data.error);
                    }
                })
                .catch(error => {
                    console.error('Error loading stage:', error);
                    alert('Ошибка загрузки этапа');
                });
        }
        
        function showEditForm(stageData) {
            const editorContent = document.getElementById('editorContent');
            
            editorContent.innerHTML = `
                <h2>Редактировать этап</h2>
                <form id="stageForm" onsubmit="saveStage(event)">
                    <input type="hidden" name="stageId" value="${'$'}{stageData.stageId}">
                    
                    <div class="form-group">
                        <label>ID этапа</label>
                        <input type="text" value="${'$'}{stageData.stageId}" disabled 
                               style="background: #f3f4f6; cursor: not-allowed;">
                        <div class="help-text">ID этапа нельзя изменить после создания</div>
                    </div>
                    
                    <div class="form-group">
                        <label for="title">Текст этапа *</label>
                        <textarea id="title" name="title" required maxlength="1000" style="min-height: 100px;">${'$'}{stageData.title}</textarea>
                        <div class="help-text">Текст, который увидит игрок на этом этапе</div>
                    </div>
                    
                    <div id="optionsSection" class="options-section">
                        <h4>Ответы (ровно 2)</h4>
                        
                        <div class="option-group">
                            <div class="option-header">Ответ 1</div>
                            <div class="form-group">
                                <label for="option1Text">Текст ответа *</label>
                                <input type="text" id="option1Text" name="option1Text" required maxlength="500" 
                                       value="${'$'}{stageData.option1Text || ''}">
                            </div>
                            <div class="form-group">
                                <label for="option1ResultType">Результат</label>
                                <select id="option1ResultType" name="option1ResultType" onchange="toggleOption1Target()">
                                    <option value="" ${'$'}{!stageData.option1ResultType ? 'selected' : ''}>-- Не указан --</option>
                                    <option value="NONE" ${'$'}{stageData.option1ResultType == 'NONE' ? 'selected' : ''}>Переход к этапу</option>
                                    <option value="WIN" ${'$'}{stageData.option1ResultType == 'WIN' ? 'selected' : ''}>WIN - Победа</option>
                                    <option value="LOSE" ${'$'}{stageData.option1ResultType == 'LOSE' ? 'selected' : ''}>LOSE - Поражение</option>
                                </select>
                                <div class="help-text">Можно заполнить позже перед публикацией</div>
                            </div>
                            <div class="form-group" id="option1TargetGroup">
                                <label for="option1Target">Ведет к этапу</label>
                                <select id="option1Target" name="option1Target">
                                    <option value="">-- Выберите этап --</option>
                                </select>
                                <div class="help-text">Обязательно при результате "Переход к этапу"</div>
                            </div>
                        </div>
                        
                        <div class="option-group">
                            <div class="option-header">Ответ 2</div>
                            <div class="form-group">
                                <label for="option2Text">Текст ответа *</label>
                                <input type="text" id="option2Text" name="option2Text" required maxlength="500"
                                       value="${'$'}{stageData.option2Text || ''}">
                            </div>
                            <div class="form-group">
                                <label for="option2ResultType">Результат</label>
                                <select id="option2ResultType" name="option2ResultType" onchange="toggleOption2Target()">
                                    <option value="" ${'$'}{!stageData.option2ResultType ? 'selected' : ''}>-- Не указан --</option>
                                    <option value="NONE" ${'$'}{stageData.option2ResultType == 'NONE' ? 'selected' : ''}>Переход к этапу</option>
                                    <option value="WIN" ${'$'}{stageData.option2ResultType == 'WIN' ? 'selected' : ''}>WIN - Победа</option>
                                    <option value="LOSE" ${'$'}{stageData.option2ResultType == 'LOSE' ? 'selected' : ''}>LOSE - Поражение</option>
                                </select>
                                <div class="help-text">Можно заполнить позже перед публикацией</div>
                            </div>
                            <div class="form-group" id="option2TargetGroup">
                                <label for="option2Target">Ведет к этапу</label>
                                <select id="option2Target" name="option2Target">
                                    <option value="">-- Выберите этап --</option>
                                </select>
                                <div class="help-text">Обязательно при результате "Переход к этапу"</div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="btn-group">
                        <button type="submit" class="btn btn-success">Сохранить изменения</button>
                        <button type="button" onclick="deleteStageConfirm('${'$'}{stageData.stageId}')" class="btn btn-danger">Удалить этап</button>
                        <button type="button" onclick="clearEditor()" class="btn btn-secondary">Отмена</button>
                    </div>
                </form>
            `;
            populateStageDropdowns();
            
            // Set selected values after population
            if (stageData.option1Target && stageData.option1Target !== 'PENDING') {
                document.getElementById('option1Target').value = stageData.option1Target;
            }
            if (stageData.option2Target && stageData.option2Target !== 'PENDING') {
                document.getElementById('option2Target').value = stageData.option2Target;
            }
            
            toggleOption1Target();
            toggleOption2Target();
        }
        
        function deleteStageConfirm(stageId) {
            if (!confirm('Вы уверены, что хотите удалить этот этап? Это действие нельзя отменить.')) {
                return;
            }
            
            const formData = new FormData();
            formData.append('action', 'deleteStage');
            formData.append('questId', questId);
            formData.append('stageId', stageId);
            
            fetch(`${contextPath}/quest-builder`, {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    window.location.reload();
                } else {
                    alert('Ошибка: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Error deleting stage:', error);
                alert('Ошибка удаления этапа');
            });
        }
        
        function toggleOption1Target() {
            const resultType = document.getElementById('option1ResultType')?.value;
            const targetGroup = document.getElementById('option1TargetGroup');
            const targetSelect = document.getElementById('option1Target');
            
            if (!targetGroup || !targetSelect) return;
            
            if (resultType === 'NONE') {
                targetGroup.style.display = 'block';
            } else {
                // WIN, LOSE, or empty - hide target selection
                targetGroup.style.display = 'none';
                targetSelect.value = '';
            }
        }
        
        function toggleOption2Target() {
            const resultType = document.getElementById('option2ResultType')?.value;
            const targetGroup = document.getElementById('option2TargetGroup');
            const targetSelect = document.getElementById('option2Target');
            
            if (!targetGroup || !targetSelect) return;
            
            if (resultType === 'NONE') {
                targetGroup.style.display = 'block';
            } else {
                // WIN, LOSE, or empty - hide target selection
                targetGroup.style.display = 'none';
                targetSelect.value = '';
            }
        }
        
        function saveStage(event) {
            event.preventDefault();
            
            const formData = new FormData(event.target);
            
            // Explicitly set required parameters
            formData.set('action', 'saveStage');
            formData.set('questId', questId.toString());
            
            // Log what we're sending for debugging
            console.log('Saving stage with data:');
            for (let [key, value] of formData.entries()) {
                console.log(`  ${key}: ${value}`);
            }
            
            // Disable submit button to prevent double-submit
            const submitBtn = event.target.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Сохранение...';
            }
            
            fetch(`${contextPath}/quest-builder`, {
                method: 'POST',
                body: formData
            })
                .then(response => {
                    console.log('Response status:', response.status);
                    console.log('Response headers:', response.headers.get('content-type'));
                    return response.text().then(text => {
                        console.log('Response text:', text);
                        try {
                            return JSON.parse(text);
                        } catch (e) {
                            throw new Error('Invalid JSON response: ' + text.substring(0, 200));
                        }
                    });
                })
                .then(data => {
                    if (data.success) {
                        alert(data.message);
                        location.reload();
                    } else {
                        alert('Ошибка: ' + data.error);
                        if (submitBtn) {
                            submitBtn.disabled = false;
                            submitBtn.textContent = 'Сохранить этап';
                        }
                    }
                })
                .catch(error => {
                    console.error('Error saving stage:', error);
                    alert('Произошла ошибка: ' + error.message);
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.textContent = 'Сохранить этап';
                    }
                });
        }
        
        function publishQuest() {
            if (!confirm('Опубликовать квест? После публикации игроки смогут его проходить.')) {
                return;
            }
            
            const formData = new FormData();
            formData.append('action', 'publish');
            formData.append('questId', questId);
            
            fetch(`${contextPath}/quest-builder`, {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    window.location.reload();
                } else {
                    alert('Ошибка: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Error publishing quest:', error);
                alert('Ошибка публикации квеста');
            });
        }
        
        function unpublishQuest() {
            if (!confirm('Снять квест с публикации? Игроки не смогут его проходить.')) {
                return;
            }
            
            const formData = new FormData();
            formData.append('action', 'unpublish');
            formData.append('questId', questId);
            
            fetch(`${contextPath}/quest-builder`, {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    window.location.reload();
                } else {
                    alert('Ошибка: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Error unpublishing quest:', error);
                alert('Ошибка снятия квеста с публикации');
            });
        }
        
        function validateQuest() {
            fetch(`${'$'}{contextPath}/quest-builder/?questId=${'$'}{questId}&action=validate`)
                .then(response => response.json())
                .then(data => {
                    if (data.valid) {
                        alert('✓ Квест валиден и готов к публикации!\n\n' + data.statusInfo);
                    } else {
                        alert('✗ Квест содержит ошибки:\n\n' + data.errors.join('\n'));
                    }
                })
                .catch(error => {
                    console.error('Error validating quest:', error);
                    alert('Ошибка валидации квеста');
                });
        }
        
        function clearEditor() {
            const editorContent = document.getElementById('editorContent');
            editorContent.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">👈</div>
                    <p>Выберите этап для редактирования или создайте новый</p>
                </div>
            `;
            document.querySelectorAll('.stage-card').forEach(card => {
                card.classList.remove('active');
            });
        }
    </script>
</body>
</html>
