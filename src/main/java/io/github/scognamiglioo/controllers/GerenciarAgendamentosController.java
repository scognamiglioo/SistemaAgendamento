package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.*;
import io.github.scognamiglioo.services.AgendamentoServiceLocal;
import io.github.scognamiglioo.services.DataServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller para gerenciar todos os agendamentos (área administrativa)
 */
@Named
@ViewScoped
public class GerenciarAgendamentosController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(GerenciarAgendamentosController.class.getName());

    @EJB
    private AgendamentoServiceLocal agendamentoService;

    @EJB
    private DataServiceLocal dataService;

    // Listas
    private List<Agendamento> agendamentos;
    private List<Funcionario> funcionariosDisponiveis;

    // Filtros
    private Date filtroDataInicio;
    private Date filtroDataFim;
    private String filtroStatus;
    private String filtroId;
    private String filtroUsuario;

    // Seleção/Edição
    private Agendamento agendamentoSelecionado;
    private Long funcionarioSelecionadoId;
    private String statusSelecionado;

    @PostConstruct
    public void init() {
        carregarAgendamentos();
        carregarFuncionarios();
    }

    /**
     * Carrega todos os agendamentos
     */
    public void carregarAgendamentos() {
        try {
            agendamentos = agendamentoService.getAllAgendamentos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar agendamentos", e);
            addErrorMessage("Erro ao carregar agendamentos");
            agendamentos = new ArrayList<>();
        }
    }

    /**
     * Carrega todos os funcionários ativos
     */
    private void carregarFuncionarios() {
        try {
            funcionariosDisponiveis = dataService.getAllFuncionarios();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar funcionários", e);
            funcionariosDisponiveis = new ArrayList<>();
        }
    }

    /**
     * Carrega apenas os funcionários que prestam o serviço do agendamento selecionado
     */
    private void carregarFuncionariosDoServico() {
        try {
            if (agendamentoSelecionado != null && agendamentoSelecionado.getServico() != null) {
                Long servicoId = agendamentoSelecionado.getServico().getId();
                LOGGER.log(Level.INFO, "Carregando funcionários para o serviço ID: {0}", servicoId);

                funcionariosDisponiveis = agendamentoService.findFuncionariosDisponiveisParaServico(servicoId);

                LOGGER.log(Level.INFO, "Funcionários encontrados: {0}", funcionariosDisponiveis.size());

                if (funcionariosDisponiveis.isEmpty()) {
                    LOGGER.log(Level.WARNING, "Nenhum funcionário disponível para o serviço ID: {0}", servicoId);
                    addWarnMessage("Nenhum funcionário disponível para este serviço");
                }
            } else {
                LOGGER.log(Level.WARNING, "Agendamento ou serviço não definido");
                funcionariosDisponiveis = new ArrayList<>();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar funcionários do serviço", e);
            addErrorMessage("Erro ao carregar funcionários: " + e.getMessage());
            funcionariosDisponiveis = new ArrayList<>();
        }
    }

    /**
     * Aplica filtros na listagem
     */
    public void aplicarFiltros() {
        try {
            // Carrega todos os agendamentos primeiro
            agendamentos = agendamentoService.getAllAgendamentos();

            List<Agendamento> resultado = new ArrayList<>(agendamentos);

            // Filtro por ID
            if (filtroId != null && !filtroId.trim().isEmpty()) {
                try {
                    Long id = Long.parseLong(filtroId.trim());
                    resultado = resultado.stream()
                            .filter(a -> a.getId().equals(id))
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    addErrorMessage("ID inválido. Digite apenas números.");
                    return;
                }
            }

            // Filtro por usuário (nome do paciente)
            if (filtroUsuario != null && !filtroUsuario.trim().isEmpty()) {
                String usuarioLower = filtroUsuario.trim().toLowerCase();
                resultado = resultado.stream()
                        .filter(a -> a.getUser() != null &&
                                     a.getUser().getNome() != null &&
                                     a.getUser().getNome().toLowerCase().contains(usuarioLower))
                        .collect(Collectors.toList());
            }

            // Filtro por data
            if (filtroDataInicio != null && filtroDataFim != null) {
                LocalDate inicio = new java.sql.Date(filtroDataInicio.getTime()).toLocalDate();
                LocalDate fim = new java.sql.Date(filtroDataFim.getTime()).toLocalDate();
                resultado = resultado.stream()
                        .filter(a -> a.getData() != null &&
                                     !a.getData().isBefore(inicio) &&
                                     !a.getData().isAfter(fim))
                        .collect(Collectors.toList());
            } else if (filtroDataInicio != null) {
                LocalDate inicio = new java.sql.Date(filtroDataInicio.getTime()).toLocalDate();
                resultado = resultado.stream()
                        .filter(a -> a.getData() != null && !a.getData().isBefore(inicio))
                        .collect(Collectors.toList());
            } else if (filtroDataFim != null) {
                LocalDate fim = new java.sql.Date(filtroDataFim.getTime()).toLocalDate();
                resultado = resultado.stream()
                        .filter(a -> a.getData() != null && !a.getData().isAfter(fim))
                        .collect(Collectors.toList());
            }

            // Filtro por status
            if (filtroStatus != null && !filtroStatus.isEmpty()) {
                StatusAgendamento status = StatusAgendamento.valueOf(filtroStatus);
                resultado = resultado.stream()
                        .filter(a -> a.getStatus() == status)
                        .collect(Collectors.toList());
            }

            agendamentos = resultado;

            if (agendamentos.isEmpty()) {
                addInfoMessage("Nenhum agendamento encontrado com os filtros aplicados");
            } else {
                addInfoMessage("Filtros aplicados: " + agendamentos.size() + " agendamento(s) encontrado(s)");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao aplicar filtros", e);
            addErrorMessage("Erro ao aplicar filtros: " + e.getMessage());
            carregarAgendamentos();
        }
    }

    /**
     * Prepara o agendamento para edição
     */
    public void editarAgendamento(Agendamento agendamento) {
        try {
            if (agendamento == null) {
                addErrorMessage("Agendamento inválido");
                return;
            }

            // Recarrega o agendamento do banco para garantir que todas as relações estejam inicializadas
            this.agendamentoSelecionado = agendamentoService.findAgendamentoById(agendamento.getId());

            if (this.agendamentoSelecionado == null) {
                addErrorMessage("Agendamento não encontrado");
                return;
            }

            // Inicializa os valores dos campos editáveis
            this.funcionarioSelecionadoId = this.agendamentoSelecionado.getFuncionario() != null ?
                    this.agendamentoSelecionado.getFuncionario().getId() : null;
            this.statusSelecionado = this.agendamentoSelecionado.getStatus().name();

            // Carrega apenas os funcionários que prestam o serviço deste agendamento
            carregarFuncionariosDoServico();

            LOGGER.log(Level.INFO, "Agendamento #{0} preparado para edição", agendamento.getId());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao preparar agendamento para edição", e);
            addErrorMessage("Erro ao carregar dados do agendamento");
            this.agendamentoSelecionado = null;
        }
    }

    /**
     * Salva as alterações do agendamento
     */
    public void salvarEdicao() {
        try {
            if (agendamentoSelecionado == null) {
                addErrorMessage("Nenhum agendamento selecionado");
                return;
            }

            Long agendamentoId = agendamentoSelecionado.getId();
            LOGGER.log(Level.INFO, "Salvando alterações do agendamento #{0}", agendamentoId);

            // Atribui funcionário APENAS se mudou
            if (funcionarioSelecionadoId != null) {
                Long funcionarioAtualId = agendamentoSelecionado.getFuncionario() != null ?
                    agendamentoSelecionado.getFuncionario().getId() : null;

                if (!funcionarioSelecionadoId.equals(funcionarioAtualId)) {
                    LOGGER.log(Level.INFO, "Atribuindo funcionário ID: {0} (anterior: {1})",
                        new Object[]{funcionarioSelecionadoId, funcionarioAtualId});
                    agendamentoService.atribuirFuncionario(agendamentoId, funcionarioSelecionadoId);
                } else {
                    LOGGER.log(Level.INFO, "Funcionário não mudou (ID: {0}), pulando atribuição", funcionarioSelecionadoId);
                }
            }

            // Altera status APENAS se mudou
            if (statusSelecionado != null && !statusSelecionado.trim().isEmpty()) {
                String statusAtual = agendamentoSelecionado.getStatus().name();

                if (!statusSelecionado.equals(statusAtual)) {
                    StatusAgendamento novoStatus = StatusAgendamento.valueOf(statusSelecionado);
                    LOGGER.log(Level.INFO, "Alterando status de {0} para {1}",
                        new Object[]{statusAtual, novoStatus});
                    agendamentoService.alterarStatus(agendamentoId, novoStatus);
                } else {
                    LOGGER.log(Level.INFO, "Status não mudou ({0}), pulando alteração", statusAtual);
                }
            }

            // Atualiza observações (sempre, pois podem ter mudado)
            Agendamento agendamentoAtualizado = agendamentoService.findAgendamentoById(agendamentoId);
            if (agendamentoAtualizado != null) {
                String obsAntiga = agendamentoAtualizado.getObservacoes();
                String obsNova = agendamentoSelecionado.getObservacoes();

                // Normaliza nulls para comparação
                obsAntiga = (obsAntiga == null || obsAntiga.trim().isEmpty()) ? null : obsAntiga.trim();
                obsNova = (obsNova == null || obsNova.trim().isEmpty()) ? null : obsNova.trim();

                if ((obsNova != null && !obsNova.equals(obsAntiga)) || (obsNova == null && obsAntiga != null)) {
                    agendamentoAtualizado.setObservacoes(obsNova);
                    agendamentoService.updateAgendamento(agendamentoAtualizado);
                    LOGGER.log(Level.INFO, "Observações atualizadas");
                } else {
                    LOGGER.log(Level.INFO, "Observações não mudaram");
                }
            }

            addSuccessMessage("Agendamento atualizado com sucesso!");

            // Recarrega a lista
            carregarAgendamentos();

            // Limpa a seleção
            agendamentoSelecionado = null;
            funcionarioSelecionadoId = null;
            statusSelecionado = null;

        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Erro de validação ao salvar agendamento", ex);
            addErrorMessage("Erro: " + ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar agendamento", ex);
            addErrorMessage("Erro ao salvar agendamento: " + ex.getMessage());
        }
    }

    /**
     * Cancela um agendamento
     */
    public void cancelarAgendamento(Long agendamentoId) {
        try {
            agendamentoService.cancelarAgendamento(agendamentoId);
            addSuccessMessage("Agendamento cancelado com sucesso!");
            carregarAgendamentos();
        } catch (IllegalArgumentException ex) {
            // Erro de validação (ex: menos de 24h de antecedência)
            LOGGER.log(Level.WARNING, "Validação de cancelamento falhou: {0}", ex.getMessage());
            addErrorMessage(ex.getMessage());
        } catch (Exception ex) {
            // Erro inesperado
            LOGGER.log(Level.SEVERE, "Erro ao cancelar agendamento", ex);
            addErrorMessage("Erro inesperado ao cancelar agendamento. Tente novamente.");
        }
    }

    // Métodos auxiliares de mensagens

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", message));
    }

    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", message));
    }

    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Informação", message));
    }

    private void addWarnMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", message));
    }

    // Métodos para estatísticas

    /**
     * Total de agendamentos
     */
    public int getTotalAgendamentos() {
        return agendamentos != null ? agendamentos.size() : 0;
    }

    /**
     * Agendamentos de hoje
     */
    public long getAgendamentosHoje() {
        if (agendamentos == null) return 0;
        LocalDate hoje = LocalDate.now();
        return agendamentos.stream()
                .filter(a -> a.getData() != null && a.getData().equals(hoje))
                .count();
    }

    /**
     * Agendamentos pendentes (Agendado + Confirmado)
     */
    public long getAgendamentosPendentes() {
        if (agendamentos == null) return 0;
        return agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.AGENDADO ||
                           a.getStatus() == StatusAgendamento.CONFIRMADO)
                .count();
    }

    /**
     * Agendamentos concluídos
     */
    public long getAgendamentosConcluidos() {
        if (agendamentos == null) return 0;
        return agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO)
                .count();
    }

    // Getters e Setters

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public List<Funcionario> getFuncionariosDisponiveis() {
        return funcionariosDisponiveis;
    }

    public void setFuncionariosDisponiveis(List<Funcionario> funcionariosDisponiveis) {
        this.funcionariosDisponiveis = funcionariosDisponiveis;
    }



    public Date getFiltroDataInicio() {
        return filtroDataInicio;
    }

    public void setFiltroDataInicio(Date filtroDataInicio) {
        this.filtroDataInicio = filtroDataInicio;
    }

    public Date getFiltroDataFim() {
        return filtroDataFim;
    }

    public void setFiltroDataFim(Date filtroDataFim) {
        this.filtroDataFim = filtroDataFim;
    }

    public String getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus;
    }

    public Agendamento getAgendamentoSelecionado() {
        return agendamentoSelecionado;
    }

    public void setAgendamentoSelecionado(Agendamento agendamentoSelecionado) {
        this.agendamentoSelecionado = agendamentoSelecionado;
    }

    public Long getFuncionarioSelecionadoId() {
        return funcionarioSelecionadoId;
    }

    public void setFuncionarioSelecionadoId(Long funcionarioSelecionadoId) {
        this.funcionarioSelecionadoId = funcionarioSelecionadoId;
    }

    public String getStatusSelecionado() {
        return statusSelecionado;
    }

    public void setStatusSelecionado(String statusSelecionado) {
        this.statusSelecionado = statusSelecionado;
    }

    public String getFiltroId() {
        return filtroId;
    }

    public void setFiltroId(String filtroId) {
        this.filtroId = filtroId;
    }

    public String getFiltroUsuario() {
        return filtroUsuario;
    }

    public void setFiltroUsuario(String filtroUsuario) {
        this.filtroUsuario = filtroUsuario;
    }

    /**
     * Busca o nome da localização onde o serviço do agendamento é prestado.
     * Usa o JOIN: Agendamento -> FuncionarioServico -> Localizacao
     *
     * @param agendamentoId ID do agendamento
     * @return Nome da localização ou "Não definido" se não encontrar
     */
    public String getNomeLocalizacao(Long agendamentoId) {
        try {
            Localizacao localizacao = agendamentoService.buscarLocalizacaoDoAgendamento(agendamentoId);
            return (localizacao != null) ? localizacao.getNome() : "Não definido";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao obter nome da localização para agendamento: " + agendamentoId, e);
            return "Erro ao carregar";
        }
    }
}
