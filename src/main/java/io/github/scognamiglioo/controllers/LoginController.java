package io.github.scognamiglioo.controllers;

import io.github.scognamiglioo.services.DataServiceLocal;
import java.io.IOException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;

@Named
@RequestScoped
public class LoginController {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @Inject
    private FacesContext facesContext;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private DataServiceLocal dataService;

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    //</editor-fold>

    public void execute() throws IOException {

        
        if (username != null && username.matches("\\d{11}")) {

            String userByCpf = dataService.getUsernameByCpf(username);

            if (userByCpf != null) {
                
                username = userByCpf;
            } else {
                facesContext.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "CPF não encontrado.", null));
                return;
            }
        }

        
        switch (processAuthentication()) {

            case SEND_CONTINUE:
                facesContext.responseComplete();
                break;

            case SEND_FAILURE:
                facesContext.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Credenciais inválidas.", null));
                break;

            case SUCCESS:

                // Verifica se conta está ativa após a autenticação
                if (!isUserActive(username)) {
                    facesContext.addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Sua conta ainda não está ativada. Verifique seu e-mail.",
                                    null));

                    // Invalida sessão criada automaticamente pelo container
                    getExternalContext().invalidateSession();
                    return;
                }

                // Redireciona dependendo do papel do usuário
                redirectBasedOnRole();
                break;
        }
    }

    private boolean isUserActive(String username) {
        return dataService.isUserActive(username);
    }

    private void redirectBasedOnRole() throws IOException {
        String contextPath = getExternalContext().getRequestContextPath();

        if (securityContext.isCallerInRole("admin")) {
            getExternalContext().redirect(contextPath + "/app/admin.xhtml");
            return;
        }

        if (securityContext.isCallerInRole("recepcionista")) {
            getExternalContext().redirect(contextPath + "/app/agendamento/gerenciar_agendamentos.xhtml");
            return;
        }

        if (securityContext.isCallerInRole("atendente")) {
            getExternalContext().redirect(contextPath + "/app/atendente/disponibilidade.xhtml");
            return;
        }

        // Caso seja usuário comum
        getExternalContext().redirect(contextPath + "/app/index.xhtml");
    }


    private AuthenticationStatus processAuthentication() {
        ExternalContext ec = getExternalContext();
        return securityContext.authenticate(
                (HttpServletRequest) ec.getRequest(),
                (HttpServletResponse) ec.getResponse(),
                AuthenticationParameters.withParams().credential(
                        new UsernamePasswordCredential(username, password)));
    }

    private ExternalContext getExternalContext() {
        return facesContext.getExternalContext();
    }

    public String logout() throws IOException {
        // Invalida a sessão atual
        ExternalContext ec = getExternalContext();
        ec.invalidateSession();

        // Redireciona para a página de login
        return "/login.xhtml?faces-redirect=true";
    }
}
