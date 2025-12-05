package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Guiche;
import io.github.scognamiglioo.entities.Role;
import io.github.scognamiglioo.entities.Cargo;
import io.github.scognamiglioo.services.DataServiceLocal;
import io.github.scognamiglioo.services.CargoServiceLocal;
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

import jakarta.inject.Inject;
import java.util.ArrayList;
import io.github.scognamiglioo.entities.Servico;
import io.github.scognamiglioo.entities.User;
import io.github.scognamiglioo.services.ServicoService;

@Named("employeeController")
@ViewScoped
public class EmployeeController implements Serializable {

    @Inject
    private DataServiceLocal dataService;

    @EJB
    private CargoServiceLocal cargoService;

    @Inject
    private ServicoService servicoService;

    private Funcionario employee;
    private Role selectedRole;

    private Long selectedGuicheId;
    private Long selectedCargoId;

    private List<Servico> servicos;
    private List<Long> selectedServicosIds;
    private List<Cargo> cargos;
    private Role[] roles = Role.values();

    public Role[] getRoles() {
        return roles;
    }

    @PostConstruct
    public void init() {
        employee = new Funcionario();
        selectedServicosIds = new ArrayList<>();
        servicos = servicoService.getAllServicos();
        cargos = cargoService.getAllCargos();
    }

    // ---------- GETTERS E SETTERS OBRIGATÓRIOS PARA JSF -----------
    public Funcionario getEmployee() {
        return employee;
    }

    public void setEmployee(Funcionario employee) {
        this.employee = employee;
    }

    public Role getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(Role selectedRole) {
        this.selectedRole = selectedRole;
    }

    public List<Servico> getServicos() {
        return servicos;
    }

    public List<Long> getSelectedServicosIds() {
        return selectedServicosIds;
    }

    public void setSelectedServicosIds(List<Long> ids) {
        this.selectedServicosIds = ids;
    }

    public Long getSelectedGuicheId() {
        return selectedGuicheId;
    }

    public void setSelectedGuicheId(Long id) {
        this.selectedGuicheId = id;
    }

    public Long getSelectedCargoId() {
        return selectedCargoId;
    }

    public void setSelectedCargoId(Long cargoId) {
        this.selectedCargoId = cargoId;
    }

    public List<Cargo> getCargos() {
        return cargos;
    }

    public void setCargos(List<Cargo> cargos) {
        this.cargos = cargos;
    }

    // ------------------- SALVAR -----------------------
    public String save() {
    try {
        
        // Salvar o Funcionario
        dataService.createFuncionario(
                employee.getNome(),
                employee.getCpf(),
                employee.getEmail(),
                employee.getTelefone(),
                employee.getUsername(),
                employee.getPassword(),
                selectedRole,
                selectedGuicheId,
                selectedCargoId,
                employee.isAtivo(),
                selectedServicosIds
        );

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Funcionário criado com sucesso!"));

        return "/app/admin.xhtml?faces-redirect=true";

    } catch (IllegalArgumentException ex) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), null));
        return null;
    }
}
}