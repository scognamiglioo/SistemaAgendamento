/**
 * Módulo para validação e interações específicas de cargo
 */
(function() {
    'use strict';

    /**
     * Valida se os campos obrigatórios estão preenchidos
     */
    function validateRequiredFields() {
        const nome = document.querySelector('input[id$="nome"]');
        const descricao = document.querySelector('textarea[id$="descricao"]');
        
        if (!nome || nome.value.trim() === '') {
            const targetButton = document.querySelector('.btn-danger');
            if (window.FloatingMessages && targetButton) {
                window.FloatingMessages.show('Nome do cargo é obrigatório!', targetButton, 'error');
            }
            return false;
        }
        
        if (!descricao || descricao.value.trim() === '') {
            const targetButton = document.querySelector('.btn-danger');
            if (window.FloatingMessages && targetButton) {
                window.FloatingMessages.show('Descrição do cargo é obrigatória!', targetButton, 'error');
            }
            return false;
        }
        
        return true;
    }

    /**
     * Confirma exclusão de cargo - versão para página de lista
     */
    function confirmDeleteFromList(buttonElement) {
        if (!buttonElement) {
            return confirm('Tem certeza que deseja excluir este cargo?');
        }
        
        // Extrair dados dos atributos data-*
        const funcionariosCount = parseInt(buttonElement.getAttribute('data-funcionarios') || '0');
        const cargoNome = buttonElement.getAttribute('data-nome') || 'Cargo';
        const cargoId = buttonElement.getAttribute('data-id');
        
        // Verificar se há funcionários associados
        if (funcionariosCount > 0) {
            const message = `Não é possível excluir "${cargoNome}" pois há ${funcionariosCount} funcionário${funcionariosCount > 1 ? 's' : ''} associado${funcionariosCount > 1 ? 's' : ''} a este cargo!`;
            
            // Mostrar mensagem flutuante de erro
            if (window.FloatingMessages) {
                window.FloatingMessages.show(message, buttonElement, 'error');
            } else {
                alert(message);
            }
            
            return false; // Impede a exclusão
        }
        
        // Para cargos sem funcionários, usar confirmação tradicional
        return confirm(`Tem certeza que deseja excluir o cargo "${cargoNome}"?\n\nEsta ação não poderá ser desfeita.`);
    }

    /**
     * Confirma exclusão de cargo - versão genérica
     */
    function confirmDelete(cargoId, cargoNome) {
        const targetButton = document.querySelector(`input[onclick*="confirmarExclusao(${cargoId})"]`);
        
        if (window.FloatingMessages && targetButton) {
            window.FloatingMessages.show(
                `Tem certeza que deseja excluir o cargo "${cargoNome}"?`,
                targetButton,
                'warning'
            );
        }
        
        return confirm(`Tem certeza que deseja excluir o cargo "${cargoNome}"?\n\nEsta ação não poderá ser desfeita.`);
    }

    /**
     * Alterna exibição de funcionários de um cargo
     */
    function toggleEmployeesList(cargoId) {
        const contentDiv = document.getElementById('funcionarios-content-cargo-' + cargoId);
        const button = document.getElementById('btn-funcionarios-cargo-' + cargoId);
        
        if (!contentDiv || !button) {
            return;
        }
        
        const isVisible = contentDiv.style.display !== 'none';
        
        if (isVisible) {
            contentDiv.style.display = 'none';
            button.textContent = '👥 Ver Funcionários';
            button.classList.remove('btn-warning');
            button.classList.add('btn-outline-secondary');
        } else {
            contentDiv.style.display = 'block';
            button.textContent = '🔼 Ocultar';
            button.classList.remove('btn-outline-secondary');
            button.classList.add('btn-warning');
        }
    }

    /**
     * Manipula o evento de conclusão da exclusão
     */
    function handleDeleteComplete(event) {
        // Verificar se houve sucesso na exclusão via JSF status
        if (event && event.source && event.status === 'success') {
            // Buscar se há mensagem de sucesso para exibir
            if (window.FormSuccessMonitor) {
                window.FormSuccessMonitor.monitor();
            }
        }
    }

    // Exporta para o escopo global
    window.CargoValidation = {
        validateFields: validateRequiredFields,
        confirmDelete: confirmDelete,
        confirmDeleteFromList: confirmDeleteFromList,
        toggleEmployees: toggleEmployeesList,
        handleDeleteComplete: handleDeleteComplete
    };

    // Mantém compatibilidade com funções antigas
    window.verificarCampos = validateRequiredFields;
    window.verificarExclusaoAntes = confirmDeleteFromList; // Usa a versão da lista por padrão
    window.toggleFuncionariosCargo = toggleEmployeesList;
    window.handleDeleteComplete = handleDeleteComplete;

})();