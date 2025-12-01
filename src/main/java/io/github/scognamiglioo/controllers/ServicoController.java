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
    private Long servicoAtualFuncionarios; // ID do serviço que tem funcionários carregados
    
    // Map para carregar todos os funcionários de uma vez
    private Map<Long, List<Funcionario>> funcionariosPorServicoMap;

    @PostConstruct
    public void init() {
        loadServicos();
        loadTodosFuncionarios();
        loadAllFuncionariosPorServico();
    }

    public void loadServicos() {
        servicos = servicoService.getAllServicos();
    }
    
    public void loadTodosFuncionarios() {
        todosFuncionarios = dataService.getAllFuncionarios();
    }
    
    public void loadAllFuncionariosPorServico() {
        funcionariosPorServicoMap = new HashMap<>();
        if (servicos != null) {
            for (Servico servico : servicos) {
                try {
                    List<Funcionario> funcionarios = servicoService.findFuncionariosByServico(servico.getId());
                    if (funcionarios == null) {
                        funcionarios = new ArrayList<>();
                    }
                    funcionariosPorServicoMap.put(servico.getId(), funcionarios);
                    System.out.println("Carregados " + funcionarios.size() + " funcionários para serviço " + servico.getId());
                } catch (Exception e) {
                    System.err.println("Erro ao carregar funcionários para serviço " + servico.getId() + ": " + e.getMessage());
                    funcionariosPorServicoMap.put(servico.getId(), new ArrayList<>());
                }
            }
        }
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
                    
                    // Recarregar a lista após atualização
                    loadServicos();
                    loadAllFuncionariosPorServico();
                    
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Serviço atualizado com sucesso!", null));
                }
            } else {
                // Modo criação
                servicoService.createServico(servico.getNome(), servico.getValor());
                
                // Recarregar a lista após criação
                loadServicos();
                loadAllFuncionariosPorServico();
                
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
            loadAllFuncionariosPorServico();
            
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
            // Sempre recarregar funcionários após busca
            loadAllFuncionariosPorServico();
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro na busca: " + ex.getMessage(), null));
        }
    }

    public void loadFuncionariosPorServico(Long servicoId) {
        try {
            System.out.println("Carregando funcionários para serviço ID: " + servicoId);
            if (servicoId != null) {
                funcionariosPorServico = servicoService.findFuncionariosByServico(servicoId);
                // Não mudamos servicoParaAssociar aqui
                System.out.println("Funcionários encontrados: " + 
                    (funcionariosPorServico != null ? funcionariosPorServico.size() : "null"));
                if (funcionariosPorServico != null) {
                    for (Funcionario f : funcionariosPorServico) {
                        System.out.println("- " + f.getNome());
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao carregar funcionários: " + ex.getMessage());
            ex.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar funcionários: " + ex.getMessage(), null));
        }
    }

    public void loadFuncionariosOnly(Long servicoId) {
        try {
            System.out.println("=== DEBUG loadFuncionariosOnly ===");
            System.out.println("Carregando apenas funcionários para serviço ID: " + servicoId);
            System.out.println("Estado atual funcionariosPorServico antes da consulta: " + 
                (funcionariosPorServico != null ? funcionariosPorServico.size() + " itens" : "null"));
            
            if (servicoId != null) {
                funcionariosPorServico = servicoService.findFuncionariosByServico(servicoId);
                servicoAtualFuncionarios = servicoId; // Define qual serviço tem os dados
                System.out.println("Funcionários encontrados: " + 
                    (funcionariosPorServico != null ? funcionariosPorServico.size() : "null"));
                
                if (funcionariosPorServico != null && !funcionariosPorServico.isEmpty()) {
                    System.out.println("Lista de funcionários carregados:");
                    for (int i = 0; i < funcionariosPorServico.size(); i++) {
                        Funcionario f = funcionariosPorServico.get(i);
                        System.out.println("  [" + i + "] ID: " + f.getId() + ", Nome: " + f.getNome() + ", Email: " + f.getEmail());
                    }
                } else {
                    System.out.println("Nenhum funcionário encontrado ou lista é null/empty");
                }
                
                System.out.println("Estado final funcionariosPorServico: " + 
                    (funcionariosPorServico != null ? funcionariosPorServico.size() + " itens" : "null"));
                System.out.println("servicoAtualFuncionarios definido como: " + servicoAtualFuncionarios);
            } else {
                System.out.println("servicoId é null - não executando consulta");
            }
            System.out.println("=== FIM DEBUG loadFuncionariosOnly ===");
        } catch (Exception ex) {
            System.err.println("Erro ao carregar funcionários: " + ex.getMessage());
            ex.printStackTrace();
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
        loadAllFuncionariosPorServico();
    }
    
    public void toggleFuncionarios(Long servicoId) {
        System.out.println("Toggle funcionarios chamado para serviço ID: " + servicoId);
        System.out.println("Serviço atual para associar: " + servicoParaAssociar);
        
        if (servicoParaAssociar != null && servicoParaAssociar.equals(servicoId)) {
            // Se o mesmo serviço já está selecionado, esconde
            servicoParaAssociar = null;
            funcionariosPorServico = null;
            System.out.println("Recolhendo funcionários - servicoParaAssociar agora é null");
        } else {
            // Carrega funcionários do novo serviço
            loadFuncionariosPorServico(servicoId);
            System.out.println("Expandindo funcionários - funcionários carregados: " + 
                (funcionariosPorServico != null ? funcionariosPorServico.size() : 0));
        }
    }

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
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Serviço não encontrado!", null));
                }
            } catch (Exception ex) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar serviço: " + ex.getMessage(), null));
            }
        } else {
            editMode = false;
            servico = new Servico();
        }
    }

    public String saveAndReturn() {
        String result = save();
        if (result == null) { // Se save() retornou null, significa sucesso
            // Verifica se não há mensagens de erro
            boolean hasErrors = FacesContext.getCurrentInstance()
                .getMessageList()
                .stream()
                .anyMatch(msg -> msg.getSeverity().equals(FacesMessage.SEVERITY_ERROR));
                
            if (!hasErrors) {
                return "/app/servico/gerenciar_servicos.xhtml?faces-redirect=true";
            }
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

    public boolean hasValidFuncionariosFor(Long servicoId) {
        System.out.println("=== DEBUG hasValidFuncionariosFor(" + servicoId + ") ===");
        System.out.println("servicoAtualFuncionarios: " + servicoAtualFuncionarios);
        System.out.println("funcionariosPorServico != null: " + (funcionariosPorServico != null));
        System.out.println("!funcionariosPorServico.isEmpty(): " + (funcionariosPorServico != null && !funcionariosPorServico.isEmpty()));
        
        boolean result = servicoAtualFuncionarios != null && 
                        servicoAtualFuncionarios.equals(servicoId) && 
                        funcionariosPorServico != null && 
                        !funcionariosPorServico.isEmpty();
        
        System.out.println("Resultado: " + result);
        System.out.println("=== FIM DEBUG hasValidFuncionariosFor ===");
        return result;
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

    public Long getServicoAtualFuncionarios() {
        return servicoAtualFuncionarios;
    }

    public void setServicoAtualFuncionarios(Long servicoAtualFuncionarios) {
        this.servicoAtualFuncionarios = servicoAtualFuncionarios;
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