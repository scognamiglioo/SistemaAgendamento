package io.github.scognamiglioo.websocket;

/**
 * Utilitário para facilitar envio de chamadas via WebSocket.
 */
public class ChamadaWebSocketUtil {

    private ChamadaWebSocketUtil() {}

    /**
     * Envia chamada para o painel público.
     *
     * @param nomeUsuario Nome do usuário chamado
     * @param localizacao Localização do atendimento
     * @param quantidadeFila Quantidade de pessoas na fila de espera
     */
    public static void enviarChamada(String nomeUsuario, String localizacao, int quantidadeFila) {
        PainelChamadaService.getInstance().enviarChamada(nomeUsuario, localizacao, quantidadeFila);
    }

    /**
     * Atualiza apenas a quantidade de pessoas na fila.
     */
    public static void atualizarQuantidadeFila(int quantidadeFila) {
        PainelChamadaService.getInstance().atualizarQuantidadeNaFila(quantidadeFila);
    }

    /**
     * Retorna quantidade de clientes conectados.
     */
    public static int getQuantidadeConectados() {
        return PainelChamadaService.getInstance().obterQuantidadeConectados();
    }
}
