package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.Agendamento;
import io.github.scognamiglioo.entities.Localizacao;
import io.github.scognamiglioo.services.AgendamentoServiceLocal;
import io.github.scognamiglioo.websocket.PainelChamadaService;
import io.github.scognamiglioo.websocket.ChamadaWebSocketUtil;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller para gerenciar a fila de atendimentos
 */
@Named
@ViewScoped
public class AtendimentoController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(AtendimentoController.class.getName());

    @EJB
    private AgendamentoServiceLocal agendamentoService;

    @Inject
    private FacesContext facesContext;

    // Listas para a view
    private List<Agendamento> filaEspera;
    private List<Agendamento> emAtendimento;

    // Agendamento selecionado
    private Agendamento agendamentoSelecionado;

    @PostConstruct
    public void init() {
        carregarDados();
    }

    /**
     * Carrega todos os dados da fila
     */
    public void carregarDados() {
        carregarFilaEspera();
        carregarEmAtendimento();
    }

    /**
     * Carrega agendamentos na fila de espera (CONFIRMADO ou AGENDADO para hoje)
     */
    public void carregarFilaEspera() {
        try {
            filaEspera = agendamentoService.findAgendamentosFilaEspera();
            PainelChamadaService.getInstance()
                .atualizarQuantidadeNaFila(filaEspera.size());
            LOGGER.log(Level.INFO, "Carregados {0} agendamentos na fila de espera", filaEspera.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar fila de espera", e);
            addErrorMessage("Erro ao carregar fila de espera");
            filaEspera = new ArrayList<>();
        }
    }

    /**
     * Carrega agendamentos em atendimento
     */
    public void carregarEmAtendimento() {
        try {
            emAtendimento = agendamentoService.findAgendamentosEmAtendimento();
            LOGGER.log(Level.INFO, "Carregados {0} agendamentos em atendimento", emAtendimento.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar atendimentos em andamento", e);
            addErrorMessage("Erro ao carregar atendimentos em andamento");
            emAtendimento = new ArrayList<>();
        }
    }

    /**
     * Inicia o atendimento de um agendamento
     */
    public void iniciarAtendimento(Agendamento agendamento) {
        try {
            if (agendamento == null || agendamento.getId() == null) {
                addErrorMessage("Agendamento inválido");
                return;
            }

            agendamentoService.iniciarAtendimento(agendamento.getId());
            addSuccessMessage("Atendimento iniciado com sucesso!");

            // Notificar painel público via WebSocket
            notificarPainelPublico(agendamento);

            // Recarrega os dados
            carregarDados();
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Erro ao iniciar atendimento: {0}", e.getMessage());
            addErrorMessage(e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao iniciar atendimento", e);
            addErrorMessage("Erro ao iniciar atendimento: " + e.getMessage());
        }
    }

    /**
     * Finaliza o atendimento de um agendamento
     */
    public void finalizarAtendimento(Agendamento agendamento) {
        try {
            if (agendamento == null || agendamento.getId() == null) {
                addErrorMessage("Agendamento inválido");
                return;
            }

            agendamentoService.finalizarAtendimento(agendamento.getId());
            addSuccessMessage("Atendimento finalizado com sucesso!");

            // Recarrega os dados
            carregarDados();
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Erro ao finalizar atendimento: {0}", e.getMessage());
            addErrorMessage(e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao finalizar atendimento", e);
            addErrorMessage("Erro ao finalizar atendimento: " + e.getMessage());
        }
    }

    /**
     * Notifica o painel público sobre a chamada via WebSocket
     */
    private void notificarPainelPublico(Agendamento agendamento) {
        try {
            if (agendamento == null) {
                return;
            }

            boolean isWalkin = Boolean.TRUE.equals(agendamento.getIsWalkin());
            LOGGER.log(Level.INFO, "notificarPainelPublico: isWalkin={0}, agendamentoId={1}", 
                new Object[]{isWalkin, agendamento.getId()});
            
            String nomeUsuario;

            if (isWalkin) {
                nomeUsuario = agendamento.getWalkinNome() != null ? agendamento.getWalkinNome() : "Walk-in";
                LOGGER.log(Level.INFO, "Walk-in: nomeUsuario={0}", nomeUsuario);
            } else {
                if (agendamento.getUser() == null) {
                    return;
                }
                nomeUsuario = agendamento.getUser().getNome();
            }

            String localizacao = getLocalizacaoAtendimento(agendamento);
            LOGGER.log(Level.INFO, "notificarPainelPublico: localizacao={0}", localizacao);
            int quantidadeFila = getQuantidadeFilaEspera();

            if (isWalkin) {
                LOGGER.log(Level.INFO, "Enviando walk-in para painel: {0} -> {1}", 
                    new Object[]{nomeUsuario, localizacao});
                ChamadaWebSocketUtil.enviarChamadaWalkin(nomeUsuario, localizacao, quantidadeFila);
            } else {
                PainelChamadaService.getInstance()
                    .enviarChamada(nomeUsuario, localizacao, quantidadeFila);
            }

            LOGGER.log(Level.INFO, "Painel público notificado: " + nomeUsuario +
                " -> " + localizacao + " (fila: " + quantidadeFila + ")");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao notificar painel público", e);
            // Não bloqueia o fluxo se falhar a notificação
        }
    }

    /**
     * Busca a localização onde será realizado o atendimento
     */
    public String getLocalizacaoAtendimento(Agendamento agendamento) {
        try {
            if (agendamento == null || agendamento.getId() == null) {
                return "Não especificada";
            }

            Localizacao localizacao = agendamentoService.buscarLocalizacaoDoAgendamento(agendamento.getId());
            if (localizacao != null) {
                return localizacao.getNome();
            }
            return "Não especificada";
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar localização", e);
            return "Erro ao buscar";
        }
    }

    /**
     * Retorna a quantidade de pessoas na fila
     */
    public int getQuantidadeFilaEspera() {
        return filaEspera != null ? filaEspera.size() : 0;
    }

    /**
     * Retorna a quantidade de atendimentos em andamento
     */
    public int getQuantidadeEmAtendimento() {
        return emAtendimento != null ? emAtendimento.size() : 0;
    }

    // Métodos auxiliares para mensagens
    private void addSuccessMessage(String message) {
        if (facesContext != null) {
            facesContext.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", message));
        }
    }

    private void addErrorMessage(String message) {
        if (facesContext != null) {
            facesContext.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", message));
        }
    }

    // Getters e Setters
    public List<Agendamento> getFilaEspera() {
        return filaEspera;
    }

    public void setFilaEspera(List<Agendamento> filaEspera) {
        this.filaEspera = filaEspera;
    }

    public List<Agendamento> getEmAtendimento() {
        return emAtendimento;
    }

    public void setEmAtendimento(List<Agendamento> emAtendimento) {
        this.emAtendimento = emAtendimento;
    }

    public Agendamento getAgendamentoSelecionado() {
        return agendamentoSelecionado;
    }

    public void setAgendamentoSelecionado(Agendamento agendamentoSelecionado) {
        this.agendamentoSelecionado = agendamentoSelecionado;
    }
}

