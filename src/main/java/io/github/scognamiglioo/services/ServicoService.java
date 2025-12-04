package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Servico;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
@LocalBean
public class ServicoService implements ServicoServiceLocal {

    @PersistenceContext(unitName = "SecureAppPU")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Servico createServico(String nome, Float valor) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do serviço é obrigatório");
        }
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("Valor do serviço deve ser maior que zero");
        }
        if (servicoExists(nome.trim())) {
            throw new IllegalArgumentException("Já existe um serviço com este nome");
        }

        Servico servico = new Servico(nome.trim(), valor);
        em.persist(servico);
        em.flush();
        return servico;
    }

    @Override
    public List<Servico> getAllServicos() {
        return em.createNamedQuery("Servico.findAll", Servico.class)
                .getResultList();
    }

    @Override
    public Servico findServicoById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Servico.class, id);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateServico(Servico servico) {
        if (servico == null) {
            throw new IllegalArgumentException("Serviço não pode ser nulo");
        }
        if (servico.getId() == null) {
            throw new IllegalArgumentException("ID do serviço é obrigatório para atualização");
        }
        if (servico.getNome() == null || servico.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do serviço é obrigatório");
        }
        if (servico.getValor() == null || servico.getValor() <= 0) {
            throw new IllegalArgumentException("Valor do serviço deve ser maior que zero");
        }

        // Verificar se já existe outro serviço com o mesmo nome
        List<Servico> servicosComMesmoNome = findServicosByNome(servico.getNome().trim());
        for (Servico s : servicosComMesmoNome) {
            if (!s.getId().equals(servico.getId())) {
                throw new IllegalArgumentException("Já existe outro serviço com este nome");
            }
        }

        servico.setNome(servico.getNome().trim());
        em.merge(servico);
        em.flush();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteServico(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do serviço é obrigatório");
        }
        
        Servico servico = findServicoById(id);
        if (servico != null) {
            em.remove(servico);
            em.flush();
        } else {
            throw new IllegalArgumentException("Serviço não encontrado");
        }
    }

    @Override
    public boolean servicoExists(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        
        Long count = em.createNamedQuery("Servico.countByNome", Long.class)
                .setParameter("nome", nome.trim())
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public List<Servico> findServicosByNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return getAllServicos();
        }
        
        return em.createNamedQuery("Servico.findByNome", Servico.class)
                .setParameter("nome", nome.trim())
                .getResultList();
    }

    @Override
    public List<Servico> findServicosByNomePartial(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return getAllServicos();
        }
        
        // Adiciona % para busca parcial (contém o texto)
        String nomePattern = "%" + nome.trim() + "%";
        
        return em.createNamedQuery("Servico.findByNomePartial", Servico.class)
                .setParameter("nome", nomePattern)
                .getResultList();
    }

    @Override
    public List<Funcionario> findFuncionariosByServico(Long servicoId) {
        if (servicoId == null) {
            throw new IllegalArgumentException("ID do serviço é obrigatório");
        }
        
        return em.createNamedQuery("Servico.findFuncionariosByServico", Funcionario.class)
                .setParameter("servicoId", servicoId)
                .getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void associarFuncionarioAoServico(Long funcionarioId, Long servicoId) {
        if (funcionarioId == null) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (servicoId == null) {
            throw new IllegalArgumentException("ID do serviço é obrigatório");
        }

        Funcionario funcionario = em.find(Funcionario.class, funcionarioId);
        Servico servico = em.find(Servico.class, servicoId);

        if (funcionario == null) {
            throw new IllegalArgumentException("Funcionário não encontrado");
        }
        if (servico == null) {
            throw new IllegalArgumentException("Serviço não encontrado");
        }

        funcionario.addServico(servico);
        em.merge(funcionario);
        em.flush();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void desassociarFuncionarioDoServico(Long funcionarioId, Long servicoId) {
        if (funcionarioId == null) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        if (servicoId == null) {
            throw new IllegalArgumentException("ID do serviço é obrigatório");
        }

        Funcionario funcionario = em.find(Funcionario.class, funcionarioId);
        Servico servico = em.find(Servico.class, servicoId);

        if (funcionario == null) {
            throw new IllegalArgumentException("Funcionário não encontrado");
        }
        if (servico == null) {
            throw new IllegalArgumentException("Serviço não encontrado");
        }

        funcionario.removeServico(servico);
        em.merge(funcionario);
        em.flush();
    }
}