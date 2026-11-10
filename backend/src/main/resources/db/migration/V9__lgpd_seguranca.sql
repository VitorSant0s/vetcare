-- Sprint 8: LGPD e endurecimento de segurança.

alter table usuarios add column token_version integer not null default 0;

alter table tutores add column token_version integer not null default 0;
alter table tutores add column consentimento_em timestamp with time zone;
alter table tutores add column consentimento_versao varchar(20);
alter table tutores add column anonimizado boolean not null default false;
alter table tutores add column exclusao_solicitada_em timestamp with time zone;
