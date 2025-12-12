/*
 * Controlador para gerenciar atendimentos presenciais (walk-in)
 * sem agendamento prévio
 */
package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.Agendamento;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Localizacao;
import io.github.scognamiglioo.entities.Servico;
import io.github.scognamiglioo.services.AgendamentoServiceLocal;
import io.github.scognamiglioo.services.DataServiceLocal;
import io.github.scognamiglioo.services.FuncionarioServicoServiceLocal;
import io.github.scognamiglioo.services.ServicoServiceLocal;
import io.github.scognamiglioo.websocket.ChamadaWebSocketUtil;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para gerenciar atendimentos walk-in (presenciais sem agendamento)
 * @author Sistema Agendamento
 */
@Named
@ViewScoped
public class WalkinController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(WalkinController.class.getName());

    @EJB
    private AgendamentoServiceLocal agendamentoService;

    @EJB
    private ServicoServiceLocal servicoService;

    @EJB
    private FuncionarioServicoServiceLocal funcionarioServicoService;

    @EJB
    private DataServiceLocal dataService;

    // ===== DADOS DO FORMULÁRIO =====
    private String nomeCliente;
    private String cpfCliente;
    private String telefoneCliente;
    private Long servicoSelecionadoId;
    private Long localizacaoSelecionadaId;
    private Long funcionarioSelecionadoId;
    private List<Servico> servicosList;
    private List<Localizacao> localizacoesList;
    private List<Funcionario> funcionariosList;

    @PostConstruct
    public void init() {
        try {
            servicosList = servicoService.getAllServicos();
            LOGGER.log(Level.INFO, "Serviços carregados: {0}", servicosList.size());
            
            if (servicosList == null || servicosList.isEmpty()) {
                addWarnMessage("Nenhum serviço disponível no momento.");
                servicosList = new ArrayList<>();
            }
            
            localizacoesList = new ArrayList<>();
            funcionariosList = new ArrayList<>();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar serviços", e);
            addErrorMessage("Erro ao carregar lista de serviços: " + e.getMessage());
            servicosList = new ArrayList<>();
            localizacoesList = new ArrayList<>();
            funcionariosList = new ArrayList<>();
        }
    }

    /**
     * Quando seleciona um serviço, carrega as localizações disponíveis
     */
    public void onServicoChange() {
        try {
            LOGGER.log(Level.INFO, "onServicoChange chamado. servicoSelecionadoId: {0}", servicoSelecionadoId);
            
            localizacoesList = new ArrayList<>();
            localizacaoSelecionadaId = null;
            funcionariosList = new ArrayList<>();
            funcionarioSelecionadoId = null;
            
            if (servicoSelecionadoId != null) {
                localizacoesList = funcionarioServicoService.findLocalizacoesByServico(servicoSelecionadoId);
                LOGGER.log(Level.INFO, "Localizações encontradas: {0}", localizacoesList.size());
                
                if (localizacoesList.isEmpty()) {
                    addWarnMessage("Nenhuma localização disponível para este serviço.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar localizações do serviço", e);
            addErrorMessage("Erro ao carregar localizações: " + e.getMessage());
            localizacoesList = new ArrayList<>();
        }
    }

    /**
     * Quando seleciona uma localização, carrega os funcionários disponíveis
     */
    public void onLocalizacaoChange() {
        try {
            LOGGER.log(Level.INFO, "onLocalizacaoChange chamado. localizacaoSelecionadaId: {0}", localizacaoSelecionadaId);
            
            funcionariosList = new ArrayList<>();
            funcionarioSelecionadoId = null;
            
            if (servicoSelecionadoId != null && localizacaoSelecionadaId != null) {
                funcionariosList = funcionarioServicoService.findFuncionariosByServicoAndLocalizacao(
                    servicoSelecionadoId, localizacaoSelecionadaId);
                LOGGER.log(Level.INFO, "Funcionários encontrados: {0}", funcionariosList.size());
                
                if (funcionariosList.isEmpty()) {
                    addWarnMessage("Nenhum funcionário disponível para este serviço e localização.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar funcionários", e);
            addErrorMessage("Erro ao carregar funcionários: " + e.getMessage());
            funcionariosList = new ArrayList<>();
        }
    }

    /**
     * Valida os dados do formulário
     */
    private boolean validarDados() {
        if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
            addErrorMessage("Nome do cliente é obrigatório.");
            return false;
        }

        if (cpfCliente == null || cpfCliente.trim().isEmpty()) {
            addErrorMessage("CPF é obrigatório.");
            return false;
        }

        // Valida formato do CPF (apenas números, 11 dígitos)
        if (!validarCPF(cpfCliente.trim())) {
            addErrorMessage("CPF inválido. Insira 11 dígitos numéricos.");
            return false;
        }

        if (telefoneCliente == null || telefoneCliente.trim().isEmpty()) {
            addErrorMessage("Telefone é obrigatório.");
            return false;
        }

        if (servicoSelecionadoId == null || servicoSelecionadoId <= 0) {
            addErrorMessage("Selecione um serviço.");
            return false;
        }

        if (localizacaoSelecionadaId == null || localizacaoSelecionadaId <= 0) {
            addErrorMessage("Selecione uma localização.");
            return false;
        }

        if (funcionarioSelecionadoId == null || funcionarioSelecionadoId <= 0) {
            addErrorMessage("Selecione um funcionário.");
            return false;
        }

        return true;
    }

    /**
     * Valida formato do CPF
     */
    private boolean validarCPF(String cpf) {
        // Remove caracteres não numéricos
        String cpfLimpo = cpf.replaceAll("\\D", "");
        
        // Deve ter 11 dígitos
        if (cpfLimpo.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (CPF inválido)
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            return false;
        }

        return true;
    }

    /**
     * Registra um novo atendimento walk-in
     */
    public void registrarWalkin() {
        if (!validarDados()) {
            return;
        }

        try {
            // Busca o serviço selecionado
            Servico servico = servicoService.findServicoById(servicoSelecionadoId);
            if (servico == null) {
                addErrorMessage("Serviço não encontrado.");
                return;
            }

            // Busca o funcionário selecionado
            Funcionario funcionario = dataService.findFuncionarioById(funcionarioSelecionadoId);
            if (funcionario == null) {
                addErrorMessage("Funcionário não encontrado.");
                return;
            }

            // Cria agendamento walk-in usando o novo método do serviço
            Agendamento agendamento = agendamentoService.createWalkinAgendamento(
                nomeCliente.trim(),
                cpfCliente.trim(),
                telefoneCliente.trim(),
                servico,
                LocalDate.now(),
                LocalTime.of(23, 59) // horário mascarado para não acionar chamada automática por horário
            );

            // Atribui o funcionário ao agendamento
            agendamento.setFuncionario(funcionario);
            agendamentoService.updateAgendamento(agendamento);

            LOGGER.log(Level.INFO, "Atendimento walk-in registrado: {0} com funcionário: {1}", 
                new Object[]{agendamento.getId(), funcionario.getNome()});
            addSuccessMessage("Atendimento walk-in registrado com sucesso! Cliente " + nomeCliente + " foi adicionado à fila.");

            // Limpa o formulário
            limparFormulario();

        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Erro de validação ao registrar walk-in", ex);
            addErrorMessage(ex.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao registrar atendimento walk-in", e);
            addErrorMessage("Erro ao registrar atendimento: " + e.getMessage());
        }
    }

    /**
     * Limpa todos os campos do formulário
     */
    private void limparFormulario() {
        nomeCliente = null;
        cpfCliente = null;
        telefoneCliente = null;
        servicoSelecionadoId = null;
        localizacaoSelecionadaId = null;
        funcionarioSelecionadoId = null;
        localizacoesList = new ArrayList<>();
        funcionariosList = new ArrayList<>();
    }

    /**
     * Adiciona mensagem de sucesso
     */
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", message));
    }

    /**
     * Adiciona mensagem de erro
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", message));
    }

    /**
     * Adiciona mensagem de aviso
     */
    private void addWarnMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", message));
    }

    // ===== GETTERS E SETTERS =====

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getCpfCliente() {
        return cpfCliente;
    }

    public void setCpfCliente(String cpfCliente) {
        this.cpfCliente = cpfCliente;
    }

    public String getTelefoneCliente() {
        return telefoneCliente;
    }

    public void setTelefoneCliente(String telefoneCliente) {
        this.telefoneCliente = telefoneCliente;
    }

    public Long getServicoSelecionadoId() {
        return servicoSelecionadoId;
    }

    public void setServicoSelecionadoId(Long servicoSelecionadoId) {
        this.servicoSelecionadoId = servicoSelecionadoId;
    }

    public List<Servico> getServicosList() {
        return servicosList;
    }

    public void setServicosList(List<Servico> servicosList) {
        this.servicosList = servicosList;
    }

    public Long getLocalizacaoSelecionadaId() {
        return localizacaoSelecionadaId;
    }

    public void setLocalizacaoSelecionadaId(Long localizacaoSelecionadaId) {
        this.localizacaoSelecionadaId = localizacaoSelecionadaId;
    }

    public List<Localizacao> getLocalizacoesList() {
        return localizacoesList;
    }

    public void setLocalizacoesList(List<Localizacao> localizacoesList) {
        this.localizacoesList = localizacoesList;
    }

    public Long getFuncionarioSelecionadoId() {
        return funcionarioSelecionadoId;
    }

    public void setFuncionarioSelecionadoId(Long funcionarioSelecionadoId) {
        this.funcionarioSelecionadoId = funcionarioSelecionadoId;
    }

    public List<Funcionario> getFuncionariosList() {
        return funcionariosList;
    }

    public void setFuncionariosList(List<Funcionario> funcionariosList) {
        this.funcionariosList = funcionariosList;
    }
}
