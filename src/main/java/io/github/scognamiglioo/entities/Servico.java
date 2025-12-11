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
    @NamedQuery(name = "Servico.findFuncionariosByServico", query = "SELECT fs.funcionario FROM FuncionarioServico fs WHERE fs.servico.id = :servicoId ORDER BY fs.funcionario.user.nome")
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

    // Relacionamento OneToMany com a entidade associativa FuncionarioServico
    @OneToMany(mappedBy = "servico", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FuncionarioServico> funcionarioServicos = new ArrayList<>();

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

    public List<FuncionarioServico> getFuncionarioServicos() {
        return funcionarioServicos;
    }

    public void setFuncionarioServicos(List<FuncionarioServico> funcionarioServicos) {
        this.funcionarioServicos = funcionarioServicos;
    }

    // Métodos de conveniência para trabalhar com funcionários
    public List<Funcionario> getFuncionarios() {
        List<Funcionario> funcionarios = new ArrayList<>();
        if (funcionarioServicos != null) {
            for (FuncionarioServico fs : funcionarioServicos) {
                if (!funcionarios.contains(fs.getFuncionario())) {
                    funcionarios.add(fs.getFuncionario());
                }
            }
        }
        return funcionarios;
    }

    // Métodos para gerenciar associações funcionário-serviço-localização
    public void addFuncionarioLocalizacao(Funcionario funcionario, Localizacao localizacao) {
        FuncionarioServico fs = new FuncionarioServico(funcionario, this, localizacao);
        funcionarioServicos.add(fs);
    }

    public void removeFuncionarioLocalizacao(Funcionario funcionario, Localizacao localizacao) {
        funcionarioServicos.removeIf(fs -> 
            fs.getFuncionario().equals(funcionario) && fs.getLocalizacao().equals(localizacao));
    }

    public boolean hasFuncionarioInLocalizacao(Funcionario funcionario, Localizacao localizacao) {
        return funcionarioServicos.stream()
            .anyMatch(fs -> fs.getFuncionario().equals(funcionario) && 
                           fs.getLocalizacao().equals(localizacao));
    }

    @Override
    public String toString() {
        return nome + " - R$ " + valor;
    }
}
