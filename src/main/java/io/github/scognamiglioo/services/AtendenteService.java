/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.StatusAtendente;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.Instant;

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

        novoStatus.setFuncionario(funcionario);
        novoStatus.setAtualizacao(Instant.now());

        em.persist(novoStatus);
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
}
