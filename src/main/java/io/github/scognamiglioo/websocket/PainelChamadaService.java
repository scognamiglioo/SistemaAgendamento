package io.github.scognamiglioo.websocket;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;

/**
 * Serviço singleton para gerenciar o broadcast de chamadas
 * para o painel público.
 */
public class PainelChamadaService {

    private static PainelChamadaService instancia;
    private final Set<Session> sessoes = new CopyOnWriteArraySet<>();
    private final Deque<Chamada> historico = new ArrayDeque<>(5);
    private Chamada chamadaAtual = null;
    private int quantidadeFilaEspera = 0;

    public static synchronized PainelChamadaService getInstance() {
        if (instancia == null) {
            instancia = new PainelChamadaService();
        }
        return instancia;
    }

    public void registrarSessao(Session sessao) {
        sessoes.add(sessao);
        enviarEstadoAtual(sessao);
    }

    public void removerSessao(Session sessao) {
        sessoes.remove(sessao);
    }

    public void enviarChamada(String nomeUsuario, String localizacao, int quantidadeFila) {
        try {
            if (quantidadeFila >= 0) {
                this.quantidadeFilaEspera = quantidadeFila;
            }

            Chamada chamada = new Chamada(nomeUsuario, localizacao, true);
            chamada.setQuantidadeFila(this.quantidadeFilaEspera);

            if (chamadaAtual != null) {
                chamadaAtual.setAtivo(false);
                adicionarAoHistorico(chamadaAtual);
            }

            chamadaAtual = chamada;
            broadcast(chamada);
        } catch (Exception e) {
            System.err.println("[PAINEL-SERVICE] Erro ao enviar chamada: " + e.getMessage());
        }
    }

    public void atualizarQuantidadeNaFila(int quantidade) {
        this.quantidadeFilaEspera = quantidade;
        broadcastQuantidadeFila();
    }

    public int obterQuantidadeNaFila() {
        return quantidadeFilaEspera;
    }

    public int obterQuantidadeConectados() {
        return sessoes.size();
    }

    public List<Chamada> obterHistorico() {
        return new ArrayList<>(historico);
    }

    public Chamada obterChamadaAtual() {
        return chamadaAtual;
    }

    private void enviarEstadoAtual(Session sessao) {
        try {
            if (!sessao.isOpen()) {
                return;
            }

            if (chamadaAtual != null) {
                sessao.getBasicRemote().sendObject(chamadaAtual);
            }
            // Sempre enviar a quantidade de fila ao conectar
            Chamada fila = new Chamada("", "", false);
            fila.setQuantidadeFila(quantidadeFilaEspera);
            sessao.getBasicRemote().sendObject(fila);
        } catch (EncodeException | IOException e) {
            System.err.println("[PAINEL-SERVICE] Erro ao enviar estado: " + e.getMessage());
        }
    }

    private void broadcast(Chamada chamada) {
        for (Session sessao : sessoes) {
            if (sessao.isOpen()) {
                try {
                    sessao.getBasicRemote().sendObject(chamada);
                } catch (EncodeException | IOException e) {
                    removerSessao(sessao);
                }
            }
        }
    }

    private void broadcastQuantidadeFila() {
        Chamada infoFila = new Chamada("", "", false);
        infoFila.setQuantidadeFila(quantidadeFilaEspera);
        broadcast(infoFila);
    }

    private void adicionarAoHistorico(Chamada chamada) {
        if (historico.size() >= 5) {
            historico.removeFirst();
        }
        historico.addLast(chamada);
    }
}
