# ADR-12 — Segredo JWT sem valor padrão e seed opt-in

> **Substituída parcialmente pela ADR-17.** O seed foi removido; o administrador inicial agora vem do `.env`.

## Contexto

A revisão de segurança da Sprint 1 identificou dois riscos na configuração inicial:

1. `application.yml` trazia um segredo JWT fixo como valor padrão. Como o repositório é público,
   qualquer pessoa poderia forjar um token de administrador sem conhecer a senha.
2. O seed de demonstração era executado por padrão, criando contas com credenciais publicadas no
   README. Em um ambiente exposto, isso permitiria login administrativo direto.

## Decisão

- `JWT_SECRET` não possui valor padrão. Se estiver ausente ou tiver menos de 32 caracteres, a
  aplicação gera um segredo aleatório de 64 bytes na inicialização e registra um aviso. Tokens
  deixam de ser válidos a cada reinício até que um segredo estável seja configurado.
- O arquivo `.env` da raiz é importado automaticamente pelo Spring Boot
  (`spring.config.import`), para que a configuração documentada funcione de fato.
- O seed de demonstração é **opt-in** (`APP_SEED=false` por padrão). O `.env.example` usado na
  apresentação define `APP_SEED=true`.
- O PostgreSQL e o Redis do Docker Compose ficam publicados apenas em `127.0.0.1`.
- O seed não escreve mais as senhas no log.

## Status

Aceita.

## Consequências

**Positivas:**
- O repositório não contém segredo capaz de autenticar na aplicação.
- Deploy sem configuração explícita não cria contas conhecidas.
- O fluxo documentado (`.env`) passa a ter efeito real.

**Negativas:**
- Sem `JWT_SECRET` estável, reinícios invalidam sessões.
- A apresentação exige o passo de copiar `.env.example` para `.env` para ter os dados de demonstração.
- Em produção, o segredo deve vir de um cofre ou variável de ambiente gerenciada, não do `.env`.
