package io.github.scognamiglioo.services;

import io.github.scognamiglioo.entities.Agendamento;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class AgendamentoMailService implements AgendamentoMailServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(AgendamentoMailService.class.getName());

    @Resource(name = "java:/MailGun")
    private Session mailSession;

    @Override
    public void sendConfirmacaoAgendamento(Agendamento agendamento) throws MessagingException {
        try {
            // Tenta usar a sessão do servidor primeiro
            if (mailSession == null) {
                LOGGER.log(Level.WARNING, "Mail Session não encontrada, criando manualmente...");
                mailSession = createManualSession();
            }

            MimeMessage mail = new MimeMessage(mailSession);
            Dotenv dotenv = Dotenv.load();

            String email = dotenv.get("EMAIL");
            
            if (email == null || email.isEmpty()) {
                throw new MessagingException("EMAIL não configurado no arquivo .env");
            }

            LOGGER.log(Level.INFO, "Enviando e-mail de: {0} para: {1}", 
                new Object[]{email, agendamento.getUser().getEmail()});

            try {
                mail.setFrom(new InternetAddress(email, "Sistema de Agendamento"));
            } catch (UnsupportedEncodingException ex) {
                mail.setFrom(new InternetAddress(email));
            }

            mail.setSubject("Confirmação de Agendamento");
            mail.setRecipient(Message.RecipientType.TO, 
                new InternetAddress(agendamento.getUser().getEmail()));

            MimeMultipart content = new MimeMultipart();
            MimeBodyPart body = new MimeBodyPart();

            // Formatação de data e hora
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            String dataFormatada = agendamento.getData().format(dateFormatter);
            String horaFormatada = agendamento.getHora().format(timeFormatter);

            // Informações do funcionário
            String nomeFuncionario = agendamento.getFuncionario() != null 
                ? agendamento.getFuncionario().getNome() 
                : "A definir";

            

            body.setContent(buildEmailContent(
                agendamento.getUser().getNome(),
                dataFormatada,
                horaFormatada,
                agendamento.getId(),
                agendamento.getServico().getNome(),
                nomeFuncionario,
                agendamento.getStatus().getDescricao()
            ), "text/html; charset=utf-8");

            content.addBodyPart(body);
            mail.setContent(content);

            Transport.send(mail);
            
            LOGGER.log(Level.INFO, "E-mail enviado com sucesso para: {0}", 
                agendamento.getUser().getEmail());

        } catch (MessagingException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao enviar e-mail de confirmação", ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro inesperado ao enviar e-mail", ex);
            throw new MessagingException("Erro ao enviar e-mail: " + ex.getMessage(), ex);
        }
    }

    /**
     * Cria uma sessão de e-mail manual caso o recurso do servidor não esteja disponível
     */
    private Session createManualSession() {
        Dotenv dotenv = Dotenv.load();
        
        String smtpHost = dotenv.get("SMTP_HOST");
        String smtpPort = dotenv.get("SMTP_PORT");
        String smtpUsername = dotenv.get("SMTP_USERNAME");
        String smtpPassword = dotenv.get("SMTP_PASSWORD");
        String smtpAuth = dotenv.get("SMTP_AUTH");
        String smtpStartTls = dotenv.get("SMTP_STARTTLS");

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost != null ? smtpHost : "smtp.gmail.com");
        props.put("mail.smtp.port", smtpPort != null ? smtpPort : "587");
        props.put("mail.smtp.auth", smtpAuth != null ? smtpAuth : "true");
        props.put("mail.smtp.starttls.enable", smtpStartTls != null ? smtpStartTls : "true");

        LOGGER.log(Level.INFO, "Configuração SMTP: host={0}, port={1}", 
            new Object[]{props.get("mail.smtp.host"), props.get("mail.smtp.port")});

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });
    }

    private String buildEmailContent(String nomeUsuario, String data, String hora, 
                                     Long codigo, String servico, String funcionario, 
                                    String status) {
        return String.format("""
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
                    .info-card {
                        background: #f8f9fa;
                        border-left: 4px solid #343a40;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 15px 0;
                    }
                    .info-row {
                        display: flex;
                        margin: 12px 0;
                        align-items: center;
                    }
                    .info-label {
                        color: #6c757d;
                        font-weight: 600;
                        text-transform: uppercase;
                        font-size: 12px;
                        letter-spacing: 0.5px;
                        min-width: 120px;
                    }
                    .info-value {
                        color: #212529;
                        font-weight: 600;
                        font-size: 16px;
                    }
                    .highlight {
                        background: linear-gradient(135deg, #212529 0%%, #000000 100%%);
                        color: white;
                        padding: 20px;
                        border-radius: 8px;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .highlight h2 {
                        margin: 0 0 10px 0;
                        font-size: 32px;
                    }
                    .highlight p {
                        margin: 0;
                        opacity: 0.9;
                    }
                    .message {
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
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Agendamento Confirmado</h1>
                        <p>Seu agendamento foi confirmado com sucesso!</p>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Olá, <strong>%s</strong>!</p>
                        
                        <p>Confirmamos seu agendamento. Confira os detalhes abaixo:</p>
                        
                        <div class="highlight">
                            <h2> %s</h2>
                            <p> %s</p>
                        </div>
                        
                        <div class="info-card">
                            <div class="info-row">
                                <span class="info-label"> Código:</span>
                                <span class="info-value">#%d</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label"> Serviço:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label"> Profissional:</span>
                                <span class="info-value">%s</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="info-label"> Status:</span>
                                <span class="info-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="message">
                            <strong> Informações Importantes:</strong><br/>
                            • Chegue com 10 minutos de antecedência<br/>
                            • Traga documento de identidade<br/>
                            • Em caso de impossibilidade de comparecimento, cancele com antecedência<br/>
                            • Guarde o código do agendamento para consultas
                        </div>
                        
                        <p style="color: #6c757d; font-size: 14px; margin-top: 20px;">
                            Caso precise cancelar ou reagendar, acesse o sistema com suas credenciais.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Sistema de Agendamento</strong></p>
                        <p>Este é um e-mail automático, por favor não responda.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            nomeUsuario, data, hora, codigo, servico, funcionario, status
        );
    }

    @Override
    public void sendCancelamentoAgendamento(Agendamento agendamento) throws MessagingException {
        try {
            if (mailSession == null) {
                LOGGER.log(Level.WARNING, "Mail Session não encontrada, criando manualmente...");
                mailSession = createManualSession();
            }

            MimeMessage mail = new MimeMessage(mailSession);
            Dotenv dotenv = Dotenv.load();

            String email = dotenv.get("EMAIL");
            
            if (email == null || email.isEmpty()) {
                throw new MessagingException("EMAIL não configurado no arquivo .env");
            }

            LOGGER.log(Level.INFO, "Enviando e-mail de cancelamento de: {0} para: {1}", 
                new Object[]{email, agendamento.getUser().getEmail()});

            try {
                mail.setFrom(new InternetAddress(email, "Sistema de Agendamento"));
            } catch (UnsupportedEncodingException ex) {
                mail.setFrom(new InternetAddress(email));
            }

            mail.setSubject("Cancelamento de Agendamento");
            mail.setRecipient(Message.RecipientType.TO, 
                new InternetAddress(agendamento.getUser().getEmail()));

            MimeMultipart content = new MimeMultipart();
            MimeBodyPart body = new MimeBodyPart();

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            String dataFormatada = agendamento.getData().format(dateFormatter);
            String horaFormatada = agendamento.getHora().format(timeFormatter);

            String nomeFuncionario = agendamento.getFuncionario() != null 
                ? agendamento.getFuncionario().getNome() 
                : "A definir";

            body.setContent(buildCancelamentoEmailContent(
                agendamento.getUser().getNome(),
                dataFormatada,
                horaFormatada,
                agendamento.getId(),
                agendamento.getServico().getNome(),
                nomeFuncionario
            ), "text/html; charset=utf-8");

            content.addBodyPart(body);
            mail.setContent(content);

            Transport.send(mail);
            
            LOGGER.log(Level.INFO, "E-mail de cancelamento enviado com sucesso para: {0}", 
                agendamento.getUser().getEmail());

        } catch (MessagingException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao enviar e-mail de cancelamento", ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro inesperado ao enviar e-mail de cancelamento", ex);
            throw new MessagingException("Erro ao enviar e-mail: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void sendReagendamento(Agendamento agendamentoOriginal, Agendamento novoAgendamento) throws MessagingException {
        try {
            if (mailSession == null) {
                LOGGER.log(Level.WARNING, "Mail Session não encontrada, criando manualmente...");
                mailSession = createManualSession();
            }

            MimeMessage mail = new MimeMessage(mailSession);
            Dotenv dotenv = Dotenv.load();

            String email = dotenv.get("EMAIL");
            
            if (email == null || email.isEmpty()) {
                throw new MessagingException("EMAIL não configurado no arquivo .env");
            }

            LOGGER.log(Level.INFO, "Enviando e-mail de reagendamento de: {0} para: {1}", 
                new Object[]{email, novoAgendamento.getUser().getEmail()});

            try {
                mail.setFrom(new InternetAddress(email, "Sistema de Agendamento"));
            } catch (UnsupportedEncodingException ex) {
                mail.setFrom(new InternetAddress(email));
            }

            mail.setSubject("Reagendamento Confirmado");
            mail.setRecipient(Message.RecipientType.TO, 
                new InternetAddress(novoAgendamento.getUser().getEmail()));

            MimeMultipart content = new MimeMultipart();
            MimeBodyPart body = new MimeBodyPart();

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // Dados do agendamento original
            String dataOriginal = agendamentoOriginal.getData().format(dateFormatter);
            String horaOriginal = agendamentoOriginal.getHora().format(timeFormatter);
            String funcionarioOriginal = agendamentoOriginal.getFuncionario() != null 
                ? agendamentoOriginal.getFuncionario().getNome() 
                : "A definir";

            // Dados do novo agendamento
            String dataNova = novoAgendamento.getData().format(dateFormatter);
            String horaNova = novoAgendamento.getHora().format(timeFormatter);
            String funcionarioNovo = novoAgendamento.getFuncionario() != null 
                ? novoAgendamento.getFuncionario().getNome() 
                : "A definir";

            body.setContent(buildReagendamentoEmailContent(
                novoAgendamento.getUser().getNome(),
                agendamentoOriginal.getId(),
                dataOriginal,
                horaOriginal,
                funcionarioOriginal,
                novoAgendamento.getId(),
                dataNova,
                horaNova,
                novoAgendamento.getServico().getNome(),
                funcionarioNovo
            ), "text/html; charset=utf-8");

            content.addBodyPart(body);
            mail.setContent(content);

            Transport.send(mail);
            
            LOGGER.log(Level.INFO, "E-mail de reagendamento enviado com sucesso para: {0}", 
                novoAgendamento.getUser().getEmail());

        } catch (MessagingException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao enviar e-mail de reagendamento", ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro inesperado ao enviar e-mail de reagendamento", ex);
            throw new MessagingException("Erro ao enviar e-mail: " + ex.getMessage(), ex);
        }
    }

    private String buildCancelamentoEmailContent(String nomeUsuario, String data, String hora, 
                                             Long codigo, String servico, String funcionario) {
        return String.format("""
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
                        background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%);
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
                    .alert-box {
                        background: #f8d7da;
                        border-left: 4px solid #dc3545;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                        color: #721c24;
                    }
                    .alert-box strong {
                        display: block;
                        font-size: 18px;
                        margin-bottom: 10px;
                    }
                    .info-card {
                        background: #f8f9fa;
                        border-left: 4px solid #6c757d;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 15px 0;
                    }
                    .info-row {
                        display: flex;
                        margin: 12px 0;
                        align-items: center;
                    }
                    .info-label {
                        color: #6c757d;
                        font-weight: 600;
                        text-transform: uppercase;
                        font-size: 12px;
                        letter-spacing: 0.5px;
                        min-width: 120px;
                    }
                    .info-value {
                        color: #212529;
                        font-weight: 600;
                        font-size: 16px;
                    }
                    .cancelled-badge {
                        background: #dc3545;
                        color: white;
                        padding: 20px;
                        border-radius: 8px;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .cancelled-badge h2 {
                        margin: 0 0 10px 0;
                        font-size: 32px;
                    }
                    .cancelled-badge p {
                        margin: 0;
                        opacity: 0.9;
                    }
                    .message {
                        background: #e9ecef;
                        border-radius: 8px;
                        padding: 15px;
                        margin: 20px 0;
                        color: #495057;
                        line-height: 1.6;
                    }
                    .button-container {
                        text-align: center;
                        margin: 30px 0;
                    }
                    .btn-new {
                        display: inline-block;
                        background: linear-gradient(135deg, #212529 0%%, #000000 100%%);
                        color: white;
                        padding: 15px 40px;
                        border-radius: 8px;
                        text-decoration: none;
                        font-weight: 700;
                        font-size: 16px;
                        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        border-top: 2px solid #e9ecef;
                        color: #6c757d;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Agendamento Cancelado</h1>
                        <p>Seu agendamento foi cancelado</p>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Olá, <strong>%s</strong>!</p>
                        
                        <div class="alert-box">
                            <strong>Cancelamento Confirmado</strong>
                            <p style="margin: 5px 0 0 0;">
                                Seu agendamento foi cancelado com sucesso. Confira os detalhes abaixo:
                            </p>
                        </div>
                        
                        <div class="cancelled-badge">
                            <h2>%s</h2>
                            <p>%s</p>
                        </div>
                        
                        <div class="info-card">
                            <div class="info-row">
                                <span class="info-label">Código:</span>
                                <span class="info-value">#%d</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Serviço:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Profissional:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Status:</span>
                                <span class="info-value" style="color: #dc3545;">CANCELADO</span>
                            </div>
                        </div>
                        
                        <div class="message">
                            <strong>Gostaria de fazer um novo agendamento?</strong><br/>
                            Acesse o sistema e escolha um novo horário que melhor se adeque à sua agenda.
                        </div>
                        
                        <p style="color: #6c757d; font-size: 14px; margin-top: 20px;">
                            Se você não solicitou este cancelamento ou tem alguma dúvida, 
                            entre em contato conosco através do sistema.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Sistema de Agendamento</strong></p>
                        <p>Este é um e-mail automático, por favor não responda.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            nomeUsuario, data, hora, codigo, servico, funcionario
        );
    }

    private String buildReagendamentoEmailContent(String nomeUsuario, Long codigoOriginal,
                                              String dataOriginal, String horaOriginal, String funcionarioOriginal,
                                              Long codigoNovo, String dataNova, String horaNova,
                                              String servico, String funcionarioNovo) {
        return String.format("""
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
                        background: linear-gradient(135deg, #17a2b8 0%%, #117a8b 100%%);
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
                    .comparison-box {
                        display: flex;
                        gap: 15px;
                        margin: 20px 0;
                    }
                    .old-info, .new-info {
                        flex: 1;
                        border-radius: 8px;
                        padding: 20px;
                    }
                    .old-info {
                        background: #f8d7da;
                        border-left: 4px solid #dc3545;
                    }
                    .new-info {
                        background: #d4edda;
                        border-left: 4px solid #28a745;
                    }
                    .comparison-title {
                        font-weight: 700;
                        font-size: 14px;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        margin-bottom: 15px;
                        display: flex;
                        align-items: center;
                        gap: 8px;
                    }
                    .old-info .comparison-title {
                        color: #721c24;
                    }
                    .new-info .comparison-title {
                        color: #155724;
                    }
                    .comparison-item {
                        margin: 10px 0;
                    }
                    .comparison-label {
                        color: #6c757d;
                        font-size: 12px;
                        text-transform: uppercase;
                        font-weight: 600;
                        display: block;
                        margin-bottom: 4px;
                    }
                    .comparison-value {
                        color: #212529;
                        font-size: 16px;
                        font-weight: 600;
                    }
                    .highlight {
                        background: linear-gradient(135deg, #212529 0%%, #000000 100%%);
                        color: white;
                        padding: 20px;
                        border-radius: 8px;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .highlight h2 {
                        margin: 0 0 10px 0;
                        font-size: 32px;
                    }
                    .highlight p {
                        margin: 0;
                        opacity: 0.9;
                    }
                    .info-card {
                        background: #f8f9fa;
                        border-left: 4px solid #343a40;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 15px 0;
                    }
                    .message {
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
                    @media (max-width: 600px) {
                        .comparison-box {
                            flex-direction: column;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Reagendamento Confirmado</h1>
                        <p>Seu agendamento foi alterado com sucesso!</p>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Olá, <strong>%s</strong>!</p>
                        
                        <p>Seu reagendamento foi confirmado. Confira as alterações:</p>
                        
                        <div class="comparison-box">
                            <div class="old-info">
                                <div class="comparison-title">
                                    ANTERIOR
                                </div>
                                <div class="comparison-item">
                                    <span class="comparison-label">Código</span>
                                    <span class="comparison-value">#%d</span>
                                </div>
                                <div class="comparison-item">
                                    <span class="comparison-label">Data</span>
                                    <span class="comparison-value">%s</span>
                                </div>
                                <div class="comparison-item">
                                    <span class="comparison-label">Horário</span>
                                    <span class="comparison-value">%s</span>
                                </div>
                                <div class="comparison-item">
                                    <span class="comparison-label">Profissional</span>
                                    <span class="comparison-value">%s</span>
                                </div>
                            </div>
                            
                            <div class="new-info">
                                <div class="comparison-title">
                                    NOVO
                                </div>
                                <div class="comparison-item">
                                    <span class="comparison-label">Código</span>
                                    <span class="comparison-value">#%d</span>
                                </div>
                                <div class="comparison-item">
                                    <span class="comparison-label">Data</span>
                                    <span class="comparison-value">%s</span>
                                </div>
                                <div class="comparison-item">
                                    <span class="comparison-label">Horário</span>
                                    <span class="comparison-value">%s</span>
                                </div>
                                <div class="comparison-item">
                                    <span class="comparison-label">Profissional</span>
                                    <span class="comparison-value">%s</span>
                                </div>
                            </div>
                        </div>
                        
                        <div class="highlight">
                            <h2>%s</h2>
                            <p>%s</p>
                        </div>
                        
                        <div class="info-card">
                            <div style="text-align: center;">
                                <div style="margin: 10px 0;">
                                    <span style="color: #6c757d; font-size: 12px; text-transform: uppercase; font-weight: 600;">Serviço</span>
                                    <div style="color: #212529; font-size: 18px; font-weight: 700; margin-top: 5px;">%s</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="message">
                            <strong>Informações Importantes:</strong><br/>
                            • Chegue com 10 minutos de antecedência<br/>
                            • Traga documento de identidade<br/>
                            • Guarde o novo código do agendamento (#%d)<br/>
                            • Em caso de nova impossibilidade, cancele com antecedência
                        </div>
                        
                        <p style="color: #6c757d; font-size: 14px; margin-top: 20px;">
                            O agendamento anterior (#%d) foi automaticamente cancelado. 
                            Caso precise fazer novas alterações, acesse o sistema com suas credenciais.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Sistema de Agendamento</strong></p>
                        <p>Este é um e-mail automático, por favor não responda.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            nomeUsuario, 
            codigoOriginal, dataOriginal, horaOriginal, funcionarioOriginal,
            codigoNovo, dataNova, horaNova, funcionarioNovo,
            dataNova, horaNova,
            servico,
            codigoNovo,
            codigoOriginal
        );
    }
}
