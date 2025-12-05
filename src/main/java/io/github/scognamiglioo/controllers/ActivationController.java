/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.services.DataService;
import io.github.scognamiglioo.services.DataServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;

@Named
@RequestScoped
public class ActivationController {

    @Inject
    private DataServiceLocal dataService;

    @Inject
    private FacesContext facesContext;

    private String token;

    private boolean activationAllowed = false;

    @PostConstruct
    public void init() {
        
        token = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("token");

        if (token != null && !token.isBlank()) {
            activationAllowed = dataService.canActivate(token);
        }
    }

     public String activate() {
        boolean success = dataService.activateUser(token);
        activationAllowed = !success;
        return null;
    }

    // getters e setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public boolean isActivationAllowed() { return activationAllowed; }
    
      public void checkToken() {
        activationAllowed = dataService.canActivate(token);
    }

    private void addMessage(String msg) {
        FacesContext
                .getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    

    

}
