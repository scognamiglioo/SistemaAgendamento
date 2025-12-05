package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.User;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(
        name = "Activation",
        urlPatterns = {"/activation"}
)
public class ActivationServlet extends HttpServlet {

    @Inject
    private DataServiceLocal dataService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");

        if (token == null || token.isBlank()) {
            response.sendRedirect("activation_error.xhtml");
            return;
        }

        // obtém o usuário pelo token
        User user = dataService.getUserByToken(token);

        if (user == null) {
            response.sendRedirect("activation_error.xhtml");
            return;
        }

        // Ativa o usuário no banco
        boolean success = dataService.activateUser(token);

        if (success) {
            response.sendRedirect("activation_success.xhtml");
        } else {
            response.sendRedirect("activation_error.xhtml");
        }
    }
}
