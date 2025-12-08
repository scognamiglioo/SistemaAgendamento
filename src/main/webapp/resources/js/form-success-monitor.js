/**
 * Módulo para monitoramento de mensagens de sucesso em formulários
 */
(function() {
    'use strict';

    // Rastreia a última mensagem exibida para evitar duplicação
    let lastDisplayedMessage = null;
    let lastDisplayedTime = 0;

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
        return document.querySelector('input[value="Salvar e Continuar"], .btn-outline-primary, .btn-primary, .btn-danger');
    }

    /**
     * Verifica se a mensagem já foi exibida recentemente
     */
    function shouldShowMessage(message) {
        const now = Date.now();
        const timeSinceLastDisplay = now - lastDisplayedTime;
        
        // Se é uma mensagem diferente OU passaram mais de 500ms desde a última exibição
        if (message !== lastDisplayedMessage || timeSinceLastDisplay > 500) {
            lastDisplayedMessage = message;
            lastDisplayedTime = now;
            return true;
        }
        
        return false;
    }

    /**
     * Monitora mensagens de sucesso após submissão de formulário
     */
    function monitorSuccessMessage() {
        let attempts = 0;
        const maxAttempts = 15;
        const checkInterval = 200;
        
        const interval = setInterval(() => {
            attempts++;
            
            const { messageField, typeField } = findHiddenMessageFields();
            
            if (messageField && messageField.value.trim() !== '') {
                const message = messageField.value;
                const type = (typeField && typeField.value) ? typeField.value : 'success';
                
                // Só exibe se não foi mostrada recentemente
                if (shouldShowMessage(message)) {
                    const targetButton = findTargetButton();
                    
                    if (targetButton && window.FloatingMessages) {
                        window.FloatingMessages.show(message, targetButton, type);
                    }
                }
                
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