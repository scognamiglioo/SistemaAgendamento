package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.*;
import io.github.scognamiglioo.services.AgendamentoServiceLocal;
import io.github.scognamiglioo.services.DataServiceLocal;
import io.github.scognamiglioo.services.ServicoServiceLocal;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller para gerenciamento de agendamentos com fluxo correto:
 * 1. Selecionar Serviço
 * 2. Selecionar Funcionário que presta esse serviço
 * 3. Selecionar Data
 * 4. Selecionar Horário disponível (funcionário livre naquela data/hora)
 */
@Named
@ViewScoped
public class AgendamentoController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(AgendamentoController.class.getName());

    @EJB
    private AgendamentoServiceLocal agendamentoService;

    @EJB
    private ServicoServiceLocal servicoService;

    @EJB
    private DataServiceLocal dataService;

    // Seleções do usuário (fluxo do agendamento)
    private Long servicoSelecionadoId;
    private Long funcionarioSelecionadoId;
    private Date dataSelecionada;
    private String horarioSelecionado;
    private String observacoes;

    // Listas dinâmicas que mudam conforme as seleções
    private List<Servico> servicosDisponiveis;
    private List<Funcionario> funcionariosDisponiveis;
    private List<String> horariosDisponiveis;

    // Lista de agendamentos do usuário
    private List<Agendamento> meusAgendamentos;

    // Data mínima para o calendário (hoje)
    private Date dataMinima;

    // Agendamento selecionado para visualização de detalhes
    private Agendamento agendamentoSelecionado;

    // Filtros de pesquisa
    private String filtroCodigo;
    private Date filtroData;
    private String filtroStatus;
    private List<Agendamento> agendamentosFiltrados;

    @PostConstruct
    public void init() {
        loadServicosDisponiveis();
        loadMeusAgendamentos();
        dataMinima = new Date();
        funcionariosDisponiveis = new ArrayList<>();
        horariosDisponiveis = new ArrayList<>();
    }

    /**
     * Carrega todos os serviços disponíveis para seleção
     */
    public void loadServicosDisponiveis() {
        try {
            servicosDisponiveis = servicoService.getAllServicos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar serviços", e);
            addErrorMessage("Erro ao carregar lista de serviços");
        }
    }

    /**
     * Quando o usuário seleciona um serviço, carrega os funcionários que prestam esse serviço
     */
    public void onServicoChange() {
        try {
            LOGGER.log(Level.INFO, "onServicoChange chamado. servicoSelecionadoId: {0}", servicoSelecionadoId);

            funcionariosDisponiveis = new ArrayList<>();
            horariosDisponiveis = new ArrayList<>();
            funcionarioSelecionadoId = null;
            dataSelecionada = null;
            horarioSelecionado = null;

            if (servicoSelecionadoId != null) {
                // Busca funcionários que prestam este serviço
                funcionariosDisponiveis = agendamentoService.findFuncionariosDisponiveisParaServico(servicoSelecionadoId);

                LOGGER.log(Level.INFO, "Funcionários encontrados: {0}", funcionariosDisponiveis.size());

                if (funcionariosDisponiveis.isEmpty()) {
                    addWarnMessage("Nenhum funcionário disponível para este serviço no momento");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar funcionários do serviço", e);
            addErrorMessage("Erro ao carregar funcionários: " + e.getMessage());
        }
    }

    /**
     * Quando o usuário seleciona um funcionário, prepara para seleção de data
     */
    public void onFuncionarioChange() {
        horariosDisponiveis = new ArrayList<>();
        horarioSelecionado = null;

        if (funcionarioSelecionadoId != null && dataSelecionada != null) {
            carregarHorariosDisponiveis();
        }
    }

    /**
     * Quando o usuário seleciona uma data, carrega os horários disponíveis
     */
    public void onDataChange() {
        horariosDisponiveis = new ArrayList<>();
        horarioSelecionado = null;

        if (funcionarioSelecionadoId != null && dataSelecionada != null) {
            carregarHorariosDisponiveis();
        }
    }

    /**
     * Carrega horários disponíveis para o funcionário selecionado na data selecionada
     */
    private void carregarHorariosDisponiveis() {
        try {
            if (funcionarioSelecionadoId == null || dataSelecionada == null) {
                return;
            }

            LocalDate data = new java.sql.Date(dataSelecionada.getTime()).toLocalDate();

            // Todos os horários possíveis (8h às 18h, intervalos de 30min)
            List<String> todosHorarios = agendamentoService.getHorariosDisponiveis();
            horariosDisponiveis = new ArrayList<>();

            // Filtra apenas horários em que o funcionário está disponível
            for (String horarioStr : todosHorarios) {
                LocalTime hora = LocalTime.parse(horarioStr, DateTimeFormatter.ofPattern("HH:mm"));

                if (agendamentoService.isHorarioDisponivel(data, hora, funcionarioSelecionadoId)) {
                    horariosDisponiveis.add(horarioStr);
                }
            }

            if (horariosDisponiveis.isEmpty()) {
                addWarnMessage("Nenhum horário disponível para esta data. Tente outra data.");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar horários disponíveis", e);
            addErrorMessage("Erro ao carregar horários disponíveis");
        }
    }

    /**
     * Carrega os agendamentos do usuário logado
     */
    public void loadMeusAgendamentos() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String loggedUsername = context.getExternalContext().getRemoteUser();

            if (loggedUsername != null) {
                meusAgendamentos = agendamentoService.findAgendamentosByUsername(loggedUsername);
            } else {
                meusAgendamentos = new ArrayList<>();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar agendamentos do usuário", e);
            addErrorMessage("Erro ao carregar seus agendamentos");
            meusAgendamentos = new ArrayList<>();
        }
    }

    /**
     * Salva o novo agendamento
     */
    public String salvarAgendamento() {
        try {
            // Pega o usuário logado
            FacesContext context = FacesContext.getCurrentInstance();
            String loggedUsername = context.getExternalContext().getRemoteUser();

            if (loggedUsername == null || loggedUsername.trim().isEmpty()) {
                addErrorMessage("Usuário não está logado");
                return null;
            }

            // Validações
            if (servicoSelecionadoId == null) {
                addErrorMessage("Selecione um serviço");
                return null;
            }

            if (funcionarioSelecionadoId == null) {
                addErrorMessage("Selecione um funcionário");
                return null;
            }

            if (dataSelecionada == null) {
                addErrorMessage("Selecione uma data");
                return null;
            }

            if (horarioSelecionado == null || horarioSelecionado.trim().isEmpty()) {
                addErrorMessage("Selecione um horário");
                return null;
            }

            // Busca entidades
            User user = dataService.getUser(loggedUsername.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            Servico servico = servicoService.findServicoById(servicoSelecionadoId);
            if (servico == null) {
                throw new IllegalArgumentException("Serviço não encontrado");
            }

            Funcionario funcionario = dataService.findFuncionarioById(funcionarioSelecionadoId);
            if (funcionario == null) {
                throw new IllegalArgumentException("Funcionário não encontrado");
            }

            // Conversões
            LocalDate data = new java.sql.Date(dataSelecionada.getTime()).toLocalDate();
            LocalTime hora = LocalTime.parse(horarioSelecionado, DateTimeFormatter.ofPattern("HH:mm"));

            // Verifica disponibilidade final antes de criar
            if (!agendamentoService.isHorarioDisponivel(data, hora, funcionarioSelecionadoId)) {
                addErrorMessage("Este horário não está mais disponível. Por favor, selecione outro.");
                carregarHorariosDisponiveis();
                return null;
            }

            // Cria o agendamento COM funcionário já atribuído
            Agendamento novoAgendamento = agendamentoService.createAgendamento(user, servico, funcionario, data, hora);

            // Adiciona observações se houver
            if (observacoes != null && !observacoes.trim().isEmpty()) {
                novoAgendamento.setObservacoes(observacoes.trim());
                agendamentoService.updateAgendamento(novoAgendamento);
            }

            addSuccessMessage("Agendamento realizado com sucesso! Funcionário: " + funcionario.getNome());

            // Limpa o formulário e recarrega a lista
            resetForm();
            loadMeusAgendamentos();

            return null;

        } catch (IllegalArgumentException ex) {
            addErrorMessage(ex.getMessage());
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar agendamento", ex);
            addErrorMessage("Erro ao salvar agendamento: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Cancela um agendamento
     */
    public void cancelarAgendamento(Long agendamentoId) {
        try {
            agendamentoService.cancelarAgendamento(agendamentoId);
            addSuccessMessage("Agendamento cancelado com sucesso!");
            loadMeusAgendamentos();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao cancelar agendamento", ex);
            addErrorMessage("Erro ao cancelar agendamento: " + ex.getMessage());
        }
    }

    /**
     * Seleciona um agendamento para visualização de detalhes
     */
    public void selecionarAgendamento(Agendamento agendamento) {
        this.agendamentoSelecionado = agendamento;
        LOGGER.log(Level.INFO, "Agendamento selecionado para detalhes: {0}", agendamento != null ? agendamento.getId() : "null");
    }

    /**
     * Aplica os filtros de pesquisa
     */
    public void aplicarFiltros() {
        try {
            // Garante que os agendamentos estão carregados
            if (meusAgendamentos == null || meusAgendamentos.isEmpty()) {
                loadMeusAgendamentos();
            }

            LOGGER.log(Level.INFO, "Aplicando filtros. Total de agendamentos: {0}",
                meusAgendamentos != null ? meusAgendamentos.size() : 0);
            LOGGER.log(Level.INFO, "Filtros - Código: {0}, Data: {1}, Status: {2}",
                new Object[]{filtroCodigo, filtroData, filtroStatus});

            agendamentosFiltrados = new ArrayList<>(meusAgendamentos);

            // Filtro por código
            if (filtroCodigo != null && !filtroCodigo.trim().isEmpty()) {
                try {
                    Long codigo = Long.parseLong(filtroCodigo.trim());
                    LOGGER.log(Level.INFO, "Filtrando por código: {0}", codigo);
                    agendamentosFiltrados = agendamentosFiltrados.stream()
                            .filter(a -> a.getId().equals(codigo))
                            .toList();
                    LOGGER.log(Level.INFO, "Após filtro por código: {0} resultados", agendamentosFiltrados.size());
                } catch (NumberFormatException e) {
                    addWarnMessage("Código inválido. Digite apenas números.");
                    return;
                }
            }

            // Filtro por data
            if (filtroData != null) {
                LocalDate dataFiltro = new java.sql.Date(filtroData.getTime()).toLocalDate();
                LOGGER.log(Level.INFO, "Filtrando por data: {0}", dataFiltro);
                agendamentosFiltrados = agendamentosFiltrados.stream()
                        .filter(a -> a.getData() != null && a.getData().equals(dataFiltro))
                        .toList();
                LOGGER.log(Level.INFO, "Após filtro por data: {0} resultados", agendamentosFiltrados.size());
            }

            // Filtro por status
            if (filtroStatus != null && !filtroStatus.isEmpty()) {
                StatusAgendamento statusFiltro = StatusAgendamento.valueOf(filtroStatus);
                LOGGER.log(Level.INFO, "Filtrando por status: {0}", statusFiltro);
                agendamentosFiltrados = agendamentosFiltrados.stream()
                        .filter(a -> a.getStatus() == statusFiltro)
                        .toList();
                LOGGER.log(Level.INFO, "Após filtro por status: {0} resultados", agendamentosFiltrados.size());
            }

            if (agendamentosFiltrados.isEmpty()) {
                addWarnMessage("Nenhum agendamento encontrado com os filtros aplicados.");
            } else {
                addSuccessMessage(agendamentosFiltrados.size() + " agendamento(s) encontrado(s).");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao aplicar filtros", e);
            addErrorMessage("Erro ao filtrar agendamentos: " + e.getMessage());
            agendamentosFiltrados = meusAgendamentos != null ? new ArrayList<>(meusAgendamentos) : new ArrayList<>();
        }
    }

    /**
     * Limpa os filtros de pesquisa
     */
    public void limparFiltros() {
        filtroCodigo = null;
        filtroData = null;
        filtroStatus = "";
        agendamentosFiltrados = null;
        addSuccessMessage("Filtros limpos com sucesso!");
    }

    /**
     * Retorna a lista filtrada ou a lista completa se não houver filtros
     */
    public List<Agendamento> getAgendamentosFiltrados() {
        // Garante que os agendamentos estão carregados
        if (meusAgendamentos == null || meusAgendamentos.isEmpty()) {
            loadMeusAgendamentos();
        }

        if (agendamentosFiltrados != null) {
            return agendamentosFiltrados;
        }
        return meusAgendamentos != null ? meusAgendamentos : new ArrayList<>();
    }

    /**
     * Reseta o formulário
     */
    private void resetForm() {
        servicoSelecionadoId = null;
        funcionarioSelecionadoId = null;
        dataSelecionada = null;
        horarioSelecionado = null;
        observacoes = null;
        funcionariosDisponiveis = new ArrayList<>();
        horariosDisponiveis = new ArrayList<>();
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

    public Long getServicoSelecionadoId() {
        return servicoSelecionadoId;
    }

    public void setServicoSelecionadoId(Long servicoSelecionadoId) {
        this.servicoSelecionadoId = servicoSelecionadoId;
    }

    public Long getFuncionarioSelecionadoId() {
        return funcionarioSelecionadoId;
    }

    public void setFuncionarioSelecionadoId(Long funcionarioSelecionadoId) {
        this.funcionarioSelecionadoId = funcionarioSelecionadoId;
    }

    public Date getDataSelecionada() {
        return dataSelecionada;
    }

    public void setDataSelecionada(Date dataSelecionada) {
        this.dataSelecionada = dataSelecionada;
    }

    public String getHorarioSelecionado() {
        return horarioSelecionado;
    }

    public void setHorarioSelecionado(String horarioSelecionado) {
        this.horarioSelecionado = horarioSelecionado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<Servico> getServicosDisponiveis() {
        return servicosDisponiveis;
    }

    public void setServicosDisponiveis(List<Servico> servicosDisponiveis) {
        this.servicosDisponiveis = servicosDisponiveis;
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

    public List<Agendamento> getMeusAgendamentos() {
        return meusAgendamentos;
    }

    public void setMeusAgendamentos(List<Agendamento> meusAgendamentos) {
        this.meusAgendamentos = meusAgendamentos;
    }

    public Date getDataMinima() {
        return dataMinima;
    }

    public void setDataMinima(Date dataMinima) {
        this.dataMinima = dataMinima;
    }

    public Agendamento getAgendamentoSelecionado() {
        return agendamentoSelecionado;
    }

    public void setAgendamentoSelecionado(Agendamento agendamentoSelecionado) {
        this.agendamentoSelecionado = agendamentoSelecionado;
    }

    public String getFiltroCodigo() {
        return filtroCodigo;
    }

    public void setFiltroCodigo(String filtroCodigo) {
        this.filtroCodigo = filtroCodigo;
    }

    public Date getFiltroData() {
        return filtroData;
    }

    public void setFiltroData(Date filtroData) {
        this.filtroData = filtroData;
    }

    public String getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus;
    }

    public void setAgendamentosFiltrados(List<Agendamento> agendamentosFiltrados) {
        this.agendamentosFiltrados = agendamentosFiltrados;
    }
}
