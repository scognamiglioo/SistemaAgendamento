package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Servico;
import java.util.List;
import jakarta.ejb.Local;

@Local
public interface ServicoServiceLocal {
    
    Servico createServico(String nome, Float valor);
    
    List<Servico> getAllServicos();
    
    Servico findServicoById(Long id);
    
    void updateServico(Servico servico);
    
    void deleteServico(Long id);
    
    boolean servicoExists(String nome);
    
    List<Servico> findServicosByNome(String nome);
    
    List<Servico> findServicosByNomePartial(String nome);
    
    List<Funcionario> findFuncionariosByServico(Long servicoId);
    
    void associarFuncionarioAoServico(Long funcionarioId, Long servicoId);
    
    void desassociarFuncionarioDoServico(Long funcionarioId, Long servicoId);
}