package io.github.scognamiglioo.controllers;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;

@Named
@SessionScoped
public class UserSessionController implements Serializable {

    public String logout() throws IOException {
        // Invalida a sessão atual
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        
        // Redireciona para a página de login
        return "/login.xhtml?faces-redirect=true";
    }
    
    public String getCurrentUser() {
        return FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
    }
    
    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }
}