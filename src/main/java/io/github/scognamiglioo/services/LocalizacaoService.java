package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Localizacao;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Implementação do serviço de Localizacao
 */
@Stateless
public class LocalizacaoService implements LocalizacaoServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(LocalizacaoService.class.getName());

    @PersistenceContext
    private EntityManager em;

    // ========== CRUD BÁSICO ==========

    @Override
    public Localizacao createLocalizacao(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do local é obrigatório");
        }

        String nomeLimpo = nome.trim();
        
        if (localizacaoExistsByNome(nomeLimpo)) {
            throw new IllegalArgumentException("Já existe um local com este nome: " + nomeLimpo);
        }

        Localizacao local = new Localizacao(nomeLimpo);
        em.persist(local);
        LOGGER.log(Level.INFO, "Local criado: {0}", local);
        return local;
    }

    @Override
    public Localizacao createLocalizacao(String nome, String descricao) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do local é obrigatório");
        }

        String nomeLimpo = nome.trim();
        
        if (localizacaoExistsByNome(nomeLimpo)) {
            throw new IllegalArgumentException("Já existe um local com este nome: " + nomeLimpo);
        }

        Localizacao local = new Localizacao(nomeLimpo, descricao);
        em.persist(local);
        LOGGER.log(Level.INFO, "Local criado: {0}", local);
        return local;
    }

    @Override
    public Localizacao createLocalizacao(Localizacao local) {
        if (local == null) {
            throw new IllegalArgumentException("Local não pode ser nulo");
        }

        if (local.getNome() == null || local.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do local é obrigatório");
        }

        String nomeLimpo = local.getNome().trim();
        local.setNome(nomeLimpo);

        if (localizacaoExistsByNome(nomeLimpo)) {
            throw new IllegalArgumentException("Já existe um local com este nome: " + nomeLimpo);
        }

        em.persist(local);
        LOGGER.log(Level.INFO, "Local criado: {0}", local);
        return local;
    }

    @Override
    public List<Localizacao> getAllLocalizacoes() {
        try {
            TypedQuery<Localizacao> query = em.createNamedQuery("Localizacao.findAll", Localizacao.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar todos os locais", e);
            throw new RuntimeException("Erro ao buscar locais", e);
        }
    }

    @Override
    public List<Localizacao> findLocalizacoesByNomePartial(String nome) {
        try {
            TypedQuery<Localizacao> query = em.createNamedQuery("Localizacao.findByNomePartial", Localizacao.class);
            query.setParameter("nome", "%" + nome + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar locais por nome parcial: " + nome, e);
            throw new RuntimeException("Erro na busca", e);
        }
    }

    @Override
    public Localizacao findLocalizacaoById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return em.find(Localizacao.class, id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar local por ID: " + id, e);
            throw new RuntimeException("Erro ao buscar local", e);
        }
    }

    @Override
    public Localizacao findLocalizacaoByNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }

        try {
            TypedQuery<Localizacao> query = em.createNamedQuery("Localizacao.findByNome", Localizacao.class);
            query.setParameter("nome", nome.trim());
            List<Localizacao> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar local por nome: " + nome, e);
            throw new RuntimeException("Erro ao buscar local", e);
        }
    }

    @Override
    public Localizacao updateLocalizacao(Localizacao local) {
        if (local == null || local.getId() == null) {
            throw new IllegalArgumentException("Local e ID são obrigatórios para atualização");
        }

        if (local.getNome() == null || local.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do local é obrigatório");
        }

        String nomeLimpo = local.getNome().trim();
        local.setNome(nomeLimpo);

        if (localizacaoExistsByNomeAndDifferentId(nomeLimpo, local.getId())) {
            throw new IllegalArgumentException("Já existe outro local com este nome: " + nomeLimpo);
        }

        try {
            Localizacao localAtualizado = em.merge(local);
            LOGGER.log(Level.INFO, "Local atualizado: {0}", localAtualizado);
            return localAtualizado;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar local: " + local, e);
            throw new RuntimeException("Erro ao atualizar local", e);
        }
    }

    @Override
    public void deleteLocalizacao(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório para exclusão");
        }

        // Verificar se há funcionário-serviços associados a este local
        long count = countFuncionarioServicoByLocalizacao(id);
        if (count > 0) {
            throw new IllegalStateException("Não é possível excluir este local pois existem " + count + 
                                          " associação(ões) funcionário-serviço vinculada(s) a ele.");
        }

        try {
            Localizacao local = em.find(Localizacao.class, id);
            if (local != null) {
                em.remove(local);
                em.flush();
                LOGGER.log(Level.INFO, "Local excluído: ID={0}", id);
            } else {
                throw new IllegalArgumentException("Local não encontrado com ID: " + id);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir local com ID: " + id, e);
            throw new RuntimeException("Erro ao excluir local", e);
        }
    }

    // ========== VALIDAÇÕES E CONTADORES ==========

    @Override
    public boolean localizacaoExistsByNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }

        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(l) FROM Localizacao l WHERE LOWER(l.nome) = LOWER(:nome)", Long.class);
            query.setParameter("nome", nome.trim());
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao verificar existência de local por nome: " + nome, e);
            return false;
        }
    }

    @Override
    public boolean localizacaoExistsByNomeAndDifferentId(String nome, Long id) {
        if (nome == null || nome.trim().isEmpty() || id == null) {
            return false;
        }

        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(l) FROM Localizacao l WHERE LOWER(l.nome) = LOWER(:nome) AND l.id != :id", Long.class);
            query.setParameter("nome", nome.trim());
            query.setParameter("id", id);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao verificar existência de local por nome e ID diferente", e);
            return false;
        }
    }

    @Override
    public long countFuncionarioServicoByLocalizacao(Long localId) {
        if (localId == null) {
            return 0;
        }

        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(fs) FROM FuncionarioServico fs WHERE fs.localizacao.id = :localizacaoId", Long.class);
            query.setParameter("localizacaoId", localId);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao contar funcionário-serviços por localização: " + localId, e);
            return 0;
        }
    }
}