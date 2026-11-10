-- Sprint 5: portal do cliente (tutor) e solicitações de agendamento.

alter table tutores add column senha varchar(100);
alter table tutores add column ativo boolean not null default true;
alter table tutores add column primeiro_acesso boolean not null default true;

create table solicitacoes_agendamento (
    id bigserial primary key,
    tutor_id bigint not null,
    animal_id bigint not null,
    data_preferida timestamp not null,
    motivo varchar(500),
    status varchar(20) not null default 'PENDENTE',
    observacao_resposta varchar(500),
    consulta_id bigint,
    criado_em timestamp with time zone not null default now(),
    analisado_em timestamp with time zone,
    constraint fk_solicitacoes_tutor foreign key (tutor_id) references tutores (id),
    constraint fk_solicitacoes_animal foreign key (animal_id) references animais (id),
    constraint fk_solicitacoes_consulta foreign key (consulta_id) references consultas (id),
    constraint ck_solicitacoes_status check (status in ('PENDENTE','APROVADA','RECUSADA','CANCELADA'))
);

create index idx_solicitacoes_status on solicitacoes_agendamento (status);
create index idx_solicitacoes_tutor on solicitacoes_agendamento (tutor_id);
create index idx_solicitacoes_animal on solicitacoes_agendamento (animal_id);
