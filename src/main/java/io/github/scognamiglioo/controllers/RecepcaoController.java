/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.Agendamento;
import io.github.scognamiglioo.entities.Atendimento;
import io.github.scognamiglioo.entities.AtendimentoFila;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Servico;
import io.github.scognamiglioo.entities.StatusAgendamento;
import io.github.scognamiglioo.entities.User;
import io.github.scognamiglioo.services.AgendamentoService;
import io.github.scognamiglioo.services.AgendamentoServiceLocal;
import io.github.scognamiglioo.services.FilaServiceLocal;
import io.github.scognamiglioo.services.RecepcaoServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author PABLO DANIEL
 */
@Named
@ViewScoped
public class RecepcaoController implements Serializable {

    @EJB
    private AgendamentoServiceLocal agendamentoService;

    // === Campos de check-in ===
    private String termoBusca; // CPF, nome ou protocolo
    private List<Agendamento> resultadosBusca;
    private Agendamento agendamentoSelecionado;

    // === Campos de walk-in ===
    private String walkNome;
    private String walkCpf;
    private Servico walkServico;
    private Funcionario walkFuncionario;
    private LocalDate walkData;
    private LocalTime walkHora;

    @PostConstruct
    public void init() {
        resultadosBusca = new ArrayList<>();
        walkData = LocalDate.now();
    }

    // ---------------------------------------------------------------------
    // CHECK-IN: busca agendamento existente e confirma
    // ---------------------------------------------------------------------
    public void buscarAgendamento() {
        if (termoBusca == null || termoBusca.isBlank()) {
            addWarnMessage("Informe CPF, protocolo ou nome.");
            resultadosBusca = new ArrayList<>();
            agendamentoSelecionado = null;
            return;
        }

        resultadosBusca = agendamentoService.searchByCpfOrProtocoloOrName(termoBusca.trim());
        if (resultadosBusca == null) resultadosBusca = new ArrayList<>();

        // Filtra apenas agendamentos de hoje
        resultadosBusca.removeIf(a -> a.getData() == null || !a.getData().isEqual(LocalDate.now()));

        if (resultadosBusca.isEmpty()) {
            addWarnMessage("Nenhum agendamento encontrado para hoje com esse termo.");
            agendamentoSelecionado = null;
            return;
        }

        // Seleciona o primeiro por padrão
        agendamentoSelecionado = resultadosBusca.get(0);
        addSuccessMessage("Agendamento encontrado: selecione na lista ou confirme o primeiro resultado.");
    }

    public void confirmarCheckin() {
        if (agendamentoSelecionado == null) {
            addWarnMessage("Nenhum agendamento selecionado para check-in.");
            return;
        }

        agendamentoService.alterarStatus(agendamentoSelecionado.getId(), StatusAgendamento.CONFIRMADO);
        addSuccessMessage("Check-in confirmado.");
        
        // Limpa seleção
        agendamentoSelecionado = null;
        resultadosBusca = new ArrayList<>();
        termoBusca = null;
    }

    // ---------------------------------------------------------------------
    // WALK-IN: cria agendamento para cliente sem reserva
    // ---------------------------------------------------------------------
    public void registrarWalkin() {
        if (walkNome == null || walkNome.isBlank() || walkCpf == null || walkCpf.isBlank() || walkServico == null) {
            addWarnMessage("Preencha todos os dados do walk-in: nome, CPF e serviço.");
            return;
        }

        try {
            // Cria User fictício para walk-in
            User user = new User();
            user.setNome(walkNome);
            user.setCpf(walkCpf);

            // Se não escolher funcionário, será null (qualquer disponível)
            Agendamento novo = agendamentoService.createAgendamento(user, walkServico, walkFuncionario, walkData, walkHora);

            // Após criar, já confirma check-in automaticamente
            agendamentoService.alterarStatus(novo.getId(), StatusAgendamento.CONFIRMADO);
            addSuccessMessage("Walk-in registrado e check-in confirmado para " + walkNome);

            // Limpa campos do walk-in
            walkNome = null;
            walkCpf = null;
            walkServico = null;
            walkFuncionario = null;
            walkData = LocalDate.now();
            walkHora = LocalTime.now();

        } catch (Exception ex) {
            addErrorMessage("Erro ao registrar walk-in: " + ex.getMessage());
        }
    }

    // ---------------------------
    // Mensagens utilitárias
    // ---------------------------
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

    // ---------------------------
    // Getters / Setters
    // ---------------------------
    public String getTermoBusca() { return termoBusca; }
    public void setTermoBusca(String termoBusca) { this.termoBusca = termoBusca; }

    public List<Agendamento> getResultadosBusca() { return resultadosBusca; }
    public Agendamento getAgendamentoSelecionado() { return agendamentoSelecionado; }
    public void setAgendamentoSelecionado(Agendamento agendamentoSelecionado) { this.agendamentoSelecionado = agendamentoSelecionado; }

    public String getWalkNome() { return walkNome; }
    public void setWalkNome(String walkNome) { this.walkNome = walkNome; }
    public String getWalkCpf() { return walkCpf; }
    public void setWalkCpf(String walkCpf) { this.walkCpf = walkCpf; }
    public Servico getWalkServico() { return walkServico; }
    public void setWalkServico(Servico walkServico) { this.walkServico = walkServico; }
    public Funcionario getWalkFuncionario() { return walkFuncionario; }
    public void setWalkFuncionario(Funcionario walkFuncionario) { this.walkFuncionario = walkFuncionario; }
    public LocalDate getWalkData() { return walkData; }
    public void setWalkData(LocalDate walkData) { this.walkData = walkData; }
    public LocalTime getWalkHora() { return walkHora; }
    public void setWalkHora(LocalTime walkHora) { this.walkHora = walkHora; }
}