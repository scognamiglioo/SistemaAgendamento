package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.FuncionarioServico;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Servico;
import io.github.scognamiglioo.entities.Localizacao;
import io.github.scognamiglioo.services.FuncionarioServicoServiceLocal;
import io.github.scognamiglioo.services.DataServiceLocal;
import io.github.scognamiglioo.services.ServicoServiceLocal;
import io.github.scognamiglioo.services.LocalizacaoServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller para gerenciamento de associações funcionário-serviço-localização.
 */
@Named
@ViewScoped
public class FuncionarioServicoController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(FuncionarioServicoController.class.getName());

    @EJB
    private FuncionarioServicoServiceLocal funcionarioServicoService;
    
    @EJB
    private DataServiceLocal dataService;
    
    @EJB
    private ServicoServiceLocal servicoService;
    
    @EJB
    private LocalizacaoServiceLocal localizacaoService;

    // Estado do formulário
    private FuncionarioServico associacao = new FuncionarioServico();
    private Long funcionarioId;
    private Long servicoId;
    private Long localizacaoId;
    private boolean editMode = false;
    
    // Dados principais
    private List<FuncionarioServico> associacoes;
    private List<Funcionario> funcionarios;
    private List<Servico> servicos;
    private List<Localizacao> locais;
    
    // Filtros
    private Long filterFuncionarioId;
    private Long filterServicoId;
    private Long filterLocalizacaoId;
    
    // Seleções para nova associação
    private Long selectedFuncionarioId;
    private Long selectedServicoId;
    private Long selectedLocalizacaoId;
    
    // Mensagem para exibição flutuante
    private String lastMessage = "";
    private String messageType = "";

    @PostConstruct
    public void init() {
        loadData();
    }

    // ========== CARREGAMENTO DE DADOS ==========
    
    private void loadData() {
        loadAssociacoes();
        loadFuncionarios();
        loadServicos();
        loadLocalizacoes();
    }

    public void loadAssociacoes() {
        try {
            associacoes = funcionarioServicoService.getAllAssociacoes();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar associações", e);
            addErrorMessage("Erro ao carregar lista de associações");
        }
    }

    public void loadFuncionarios() {
        try {
            funcionarios = funcionarioServicoService.getAllFuncionariosWithCargo();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar funcionários", e);
            funcionarios = List.of();
            addErrorMessage("Erro ao carregar funcionários");
        }
    }

    public void loadServicos() {
        try {
            servicos = servicoService.getAllServicos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar serviços", e);
        }
    }

    public void loadLocalizacoes() {
        try {
            locais = localizacaoService.getAllLocalizacoes();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar localizações", e);
        }
    }

    // ========== CRUD ASSOCIAÇÕES ==========
    
    public String save() {
        try {
            // Validação básica
            if (funcionarioId == null) {
                lastMessage = "Selecione um funcionário";
                messageType = "error";
                return null;
            }
            
            if (servicoId == null) {
                lastMessage = "Selecione um serviço";
                messageType = "error";
                return null;
            }
            
            if (localizacaoId == null) {
                lastMessage = "Selecione um local";
                messageType = "error";
                return null;
            }
            
            if (editMode) {
                updateExistingAssociacao();
            } else {
                createNewAssociacao();
            }
            
            // Salvar mensagem antes de resetar
            String savedMessage = lastMessage;
            String savedMessageType = messageType;
            
            resetForm();
            loadAssociacoes();
            
            // Restaurar mensagem após reset
            lastMessage = savedMessage;
            messageType = savedMessageType;
            
            return null;
            
        } catch (IllegalArgumentException ex) {
            lastMessage = ex.getMessage();
            messageType = "error";
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar associação", ex);
            lastMessage = "Erro interno: " + ex.getMessage();
            messageType = "error";
            return null;
        }
    }
    
    private void updateExistingAssociacao() {
        // Como não há mais selectedAssociacaoId, vamos buscar pela chave composta atual
        FuncionarioServico associacaoParaAtualizar = funcionarioServicoService.findAssociacaoById(funcionarioId, servicoId, localizacaoId);
        if (associacaoParaAtualizar != null) {
            // A associação foi encontrada e pode ser atualizada
            funcionarioServicoService.updateAssociacao(associacaoParaAtualizar);
            lastMessage = "Associação atualizada com sucesso!";
            messageType = "success";
        } else {
            lastMessage = "Associação não encontrada!";
            messageType = "error";
        }
    }
    
    private void createNewAssociacao() {
        // Verificar se a associação já existe
        if (funcionarioServicoService.existsAssociacao(funcionarioId, servicoId, localizacaoId)) {
            lastMessage = "Já existe uma associação entre este funcionário, serviço e local";
            messageType = "error";
            return;
        }
        
        FuncionarioServico novaAssociacao = funcionarioServicoService.createAssociacao(
            funcionarioId, servicoId, localizacaoId);
        
        lastMessage = "Associação criada com sucesso!";
        messageType = "success";
    }
    
    private void resetForm() {
        associacao = new FuncionarioServico();
        funcionarioId = null;
        servicoId = null;
        localizacaoId = null;
        editMode = false;
    }

    public void edit(Long funcionarioIdParam, Long servicoIdParam, Long localizacaoIdParam) {
        try {
            FuncionarioServico associacaoParaEditar = funcionarioServicoService.findAssociacaoById(funcionarioIdParam, servicoIdParam, localizacaoIdParam);
            if (associacaoParaEditar != null) {
                associacao = new FuncionarioServico();
                
                funcionarioId = funcionarioIdParam;
                servicoId = servicoIdParam;
                localizacaoId = localizacaoIdParam;
                
                editMode = true;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar associação para edição", ex);
            addErrorMessage("Erro ao carregar associação: " + ex.getMessage());
        }
    }

    public void delete(Long funcionarioIdParam, Long servicoIdParam, Long localizacaoIdParam) {
        deleteAssociacao(funcionarioIdParam, servicoIdParam, localizacaoIdParam);
    }

    public void cancel() {
        resetForm();
    }
    
    public void createAssociacao() {
        try {
            // Validação
            if (selectedFuncionarioId == null || selectedServicoId == null || selectedLocalizacaoId == null) {
                lastMessage = "Selecione funcionário, serviço e localização";
                messageType = "error";
                return;
            }
            
            // Verificar se já existe
            if (funcionarioServicoService.existsAssociacao(selectedFuncionarioId, selectedServicoId, selectedLocalizacaoId)) {
                lastMessage = "Esta associação já existe";
                messageType = "error";
                return;
            }
            
            // Criar associação
            funcionarioServicoService.createAssociacao(selectedFuncionarioId, selectedServicoId, selectedLocalizacaoId);
            
            // Limpar seleções
            selectedFuncionarioId = null;
            selectedServicoId = null;
            selectedLocalizacaoId = null;
            
            // Recarregar lista
            loadAssociacoes();
            
            lastMessage = "Associação criada com sucesso!";
            messageType = "success";
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao criar associação", ex);
            lastMessage = "Erro ao criar associação: " + ex.getMessage();
            messageType = "error";
        }
    }
    
    public void deleteAssociacao(Long funcionarioIdParam, Long servicoIdParam, Long localizacaoIdParam) {
        try {
            funcionarioServicoService.deleteAssociacao(funcionarioIdParam, servicoIdParam, localizacaoIdParam);
            loadAssociacoes();
            
            lastMessage = "Associação excluída com sucesso!";
            messageType = "success";
            addSuccessMessage(lastMessage);
            
        } catch (IllegalArgumentException ex) {
            lastMessage = ex.getMessage();
            messageType = "error";
            addErrorMessage(lastMessage);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir associação", ex);
            lastMessage = "Erro ao excluir associação";
            messageType = "error";
            addErrorMessage(lastMessage);
        }
    }

    // ========== FILTROS E BUSCA ==========
    
    public void applyFilters() {
        try {
            if (hasActiveFilters()) {
                associacoes = funcionarioServicoService.findAssociacoesWithFilters(
                    filterFuncionarioId, filterServicoId, filterLocalizacaoId);
            } else {
                loadAssociacoes();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao aplicar filtros", ex);
            addErrorMessage("Erro ao aplicar filtros: " + ex.getMessage());
        }
    }

    public void clearFilters() {
        filterFuncionarioId = null;
        filterServicoId = null;
        filterLocalizacaoId = null;
        loadAssociacoes();
    }
    
    private boolean hasActiveFilters() {
        return filterFuncionarioId != null || filterServicoId != null || 
               filterLocalizacaoId != null;
    }

    // ========== MÉTODOS AUXILIARES ==========
    
    public List<FuncionarioServico> getAssociacoesPorFuncionario(Long funcionarioId) {
        try {
            return funcionarioServicoService.findAssociacoesByFuncionario(funcionarioId);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar associações por funcionário", e);
            return List.of();
        }
    }
    
    public List<FuncionarioServico> getAssociacoesPorServico(Long servicoId) {
        try {
            return funcionarioServicoService.findAssociacoesByServico(servicoId);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar associações por serviço", e);
            return List.of();
        }
    }
    
    public List<FuncionarioServico> getAssociacoesPorLocal(Long localizacaoId) {
        try {
            return funcionarioServicoService.findAssociacoesByLocalizacao(localizacaoId);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar associações por local", e);
            return List.of();
        }
    }

    // ========== NAVEGAÇÃO ==========
    
    public void loadAssociacaoForEdit() {
        if (funcionarioId != null && servicoId != null && localizacaoId != null) {
            try {
                FuncionarioServico associacaoParaEditar = funcionarioServicoService.findAssociacaoById(funcionarioId, servicoId, localizacaoId);
                if (associacaoParaEditar != null) {
                    associacao = new FuncionarioServico();
                    
                    editMode = true;
                } else {
                    addErrorMessage("Associação não encontrada!");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Erro ao carregar associação para edição", ex);
                addErrorMessage("Erro ao carregar associação: " + ex.getMessage());
            }
        } else {
            editMode = false;
            resetForm();
        }
    }
    
    // ========== UTILIDADES ==========
    
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }
    
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
    
    private boolean hasErrors() {
        return FacesContext.getCurrentInstance()
            .getMessageList()
            .stream()
            .anyMatch(msg -> msg.getSeverity().equals(FacesMessage.SEVERITY_ERROR));
    }
    
    public String formatDate(Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
    }

    // ========== GETTERS E SETTERS ==========
    
    public FuncionarioServico getAssociacao() {
        return associacao;
    }

    public void setAssociacao(FuncionarioServico associacao) {
        this.associacao = associacao;
    }

    public List<FuncionarioServico> getAssociacoes() {
        return associacoes;
    }

    public void setAssociacoes(List<FuncionarioServico> associacoes) {
        this.associacoes = associacoes;
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }

    public List<Servico> getServicos() {
        return servicos;
    }

    public void setServicos(List<Servico> servicos) {
        this.servicos = servicos;
    }

    public List<Localizacao> getLocalizacoes() {
        return locais;
    }

    public void setLocalizacoes(List<Localizacao> locais) {
        this.locais = locais;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Long funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
    }

    public Long getLocalizacaoId() {
        return localizacaoId;
    }

    public void setLocalizacaoId(Long localizacaoId) {
        this.localizacaoId = localizacaoId;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getFormTitle() {
        return editMode ? "Editar Associação" : "Nova Associação";
    }

    public String getSaveButtonText() {
        return editMode ? "Atualizar" : "Salvar";
    }

    public Long getFilterFuncionarioId() {
        return filterFuncionarioId;
    }

    public void setFilterFuncionarioId(Long filterFuncionarioId) {
        this.filterFuncionarioId = filterFuncionarioId;
    }

    public Long getFilterServicoId() {
        return filterServicoId;
    }

    public void setFilterServicoId(Long filterServicoId) {
        this.filterServicoId = filterServicoId;
    }

    public Long getFilterLocalizacaoId() {
        return filterLocalizacaoId;
    }

    public void setFilterLocalizacaoId(Long filterLocalizacaoId) {
        this.filterLocalizacaoId = filterLocalizacaoId;
    }
    
    public int getAssociacoesCount() {
        return associacoes != null ? associacoes.size() : 0;
    }
    
    public int getTotalAssociacoes() {
        return getAssociacoesCount();
    }
    
    // Getters e setters para seleções de nova associação
    public Long getSelectedFuncionarioId() {
        return selectedFuncionarioId;
    }
    
    public void setSelectedFuncionarioId(Long selectedFuncionarioId) {
        this.selectedFuncionarioId = selectedFuncionarioId;
    }
    
    public Long getSelectedServicoId() {
        return selectedServicoId;
    }
    
    public void setSelectedServicoId(Long selectedServicoId) {
        this.selectedServicoId = selectedServicoId;
    }
    
    public Long getSelectedLocalizacaoId() {
        return selectedLocalizacaoId;
    }
    
    public void setSelectedLocalizacaoId(Long selectedLocalizacaoId) {
        this.selectedLocalizacaoId = selectedLocalizacaoId;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    // ========== MÉTODOS AUXILIARES PARA AGRUPAMENTO ========== 
    
    /**
     * Verifica se um funcionário tem associações
     */
    public boolean funcionarioTemAssociacoes(Long funcionarioId) {
        if (associacoes == null || funcionarioId == null) {
            return false;
        }
        
        return associacoes.stream()
                .anyMatch(assoc -> assoc.getFuncionario().getId().equals(funcionarioId));
    }
}