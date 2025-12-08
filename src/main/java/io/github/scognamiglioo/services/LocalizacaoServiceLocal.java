package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Localizacao;
import jakarta.ejb.Local;
import java.util.List;

/**
 * Interface local para o serviço de Localizacao
 */
@Local
public interface LocalizacaoServiceLocal {

    /**
     * Criar uma nova localização
     */
    Localizacao createLocalizacao(String nome);
    Localizacao createLocalizacao(String nome, String descricao);
    Localizacao createLocalizacao(Localizacao localizacao);

    /**
     * Buscar localizações
     */
    List<Localizacao> getAllLocalizacoes();
    List<Localizacao> findLocalizacoesByNomePartial(String nome);
    Localizacao findLocalizacaoById(Long id);
    Localizacao findLocalizacaoByNome(String nome);

    /**
     * Atualizar localização
     */
    Localizacao updateLocalizacao(Localizacao localizacao);

    /**
     * Excluir localização
     */
    void deleteLocalizacao(Long id);

    /**
     * Validações e contadores
     */
    boolean localizacaoExistsByNome(String nome);
    boolean localizacaoExistsByNomeAndDifferentId(String nome, Long id);
    long countFuncionarioServicoByLocalizacao(Long localizacaoId);
}