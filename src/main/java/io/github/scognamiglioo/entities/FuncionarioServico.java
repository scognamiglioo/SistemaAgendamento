package io.github.scognamiglioo.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.persistence.*;

/**
 * Entidade que representa a tabela funcionario_servico
 * Tabela associativa entre Funcionario, Servico e Localizacao
 */
@Entity
@Table(name = "funcionario_servico")
@IdClass(FuncionarioServicoId.class)
@NamedQueries({
    @NamedQuery(name = "FuncionarioServico.findAll", 
                query = "SELECT fs FROM FuncionarioServico fs ORDER BY fs.funcionario.user.nome, fs.servico.nome, fs.localizacao.nome"),
    @NamedQuery(name = "FuncionarioServico.findByFuncionario", 
                query = "SELECT fs FROM FuncionarioServico fs WHERE fs.funcionario.id = :funcionarioId ORDER BY fs.servico.nome, fs.localizacao.nome"),
    @NamedQuery(name = "FuncionarioServico.findByServico", 
                query = "SELECT fs FROM FuncionarioServico fs WHERE fs.servico.id = :servicoId ORDER BY fs.funcionario.user.nome, fs.localizacao.nome"),
    @NamedQuery(name = "FuncionarioServico.findByLocalizacao", 
                query = "SELECT fs FROM FuncionarioServico fs WHERE fs.localizacao.id = :localizacaoId ORDER BY fs.funcionario.user.nome, fs.servico.nome"),
    @NamedQuery(name = "FuncionarioServico.findByFuncionarioAndServico", 
                query = "SELECT fs FROM FuncionarioServico fs WHERE fs.funcionario.id = :funcionarioId AND fs.servico.id = :servicoId"),
    @NamedQuery(name = "FuncionarioServico.countByFuncionario", 
                query = "SELECT COUNT(fs) FROM FuncionarioServico fs WHERE fs.funcionario.id = :funcionarioId"),
    @NamedQuery(name = "FuncionarioServico.countByServico", 
                query = "SELECT COUNT(fs) FROM FuncionarioServico fs WHERE fs.servico.id = :servicoId"),
    @NamedQuery(name = "FuncionarioServico.countByLocalizacao", 
                query = "SELECT COUNT(fs) FROM FuncionarioServico fs WHERE fs.localizacao.id = :localizacaoId")
})
public class FuncionarioServico implements Serializable {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localizacao_id", nullable = false)
    private Localizacao localizacao;

    @Column(name = "data_associacao", nullable = false)
    private LocalDateTime dataAssociacao;

    // Construtores
    public FuncionarioServico() {
        this.dataAssociacao = LocalDateTime.now();
    }

    public FuncionarioServico(Funcionario funcionario, Servico servico, Localizacao localizacao) {
        this();
        this.funcionario = funcionario;
        this.servico = servico;
        this.localizacao = localizacao;
    }

    // Getters e Setters
    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Localizacao getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(Localizacao localizacao) {
        this.localizacao = localizacao;
    }

    public LocalDateTime getDataAssociacao() {
        return dataAssociacao;
    }

    public void setDataAssociacao(LocalDateTime dataAssociacao) {
        this.dataAssociacao = dataAssociacao;
    }

    // Métodos de conveniência para acessar informações das entidades relacionadas
    public String getFuncionarioNome() {
        return funcionario != null && funcionario.getUser() != null ? funcionario.getUser().getNome() : null;
    }

    public String getServicoNome() {
        return servico != null ? servico.getNome() : null;
    }

    public String getLocalizacaoNome() {
        return localizacao != null ? localizacao.getNome() : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(funcionario, servico, localizacao);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final FuncionarioServico other = (FuncionarioServico) obj;
        return Objects.equals(this.funcionario, other.funcionario) &&
               Objects.equals(this.servico, other.servico) &&
               Objects.equals(this.localizacao, other.localizacao);
    }

    @Override
    public String toString() {
        return "FuncionarioServico{" +
               "funcionario=" + getFuncionarioNome() +
               ", servico=" + getServicoNome() +
               ", localizacao=" + getLocalizacaoNome() +
               '}';
    }
}