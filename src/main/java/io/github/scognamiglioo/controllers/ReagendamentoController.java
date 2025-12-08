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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller responsável pelo reagendamento de atendimentos.
 * Permite que o usuário selecione um novo profissional, data e horário
 * mantendo o mesmo serviço do agendamento original.
 */
@Named
@ViewScoped
public class ReagendamentoController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(ReagendamentoController.class.getName());

    @EJB
    private AgendamentoServiceLocal agendamentoService;

    @EJB
    private DataServiceLocal dataService;

    // Agendamento original que será reagendado
    private Agendamento agendamentoOriginal;

    // Novos dados selecionados pelo usuário
    private Long novoFuncionarioId;
    private Date novaData;
    private String novoHorario;
    private String novasObservacoes;

    // Listas dinâmicas
    private List<Funcionario> funcionariosDisponiveis;
    private List<String> horariosDisponiveis;

    // Data mínima (hoje)
    private Date dataMinima;

    @PostConstruct
    public void init() {
        try {
            dataMinima = new Date();
            funcionariosDisponiveis = new ArrayList<>();
            horariosDisponiveis = new ArrayList<>();

            // Recupera o ID do agendamento dos parâmetros da URL
            FacesContext context = FacesContext.getCurrentInstance();
            Map<String, String> params = context.getExternalContext().getRequestParameterMap();
            String agendamentoIdStr = params.get("id");

            if (agendamentoIdStr != null && !agendamentoIdStr.trim().isEmpty()) {
                Long agendamentoId = Long.parseLong(agendamentoIdStr);
                loadAgendamentoOriginal(agendamentoId);
            } else {
                addErrorMessage("Agendamento não especificado. Retorne à página anterior.");
                LOGGER.log(Level.WARNING, "Tentativa de acesso sem ID de agendamento");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao inicializar reagendamento", e);
            addErrorMessage("Erro ao carregar dados do agendamento: " + e.getMessage());
        }
    }

    /**
     * Carrega o agendamento original que será reagendado
     */
    private void loadAgendamentoOriginal(Long agendamentoId) {
        try {
            agendamentoOriginal = agendamentoService.findAgendamentoById(agendamentoId);

            if (agendamentoOriginal == null) {
                addErrorMessage("Agendamento não encontrado.");
                LOGGER.log(Level.WARNING, "Agendamento ID {0} não encontrado", agendamentoId);
                return;
            }

            // Verifica se o agendamento pertence ao usuário logado
            FacesContext context = FacesContext.getCurrentInstance();
            String loggedUsername = context.getExternalContext().getRemoteUser();

            if (agendamentoOriginal.getUser() == null || !agendamentoOriginal.getUser().getUsername().equals(loggedUsername)) {
                addErrorMessage("Você não tem permissão para reagendar este agendamento.");
                LOGGER.log(Level.WARNING, "Usuário {0} tentou acessar agendamento de outro usuário", loggedUsername);
                agendamentoOriginal = null;
                return;
            }

            // Verifica se o agendamento pode ser reagendado
            if (agendamentoOriginal.getStatus().name().equals("CANCELADO")) {
                addErrorMessage("Não é possível reagendar um agendamento cancelado.");
                agendamentoOriginal = null;
                return;
            }

            if (agendamentoOriginal.getStatus().name().equals("CONCLUIDO")) {
                addErrorMessage("Não é possível reagendar um agendamento já concluído.");
                agendamentoOriginal = null;
                return;
            }

            // Carrega os funcionários que prestam o serviço do agendamento original
            loadFuncionariosDisponiveis();

            LOGGER.log(Level.INFO, "Agendamento original carregado: ID={0}, Serviço={1}",
                    new Object[]{agendamentoOriginal.getId(), agendamentoOriginal.getNomeServico()});

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar agendamento original", e);
            addErrorMessage("Erro ao carregar agendamento: " + e.getMessage());
        }
    }

    /**
     * Carrega os funcionários que prestam o serviço do agendamento original
     */
    private void loadFuncionariosDisponiveis() {
        try {
            if (agendamentoOriginal != null && agendamentoOriginal.getServico() != null) {
                Long servicoId = agendamentoOriginal.getServico().getId();
                funcionariosDisponiveis = agendamentoService.findFuncionariosDisponiveisParaServico(servicoId);

                LOGGER.log(Level.INFO, "Funcionários carregados para o serviço: {0}", funcionariosDisponiveis.size());

                if (funcionariosDisponiveis.isEmpty()) {
                    addWarnMessage("Nenhum funcionário disponível para este serviço no momento.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar funcionários disponíveis", e);
            addErrorMessage("Erro ao carregar lista de profissionais: " + e.getMessage());
            funcionariosDisponiveis = new ArrayList<>();
        }
    }

    /**
     * Executado quando o usuário seleciona um novo funcionário
     */
    public void onNovoFuncionarioChange() {
        horariosDisponiveis = new ArrayList<>();
        novoHorario = null;

        if (novoFuncionarioId != null && novaData != null) {
            carregarHorariosDisponiveis();
        }
    }

    /**
     * Executado quando o usuário seleciona uma nova data
     */
    public void onNovaDataChange() {
        horariosDisponiveis = new ArrayList<>();
        novoHorario = null;

        if (novoFuncionarioId != null && novaData != null) {
            carregarHorariosDisponiveis();
        }
    }

    /**
     * Carrega os horários disponíveis para o funcionário selecionado na data selecionada
     */
    private void carregarHorariosDisponiveis() {
        try {
            if (novoFuncionarioId == null || novaData == null) {
                return;
            }

            LocalDate data = new java.sql.Date(novaData.getTime()).toLocalDate();

            // Todos os horários possíveis (8h às 18h, intervalos de 30min)
            List<String> todosHorarios = agendamentoService.getHorariosDisponiveis();
            horariosDisponiveis = new ArrayList<>();

            // Filtra apenas horários em que o funcionário está disponível
            for (String horarioStr : todosHorarios) {
                LocalTime hora = LocalTime.parse(horarioStr, DateTimeFormatter.ofPattern("HH:mm"));

                // Se for o mesmo funcionário, mesma data e mesmo horário do agendamento original,
                // ainda assim devemos permitir (para não bloquear o próprio horário)
                boolean isMesmoAgendamento = false;
                if (agendamentoOriginal != null &&
                    agendamentoOriginal.getFuncionario() != null &&
                    agendamentoOriginal.getFuncionario().getId().equals(novoFuncionarioId) &&
                    agendamentoOriginal.getData().equals(data) &&
                    agendamentoOriginal.getHora().equals(hora)) {
                    isMesmoAgendamento = true;
                }

                if (isMesmoAgendamento || agendamentoService.isHorarioDisponivel(data, hora, novoFuncionarioId)) {
                    horariosDisponiveis.add(horarioStr);
                }
            }

            if (horariosDisponiveis.isEmpty()) {
                addWarnMessage("Nenhum horário disponível para esta data. Tente outra data.");
            } else {
                addSuccessMessage(horariosDisponiveis.size() + " horário(s) disponível(is) encontrado(s).");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar horários disponíveis", e);
            addErrorMessage("Erro ao carregar horários disponíveis: " + e.getMessage());
        }
    }

    /**
     * Confirma o reagendamento
     */
    public String confirmarReagendamento() {
        try {
            // Validações
            if (agendamentoOriginal == null) {
                addErrorMessage("Agendamento original não encontrado.");
                return null;
            }

            if (novoFuncionarioId == null) {
                addErrorMessage("Selecione um profissional.");
                return null;
            }

            if (novaData == null) {
                addErrorMessage("Selecione uma data.");
                return null;
            }

            if (novoHorario == null || novoHorario.trim().isEmpty()) {
                addErrorMessage("Selecione um horário.");
                return null;
            }

            // Busca o usuário logado
            FacesContext context = FacesContext.getCurrentInstance();
            String loggedUsername = context.getExternalContext().getRemoteUser();

            User user = dataService.getUser(loggedUsername.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            // Busca o funcionário selecionado
            Funcionario novoFuncionario = dataService.findFuncionarioById(novoFuncionarioId);
            if (novoFuncionario == null) {
                throw new IllegalArgumentException("Funcionário não encontrado");
            }

            // Verifica se o funcionário presta o serviço do agendamento original
            Servico servico = agendamentoOriginal.getServico();
            Long servicoId = servico.getId();

            // Usa o método do service para verificar dentro de uma transação (evita LazyInitializationException)
            if (!agendamentoService.funcionarioPrestServico(novoFuncionarioId, servicoId)) {
                addErrorMessage("O profissional selecionado não presta este tipo de serviço.");
                return null;
            }

            // Conversões de data e hora
            LocalDate data = new java.sql.Date(novaData.getTime()).toLocalDate();
            LocalTime hora = LocalTime.parse(novoHorario, DateTimeFormatter.ofPattern("HH:mm"));

            // Verifica disponibilidade final (exceto se for o mesmo horário do agendamento original)
            boolean isMesmoAgendamento =
                agendamentoOriginal.getFuncionario() != null &&
                agendamentoOriginal.getFuncionario().getId().equals(novoFuncionarioId) &&
                agendamentoOriginal.getData().equals(data) &&
                agendamentoOriginal.getHora().equals(hora);

            if (!isMesmoAgendamento && !agendamentoService.isHorarioDisponivel(data, hora, novoFuncionarioId)) {
                addErrorMessage("Este horário não está mais disponível. Por favor, selecione outro.");
                carregarHorariosDisponiveis();
                return null;
            }

            // Cancela o agendamento original apenas se estiver como agendado
            if(agendamentoOriginal.getStatus() == StatusAgendamento.AGENDADO) {
                agendamentoService.cancelarAgendamento(agendamentoOriginal.getId());
            }

            // Cria o novo agendamento
            Agendamento novoAgendamento = agendamentoService.createAgendamento(user, servico, novoFuncionario, data, hora);

            // Adiciona observações do reagendamento
            String observacoesCompletas = "REAGENDAMENTO - Agendamento #" + agendamentoOriginal.getId() +
                                         " (Data: " + agendamentoOriginal.getDataFormatada() +
                                         " às " + agendamentoOriginal.getHoraFormatada() + ")";

            if (novasObservacoes != null && !novasObservacoes.trim().isEmpty()) {
                observacoesCompletas += "\n" + novasObservacoes.trim();
            }

            // Mantém as observações originais se houver
            if (agendamentoOriginal.getObservacoes() != null && !agendamentoOriginal.getObservacoes().trim().isEmpty()) {
                observacoesCompletas += "\nObservações originais: " + agendamentoOriginal.getObservacoes();
            }

            novoAgendamento.setObservacoes(observacoesCompletas);
            agendamentoService.updateAgendamento(novoAgendamento);

            LOGGER.log(Level.INFO, "Reagendamento realizado com sucesso. Agendamento original: {0}, Novo agendamento: {1}",
                    new Object[]{agendamentoOriginal.getId(), novoAgendamento.getId()});

            addSuccessMessage("Reagendamento realizado com sucesso! Novo agendamento #" + novoAgendamento.getId() +
                            " criado para " + novoAgendamento.getDataFormatada() + " às " + novoAgendamento.getHoraFormatada());

            // Redireciona para a página de meus agendamentos após 2 segundos
            return "meus-agendamentos?faces-redirect=true";

        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Erro de validação no reagendamento", ex);
            addErrorMessage(ex.getMessage());
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao confirmar reagendamento", ex);
            addErrorMessage("Erro ao reagendar: " + ex.getMessage());
            return null;
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

    private void addWarnMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", message));
    }

    // Getters e Setters

    public Agendamento getAgendamentoOriginal() {
        return agendamentoOriginal;
    }

    public void setAgendamentoOriginal(Agendamento agendamentoOriginal) {
        this.agendamentoOriginal = agendamentoOriginal;
    }

    public Long getNovoFuncionarioId() {
        return novoFuncionarioId;
    }

    public void setNovoFuncionarioId(Long novoFuncionarioId) {
        this.novoFuncionarioId = novoFuncionarioId;
    }

    public Date getNovaData() {
        return novaData;
    }

    public void setNovaData(Date novaData) {
        this.novaData = novaData;
    }

    public String getNovoHorario() {
        return novoHorario;
    }

    public void setNovoHorario(String novoHorario) {
        this.novoHorario = novoHorario;
    }

    public String getNovasObservacoes() {
        return novasObservacoes;
    }

    public void setNovasObservacoes(String novasObservacoes) {
        this.novasObservacoes = novasObservacoes;
    }

    public List<Funcionario> getFuncionariosDisponiveis() {
        return funcionariosDisponiveis;
    }

    public void setFuncionariosDisponiveis(List<Funcionario> funcionariosDisponiveis) {
        this.funcionariosDisponiveis = funcionariosDisponiveis;
    }

    public List<String> getHorariosDisponiveis() {
        return horariosDisponiveis;
    }

    public void setHorariosDisponiveis(List<String> horariosDisponiveis) {
        this.horariosDisponiveis = horariosDisponiveis;
    }

    public Date getDataMinima() {
        return dataMinima;
    }

    public void setDataMinima(Date dataMinima) {
        this.dataMinima = dataMinima;
    }
}

