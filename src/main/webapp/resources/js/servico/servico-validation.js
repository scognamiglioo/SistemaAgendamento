/**
 * Módulo para validação e interações específicas de serviço
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
                window.FloatingMessages.show('Nome do serviço é obrigatório!', targetButton, 'error');
            }
            return false;
        }
        
        if (!descricao || descricao.value.trim() === '') {
            const targetButton = document.querySelector('.btn-danger');
            if (window.FloatingMessages && targetButton) {
                window.FloatingMessages.show('Descrição do serviço é obrigatória!', targetButton, 'error');
            }
            return false;
        }
        
        return true;
    }

    /**
     * Confirma exclusão de serviço - versão para página de lista
     */
    function confirmDeleteFromList(buttonElement) {
        if (!buttonElement) {
            return confirm('Tem certeza que deseja excluir este serviço?');
        }
        
        // Extrair dados dos atributos data-*
        const funcionariosCount = parseInt(buttonElement.getAttribute('data-funcionarios') || '0');
        const servicoNome = buttonElement.getAttribute('data-nome') || 'Serviço';
        
        // Verificar se há funcionários associados
        if (funcionariosCount > 0) {
            const message = `Não é possível excluir "${servicoNome}" pois há ${funcionariosCount} funcionário${funcionariosCount > 1 ? 's' : ''} associado${funcionariosCount > 1 ? 's' : ''} a este serviço!`;
            
            // Mostrar mensagem flutuante de erro
            if (window.FloatingMessages) {
                window.FloatingMessages.show(message, buttonElement, 'error');
            } else {
                alert(message);
            }
            
            return false; // Impede a exclusão
        }
        
        // Para serviços sem funcionários, usar confirmação tradicional
        return confirm(`Tem certeza que deseja excluir o serviço "${servicoNome}"?\n\nEsta ação não poderá ser desfeita.`);
    }

    /**
     * Confirma exclusão de serviço - versão genérica
     */
    function confirmDelete(servicoId, servicoNome) {
        const targetButton = document.querySelector(`input[onclick*="confirmarExclusao(${servicoId})"]`);
        
        if (window.FloatingMessages && targetButton) {
            window.FloatingMessages.show(
                `Tem certeza que deseja excluir o serviço "${servicoNome}"?`,
                targetButton,
                'warning'
            );
        }
        
        return confirm(`Tem certeza que deseja excluir o serviço "${servicoNome}"?\n\nEsta ação não poderá ser desfeita.`);
    }

    /**
     * Alterna exibição de funcionários de um serviço
     */
    function toggleEmployeesList(servicoId) {
        const contentDiv = document.getElementById('funcionarios-content-' + servicoId);
        const button = document.getElementById('btn-funcionarios-' + servicoId);
        
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
            button.textContent = '👥 Ocultar';
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
    window.ServicoValidation = {
        validateFields: validateRequiredFields,
        confirmDelete: confirmDelete,
        confirmDeleteFromList: confirmDeleteFromList,
        toggleEmployees: toggleEmployeesList,
        handleDeleteComplete: handleDeleteComplete
    };

    // Mantém compatibilidade com funções antigas
    window.verificarCamposServico = validateRequiredFields;
    window.verificarExclusaoAntesServico = confirmDeleteFromList;
    window.toggleFuncionarios = toggleEmployeesList;
    window.handleDeleteCompleteServico = handleDeleteComplete;

})();