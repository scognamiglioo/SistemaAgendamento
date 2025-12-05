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

@WebServlet(name = "PasswordReset", urlPatterns = {"/reset-password"})
public class PasswordResetServlet extends HttpServlet {

    @Inject
    private DataServiceLocal dataService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String token = request.getParameter("token");

        if (token == null || !dataService.validateResetToken(token)) {
            response.sendRedirect("forbidden.xhtml");
        } else {
            request.setAttribute("token", token);
            request.getRequestDispatcher("/reset_password.xhtml").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String token = request.getParameter("token");
        String newPassword = request.getParameter("password");

        boolean ok = dataService.resetPassword(token, newPassword);

        if (ok) {
            response.sendRedirect("login.xhtml");
        } else {
            response.sendRedirect("forbidden.xhtml");
        }
    }
}

