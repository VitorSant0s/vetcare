# ADR-18 — LGPD e endurecimento de segurança

## Contexto

O VetCare trata dados pessoais de tutores e dados clínicos de animais, além de informações
financeiras. A operação precisa atender princípios da LGPD (finalidade, necessidade, segurança,
transparência e prestação de contas) e reduzir riscos de acesso indevido, força bruta e sessões
roubadas.

## Decisão

### LGPD

- Política de privacidade pública em `/privacidade`, versionada (1.0).
- Consentimento do cliente registrado em `tutores.consentimento_em` e `consentimento_versao`,
  solicitado no primeiro acesso ao portal.
- Direito de acesso/portabilidade: `GET /api/cliente/meus-dados` exporta tutor, pets, consultas,
  atendimentos, vacinas, pagamentos, solicitações e notificações em JSON.
- Direito de eliminação: `DELETE /api/cliente/conta` anonimiza dados pessoais (nome, CPF, e-mail,
  telefone, endereço e senha), desativa o acesso e incrementa a versão do token. Registros clínicos e
  financeiros permanecem pelo prazo legal aplicável.
- Consentimento, exportação e exclusão geram eventos de auditoria.

### Segurança

- Rate limit de login por IP + identificador: 5 tentativas em 15 minutos, bloqueio temporário e
  evento `LOGIN_BLOQUEADO`/`LOGIN_CLIENTE_BLOQUEADO`.
- `token_version` em usuários e tutores, incluída no JWT. Logout e troca de senha incrementam a
  versão, invalidando tokens antigos. O filtro valida a versão e o status do titular a cada requisição.
- Logout explícito para equipe e cliente.
- Cabeçalhos HTTP de segurança (`X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`,
  `Permissions-Policy`) e `Cache-Control: no-store` para APIs.
- Dados sensíveis continuam restritos por perfil e auditados.

## Status

Aceita.

## Consequências

**Positivas:**
- Maior conformidade com LGPD e rastreabilidade de consentimento e direitos.
- Sessões roubadas deixam de valer após logout/troca de senha.
- Força bruta fica limitada e auditada.

**Negativas:**
- A anonimização é irreversível e exige confirmação do cliente.
- O rate limit em memória é adequado a uma instância; múltiplas instâncias exigem Redis.
- A retenção legal impede exclusão total de registros clínicos/financeiros.
- Ainda não há DPO formal, política de incidentes ou revisão jurídica da política.
