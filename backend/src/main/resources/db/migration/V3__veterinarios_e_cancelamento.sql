-- Sprint 2: atributos profissionais do veterinário e cancelamento de consultas.

alter table usuarios add column crmv varchar(30);
alter table usuarios add column especialidade varchar(120);

create unique index uk_usuarios_crmv on usuarios (crmv);

alter table consultas add column horario_ativo timestamp;
alter table consultas add column motivo_cancelamento varchar(300);
alter table consultas add column cancelada_em timestamp with time zone;

update consultas set horario_ativo = data_hora where status <> 'CANCELADA';

drop index uk_consultas_veterinario_horario;
create unique index uk_consultas_veterinario_horario_ativo
    on consultas (veterinario_id, horario_ativo);
