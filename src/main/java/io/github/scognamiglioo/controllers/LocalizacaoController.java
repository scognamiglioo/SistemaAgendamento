package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.Localizacao;
import io.github.scognamiglioo.services.LocalizacaoServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller para gerenciamento de locais.
 */
@Named
@ViewScoped
public class LocalizacaoController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(LocalizacaoController.class.getName());

    @EJB
    private LocalizacaoServiceLocal localizacaoService;

    // Estado do formulário
    private Localizacao localizacao = new Localizacao();
    private Long selectedLocalId;
    private boolean editMode = false;
    
    // Dados principais
    private List<Localizacao> locais;
    private String searchNome = "";
    
    // Mensagem para exibição flutuante
    private String lastMessage = "";
    private String messageType = "";

    @PostConstruct
    public void init() {
        loadLocalizacaoizacoes();
    }

    // ========== CRUD LOCAIS ==========
    
    public void loadLocalizacaoizacoes() {
        try {
            locais = localizacaoService.getAllLocalizacoes();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar locais", e);
            addErrorMessage("Erro ao carregar lista de locais");
        }
    }

    public String save() {
        try {
            // Validação do nome do local
            if (localizacao == null || localizacao.getNome() == null || localizacao.getNome().trim().isEmpty()) {
                lastMessage = "Nome do local é obrigatório";
                messageType = "error";
                return null;
            }
            
            String nomeValidado = localizacao.getNome().trim();
            if (nomeValidado.length() < 2) {
                lastMessage = "O nome do local deve ter pelo menos 2 caracteres";
                messageType = "error";
                return null;
            }
            
            if (nomeValidado.length() > 100) {
                lastMessage = "O nome do local deve ter no máximo 100 caracteres";
                messageType = "error";
                return null;
            }
            
            // Atualizar o nome limpo
            localizacao.setNome(nomeValidado);
            
            if (editMode && selectedLocalId != null) {
                updateExistingLocal();
            } else {
                createNewLocal();
            }
            
            // Salvar mensagem antes de resetar
            String savedMessage = lastMessage;
            String savedMessageType = messageType;
            
            resetForm();
            loadLocalizacaoizacoes();
            
            // Restaurar mensagem após reset
            lastMessage = savedMessage;
            messageType = savedMessageType;
            
            return null;
            
        } catch (IllegalArgumentException ex) {
            lastMessage = ex.getMessage();
            messageType = "error";
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar local", ex);
            lastMessage = "Erro interno: " + ex.getMessage();
            messageType = "error";
            return null;
        }
    }
    
    private void updateExistingLocal() {
        Localizacao localParaAtualizar = localizacaoService.findLocalizacaoById(selectedLocalId);
        if (localParaAtualizar != null) {
            localParaAtualizar.setNome(localizacao.getNome());
            localParaAtualizar.setDescricao(localizacao.getDescricao());
            localizacaoService.updateLocalizacao(localParaAtualizar);
            lastMessage = "Local atualizado com sucesso!";
            messageType = "success";
        } else {
            lastMessage = "Local não encontrado!";
            messageType = "error";
        }
    }
    
    private void createNewLocal() {
        Localizacao localCriado = localizacaoService.createLocalizacao(localizacao.getNome(), localizacao.getDescricao());
        lastMessage = "Local criado com sucesso!";
        messageType = "success";
    }
    
    private void resetForm() {
        localizacao = new Localizacao();
        editMode = false;
        selectedLocalId = null;
    }

    public void edit(Long id) {
        try {
            Localizacao localParaEditar = localizacaoService.findLocalizacaoById(id);
            if (localParaEditar != null) {
                localizacao = new Localizacao();
                localizacao.setNome(localParaEditar.getNome());
                localizacao.setDescricao(localParaEditar.getDescricao());
                selectedLocalId = id;
                editMode = true;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar local para edição", ex);
            addErrorMessage("Erro ao carregar local: " + ex.getMessage());
        }
    }

    public void delete(Long id) {
        try {
            // Verificar primeiro se há associações com este local
            long associacoesCount = localizacaoService.countFuncionarioServicoByLocalizacao(id);
            if (associacoesCount > 0) {
                Localizacao localParaExcluir = localizacaoService.findLocalizacaoById(id);
                String nomeLocal = localParaExcluir != null ? localParaExcluir.getNome() : "este local";
                
                lastMessage = "Não é possível excluir \"" + nomeLocal + "\" pois há " + associacoesCount + 
                             " associação(ões) funcionário-serviço vinculada(s) a este localizacao. Remova as associações primeiro.";
                messageType = "error";
                return;
            }
            
            localizacaoService.deleteLocalizacao(id);
            loadLocalizacaoizacoes();
            lastMessage = "Local excluído com sucesso!";
            messageType = "success";
        } catch (IllegalStateException ex) {
            lastMessage = ex.getMessage();
            messageType = "error";
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir local", ex);
            lastMessage = "Erro ao excluir local: " + ex.getMessage();
            messageType = "error";
        }
    }

    public void cancel() {
        resetForm();
    }

    // ========== BUSCA ==========
    
    public void searchByNome() {
        try {
            if (searchNome != null && !searchNome.trim().isEmpty()) {
                locais = localizacaoService.findLocalizacoesByNomePartial(searchNome.trim());
            } else {
                loadLocalizacaoizacoes();
                return;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro na busca", ex);
            addErrorMessage("Erro na busca: " + ex.getMessage());
        }
    }

    public void clearFilters() {
        searchNome = "";
        loadLocalizacaoizacoes();
    }

    // ========== NAVEGAÇÃO ==========
    
    public void loadLocalizacaoForEdit() {
        if (selectedLocalId != null && selectedLocalId > 0) {
            try {
                Localizacao localParaEditar = localizacaoService.findLocalizacaoById(selectedLocalId);
                if (localParaEditar != null) {
                    localizacao = new Localizacao();
                    localizacao.setNome(localParaEditar.getNome());
                    localizacao.setDescricao(localParaEditar.getDescricao());
                    editMode = true;
                } else {
                    addErrorMessage("Local não encontrado!");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Erro ao carregar local para edição", ex);
                addErrorMessage("Erro ao carregar local: " + ex.getMessage());
            }
        } else {
            editMode = false;
            localizacao = new Localizacao();
        }
    }

    public String saveAndReturn() {
        String result = save();
        if (result == null && !hasErrors()) {
            return navigateToList();
        }
        return null;
    }

    public String navigateToEdit(Long localId) {
        selectedLocalId = localId;
        return "/app/local/adicionar_localizacao.xhtml?faces-redirect=true&id=" + localId;
    }

    public String navigateToAdd() {
        return "/app/local/adicionar_localizacao.xhtml?faces-redirect=true";
    }

    public String navigateToList() {
        return "/app/local/gerenciar_locais.xhtml?faces-redirect=true";
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

    // ========== GETTERS E SETTERS ==========
    
    public Localizacao getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(Localizacao localizacao) {
        this.localizacao = localizacao;
    }

    public List<Localizacao> getLocalizacoes() {
        return locais;
    }

    public void setLocalizacoes(List<Localizacao> locais) {
        this.locais = locais;
    }

    public Long getSelectedLocalId() {
        return selectedLocalId;
    }

    public void setSelectedLocalId(Long selectedLocalId) {
        this.selectedLocalId = selectedLocalId;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getFormTitle() {
        return editMode ? "Editar Local" : "Novo Local";
    }

    public String getSaveButtonText() {
        return editMode ? "Atualizar" : "Salvar";
    }

    public String getSearchNome() {
        return searchNome;
    }

    public void setSearchNome(String searchNome) {
        this.searchNome = searchNome;
    }
    
    public int getLocalizacoesCount() {
        return locais != null ? locais.size() : 0;
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
}