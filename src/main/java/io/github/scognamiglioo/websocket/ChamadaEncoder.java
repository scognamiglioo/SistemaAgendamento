package io.github.scognamiglioo.websocket;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

/**
 * Codificador para converter Chamada em JSON.
 */
public class ChamadaEncoder implements Encoder.Text<Chamada> {

    @Override
    public String encode(Chamada chamada) throws EncodeException {
        JsonObject json = Json.createObjectBuilder()
            .add("nomeUsuario", chamada.getNomeUsuario() == null ? "" : chamada.getNomeUsuario())
            .add("localizacao", chamada.getLocalizacao() == null ? "" : chamada.getLocalizacao())
            .add("dataHora", chamada.getDataHora() == null ? "" : chamada.getDataHora())
            .add("ativo", chamada.isAtivo())
            .add("quantidadeFila", chamada.getQuantidadeFila())
            .build();
        return json.toString();
    }

    @Override
    public void init(EndpointConfig config) {}

    @Override
    public void destroy() {}
}
