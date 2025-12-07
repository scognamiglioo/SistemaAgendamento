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

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        em.merge(agendamento);
        em.flush();
        LOGGER.log(Level.INFO, "Agendamento cancelado: {0}", agendamentoId);
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

        // Verifica disponibilidade
        if (!isHorarioDisponivel(agendamento.getData(), agendamento.getHora(), funcionarioId)) {
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
    public void atribuirGuiche(Long agendamentoId, Long guicheId) {
        Agendamento agendamento = findAgendamentoById(agendamentoId);
        if (agendamento == null) {
            throw new IllegalArgumentException("Agendamento não encontrado");
        }

        Guiche guiche = em.find(Guiche.class, guicheId);
        if (guiche == null) {
            throw new IllegalArgumentException("Guichê não encontrado");
        }

        agendamento.setGuiche(guiche);
        em.merge(agendamento);
        em.flush();
        LOGGER.log(Level.INFO, "Guichê {0} atribuído ao agendamento {1}",
                new Object[]{guicheId, agendamentoId});
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

        // Busca funcionários que prestam este serviço
        // Primeira query: busca IDs dos funcionários (sem DISTINCT no fetch)
        // Segunda query: carrega funcionários com FETCH para evitar LazyInitializationException

        // Query em duas etapas para evitar problema DISTINCT + ORDER BY + FETCH no PostgreSQL
        TypedQuery<Long> idsQuery = em.createQuery(
            "SELECT DISTINCT f.id FROM Funcionario f JOIN f.servicos s WHERE s.id = :servicoId AND f.ativo = true",
            Long.class
        );
        idsQuery.setParameter("servicoId", servicoId);
        List<Long> ids = idsQuery.getResultList();

        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        // Carrega funcionários completos com FETCH
        TypedQuery<Funcionario> query = em.createQuery(
            "SELECT f FROM Funcionario f LEFT JOIN FETCH f.cargo LEFT JOIN FETCH f.user WHERE f.id IN :ids ORDER BY f.user.nome",
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
}
