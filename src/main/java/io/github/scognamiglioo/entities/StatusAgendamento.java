package io.github.scognamiglioo.entities;

/**
 * Enum que representa os possíveis status de um agendamento
 */
public enum StatusAgendamento {
    AGENDADO("Agendado"),
    CONFIRMADO("Confirmado"),
    EM_ATENDIMENTO("Em Atendimento"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado"),
    NAO_COMPARECEU("Não Compareceu");

    private final String descricao;

    StatusAgendamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}

