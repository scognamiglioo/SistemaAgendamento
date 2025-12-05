/**
 * Sistema de mensagens para serviço - Arquivo principal
 * Carrega e inicializa todos os módulos necessários
 */
(function() {
    'use strict';

    // Função principal para monitorar mensagens de sucesso
    function monitorarMensagemSucessoServico() {
        if (window.FormSuccessMonitor) {
            window.FormSuccessMonitor.monitor();
        }
    }

    // Função para exibir mensagem flutuante
    function showFloatingMessageServico(message, targetElement, type = 'info') {
        if (window.FloatingMessages) {
            window.FloatingMessages.show(message, targetElement, type);
        }
    }

    // Funções de validação (compatibilidade) 
    function verificarCamposServico() {
        return window.ServicoValidation ? window.ServicoValidation.validateFields() : true;
    }

    function verificarExclusaoAntesServico(servicoId, servicoNome) {
        // Se o primeiro parâmetro é um elemento HTML (caso da lista)
        if (servicoId && typeof servicoId === 'object' && servicoId.nodeType === 1) {
            return window.ServicoValidation ? window.ServicoValidation.confirmDeleteFromList(servicoId) : true;
        }
        
        // Caso contrário, usar função genérica
        return window.ServicoValidation ? window.ServicoValidation.confirmDelete(servicoId, servicoNome) : true;
    }

    function toggleFuncionariosServico(servicoId) {
        if (window.ServicoValidation) {
            window.ServicoValidation.toggleEmployees(servicoId);
        }
    }

    function handleDeleteCompleteServico(event) {
        if (window.ServicoValidation) {
            window.ServicoValidation.handleDeleteComplete(event);
        }
    }

    // Exporta funções para o escopo global
    window.monitorarMensagemSucessoServico = monitorarMensagemSucessoServico;
    window.showFloatingMessageServico = showFloatingMessageServico;
    window.verificarCamposServico = verificarCamposServico;
    window.verificarExclusaoAntesServico = verificarExclusaoAntesServico;
    window.toggleFuncionariosServico = toggleFuncionariosServico;
    window.handleDeleteCompleteServico = handleDeleteCompleteServico;

})();