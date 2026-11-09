-- Garante no banco que um veterinário não tenha duas consultas no mesmo horário.
-- O serviço normaliza a data/hora para o minuto antes de gravar.
create unique index uk_consultas_veterinario_horario
    on consultas (veterinario_id, data_hora);
