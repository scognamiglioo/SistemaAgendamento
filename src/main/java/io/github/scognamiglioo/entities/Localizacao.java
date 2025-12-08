package io.github.scognamiglioo.entities;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Entidade Localizacao - representa os locais onde os serviços podem ser executados
 */
@Entity
@Table(name = "localizacao")
@NamedQueries({
    @NamedQuery(name = "Localizacao.findAll", query = "SELECT l FROM Localizacao l ORDER BY l.nome"),
    @NamedQuery(name = "Localizacao.findByNome", query = "SELECT l FROM Localizacao l WHERE l.nome = :nome"),
    @NamedQuery(name = "Localizacao.findByNomePartial", query = "SELECT l FROM Localizacao l WHERE LOWER(l.nome) LIKE LOWER(:nome) ORDER BY l.nome")
})
public class Localizacao implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    @NotNull(message = "Nome do local é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Column(name = "descricao", length = 500)
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    // Construtores
    public Localizacao() {
    }

    public Localizacao(String nome) {
        this.nome = nome;
    }

    public Localizacao(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
        final Localizacao other = (Localizacao) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Localizacao{" + "id=" + id + ", nome=" + nome + '}';
    }
}