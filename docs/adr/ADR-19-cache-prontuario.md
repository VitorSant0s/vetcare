# ADR-19 — Cache de prontuário com auditoria preservada

## Contexto

O prontuário é uma das telas mais pesadas do sistema: agrega animal, tutor, atendimentos,
prescrições, vacinações e consultas. Ele é aberto durante o atendimento e precisa responder rápido.
Ao mesmo tempo, é dado clínico sensível: todo acesso deve ser autorizado e auditado, e qualquer
alteração clínica precisa refletir imediatamente.

## Decisão

- O conteúdo do prontuário é cacheado no Redis com TTL de 60 segundos, chave `prontuario:{animalId}`.
- A montagem pesada fica em `ProntuarioCacheService`, anotada com `@Cacheable`.
- `ProntuarioService` mantém, fora do cache, a verificação de existência do animal, a autorização do
  veterinário e o registro `PRONTUARIO_ACESSADO`. Assim, cache hit não pula auditoria.
- O cache é invalidado integralmente (`allEntries = true`) em qualquer mutação que altere o
  prontuário:
  - agendar ou cancelar consulta;
  - registrar atendimento;
  - criar ou excluir vacinação;
  - bloquear/desbloquear agenda;
  - atualizar/excluir animal, tutor ou usuário;
  - atualizar/excluir veterinário.
- Os DTOs do prontuário implementam `Serializable`, permitindo a serialização JDK já usada no cache
  da agenda.

## Status

Aceita.

## Consequências

**Positivas:**
- Abertura repetida do prontuário evita quatro consultas pesadas ao PostgreSQL.
- Autorização e auditoria continuam em toda requisição.
- Dados clínicos não ficam defasados por mais de 60 segundos e mutações invalidam na hora.

**Negativas:**
- Dados clínicos ficam temporariamente no Redis; o Redis deve ser tratado como dado sensível.
- Invalidação ampla pode reduzir o hit rate em períodos de muitas mutações.
- A serialização JDK é menos interoperável que JSON; uma evolução futura pode migrar para JSON.
- Em múltiplas instâncias, o Redis compartilhado mantém o comportamento; um cache local não manteria.
