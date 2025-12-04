/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package io.github.scognamiglioo.services;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import io.github.cdimascio.dotenv.Dotenv;

@Stateless
public class MailServiceReset
        implements MailServiceResetLocal {


    @Resource(name = "java:/MailGun")
    private Session mailSession;

    @Override
    public void sendMail(String name, String to, String link)
            throws MessagingException {
       
        MimeMessage mail = new MimeMessage(mailSession);
        Dotenv dotenv = Dotenv.load();

        String email = dotenv.get("EMAIL");
        try {
            //        mail.setFrom("webappactivation@outlook.com");
            mail.setFrom(
                    new InternetAddress(email,
                            "Sistema de Agendamento")
            );
        } catch (UnsupportedEncodingException ex) {
            System.getLogger(MailServiceReset.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
        mail.setSubject("Recuperação de Senha");
        mail.setRecipient(Message.RecipientType.TO,
                new InternetAddress(to));

        MimeMultipart content = new MimeMultipart();

        mail.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        mail.setSubject("Recuperação de Senha");

        MimeBodyPart body = new MimeBodyPart();
        body.setContent(String.format("""
                <html>
                    <h2>Olá, %s!</h2>
                    <p>
                        Clique no link abaixo para recuperar sua senha:
                    </p>
                    <p>
                        <a href="%s">Recuperar senha</a>
                    </p>
                </html>
                """, name, link),
                "text/html; charset=utf-8");

        content.addBodyPart(body);
        mail.setContent(content);

        Transport.send(mail);
    }
}
