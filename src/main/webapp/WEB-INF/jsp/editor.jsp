<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Редактор квеста</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/toastify-js/src/toastify.min.css">
    <style>
        .stage-card {
            border: 1px solid #dee2e6;
            border-radius: 8px;
            margin-bottom: 20px;
            transition: all 0.3s ease;
        }
        .stage-card:hover {
            box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
        }
        .stage-card .card-header {
            background-color: #f8f9fa;
            font-weight: 500;
        }
        .stage-card .card-body {
            padding: 1.25rem;
        }
        .stage-error-highlight {
            border-color: #dc3545 !important;
            box-shadow: 0 0 0 0.25rem rgba(220, 53, 69, 0.25) !important;
            animation: pulse-border 1s ease-in-out 3;
        }
        @keyframes pulse-border {
            0% { box-shadow: 0 0 0 0.25rem rgba(220, 53, 69, 0.25); }
            50% { box-shadow: 0 0 0 0.5rem rgba(220, 53, 69, 0.35); }
            100% { box-shadow: 0 0 0 0.25rem rgba(220, 53, 69, 0.25); }
        }
        .option-item {
            background-color: #f8f9fa;
            border-radius: 4px;
            padding: 8px 12px;
            margin-bottom: 8px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .btn-add-option {
            width: 100%;
            margin-top: 10px;
        }
        .stage-actions {
            display: flex;
            gap: 5px;
            justify-content: flex-end;
        }
        .stage-list {
            max-height: 80vh;
            overflow-y: auto;
        }
        .stage-preview {
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            height: 100%;
        }
    </style>
</head>
<body>
    <!-- Toast container -->
    <div id="toast" class="toast align-items-center text-white position-fixed top-0 end-0 m-3" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
            <div class="toast-body"></div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    </div>

    <div class="container-fluid py-4">
        <!-- Error Message -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- Success Message -->
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="row mb-4">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center">
                    <h1>Редактор квеста</h1>
                    <div>
                        <a href="${pageContext.request.contextPath}/welcome" class="btn btn-outline-secondary me-2">
                            <i class="bi bi-arrow-left"></i> Назад в игру
                        </a>
                        <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#addStageModal">
                            <i class="bi bi-plus-lg"></i> Добавить этап
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">Этапы квеста</h5>
                        <small class="text-muted">Перетащите для изменения порядка</small>
                    </div>
                    <div class="card-body p-0">
                        <div class="list-group list-group-flush stage-list" id="stage-list">
                            <c:forEach items="${stages}" var="stage">
                                <div class="list-group-item list-group-item-action p-0 border-0 mb-2 stage-card" 
                                     id="stage-${stage.key}" data-stage-id="${stage.key}">
                                    <div class="card">
                                        <div class="card-header d-flex justify-content-between align-items-center">
                                            <div class="d-flex align-items-center">
                                                <span class="drag-handle me-2" style="cursor: move;">
                                                    <i class="bi bi-grip-vertical"></i>
                                                </span>
                                                <span class="badge bg-primary me-2">${stage.value.stageIndex}</span>
                                                <span class="stage-title me-2">${stage.value.title}</span>
                                                <c:if test="${quest.startStageId eq stage.key}">
                                                    <span class="badge bg-success">Старт</span>
                                                </c:if>
                                            </div>
                                            <div class="stage-actions">
                                                <button class="btn btn-sm btn-outline-primary edit-stage me-1" 
                                                        data-stage-id="${stage.key}"
                                                        title="Редактировать">
                                                    <i class="bi bi-pencil"></i>
                                                </button>
                                                <button class="btn btn-sm btn-outline-danger delete-stage" 
                                                        data-stage-id="${stage.key}"
                                                        title="Удалить">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </div>
                                        </div>
                                        <div class="card-body">
                                            <p class="card-text small text-muted mb-2">${stage.value.description}</p>
                                            <c:if test="${not empty stage.value.options}">
                                                <div class="options-list">
                                                    <h6 class="small mb-2"><i class="bi bi-arrow-right-circle"></i> Варианты выбора:</h6>
                                                    <c:set var="optionIndex" value="1" />
                                                    <c:forEach items="${stage.value.options}" var="option">
                                                        <div class="d-flex align-items-center mb-1">
                                                            <span class="badge bg-info me-2">${stage.value.stageIndex}.${optionIndex}</span>
                                                            <span class="badge bg-secondary me-2">${option.value}</span>
                                                            <i class="bi bi-arrow-right me-2"></i>
                                                            <small class="text-muted">${option.key}</small>
                                                        </div>
                                                        <c:set var="optionIndex" value="${optionIndex + 1}" />
                                                    </c:forEach>
                                                </div>
                                            </c:if>
                                            <c:if test="${empty stage.value.options}">
                                                <small class="text-muted"><i class="bi bi-info-circle"></i> Нет вариантов выбора (конечный этап)</small>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-md-8">
                <div class="stage-preview">
                    <div class="text-center py-5" id="no-stage-selected">
                        <i class="bi bi-journal-text" style="font-size: 3rem; color: #6c757d;"></i>
                        <h4 class="mt-3">Выберите этап для редактирования</h4>
                        <p class="text-muted">Нажмите на кнопку редактирования рядом с этапом, чтобы начать</p>
                    </div>
                    
                    <div id="stage-edit-form" style="display: none;">
                        <div class="alert alert-info mb-3">
                            <i class="bi bi-info-circle"></i> Редактирование этапа <strong><span id="currentStageIndex"></span></strong>
                        </div>
                        <form id="editStageForm">
                            <input type="hidden" id="stageId" name="stageId">
                            <div class="mb-3">
                                <label for="stageTitle" class="form-label">Название этапа</label>
                                <input type="text" class="form-control" id="stageTitle" name="stageTitle" required>
                            </div>
                            <div class="mb-3">
                                <label for="stageDescription" class="form-label">Описание</label>
                                <textarea class="form-control" id="stageDescription" name="stageDescription" rows="4" required></textarea>
                            </div>
                            
                            <div class="mb-3">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <label class="form-label mb-0"><i class="bi bi-arrow-down-circle"></i> Родительские этапы (что ведет К этому этапу)</label>
                                    <button type="button" class="btn btn-sm btn-outline-success add-parent">
                                        <i class="bi bi-plus"></i> Добавить родителя
                                    </button>
                                </div>
                                <small class="text-muted">Укажите, из каких этапов можно попасть на этот этап</small>
                                <div id="parents-container" class="mt-2">
                                    <!-- Parents will be added here dynamically -->
                                </div>
                            </div>
                            
                            <hr>
                            
                            <div class="mb-3">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <label class="form-label mb-0"><i class="bi bi-arrow-right-circle"></i> Дочерние этапы (куда ведет ЭТОТ этап)</label>
                                    <button type="button" class="btn btn-sm btn-outline-primary add-child">
                                        <i class="bi bi-plus"></i> Добавить вариант
                                    </button>
                                </div>
                                <small class="text-muted">Укажите, на какие этапы можно перейти с этого этапа</small>
                                <div id="children-container" class="mt-2">
                                    <!-- Children will be added here dynamically -->
                                </div>
                            </div>
                            
                            <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                                <button type="button" class="btn btn-secondary me-md-2" id="cancelEdit">Отмена</button>
                                <button type="submit" class="btn btn-primary">Сохранить изменения</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Add Stage Modal -->
    <div class="modal fade" id="addStageModal" tabindex="-1" aria-labelledby="addStageModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="addStageModalLabel">Добавить новый этап</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form id="addStageForm" action="${pageContext.request.contextPath}/editor?action=add" method="post">
                    <div class="modal-body">
                        <!-- Поле ID удалено, теперь ID будет генерироваться автоматически -->
                        <div class="mb-3">
                            <label for="newStageTitle" class="form-label">Название</label>
                            <input type="text" class="form-control" id="newStageTitle" name="title" required>
                        </div>
                        <div class="mb-3">
                            <label for="newStageDescription" class="form-label">Описание</label>
                            <textarea class="form-control" id="newStageDescription" name="description" rows="3" required></textarea>
                        </div>
                        <div class="mb-3">
                            <div class="alert alert-info mb-0">
                                <i class="bi bi-info-circle"></i>
                                <small>Варианты выбора можно добавить после создания этапа в режиме редактирования.</small>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Отмена</button>
                        <button type="submit" class="btn btn-primary">Создать этап</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/toastify-js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sortablejs@1.15.0/Sortable.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Initialize toast
            const toastEl = document.getElementById('toast');
            const toast = new bootstrap.Toast(toastEl, { autohide: true, delay: 5000 });
            const toastBody = toastEl.querySelector('.toast-body');
            
            // If backend set an errorStageId, highlight and scroll to that stage
            const errorStageId = '${errorStageId}';
            if (errorStageId) {
                const stageEl = document.getElementById('stage-' + errorStageId);
                if (stageEl) {
                    stageEl.classList.add('stage-error-highlight');
                    stageEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    setTimeout(() => stageEl.classList.remove('stage-error-highlight'), 4000);
                }
            }

            // If there's a server error message, show it as a toast as well
            const serverError = `${error}`;
            if (serverError && serverError.trim().length > 0) {
                showToast(serverError, 'danger');
            }
            
            // Make stages sortable
            const stageList = document.getElementById('stage-list');
            if (stageList) {
                new Sortable(stageList, {
                    animation: 150,
                    handle: '.drag-handle',
                    onEnd: function() {
                        const stageOrder = Array.from(stageList.children).map(el => el.dataset.stageId);
                        saveStageOrder(stageOrder);
                    }
                });
            }
            
            // Save the new stage order to the server
            function saveStageOrder(stageOrder) {
                const params = stageOrder.map(id => 'stageOrder[]=' + encodeURIComponent(id)).join('&');
                fetch('editor', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'action=reorder&' + params
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        showToast('Stage order updated successfully - reloading...', 'success');
                        // Reload the page to refresh stage indexes
                        setTimeout(() => {
                            window.location.reload();
                        }, 1000);
                    } else {
                        showToast('Failed to update stage order: ' + (data.error || 'Unknown error'), 'danger');
                    }
                })
                .catch(error => {
                    console.error('Error updating stage order:', error);
                    showToast('Error updating stage order: ' + error.message, 'danger');
                });
            }
            
            // Show toast message
            function showToast(message, type = 'success') {
                toastBody.textContent = message;
                toastEl.className = 'toast align-items-center text-white bg-' + type + ' position-fixed top-0 end-0 m-3';
                toast.show();
            }

            // Load stage data for editing
            function loadStageForEdit(stageId) {
                fetch('${pageContext.request.contextPath}/editor?edit=' + stageId, {
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.error) {
                            showToast(data.error, 'danger');
                            return;
                        }
                        
                        const form = document.getElementById('editStageForm');
                        const noStageSelected = document.getElementById('no-stage-selected');
                        const stageEditForm = document.getElementById('stage-edit-form');
                        
                        // Show the edit form and hide the placeholder
                        noStageSelected.style.display = 'none';
                        stageEditForm.style.display = 'block';
                        
                        // Fill the form with stage data
                        form.elements['stageId'].value = stageId;
                        form.elements['stageTitle'].value = data.title || '';
                        form.elements['stageDescription'].value = data.description || '';
                        
                        // Update the current stage index display
                        const currentStageIndexSpan = document.getElementById('currentStageIndex');
                        if (currentStageIndexSpan && data.stageIndex) {
                            currentStageIndexSpan.textContent = '#' + data.stageIndex;
                        }
                        
                        // Clear existing parents and children
                        const parentsContainer = document.getElementById('parents-container');
                        const childrenContainer = document.getElementById('children-container');
                        parentsContainer.innerHTML = '';
                        childrenContainer.innerHTML = '';
                        
                        // Add parents (stages that link TO this stage)
                        if (data.parents && typeof data.parents === 'object') {
                            Object.entries(data.parents).forEach(([parentId, buttonText]) => {
                                addParentToForm(parentId, buttonText);
                            });
                        }
                        
                        // Add children (stages this stage links TO)
                        if (data.children && typeof data.children === 'object') {
                            Object.entries(data.children).forEach(([childId, buttonText]) => {
                                addChildToForm(childId, buttonText);
                            });
                        }
                        
                        // Scroll to the form
                        stageEditForm.scrollIntoView({ behavior: 'smooth' });
                    })
                    .catch(error => {
                        console.error('Error loading stage:', error);
                        showToast('Ошибка при загрузке этапа', 'danger');
                    });
            }
            
            // Get all available stages for dropdowns
            function getAvailableStages(excludeStageId = null) {
                return Array.from(document.querySelectorAll('#stage-list .stage-card'))
                    .map(card => {
                        const stageId = card.dataset.stageId;
                        const stageTitle = card.querySelector('.stage-title')?.textContent.trim();
                        const stageIndexBadge = card.querySelector('.badge.bg-primary');
                        const stageIndex = stageIndexBadge ? stageIndexBadge.textContent.trim() : '';
                        return { id: stageId, title: stageTitle, index: stageIndex };
                    })
                    .filter(stage => stage.id !== excludeStageId);
            }
            
            // Add parent to the edit form
            function addParentToForm(parentId = '', buttonText = '') {
                const parentsContainer = document.getElementById('parents-container');
                const currentStageId = document.getElementById('stageId').value;
                const stageOptions = getAvailableStages(currentStageId);

                let optionsHtml = '';
                stageOptions.forEach(stage => {
                    const selected = stage.id === parentId ? 'selected' : '';
                    const displayText = stage.index ? '[' + stage.index + '] ' + stage.title : stage.id + ' - ' + stage.title;
                    optionsHtml += '<option value="' + stage.id + '" ' + selected + '>' + displayText + '</option>';
                });

                const parentHtml = 
                    '<div class="option-item mb-2 bg-light p-2 rounded">' +
                        '<div class="row g-2 align-items-center">' +
                            '<div class="col-md-5">' +
                                '<select class="form-select form-select-sm" name="parentIds" required>' +
                                    '<option value="">Выберите родительский этап...</option>' +
                                    optionsHtml +
                                '</select>' +
                            '</div>' +
                            '<div class="col-md-6">' +
                                '<input type="text" class="form-control form-control-sm"' +
                                       ' name="parentButtonTexts" placeholder="Текст кнопки на родителе"' +
                                       ' value="' + buttonText + '" required>' +
                            '</div>' +
                            '<div class="col-md-1">' +
                                '<button type="button" class="btn btn-sm btn-outline-danger remove-parent"' +
                                        ' title="Удалить родителя">' +
                                    '<i class="bi bi-trash"></i>' +
                                '</button>' +
                            '</div>' +
                        '</div>' +
                    '</div>';
                
                parentsContainer.insertAdjacentHTML('beforeend', parentHtml);
                parentsContainer.lastElementChild.querySelector('.remove-parent').addEventListener('click', function() {
                    this.closest('.option-item').remove();
                });
            }
            
            // Add child to the edit form
            function addChildToForm(childId = '', buttonText = '') {
                const childrenContainer = document.getElementById('children-container');
                const currentStageId = document.getElementById('stageId').value;
                const stageOptions = getAvailableStages(currentStageId);

                let optionsHtml = '';
                stageOptions.forEach(stage => {
                    const selected = stage.id === childId ? 'selected' : '';
                    const displayText = stage.index ? '[' + stage.index + '] ' + stage.title : stage.id + ' - ' + stage.title;
                    optionsHtml += '<option value="' + stage.id + '" ' + selected + '>' + displayText + '</option>';
                });

                const childHtml = 
                    '<div class="option-item mb-2 bg-light p-2 rounded">' +
                        '<div class="row g-2 align-items-center">' +
                            '<div class="col-md-5">' +
                                '<select class="form-select form-select-sm" name="optionKeys" required>' +
                                    '<option value="">Выберите дочерний этап...</option>' +
                                    optionsHtml +
                                '</select>' +
                            '</div>' +
                            '<div class="col-md-6">' +
                                '<input type="text" class="form-control form-control-sm"' +
                                       ' name="optionValues" placeholder="Текст кнопки выбора"' +
                                       ' value="' + buttonText + '" required>' +
                            '</div>' +
                            '<div class="col-md-1">' +
                                '<button type="button" class="btn btn-sm btn-outline-danger remove-child"' +
                                        ' title="Удалить вариант">' +
                                    '<i class="bi bi-trash"></i>' +
                                '</button>' +
                            '</div>' +
                        '</div>' +
                    '</div>';
                
                childrenContainer.insertAdjacentHTML('beforeend', childHtml);
                childrenContainer.lastElementChild.querySelector('.remove-child').addEventListener('click', function() {
                    this.closest('.option-item').remove();
                });
            }

            // Show loading state
            function setLoading(button, isLoading) {
                const originalHtml = button.innerHTML;
                if (isLoading) {
                    button.disabled = true;
                    button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Обработка...';
                } else {
                    button.disabled = false;
                    button.innerHTML = originalHtml;
                }
            }

            // Handle set start stage
            document.querySelectorAll('.set-start-stage').forEach(button => {
                button.addEventListener('click', function() {
                    const stageId = this.dataset.stageId;
                    const button = this;
                    setLoading(button, true);

                    fetch('${pageContext.request.contextPath}/editor', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                        },
                        body: 'action=setStart&stageId=' + encodeURIComponent(stageId)
                    })
                    .then(response => {
                        if (response.redirected) {
                            window.location.href = response.url;
                        } else if (!response.ok) {
                            throw new Error('Ошибка при установке стартового этапа');
                        }
                    })
                    .catch(error => {
                        showToast(error.message, 'danger');
                        setLoading(button, false);
                    });
                });
            });

            // Handle delete stage
            document.querySelectorAll('.delete-stage').forEach(button => {
                button.addEventListener('click', function() {
                    const stageId = this.dataset.stageId;
                    const stageTitle = this.closest('.stage-card').querySelector('.stage-title').textContent.trim();

                    if (confirm(`Вы уверены, что хотите удалить этап "${stageTitle}"? Это действие нельзя отменить.`)) {
                        const button = this;
                        setLoading(button, true);

                        fetch('${pageContext.request.contextPath}/editor', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded',
                            },
                            body: 'action=delete&stageId=' + encodeURIComponent(stageId)
                        })
                        .then(response => {
                            if (response.redirected) {
                                window.location.href = response.url;
                            } else if (!response.ok) {
                                throw new Error('Ошибка при удалении этапа');
                            }
                        })
                        .catch(error => {
                            showToast(error.message, 'danger');
                            setLoading(button, false);
                        });
                    }
                });
            });

            // Handle edit stage buttons
            document.querySelectorAll('.edit-stage').forEach(button => {
                button.addEventListener('click', function() {
                    const stageId = this.dataset.stageId;
                    loadStageForEdit(stageId);
                });
            });

            // Handle add parent button
            document.querySelector('.add-parent')?.addEventListener('click', function() {
                addParentToForm();
            });
            
            // Handle add child button
            document.querySelector('.add-child')?.addEventListener('click', function() {
                addChildToForm();
            });

            // Handle form submission
            const editForm = document.getElementById('editStageForm');
            if (editForm) {
                editForm.addEventListener('submit', function(e) {
                    e.preventDefault();
                    const form = this;
                    const submitButton = form.querySelector('button[type="submit"]');
                    setLoading(submitButton, true);

                    const formData = new FormData(form);
                    const stageId = formData.get('stageId');
                    // Collect parents
                    const parentIds = formData.getAll('parentIds');
                    const parentButtonTexts = formData.getAll('parentButtonTexts');
                    // Collect children (options)
                    const optionKeys = formData.getAll('optionKeys');
                    const optionValues = formData.getAll('optionValues');

                    // Create URL-encoded form data
                    const urlEncoded = new URLSearchParams();
                    urlEncoded.append('action', 'update');
                    urlEncoded.append('stageId', stageId);
                    urlEncoded.append('title', formData.get('stageTitle'));
                    urlEncoded.append('description', formData.get('stageDescription'));

                    // Add parent relationships
                    parentIds.forEach((parentId, index) => {
                        if (parentId && parentButtonTexts[index]) {
                            urlEncoded.append('parentIds', parentId);
                            urlEncoded.append('parentButtonTexts', parentButtonTexts[index]);
                        }
                    });

                    // Add child relationships (options)
                    optionKeys.forEach((key, index) => {
                        if (key && optionValues[index]) {
                            urlEncoded.append('optionKeys', key);
                            urlEncoded.append('optionValues', optionValues[index]);
                        }
                    });

                    fetch('${pageContext.request.contextPath}/editor', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                        },
                        body: urlEncoded
                    })
                    .then(response => {
                        if (response.redirected) {
                            window.location.href = response.url;
                        } else if (!response.ok) {
                            throw new Error('Ошибка при сохранении изменений');
                        }
                    })
                    .catch(error => {
                        showToast(error.message, 'danger');
                        setLoading(submitButton, false);
                    });
                });
            }

            // Handle cancel edit button
            document.getElementById('cancelEdit')?.addEventListener('click', function() {
                const noStageSelected = document.getElementById('no-stage-selected');
                const stageEditForm = document.getElementById('stage-edit-form');
                
                stageEditForm.style.display = 'none';
                noStageSelected.style.display = 'block';
                
                // Clear the form
                document.getElementById('editStageForm').reset();
                document.getElementById('parents-container').innerHTML = '';
                document.getElementById('children-container').innerHTML = '';
            });

            // Auto-focus on the first input in modals when shown
            const modals = document.querySelectorAll('.modal');
            modals.forEach(modal => {
                modal.addEventListener('shown.bs.modal', function () {
                    const input = this.querySelector('input[type="text"]');
                    if (input) input.focus();
                });
            });

            // Show edit form if stageId is in URL
            const urlParams = new URLSearchParams(window.location.search);
            const editStageId = urlParams.get('edit');
            if (editStageId) {
                const editButton = document.querySelector(`.edit-stage[data-stage-id="\${editStageId}"]`);
                if (editButton) {
                    editButton.click();
                    // Scroll to the form
                    setTimeout(() => {
                        document.getElementById('stage-edit-form').scrollIntoView({ behavior: 'smooth' });
                    }, 100);
                }
            }
        });
    </script>
</body>
</html>
