/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.StatusAtendente;
import io.github.scognamiglioo.entities.StatusAtendente.Situacao;
import io.github.scognamiglioo.services.AtendenteServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.security.enterprise.SecurityContext;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 *
 * @author PABLO DANIEL
 */


@Named("atendenteController")
@ViewScoped
public class AtendenteController implements Serializable {

    @Inject
    private AtendenteServiceLocal atendimentoService;

    private Long idFuncionario;
    private Funcionario atendente;
    private StatusAtendente.Situacao situacaoAtual = StatusAtendente.Situacao.INDISPONIVEL;
    private String motivoPausa;
    private List<StatusAtendente> historico;

    @PostConstruct
    public void init() {
        try {
            String username = FacesContext.getCurrentInstance()
                                  .getExternalContext()
                                  .getRemoteUser(); // ou SecurityContext se usar

            idFuncionario = atendimentoService.buscarIdFuncionarioPorUsername(username);
            if (idFuncionario == null) throw new IllegalStateException("Funcionário não encontrado");

            // Buscar funcionário
            atendente = FacesContext.getCurrentInstance()
                          .getApplication()
                          .evaluateExpressionGet(FacesContext.getCurrentInstance(),
                                  "#{em.find('io.github.scognamiglioo.entities.Funcionario'," + idFuncionario + ")}",
                                  Funcionario.class);

            // Buscar status atual do atendente
            situacaoAtual = atendimentoService.buscarStatusAtual(idFuncionario);

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Erro ao carregar dados do atendente: " + e.getMessage(), null));
        }
    }

    public void disponibilizar() { alterarSituacao(StatusAtendente.Situacao.DISPONIVEL); }
    public void pausar() { alterarSituacao(StatusAtendente.Situacao.PAUSA); }
    public void indisponibilizar() { alterarSituacao(StatusAtendente.Situacao.INDISPONIVEL); }
    public void ficarOcupado() { alterarSituacao(StatusAtendente.Situacao.OCUPADO); }

    private void alterarSituacao(StatusAtendente.Situacao nova) {
        try {
            StatusAtendente status = new StatusAtendente();
            status.setSituacao(nova);

            atendimentoService.alterarStatusAtendente(idFuncionario, status);
            situacaoAtual = nova;

            if (nova != StatusAtendente.Situacao.PAUSA) motivoPausa = null;

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Situação atualizada para: " + nova));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Erro ao alterar status: " + e.getMessage(), null));
        }
    }
    
    public String irParaHistorico() {
        return "/app/atendente/historico_disponibilidade.xhtml?faces-redirect=true";
    }

    public void carregarHistorico() {
        if (idFuncionario != null) {
            historico = atendimentoService.buscarHistorico(idFuncionario);
        }
    }

    public List<StatusAtendente> getHistorico() {
        if (historico == null) {
            carregarHistorico();
        }
        return historico;
    }
    

    /* logout */
    public String logout() throws IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }
    

    
    // getters e setters
    public Funcionario getAtendente() { return atendente; }
    public StatusAtendente.Situacao getSituacaoAtual() { return situacaoAtual; }
    public String getMotivoPausa() { return motivoPausa; }
    public void setMotivoPausa(String motivoPausa) { this.motivoPausa = motivoPausa; }
}