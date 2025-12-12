/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.Agendamento;
import io.github.scognamiglioo.entities.StatusAgendamento;
import io.github.scognamiglioo.entities.StatusAtendenteAtual;
import io.github.scognamiglioo.services.AgendamentoServiceLocal;
import io.github.scognamiglioo.services.AtendenteServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PABLO DANIEL
 */
@Named
@ViewScoped
public class RecepcaoController implements Serializable {

    @EJB
    private AgendamentoServiceLocal agendamentoService;
    
    @EJB
    private AtendenteServiceLocal atendenteService;


    // === Check-in ===
    private String termoBusca; // CPF, nome ou protocolo
    private List<Agendamento> resultadosBusca;
    private Agendamento agendamentoSelecionado;

    // === Walk-in (apenas confirma presença) ===
    private String walkCpf;
    
    //=== Lista de Status dos atendentes===
    private List<StatusAtendenteAtual> statusAtendentes;

    @PostConstruct
    public void init() {
        resultadosBusca = new ArrayList<>();
        carregarStatusAtendentes();
    }

    // -----------------------------
    // LISTA DE STATUS DE ATENDENTES
    // -----------------------------    
    // === Método para atualizar lista no clique do botão ===
    public void carregarStatusAtendentes() {
        statusAtendentes = atendenteService.buscarTodosStatusAtendentes();
    }
    
    // -----------------------------
    // CHECK-IN: busca agendamento existente de hoje
    // -----------------------------
    public void buscarAgendamento() {
        if (termoBusca == null || termoBusca.isBlank()) {
            addWarnMessage("Informe CPF, protocolo ou nome.");
            resultadosBusca = new ArrayList<>();
            agendamentoSelecionado = null;
            return;
        }

        resultadosBusca = agendamentoService.searchByCpfOrProtocoloOrName(termoBusca.trim());

        if (resultadosBusca.isEmpty()) {
            addWarnMessage("Nenhum agendamento encontrado para hoje com esse termo.");
            agendamentoSelecionado = null;
            return;
        }

        agendamentoSelecionado = resultadosBusca.get(0);

        addSuccessMessage("Agendamento encontrado: selecione na lista ou confirme o primeiro resultado.");
    }


    // -----------------------------
    // CONFIRMAR CHECK-IN (somente para agendamentos de hoje)
    // -----------------------------
    public void confirmarCheckin() {
        if (agendamentoSelecionado == null) {
            addWarnMessage("Nenhum agendamento selecionado.");
            return;
        }

        // Apenas valida, sem mudar status
        addSuccessMessage("Agendamento válido para: " +
                agendamentoSelecionado.getUser().getNome());

        // Não altera status e não salva nada no banco
    }

    // -----------------------------
    // WALK-IN: confirma presença de agendamento existente de hoje
    // -----------------------------
    public void registrarWalkin() {
        if (walkCpf == null || walkCpf.isBlank()) {
            addWarnMessage("Informe o CPF do cliente para confirmar presença.");
            return;
        }

        List<Agendamento> agendamentos = agendamentoService.searchByCpfOrProtocoloOrName(walkCpf.trim());
        if (agendamentos.isEmpty()) {
            addWarnMessage("Nenhum agendamento encontrado para hoje com esse CPF.");
            return;
        }

        Agendamento agendamento = agendamentos.get(0);
        agendamentoService.alterarStatus(agendamento.getId(), StatusAgendamento.CONFIRMADO);
        addSuccessMessage("Presença confirmada para " + agendamento.getUser().getNome());

        walkCpf = null;
    }


    // -----------------------------
    // Mensagens utilitárias
    // -----------------------------
    private void addErrorMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    private void addSuccessMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    private void addWarnMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", msg));
    }

    // -----------------------------
    // Getters / Setters
    // -----------------------------
    public String getTermoBusca() { return termoBusca; }
    public void setTermoBusca(String termoBusca) { this.termoBusca = termoBusca; }

    public List<Agendamento> getResultadosBusca() { return resultadosBusca; }
    public Agendamento getAgendamentoSelecionado() { return agendamentoSelecionado; }
    public void setAgendamentoSelecionado(Agendamento agendamentoSelecionado) { this.agendamentoSelecionado = agendamentoSelecionado; }
    public List<StatusAtendenteAtual> getStatusAtendentes() {
        return statusAtendentes;
    }
    public String getWalkCpf() { return walkCpf; }
    public void setWalkCpf(String walkCpf) { this.walkCpf = walkCpf; }
}