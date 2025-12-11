package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.*;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço EJB para gerenciar agendamentos
 */
@Stateless
@LocalBean
public class AgendamentoService implements AgendamentoServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(AgendamentoService.class.getName());

    @PersistenceContext(unitName = "SecureAppPU")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Agendamento createAgendamento(User user, Servico servico, LocalDate data, LocalTime hora) {
        if (user == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }
        if (servico == null) {
            throw new IllegalArgumentException("Serviço é obrigatório");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        if (hora == null) {
            throw new IllegalArgumentException("Hora é obrigatória");
        }
        if (data.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data do agendamento não pode ser anterior à data atual");
        }

        Agendamento agendamento = new Agendamento(user, servico, data, hora);
        em.persist(agendamento);
        em.flush();

        LOGGER.log(Level.INFO, "Agendamento criado: {0}", agendamento);
        return agendamento;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Agendamento createAgendamento(User user, Servico servico, Funcionario funcionario, LocalDate data, LocalTime hora) {
        if (funcionario != null && !isHorarioDisponivel(data, hora, funcionario.getId())) {
            throw new IllegalArgumentException("O horário selecionado não está disponível para este funcionário");
        }

        Agendamento agendamento = createAgendamento(user, servico, data, hora);
        agendamento.setFuncionario(funcionario);
        em.merge(agendamento);
        em.flush();

        return agendamento;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateAgendamento(Agendamento agendamento) {
        if (agendamento == null || agendamento.getId() == null) {
            throw new IllegalArgumentException("Agendamento inválido");
        }

        em.merge(agendamento);
        em.flush();
        LOGGER.log(Level.INFO, "Agendamento atualizado: {0}", agendamento.getId());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void cancelarAgendamento(Long agendamentoId) {
        Agendamento agendamento = findAgendamentoById(agendamentoId);
        if (agendamento == null) {
            throw new IllegalArgumentException("Agendamento não encontrado");
        }

        // Verifica se o cancelamento está sendo feito com pelo menos 24 horas de antecedência
        LocalDate dataAgendamento = agendamento.getData();
        LocalTime horaAgendamento = agendamento.getHora();

        // Combina data e hora do agendamento
        java.time.LocalDateTime dataHoraAgendamento = java.time.LocalDateTime.of(dataAgendamento, horaAgendamento);

        // Obtém data/hora atual
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();

        // Calcula diferença em horas
        long horasRestantes = java.time.Duration.between(agora, dataHoraAgendamento).toHours();

        // Valida se há pelo menos 24 horas de antecedência
        if (horasRestantes < 24) {
            throw new IllegalArgumentException(
                    "Não é possível cancelar o agendamento com menos de 24 horas de antecedência. " +
                            "Agendamento marcado para " + agendamento.getDataFormatada() + " às " + agendamento.getHoraFormatada() + "."
            );
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        em.merge(agendamento);
        em.flush();
        LOGGER.log(Level.INFO, "Agendamento {0} cancelado com {1} horas de antecedência",
                new Object[]{agendamentoId, horasRestantes});
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteAgendamento(Long agendamentoId) {
        Agendamento agendamento = findAgendamentoById(agendamentoId);
        if (agendamento != null) {
            em.remove(agendamento);
            em.flush();
            LOGGER.log(Level.INFO, "Agendamento deletado: {0}", agendamentoId);
        }
    }

    @Override
    public Agendamento findAgendamentoById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Agendamento.class, id);
    }

    @Override
    public List<Agendamento> getAllAgendamentos() {
        return em.createNamedQuery("Agendamento.findAll", Agendamento.class)
                .getResultList();
    }

    @Override
    public List<Agendamento> findAgendamentosByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }

        return em.createNamedQuery("Agendamento.findByUser", Agendamento.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<Agendamento> findAgendamentosByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return em.createNamedQuery("Agendamento.findByUsername", Agendamento.class)
                .setParameter("username", username.trim())
                .getResultList();
    }

    @Override
    public List<Agendamento> findAgendamentosByFuncionario(Long funcionarioId) {
        if (funcionarioId == null) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }

        return em.createNamedQuery("Agendamento.findByFuncionario", Agendamento.class)
                .setParameter("funcionarioId", funcionarioId)
                .getResultList();
    }

    @Override
    public List<Agendamento> findAgendamentosByStatus(StatusAgendamento status) {
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }

        return em.createNamedQuery("Agendamento.findByStatus", Agendamento.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<Agendamento> findAgendamentosByDataBetween(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }

        return em.createNamedQuery("Agendamento.findByDataBetween", Agendamento.class)
                .setParameter("dataInicio", dataInicio)
                .setParameter("dataFim", dataFim)
                .getResultList();
    }

    @Override
    public List<Agendamento> findAgendamentosByDataAndFuncionario(LocalDate data, Long funcionarioId) {
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        if (funcionarioId == null) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }

        return em.createNamedQuery("Agendamento.findByDataAndFuncionario", Agendamento.class)
                .setParameter("data", data)
                .setParameter("funcionarioId", funcionarioId)
                .getResultList();
    }

    @Override
    public boolean isHorarioDisponivel(LocalDate data, LocalTime hora, Long funcionarioId) {
        if (data == null || hora == null || funcionarioId == null) {
            return false;
        }

        Long count = em.createNamedQuery("Agendamento.countByDataHoraFuncionario", Long.class)
                .setParameter("data", data)
                .setParameter("hora", hora)
                .setParameter("funcionarioId", funcionarioId)
                .getSingleResult();

        return count == 0;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void atribuirFuncionario(Long agendamentoId, Long funcionarioId) {
        Agendamento agendamento = findAgendamentoById(agendamentoId);
        if (agendamento == null) {
            throw new IllegalArgumentException("Agendamento não encontrado");
        }

        Funcionario funcionario = em.find(Funcionario.class, funcionarioId);
        if (funcionario == null) {
            throw new IllegalArgumentException("Funcionário não encontrado");
        }

        // Verifica disponibilidade EXCLUINDO o próprio agendamento
        Long count = em.createQuery(
                        "SELECT COUNT(a) FROM Agendamento a WHERE a.data = :data AND a.hora = :hora " +
                                "AND a.funcionario.id = :funcionarioId AND a.id <> :agendamentoId AND a.status <> 'CANCELADO'",
                        Long.class)
                .setParameter("data", agendamento.getData())
                .setParameter("hora", agendamento.getHora())
                .setParameter("funcionarioId", funcionarioId)
                .setParameter("agendamentoId", agendamentoId)
                .getSingleResult();

        if (count > 0) {
            throw new IllegalArgumentException("Funcionário não disponível neste horário");
        }

        agendamento.setFuncionario(funcionario);
        em.merge(agendamento);
        em.flush();
        LOGGER.log(Level.INFO, "Funcionário {0} atribuído ao agendamento {1}",
                new Object[]{funcionarioId, agendamentoId});
    }


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void alterarStatus(Long agendamentoId, StatusAgendamento novoStatus) {
        Agendamento agendamento = findAgendamentoById(agendamentoId);
        if (agendamento == null) {
            throw new IllegalArgumentException("Agendamento não encontrado");
        }
        if (novoStatus == null) {
            throw new IllegalArgumentException("Novo status é obrigatório");
        }

        agendamento.setStatus(novoStatus);
        em.merge(agendamento);
        em.flush();
        LOGGER.log(Level.INFO, "Status do agendamento {0} alterado para {1}",
                new Object[]{agendamentoId, novoStatus});
    }

    @Override
    public List<Funcionario> findFuncionariosDisponiveisParaServico(Long servicoId) {
        if (servicoId == null) {
            throw new IllegalArgumentException("ID do serviço é obrigatório");
        }

        // Step 1: Buscar IDs dos funcionários que prestam o serviço
        TypedQuery<Long> idsQuery = em.createQuery(
                "SELECT DISTINCT f.id FROM Funcionario f " +
                        "JOIN f.funcionarioServicos fs " +
                        "WHERE fs.servico.id = :servicoId AND f.ativo = true",
                Long.class
        );
        idsQuery.setParameter("servicoId", servicoId);
        List<Long> ids = idsQuery.getResultList();

        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 2: Carrega funcionários completos com FETCH para evitar lazy loading
        TypedQuery<Funcionario> query = em.createQuery(
                "SELECT f FROM Funcionario f " +
                        "LEFT JOIN FETCH f.cargo " +
                        "LEFT JOIN FETCH f.user " +
                        "WHERE f.id IN :ids " +
                        "ORDER BY f.user.nome",
                Funcionario.class
        );
        query.setParameter("ids", ids);

        return query.getResultList();
    }


    @Override
    public List<String> getHorariosDisponiveis() {
        List<String> horarios = new ArrayList<>();

        // Horários de 8h às 18h, com intervalos de 30 minutos
        for (int hora = 8; hora < 18; hora++) {
            horarios.add(String.format("%02d:00", hora));
            horarios.add(String.format("%02d:30", hora));
        }
        horarios.add("18:00");

        return horarios;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean funcionarioPrestServico(Long funcionarioId, Long servicoId) {
        if (funcionarioId == null || servicoId == null) {
            return false;
        }

        try {
            // Query para verificar se existe relação entre funcionário e serviço
            Long count = em.createQuery(
                            "SELECT COUNT(fs) FROM FuncionarioServico fs " +
                                    "WHERE fs.funcionario.id = :funcionarioId AND fs.servico.id = :servicoId",
                            Long.class)
                    .setParameter("funcionarioId", funcionarioId)
                    .setParameter("servicoId", servicoId)
                    .getSingleResult();

            return count > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao verificar se funcionário presta serviço", e);
            return false;
        }
    }

    /**
     * Busca a localização onde o funcionário presta o serviço do agendamento.
     * Faz o JOIN: Agendamento -> FuncionarioServico -> Localizacao
     *
     * @param agendamentoId ID do agendamento
     * @return Localizacao ou null se não encontrar
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Localizacao buscarLocalizacaoDoAgendamento(Long agendamentoId) {
        if (agendamentoId == null) {
            LOGGER.log(Level.WARNING, "ID do agendamento não pode ser nulo");
            return null;
        }

        try {
            List<Localizacao> resultados = em.createNamedQuery("Agendamento.findLocalizacaoServicoPrestado", Localizacao.class)
                .setParameter("agendamentoId", agendamentoId)
                .getResultList();

            if (resultados.isEmpty()) {
                LOGGER.log(Level.WARNING, "Nenhuma localização encontrada para o agendamento: " + agendamentoId);
                return null;
            }

            if (resultados.size() > 1) {
                LOGGER.log(Level.WARNING,
                    "Múltiplas localizações encontradas para o agendamento " + agendamentoId +
                    ". Retornando a primeira.");
            }

            return resultados.get(0);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar localização do agendamento: " + agendamentoId, e);
            return null;
        }
    }

    /**
     * Busca a localização usando query dinâmica (alternativa à NamedQuery).
     * Útil para entender como funciona o JOIN em JPQL.
     *
     * @param agendamentoId ID do agendamento
     * @return Localizacao ou null se não encontrar
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Localizacao buscarLocalizacaoComQueryDinamica(Long agendamentoId) {
        if (agendamentoId == null) {
            return null;
        }

        try {
            String jpql = "SELECT fs.localizacao FROM Agendamento a " +
                          "JOIN FuncionarioServico fs ON fs.funcionario.id = a.funcionario.id " +
                          "AND fs.servico.id = a.servico.id " +
                          "WHERE a.id = :agendamentoId";

            List<Localizacao> resultados = em.createQuery(jpql, Localizacao.class)
                .setParameter("agendamentoId", agendamentoId)
                .getResultList();

            return resultados.isEmpty() ? null : resultados.get(0);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar localização com query dinâmica", e);
            return null;
        }
    }

    @Override
    public List<Agendamento> findAgendamentosFilaEspera() {
        try {
            LocalDate hoje = LocalDate.now();

            // Busca agendamentos CONFIRMADOS ou AGENDADOS para hoje, ordenados por hora
            String jpql = "SELECT a FROM Agendamento a " +
                         "LEFT JOIN FETCH a.user " +
                         "LEFT JOIN FETCH a.servico " +
                         "LEFT JOIN FETCH a.funcionario " +
                         "WHERE a.data = :data " +
                         "AND (a.status = :statusConfirmado OR a.status = :statusAgendado) " +
                         "ORDER BY a.hora ASC";

            return em.createQuery(jpql, Agendamento.class)
                    .setParameter("data", hoje)
                    .setParameter("statusConfirmado", StatusAgendamento.CONFIRMADO)
                    .setParameter("statusAgendado", StatusAgendamento.AGENDADO)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar fila de espera", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Agendamento> findAgendamentosEmAtendimento() {
        try {
            LocalDate hoje = LocalDate.now();

            // Busca agendamentos EM_ATENDIMENTO para hoje
            String jpql = "SELECT a FROM Agendamento a " +
                         "LEFT JOIN FETCH a.user " +
                         "LEFT JOIN FETCH a.servico " +
                         "LEFT JOIN FETCH a.funcionario " +
                         "WHERE a.data = :data " +
                         "AND a.status = :status " +
                         "ORDER BY a.hora ASC";

            return em.createQuery(jpql, Agendamento.class)
                    .setParameter("data", hoje)
                    .setParameter("status", StatusAgendamento.EM_ATENDIMENTO)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar agendamentos em atendimento", e);
            return new ArrayList<>();
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void iniciarAtendimento(Long agendamentoId) {
        Agendamento agendamento = findAgendamentoById(agendamentoId);
        if (agendamento == null) {
            throw new IllegalArgumentException("Agendamento não encontrado");
        }

        if (agendamento.getStatus() == StatusAgendamento.EM_ATENDIMENTO) {
            throw new IllegalArgumentException("Agendamento já está em atendimento");
        }

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new IllegalArgumentException("Agendamento já foi concluído");
        }

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new IllegalArgumentException("Agendamento está cancelado");
        }

        agendamento.setStatus(StatusAgendamento.EM_ATENDIMENTO);
        em.merge(agendamento);
        em.flush();
        LOGGER.log(Level.INFO, "Atendimento iniciado para o agendamento {0}", agendamentoId);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void finalizarAtendimento(Long agendamentoId) {
        Agendamento agendamento = findAgendamentoById(agendamentoId);
        if (agendamento == null) {
            throw new IllegalArgumentException("Agendamento não encontrado");
        }

        if (agendamento.getStatus() != StatusAgendamento.EM_ATENDIMENTO) {
            throw new IllegalArgumentException("Agendamento não está em atendimento");
        }

        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        em.merge(agendamento);
        em.flush();
        LOGGER.log(Level.INFO, "Atendimento finalizado para o agendamento {0}", agendamentoId);
    }

    
    @Override
    public List<Agendamento> searchByCpfOrProtocoloOrName(String termo) {
        if (termo == null || termo.isBlank()) return new ArrayList<>();

        List<Agendamento> resultados = em.createQuery(
            "SELECT a FROM Agendamento a " +
            "WHERE (a.user.cpf = :cpf OR LOWER(a.user.nome) LIKE :nome OR CAST(a.id AS string) = :id) " +
            "AND a.data = :hoje", Agendamento.class)
            .setParameter("cpf", termo)
            .setParameter("nome", "%" + termo.toLowerCase() + "%")
            .setParameter("id", termo)
            .setParameter("hoje", LocalDate.now())
            .getResultList();

        return resultados;
    }

}
