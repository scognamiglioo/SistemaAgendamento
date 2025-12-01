/**
 * Módulo para monitoramento de mensagens de sucesso em formulários
 */
(function() {
    'use strict';

    /**
     * Busca campos hidden com mensagens do JSF
     */
    function findHiddenMessageFields() {
        const allHidden = document.querySelectorAll('input[type="hidden"]');
        let messageField = null;
        let typeField = null;
        
        allHidden.forEach(field => {
            if (field.name && field.name.includes('hiddenLastMessage') && field.value.trim()) {
                messageField = field;
            }
            if (field.name && field.name.includes('hiddenMessageType')) {
                typeField = field;
            }
        });
        
        return { messageField, typeField };
    }

    /**
     * Encontra o botão alvo para posicionar a mensagem
     */
    function findTargetButton() {
        return document.querySelector('input[value="Salvar e Continuar"], .btn-outline-primary, .btn-primary');
    }

    /**
     * Limpa os campos de mensagem após exibição
     */
    function clearMessageFields(messageField, typeField) {
        if (messageField) messageField.value = '';
        if (typeField) typeField.value = '';
    }

    /**
     * Monitora mensagens de sucesso após submissão de formulário
     */
    function monitorSuccessMessage() {
        let attempts = 0;
        const maxAttempts = 15;
        const checkInterval = 300;
        
        const interval = setInterval(() => {
            attempts++;
            
            const { messageField, typeField } = findHiddenMessageFields();
            
            if (messageField && messageField.value.trim() !== '') {
                const message = messageField.value;
                const type = (typeField && typeField.value) ? typeField.value : 'success';
                const targetButton = findTargetButton();
                
                if (targetButton && window.FloatingMessages) {
                    window.FloatingMessages.show(message, targetButton, type);
                }
                
                clearMessageFields(messageField, typeField);
                clearInterval(interval);
            } else if (attempts >= maxAttempts) {
                clearInterval(interval);
            }
        }, checkInterval);
    }

    // Exporta para o escopo global
    window.FormSuccessMonitor = {
        monitor: monitorSuccessMessage
    };

})();