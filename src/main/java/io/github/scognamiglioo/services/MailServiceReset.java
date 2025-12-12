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

        MimeBodyPart body = new MimeBodyPart();
        body.setContent(String.format("""
                <html>
                <head>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f5f5f5;
                            margin: 0;
                            padding: 20px;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background: white;
                            border-radius: 15px;
                            overflow: hidden;
                            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #495057 0%%, #343a40 100%%);
                            color: white;
                            padding: 30px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 700;
                        }
                        .header p {
                            margin: 10px 0 0 0;
                            opacity: 0.9;
                        }
                        .content {
                            padding: 30px;
                        }
                        .greeting {
                            color: #212529;
                            font-size: 18px;
                            margin-bottom: 20px;
                        }
                        .message {
                            background: #f8f9fa;
                            border-left: 4px solid #343a40;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 20px 0;
                            color: #495057;
                            line-height: 1.6;
                        }
                        .button-container {
                            text-align: center;
                            margin: 30px 0;
                        }
                        .btn-reset {
                            display: inline-block;
                            background: linear-gradient(135deg, #212529 0%%, #000000 100%%);
                            color: white;
                            padding: 15px 40px;
                            border-radius: 8px;
                            text-decoration: none;
                            font-weight: 700;
                            font-size: 16px;
                            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
                            transition: all 0.3s ease;
                        }
                        .btn-reset:hover {
                            box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
                            transform: translateY(-2px);
                        }
                        .info-box {
                            background: #e9ecef;
                            border-radius: 8px;
                            padding: 15px;
                            margin: 20px 0;
                            color: #495057;
                            line-height: 1.6;
                        }
                        .footer {
                            background: #f8f9fa;
                            padding: 20px;
                            text-align: center;
                            border-top: 2px solid #e9ecef;
                            color: #6c757d;
                            font-size: 14px;
                        }
                        .security-note {
                            background: #fff3cd;
                            border-left: 4px solid #ffc107;
                            border-radius: 8px;
                            padding: 15px;
                            margin: 20px 0;
                            color: #856404;
                            font-size: 14px;
                        }
                        .alert-box {
                            background: #f8d7da;
                            border-left: 4px solid #dc3545;
                            border-radius: 8px;
                            padding: 15px;
                            margin: 20px 0;
                            color: #721c24;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Recuperação de Senha</h1>
                            <p>Solicitação de redefinição de senha</p>
                        </div>
                        
                        <div class="content">
                            <p class="greeting">Olá, <strong>%s</strong>!</p>
                            
                            <div class="message">
                                <p style="margin: 0;">
                                    Recebemos uma solicitação para redefinir a senha da sua conta no Sistema de Agendamento. 
                                    Se foi você quem solicitou, clique no botão abaixo para criar uma nova senha.
                                </p>
                            </div>
                            
                            <div class="button-container">
                                <a href="%s" class="btn-reset">🔑 Redefinir Minha Senha</a>
                            </div>
                            
                            <div class="info-box">
                                <strong>Instruções:</strong><br/>
                                1. Clique no botão acima para acessar a página de redefinição<br/>
                                2. Crie uma senha forte e segura<br/>
                                3. Confirme sua nova senha<br/>
                                4. Faça login com suas novas credenciais
                            </div>
                            
                            <div class="security-note">
                                <strong>Atenção:</strong><br/>
                                Este link de recuperação expira em 1 hora por motivos de segurança. 
                                Se você não redefinir sua senha dentro deste prazo, será necessário solicitar um novo link.
                            </div>
                            
                            <div class="alert-box">
                                <strong>Você não solicitou isso?</strong><br/>
                                Se você não solicitou a recuperação de senha, por favor ignore este e-mail e sua senha permanecerá inalterada. 
                                Recomendamos que você altere sua senha periodicamente para manter sua conta segura.
                            </div>
                            
                            <p style="color: #6c757d; font-size: 14px; margin-top: 20px;">
                                Se o botão não funcionar, copie e cole o link abaixo no seu navegador:<br/>
                                <a href="%s" style="color: #212529; word-break: break-all;">%s</a>
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p><strong>Sistema de Agendamento</strong></p>
                            <p>Este é um e-mail automático, por favor não responda.</p>
                            <p style="margin-top: 10px; font-size: 12px;">
                                Por questões de segurança, nunca compartilhe este link com outras pessoas.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, name, link, link, link),
                "text/html; charset=utf-8");

        content.addBodyPart(body);
        mail.setContent(content);

        Transport.send(mail);
    }
}
