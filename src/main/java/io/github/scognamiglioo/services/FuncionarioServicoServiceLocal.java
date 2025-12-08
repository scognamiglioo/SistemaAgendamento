package io.github.scognamiglioo.services;

import jakarta.ejb.LocalBean;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Servico;
import io.github.scognamiglioo.entities.Localizacao;
import io.github.scognamiglioo.entities.FuncionarioServico;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Interface local para o serviço de FuncionarioServico
 */
@Local
public interface FuncionarioServicoServiceLocal {

    // ========== CRIAÇÃO ==========
    FuncionarioServico createAssociacao(Funcionario funcionario, Servico servico, Localizacao localizacao);
    FuncionarioServico createAssociacao(Long funcionarioId, Long servicoId, Long localizacaoId);

    // ========== BUSCA ==========
    List<FuncionarioServico> getAllAssociacoes();
    List<FuncionarioServico> findAssociacoesByFuncionario(Long funcionarioId);
    List<FuncionarioServico> findAssociacoesByServico(Long servicoId);
    List<FuncionarioServico> findAssociacoesByLocalizacao(Long localizacaoId);
    List<FuncionarioServico> findAssociacoesByFuncionarioAndServico(Long funcionarioId, Long servicoId);
    FuncionarioServico findAssociacaoById(Long funcionarioId, Long servicoId, Long localizacaoId);
    
    @Deprecated
    FuncionarioServico findAssociacaoById(Long id);

    /**
     * Buscar funcionários por serviço
     */
    List<Funcionario> findFuncionariosByServico(Long servicoId);
    List<Funcionario> findFuncionariosByServicoAndLocalizacao(Long servicoId, Long localizacaoId);

    /**
     * Buscar serviços por funcionário
     */
    List<Servico> findServicosByFuncionario(Long funcionarioId);
    List<Servico> findServicosByFuncionarioAndLocalizacao(Long funcionarioId, Long localizacaoId);

    /**
     * Buscar localizações
     */
    List<Localizacao> findLocalizacoesByFuncionario(Long funcionarioId);
    List<Localizacao> findLocalizacoesByServico(Long servicoId);
    List<Localizacao> findLocalizacoesByFuncionarioAndServico(Long funcionarioId, Long servicoId);

    // ========== VALIDAÇÃO ==========
    boolean existsAssociacao(Long funcionarioId, Long servicoId, Long localizacaoId);
    long countAssociacoesByFuncionario(Long funcionarioId);
    long countAssociacoesByServico(Long servicoId);
    long countAssociacoesByLocalizacao(Long localizacaoId);

    // ========== UPDATE E DELETE ==========
    FuncionarioServico updateAssociacao(FuncionarioServico associacao);
    void deleteAssociacao(Long funcionarioId, Long servicoId, Long localizacaoId);
    void deleteAssociacoesByFuncionario(Long funcionarioId);
    void deleteAssociacoesByServico(Long servicoId);
    void deleteAssociacoesByLocalizacao(Long localizacaoId);
    
    // ========== BUSCA COM FILTROS ==========
    List<FuncionarioServico> findAssociacoesWithFilters(Long funcionarioId, Long servicoId, Long localizacaoId);
    
    // ========== BUSCA DE FUNCIONÁRIOS COM RELACIONAMENTOS ==========
    List<Funcionario> getAllFuncionariosWithCargo();
}