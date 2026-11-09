# ADR-15 — Notificações por caixa de saída com canal simulado

## Contexto

O REQ16 exige avisar o tutor sobre consultas agendadas e datas previstas de vacinação. A Etapa 1
(ADR-06) definiu processamento assíncrono por serviço externo de e-mail/SMS. Integrar um provedor
real exigiria credenciais, custo e disponibilidade externa, o que não é adequado para a entrega
acadêmica nem para o ambiente de demonstração.

## Decisão

- As notificações são persistidas em uma tabela `notificacoes` (caixa de saída) com tipo, canal,
  destinatário, mensagem, status, erro e datas de criação/envio.
- A geração é feita para consultas nas próximas 24 horas e vacinas com próxima dose nos próximos
  7 dias, com deduplicação por consulta/vacina.
- O envio passa por uma porta `CanalNotificacao`. Nesta sprint o adaptador `EmailSimuladoCanal`
  registra o envio no log e marca a notificação como `ENVIADA`.
- Um agendador diário (`NotificacaoScheduler`, às 08:00) gera e processa os avisos. A tela de
  notificações também permite disparar o processamento manualmente.
- O agendamento pode ser desligado por `app.notificacoes.agendamento=false` (usado nos testes).

## Status

Aceita.

## Consequências

**Positivas:**
- O fluxo de aviso funciona sem depender de serviço externo.
- A caixa de saída preserva histórico e status, permitindo auditoria e reenvio.
- Trocar o adaptador simulado por um provedor real não altera as regras de negócio.

**Negativas:**
- Nesta sprint o aviso não chega de fato ao tutor; é apenas registrado no log.
- O processamento é síncrono dentro da chamada; em produção deve ir para fila/worker.
- Não há retentativa automática de falhas nem confirmação de leitura.
