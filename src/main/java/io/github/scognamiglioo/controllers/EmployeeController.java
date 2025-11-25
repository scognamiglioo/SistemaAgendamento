package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Guiche;
import io.github.scognamiglioo.entities.Role;
import io.github.scognamiglioo.services.DataServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class EmployeeController implements Serializable {

    @EJB
    private DataServiceLocal dataService;

    private Funcionario employee = new Funcionario();
    private List<Guiche> guiches;
    private Long selectedGuicheId;
    private Role selectedRole;

    @PostConstruct
    public void init() {
        guiches = dataService.listGuiches();
    }

    public List<Guiche> getGuiches() {
        return guiches;
    }

    public Funcionario getEmployee() {
        return employee;
    }

    public void setEmployee(Funcionario e) {
        this.employee = e;
    }

    public Long getSelectedGuicheId() {
        return selectedGuicheId;
    }

    public void setSelectedGuicheId(Long id) {
        this.selectedGuicheId = id;
    }

    public Role getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(Role selectedRole) { this.selectedRole = selectedRole; }

    public Role[] getRoles() {
        return Role.values();
    }

    public String save() {
        try {
            employee.setRole(selectedRole);

            dataService.createFuncionario(
                    employee.getNome(),
                    employee.getCpf(),
                    employee.getEmail(),
                    employee.getTelefone(),
                    employee.getUsername(),
                    employee.getPassword(),
                    selectedRole,
                    selectedGuicheId,
                    employee.isAtivo()
            );

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Funcionário criado", null));

            return "/app/admin.xhtml?faces-redirect=true";

        } catch (IllegalArgumentException ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
            return null;
        }
    }

}
