package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.User;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.services.DataServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

@Named("adminUserBean")
@ViewScoped
public class AdminUserController implements Serializable {

    @Inject
    private DataServiceLocal dataService;

    private List<User> users;
    private List<Funcionario> funcionarios;
    private List<User> plainUsers;

    // campos para edição (carregados pela página edit-user.xhtml)
    private Long editId;
    private String editType; // "user" ou "funcionario"

        private User selectedUser;
    private Funcionario selectedFuncionario;

    // campos temporários para edição (padronizados para o form)
    private String editNome;
    private String editEmail;
    private String editTelefone;
    private Boolean editAtivo;
    // opcional: username/role se precisar
    private String editUsername;

    @PostConstruct
    public void init() {
        loadAll();
    }

    public void loadAll() {
        users = dataService.getAllUsers();
        funcionarios = dataService.getAllFuncionarios();

        // Monta lista de usuários "comuns" — aqueles que não estão referenciados por Funcionario.user
        Set<Long> funcionarioUserIds = new HashSet<>();
        if (funcionarios != null) {
            for (Funcionario f : funcionarios) {
                if (f != null && f.getUser() != null && f.getUser().getId() != null) {
                    funcionarioUserIds.add(f.getUser().getId());
                }
            }
        }

        plainUsers = new ArrayList<>();
        if (users != null) {
            for (User u : users) {
                if (u == null) continue;
                if (u.getId() == null || !funcionarioUserIds.contains(u.getId())) {
                    plainUsers.add(u);
                }
            }
        }
    }
    

    // --- getters / setters ---
    public List<User> getUsers() { return plainUsers; }
    public List<Funcionario> getFuncionarios() { return funcionarios; }

    public Long getEditId() { return editId; }
    public void setEditId(Long editId) { this.editId = editId; }

    public String getEditType() { return editType; }
    public void setEditType(String editType) { this.editType = editType; }

    public User getSelectedUser() { return selectedUser; }
    public void setSelectedUser(User selectedUser) { this.selectedUser = selectedUser; }

    public Funcionario getSelectedFuncionario() { return selectedFuncionario; }
    public void setSelectedFuncionario(Funcionario selectedFuncionario) { this.selectedFuncionario = selectedFuncionario; }

    public String getEditNome() { return editNome; }
    public void setEditNome(String editNome) { this.editNome = editNome; }

    public String getEditEmail() { return editEmail; }
    public void setEditEmail(String editEmail) { this.editEmail = editEmail; }

    public String getEditTelefone() { return editTelefone; }
    public void setEditTelefone(String editTelefone) { this.editTelefone = editTelefone; }

    public Boolean getEditAtivo() { return editAtivo; }
    public void setEditAtivo(Boolean editAtivo) { this.editAtivo = editAtivo; }
    
    public String getEditUsername() { return editUsername; }
    public void setEditUsername(String editUsername) { this.editUsername = editUsername; }

    public void loadEntityForEdit() {
    // Chamado via <f:viewParam> ou link para carregar a entidade para edição
    if (editId == null || editType == null) return;

        if ("user".equals(editType)) {
            selectedUser = dataService.findUserById(editId);
            selectedFuncionario = null;
        } else if ("funcionario".equals(editType)) {
            selectedFuncionario = dataService.findFuncionarioById(editId);
            selectedUser = null;
        }

        // popular campos do formulário com os dados carregados
        if (selectedUser != null) {
            editNome = selectedUser.getNome();
            editEmail = selectedUser.getEmail();
            editTelefone = selectedUser.getTelefone();
            editAtivo = selectedUser.isActive();
            editUsername = selectedUser.getUsername();
        } else if (selectedFuncionario != null) {
            // delega para user dentro de funcionario
            if (selectedFuncionario.getUser() != null) {
                editNome = selectedFuncionario.getUser().getNome();
                editEmail = selectedFuncionario.getUser().getEmail();
                editTelefone = selectedFuncionario.getUser().getTelefone();
                editAtivo = selectedFuncionario.isAtivo();
                editUsername = selectedFuncionario.getUser().getUsername();
            }
        }
    }

    // Salvar edições (invocado do edit-user.xhtml)
    public String save() {
        // aplica campos do formulário na entidade correta
        if ("user".equals(editType) && selectedUser != null) {
            selectedUser.setNome(editNome);
            selectedUser.setEmail(editEmail);
            selectedUser.setTelefone(editTelefone);
            selectedUser.setActive(editAtivo != null ? editAtivo : Boolean.TRUE);
            selectedUser.setUsername(editUsername);
            dataService.updateUser(selectedUser);
        } else if ("funcionario".equals(editType) && selectedFuncionario != null) {
            // aplica em user associado
            if (selectedFuncionario.getUser() == null) {
                // garante existência
                selectedFuncionario.setUser(new User());
            }
            selectedFuncionario.getUser().setNome(editNome);
            selectedFuncionario.getUser().setEmail(editEmail);
            selectedFuncionario.getUser().setTelefone(editTelefone);
            selectedFuncionario.setAtivo(editAtivo != null ? editAtivo : true);
            selectedFuncionario.getUser().setUsername(editUsername);
            dataService.updateFuncionario(selectedFuncionario);
        }

         // recarrega listas
         loadAll();

         // redireciona de volta para a lista
         return "/app/user-list.xhtml?faces-redirect=true";
     }
}
