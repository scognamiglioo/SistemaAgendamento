package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.Cargo;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.services.CargoServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
/**
 * Controller para gerenciamento de cargos.
 * Responsabilidades:
 * - CRUD de cargos
 * - Busca e filtros
 * - Navegação entre páginas
 * - Validação e tratamento de erros
 */
@Named
@ViewScoped
public class CargoController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(CargoController.class.getName());

    @EJB
    private CargoServiceLocal cargoService;

    // Estado do formulário
    private Cargo cargo = new Cargo();
    private Long selectedCargoId;
    private boolean editMode = false;
    
    // Dados principais
    private List<Cargo> cargos;
    private String searchNome = "";
    
    // Funcionários relacionados
    private List<Funcionario> funcionariosDoCargoSelecionado;
    private Map<Long, List<Funcionario>> funcionariosPorCargoMap;

    // Mensagem para exibição flutuante
    private String lastMessage = "";
    private String messageType = "";

    @PostConstruct
    public void init() {
        try {
            funcionariosPorCargoMap = new HashMap<>();
            loadCargos(); // Carrega cargos e popula o mapa
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao inicializar CargoController", ex);
            cargos = new ArrayList<>();
            funcionariosPorCargoMap = new HashMap<>();
        }
    }

    // ========== CRUD CARGOS ==========
    
    public void loadCargos() {
        try {
            cargos = cargoService.getAllCargos();
            funcionariosPorCargoMap.clear();
            
            // Popula o mapa com funcionários de cada cargo
            if (cargos != null) {
                for (Cargo cargo : cargos) {
                    try {
                        List<Funcionario> funcionarios = cargoService.findFuncionariosByCargo(cargo.getId());
                        funcionariosPorCargoMap.put(cargo.getId(), funcionarios != null ? funcionarios : new ArrayList<>());
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Erro ao carregar funcionários do cargo " + cargo.getId(), ex);
                        funcionariosPorCargoMap.put(cargo.getId(), new ArrayList<>());
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar cargos", ex);
            cargos = new ArrayList<>();
        }
    }

    public String save() {
        try {
            // Validação do nome do cargo
            if (cargo == null || cargo.getNome() == null || cargo.getNome().trim().isEmpty()) {
                lastMessage = "Nome do cargo é obrigatório";
                messageType = "error";
                return null;
            }
            
            String nomeValidado = cargo.getNome().trim();
            if (nomeValidado.length() < 2) {
                lastMessage = "O nome do cargo deve ter pelo menos 2 caracteres";
                messageType = "error";
                return null;
            }
            
            if (nomeValidado.length() > 100) {
                lastMessage = "O nome do cargo deve ter no máximo 100 caracteres";
                messageType = "error";
                return null;
            }
            
            // Atualizar o nome limpo
            cargo.setNome(nomeValidado);
            
            if (editMode && selectedCargoId != null) {
                updateExistingCargo();
            } else {
                createNewCargo();
            }
            
            // Salvar mensagem antes de resetar
            String savedMessage = lastMessage;
            String savedMessageType = messageType;
            
            resetForm();
            loadCargos();
            
            // Restaurar mensagem após reset
            lastMessage = savedMessage;
            messageType = savedMessageType;
            
            return null;
            
        } catch (IllegalArgumentException ex) {
            lastMessage = ex.getMessage();
            messageType = "error";
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar cargo", ex);
            lastMessage = "Erro interno: " + ex.getMessage();
            messageType = "error";
            return null;
        }
    }
    
    private void updateExistingCargo() {
        Cargo cargoParaAtualizar = cargoService.findCargoById(selectedCargoId);
        if (cargoParaAtualizar != null) {
            cargoParaAtualizar.setNome(cargo.getNome()); // Nome já foi validado e limpo
            cargoService.updateCargo(cargoParaAtualizar);
            lastMessage = "Cargo atualizado com sucesso!";
            messageType = "success";
        } else {
            lastMessage = "Cargo não encontrado!";
            messageType = "error";
        }
    }
    
    private void createNewCargo() {
        cargoService.createCargo(cargo.getNome()); // Nome já foi validado e limpo
        lastMessage = "Cargo criado com sucesso!";
        messageType = "success";
    }
    
    private void resetForm() {
        cargo = new Cargo();
        editMode = false;
        selectedCargoId = null;
        funcionariosDoCargoSelecionado = null;
        // Não limpar lastMessage e messageType aqui para permitir exibição
    }

    public void edit(Long id) {
        try {
            Cargo cargoParaEditar = cargoService.findCargoById(id);
            if (cargoParaEditar != null) {
                cargo = new Cargo();
                cargo.setNome(cargoParaEditar.getNome());
                selectedCargoId = id;
                editMode = true;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar cargo para edição", ex);
            addErrorMessage("Erro ao carregar cargo: " + ex.getMessage());
        }
    }

    public void delete(Long id) {
        try {
            // Verificar primeiro se há funcionários associados
            long funcionariosCount = cargoService.countFuncionariosByCargo(id);
            if (funcionariosCount > 0) {
                lastMessage = "Não é possível excluir este cargo pois existem " + funcionariosCount + " funcionário(s) associado(s) a ele. Remova os funcionários primeiro.";
                messageType = "error";
                return;
            }
            
            cargoService.deleteCargo(id);
            loadCargos();
            lastMessage = "Cargo excluído com sucesso!";
            messageType = "success";
        } catch (IllegalStateException ex) {
            lastMessage = ex.getMessage();
            messageType = "error";
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir cargo", ex);
            lastMessage = "Erro ao excluir cargo: " + ex.getMessage();
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
                cargos = cargoService.findCargosByNomePartial(searchNome.trim());
            } else {
                loadCargos();
                return;
            }
            // Não é mais necessário chamar loadAllFuncionarios aqui
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro na busca", ex);
            addErrorMessage("Erro na busca: " + ex.getMessage());
        }
    }

    public void clearFilters() {
        searchNome = "";
        loadCargos();
    }

    // ========== FUNCIONÁRIOS ==========
    
    public void loadFuncionariosByCargo(Long cargoId) {
        try {
            funcionariosDoCargoSelecionado = cargoService.findFuncionariosByCargo(cargoId);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar funcionários do cargo", ex);
            addErrorMessage("Erro ao carregar funcionários do cargo");
            funcionariosDoCargoSelecionado = List.of();
        }
    }

    // ========== NAVEGAÇÃO ==========
    
    public void loadCargoForEdit() {
        if (selectedCargoId != null && selectedCargoId > 0) {
            try {
                Cargo cargoParaEditar = cargoService.findCargoById(selectedCargoId);
                if (cargoParaEditar != null) {
                    cargo = new Cargo();
                    cargo.setNome(cargoParaEditar.getNome());
                    editMode = true;
                    // Auto-carregar funcionários quando em modo de edição
                    loadFuncionariosByCargo(selectedCargoId);
                } else {
                    addErrorMessage("Cargo não encontrado!");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Erro ao carregar cargo para edição", ex);
                addErrorMessage("Erro ao carregar cargo: " + ex.getMessage());
            }
        } else {
            editMode = false;
            cargo = new Cargo();
            funcionariosDoCargoSelecionado = null;
        }
    }

    public String saveAndReturn() {
        String result = save();
        if (result == null && !hasErrors()) {
            return navigateToList();
        }
        return null;
    }

    public String navigateToEdit(Long cargoId) {
        selectedCargoId = cargoId;
        return "/app/cargo/adicionar_cargo.xhtml?faces-redirect=true&id=" + cargoId;
    }

    public String navigateToAdd() {
        return "/app/cargo/adicionar_cargo.xhtml?faces-redirect=true";
    }

    public String navigateToList() {
        return "/app/cargo/gerenciar_cargos.xhtml?faces-redirect=true";
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
    
    public Map<Long, List<Funcionario>> getFuncionariosPorCargoMap() {
        return funcionariosPorCargoMap;
    }
    
    public int getFuncionariosCount(Long cargoId) {
        List<Funcionario> funcionarios = funcionariosPorCargoMap.get(cargoId);
        return funcionarios != null ? funcionarios.size() : 0;
    }

    // ========== GETTERS E SETTERS ==========
    
    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public List<Cargo> getCargos() {
        return cargos;
    }

    public void setCargos(List<Cargo> cargos) {
        this.cargos = cargos;
    }

    public Long getSelectedCargoId() {
        return selectedCargoId;
    }

    public void setSelectedCargoId(Long selectedCargoId) {
        this.selectedCargoId = selectedCargoId;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getFormTitle() {
        return editMode ? "Editar Cargo" : "Novo Cargo";
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

    public List<Funcionario> getFuncionariosDoCargoSelecionado() {
        return funcionariosDoCargoSelecionado;
    }

    public void setFuncionariosDoCargoSelecionado(List<Funcionario> funcionariosDoCargoSelecionado) {
        this.funcionariosDoCargoSelecionado = funcionariosDoCargoSelecionado;
    }
    
    public int getCargosCount() {
        return cargos != null ? cargos.size() : 0;
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