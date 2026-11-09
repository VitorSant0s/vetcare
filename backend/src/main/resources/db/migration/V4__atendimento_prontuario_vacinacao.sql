-- Sprint 3: atendimento, prontuário, prescrições e vacinação.

create table atendimentos (
    id bigserial primary key,
    consulta_id bigint not null,
    animal_id bigint not null,
    veterinario_id bigint not null,
    data_hora timestamp not null,
    anamnese varchar(2000),
    diagnostico varchar(2000) not null,
    procedimentos varchar(2000),
    observacoes varchar(2000),
    criado_em timestamp with time zone not null default now(),
    constraint uk_atendimentos_consulta unique (consulta_id),
    constraint fk_atendimentos_consulta foreign key (consulta_id) references consultas (id),
    constraint fk_atendimentos_animal foreign key (animal_id) references animais (id),
    constraint fk_atendimentos_veterinario foreign key (veterinario_id) references usuarios (id)
);

create index idx_atendimentos_animal on atendimentos (animal_id, data_hora);

create table prescricoes (
    id bigserial primary key,
    atendimento_id bigint not null,
    medicamento varchar(160) not null,
    dosagem varchar(120),
    frequencia varchar(120),
    duracao varchar(120),
    orientacoes varchar(500),
    constraint fk_prescricoes_atendimento foreign key (atendimento_id) references atendimentos (id) on delete cascade
);

create index idx_prescricoes_atendimento on prescricoes (atendimento_id);

create table vacinacoes (
    id bigserial primary key,
    animal_id bigint not null,
    veterinario_id bigint,
    tipo_vacina varchar(120) not null,
    lote varchar(60),
    data_aplicacao date not null,
    proxima_dose date,
    observacoes varchar(500),
    criado_em timestamp with time zone not null default now(),
    constraint fk_vacinacoes_animal foreign key (animal_id) references animais (id),
    constraint fk_vacinacoes_veterinario foreign key (veterinario_id) references usuarios (id)
);

create index idx_vacinacoes_animal on vacinacoes (animal_id, data_aplicacao);
create index idx_vacinacoes_proxima_dose on vacinacoes (proxima_dose);
