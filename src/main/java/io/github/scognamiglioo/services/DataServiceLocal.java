package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Funcionario;
import io.github.scognamiglioo.entities.Guiche;
import io.github.scognamiglioo.entities.Role;
import io.github.scognamiglioo.entities.User;
import java.util.List;
import java.util.Optional;
import jakarta.ejb.Local;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Local
public interface DataServiceLocal {

    public User createUser(String nome, String cpf, String email, String telefone, String username, String userPassword, String userGroup);

    public User createInitialUser(String nome, String cpf, String email, String telefone, String username, String userPassword, String userGroup);

    Guiche createGuiche(String nome);

    public List<User> getAllUsers();

    public Optional<User> getUser(String username);

    public String getUsernameByCpf(String cpf);

    User getUserByToken(String token);

    public boolean canActivate(String token);

    public boolean activateUser(String token);

    boolean isUserActive(String email);

    public boolean requestPasswordReset(String email);

    public boolean resetPassword(String token, String newPassword);

    public boolean validateResetToken(String token);

    Funcionario createFuncionario(String nome, String cpf, String email, String telefone,
            String username, String plainPassword, Role role, Long guicheId, boolean ativo);

    List<Guiche> listGuiches();

    Guiche findGuicheById(Long id);

    List<Funcionario> listEmployees();

    boolean employeeCpfExists(String cpf);

    boolean employeeUsernameExists(String username);

    User findUserByUsername(String username);

    public boolean cpfExists(String cpfNorm);

    // -------------------------------------------------------
    List<Funcionario> getAllFuncionarios();

    void updateUser(User u);

    void updateFuncionario(Funcionario f);

    User findUserById(Long id);

    Funcionario findFuncionarioById(Long  id);

}
