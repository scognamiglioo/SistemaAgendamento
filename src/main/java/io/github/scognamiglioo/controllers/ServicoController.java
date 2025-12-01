package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Servico;
import io.github.scognamiglioo.services.ServicoServiceLocal;
import io.github.scognamiglioo.services.DataServiceLocal;
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
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller otimizado para gerenciamento de serviços.
 * Responsabilidades:
 * - CRUD de serviços
 * - Carregamento otimizado de funcionários por serviço
 * - Navegação entre páginas
 * - Gerenciamento de associações funcionário-serviço
 */
@Named
@ViewScoped
public class ServicoController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(ServicoController.class.getName());

    @EJB
    private ServicoServiceLocal servicoService;
    
    @EJB
    private DataServiceLocal dataService;

    // Estado do formulário
    private Servico servico = new Servico();
    private Long selectedServicoId;
    private boolean editMode = false;
    
    // Dados principais
    private List<Servico> servicos;
    private String searchNome = "";
    
    // Funcionários (otimizado para carregamento único)
    private List<Funcionario> todosFuncionarios;
    private Long selectedFuncionarioId;
    private Map<Long, List<Funcionario>> funcionariosPorServicoMap;

    @PostConstruct
    public void init() {
        loadServicos();
        loadTodosFuncionarios();
        loadAllFuncionariosPorServico();
    }

    // ========== CRUD SERVIÇOS ==========
    
    public void loadServicos() {
        try {
            servicos = servicoService.getAllServicos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar serviços", e);
            addErrorMessage("Erro ao carregar lista de serviços");
        }
    }

    public String save() {
        try {
            if (editMode && selectedServicoId != null) {
                updateExistingServico();
            } else {
                createNewServico();
            }
            
            resetForm();
            loadServicos();
            loadAllFuncionariosPorServico();
            return null;
            
        } catch (IllegalArgumentException ex) {
            addErrorMessage(ex.getMessage());
            return null;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar serviço", ex);
            addErrorMessage("Erro interno: " + ex.getMessage());
            return null;
        }
    }
    
    private void updateExistingServico() {
        Servico servicoParaAtualizar = servicoService.findServicoById(selectedServicoId);
        if (servicoParaAtualizar != null) {
            servicoParaAtualizar.setNome(servico.getNome());
            servicoParaAtualizar.setValor(servico.getValor());
            servicoService.updateServico(servicoParaAtualizar);
            addSuccessMessage("Serviço atualizado com sucesso!");
        }
    }
    
    private void createNewServico() {
        servicoService.createServico(servico.getNome(), servico.getValor());
        addSuccessMessage("Serviço criado com sucesso!");
    }
    
    private void resetForm() {
        servico = new Servico();
        editMode = false;
        selectedServicoId = null;
    }

    public void edit(Long id) {
        try {
            Servico servicoParaEditar = servicoService.findServicoById(id);
            if (servicoParaEditar != null) {
                servico = new Servico();
                servico.setNome(servicoParaEditar.getNome());
                servico.setValor(servicoParaEditar.getValor());
                selectedServicoId = id;
                editMode = true;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar serviço para edição", ex);
            addErrorMessage("Erro ao carregar serviço: " + ex.getMessage());
        }
    }

    public void delete(Long id) {
        try {
            servicoService.deleteServico(id);
            loadServicos();
            loadAllFuncionariosPorServico();
            addSuccessMessage("Serviço excluído com sucesso!");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir serviço", ex);
            addErrorMessage("Erro ao excluir serviço: " + ex.getMessage());
        }
    }

    public void cancel() {
        resetForm();
    }

    // ========== BUSCA ==========
    
    public void searchByNome() {
        try {
            if (searchNome != null && !searchNome.trim().isEmpty()) {
                // Usa busca parcial para melhor experiência do usuário
                servicos = servicoService.findServicosByNomePartial(searchNome.trim());
            } else {
                loadServicos();
            }
            loadAllFuncionariosPorServico();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro na busca", ex);
            addErrorMessage("Erro na busca: " + ex.getMessage());
        }
    }

    public void clearFilters() {
        searchNome = "";
        loadServicos();
        loadAllFuncionariosPorServico();
    }

    // ========== FUNCIONÁRIOS ==========
    
    public void loadTodosFuncionarios() {
        try {
            todosFuncionarios = dataService.getAllFuncionarios();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar funcionários", e);
            addErrorMessage("Erro ao carregar lista de funcionários");
        }
    }
    
    /**
     * Carrega todos os funcionários por serviço de uma só vez para otimizar performance.
     * Esta abordagem reduz o número de consultas ao banco e melhora a experiência do usuário.
     */
    public void loadAllFuncionariosPorServico() {
        funcionariosPorServicoMap = new HashMap<>();
        if (servicos != null) {
            for (Servico servico : servicos) {
                try {
                    List<Funcionario> funcionarios = servicoService.findFuncionariosByServico(servico.getId());
                    funcionariosPorServicoMap.put(servico.getId(), funcionarios != null ? funcionarios : new ArrayList<>());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Erro ao carregar funcionários do serviço " + servico.getId(), e);
                    funcionariosPorServicoMap.put(servico.getId(), new ArrayList<>());
                }
            }
        }
    }
    
    public void associarFuncionario() {
        // Este método precisa ser implementado conforme a necessidade
        // Mantido para compatibilidade com possível funcionalidade futura
    }

    public void desassociarFuncionario(Long funcionarioId) {
        // Este método precisa ser implementado conforme a necessidade  
        // Mantido para compatibilidade com possível funcionalidade futura
    }

    // ========== NAVEGAÇÃO ==========
    
    public void loadServicoForEdit() {
        if (selectedServicoId != null && selectedServicoId > 0) {
            try {
                Servico servicoParaEditar = servicoService.findServicoById(selectedServicoId);
                if (servicoParaEditar != null) {
                    servico = new Servico();
                    servico.setNome(servicoParaEditar.getNome());
                    servico.setValor(servicoParaEditar.getValor());
                    editMode = true;
                } else {
                    addErrorMessage("Serviço não encontrado!");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Erro ao carregar serviço para edição", ex);
                addErrorMessage("Erro ao carregar serviço: " + ex.getMessage());
            }
        } else {
            editMode = false;
            servico = new Servico();
        }
    }

    public String saveAndReturn() {
        String result = save();
        if (result == null && !hasErrors()) {
            return navigateToList();
        }
        return null;
    }

    public String navigateToEdit(Long servicoId) {
        selectedServicoId = servicoId;
        return "/app/servico/adicionar_servico.xhtml?faces-redirect=true&id=" + servicoId;
    }

    public String navigateToAdd() {
        return "/app/servico/adicionar_servico.xhtml?faces-redirect=true";
    }

    public String navigateToList() {
        return "/app/servico/gerenciar_servicos.xhtml?faces-redirect=true";
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
    
    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public List<Servico> getServicos() {
        return servicos;
    }

    public void setServicos(List<Servico> servicos) {
        this.servicos = servicos;
    }

    public Long getSelectedServicoId() {
        return selectedServicoId;
    }

    public void setSelectedServicoId(Long selectedServicoId) {
        this.selectedServicoId = selectedServicoId;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getFormTitle() {
        return editMode ? "Editar Serviço" : "Novo Serviço";
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

    public List<Funcionario> getTodosFuncionarios() {
        return todosFuncionarios;
    }

    public void setTodosFuncionarios(List<Funcionario> todosFuncionarios) {
        this.todosFuncionarios = todosFuncionarios;
    }

    public Long getSelectedFuncionarioId() {
        return selectedFuncionarioId;
    }

    public void setSelectedFuncionarioId(Long selectedFuncionarioId) {
        this.selectedFuncionarioId = selectedFuncionarioId;
    }

    public Map<Long, List<Funcionario>> getFuncionariosPorServicoMap() {
        return funcionariosPorServicoMap;
    }

    public void setFuncionariosPorServicoMap(Map<Long, List<Funcionario>> funcionariosPorServicoMap) {
        this.funcionariosPorServicoMap = funcionariosPorServicoMap;
    }
    
    public int getServicosCount() {
        return servicos != null ? servicos.size() : 0;
    }
    
    public int getFuncionariosCountByServico(Long servicoId) {
        if (funcionariosPorServicoMap == null || servicoId == null) {
            return 0;
        }
        List<Funcionario> funcionarios = funcionariosPorServicoMap.get(servicoId);
        return funcionarios != null ? funcionarios.size() : 0;
    }
}