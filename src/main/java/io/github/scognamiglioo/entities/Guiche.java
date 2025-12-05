package io.github.scognamiglioo.entities;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name = "guiches")
public class Guiche implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String nome;

    public Guiche() {}
    public Guiche(String nome) { this.nome = nome; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    @Override
    public String toString() { return nome; }
}
