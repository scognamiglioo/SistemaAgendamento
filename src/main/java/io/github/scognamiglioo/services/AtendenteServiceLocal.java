/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/SessionLocal.java to edit this template
 */
package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.StatusAtendente;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author PABLO DANIEL
 */
@Local
public interface AtendenteServiceLocal {
    
    void alterarStatusAtendente(Long idAtendente, StatusAtendente novoStatus);

    Long buscarIdFuncionarioPorUsername(String username);
    
    public StatusAtendente.Situacao buscarStatusAtual(Long idFuncionario);
    
    List<StatusAtendente> buscarHistorico(Long idFuncionario); // novo método

}


