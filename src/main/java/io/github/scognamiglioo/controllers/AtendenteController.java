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
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.security.enterprise.SecurityContext;
import java.io.Serializable;
import java.time.Instant;

/**
 *
 * @author PABLO DANIEL
 */


@Named("atendenteController")
@ViewScoped
public class AtendenteController implements Serializable {

    @Inject
    private AtendenteServiceLocal atendimentoService;

    @Inject
    private SecurityContext securityContext;

    @PersistenceContext
    private EntityManager em;

    private Long idFuncionario;
    private Funcionario atendente;

    private Situacao situacaoAtual = Situacao.INDISPONIVEL;
    private String motivoPausa;

    @PostConstruct
    public void init() {
        try {
            String username = securityContext.getCallerPrincipal().getName();
            idFuncionario = atendimentoService.buscarIdFuncionarioPorUsername(username);

            if (idFuncionario == null) {
                throw new IllegalStateException("Funcionário não encontrado para login: " + username);
            }

            atendente = em.find(Funcionario.class, idFuncionario);

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Erro ao carregar dados do atendente: " + e.getMessage(), null));
        }
    }

    public void ficarDisponivel() { alterarSituacao(Situacao.DISPONIVEL); }
    public void ficarOcupado() { alterarSituacao(Situacao.OCUPADO); }
    public void pausar() { alterarSituacao(Situacao.PAUSA); }
    public void ficarIndisponivel() { alterarSituacao(Situacao.INDISPONIVEL); }

    public void disponibilizar() { ficarDisponivel(); }
    public void indisponibilizar() { ficarIndisponivel(); }

    private void alterarSituacao(Situacao nova) {
        try {
            StatusAtendente status = new StatusAtendente();
            status.setFuncionario(atendente);
            status.setSituacao(nova);
            status.setAtualizacao(Instant.now());

            atendimentoService.alterarStatusAtendente(idFuncionario, status);

            situacaoAtual = nova;

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Situação atualizada para: " + nova));

            if (nova != Situacao.PAUSA) motivoPausa = null;

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Erro ao alterar status: " + e.getMessage(), null));
        }
    }

    // GETTERS e SETTERS
    public Funcionario getAtendente() { return atendente; }
    public Situacao getSituacaoAtual() { return situacaoAtual; }
    public String getMotivoPausa() { return motivoPausa; }
    public void setMotivoPausa(String motivoPausa) { this.motivoPausa = motivoPausa; }
}