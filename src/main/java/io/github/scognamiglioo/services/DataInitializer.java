package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Localizacao;
import io.github.scognamiglioo.entities.Servico;
import io.github.scognamiglioo.entities.User;
import io.github.scognamiglioo.entities.Cargo;
import io.github.scognamiglioo.entities.Role;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Singleton
@jakarta.ejb.Startup
@ApplicationScoped
public class DataInitializer {

    @EJB
    private DataServiceLocal dataService;
    
    @EJB
    private ServicoServiceLocal servicoService;
    
    @EJB
    private CargoServiceLocal cargoService;
    
    @EJB
    private LocalizacaoServiceLocal localizacaoService;
    
    @EJB
    private FuncionarioServicoServiceLocal funcionarioServicoService;
   
    @EJB
    private AgendamentoServiceLocal agendamentoService;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void execute(@Observes @Initialized(ApplicationScoped.class) Object event) {
        
        // Criar localizações iniciais
        if (localizacaoService.getAllLocalizacoes().isEmpty()) {
            
            localizacaoService.createLocalizacao("Consultório Odontológico", "Sala equipada para atendimentos odontológicos");
            localizacaoService.createLocalizacao("Mesa de Recepção", "Local de atendimento inicial e cadastro de pacientes");
            localizacaoService.createLocalizacao("Guichê 1", "Primeiro guichê de atendimento");
            localizacaoService.createLocalizacao("Guichê 2", "Segundo guichê de atendimento");
            localizacaoService.createLocalizacao("Sala de Exames", "Sala para realização de exames clínicos");
            localizacaoService.createLocalizacao("Laboratório", "Local para coleta de exames laboratoriais");
            localizacaoService.createLocalizacao("Sala de Raio-X", "Sala equipada com aparelho de Raio-X");
            localizacaoService.createLocalizacao("Consultório Cardiológico", "Sala especializada em cardiologia");
            localizacaoService.createLocalizacao("Consultório Dermatológico", "Sala para consultas dermatológicas");
            localizacaoService.createLocalizacao("Sala de Vacinação", "Local específico para aplicação de vacinas");
            localizacaoService.createLocalizacao("Consultório Pediátrico", "Sala especializada em pediatria");
            localizacaoService.createLocalizacao("Posto de Enfermagem", "Central de procedimentos de enfermagem");
            
            System.out.println(">>> Localizações iniciais criadas.");
        }
        
        // Criar cargos iniciais
        if (cargoService.getAllCargos().isEmpty()) {
            
            cargoService.createCargo("Médico Clínico Geral");
            cargoService.createCargo("Cardiologista");
            cargoService.createCargo("Dermatologista");
            cargoService.createCargo("Pediatra");
            cargoService.createCargo("Radiologista");
            cargoService.createCargo("Oftalmologista");
            cargoService.createCargo("Ortopedista");
            cargoService.createCargo("Ginecologista");
            cargoService.createCargo("Neurologista");
            cargoService.createCargo("Enfermeiro");
            cargoService.createCargo("Técnico em Enfermagem");
            cargoService.createCargo("Fisioterapeuta");
            cargoService.createCargo("Farmacêutico");
            cargoService.createCargo("Nutricionista");
            cargoService.createCargo("Psicólogo");
            cargoService.createCargo("Biomédico");
            cargoService.createCargo("Técnico em Radiologia");
            
            System.out.println(">>> Cargos iniciais criados.");
        }
        
        
        if (dataService.getAllUsers().isEmpty()) {

            User guisso = dataService.createInitialUser(
                    "Luis Guisso", // nome
                    "00000000188", // cpf
                    "guisso@example.com",// email
                    "11999990001", // telefone
                    "guisso", // username
                    "asdf", // password
                    "user" // group
                    
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
            
            // Buscar cargos para associar aos funcionários (apenas cargos técnicos/médicos)
            Cargo cargoMedicoGeral = cargoService.findCargoByNome("Médico Clínico Geral");
            Cargo cargoCardiologista = cargoService.findCargoByNome("Cardiologista");
            Cargo cargoDermatologista = cargoService.findCargoByNome("Dermatologista");
            Cargo cargoPediatra = cargoService.findCargoByNome("Pediatra");
            Cargo cargoRadiologista = cargoService.findCargoByNome("Radiologista");
            Cargo cargoEnfermeiro = cargoService.findCargoByNome("Enfermeiro");
            
            // Funcionários administrativos (sem cargo específico - definido pelo Role)
            Funcionario maria = dataService.createFuncionario(
                "Maria Silva Santos",
                "00000000191",
                "maria.santos@clinica.com",
                "11987654321",
                "maria.santos",
                "asdf",
                Role.admin,
                null, // Admin não precisa de cargo específico
                true,
                new ArrayList<>() // Lista vazia de serviços
            );
            
            // Recepcionistas (sem cargo específico - definido pelo Role)
            Funcionario ana = dataService.createFuncionario(
                "Ana Paula Costa",
                "23456789012", 
                "ana.costa@clinica.com",
                "11987654322",
                "ana.costa",
                "senha123",
                Role.recepcionista,
                null, // Recepcionista não precisa de cargo específico
                true,
                new ArrayList<>() // Lista vazia de serviços
            );
            
            Funcionario carlos = dataService.createFuncionario(
                "Carlos Eduardo Lima",
                "34567890123",
                "carlos.lima@clinica.com",
                "11987654323",
                "carlos.lima",
                "senha123",
                Role.recepcionista,
                null, // Recepcionista não precisa de cargo específico
                true,
                new ArrayList<>()
            );
            
            // Médicos/Atendentes (com cargos específicos)
            Funcionario drRoberto = dataService.createFuncionario(
                "Dr. Roberto Almeida",
                "45678901234",
                "roberto.almeida@clinica.com",
                "11987654324", 
                "roberto.almeida",
                "senha123",
                Role.atendente,
                cargoMedicoGeral != null ? cargoMedicoGeral.getId() : null,
                true,
                new ArrayList<>()
            );
            
            Funcionario draPatricia = dataService.createFuncionario(
                "Dra. Patricia Fernandes",
                "56789012345",
                "patricia.fernandes@clinica.com",
                "11987654325",
                "patricia.fernandes", 
                "senha123",
                Role.atendente,
                cargoCardiologista != null ? cargoCardiologista.getId() : null,
                true,
                new ArrayList<>()
            );
            
            Funcionario drJoao = dataService.createFuncionario(
                "Dr. João Oliveira",
                "67890123456",
                "joao.oliveira@clinica.com",
                "11987654326",
                "joao.oliveira",
                "senha123", 
                Role.atendente,
                cargoRadiologista != null ? cargoRadiologista.getId() : null,
                true,
                new ArrayList<>()
            );
            
            Funcionario draFernanda = dataService.createFuncionario(
                "Dra. Fernanda Souza",
                "78901234567",
                "fernanda.souza@clinica.com",
                "11987654327",
                "fernanda.souza",
                "senha123",
                Role.atendente,
                cargoDermatologista != null ? cargoDermatologista.getId() : null,
                true,
                new ArrayList<>()
            );
            
            Funcionario enfLucas = dataService.createFuncionario(
                "Enfermeiro Lucas Santos",
                "89012345678", 
                "lucas.santos@clinica.com",
                "11987654328",
                "lucas.santos",
                "senha123",
                Role.atendente,
                cargoEnfermeiro != null ? cargoEnfermeiro.getId() : null,
                true,
                new ArrayList<>()
            );
            
            System.out.println(">>> Funcionários iniciais criados com cargos associados.");
        }
        
        // Associar funcionários aos serviços com localizações (nova estrutura FuncionarioServico)
        if (dataService.getAllFuncionarios().size() > 0 && servicoService.getAllServicos().size() > 0 && localizacaoService.getAllLocalizacoes().size() > 0) {
            
            try {
                // Buscar funcionários, serviços e localizações
                var funcionarios = dataService.getAllFuncionarios();
                var servicos = servicoService.getAllServicos();
                var localizacoes = localizacaoService.getAllLocalizacoes();
                
                // Encontrar médicos/atendentes
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
                    
                var limpezaDentaria = servicos.stream()
                    .filter(s -> s.getNome().contains("Limpeza Dentária"))
                    .findFirst().orElse(null);
                
                // Encontrar localizações específicas
                var consultorioOdonto = localizacoes.stream()
                    .filter(l -> l.getNome().contains("Consultório Odontológico"))
                    .findFirst().orElse(null);
                    
                var salaExames = localizacoes.stream()
                    .filter(l -> l.getNome().contains("Sala de Exames"))
                    .findFirst().orElse(null);
                    
                var laboratorio = localizacoes.stream()
                    .filter(l -> l.getNome().contains("Laboratório"))
                    .findFirst().orElse(null);
                    
                var salaRaioX = localizacoes.stream()
                    .filter(l -> l.getNome().contains("Sala de Raio-X"))
                    .findFirst().orElse(null);
                    
                var consultorioCardio = localizacoes.stream()
                    .filter(l -> l.getNome().contains("Consultório Cardiológico"))
                    .findFirst().orElse(null);
                    
                var consultorioDermo = localizacoes.stream()
                    .filter(l -> l.getNome().contains("Consultório Dermatológico"))
                    .findFirst().orElse(null);
                    
                var salaVacinacao = localizacoes.stream()
                    .filter(l -> l.getNome().contains("Sala de Vacinação"))
                    .findFirst().orElse(null);
                    
                var consultorioPediatrico = localizacoes.stream()
                    .filter(l -> l.getNome().contains("Consultório Pediátrico"))
                    .findFirst().orElse(null);
                    
                var postoEnfermagem = localizacoes.stream()
                    .filter(l -> l.getNome().contains("Posto de Enfermagem"))
                    .findFirst().orElse(null);
                
                // Criar associações funcionário-serviço-localização usando o novo service
                
                // Dr. Roberto (Clínico Geral) - consultas gerais e exames básicos
                if (drRoberto != null) {
                    if (consultaMedica != null && salaExames != null) {
                        funcionarioServicoService.createAssociacao(drRoberto.getId(), consultaMedica.getId(), salaExames.getId());
                    }
                    if (examesSangue != null && laboratorio != null) {
                        funcionarioServicoService.createAssociacao(drRoberto.getId(), examesSangue.getId(), laboratorio.getId());
                    }
                }
                
                // Dra. Patricia (Cardiologista) - especializada em cardiologia
                if (draPatricia != null) {
                    if (consultaCardio != null && consultorioCardio != null) {
                        funcionarioServicoService.createAssociacao(draPatricia.getId(), consultaCardio.getId(), consultorioCardio.getId());
                    }
                    if (consultaMedica != null && consultorioCardio != null) {
                        funcionarioServicoService.createAssociacao(draPatricia.getId(), consultaMedica.getId(), consultorioCardio.getId());
                    }
                }
                
                // Dr. João (Radiologista) - exames de imagem
                if (drJoao != null) {
                    if (raioX != null && salaRaioX != null) {
                        funcionarioServicoService.createAssociacao(drJoao.getId(), raioX.getId(), salaRaioX.getId());
                    }
                }
                
                // Dra. Fernanda (Dermatologista + Pediatra)
                if (draFernanda != null) {
                    if (consultaDermo != null && consultorioDermo != null) {
                        funcionarioServicoService.createAssociacao(draFernanda.getId(), consultaDermo.getId(), consultorioDermo.getId());
                    }
                    if (consultaPediatrica != null && consultorioPediatrico != null) {
                        funcionarioServicoService.createAssociacao(draFernanda.getId(), consultaPediatrica.getId(), consultorioPediatrico.getId());
                    }
                }
                
                // Enfermeiro Lucas - vacinação e procedimentos de enfermagem
                if (enfLucas != null) {
                    if (vacinacao != null && salaVacinacao != null) {
                        funcionarioServicoService.createAssociacao(enfLucas.getId(), vacinacao.getId(), salaVacinacao.getId());
                    }
                    if (examesSangue != null && postoEnfermagem != null) {
                        funcionarioServicoService.createAssociacao(enfLucas.getId(), examesSangue.getId(), postoEnfermagem.getId());
                    }
                }
                
                // Adicionar associação odontológica se houver dentista ou serviço odontológico
                if (limpezaDentaria != null && consultorioOdonto != null && drRoberto != null) {
                    funcionarioServicoService.createAssociacao(drRoberto.getId(), limpezaDentaria.getId(), consultorioOdonto.getId());
                }
                
                System.out.println(">>> Associações funcionário-serviço-localização criadas com nova estrutura.");
                
            } catch (Exception e) {
                System.err.println(">>> Erro ao criar associações funcionário-serviço-localização: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Criar agendamentos de exemplo para o usuário guisso
        if (dataService.getAllUsers().size() > 0 && servicoService.getAllServicos().size() > 0) {
            try {
                // Buscar usuário guisso
                User guisso = dataService.getAllUsers().stream()
                    .filter(u -> u.getUsername().equals("guisso"))
                    .findFirst().orElse(null);

                if (guisso != null) {
                    var funcionarios = dataService.getAllFuncionarios();
                    var servicos = servicoService.getAllServicos();

                    // Buscar funcionários
                    Funcionario drRoberto = funcionarios.stream()
                        .filter(f -> f.getNome().contains("Roberto Almeida"))
                        .findFirst().orElse(null);

                    Funcionario draPatricia = funcionarios.stream()
                        .filter(f -> f.getNome().contains("Patricia Fernandes"))
                        .findFirst().orElse(null);

                    Funcionario draFernanda = funcionarios.stream()
                        .filter(f -> f.getNome().contains("Fernanda Souza"))
                        .findFirst().orElse(null);

                    // Buscar serviços
                    var consultaMedica = servicos.stream()
                        .filter(s -> s.getNome().contains("Consulta Médica Geral"))
                        .findFirst().orElse(null);

                    var consultaCardio = servicos.stream()
                        .filter(s -> s.getNome().contains("Consulta Cardiológica"))
                        .findFirst().orElse(null);

                    var consultaDermo = servicos.stream()
                        .filter(s -> s.getNome().contains("Consulta Dermatológica"))
                        .findFirst().orElse(null);

                    // Datas e horários
                    LocalDate hoje = LocalDate.now();
                    LocalTime hora1 = LocalTime.of(14, 0); // 14:00
                    LocalTime hora2 = LocalTime.of(15, 30); // 15:30
                    LocalTime hora3 = LocalTime.of(10, 0); // 10:00

                    LocalDate amanha = hoje.plusDays(1);
                    LocalDate doisDias = hoje.plusDays(2);

                    // Agendamento 1: Status AGENDADO para hoje
                    if (consultaMedica != null && drRoberto != null) {
                        io.github.scognamiglioo.entities.Agendamento ag1 =
                            agendamentoService.createAgendamento(guisso, consultaMedica, drRoberto, hoje, hora1);
                        ag1.setObservacoes("Consulta de rotina");
                        agendamentoService.updateAgendamento(ag1);
                        System.out.println(">>> Agendamento 1 criado: AGENDADO (Consulta Médica Geral)");
                    }

                    // Agendamento 2: Status CONFIRMADO para amanhã
                    if (consultaCardio != null && draPatricia != null) {
                        io.github.scognamiglioo.entities.Agendamento ag2 =
                            agendamentoService.createAgendamento(guisso, consultaCardio, draPatricia, amanha, hora2);
                        agendamentoService.alterarStatus(ag2.getId(), io.github.scognamiglioo.entities.StatusAgendamento.CONFIRMADO);
                        ag2.setObservacoes("Retorno cardiológico");
                        agendamentoService.updateAgendamento(ag2);
                        System.out.println(">>> Agendamento 2 criado: CONFIRMADO (Consulta Cardiológica)");
                    }

                    // Agendamento 3: Status EM_ATENDIMENTO para daqui a 2 dias
                    if (consultaDermo != null && draFernanda != null) {
                        io.github.scognamiglioo.entities.Agendamento ag3 =
                            agendamentoService.createAgendamento(guisso, consultaDermo, draFernanda, doisDias, hora3);
                        agendamentoService.alterarStatus(ag3.getId(), io.github.scognamiglioo.entities.StatusAgendamento.EM_ATENDIMENTO);
                        ag3.setObservacoes("Avaliação dermatológica");
                        agendamentoService.updateAgendamento(ag3);
                        System.out.println(">>> Agendamento 3 criado: EM_ATENDIMENTO (Consulta Dermatológica)");
                    }

                    System.out.println(">>> 3 agendamentos de exemplo criados para o usuário guisso.");
                }

            } catch (Exception e) {
                System.err.println(">>> Erro ao criar agendamentos de exemplo: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
