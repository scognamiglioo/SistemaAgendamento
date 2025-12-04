package io.github.scognamiglioo.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "servico")
@NamedQueries({
    @NamedQuery(name = "Servico.findAll", query = "SELECT s FROM Servico s ORDER BY s.nome"),
    @NamedQuery(name = "Servico.findByNome", query = "SELECT s FROM Servico s WHERE s.nome = :nome"),
    @NamedQuery(name = "Servico.findByNomePartial", query = "SELECT s FROM Servico s WHERE LOWER(s.nome) LIKE LOWER(:nome) ORDER BY s.nome"),
    @NamedQuery(name = "Servico.countByNome", query = "SELECT COUNT(s) FROM Servico s WHERE s.nome = :nome"),
    @NamedQuery(name = "Servico.findFuncionariosByServico", query = "SELECT f FROM Funcionario f JOIN f.servicos s WHERE s.id = :servicoId ORDER BY f.nome")
})
public class Servico implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String nome;

    @NotNull
    @Column(nullable = false)
    private Float valor;

    @ManyToMany(mappedBy = "servicos", fetch = FetchType.LAZY)
    private List<Funcionario> funcionarios = new ArrayList<>();

    public Servico() {}

    public Servico(String nome, Float valor) {
        this.nome = nome;
        this.valor = valor;
    }

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

    public Float getValor() {
        return valor;
    }

    public void setValor(Float valor) {
        this.valor = valor;
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }

    // Métodos utilitários para gerenciar relacionamento
    public void addFuncionario(Funcionario funcionario) {
        if (!funcionarios.contains(funcionario)) {
            funcionarios.add(funcionario);
            if (!funcionario.getServicos().contains(this)) {
                funcionario.getServicos().add(this);
            }
        }
    }

    public void removeFuncionario(Funcionario funcionario) {
        if (funcionarios.contains(funcionario)) {
            funcionarios.remove(funcionario);
            if (funcionario.getServicos().contains(this)) {
                funcionario.getServicos().remove(this);
            }
        }
    }

    @Override
    public String toString() {
        return nome + " - R$ " + valor;
    }
}
