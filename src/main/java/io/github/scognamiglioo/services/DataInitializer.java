package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Guiche;
import io.github.scognamiglioo.entities.Role;
import io.github.scognamiglioo.entities.User;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class DataInitializer {

    @EJB
    private DataServiceLocal dataService;
    
    @EJB
    private ServicoServiceLocal servicoService;

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

            System.out.println(">>> Usuários iniciais criados.");
        }
        
        // Criar serviços iniciais
        if (servicoService.getAllServicos().isEmpty()) {
            
            servicoService.createServico("Consulta Médica Geral", 150.00f);
            servicoService.createServico("Exame de Sangue", 80.00f);
            servicoService.createServico("Raio-X", 120.00f);
            servicoService.createServico("Ultrassonografia", 200.00f);
            servicoService.createServico("Eletrocardiograma", 60.00f);
            servicoService.createServico("Consulta Cardiológica", 300.00f);
            servicoService.createServico("Consulta Dermatológica", 250.00f);
            servicoService.createServico("Fisioterapia", 100.00f);
            servicoService.createServico("Vacinação", 45.00f);
            servicoService.createServico("Consulta Pediátrica", 180.00f);
            servicoService.createServico("Exame Oftalmológico", 120.00f);
            servicoService.createServico("Limpeza Dentária", 90.00f);
            
            System.out.println(">>> Serviços iniciais criados.");
        }
        
        // Criar funcionários iniciais
        if (dataService.getAllFuncionarios().isEmpty()) {
            
            // Buscar guichês para associar aos funcionários
            Guiche guiche1 = dataService.listGuiches().get(0); // Guichê 1
            Guiche guiche2 = dataService.listGuiches().get(1); // Guichê 2
            Guiche sala101 = dataService.listGuiches().get(3); // Sala 101
            Guiche sala102 = dataService.listGuiches().get(4); // Sala 102
            
            // Funcionários administrativos
            dataService.createFuncionario(
                "Maria Silva Santos",
                "12345678901",
                "maria.santos@clinica.com",
                "11987654321",
                "maria.santos",
                "senha123",
                Role.admin,
                null, // Admin não precisa de guichê
                true
            );
            
            // Recepcionistas
            dataService.createFuncionario(
                "Ana Paula Costa",
                "23456789012", 
                "ana.costa@clinica.com",
                "11987654322",
                "ana.costa",
                "senha123",
                Role.recepcionista,
                guiche1.getId(),
                true
            );
            
            dataService.createFuncionario(
                "Carlos Eduardo Lima",
                "34567890123",
                "carlos.lima@clinica.com", 
                "11987654323",
                "carlos.lima",
                "senha123",
                Role.recepcionista,
                guiche2.getId(),
                true
            );
            
            // Atendentes/Médicos
            dataService.createFuncionario(
                "Dr. Roberto Almeida",
                "45678901234",
                "roberto.almeida@clinica.com",
                "11987654324", 
                "roberto.almeida",
                "senha123",
                Role.atendente,
                sala101.getId(),
                true
            );
            
            dataService.createFuncionario(
                "Dra. Patricia Fernandes",
                "56789012345",
                "patricia.fernandes@clinica.com",
                "11987654325",
                "patricia.fernandes", 
                "senha123",
                Role.atendente,
                sala102.getId(),
                true
            );
            
            dataService.createFuncionario(
                "Dr. João Oliveira",
                "67890123456",
                "joao.oliveira@clinica.com",
                "11987654326",
                "joao.oliveira",
                "senha123", 
                Role.atendente,
                sala101.getId(),
                true
            );
            
            dataService.createFuncionario(
                "Dra. Fernanda Souza",
                "78901234567",
                "fernanda.souza@clinica.com",
                "11987654327",
                "fernanda.souza",
                "senha123",
                Role.atendente,
                sala102.getId(),
                true
            );
            
            dataService.createFuncionario(
                "Enfermeiro Lucas Santos",
                "89012345678", 
                "lucas.santos@clinica.com",
                "11987654328",
                "lucas.santos",
                "senha123",
                Role.atendente,
                guiche1.getId(),
                true
            );
            
            System.out.println(">>> Funcionários iniciais criados.");
        }
        
        // Associar funcionários aos serviços (relacionamento many-to-many)
        if (dataService.getAllFuncionarios().size() > 0 && servicoService.getAllServicos().size() > 0) {
            
            try {
                // Buscar alguns funcionários e serviços para criar associações
                var funcionarios = dataService.getAllFuncionarios();
                var servicos = servicoService.getAllServicos();
                
                // Encontrar médicos/atendentes (Dr. Roberto, Dra. Patricia, etc.)
                Funcionario drRoberto = funcionarios.stream()
                    .filter(f -> f.getNome().contains("Roberto Almeida"))
                    .findFirst().orElse(null);
                    
                Funcionario draPatricia = funcionarios.stream()
                    .filter(f -> f.getNome().contains("Patricia Fernandes"))
                    .findFirst().orElse(null);
                    
                Funcionario drJoao = funcionarios.stream()
                    .filter(f -> f.getNome().contains("João Oliveira"))
                    .findFirst().orElse(null);
                    
                Funcionario draFernanda = funcionarios.stream()
                    .filter(f -> f.getNome().contains("Fernanda Souza"))
                    .findFirst().orElse(null);
                    
                Funcionario enfLucas = funcionarios.stream()
                    .filter(f -> f.getNome().contains("Lucas Santos"))
                    .findFirst().orElse(null);
                
                // Encontrar serviços específicos
                var consultaMedica = servicos.stream()
                    .filter(s -> s.getNome().contains("Consulta Médica Geral"))
                    .findFirst().orElse(null);
                    
                var examesSangue = servicos.stream()
                    .filter(s -> s.getNome().contains("Exame de Sangue"))
                    .findFirst().orElse(null);
                    
                var raioX = servicos.stream()
                    .filter(s -> s.getNome().contains("Raio-X"))
                    .findFirst().orElse(null);
                    
                var consultaCardio = servicos.stream()
                    .filter(s -> s.getNome().contains("Consulta Cardiológica"))
                    .findFirst().orElse(null);
                    
                var consultaDermo = servicos.stream()
                    .filter(s -> s.getNome().contains("Consulta Dermatológica"))
                    .findFirst().orElse(null);
                    
                var vacinacao = servicos.stream()
                    .filter(s -> s.getNome().contains("Vacinação"))
                    .findFirst().orElse(null);
                    
                var consultaPediatrica = servicos.stream()
                    .filter(s -> s.getNome().contains("Consulta Pediátrica"))
                    .findFirst().orElse(null);
                
                // Criar associações lógicas
                
                // Dr. Roberto (Clínico Geral) - pode fazer consultas gerais, exames básicos
                if (drRoberto != null) {
                    if (consultaMedica != null) servicoService.associarFuncionarioAoServico(drRoberto.getId(), consultaMedica.getId());
                    if (examesSangue != null) servicoService.associarFuncionarioAoServico(drRoberto.getId(), examesSangue.getId());
                }
                
                // Dra. Patricia (Cardiologista) - especializada em cardiologia
                if (draPatricia != null) {
                    if (consultaCardio != null) servicoService.associarFuncionarioAoServico(draPatricia.getId(), consultaCardio.getId());
                    if (consultaMedica != null) servicoService.associarFuncionarioAoServico(draPatricia.getId(), consultaMedica.getId());
                }
                
                // Dr. João (Radiologista) - exames de imagem
                if (drJoao != null) {
                    if (raioX != null) servicoService.associarFuncionarioAoServico(drJoao.getId(), raioX.getId());
                }
                
                // Dra. Fernanda (Dermatologista + Pediatra)
                if (draFernanda != null) {
                    if (consultaDermo != null) servicoService.associarFuncionarioAoServico(draFernanda.getId(), consultaDermo.getId());
                    if (consultaPediatrica != null) servicoService.associarFuncionarioAoServico(draFernanda.getId(), consultaPediatrica.getId());
                }
                
                // Enfermeiro Lucas - vacinação e procedimentos básicos
                if (enfLucas != null) {
                    if (vacinacao != null) servicoService.associarFuncionarioAoServico(enfLucas.getId(), vacinacao.getId());
                    if (examesSangue != null) servicoService.associarFuncionarioAoServico(enfLucas.getId(), examesSangue.getId());
                }
                
                System.out.println(">>> Associações funcionário-serviço criadas.");
                
            } catch (Exception e) {
                System.err.println(">>> Erro ao criar associações funcionário-serviço: " + e.getMessage());
            }
        }
    }

}
