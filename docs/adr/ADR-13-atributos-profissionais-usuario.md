# ADR-13 — Atributos profissionais na entidade Usuário

## Contexto

O REQ06 (Manter Veterinário) passa a fazer parte do escopo na Sprint 2. A decisão anterior (ADR-10)
representava o veterinário como um `Usuario` com perfil `VETERINARIO`, mas não havia onde armazenar
CRMV e especialidade. Criar uma entidade `Veterinario` separada duplicaria nome, e-mail, senha e
status de acesso, além de exigir sincronização entre dois cadastros.

## Decisão

Manter o veterinário como `Usuario` com perfil `VETERINARIO` e acrescentar à tabela `usuarios` os
campos opcionais `crmv` (único quando preenchido) e `especialidade`. O módulo REQ06 é exposto por
`/api/veterinarios`, com CRUD restrito ao Administrador e leitura para os demais perfis
autenticados. O CRMV é obrigatório na criação/atualização feita por esse módulo.

## Status

Aceita. Refina a ADR-10.

## Consequências

**Positivas:**
- Uma única identidade para autenticação e dados profissionais.
- O agendamento continua referenciando `usuarios.id` sem migração de chave.
- CRMV e especialidade aparecem no portal do veterinário e na listagem da equipe.

**Negativas:**
- A tabela `usuarios` acumula atributos de perfis diferentes (campos nulos para recepção/admin).
- Se o sistema crescer para múltiplos tipos de profissional, pode ser necessário separar o perfil
  profissional em uma tabela própria.
