package io.github.scognamiglioo.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Classe de chave primária composta para FuncionarioServico
 */
public class FuncionarioServicoId implements Serializable {
    
    private Long funcionario;
    private Long servico;
    private Long localizacao;
    
    // Constructors
    public FuncionarioServicoId() {}
    
    public FuncionarioServicoId(Long funcionario, Long servico, Long localizacao) {
        this.funcionario = funcionario;
        this.servico = servico;
        this.localizacao = localizacao;
    }
    
    // Getters and Setters
    public Long getFuncionario() {
        return funcionario;
    }
    
    public void setFuncionario(Long funcionario) {
        this.funcionario = funcionario;
    }
    
    public Long getServico() {
        return servico;
    }
    
    public void setServico(Long servico) {
        this.servico = servico;
    }
    
    public Long getLocalizacao() {
        return localizacao;
    }
    
    public void setLocalizacao(Long localizacao) {
        this.localizacao = localizacao;
    }
    
    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuncionarioServicoId that = (FuncionarioServicoId) o;
        return Objects.equals(funcionario, that.funcionario) &&
               Objects.equals(servico, that.servico) &&
               Objects.equals(localizacao, that.localizacao);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(funcionario, servico, localizacao);
    }
    
    @Override
    public String toString() {
        return "FuncionarioServicoId{" +
               "funcionario=" + funcionario +
               ", servico=" + servico +
               ", localizacao=" + localizacao +
               '}';
    }
}