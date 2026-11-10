# ADR-17 — Remoção de dados de demonstração e envio real por SMTP

## Contexto

A versão acadêmica usava um seed com contas de exemplo e um adaptador que apenas registrava o e-mail
no log. Para operar com dados reais e tudo persistido no PostgreSQL, era preciso remover os dados
fictícios, criar o primeiro administrador de forma controlada e enviar notificações por um provedor
real.

## Decisão

- O `DataSeeder` foi removido. Não há usuários, tutores, animais, consultas ou pagamentos
  pré-cadastrados.
- O primeiro administrador é criado por `AdminBootstrap` a partir de `ADMIN_EMAIL`, `ADMIN_PASSWORD`
  e `ADMIN_NOME`, somente quando não existe administrador ativo.
- O restante dos dados é criado exclusivamente pelos fluxos do sistema e persistido no PostgreSQL.
- O adaptador de e-mail simulado foi removido. `SmtpCanalNotificacao` usa `JavaMailSender` com
  `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` e `NOTIFICACOES_CANAL=SMTP`.
- A caixa de saída de notificações continua no banco; falhas de SMTP são gravadas com status
  `FALHA` e a mensagem de erro.
- Os testes de integração usam um dublê de `CanalNotificacao` apenas no escopo de teste, para não
  enviar e-mail real durante a suíte.

## Status

Aceita. Substitui a parte de seed da ADR-12.

## Consequências

**Positivas:**
- Nenhum dado fictício em produção.
- O administrador inicial é explícito e configurável.
- Notificações reais, com rastreabilidade de sucesso/falha no banco.
- O banco pode começar vazio sem impedir o primeiro acesso.

**Negativas:**
- É obrigatório configurar `ADMIN_EMAIL`/`ADMIN_PASSWORD` no primeiro start.
- Sem SMTP configurado, as notificações ficam com status `FALHA`.
- A demonstração acadêmica precisa cadastrar os dados manualmente ou por script.
