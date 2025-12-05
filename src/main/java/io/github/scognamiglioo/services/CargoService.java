package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Cargo;
import io.github.scognamiglioo.entities.Funcionario;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementação EJB dos serviços para entidade Cargo.
 * Fornece operações CRUD e lógica de negócio para gerenciamento de cargos.
 */
@Stateless
public class CargoService implements CargoServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(CargoService.class.getName());

    @PersistenceContext(unitName = "SecureAppPU")
    private EntityManager em;

    @Override
    public Cargo createCargo(String nome) throws IllegalArgumentException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do cargo é obrigatório");
        }

        String nomeNormalizado = nome.trim();

        if (nomeNormalizado.length() < 2 || nomeNormalizado.length() > 100) {
            throw new IllegalArgumentException("Nome do cargo deve ter entre 2 e 100 caracteres");
        }

        // Verifica se já existe cargo com esse nome
        Cargo cargoExistente = findCargoByNome(nomeNormalizado);
        if (cargoExistente != null) {
            throw new IllegalArgumentException("Já existe um cargo com esse nome");
        }

        try {
            Cargo cargo = new Cargo(nomeNormalizado);
            em.persist(cargo);
            em.flush();
            
            LOGGER.log(Level.INFO, "Cargo criado com sucesso: {0}", cargo.getNome());
            return cargo;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar cargo", e);
            throw new RuntimeException("Erro ao salvar cargo no banco de dados", e);
        }
    }

    @Override
    public Cargo updateCargo(Cargo cargo) throws IllegalArgumentException {
        if (cargo == null || cargo.getId() == null) {
            throw new IllegalArgumentException("Cargo ou ID não pode ser nulo");
        }

        if (cargo.getNome() == null || cargo.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do cargo é obrigatório");
        }

        String nomeNormalizado = cargo.getNome().trim();

        if (nomeNormalizado.length() < 2 || nomeNormalizado.length() > 100) {
            throw new IllegalArgumentException("Nome do cargo deve ter entre 2 e 100 caracteres");
        }

        // Verifica se existe outro cargo com o mesmo nome
        Cargo cargoComMesmoNome = findCargoByNome(nomeNormalizado);
        if (cargoComMesmoNome != null && !cargoComMesmoNome.getId().equals(cargo.getId())) {
            throw new IllegalArgumentException("Já existe um cargo com esse nome");
        }

        try {
            Cargo cargoMerged = em.merge(cargo);
            em.flush();
            
            LOGGER.log(Level.INFO, "Cargo atualizado com sucesso: {0}", cargoMerged.getNome());
            return cargoMerged;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar cargo", e);
            throw new RuntimeException("Erro ao atualizar cargo no banco de dados", e);
        }
    }

    @Override
    public Cargo findCargoById(Long id) {
        if (id == null) {
            return null;
        }
        
        try {
            return em.find(Cargo.class, id);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar cargo por ID: " + id, e);
            return null;
        }
    }

    @Override
    public Cargo findCargoByNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }

        try {
            TypedQuery<Cargo> query = em.createNamedQuery("Cargo.findByNome", Cargo.class);
            query.setParameter("nome", nome.trim());
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar cargo por nome: " + nome, e);
            return null;
        }
    }

    @Override
    public List<Cargo> findCargosByNomePartial(String nome) {
        try {
            TypedQuery<Cargo> query = em.createNamedQuery("Cargo.findByNomePartial", Cargo.class);
            query.setParameter("nome", "%" + nome.trim() + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar cargos por nome parcial: " + nome, e);
            throw new RuntimeException("Erro ao executar busca de cargos", e);
        }
    }

    @Override
    public List<Cargo> getAllCargos() {
        try {
            TypedQuery<Cargo> query = em.createNamedQuery("Cargo.findAll", Cargo.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar todos os cargos", e);
            throw new RuntimeException("Erro ao buscar lista de cargos", e);
        }
    }

    @Override
    public void deleteCargo(Long id) throws IllegalStateException {
        if (id == null) {
            throw new IllegalArgumentException("ID do cargo não pode ser nulo");
        }

        try {
            // Verifica se o cargo está sendo usado
            if (isCargoInUse(id)) {
                throw new IllegalStateException("Não é possível excluir o cargo pois existem funcionários associados a ele");
            }

            Cargo cargo = findCargoById(id);
            if (cargo != null) {
                em.remove(cargo);
                em.flush();
                
                LOGGER.log(Level.INFO, "Cargo excluído com sucesso: {0}", cargo.getNome());
            } else {
                throw new IllegalArgumentException("Cargo não encontrado com ID: " + id);
            }
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Re-throw business exceptions
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir cargo com ID: " + id, e);
            throw new RuntimeException("Erro ao excluir cargo do banco de dados", e);
        }
    }

    @Override
    public boolean isCargoInUse(Long cargoId) {
        return countFuncionariosByCargo(cargoId) > 0;
    }

    @Override
    public long countFuncionariosByCargo(Long cargoId) {
        if (cargoId == null) {
            return 0;
        }

        try {
            TypedQuery<Long> query = em.createNamedQuery("Cargo.countFuncionarios", Long.class);
            query.setParameter("cargoId", cargoId);
            Long count = query.getSingleResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao contar funcionários do cargo ID: " + cargoId, e);
            return 0;
        }
    }

    @Override
    public List<Funcionario> findFuncionariosByCargo(Long cargoId) {
        if (cargoId == null || cargoId <= 0) {
            return new ArrayList<>();
        }
        try {
            // JPQL com cast para garantir tipo correto
            return em.createQuery(
                "SELECT f FROM Funcionario f WHERE CAST(f.cargo.id AS java.lang.Long) = :cargoId", 
                Funcionario.class
            )
            .setParameter("cargoId", cargoId)
            .getResultList();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar funcionários do cargo " + cargoId + ": " + ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }
}