package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Agendamento;
import jakarta.ejb.Local;
import jakarta.mail.MessagingException;

@Local
public interface AgendamentoMailServiceLocal {
    
    void sendConfirmacaoAgendamento(Agendamento agendamento) throws MessagingException;
    
    void sendCancelamentoAgendamento(Agendamento agendamento) throws MessagingException;
    
    void sendReagendamento(Agendamento agendamentoOriginal, Agendamento novoAgendamento) throws MessagingException;
}
