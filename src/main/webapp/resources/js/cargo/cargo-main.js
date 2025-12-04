/**
 * Sistema de mensagens para cargo - Arquivo principal
 * Carrega e inicializa todos os módulos necessários
 */
(function() {
    'use strict';

    // Função principal para monitorar mensagens de sucesso
    function monitorarMensagemSucesso() {
        if (window.FormSuccessMonitor) {
            window.FormSuccessMonitor.monitor();
        }
    }

    // Função para exibir mensagem flutuante
    function showFloatingMessage(message, targetElement, type = 'info') {
        if (window.FloatingMessages) {
            window.FloatingMessages.show(message, targetElement, type);
        }
    }

    // Funções de validação (compatibilidade) 
    function verificarCampos() {
        return window.CargoValidation ? window.CargoValidation.validateFields() : true;
    }

    function verificarExclusaoAntes(cargoId, cargoNome) {
        // Se o primeiro parâmetro é um elemento HTML (caso da lista)
        if (cargoId && typeof cargoId === 'object' && cargoId.nodeType === 1) {
            return window.CargoValidation ? window.CargoValidation.confirmDeleteFromList(cargoId) : true;
        }
        
        // Caso contrário, usar função genérica
        return window.CargoValidation ? window.CargoValidation.confirmDelete(cargoId, cargoNome) : true;
    }

    function toggleFuncionariosCargo(cargoId) {
        if (window.CargoValidation) {
            window.CargoValidation.toggleEmployees(cargoId);
        }
    }

    // Exporta funções para o escopo global
    window.monitorarMensagemSucesso = monitorarMensagemSucesso;
    window.showFloatingMessage = showFloatingMessage;
    window.verificarCampos = verificarCampos;
    window.verificarExclusaoAntes = verificarExclusaoAntes;
    window.toggleFuncionariosCargo = toggleFuncionariosCargo;

})();