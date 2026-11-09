# ADR-10 — Veterinário representado por usuário com perfil

## Contexto

O REQ06 (Manter Veterinário) é um requisito de prioridade Alta, mas não foi selecionado no recorte de 30% desta sprint. O REQ07 (Agendar Consulta) precisa relacionar um profissional veterinário à consulta. Criar uma entidade `Veterinario` separada de `Usuario` duplicaria dados de identificação e criaria dois caminhos de autenticação para o mesmo profissional.

## Decisão

Nesta sprint, o veterinário é um `Usuario` com perfil `VETERINARIO`. O cadastro e a manutenção desses profissionais acontecem pelo REQ01 (Manter Usuário), e a listagem usada no agendamento é exposta por `GET /api/veterinarios`. A consulta referencia `usuarios.id` como veterinário responsável.

## Status

Aceita.

## Consequências

**Positivas:**
- Elimina duplicação entre dados de acesso e dados profissionais.
- Mantém o RBAC já previsto na arquitetura (o perfil Veterinário é o mesmo que autoriza o acesso).
- Permite entregar o REQ07 sem expandir o escopo para o REQ06.

**Negativas:**
- Atributos específicos do profissional (CRMV, especialidade, horários de trabalho) não têm onde ser armazenados nesta sprint.
- Quando o REQ06 for implementado, será necessário decidir entre estender `usuarios` ou criar um perfil profissional vinculado, com migração de dados.
