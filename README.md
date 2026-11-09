# VetCare

Sistema de gestão para clínicas veterinárias desenvolvido para a disciplina **Engenharia de Software 1** do Instituto de Computação da UFBA.

Esta é a **Sprint 1** do Trabalho II: implementação de aproximadamente 30% dos requisitos funcionais do VetCare, sobre a arquitetura definida na Etapa 1.

![Agenda do VetCare](docs/relatorio/img/02-agenda.png)

## Escopo da sprint

| Requisito | Descrição | Prioridade | Situação |
|---|---|---|---|
| REQ01 | Manter Usuário (incluir, alterar, excluir e consultar) | Alta | Implementado |
| REQ02 | Autenticar Usuário com JWT e perfis de acesso | Alta | Implementado |
| REQ04 | Manter Tutor | Alta | Implementado |
| REQ05 | Manter Animal vinculado a um tutor | Alta | Implementado |
| REQ07 | Agendar Consulta com validação de conflito | Alta | Implementado |

**5 de 17 requisitos = 29,4%**, dentro da meta de aproximadamente 30%.

Fora do escopo desta sprint: REQ03, REQ06, REQ08–REQ17. O REQ06 (Manter Veterinário) é parcialmente atendido: veterinários são usuários com perfil `VETERINARIO`, cadastrados pelo REQ01 e usados no agendamento. A decisão está registrada na [ADR-10](docs/adr/ADR-10-veterinario-como-usuario.md).

## Stack

- **Back-end:** Kotlin 2.3, Spring Boot 4.1, Spring Security (JWT), Spring Data JPA, Flyway, PostgreSQL, Redis, Springdoc OpenAPI
- **Front-end:** React 19, TypeScript, Vite, Tailwind CSS 4, React Router, React Hook Form + Zod, Axios
- **Infraestrutura:** Docker Compose (PostgreSQL 16 e Redis 7)
- **Testes:** JUnit 5 + MockMvc (back-end) e Vitest + Testing Library (front-end)

## Arquitetura

O sistema segue o estilo cliente-servidor com API REST organizada em camadas, conforme a Etapa 1:

```
backend/src/main/kotlin/br/ufba/vetcare/
├── api/              apresentação HTTP: controllers, DTOs e tratamento de erros
├── application/      casos de uso (serviços) e auditoria
├── domain/           entidades, enums e interfaces de repositório
└── infrastructure/   segurança (JWT), persistência e integrações
```

Decisões arquiteturais da Etapa 1 preservadas: JWT (ADR-03), PostgreSQL (ADR-04), auditoria dedicada (ADR-05), cache Redis para agenda (ADR-08) e health-check (ADR-09).

### Desvios registrados

- [ADR-10 — Veterinário representado por usuário com perfil](docs/adr/ADR-10-veterinario-como-usuario.md)
- [ADR-11 — API Gateway/BFF como camada de controllers no monólito](docs/adr/ADR-11-api-gateway-como-camada.md)

## Como rodar

### Pré-requisitos

- Docker e Docker Compose
- JDK 21+ (testado com JDK 27)
- Node.js 20+ (testado com Node 26)

### 1. Subir banco e cache

```bash
docker compose up -d
```

Isso inicia PostgreSQL em `localhost:5432` (banco/usuário/senha: `vetcare`) e Redis em `localhost:6379`.

### 2. Subir o back-end

```bash
cd backend
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. O Flyway cria o esquema e o seed de demonstração na primeira execução.

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health-check: http://localhost:8080/actuator/health

### 3. Subir o front-end

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173`.

### Credenciais de demonstração

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | `admin@vetcare.com` | `Admin@123` |
| Recepcionista | `recepcao@vetcare.com` | `Recepcao@123` |
| Veterinário | `vet@vetcare.com` | `Vet@123` |

O seed também cria tutores, animais e uma consulta de exemplo. Para desativá-lo, use `APP_SEED=false`.

### Variáveis de ambiente

Copie `.env.example` para `.env` se quiser alterar os valores padrão. As principais:

| Variável | Padrão | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/vetcare` | Conexão com o PostgreSQL |
| `DB_USER` / `DB_PASSWORD` | `vetcare` / `vetcare` | Credenciais do banco |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Cache da agenda |
| `JWT_SECRET` | segredo de desenvolvimento | Assinatura dos tokens |
| `JWT_EXPIRATION_MINUTES` | `120` | Validade do token |
| `APP_SEED` | `true` | Dados de demonstração |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Origens permitidas |

## Testes

```bash
# Back-end (17 testes de integração)
cd backend && ./mvnw test

# Front-end (9 testes)
cd frontend && npm test

# Build de produção do front-end
cd frontend && npm run build
```

Os testes de back-end cobrem autenticação, RBAC, CRUD de usuários, tutores e animais, agendamento, conflito de horário e validação de vínculo animal/tutor. Usam H2 em memória no perfil `test`, sem exigir Docker.

## Segurança e auditoria

- Senhas armazenadas com BCrypt.
- Tokens JWT assinados com HMAC-SHA256, contendo perfil do usuário.
- Autorização por perfil na borda da API:
  - **Administrador:** usuários, tutores, animais e agenda.
  - **Recepcionista:** tutores, animais e agendamento.
  - **Veterinário:** leitura de tutores/animais e apenas a própria agenda.
- Todas as mutações e tentativas de login são registradas na tabela `auditoria` com usuário, ação, entidade, IP e data/hora.
- Exclusões de usuários e animais são lógicas, preservando histórico e rastreabilidade.

## Principais endpoints

| Método | Rota | Perfis | Descrição |
|---|---|---|---|
| POST | `/api/auth/login` | público | Autenticação |
| GET | `/api/auth/me` | autenticados | Dados da sessão |
| GET/POST/PUT/DELETE | `/api/usuarios` | Administrador | REQ01 |
| GET/POST/PUT/DELETE | `/api/tutores` | Admin/Recepção (escrita) | REQ04 |
| GET/POST/PUT/DELETE | `/api/animais` | Admin/Recepção (escrita) | REQ05 |
| GET | `/api/animais/por-tutor/{id}` | autenticados | Apoio ao agendamento |
| GET | `/api/veterinarios` | autenticados | Apoio ao agendamento |
| GET | `/api/consultas` | autenticados | Agenda do período |
| POST | `/api/consultas` | Admin/Recepção | REQ07 |

A documentação interativa completa está no Swagger UI.

## Estrutura do repositório

```
vetcare/
├── backend/            API Spring Boot em Kotlin
├── frontend/           SPA React
├── docs/
│   ├── adr/            decisões arquiteturais
│   ├── relatorio/      relatório da sprint (HTML/PDF)
│   └── slides/         apresentação (PPTX)
├── docker-compose.yml  PostgreSQL + Redis
└── README.md
```

## Documentos

- [Relatório da Sprint 1 (PDF)](docs/relatorio/relatorio-sprint-1.pdf)
- [Apresentação da Sprint 1 (PPTX)](docs/slides/VetCare-Sprint-1.pptx)
- [ADRs](docs/adr)
- [Capturas de tela](docs/relatorio/img)

## Publicar no GitHub

O repositório local já está inicializado e commitado na branch `main`. Para publicar:

1. Crie um repositório **público vazio** chamado `vetcare` no GitHub (sem README nem `.gitignore`).
2. No diretório do projeto, conecte o remoto e envie:

```bash
git remote add origin https://github.com/SEU-USUARIO/vetcare.git
git push -u origin main
```

O workflow em `.github/workflows/ci.yml` executa os testes do back-end e do front-end a cada push.

## Equipe

- Vitor de Jesus Santos — UFBA

Disciplina: Engenharia de Software 1 · Prof.ª Dra. Larissa Barbosa Leôncio Pinheiro
