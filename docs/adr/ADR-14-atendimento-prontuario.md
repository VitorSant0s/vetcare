# ADR-14 — Atendimento como elo entre consulta, prontuário e prescrição

## Contexto

A Sprint 3 implementa o bloco clínico: REQ10 (registrar atendimento), REQ11 (prontuário),
REQ12 (vacinação) e REQ13 (prescrição). Era preciso decidir como esses dados se relacionam com a
consulta já existente e como o prontuário seria montado sem duplicar informação.

## Decisão

- Um **Atendimento** é criado a partir de uma **Consulta** e mantém vínculo 1:1 com ela
  (`atendimentos.consulta_id` é único). Ao registrar o atendimento, a consulta passa para o status
  `REALIZADA`.
- O atendimento guarda anamnese, diagnóstico, procedimentos e observações, além do animal e do
  veterinário responsáveis.
- **Prescrições** são filhas do atendimento (composição, `ON DELETE CASCADE`) e fazem parte da
  resposta do atendimento.
- **Vacinações** são independentes da consulta, ligadas diretamente ao animal, com data de
  aplicação e previsão da próxima dose.
- O **Prontuário** é uma visão agregada por animal: dados do animal/tutor, atendimentos com
  prescrições, vacinações e consultas. Não é uma tabela própria; é montado pelos serviços de
  leitura. Cada acesso gera registro de auditoria (`PRONTUARIO_ACESSADO`).

## Status

Aceita.

## Consequências

**Positivas:**
- O histórico clínico é rastreável e imutável por atendimento.
- Não há duplicação entre consulta, atendimento e prontuário.
- Prescrições e vacinas ficam disponíveis na mesma tela do histórico.
- O acesso a dados sensíveis é auditado.

**Negativas:**
- Um atendimento só existe vinculado a uma consulta; atendimentos de urgência sem agendamento
  precisarão de fluxo próprio no futuro.
- Cancelar ou alterar uma consulta já realizada exige regras adicionais (não implementadas nesta
  sprint).
- O prontuário agrega várias consultas por requisição; com histórico muito grande será necessário
  paginar os atendimentos.
