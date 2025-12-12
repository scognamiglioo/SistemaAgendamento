# Sistema de Agendamento em Jakarta EE

Aplicação web para gestão de agendamentos construída com Jakarta EE, JSF (Facelets) e JPA rodando em WildFly. Este documento mantém as instruções de segurança e configuração originais e acrescenta visão geral, organização e passos de execução.

## Visão Geral
- **Front-end:** JSF/Facelets com páginas em `src/main/webapp`, uso de templates e recursos estáticos em `resources/css|js|images`.
- **Back-end:** Jakarta EE (JPA, CDI, Bean Validation) com entidades e serviços em `src/main/java`.
- **Servidor de aplicação:** WildFly com Elytron/JASPI para segurança e datasource JNDI para o banco.
- **Persistência:** JPA configurada em `src/main/resources/META-INF/persistence.xml` (datasource `java:/SecureDS`).
- **Envio de e-mail:** Mailgun via sessão JNDI `java:/MailGun` no WildFly.

## Requisitos
- JDK 17+ (recomendado para WildFly atual).
- Maven 3.8+.
- WildFly instalado e executando (porta de administração padrão 9990).
- Banco de dados acessível com datasource JNDI `java:/SecureDS` apontando para o schema `secureapp`.

## Organização do Projeto
- `src/main/java/io/github/...`: código Java (entidades, serviços, beans de visão).
- `src/main/resources/META-INF/persistence.xml`: configuração JPA/datasource.
- `src/main/webapp/`: páginas JSF e templates.
  - `app/`: telas autenticadas (admin, agendamentos, cadastros).
  - `resources/`: CSS/JS/imagens compartilhados.
  - `WEB-INF/`: `faces-config.xml`, `beans.xml`, `web.xml` e templates.
- `pom.xml`: dependências e plugins Maven.

## Fluxo de Uso
1) Usuário acessa a tela de login (`/login.xhtml`).
2) Após autenticação, navega por dashboards e telas de agendamento em `webapp/app/*`.
3) Criação/edição de usuários, serviços, cargos e locais ocorre pelas páginas específicas em `app/`.
4) Reset de senha e ativação usam os fluxos de e-mail configurados via Mailgun.

## Como Rodar Localmente
1. **Configurar WildFly**
   - Inicie o servidor e acesse `http://localhost:9990`.
   - Crie o datasource `java:/SecureDS` apontando para o schema `secureapp`.
2. **Configurar Mailgun** (detalhes na seção MailGun abaixo).
3. **Variáveis de ambiente**
   - Copie `.envexample` para `src/main/resources/.env` e preencha com as credenciais do Mailgun.
4. **Build e deploy**
   - Na raiz do projeto, execute `mvn clean package` para gerar o `.war`.
   - Faça o deploy do artefato gerado em `target/` no WildFly.
5. **Acesso**
   - Aplicação: `http://localhost:8080/secureapp/`
   - Console de administração: `http://localhost:9990`

## Segurança - Hash em Senhas (Elytron/JASPI)
WildFly precisa de Elytron e Java Authentication Service Provider Interface (SPI) para ativação do Containers (JASPI).

Se o projeto não funcionar, acesse o arquivo `jboss-cli` pelo cmd e digite:

- `/subsystem=elytron/policy=jacc:add(jacc-policy={})`
  > Enable a default JACC policy with WildFly Elytron
- `/subsystem=undertow/application-security-domain=other:write-attribute(name=integrated-jaspi, value=false)`
  > Map the default (`other`) security domain to WildFly Elytron
- `:reload`
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
Coloque o seu `.env` com o mail dado pelo MailGun, como o arquivo `.envexample` sugere, na pasta `resources` do projeto (`src/main/resources/.env`).
