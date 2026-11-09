-- Sprint 4: financeiro, notificações e relatórios.

create table pagamentos (
    id bigserial primary key,
    tutor_id bigint not null,
    consulta_id bigint,
    valor numeric(10,2) not null,
    forma_pagamento varchar(30) not null,
    status varchar(20) not null default 'PAGO',
    data_pagamento date not null,
    observacoes varchar(500),
    criado_em timestamp with time zone not null default now(),
    constraint fk_pagamentos_tutor foreign key (tutor_id) references tutores (id),
    constraint fk_pagamentos_consulta foreign key (consulta_id) references consultas (id),
    constraint ck_pagamentos_forma check (forma_pagamento in ('DINHEIRO','PIX','CARTAO_CREDITO','CARTAO_DEBITO','CONVENIO')),
    constraint ck_pagamentos_status check (status in ('PAGO','PENDENTE','CANCELADO')),
    constraint ck_pagamentos_valor check (valor >= 0)
);

create index idx_pagamentos_data on pagamentos (data_pagamento);
create index idx_pagamentos_tutor on pagamentos (tutor_id);
create index idx_pagamentos_consulta on pagamentos (consulta_id);

create table notificacoes (
    id bigserial primary key,
    tutor_id bigint not null,
    consulta_id bigint,
    vacinacao_id bigint,
    tipo varchar(20) not null,
    canal varchar(20) not null default 'EMAIL',
    destinatario varchar(160) not null,
    mensagem varchar(500) not null,
    status varchar(20) not null default 'PENDENTE',
    erro varchar(300),
    criado_em timestamp with time zone not null default now(),
    enviado_em timestamp with time zone,
    constraint fk_notificacoes_tutor foreign key (tutor_id) references tutores (id),
    constraint fk_notificacoes_consulta foreign key (consulta_id) references consultas (id),
    constraint fk_notificacoes_vacinacao foreign key (vacinacao_id) references vacinacoes (id),
    constraint ck_notificacoes_tipo check (tipo in ('CONSULTA','VACINA')),
    constraint ck_notificacoes_status check (status in ('PENDENTE','ENVIADA','FALHA'))
);

create index idx_notificacoes_status on notificacoes (status);
create index idx_notificacoes_tutor on notificacoes (tutor_id);
