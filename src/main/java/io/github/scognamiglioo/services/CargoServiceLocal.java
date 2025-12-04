package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Cargo;
import io.github.scognamiglioo.entities.Funcionario;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Interface local para serviços relacionados à entidade Cargo.
 * Define os contratos de negócio para operações CRUD e consultas de cargo.
 */
@Local
public interface CargoServiceLocal {

    /**
     * Cria um novo cargo
     * @param nome Nome do cargo
     * @return Cargo criado
     * @throws IllegalArgumentException se o nome for inválido ou já existir
     */
    Cargo createCargo(String nome) throws IllegalArgumentException;

    /**
     * Atualiza um cargo existente
     * @param cargo Cargo a ser atualizado
     * @return Cargo atualizado
     * @throws IllegalArgumentException se os dados forem inválidos
     */
    Cargo updateCargo(Cargo cargo) throws IllegalArgumentException;

    /**
     * Busca um cargo por ID
     * @param id ID do cargo
     * @return Cargo encontrado ou null
     */
    Cargo findCargoById(Long id);

    /**
     * Busca um cargo pelo nome exato
     * @param nome Nome do cargo
     * @return Cargo encontrado ou null
     */
    Cargo findCargoByNome(String nome);

    /**
     * Busca cargos que contenham o nome informado (busca parcial)
     * @param nome Parte do nome do cargo
     * @return Lista de cargos encontrados
     */
    List<Cargo> findCargosByNomePartial(String nome);

    /**
     * Retorna todos os cargos ordenados por nome
     * @return Lista de todos os cargos
     */
    List<Cargo> getAllCargos();

    /**
     * Remove um cargo por ID
     * @param id ID do cargo a ser removido
     * @throws IllegalStateException se o cargo estiver associado a funcionários
     */
    void deleteCargo(Long id) throws IllegalStateException;

    /**
     * Verifica se um cargo está sendo usado por funcionários
     * @param cargoId ID do cargo
     * @return true se o cargo estiver em uso
     */
    boolean isCargoInUse(Long cargoId);

    /**
     * Conta quantos funcionários estão associados a um cargo
     * @param cargoId ID do cargo
     * @return Número de funcionários associados
     */
    long countFuncionariosByCargo(Long cargoId);

    /**
     * Busca todos os funcionários associados a um cargo
     * @param cargoId ID do cargo
     * @return Lista de funcionários do cargo
     */
    List<Funcionario> findFuncionariosByCargo(Long cargoId);
}