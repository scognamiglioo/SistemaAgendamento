/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/SessionLocal.java to edit this template
 */
package io.github.scognamiglioo.services;

import jakarta.ejb.Local;
import jakarta.mail.MessagingException;


@Local
public interface MailServiceLocal {

    public void sendMail(String to, String name, String key)
            throws MessagingException;

}
