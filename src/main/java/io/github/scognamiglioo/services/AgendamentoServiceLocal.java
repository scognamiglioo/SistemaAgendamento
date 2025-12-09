package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Agendamento;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Servico;
import io.github.scognamiglioo.entities.StatusAgendamento;
import io.github.scognamiglioo.entities.User;
import jakarta.ejb.Local;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Interface local para o serviço de agendamentos
 */
@Local
public interface AgendamentoServiceLocal {

    /**
     * Cria um novo agendamento
     */
    Agendamento createAgendamento(User user, Servico servico, LocalDate data, LocalTime hora);

    /**
     * Cria um novo agendamento com funcionário específico
     */
    Agendamento createAgendamento(User user, Servico servico, Funcionario funcionario, LocalDate data, LocalTime hora);

    /**
     * Atualiza um agendamento existente
     */
    void updateAgendamento(Agendamento agendamento);

    /**
     * Cancela um agendamento
     */
    void cancelarAgendamento(Long agendamentoId);

    /**
     * Deleta um agendamento (físico)
     */
    void deleteAgendamento(Long agendamentoId);

    /**
     * Busca agendamento por ID
     */
    Agendamento findAgendamentoById(Long id);

    /**
     * Lista todos os agendamentos
     */
    List<Agendamento> getAllAgendamentos();

    /**
     * Lista agendamentos de um usuário específico
     */
    List<Agendamento> findAgendamentosByUser(Long userId);

    /**
     * Lista agendamentos por username
     */
    List<Agendamento> findAgendamentosByUsername(String username);

    /**
     * Lista agendamentos de um funcionário específico
     */
    List<Agendamento> findAgendamentosByFuncionario(Long funcionarioId);

    /**
     * Lista agendamentos por status
     */
    List<Agendamento> findAgendamentosByStatus(StatusAgendamento status);

    /**
     * Lista agendamentos entre duas datas
     */
    List<Agendamento> findAgendamentosByDataBetween(LocalDate dataInicio, LocalDate dataFim);

    /**
     * Lista agendamentos de um funcionário em uma data específica
     */
    List<Agendamento> findAgendamentosByDataAndFuncionario(LocalDate data, Long funcionarioId);

    /**
     * Verifica se um horário está disponível para um funcionário
     */
    boolean isHorarioDisponivel(LocalDate data, LocalTime hora, Long funcionarioId);

    /**
     * Atribui um funcionário a um agendamento
     */
    void atribuirFuncionario(Long agendamentoId, Long funcionarioId);


    /**
     * Altera o status de um agendamento
     */
    void alterarStatus(Long agendamentoId, StatusAgendamento novoStatus);

    /**
     * Lista funcionários disponíveis para um serviço
     */
    List<Funcionario> findFuncionariosDisponiveisParaServico(Long servicoId);

    /**
     * Gera horários disponíveis para agendamento
     */
    List<String> getHorariosDisponiveis();

    /**
     * Verifica se um funcionário presta um serviço específico
     * 
     * @param funcionarioId o ID do funcionário a ser verificado
     * @param servicoId o ID do serviço a ser verificado
     * @return true se o funcionário presta o serviço, false caso contrário
     */
    boolean funcionarioPrestServico(Long funcionarioId, Long servicoId);

    /**
     * Busca a localização onde o funcionário presta o serviço do agendamento.
     * Faz o JOIN: Agendamento -> FuncionarioServico -> Localizacao
     *
     * @param agendamentoId ID do agendamento
     * @return Localizacao ou null se não encontrar
     */
    io.github.scognamiglioo.entities.Localizacao buscarLocalizacaoDoAgendamento(Long agendamentoId);

    /**
     * Busca a localização usando query dinâmica (alternativa à NamedQuery).
     * Útil para entender como funciona o JOIN em JPQL.
     *
     * @param agendamentoId ID do agendamento
     * @return Localizacao ou null se não encontrar
     */
    io.github.scognamiglioo.entities.Localizacao buscarLocalizacaoComQueryDinamica(Long agendamentoId);
}

