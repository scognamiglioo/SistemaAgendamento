package io.github.scognamiglioo.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "funcionario")
@NamedQueries({
    @NamedQuery(name = "Funcionario.byCpf", query = "SELECT e FROM Funcionario e WHERE e.user.cpf = :cpf"),
    @NamedQuery(name = "Funcionario.byUsername", query = "SELECT e FROM Funcionario e WHERE e.user.username = :username"),
    @NamedQuery(name = "Funcionario.all", query = "SELECT e FROM Funcionario e ORDER BY e.user.nome")
})
public class Funcionario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // cada Funcionario referencia um User
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

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

    // construtor utilitário usando User
    public Funcionario(User user, Role role, Guiche guiche, boolean ativo) {
        this.user = user;
        this.role = role;
        this.guiche = guiche;
        this.ativo = ativo;
    }

    public Funcionario(User user, Role role, Guiche guiche, Cargo cargo, boolean ativo) {
        this.user = user;
        this.role = role;
        this.guiche = guiche;
        this.cargo = cargo;
        this.ativo = ativo;
    }

    // getters / setters
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

    // conveniência: delegar alguns acessos ao User (útil para views e buscas)
    public String getNome() {
        return user != null ? user.getNome() : null;
    }

    public String getCpf() {
        return user != null ? user.getCpf() : null;
    }

    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    public String getUsername() {
        return user != null ? user.getUsername() : null;
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

    // helpers many-to-many
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

    

    public void setNome(String nome) {
        if (this.user == null) this.user = new User();
        this.user.setNome(nome);
    }

    

    public void setCpf(String cpf) {
        if (this.user == null) this.user = new User();
        this.user.setCpf(cpf);
    }

    

    public void setEmail(String email) {
        if (this.user == null) this.user = new User();
        this.user.setEmail(email);
    }

    public String getTelefone() {
        return user != null ? user.getTelefone() : null;
    }

    public void setTelefone(String telefone) {
        if (this.user == null) this.user = new User();
        this.user.setTelefone(telefone);
    }

    

    public void setUsername(String username) {
        if (this.user == null) this.user = new User();
        this.user.setUsername(username);
    }

    public String getPassword() {
        return user != null ? user.getUserPassword() : null;
    }

    public void setPassword(String password) {
        if (this.user == null) this.user = new User();
        this.user.setUserPassword(password);
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