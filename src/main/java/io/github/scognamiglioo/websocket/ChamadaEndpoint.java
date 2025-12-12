package io.github.scognamiglioo.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Endpoint WebSocket para o painel público.
 */
@ServerEndpoint(
    value = "/painel-chamadas",
    encoders = {ChamadaEncoder.class},
    decoders = {ChamadaDecoder.class}
)
public class ChamadaEndpoint {

    private final PainelChamadaService service = PainelChamadaService.getInstance();

    @OnOpen
    public void onOpen(Session session) {
        service.registrarSessao(session);
    }

    @OnMessage
    public void onMessage(Chamada chamada, Session session) {
        // Atualmente servidor não processa mensagens dos clientes
    }

    @OnClose
    public void onClose(Session session) {
        service.removerSessao(session);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        service.removerSessao(session);
    }
}
