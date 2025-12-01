package io.github.scognamiglioo.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "funcionario", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cpf"}),
    @UniqueConstraint(columnNames = {"username"}),
    @UniqueConstraint(columnNames = {"email"})
})
@NamedQueries({
    @NamedQuery(name = "Funcionario.byCpf", query = "SELECT e FROM Funcionario e WHERE e.cpf = :cpf"),
    @NamedQuery(name = "Funcionario.byUsername", query = "SELECT e FROM Funcionario e WHERE e.username = :username"),
    @NamedQuery(name = "Funcionario.all", query = "SELECT e FROM Funcionario e ORDER BY e.nome")
})

public class Funcionario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String nome;

    @NotNull
    @Size(min = 11, max = 11)
    @Column(length = 11, nullable = false)
    private String cpf;

    @NotNull
    @Email
    private String email;

    
    @Size(min = 8, max = 20)
    private String telefone;

    @NotNull
    private String username;

    @NotNull
    @Column(name = "user_password")
    private String password;
    

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "guiche_id")
    private Guiche guiche; // opcional para atendente

    // Relacionamento ManyToOne com Cargo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", nullable = true)
    private Cargo cargo;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "funcionario_servico",
        joinColumns = @JoinColumn(name = "funcionario_id"),
        inverseJoinColumns = @JoinColumn(name = "servico_id")
    )
    private List<Servico> servicos = new ArrayList<>();

    public Funcionario() {
    }

    // construtor utilitário
    public Funcionario(String nome, String cpf, String email, String telefone,
            String username, String password, Role role, Guiche guiche, boolean ativo) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.username = username;
        this.password = password;
        this.role = role;
        this.guiche = guiche;
        this.ativo = ativo;
    }

    // construtor com cargo
    public Funcionario(String nome, String cpf, String email, String telefone,
            String username, String password, Role role, Guiche guiche, Cargo cargo, boolean ativo) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.username = username;
        this.password = password;
        this.role = role;
        this.guiche = guiche;
        this.cargo = cargo;
        this.ativo = ativo;
    }

    // getters / setters (gerar todos)
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Guiche getGuiche() {
        return guiche;
    }

    public void setGuiche(Guiche guiche) {
        this.guiche = guiche;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public List<Servico> getServicos() {
        return servicos;
    }

    public void setServicos(List<Servico> servicos) {
        this.servicos = servicos;
    }

    // Métodos utilitários para gerenciar relacionamento
    public void addServico(Servico servico) {
        if (!servicos.contains(servico)) {
            servicos.add(servico);
            if (!servico.getFuncionarios().contains(this)) {
                servico.getFuncionarios().add(this);
            }
        }
    }

    public void removeServico(Servico servico) {
        if (servicos.contains(servico)) {
            servicos.remove(servico);
            if (servico.getFuncionarios().contains(this)) {
                servico.getFuncionarios().remove(this);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Funcionario other = (Funcionario) obj;
        return Objects.equals(this.id, other.id);
    }
}
