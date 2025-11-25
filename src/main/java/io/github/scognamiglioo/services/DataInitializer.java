package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.User;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class DataInitializer {

    @EJB
    private DataServiceLocal dataService;

    public void execute(@Observes @Initialized(ApplicationScoped.class) Object event) {
        
         if (dataService.listGuiches().isEmpty()) {

            dataService.createGuiche("Guichê 1");
            dataService.createGuiche("Guichê 2");
            dataService.createGuiche("Guichê 3");
            dataService.createGuiche("Sala 101");
            dataService.createGuiche("Sala 102");

            System.out.println(">>> Guichês iniciais criados.");
        }
        
        
        if (dataService.getAllUsers().isEmpty()) {

            User guisso = dataService.createInitialUser(
                    "Luis Guisso", // nome
                    "00000000191", // cpf
                    "guisso@example.com",// email
                    "11999990001", // telefone
                    "guisso", // username
                    "asdf", // password
                    "admin" // group
                    
            );

            User azacchi = dataService.createInitialUser(
                    "Andrea Zacchi",
                    "00000000192",
                    "azacchi@example.com",
                    "11999990002",
                    "azacchi",
                    "asdf",
                    "funcionario"
            );

            
        }
    }

}
