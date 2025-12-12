package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.FuncionarioServico;
import io.github.scognamiglioo.entities.FuncionarioServicoId;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Servico;
import io.github.scognamiglioo.entities.Localizacao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Implementação do serviço de FuncionarioServico
 */
@Stateless
public class FuncionarioServicoService implements FuncionarioServicoServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(FuncionarioServicoService.class.getName());

    @PersistenceContext
    private EntityManager em;

    // ========== CRIAÇÃO ==========
    @Override
    public FuncionarioServico createAssociacao(Funcionario funcionario, Servico servico, Localizacao localizacao) {
        if (funcionario == null || servico == null || localizacao == null) {
            throw new IllegalArgumentException("Funcionário, serviço e localização são obrigatórios");
        }

        if (existsAssociacao(funcionario.getId(), servico.getId(), localizacao.getId())) {
            throw new IllegalArgumentException("Já existe uma associação entre este funcionário, serviço e localização");
        }

        FuncionarioServico associacao = new FuncionarioServico(funcionario, servico, localizacao);
        em.persist(associacao);
        LOGGER.log(Level.INFO, "Associação criada: {0}", associacao);
        return associacao;
    }

    @Override
    public FuncionarioServico createAssociacao(Long funcionarioId, Long servicoId, Long localizacaoId) {
        Funcionario funcionario = em.find(Funcionario.class, funcionarioId);
        Servico servico = em.find(Servico.class, servicoId);
        Localizacao localizacao = em.find(Localizacao.class, localizacaoId);

        if (funcionario == null) {
            throw new IllegalArgumentException("Funcionário não encontrado com ID: " + funcionarioId);
        }
        if (servico == null) {
            throw new IllegalArgumentException("Serviço não encontrado com ID: " + servicoId);
        }
        if (localizacao == null) {
            throw new IllegalArgumentException("Localização não encontrada com ID: " + localizacaoId);
        }

        return createAssociacao(funcionario, servico, localizacao);
    }

    // ========== BUSCA ==========
    @Override
    public List<FuncionarioServico> getAllAssociacoes() {
        try {
            TypedQuery<FuncionarioServico> query = em.createNamedQuery("FuncionarioServico.findAll", FuncionarioServico.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar todas as associações", e);
            throw new RuntimeException("Erro ao buscar associações", e);
        }
    }

    @Override
    public List<FuncionarioServico> findAssociacoesByFuncionario(Long funcionarioId) {
        try {
            TypedQuery<FuncionarioServico> query = em.createNamedQuery("FuncionarioServico.findByFuncionario", FuncionarioServico.class);
            query.setParameter("funcionarioId", funcionarioId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar associações por funcionário: " + funcionarioId, e);
            throw new RuntimeException("Erro ao buscar associações", e);
        }
    }

    @Override
    public List<FuncionarioServico> findAssociacoesByServico(Long servicoId) {
        try {
            TypedQuery<FuncionarioServico> query = em.createNamedQuery("FuncionarioServico.findByServico", FuncionarioServico.class);
            query.setParameter("servicoId", servicoId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar associações por serviço: " + servicoId, e);
            throw new RuntimeException("Erro ao buscar associações", e);
        }
    }

    @Override
    public List<FuncionarioServico> findAssociacoesByLocalizacao(Long localizacaoId) {
        try {
            TypedQuery<FuncionarioServico> query = em.createNamedQuery("FuncionarioServico.findByLocalizacao", FuncionarioServico.class);
            query.setParameter("localizacaoId", localizacaoId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar associações por localização: " + localizacaoId, e);
            throw new RuntimeException("Erro ao buscar associações", e);
        }
    }

    @Override
    public List<FuncionarioServico> findAssociacoesByFuncionarioAndServico(Long funcionarioId, Long servicoId) {
        try {
            TypedQuery<FuncionarioServico> query = em.createNamedQuery("FuncionarioServico.findByFuncionarioAndServico", FuncionarioServico.class);
            query.setParameter("funcionarioId", funcionarioId);
            query.setParameter("servicoId", servicoId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar associações por funcionário e serviço", e);
            throw new RuntimeException("Erro ao buscar associações", e);
        }
    }

    @Override  
    public FuncionarioServico findAssociacaoById(Long funcionarioId, Long servicoId, Long localizacaoId) {
        try {
            FuncionarioServicoId id = new FuncionarioServicoId(funcionarioId, servicoId, localizacaoId);
            return em.find(FuncionarioServico.class, id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar associação por chave composta", e);
            throw new RuntimeException("Erro ao buscar associação", e);
        }
    }

    @Override
    @Deprecated
    public FuncionarioServico findAssociacaoById(Long id) {
        throw new UnsupportedOperationException("Método obsoleto: Use findAssociacaoById(Long funcionarioId, Long servicoId, Long localizacaoId)");
    }

    // ========== BUSCA DE FUNCIONÁRIOS POR SERVIÇO ==========
    @Override
    public List<Funcionario> findFuncionariosByServico(Long servicoId) {
        try {
            TypedQuery<Funcionario> query = em.createQuery(
                "SELECT fs.funcionario FROM FuncionarioServico fs WHERE fs.servico.id = :servicoId", 
                Funcionario.class);
            query.setParameter("servicoId", servicoId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar funcionários por serviço: " + servicoId, e);
            throw new RuntimeException("Erro ao buscar funcionários", e);
        }
    }

    @Override
    public List<Funcionario> findFuncionariosByServicoAndLocalizacao(Long servicoId, Long localizacaoId) {
        try {
            TypedQuery<Funcionario> query = em.createQuery(
                "SELECT DISTINCT f FROM FuncionarioServico fs " +
                "JOIN fs.funcionario f " +
                "LEFT JOIN FETCH f.user " +
                "LEFT JOIN FETCH f.cargo " +
                "WHERE fs.servico.id = :servicoId " +
                "AND fs.localizacao.id = :localizacaoId " +
                "AND f.ativo = true " +
                "ORDER BY f.user.nome",
                Funcionario.class);
            query.setParameter("servicoId", servicoId);
            query.setParameter("localizacaoId", localizacaoId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar funcionários por serviço e localização", e);
            throw new RuntimeException("Erro ao buscar funcionários", e);
        }
    }

    // ========== BUSCA DE SERVIÇOS POR FUNCIONÁRIO ==========
    @Override
    public List<Servico> findServicosByFuncionario(Long funcionarioId) {
        try {
            TypedQuery<Servico> query = em.createQuery(
                "SELECT fs.servico FROM FuncionarioServico fs WHERE fs.funcionario.id = :funcionarioId", 
                Servico.class);
            query.setParameter("funcionarioId", funcionarioId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar serviços por funcionário: " + funcionarioId, e);
            throw new RuntimeException("Erro ao buscar serviços", e);
        }
    }

    @Override
    public List<Servico> findServicosByFuncionarioAndLocalizacao(Long funcionarioId, Long localizacaoId) {
        try {
            TypedQuery<Servico> query = em.createQuery(
                "SELECT fs.servico FROM FuncionarioServico fs WHERE fs.funcionario.id = :funcionarioId AND fs.localizacao.id = :localizacaoId", 
                Servico.class);
            query.setParameter("funcionarioId", funcionarioId);
            query.setParameter("localizacaoId", localizacaoId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar serviços por funcionário e localização", e);
            throw new RuntimeException("Erro ao buscar serviços", e);
        }
    }

    // ========== BUSCA DE LOCALIZAÇÕES ==========
    @Override
    public List<Localizacao> findLocalizacoesByFuncionario(Long funcionarioId) {
        try {
            TypedQuery<Localizacao> query = em.createQuery(
                "SELECT fs.localizacao FROM FuncionarioServico fs WHERE fs.funcionario.id = :funcionarioId", 
                Localizacao.class);
            query.setParameter("funcionarioId", funcionarioId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar localizações por funcionário: " + funcionarioId, e);
            throw new RuntimeException("Erro ao buscar localizações", e);
        }
    }

    @Override
    public List<Localizacao> findLocalizacoesByServico(Long servicoId) {
        try {
            TypedQuery<Localizacao> query = em.createQuery(
                "SELECT DISTINCT fs.localizacao FROM FuncionarioServico fs " +
                "WHERE fs.servico.id = :servicoId " +
                "ORDER BY fs.localizacao.nome",
                Localizacao.class);
            query.setParameter("servicoId", servicoId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar localizações por serviço: " + servicoId, e);
            throw new RuntimeException("Erro ao buscar localizações", e);
        }
    }

    @Override
    public List<Localizacao> findLocalizacoesByFuncionarioAndServico(Long funcionarioId, Long servicoId) {
        try {
            TypedQuery<Localizacao> query = em.createQuery(
                "SELECT fs.localizacao FROM FuncionarioServico fs WHERE fs.funcionario.id = :funcionarioId AND fs.servico.id = :servicoId", 
                Localizacao.class);
            query.setParameter("funcionarioId", funcionarioId);
            query.setParameter("servicoId", servicoId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar localizações por funcionário e serviço", e);
            throw new RuntimeException("Erro ao buscar localizações", e);
        }
    }

    // ========== VALIDAÇÃO ==========
    @Override
    public boolean existsAssociacao(Long funcionarioId, Long servicoId, Long localizacaoId) {
        try {
            Long count = em.createQuery(
                "SELECT COUNT(fs) FROM FuncionarioServico fs WHERE fs.funcionario.id = :funcionarioId AND fs.servico.id = :servicoId AND fs.localizacao.id = :localizacaoId", 
                Long.class)
                .setParameter("funcionarioId", funcionarioId)
                .setParameter("servicoId", servicoId)
                .setParameter("localizacaoId", localizacaoId)
                .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao verificar se associação existe", e);
            return false;
        }
    }

    @Override
    public long countAssociacoesByFuncionario(Long funcionarioId) {
        try {
            TypedQuery<Long> query = em.createNamedQuery("FuncionarioServico.countByFuncionario", Long.class);
            query.setParameter("funcionarioId", funcionarioId);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao contar associações por funcionário: " + funcionarioId, e);
            return 0;
        }
    }

    @Override
    public long countAssociacoesByServico(Long servicoId) {
        try {
            TypedQuery<Long> query = em.createNamedQuery("FuncionarioServico.countByServico", Long.class);
            query.setParameter("servicoId", servicoId);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao contar associações por serviço: " + servicoId, e);
            return 0;
        }
    }

    @Override
    public long countAssociacoesByLocalizacao(Long localizacaoId) {
        try {
            TypedQuery<Long> query = em.createNamedQuery("FuncionarioServico.countByLocalizacao", Long.class);
            query.setParameter("localizacaoId", localizacaoId);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao contar associações por localização: " + localizacaoId, e);
            return 0;
        }
    }

    // ========== UPDATE E DELETE ==========
    @Override
    public FuncionarioServico updateAssociacao(FuncionarioServico associacao) {
        if (associacao == null || associacao.getFuncionario() == null || 
            associacao.getServico() == null || associacao.getLocalizacao() == null) {
            throw new IllegalArgumentException("Associação com funcionário, serviço e localização são obrigatórios para atualização");
        }

        try {
            FuncionarioServico associacaoAtualizada = em.merge(associacao);
            LOGGER.log(Level.INFO, "Associação atualizada: {0}", associacaoAtualizada);
            return associacaoAtualizada;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar associação: " + associacao, e);
            throw new RuntimeException("Erro ao atualizar associação", e);
        }
    }

    @Override
    public void deleteAssociacao(Long funcionarioId, Long servicoId, Long localizacaoId) {
        if (funcionarioId == null || servicoId == null || localizacaoId == null) {
            throw new IllegalArgumentException("IDs não podem ser nulos");
        }

        FuncionarioServicoId id = new FuncionarioServicoId(funcionarioId, servicoId, localizacaoId);
        FuncionarioServico associacao = em.find(FuncionarioServico.class, id);
        
        if (associacao == null) {
            throw new IllegalArgumentException("Associação não encontrada");
        }
        
        if (!em.contains(associacao)) {
            associacao = em.merge(associacao);
        }
        
        em.remove(associacao);
        em.flush();
        
        LOGGER.log(Level.INFO, "Associação removida: func={0}, serv={1}, loc={2}", 
                  new Object[]{funcionarioId, servicoId, localizacaoId});
    }

    @Override
    public void deleteAssociacoesByFuncionario(Long funcionarioId) {
        try {
            em.createQuery("DELETE FROM FuncionarioServico fs WHERE fs.funcionario.id = :funcionarioId")
                .setParameter("funcionarioId", funcionarioId)
                .executeUpdate();
            LOGGER.log(Level.INFO, "Associações do funcionário {0} removidas", funcionarioId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao deletar associações por funcionário: " + funcionarioId, e);
            throw new RuntimeException("Erro ao deletar associações", e);
        }
    }

    @Override
    public void deleteAssociacoesByServico(Long servicoId) {
        try {
            em.createQuery("DELETE FROM FuncionarioServico fs WHERE fs.servico.id = :servicoId")
                .setParameter("servicoId", servicoId)
                .executeUpdate();
            LOGGER.log(Level.INFO, "Associações do serviço {0} removidas", servicoId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao deletar associações por serviço: " + servicoId, e);
            throw new RuntimeException("Erro ao deletar associações", e);
        }
    }

    @Override
    public void deleteAssociacoesByLocalizacao(Long localizacaoId) {
        try {
            em.createQuery("DELETE FROM FuncionarioServico fs WHERE fs.localizacao.id = :localizacaoId")
                .setParameter("localizacaoId", localizacaoId)
                .executeUpdate();
            LOGGER.log(Level.INFO, "Associações da localização {0} removidas", localizacaoId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao deletar associações por localização: " + localizacaoId, e);
            throw new RuntimeException("Erro ao deletar associações", e);
        }
    }

    // ========== BUSCA COM FILTROS ==========
    @Override
    public List<FuncionarioServico> findAssociacoesWithFilters(Long funcionarioId, Long servicoId, Long localizacaoId) {
        try {
            StringBuilder jpql = new StringBuilder("SELECT fs FROM FuncionarioServico fs WHERE 1=1");
            
            if (funcionarioId != null) {
                jpql.append(" AND fs.funcionario.id = :funcionarioId");
            }
            if (servicoId != null) {
                jpql.append(" AND fs.servico.id = :servicoId");
            }
            if (localizacaoId != null) {
                jpql.append(" AND fs.localizacao.id = :localizacaoId");
            }
            
            jpql.append(" ORDER BY fs.funcionario.user.nome, fs.servico.nome, fs.localizacao.nome");
            
            TypedQuery<FuncionarioServico> query = em.createQuery(jpql.toString(), FuncionarioServico.class);
            
            if (funcionarioId != null) {
                query.setParameter("funcionarioId", funcionarioId);
            }
            if (servicoId != null) {
                query.setParameter("servicoId", servicoId);
            }
            if (localizacaoId != null) {
                query.setParameter("localizacaoId", localizacaoId);
            }
            
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar associações com filtros", e);
            throw new RuntimeException("Erro ao buscar associações", e);
        }
    }
    
    // ========== BUSCA DE FUNCIONÁRIOS COM RELACIONAMENTOS ==========
    @Override
    public List<Funcionario> getAllFuncionariosWithCargo() {
        try {
            TypedQuery<Funcionario> query = em.createQuery(
                "SELECT DISTINCT f FROM Funcionario f " +
                "JOIN FETCH f.user " +
                "LEFT JOIN FETCH f.cargo " +
                "ORDER BY f.user.nome", 
                Funcionario.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar funcionários com cargo", e);
            throw new RuntimeException("Erro ao buscar funcionários", e);
        }
    }
}
