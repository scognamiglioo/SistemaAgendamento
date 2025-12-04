package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.User;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.services.DataServiceLocal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.*;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped

public class CpfIdentityStore implements IdentityStore {

    @PersistenceContext(unitName = "SecureAppPU")
    private EntityManager em;

    @Inject
    private PasswordHash passwordHash;

    @Inject
    private DataServiceLocal dataService; 

    @Override
    public CredentialValidationResult validate(Credential credential) {

        UsernamePasswordCredential login =
                (UsernamePasswordCredential) credential;

        String input = login.getCaller();
        String password = login.getPasswordAsString();

        
        // se o usuário digitou CPF → converte para username
        
        if (input.matches("\\d{11}")) {
            String usernameFound = dataService.getUsernameByCpf(input);

            if (usernameFound == null) {
                return CredentialValidationResult.INVALID_RESULT;
            }

            input = usernameFound;
        }

        
        // procura User ou Funcionario pelo username obtido
        
        AuthRecord record = findByUsername(input);

        if (record == null) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        
        // verifica senha
        
        if (!passwordHash.verify(password.toCharArray(), record.passwordHash)) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        return new CredentialValidationResult(record.username, record.roles);
    }

    private AuthRecord findByUsername(String username) {

    
    // buscar user
    
    try {
        User u = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getSingleResult();

        Set<String> roles = new HashSet<>();
        roles.add(u.getUserGroup()); 

        return new AuthRecord(u.getUsername(), u.getPassword(), roles);

    } catch (NoResultException ignored) {}


    
    // buscar funcionario
    
    try {
        Funcionario f = em.createQuery(
                "SELECT f FROM Funcionario f WHERE f.username = :username", Funcionario.class)
                .setParameter("username", username)
                .getSingleResult();

        Set<String> roles = new HashSet<>();
        roles.add(f.getRole().name()); // "admin", "funcionario"

        return new AuthRecord(f.getUsername(), f.getPassword(), roles);

    } catch (NoResultException ignored) {}


    
    return null;
}


    record AuthRecord(String username, String passwordHash, Set<String> roles) {}
}


