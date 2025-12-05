package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.entities.User;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import io.github.scognamiglioo.services.DataServiceLocal;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;


@Named
@RequestScoped
public class NewUserController {

    @EJB
    private DataServiceLocal dataService;

    private User user;
    private List<String> qualities;

   
    public NewUserController() {
        user = new User();
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<String> getQualities() {
        return qualities;
    }

    public void setQualities(List<String> qualities) {
        this.qualities = qualities;

    }
    //</editor-fold>

    public String save() {
    try {
        user.setUserGroup("user");

        user = dataService.createUser(
                
                user.getNome(),
                user.getCpf(),
                user.getEmail(),
                user.getTelefone(),
                user.getUsername(),
                user.getUserPassword(),
                user.getUserGroup()
        );

        return "/redirecionamento?faces-redirect=true";

    } catch (IllegalArgumentException e) {
        FacesContext.getCurrentInstance().addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null)
        );
        return null; // permanece na mesma página
    }
}
    
    public void checkCpf() {
        String cpfNorm = user.getCpf() == null ? "" : user.getCpf().replaceAll("\\D+", "");
        if (!cpfNorm.isEmpty() && dataService.cpfExists(cpfNorm)) {
            FacesContext.getCurrentInstance().addMessage("createAccountForm:cpf",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "CPF já cadastrado", null));
        }
    }

}
