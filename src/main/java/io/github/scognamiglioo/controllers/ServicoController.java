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

@Named
@ViewScoped
public class ServicoController implements Serializable {

    @EJB
    private ServicoServiceLocal servicoService;
    
    @EJB
    private DataServiceLocal dataService;

    private Servico servico = new Servico();
    private List<Servico> servicos;
    private Long selectedServicoId;
    private boolean editMode = false;
    private String searchNome = "";
    
    // Para gerenciamento de funcionários por serviço
    private List<Funcionario> funcionariosPorServico;
    private List<Funcionario> todosFuncionarios;
    private Long selectedFuncionarioId;
    private Long servicoParaAssociar;

    @PostConstruct
    public void init() {
        loadServicos();
        loadTodosFuncionarios();
    }

    public void loadServicos() {
        servicos = servicoService.getAllServicos();
    }
    
    public void loadTodosFuncionarios() {
        todosFuncionarios = dataService.getAllFuncionarios();
    }

    public String save() {
        try {
            if (editMode && selectedServicoId != null) {
                // Modo edição
                Servico servicoExistente = servicoService.findServicoById(selectedServicoId);
                if (servicoExistente != null) {
                    servicoExistente.setNome(servico.getNome());
                    servicoExistente.setValor(servico.getValor());
                    servicoService.updateServico(servicoExistente);
                    
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Serviço atualizado com sucesso!", null));
                }
            } else {
                // Modo criação
                servicoService.createServico(servico.getNome(), servico.getValor());
                
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Serviço criado com sucesso!", null));
            }

            // Reset form
            servico = new Servico();
            editMode = false;
            selectedServicoId = null;
            loadServicos();

            return null; // Permanece na mesma página

        } catch (IllegalArgumentException ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
            return null;
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro interno: " + ex.getMessage(), null));
            return null;
        }
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
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar serviço: " + ex.getMessage(), null));
        }
    }

    public void delete(Long id) {
        try {
            servicoService.deleteServico(id);
            loadServicos();
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Serviço excluído com sucesso!", null));
                    
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao excluir serviço: " + ex.getMessage(), null));
        }
    }

    public void cancel() {
        servico = new Servico();
        editMode = false;
        selectedServicoId = null;
    }

    public void searchByNome() {
        try {
            if (searchNome != null && !searchNome.trim().isEmpty()) {
                servicos = servicoService.findServicosByNome(searchNome.trim());
            } else {
                loadServicos();
            }
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro na busca: " + ex.getMessage(), null));
        }
    }

    public void loadFuncionariosPorServico(Long servicoId) {
        try {
            if (servicoId != null) {
                funcionariosPorServico = servicoService.findFuncionariosByServico(servicoId);
                servicoParaAssociar = servicoId;
            }
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar funcionários: " + ex.getMessage(), null));
        }
    }

    public void associarFuncionario() {
        try {
            if (selectedFuncionarioId != null && servicoParaAssociar != null) {
                servicoService.associarFuncionarioAoServico(selectedFuncionarioId, servicoParaAssociar);
                loadFuncionariosPorServico(servicoParaAssociar);
                selectedFuncionarioId = null;
                
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Funcionário associado com sucesso!", null));
            }
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao associar funcionário: " + ex.getMessage(), null));
        }
    }

    public void desassociarFuncionario(Long funcionarioId) {
        try {
            if (funcionarioId != null && servicoParaAssociar != null) {
                servicoService.desassociarFuncionarioDoServico(funcionarioId, servicoParaAssociar);
                loadFuncionariosPorServico(servicoParaAssociar);
                
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Funcionário desassociado com sucesso!", null));
            }
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao desassociar funcionário: " + ex.getMessage(), null));
        }
    }

    public void clearFilters() {
        searchNome = "";
        loadServicos();
    }

    // Getters e Setters
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

    public List<Funcionario> getFuncionariosPorServico() {
        return funcionariosPorServico;
    }

    public void setFuncionariosPorServico(List<Funcionario> funcionariosPorServico) {
        this.funcionariosPorServico = funcionariosPorServico;
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

    public Long getServicoParaAssociar() {
        return servicoParaAssociar;
    }

    public void setServicoParaAssociar(Long servicoParaAssociar) {
        this.servicoParaAssociar = servicoParaAssociar;
    }
}