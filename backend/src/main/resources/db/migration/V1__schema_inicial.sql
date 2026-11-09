-- VetCare - esquema inicial (Sprint 1)
-- Requisitos contemplados: REQ01, REQ02, REQ04, REQ05, REQ07

create table usuarios (
    id bigserial primary key,
    nome varchar(120) not null,
    email varchar(160) not null,
    senha varchar(100) not null,
    perfil varchar(20) not null,
    ativo boolean not null default true,
    criado_em timestamp with time zone not null default now(),
    atualizado_em timestamp with time zone not null default now(),
    constraint uk_usuarios_email unique (email),
    constraint ck_usuarios_perfil check (perfil in ('ADMINISTRADOR', 'RECEPCIONISTA', 'VETERINARIO'))
);

create index idx_usuarios_perfil on usuarios (perfil);

create table tutores (
    id bigserial primary key,
    nome varchar(120) not null,
    cpf varchar(14) not null,
    email varchar(160) not null,
    telefone varchar(20) not null,
    endereco varchar(200),
    criado_em timestamp with time zone not null default now(),
    atualizado_em timestamp with time zone not null default now(),
    constraint uk_tutores_cpf unique (cpf)
);

create index idx_tutores_nome on tutores (nome);

create table animais (
    id bigserial primary key,
    nome varchar(120) not null,
    especie varchar(60) not null,
    raca varchar(60),
    sexo varchar(20),
    data_nascimento date,
    tutor_id bigint not null,
    ativo boolean not null default true,
    criado_em timestamp with time zone not null default now(),
    atualizado_em timestamp with time zone not null default now(),
    constraint fk_animais_tutor foreign key (tutor_id) references tutores (id)
);

create index idx_animais_tutor on animais (tutor_id);
create index idx_animais_nome on animais (nome);

create table consultas (
    id bigserial primary key,
    animal_id bigint not null,
    tutor_id bigint not null,
    veterinario_id bigint not null,
    data_hora timestamp not null,
    status varchar(20) not null default 'AGENDADA',
    observacoes varchar(500),
    criado_em timestamp with time zone not null default now(),
    constraint fk_consultas_animal foreign key (animal_id) references animais (id),
    constraint fk_consultas_tutor foreign key (tutor_id) references tutores (id),
    constraint fk_consultas_veterinario foreign key (veterinario_id) references usuarios (id),
    constraint ck_consultas_status check (status in ('AGENDADA', 'CANCELADA', 'REALIZADA'))
);

create index idx_consultas_data_hora on consultas (data_hora);
create index idx_consultas_veterinario_data on consultas (veterinario_id, data_hora);

create table auditoria (
    id bigserial primary key,
    usuario_email varchar(160),
    acao varchar(60) not null,
    entidade varchar(60),
    entidade_id bigint,
    detalhes varchar(500),
    ip varchar(45),
    criado_em timestamp with time zone not null default now()
);

create index idx_auditoria_criado_em on auditoria (criado_em);
