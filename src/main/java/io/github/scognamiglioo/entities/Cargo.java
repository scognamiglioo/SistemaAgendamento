package io.github.scognamiglioo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa um cargo na organização.
 * Um cargo pode ser associado a múltiplos funcionários,
 * mas um funcionário possui apenas um cargo.
 */
@Entity
@Table(name = "cargo")
@NamedQueries({
    @NamedQuery(name = "Cargo.findAll", 
                query = "SELECT c FROM Cargo c ORDER BY c.nome"),
    @NamedQuery(name = "Cargo.findByNome", 
                query = "SELECT c FROM Cargo c WHERE c.nome = :nome"),
    @NamedQuery(name = "Cargo.findByNomePartial", 
                query = "SELECT c FROM Cargo c WHERE LOWER(c.nome) LIKE LOWER(:nome) ORDER BY c.nome"),
    @NamedQuery(name = "Cargo.countFuncionarios",
                query = "SELECT COUNT(f) FROM Funcionario f WHERE f.cargo.id = :cargoId")
})
public class Cargo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Nome do cargo é obrigatório")
    @Size(min = 2, max = 100, message = "Nome do cargo deve ter entre 2 e 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100, unique = true)
    private String nome;

    // Relacionamento OneToMany com Funcionario
    @OneToMany(mappedBy = "cargo", cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    private List<Funcionario> funcionarios;

    // Construtor padrão
    public Cargo() {
    }

    // Construtor com parâmetros
    public Cargo(String nome) {
        this.nome = nome;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }

    // Métodos de conveniência
    public int getQuantidadeFuncionarios() {
        return funcionarios != null ? funcionarios.size() : 0;
    }

    // hashCode e equals
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cargo cargo = (Cargo) obj;
        return Objects.equals(id, cargo.id);
    }

    // toString
    @Override
    public String toString() {
        return "Cargo{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}