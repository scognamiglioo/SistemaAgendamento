# Sistema de Agendamento em Jakarta EE 

## Segurança - Hash em Senhas
WildFly precisa de Elytron and Java Authentication Service Provider Interface (SPI) para ativação do Containers (JASPI).

Se o projeto não funcionar, acesse o arquivo ``jboss-cli`` pelo cmd e digite:

- ``/subsystem=elytron/policy=jacc:add(jacc-policy={})``
    > Enable a default JACC policy with WildFly Elytron
- ``/subsystem=undertow/application-security-domain=other:write-attribute(name=integrated-jaspi, value=false)``
    > Map the default (``other``) security domain to WildFly Elytron
- ``:reload``
    > Reload the settings

## MailGun

https://login.mailgun.com

### 1. Obter credenciais SMTP no Mailgun

No painel Mailgun: Sending → Domain Settings → SMTP Credentials

Copie os dados:

- SMTP Host: smtp.mailgun.org
- Porta: 587
- Username: postmaster@SEU-DOMINIO
- Password: a senha gerada

### 2. Criar Mail Session no WildFly (via Admin Console)

1. Abra o painel do WildFly:  
   `http://localhost:9990`

2. Acesse:  
   **Configuration → Subsystems → Mail**

3. Clique em **Add** para criar uma nova Mail Session:

   - **JNDI Name:** `java:/MailGun`

4. Dentro da sessão criada, clique em:  
   **View → Add SMTP Server**

   Preencha:

   - **Outbound Socket Binding:** `mail-gun`
   - **Username:** *(seu usuário Mailgun)*
   - **Password:** *(sua senha Mailgun)*
   - **TLS:** habilitado  
   - **SSL:** desabilitado

### 3. Criar o Outbound Socket Binding

1. No menu, vá para:  
   **Configuration → Socket Bindings → standard-sockets → Outbound Socket Bindings → Add**

2. Preencha:

   - **Name:** `mail-gun`
   - **Remote Host:** `smtp.mailgun.org`
   - **Remote Port:** `587`

### Arquivo .env
Coloque o seu .env com o mail dado pelo MailGun, como o arquivo .envexample sugere, na pasta ´resources' do projeto. 

## Banco de dados e Persistência

Para facilitar, defina seu banco de dados como: ``secureapp`` e no Wildfly, na aba 'Configuration', coloque o datasource como ``java:/SecureDS``
