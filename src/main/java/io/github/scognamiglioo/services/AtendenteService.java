/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.StatusAtendente;
import io.github.scognamiglioo.entities.StatusAtendenteAtual;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;

/**
 *
 * @author PABLO DANIEL
 */
@Stateless
public class AtendenteService implements AtendenteServiceLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void alterarStatusAtendente(Long idFuncionario, StatusAtendente novoStatus) {
        Funcionario funcionario = em.find(Funcionario.class, idFuncionario);
        if (funcionario == null) {
            throw new IllegalArgumentException("Funcionário não encontrado: " + idFuncionario);
        }

        // --- Persistir histórico ---
        novoStatus.setFuncionario(funcionario);
        novoStatus.setAtualizacao(Instant.now());
        em.persist(novoStatus);

        // --- Atualizar status atual ---
        StatusAtendenteAtual atual = em.createQuery(
                "SELECT s FROM StatusAtendenteAtual s WHERE s.funcionario.id = :id",
                StatusAtendenteAtual.class)
                .setParameter("id", idFuncionario)
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    StatusAtendenteAtual s = new StatusAtendenteAtual();
                    s.setFuncionario(funcionario);
                    return s;
                });

        atual.setSituacao(novoStatus.getSituacao());
        atual.setAtualizacao(Instant.now());
        em.merge(atual); // merge insere ou atualiza
    }

    @Override
    public Long buscarIdFuncionarioPorUsername(String username) {
        try {
            return em.createQuery(
                    "SELECT f.id FROM Funcionario f WHERE f.user.username = :u",
                    Long.class)
                    .setParameter("u", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public StatusAtendente.Situacao buscarStatusAtual(Long idFuncionario) {
        return em.createQuery(
                "SELECT s.situacao FROM StatusAtendenteAtual s WHERE s.funcionario.id = :id",
                StatusAtendente.Situacao.class)
                .setParameter("id", idFuncionario)
                .getResultStream()
                .findFirst()
                .orElse(StatusAtendente.Situacao.INDISPONIVEL);
    }
    
    @Override
    public List<StatusAtendente> buscarHistorico(Long idFuncionario) {
        return em.createQuery(
                "SELECT s FROM StatusAtendente s WHERE s.funcionario.id = :id ORDER BY s.atualizacao DESC",
                StatusAtendente.class)
                .setParameter("id", idFuncionario)
                .getResultList();
    }

}