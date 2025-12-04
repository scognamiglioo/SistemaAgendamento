package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.User;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.services.DataServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("adminUserBean")
@ViewScoped
public class AdminUserController implements Serializable {

    @Inject
    private DataServiceLocal dataService;

    private List<User> users;
    private List<Funcionario> funcionarios;

    // campos para edição (carregados pela página edit-user.xhtml)
    private Long editId;
    private String editType; // "user" ou "funcionario"

    private User selectedUser;
    private Funcionario selectedFuncionario;

    @PostConstruct
    public void init() {
        loadAll();
    }

    public void loadAll() {
        users = dataService.getAllUsers();
        funcionarios = dataService.getAllFuncionarios();
    }

    // --- getters / setters ---
    public List<User> getUsers() { return users; }
    public List<Funcionario> getFuncionarios() { return funcionarios; }

    public Long getEditId() { return editId; }
    public void setEditId(Long editId) { this.editId = editId; }

    public String getEditType() { return editType; }
    public void setEditType(String editType) { this.editType = editType; }

    public User getSelectedUser() { return selectedUser; }
    public void setSelectedUser(User selectedUser) { this.selectedUser = selectedUser; }

    public Funcionario getSelectedFuncionario() { return selectedFuncionario; }
    public void setSelectedFuncionario(Funcionario selectedFuncionario) { this.selectedFuncionario = selectedFuncionario; }

    // Chamado via <f:viewParam> ou link para carregar a entidade para edição
    public void loadEntityForEdit() {
        if (editId == null || editType == null) return;

        if ("user".equals(editType)) {
            selectedUser = dataService.findUserById(editId);
        } else if ("funcionario".equals(editType)) {
            selectedFuncionario = dataService.findFuncionarioById(editId);
        }
    }

    // Salvar edições (invocado do edit-user.xhtml)
    public String save() {
        if ("user".equals(editType) && selectedUser != null) {
            dataService.updateUser(selectedUser);
        } else if ("funcionario".equals(editType) && selectedFuncionario != null) {
            dataService.updateFuncionario(selectedFuncionario);
        }

        // recarrega listas
        loadAll();

        // redireciona de volta para a lista
        return "/app/user-list.xhtml?faces-redirect=true";
    }
}
