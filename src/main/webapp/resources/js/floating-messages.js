/**
 * Módulo para mensagens flutuantes - tooltip style
 */
(function() {
    'use strict';

    // Estilos CSS para o tooltip flutuante
    const TOOLTIP_STYLES = `
        position: fixed;
        z-index: 9999;
        padding: 12px 16px;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 500;
        color: white;
        max-width: 300px;
        word-wrap: break-word;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        pointer-events: none;
        opacity: 0;
        transform: translateY(-10px) scale(0.95);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        border: 2px solid;
        text-align: center;
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    `;

    /**
     * Aplica estilos baseados no tipo de mensagem
     */
    function getMessageStyles(type) {
        switch(type) {
            case 'error':
                return 'background: #dc3545; border-color: #b02a37;';
            case 'success':
                return 'background: #28a745; border-color: #1e7e34;';
            case 'warning':
                return 'background: #ffc107; color: #212529; border-color: #d39e00;';
            default:
                return 'background: #17a2b8; border-color: #138496;';
        }
    }

    /**
     * Calcula posição do tooltip em relação ao elemento
     */
    function calculatePosition(element) {
        const rect = element.getBoundingClientRect();
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;

        return {
            top: rect.top + scrollTop - 60,
            left: rect.left + scrollLeft + (rect.width / 2)
        };
    }

    /**
     * Cria e exibe mensagem flutuante
     */
    function showFloatingMessage(message, targetElement, type = 'info') {
        // Remove qualquer tooltip existente
        const existingTooltip = document.querySelector('.floating-message-tooltip');
        if (existingTooltip) {
            existingTooltip.remove();
        }

        // Cria novo tooltip
        const tooltip = document.createElement('div');
        tooltip.className = 'floating-message-tooltip';
        tooltip.textContent = message;
        
        // Aplica estilos
        tooltip.style.cssText = TOOLTIP_STYLES + getMessageStyles(type);
        
        // Adiciona ao DOM
        document.body.appendChild(tooltip);
        
        // Calcula posição
        const position = calculatePosition(targetElement);
        tooltip.style.top = position.top + 'px';
        tooltip.style.left = (position.left - tooltip.offsetWidth / 2) + 'px';
        
        // Animação de entrada
        requestAnimationFrame(() => {
            tooltip.style.opacity = '1';
            tooltip.style.transform = 'translateY(0) scale(1)';
        });
        
        // Remove automaticamente após 4 segundos
        setTimeout(() => {
            if (tooltip.parentNode) {
                tooltip.style.opacity = '0';
                tooltip.style.transform = 'translateY(-10px) scale(0.95)';
                setTimeout(() => tooltip.remove(), 300);
            }
        }, 4000);
    }

    // Exporta para o escopo global
    window.FloatingMessages = {
        show: showFloatingMessage
    };

})();