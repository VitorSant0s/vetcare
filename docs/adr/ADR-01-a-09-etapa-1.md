# ADR-01 a ADR-09 — Decisões da Etapa 1

As decisões abaixo foram definidas no documento **Arquitetura do Sistema VetCare** (Etapa 1) e
permanecem válidas na Sprint 1. Este arquivo resume cada uma para manter a rastreabilidade dentro
do repositório; o texto completo está no PDF da Etapa 1.

| ADR | Decisão | Status na Sprint 1 |
|---|---|---|
| ADR-01 | Estilo cliente-servidor com API REST e organização em camadas | Mantida — SPA React + API Spring Boot em camadas |
| ADR-02 | Segurança como característica arquitetural principal | Mantida — JWT, RBAC e auditoria |
| ADR-03 | Autenticação baseada em token JWT | Mantida — token HMAC assinado, validade de 120 min |
| ADR-04 | Banco de dados relacional (PostgreSQL) | Mantida — PostgreSQL 16 com Flyway |
| ADR-05 | Camada de auditoria dedicada | Mantida — `AuditoriaService` + tabela `auditoria` |
| ADR-06 | Notificações assíncronas via serviço externo | Adiada — REQ16 fora do escopo da sprint |
| ADR-07 | Manutenibilidade via modularização em camadas | Mantida — pacotes `api`, `application`, `domain`, `infrastructure` |
| ADR-08 | Desempenho por cache Redis e índices | Mantida — cache `agenda` com TTL de 60 s e índices de agenda |
| ADR-09 | Disponibilidade por health-check e backup | Parcial — `/actuator/health` implementado; backup é operacional |

Decisões novas da Sprint 1:

- [ADR-10 — Veterinário representado por usuário com perfil](ADR-10-veterinario-como-usuario.md)
- [ADR-11 — API Gateway/BFF como camada de controllers no monólito](ADR-11-api-gateway-como-camada.md)
- [ADR-12 — Segredo JWT sem valor padrão e seed opt-in](ADR-12-segredo-jwt-e-seed.md)
