-- Sprint 7: bloqueio de agenda (dia de trabalho) por veterinário.

create table bloqueios_agenda (
    id bigserial primary key,
    veterinario_id bigint not null,
    data date not null,
    motivo varchar(500) not null,
    criado_em timestamp with time zone not null default now(),
    constraint uk_bloqueios_veterinario_data unique (veterinario_id, data),
    constraint fk_bloqueios_veterinario foreign key (veterinario_id) references usuarios (id)
);

create index idx_bloqueios_data on bloqueios_agenda (data);
