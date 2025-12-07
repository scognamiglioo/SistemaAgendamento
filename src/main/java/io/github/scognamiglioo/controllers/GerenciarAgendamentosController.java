package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.*;
import io.github.scognamiglioo.services.AgendamentoServiceLocal;
import io.github.scognamiglioo.services.DataServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller para gerenciar todos os agendamentos (área administrativa)
 */
@Named
@ViewScoped
public class GerenciarAgendamentosController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(GerenciarAgendamentosController.class.getName());

    @EJB
    private AgendamentoServiceLocal agendamentoService;

    @EJB
    private DataServiceLocal dataService;

    // Listas
    private List<Agendamento> agendamentos;
    private List<Funcionario> funcionariosDisponiveis;
    private List<Guiche> guichesDisponiveis;

    // Filtros
    private Date filtroDataInicio;
    private Date filtroDataFim;
    private String filtroStatus;

    // Seleção/Edição
    private Agendamento agendamentoSelecionado;
    private Long funcionarioSelecionadoId;
    private Long guicheSelecionadoId;
    private String statusSelecionado;

    @PostConstruct
    public void init() {
        carregarAgendamentos();
        carregarFuncionarios();
        carregarGuiches();
    }

    /**
     * Carrega todos os agendamentos
     */
    public void carregarAgendamentos() {
        try {
            agendamentos = agendamentoService.getAllAgendamentos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar agendamentos", e);
            addErrorMessage("Erro ao carregar agendamentos");
            agendamentos = new ArrayList<>();
        }
    }

    /**
     * Carrega todos os funcionários ativos
     */
    private void carregarFuncionarios() {
        try {
            funcionariosDisponiveis = dataService.getAllFuncionarios();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar funcionários", e);
            funcionariosDisponiveis = new ArrayList<>();
        }
    }

    /**
     * Carrega todos os guichês
     */
    private void carregarGuiches() {
        try {
            guichesDisponiveis = dataService.listGuiches();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar guichês", e);
            guichesDisponiveis = new ArrayList<>();
        }
    }

    /**
     * Aplica filtros na listagem
     */
    public void aplicarFiltros() {
        try {
            if (filtroDataInicio != null && filtroDataFim != null) {
                LocalDate inicio = new java.sql.Date(filtroDataInicio.getTime()).toLocalDate();
                LocalDate fim = new java.sql.Date(filtroDataFim.getTime()).toLocalDate();
                agendamentos = agendamentoService.findAgendamentosByDataBetween(inicio, fim);
            } else {
                agendamentos = agendamentoService.getAllAgendamentos();
            }

            // Filtra por status se selecionado
            if (filtroStatus != null && !filtroStatus.isEmpty()) {
                StatusAgendamento status = StatusAgendamento.valueOf(filtroStatus);
                agendamentos = agendamentoService.findAgendamentosByStatus(status);
            }

            addInfoMessage("Filtros aplicados com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao aplicar filtros", e);
            addErrorMessage("Erro ao aplicar filtros");
            carregarAgendamentos();
        }
    }

    /**
     * Prepara o agendamento para edição
     */
    public void editarAgendamento(Agendamento agendamento) {
        this.agendamentoSelecionado = agendamento;
        this.funcionarioSelecionadoId = agendamento.getFuncionario() != null ?
                agendamento.getFuncionario().getId() : null;
        this.guicheSelecionadoId = agendamento.getGuiche() != null ?
                agendamento.getGuiche().getId() : null;
        this.statusSelecionado = agendamento.getStatus().name();
    }

    /**
     * Salva as alterações do agendamento
     */
    public void salvarEdicao() {
        try {
            if (agendamentoSelecionado == null) {
                addErrorMessage("Nenhum agendamento selecionado");
                return;
            }

            // Atribui funcionário se selecionado
            if (funcionarioSelecionadoId != null) {
                agendamentoService.atribuirFuncionario(
                        agendamentoSelecionado.getId(),
                        funcionarioSelecionadoId
                );
            }

            // Atribui guichê se selecionado
            if (guicheSelecionadoId != null) {
                agendamentoService.atribuirGuiche(
                        agendamentoSelecionado.getId(),
                        guicheSelecionadoId
                );
            }

            // Altera status
            if (statusSelecionado != null) {
                StatusAgendamento novoStatus = StatusAgendamento.valueOf(statusSelecionado);
                agendamentoService.alterarStatus(agendamentoSelecionado.getId(), novoStatus);
            }

            // Atualiza observações
            agendamentoService.updateAgendamento(agendamentoSelecionado);

            addSuccessMessage("Agendamento atualizado com sucesso!");
            carregarAgendamentos();

            FacesContext.getCurrentInstance().getAttributes().put("success", true);

        } catch (IllegalArgumentException ex) {
            addErrorMessage(ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar agendamento", ex);
            addErrorMessage("Erro ao salvar agendamento: " + ex.getMessage());
        }
    }

    /**
     * Cancela um agendamento
     */
    public void cancelarAgendamento(Long agendamentoId) {
        try {
            agendamentoService.cancelarAgendamento(agendamentoId);
            addSuccessMessage("Agendamento cancelado com sucesso!");
            carregarAgendamentos();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao cancelar agendamento", ex);
            addErrorMessage("Erro ao cancelar agendamento: " + ex.getMessage());
        }
    }

    // Métodos auxiliares de mensagens

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", message));
    }

    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", message));
    }

    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }

    // Getters e Setters

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public List<Funcionario> getFuncionariosDisponiveis() {
        return funcionariosDisponiveis;
    }

    public void setFuncionariosDisponiveis(List<Funcionario> funcionariosDisponiveis) {
        this.funcionariosDisponiveis = funcionariosDisponiveis;
    }

    public List<Guiche> getGuichesDisponiveis() {
        return guichesDisponiveis;
    }

    public void setGuichesDisponiveis(List<Guiche> guichesDisponiveis) {
        this.guichesDisponiveis = guichesDisponiveis;
    }

    public Date getFiltroDataInicio() {
        return filtroDataInicio;
    }

    public void setFiltroDataInicio(Date filtroDataInicio) {
        this.filtroDataInicio = filtroDataInicio;
    }

    public Date getFiltroDataFim() {
        return filtroDataFim;
    }

    public void setFiltroDataFim(Date filtroDataFim) {
        this.filtroDataFim = filtroDataFim;
    }

    public String getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus;
    }

    public Agendamento getAgendamentoSelecionado() {
        return agendamentoSelecionado;
    }

    public void setAgendamentoSelecionado(Agendamento agendamentoSelecionado) {
        this.agendamentoSelecionado = agendamentoSelecionado;
    }

    public Long getFuncionarioSelecionadoId() {
        return funcionarioSelecionadoId;
    }

    public void setFuncionarioSelecionadoId(Long funcionarioSelecionadoId) {
        this.funcionarioSelecionadoId = funcionarioSelecionadoId;
    }

    public Long getGuicheSelecionadoId() {
        return guicheSelecionadoId;
    }

    public void setGuicheSelecionadoId(Long guicheSelecionadoId) {
        this.guicheSelecionadoId = guicheSelecionadoId;
    }

    public String getStatusSelecionado() {
        return statusSelecionado;
    }

    public void setStatusSelecionado(String statusSelecionado) {
        this.statusSelecionado = statusSelecionado;
    }
}

