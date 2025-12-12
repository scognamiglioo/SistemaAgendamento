package io.github.scognamiglioo.websocket;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;
import java.io.StringReader;

/**
 * Decodificador para converter JSON em Chamada (caso precise receber mensagens do cliente).
 */
public class ChamadaDecoder implements Decoder.Text<Chamada> {

    @Override
    public Chamada decode(String json) throws DecodeException {
        try {
            JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
            Chamada chamada = new Chamada();
            chamada.setNomeUsuario(jsonObject.getString("nomeUsuario", ""));
            chamada.setLocalizacao(jsonObject.getString("localizacao", ""));
            chamada.setDataHora(jsonObject.getString("dataHora", ""));
            chamada.setAtivo(jsonObject.getBoolean("ativo", false));
            chamada.setQuantidadeFila(jsonObject.getInt("quantidadeFila", 0));
            return chamada;
        } catch (Exception e) {
            throw new DecodeException(json, "Erro ao decodificar Chamada", e);
        }
    }

    @Override
    public boolean willDecode(String json) {
        return json != null && json.contains("nomeUsuario");
    }

    @Override
    public void init(EndpointConfig config) {}

    @Override
    public void destroy() {}
}
