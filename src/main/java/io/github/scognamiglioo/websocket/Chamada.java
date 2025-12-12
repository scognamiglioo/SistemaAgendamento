package io.github.scognamiglioo.websocket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo para representar uma chamada no painel público.
 */
public class Chamada {
    private String nomeUsuario;
    private String localizacao;
    private String dataHora;
    private boolean ativo; // true se é a chamada atual
    private int quantidadeFila;

    public Chamada() {}

    public Chamada(String nomeUsuario, String localizacao, boolean ativo) {
        this.nomeUsuario = nomeUsuario;
        this.localizacao = localizacao;
        this.ativo = ativo;
        this.dataHora = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getQuantidadeFila() {
        return quantidadeFila;
    }

    public void setQuantidadeFila(int quantidadeFila) {
        this.quantidadeFila = quantidadeFila;
    }

    @Override
    public String toString() {
        return "Chamada{" +
            "nomeUsuario='" + nomeUsuario + '\'' +
            ", localizacao='" + localizacao + '\'' +
            ", dataHora='" + dataHora + '\'' +
            ", ativo=" + ativo +
            ", quantidadeFila=" + quantidadeFila +
            '}';
    }
}
