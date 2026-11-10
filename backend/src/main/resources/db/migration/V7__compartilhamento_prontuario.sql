-- Sprint 6: compartilhamento de prontuário por código.

create table compartilhamentos_prontuario (
    id bigserial primary key,
    animal_id bigint not null,
    criado_por bigint not null,
    codigo varchar(12) not null,
    expira_em timestamp with time zone not null,
    ativo boolean not null default true,
    criado_em timestamp with time zone not null default now(),
    constraint uk_compartilhamentos_codigo unique (codigo),
    constraint fk_compartilhamentos_animal foreign key (animal_id) references animais (id),
    constraint fk_compartilhamentos_usuario foreign key (criado_por) references usuarios (id)
);

create index idx_compartilhamentos_animal on compartilhamentos_prontuario (animal_id, ativo);
create index idx_compartilhamentos_codigo on compartilhamentos_prontuario (codigo);
