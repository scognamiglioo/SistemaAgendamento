# sistema-agendamento

# Importante!

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

É importante que o MailGun esteja devidamente configurado

- Acesse: https://login.mailgun.com/login para realizar seu login
- Configure ele no Wildfly

## Banco de dados e Persistência

Para facilitar, defina seu banco de dados como: ``secureapp`` e no Wildfly, na aba 'Configuration', coloque o datasource como ``java:/SecureDS``
