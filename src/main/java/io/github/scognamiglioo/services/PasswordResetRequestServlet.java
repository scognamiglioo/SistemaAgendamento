/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package io.github.scognamiglioo.services;

import io.github.scognamiglioo.services.DataServiceLocal;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PasswordResetRequestServlet", urlPatterns = {"/request-reset"})
public class PasswordResetRequestServlet extends HttpServlet {

    @Inject
    private DataServiceLocal dataService;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String email = request.getParameter("email");
        if (email == null || email.isBlank()) {
            response.sendRedirect("request_reset_error.xhtml");
            return;
        }

        boolean ok = dataService.requestPasswordReset(email);

        if (ok) {
            response.sendRedirect("login.xhtml");
        } else {
            response.sendRedirect("request_reset_error.xhtml");
        }
    }
}
