# ADR-16 — Portal do cliente com autenticação por CPF e aprovação de agendamento

## Contexto

A clínica já possui o cadastro de tutores, mas eles não autenticavam no sistema. A Sprint 5
acrescenta um portal externo para o cliente acompanhar seus pets, solicitar agendamentos e
gerenciar os dados dos animais. O Documento de Requisitos original define apenas os perfis
Administrador, Recepcionista e Veterinário; o portal do cliente é uma extensão além dos 17
requisitos.

Era necessário decidir como o tutor autenticaria, como o pedido de agendamento entraria na agenda
e qual o limite de autonomia do cliente sobre os pets.

## Decisão

- O tutor passa a ter `senha`, `ativo` e `primeiro_acesso` na tabela `tutores`. A senha é definida
  pela recepção no cadastro/edição do tutor.
- O login do cliente usa **CPF + senha** em `/cliente/login`, com JWT próprio contendo
  `tipo=CLIENTE`, `tutorId`, nome, e-mail e CPF. A API valida `ROLE_CLIENTE` e impede que esse token
  acesse áreas internas.
- No primeiro acesso o cliente é obrigado a trocar a senha temporária.
- O cliente pode **ver, cadastrar e editar** apenas os próprios pets. Exclusão permanece com a
  clínica. O vínculo com o tutor é sempre atribuído pelo servidor, nunca pelo cliente.
- O cliente **solicita** agendamento com pet, data/horário preferidos e motivo. A solicitação nasce
  `PENDENTE`; a recepção **aprova** escolhendo veterinário e horário (gerando a consulta oficial) ou
  **recusa** com motivo. O cliente acompanha o status no portal.
- A aprovação reutiliza o `ConsultaService`, portanto valida conflito, vínculo animal/tutor e
  perfil do veterinário.

## Status

Aceita.

## Consequências

**Positivas:**
- O cliente resolve cadastro e solicitação sem ligar para a clínica.
- A recepção mantém o controle da agenda e pode remanejar o horário pedido.
- O isolamento por `tutorId` impede acesso a pets e pedidos de outros clientes.
- O fluxo de aprovação preserva as regras de negócio já existentes.

**Negativas:**
- O portal acrescenta um quarto perfil de acesso não previsto no documento original.
- A senha inicial depende da recepção definir; tutores antigos ficam sem acesso até isso ocorrer.
- Não há recuperação de senha do cliente (depende do REQ03) nem auto-cadastro.
- O cliente não pode cancelar o próprio pedido nesta versão.
