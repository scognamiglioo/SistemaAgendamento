package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Cargo;
import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Role;
import io.github.scognamiglioo.entities.User;
import jakarta.ejb.LocalBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.time.LocalDateTime;
import java.util.UUID;

@Stateless
@LocalBean
public class DataService
        implements DataServiceLocal {

    @PersistenceContext(unitName = "SecureAppPU")
    private EntityManager em;

    @Inject
    private Pbkdf2PasswordHash passwordHasher;

    @Inject
    private MailServiceLocal mailService;

    @Inject
    private MailServiceResetLocal mailServiceReset;

    @Override
    public User createUser(String nome, String cpf, String email, String telefone,
            String username, String userPassword, String userGroup) {

        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.cpf = :cpf", Long.class
        );
        query.setParameter("cpf", cpf);

        if (query.getSingleResult() > 0) {
            throw new IllegalArgumentException("O CPF informado já está cadastrado.");
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("Pbkdf2PasswordHash.Iterations", "3071");
        parameters.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacSHA512");
        parameters.put("Pbkdf2PasswordHash.SaltSizeBytes", "64");
        passwordHasher.initialize(parameters);

        String hashedPassword = passwordHasher.generate(userPassword.toCharArray());

        String token = UUID.randomUUID().toString();

        User newUser = new User(
                nome,
                cpf,
                email,
                telefone,
                username,
                hashedPassword,
                userGroup
        );

        newUser.setActivationToken(token);
        newUser.setActive(false);

        em.persist(newUser);

        // Envia e-mail de ativação
        String link = "http://localhost:8080/secureapp/activation?token=" + token;

        try {
            mailService.sendMail(nome, email, link);
        } catch (MessagingException ex) {
            System.getLogger(DataService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        return newUser;

    }

    @Override
    public User createInitialUser(String nome, String cpf, String email, String telefone,
            String username, String userPassword, String userGroup) {

        // cria o user normal
        User u = createUser(nome, cpf, email, telefone, username, userPassword, userGroup);

        // força ativação
        u.setActive(true);
        u.setActivationToken(null);

        em.merge(u);  // salva no DB

        return u;
    }

    // ----------------------------------MÉTODOS DE GET--------------------------------------------
    @Override
    public List<User> getAllUsers() {
        return em.createNamedQuery("User.all", User.class).getResultList();
    }

    @Override
    public Optional<User> getUser(String username) {
        return em.createNamedQuery("User.byUsername", User.class)
                .setParameter("username", username)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public String getUsernameByCpf(String cpf) {

        try {
            User u = em.createQuery(
                    "SELECT u FROM User u WHERE u.cpf = :cpf", User.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();

            return u.getUsername();
        } catch (Exception ignored) {
        }

        try {
            Funcionario f = em.createQuery(
                    "SELECT f FROM Funcionario f WHERE f.cpf = :cpf", Funcionario.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();

            return f.getUsername();
        } catch (Exception ignored) {
        }

        return null;
    }

    // ----------------------------------TOKEN E ATIVAÇÃO--------------------------------------------
    @Override
    public User getUserByToken(String token) {
        try {
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.activationToken = :token",
                    User.class
            )
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public boolean activateUser(String token) {

        try {
            User user = em.createQuery(
                    "SELECT u FROM User u WHERE u.activationToken = :token AND u.active = false",
                    User.class
            )
                    .setParameter("token", token)
                    .getSingleResult();

            user.setActive(true);
            user.setActivationToken(null);

            em.merge(user);
            em.flush();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canActivate(String token) {
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.activationToken = :token AND u.active = false",
                    Long.class
            )
                    .setParameter("token", token)
                    .getSingleResult();

            return count == 1;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isUserActive(String username) {

        // tenta na tabela User
        try {
            Boolean result = em.createQuery(
                    "SELECT u.active FROM User u WHERE u.username = :username",
                    Boolean.class
            )
                    .setParameter("username", username)
                    .getSingleResult();

            if (result != null) {
                return result;
            }

        } catch (Exception ignored) {
        }

        // tenta na tabela Funcionario
        try {
            Boolean result2 = em.createQuery(
                    "SELECT f.ativo FROM Funcionario f WHERE f.username = :username",
                    Boolean.class
            )
                    .setParameter("username", username)
                    .getSingleResult();

            if (result2 != null) {
                return result2;
            }

        } catch (Exception ignored) {
        }

        return false;
    }

// ----------------------------------RECUPERAR SENHA------------------------------------------------------------------------
    @Override
    public boolean requestPasswordReset(String email) {

        try {
            User user = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email",
                    User.class)
                    .setParameter("email", email)
                    .getSingleResult();

            String token = UUID.randomUUID().toString();
            LocalDateTime expiration = LocalDateTime.now().plusHours(1);

            user.setResetToken(token);
            user.setResetTokenExpiration(expiration);

            em.merge(user);

            String link = "http://localhost:8080/secureapp/reset-password?token=" + token;

            mailServiceReset.sendMail(
                    user.getNome(),
                    user.getEmail(),
                    link
            );

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean validateResetToken(String token) {
        try {
            User user = em.createQuery(
                    "SELECT u FROM User u WHERE u.resetToken = :token",
                    User.class)
                    .setParameter("token", token)
                    .getSingleResult();

            return user.getResetTokenExpiration().isAfter(LocalDateTime.now());

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {

        try {
            User user = em.createQuery(
                    "SELECT u FROM User u WHERE u.resetToken = :token",
                    User.class)
                    .setParameter("token", token)
                    .getSingleResult();

            if (user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
                return false; // token expirado
            }

            // gerar hash
            Map<String, String> parameters = new HashMap<>();
            parameters.put("Pbkdf2PasswordHash.Iterations", "3071");
            parameters.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacSHA512");
            parameters.put("Pbkdf2PasswordHash.SaltSizeBytes", "64");
            passwordHasher.initialize(parameters);

            String hashed = passwordHasher.generate(newPassword.toCharArray());
            user.setUserPassword(hashed);

            // limpar token
            user.setResetToken(null);
            user.setResetTokenExpiration(null);

            em.merge(user);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

// ----------------------------------------------------------------------------------------------------------
    @Override
    public User findUserByUsername(String username) {
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean cpfExists(String cpf) {

        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.cpf = :cpf", Long.class);
        q.setParameter("cpf", cpf);
        Long count = q.getSingleResult();
        return count != null && count > 0;
    }

    // ----------------------------------FUNCIONARIO CADASTRO--------------------------------------------
    @Override
    public Funcionario createFuncionario(String nome, String cpf, String email, String telefone,
            String username, String password, Role role,
            Long cargoId, boolean ativo,
            List<Long> servicosIds) {

        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF obrigatório");
        }
        // agora checamos no User (composição: dados únicos estão em User)
        if (cpfExists(cpf)) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }
        if (findUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username já cadastrado");
        }

        // hash da senha
        Map<String, String> parameters = new HashMap<>();
        parameters.put("Pbkdf2PasswordHash.Iterations", "3071");
        parameters.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacSHA512");
        parameters.put("Pbkdf2PasswordHash.SaltSizeBytes", "64");
        passwordHasher.initialize(parameters);
        String hashedPassword = passwordHasher.generate(password.toCharArray());

        // criar User associado (ativo por padrão para funcionários)
        String userGroup = (role != null) ? role.name() : "employee";
        User user = new User(nome, cpf, email, telefone, username, hashedPassword, userGroup);
        user.setActive(true);
        em.persist(user);

        Cargo cargo = (cargoId != null) ? em.find(Cargo.class, cargoId) : null;

        Funcionario funcionario = new Funcionario(
                user, role, cargo, ativo
        );

        em.persist(funcionario);
        em.flush(); // Garante que o ID seja gerado

        // Nota: As associações FuncionarioServico devem ser criadas separadamente
        // através do gerenciamento de associações, pois requerem Localização.
        // servicosIds aqui será ignorado - use o sistema de gerenciamento de associações.

        return funcionario;
    }

    @Override
    public boolean employeeCpfExists(String cpf) {
        // agora verifica em User (CPF único)
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.cpf = :cpf", Long.class);
        q.setParameter("cpf", cpf);
        Long count = q.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean employeeUsernameExists(String username) {
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
        q.setParameter("username", username);
        Long count = q.getSingleResult();
        return count != null && count > 0;
    }

    

    
    @Override
    public List<Funcionario> listEmployees() {
        return em.createNamedQuery("Funcionario.all", Funcionario.class).getResultList();
    }

    // ----------------------------------UPDATE FUNCIONARIO E USERS--------------------------------------------
    @Override
    public List<Funcionario> getAllFuncionarios() {
        return em.createQuery("SELECT f FROM Funcionario f", Funcionario.class)
                .getResultList();
    }

    @Override
    public User findUserById(Long id) {
        return em.find(User.class, id);
    }

    @Override
    public Funcionario findFuncionarioById(Long id) {
        return em.find(Funcionario.class, id);
    }

    @Override
    public void updateUser(User u) {
        em.merge(u);
    }

    @Override
    public void updateFuncionario(Funcionario f) {
        em.merge(f);
    }

}
