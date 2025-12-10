/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.scognamiglioo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

/**
 *
 * @author PABLO DANIEL
 */

@Entity
@Table(name = "status_atendente_atual")
public class StatusAtendenteAtual implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_funcionario", nullable = false, unique = true)
    private Funcionario funcionario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAtendente.Situacao situacao;

    @Column(nullable = false)
    private Instant atualizacao;

    public Long getId() { return id; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
    public StatusAtendente.Situacao getSituacao() { return situacao; }
    public void setSituacao(StatusAtendente.Situacao situacao) { this.situacao = situacao; }
    public Instant getAtualizacao() { return atualizacao; }
    public void setAtualizacao(Instant atualizacao) { this.atualizacao = atualizacao; }
}
