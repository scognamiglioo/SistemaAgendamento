package io.github.scognamiglioo.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Entidade que representa um agendamento no sistema.
 * Um agendamento relaciona um usuário, um serviço, um funcionário e um horário.
 */
@Entity
@Table(name = "agendamento")
@NamedQueries({
        @NamedQuery(name = "Agendamento.findAll",
                query = "SELECT a FROM Agendamento a ORDER BY a.data DESC, a.hora DESC"),
        @NamedQuery(name = "Agendamento.findByUser",
                query = "SELECT a FROM Agendamento a WHERE a.user.id = :userId ORDER BY a.data DESC, a.hora DESC"),
        @NamedQuery(name = "Agendamento.findByUsername",
                query = "SELECT a FROM Agendamento a WHERE a.user.username = :username ORDER BY a.data DESC, a.hora DESC"),
        @NamedQuery(name = "Agendamento.findByFuncionario",
                query = "SELECT a FROM Agendamento a WHERE a.funcionario.id = :funcionarioId ORDER BY a.data, a.hora"),
        @NamedQuery(name = "Agendamento.findByDataAndFuncionario",
                query = "SELECT a FROM Agendamento a WHERE a.data = :data AND a.funcionario.id = :funcionarioId"),
        @NamedQuery(name = "Agendamento.findByStatus",
                query = "SELECT a FROM Agendamento a WHERE a.status = :status ORDER BY a.data, a.hora"),
        @NamedQuery(name = "Agendamento.findByDataBetween",
                query = "SELECT a FROM Agendamento a WHERE a.data BETWEEN :dataInicio AND :dataFim ORDER BY a.data, a.hora"),
        @NamedQuery(name = "Agendamento.countByDataHoraFuncionario",
                query = "SELECT COUNT(a) FROM Agendamento a WHERE a.data = :data AND a.hora = :hora AND a.funcionario.id = :funcionarioId AND a.status <> 'CANCELADO'"),
        @NamedQuery(name = "Agendamento.findLocalizacaoServicoPrestado",
                query = "SELECT fs.localizacao FROM Agendamento a " +
                        "JOIN FuncionarioServico fs ON fs.funcionario.id = a.funcionario.id AND fs.servico.id = a.servico.id " +
                        "WHERE a.id = :agendamentoId"),
})
public class Agendamento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull(message = "Serviço é obrigatório")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    // Campos para walk-in (atendimento sem agendamento prévio)
    @Column(name = "is_walkin", nullable = false)
    private Boolean isWalkin = false;

    @Column(name = "walkin_nome", length = 255)
    private String walkinNome;

    @Column(name = "walkin_cpf", length = 14)
    private String walkinCpf;

    @Column(name = "walkin_telefone", length = 20)
    private String walkinTelefone;


    @NotNull(message = "Data é obrigatória")
    @Column(nullable = false)
    private LocalDate data;

    @NotNull(message = "Hora é obrigatória")
    @Column(nullable = false)
    private LocalTime hora;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDate criadoEm;

    @Column(name = "atualizado_em")
    private LocalDate atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDate.now();
        atualizadoEm = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDate.now();
    }

    // Construtores
    public Agendamento() {
    }

    public Agendamento(User user, Servico servico, LocalDate data, LocalTime hora) {
        this.user = user;
        this.servico = servico;
        this.data = data;
        this.hora = hora;
        this.status = StatusAgendamento.AGENDADO;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }


    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDate getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDate criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDate getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDate atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    // Métodos auxiliares
    public String getNomeUsuario() {
        if (isWalkin && walkinNome != null) {
            return walkinNome;
        }
        return user != null ? user.getNome() : "";
    }

    public String getNomeServico() {
        return servico != null ? servico.getNome() : "";
    }

    public String getNomeFuncionario() {
        return funcionario != null ? funcionario.getNome() : "Não atribuído";
    }

    public Boolean getIsWalkin() {
        return isWalkin;
    }

    public void setIsWalkin(Boolean isWalkin) {
        this.isWalkin = isWalkin;
    }

    public String getWalkinNome() {
        return walkinNome;
    }

    public void setWalkinNome(String walkinNome) {
        this.walkinNome = walkinNome;
    }

    public String getWalkinCpf() {
        return walkinCpf;
    }

    public void setWalkinCpf(String walkinCpf) {
        this.walkinCpf = walkinCpf;
    }

    public String getWalkinTelefone() {
        return walkinTelefone;
    }

    public void setWalkinTelefone(String walkinTelefone) {
        this.walkinTelefone = walkinTelefone;
    }


    /**
     * Retorna a data formatada para exibição (dd/MM/yyyy)
     */
    public String getDataFormatada() {
        if (data == null) {
            return "";
        }
        return data.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Retorna a hora formatada para exibição (HH:mm)
     */
    public String getHoraFormatada() {
        if (hora == null) {
            return "";
        }
        return hora.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public String toString() {
        return "Agendamento{" +
                "id=" + id +
                ", user=" + (user != null ? user.getNome() : "null") +
                ", servico=" + (servico != null ? servico.getNome() : "null") +
                ", data=" + data +
                ", hora=" + hora +
                ", status=" + status +
                '}';
    }


}
