"""Gera a apresentação da Etapa 2 do VetCare no estilo da Etapa 1.

Uso:
    uv run --with python-pptx python docs/slides/gerar_slides_etapa2.py
"""

from pathlib import Path

from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.util import Inches, Pt

RAIZ = Path(__file__).resolve().parents[2]
IMG = RAIZ / "docs" / "relatorio" / "img"
ASSETS = RAIZ / "docs" / "slides" / "assets"
SAIDA = RAIZ / "docs" / "slides" / "VetCare-Etapa-2.pptx"

VINHO = RGBColor(0x6B, 0x1F, 0x2A)
VINHO_MEDIO = RGBColor(0x9B, 0x2C, 0x3B)
VINHO_CLARO = RGBColor(0xF7, 0xEC, 0xEE)
TEXTO = RGBColor(0x2B, 0x2B, 0x2B)
CINZA = RGBColor(0x6B, 0x72, 0x80)
CINZA_CLARO = RGBColor(0xF7, 0xF5, 0xF4)
BRANCO = RGBColor(0xFF, 0xFF, 0xFF)

LARGURA = Inches(13.333)
ALTURA = Inches(7.5)

prs = Presentation()
prs.slide_width = LARGURA
prs.slide_height = ALTURA


def slide_novo(fundo=BRANCO):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    fill = slide.background.fill
    fill.solid()
    fill.fore_color.rgb = fundo
    return slide


def caixa(slide, x, y, w, h, texto="", tamanho=16, cor=TEXTO, negrito=False,
          alinhamento=PP_ALIGN.LEFT, fonte="Segoe UI"):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_right = tf.margin_top = tf.margin_bottom = 0
    p = tf.paragraphs[0]
    p.alignment = alinhamento
    run = p.add_run()
    run.text = texto
    run.font.size = Pt(tamanho)
    run.font.bold = negrito
    run.font.color.rgb = cor
    run.font.name = fonte
    return box


def titulo(slide, texto, subtitulo=None):
    caixa(slide, 0.7, 0.42, 12.0, 0.7, texto, 28, VINHO_MEDIO, True)
    if subtitulo:
        caixa(slide, 0.72, 1.07, 11.9, 0.4, subtitulo, 12, CINZA)
    barra = slide.shapes.add_shape(1, Inches(0.72), Inches(1.38), Inches(1.3), Inches(0.06))
    barra.fill.solid()
    barra.fill.fore_color.rgb = VINHO_MEDIO
    barra.line.fill.background()


def bullets(slide, itens, x=0.85, y=1.75, w=11.6, h=4.7, tamanho=15, espaco=7):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.word_wrap = True
    for i, item in enumerate(itens):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.space_after = Pt(espaco)
        p.line_spacing = 1.12
        if isinstance(item, tuple):
            texto, nivel = item
        else:
            texto, nivel = item, 0
        p.level = nivel
        run = p.add_run()
        run.text = ("•  " if nivel == 0 else "–  ") + texto
        run.font.size = Pt(tamanho - nivel * 2)
        run.font.color.rgb = TEXTO if nivel == 0 else CINZA
        run.font.name = "Segoe UI"
        run.font.bold = nivel == 0 and i == 0


def card(slide, x, y, w, h, titulo_card, texto_card, destaque=VINHO_MEDIO):
    fundo = slide.shapes.add_shape(1, Inches(x), Inches(y), Inches(w), Inches(h))
    fundo.fill.solid()
    fundo.fill.fore_color.rgb = CINZA_CLARO
    fundo.line.color.rgb = RGBColor(0xE8, 0xE3, 0xE1)
    faixa = slide.shapes.add_shape(1, Inches(x), Inches(y), Inches(0.07), Inches(h))
    faixa.fill.solid()
    faixa.fill.fore_color.rgb = destaque
    faixa.line.fill.background()
    caixa(slide, x + 0.25, y + 0.18, w - 0.45, 0.35, titulo_card, 14, destaque, True)
    caixa(slide, x + 0.25, y + 0.58, w - 0.45, h - 0.7, texto_card, 11.5, TEXTO)


def tabela(slide, cabecalho, linhas, x=0.8, y=1.8, w=11.8, h=4.2,
           larguras=None, tamanho=11.5):
    shape = slide.shapes.add_table(len(linhas) + 1, len(cabecalho), Inches(x), Inches(y),
                                   Inches(w), Inches(h))
    table = shape.table
    if larguras:
        for i, largura in enumerate(larguras):
            table.columns[i].width = Inches(largura)
    for j, texto in enumerate(cabecalho):
        cell = table.cell(0, j)
        cell.text = texto
        cell.fill.solid()
        cell.fill.fore_color.rgb = VINHO_MEDIO
        cell.vertical_anchor = MSO_ANCHOR.MIDDLE
        for p in cell.text_frame.paragraphs:
            for r in p.runs:
                r.font.size = Pt(tamanho)
                r.font.bold = True
                r.font.color.rgb = BRANCO
                r.font.name = "Segoe UI"
    for i, linha in enumerate(linhas, start=1):
        for j, texto in enumerate(linha):
            cell = table.cell(i, j)
            cell.text = str(texto)
            cell.fill.solid()
            cell.fill.fore_color.rgb = BRANCO if i % 2 else VINHO_CLARO
            cell.vertical_anchor = MSO_ANCHOR.MIDDLE
            for p in cell.text_frame.paragraphs:
                for r in p.runs:
                    r.font.size = Pt(tamanho)
                    r.font.color.rgb = TEXTO
                    r.font.name = "Segoe UI"
    return table


def imagem(slide, nome, x, y, largura, legenda=None):
    pic = slide.shapes.add_picture(str(IMG / nome), Inches(x), Inches(y), width=Inches(largura))
    if legenda:
        caixa(slide, x, y + pic.height / 914400 + 0.08, largura, 0.35, legenda, 10, CINZA,
              alinhamento=PP_ALIGN.CENTER)
    return pic


def rodape(slide, numero):
    caixa(slide, 0.7, 7.12, 8, 0.25, "VetCare · Trabalho II · Engenharia de Software 1 · UFBA 2026",
          8.5, CINZA)
    caixa(slide, 12.1, 7.12, 0.6, 0.25, str(numero), 8.5, CINZA, alinhamento=PP_ALIGN.RIGHT)


# ---------------------------------------------------------------- 1 capa
s = slide_novo(VINHO)
s.shapes.add_picture(str(ASSETS / "ufba.png"), Inches(10.95), Inches(0.45), width=Inches(1.25))
caixa(s, 0.8, 0.85, 9.9, 0.45, "Universidade Federal da Bahia", 20, BRANCO, True, PP_ALIGN.CENTER)
caixa(s, 0.8, 1.28, 9.9, 0.4, "Instituto de Computação", 18, BRANCO, True, PP_ALIGN.CENTER)
caixa(s, 0.9, 2.75, 9.6, 1.5, "Implementação do\nSistema VetCare", 42, BRANCO, True, PP_ALIGN.CENTER)
caixa(s, 0.9, 4.35, 9.6, 0.45, "Trabalho II — Etapa 2", 20, RGBColor(0xF0, 0xC9, 0xCF), False, PP_ALIGN.CENTER)
caixa(s, 0.8, 6.15, 6.2, 0.3, "Engenharia de Software 1", 14, BRANCO, True)
caixa(s, 0.8, 6.5, 6.2, 0.3, "Docente: Dra. Larissa Barbosa Leôncio Pinheiro", 10.5, RGBColor(0xF0, 0xC9, 0xCF))
caixa(s, 0.8, 6.85, 6.2, 0.3, "Discente: Vitor de Jesus Santos", 10.5, RGBColor(0xF0, 0xC9, 0xCF))
# pata decorativa
for (px, py, pr) in [(6.55, 5.95, 0.22), (7.15, 5.95, 0.22), (7.75, 5.95, 0.22), (6.95, 6.45, 0.42)]:
    oval = s.shapes.add_shape(9, Inches(px), Inches(py), Inches(pr), Inches(pr))
    oval.fill.solid()
    oval.fill.fore_color.rgb = RGBColor(0x8E, 0x3B, 0x47)
    oval.line.fill.background()

# ---------------------------------------------------------------- 2 contexto
s = slide_novo()
titulo(s, "Contexto e objetivos", "Da arquitetura definida na Etapa 1 para o sistema em funcionamento")
bullets(s, [
    "Sistema de gestão para clínicas veterinárias, com 17 requisitos funcionais.",
    "Etapa 1: arquitetura cliente-servidor em camadas, API REST, JWT, PostgreSQL, Redis e auditoria.",
    "Etapa 2: implementar o sistema, evoluindo de ~30% para 94,1% dos requisitos.",
    "Entregáveis: código, README, relatório PDF, apresentação, ADRs e repositório público.",
    "Diferencial: portal do cliente e recursos de LGPD/segurança além do escopo original.",
], 0.85, 1.75, 7.0, 4.6, 14.5, 8)
card(s, 8.1, 1.8, 4.2, 1.05, "Requisitos", "16 de 17 implementados (94,1%)")
card(s, 8.1, 3.0, 4.2, 1.05, "Perfis", "Administrador, Recepção, Veterinário e Cliente")
card(s, 8.1, 4.2, 4.2, 1.05, "Arquitetura", "SPA React + API Kotlin/Spring Boot em camadas")
card(s, 8.1, 5.4, 4.2, 1.05, "Qualidade", "Testes automatizados, acessibilidade e auditoria")
rodape(s, 2)

# ---------------------------------------------------------------- 3 escopo
s = slide_novo()
titulo(s, "Escopo implementado", "16 requisitos funcionais + extensões de produto")
tabela(s,
       ["Bloco", "Requisitos", "Entrega"],
       [
           ["Acesso e cadastros", "REQ01, REQ02, REQ04, REQ05, REQ06", "Usuários, login, tutores, animais e veterinários com CRMV"],
           ["Agenda", "REQ07, REQ08, REQ09", "Agendamento, cancelamento, visões dia/semana e bloqueio de dia"],
           ["Clínico", "REQ10, REQ11, REQ12, REQ13", "Atendimento, prontuário, vacinação e prescrição"],
           ["Gestão", "REQ14, REQ15, REQ16, REQ17", "Pagamentos, dashboard, notificações e relatórios"],
       ],
       y=1.8, h=3.4, larguras=[2.6, 4.2, 5.0], tamanho=12)
card(s, 0.85, 5.55, 7.6, 0.95, "Cobertura", "16/17 requisitos = 94,1%. Falta apenas o REQ03 (recuperação de senha).", VINHO)
card(s, 8.7, 5.55, 3.8, 0.95, "Extensões", "Portal do cliente, compartilhamento de prontuário, bloqueio de agenda, LGPD e auditoria.", VINHO)
rodape(s, 3)

# ---------------------------------------------------------------- 4 arquitetura
s = slide_novo()
titulo(s, "Arquitetura implementada", "Cliente-servidor com API REST e dependência unidirecional entre camadas")
camadas = [
    ("Apresentação", "SPA React + TypeScript + Tailwind — portais admin, recepção, veterinário e cliente", True),
    ("API", "Controllers REST, DTOs validados, Swagger, CORS, erros padronizados e cabeçalhos de segurança", False),
    ("Aplicação", "Casos de uso, regras de negócio, autorização, auditoria e notificações", False),
    ("Domínio", "Entidades, enums, validações e interfaces de repositório", False),
    ("Infraestrutura", "JWT, JPA, Flyway, PostgreSQL, Redis, SMTP e actuator", True),
]
y = 1.75
for nome, desc, destaque in camadas:
    fundo = s.shapes.add_shape(1, Inches(0.85), Inches(y), Inches(11.6), Inches(0.72))
    fundo.fill.solid()
    fundo.fill.fore_color.rgb = VINHO_CLARO if destaque else BRANCO
    fundo.line.color.rgb = RGBColor(0xE8, 0xE3, 0xE1)
    caixa(s, 1.05, y + 0.19, 2.4, 0.35, nome, 14, VINHO_MEDIO, True)
    caixa(s, 3.45, y + 0.22, 8.7, 0.35, desc, 11, TEXTO)
    y += 0.86
caixa(s, 0.85, 6.45, 11.6, 0.35,
      "ADR-11: API Gateway/BFF exercido pela camada de controllers do monólito.  ADR-13: veterinário como usuário com CRMV.",
      9.5, CINZA)
rodape(s, 4)

# ---------------------------------------------------------------- 5 stack
s = slide_novo()
titulo(s, "Stack e modelo de dados")
caixa(s, 0.85, 1.7, 5.4, 0.35, "Tecnologias", 15, VINHO_MEDIO, True)
bullets(s, [
    "Kotlin 2.3 + Spring Boot 4.1 + Maven Wrapper",
    "Spring Security, JWT, BCrypt e RBAC",
    "Spring Data JPA + Flyway + PostgreSQL 16",
    "Redis 7 para cache de agenda e prontuário",
    "React 19 + Vite + TypeScript + Tailwind",
    "Playwright + axe-core para acessibilidade",
], 0.95, 2.1, 5.2, 3.7, 12.5, 5)
caixa(s, 6.6, 1.7, 6.0, 0.35, "Entidades principais", 15, VINHO_MEDIO, True)
tabela(s,
       ["Tabela", "Papel"],
       [
           ["usuarios / tutores", "Acesso, perfis, consentimento e token"],
           ["animais / consultas", "Pacientes, agenda, bloqueios e cancelamentos"],
           ["atendimentos / prescricoes", "Histórico clínico e receitas"],
           ["vacinacoes", "Aplicação e próxima dose"],
           ["pagamentos / notificacoes", "Financeiro e caixa de saída"],
           ["solicitacoes / compartilhamentos", "Portal do cliente e códigos de prontuário"],
           ["auditoria", "Trilha imutável de eventos"],
       ],
       x=6.6, y=2.1, w=5.9, h=4.1, larguras=[2.6, 3.3], tamanho=10)
rodape(s, 5)

# ---------------------------------------------------------------- 6 portais
s = slide_novo()
titulo(s, "Portais por perfil", "Um único login para a equipe; o perfil define o portal e as permissões")
card(s, 0.85, 1.75, 5.85, 1.65, "Administrador", "Visão geral, usuários, veterinários, tutores, animais, agenda, financeiro, relatórios, notificações e auditoria.")
card(s, 6.85, 1.75, 5.85, 1.65, "Recepção", "Agenda do dia e da semana, cadastros, agendamento, cancelamento, vacinação, pagamentos e solicitações.")
card(s, 0.85, 3.65, 5.85, 1.65, "Veterinário", "Painel clínico, meus pacientes, prontuários, atendimento, prescrições, vacinação, compartilhamento e bloqueio de agenda.")
card(s, 6.85, 3.65, 5.85, 1.65, "Cliente", "Portal separado com login por CPF: pets, pedidos de agendamento, consentimento LGPD, exportação e exclusão de dados.")
caixa(s, 0.85, 5.65, 11.6, 0.8,
      "RBAC aplicado no front-end e revalidado na API. Tokens de cliente não acessam áreas internas.",
      13, VINHO, True)
rodape(s, 6)

# ---------------------------------------------------------------- 7 cadastros/agenda
s = slide_novo()
titulo(s, "Cadastros e agenda", "Base operacional e o módulo de agendamento")
bullets(s, [
    "CRUD de usuários, tutores, animais e veterinários, com validação de CPF e CRMV.",
    "Busca, paginação, exclusão lógica e auditoria em todas as mutações.",
    "Agendamento com validação de vínculo animal/tutor e conflito de horário.",
    "Cancelamento com motivo e liberação do horário.",
    "Visões por dia, semana e profissional; bloqueio de dia com motivo.",
    "Bloqueio cancela as consultas do dia, libera horários e notifica os tutores.",
], 0.85, 1.75, 6.3, 4.9, 13.5, 6)
imagem(s, "39-agenda-bloqueios.png", 7.3, 1.85, 5.3, "Agenda do veterinário com bloqueio de dia")
rodape(s, 7)

# ---------------------------------------------------------------- 8 clínico
s = slide_novo()
titulo(s, "Atendimento, prontuário e prescrição", "O registro clínico passa a compor o histórico do animal")
bullets(s, [
    "Atendimento vinculado à consulta, com anamnese, diagnóstico e procedimentos.",
    "A consulta passa para REALIZADA ao registrar o atendimento.",
    "Prescrições vinculadas ao atendimento, com medicamento, dose e duração.",
    "Prontuário consolida atendimentos, prescrições, vacinas e consultas.",
    "Vacinação com lote, data e previsão da próxima dose.",
    "Acesso ao prontuário auditado; veterinário vê apenas seus pacientes.",
], 0.85, 1.75, 6.3, 4.9, 13.5, 6)
imagem(s, "17-prontuario-luna.png", 7.3, 1.85, 5.3, "Prontuário com diagnóstico e prescrições")
rodape(s, 8)

# ---------------------------------------------------------------- 9 área médica
s = slide_novo()
titulo(s, "Área médica do veterinário", "Painel clínico, pacientes próprios e compartilhamento de prontuário")
bullets(s, [
    "Painel com agenda do dia, pacientes, vacinas a vencer e últimos atendimentos.",
    "Apenas animais que já tiveram consulta com o veterinário logado.",
    "Prontuários recentes e acesso rápido ao histórico completo.",
    "Compartilhamento por código de 8 caracteres, válido por 7 dias.",
    "Outro veterinário consulta em modo somente leitura; código pode ser revogado.",
    "Toda tentativa negada e acesso compartilhado é auditado.",
], 0.85, 1.75, 6.1, 4.9, 13, 6)
imagem(s, "31-portal-veterinario-clinico.png", 7.0, 1.85, 5.5, "Painel clínico do veterinário")
rodape(s, 9)

# ---------------------------------------------------------------- 10 financeiro
s = slide_novo()
titulo(s, "Financeiro, notificações e relatórios", "Fechamento administrativo da operação")
bullets(s, [
    "Pagamentos por tutor e consulta, com forma de pagamento e status.",
    "Dashboard com total recebido, ticket médio, atendimentos e distribuição por forma.",
    "Notificações de consulta e vacina em caixa de saída, com envio SMTP real.",
    "Relatório gerencial com consultas, atendimentos, animais, vacinas e finanças.",
    "Exportação CSV e impressão do relatório.",
], 0.85, 1.75, 4.9, 4.2, 12.5, 5)
imagem(s, "20-financeiro.png", 6.2, 1.8, 3.3, "Dashboard financeiro")
imagem(s, "22-relatorios.png", 9.7, 1.8, 3.3, "Relatório gerencial")
card(s, 6.2, 5.0, 6.8, 1.05, "Relatórios", "Exportação CSV, impressão e consolidação por período.", VINHO)
rodape(s, 10)

# ---------------------------------------------------------------- 11 cliente
s = slide_novo()
titulo(s, "Portal do cliente", "Uma extensão além dos 17 requisitos")
bullets(s, [
    "Login por CPF + senha definida pela recepção; troca no primeiro acesso.",
    "Cliente vê, cadastra e edita apenas os próprios pets.",
    "Pedido de agendamento com pet, data/horário e motivo.",
    "Recepção aprova escolhendo veterinário e horário, ou recusa com motivo.",
    "Consentimento LGPD, exportação dos dados e exclusão com anonimização.",
    "Isolamento por tutorId e JWT próprio; não acessa o sistema interno.",
], 0.85, 1.75, 5.8, 4.9, 12.5, 6)
imagem(s, "24-cliente-login.png", 6.5, 1.8, 3.1, "Login do cliente por CPF")
imagem(s, "25-portal-cliente.png", 9.75, 1.8, 3.1, "Início do portal do cliente")
card(s, 6.5, 4.65, 6.35, 1.3, "Fluxo do pedido", "Cliente solicita → recepção aprova ou recusa → consulta oficial é gerada. Consentimento e dados LGPD no portal.", VINHO)
rodape(s, 11)

# ---------------------------------------------------------------- 12 segurança/lgpd
s = slide_novo()
titulo(s, "Segurança, LGPD e auditoria", "Dados sensíveis tratados com rastreabilidade e controle")
bullets(s, [
    "Senhas com BCrypt e tokens JWT com perfil e versão.",
    "RBAC por perfil e isolamento de pacientes do veterinário.",
    "Rate limit de login: 5 tentativas em 15 minutos.",
    "Logout e troca de senha revogam tokens anteriores.",
    "Cabeçalhos HTTP de segurança e Cache-Control em APIs.",
    "Consentimento LGPD, exportação e exclusão com anonimização.",
    "Auditoria de logins, acessos, mutações, códigos e tentativas negadas.",
], 0.85, 1.75, 6.2, 4.9, 12.5, 5)
imagem(s, "38-auditoria.png", 7.2, 1.85, 5.3, "Trilha de auditoria no portal do administrador")
rodape(s, 12)

# ---------------------------------------------------------------- 13 testes
s = slide_novo()
titulo(s, "Testes e qualidade", "Verificação automatizada e evidências de funcionamento")
c1 = s.shapes.add_shape(1, Inches(0.85), Inches(1.8), Inches(3.65), Inches(1.55))
c1.fill.solid(); c1.fill.fore_color.rgb = VINHO_CLARO; c1.line.color.rgb = RGBColor(0xE8, 0xE3, 0xE1)
caixa(s, 1.0, 1.95, 3.35, 0.3, "Back-end", 12, CINZA)
caixa(s, 1.0, 2.3, 3.35, 0.7, "92", 32, VINHO, True)
caixa(s, 1.0, 2.95, 3.35, 0.3, "testes de integração", 10, CINZA)
c2 = s.shapes.add_shape(1, Inches(4.75), Inches(1.8), Inches(3.65), Inches(1.55))
c2.fill.solid(); c2.fill.fore_color.rgb = VINHO_CLARO; c2.line.color.rgb = RGBColor(0xE8, 0xE3, 0xE1)
caixa(s, 4.9, 1.95, 3.35, 0.3, "Front-end", 12, CINZA)
caixa(s, 4.9, 2.3, 3.35, 0.7, "10", 32, VINHO, True)
caixa(s, 4.9, 2.95, 3.35, 0.3, "testes unitários", 10, CINZA)
c3 = s.shapes.add_shape(1, Inches(8.65), Inches(1.8), Inches(3.65), Inches(1.55))
c3.fill.solid(); c3.fill.fore_color.rgb = VINHO_CLARO; c3.line.color.rgb = RGBColor(0xE8, 0xE3, 0xE1)
caixa(s, 8.8, 1.95, 3.35, 0.3, "Acessibilidade", 12, CINZA)
caixa(s, 8.8, 2.3, 3.35, 0.7, "4", 32, VINHO, True)
caixa(s, 8.8, 2.95, 3.35, 0.3, "E2E com Playwright + axe", 10, CINZA)
bullets(s, [
    "Back-end: autenticação, RBAC, CRUDs, agenda, clínica, financeiro, LGPD e segurança.",
    "Front-end: formatadores, login, datas e doses.",
    "E2E: login interno, login do cliente, política de privacidade e modal acessível.",
    "CI no GitHub Actions: back-end, front-end e acessibilidade.",
    "Contraste WCAG AA, foco preso em modais, skip link e navegação por teclado.",
], 0.9, 3.65, 11.5, 2.8, 12.5, 7)
rodape(s, 13)

# ---------------------------------------------------------------- 14 adrs
s = slide_novo()
titulo(s, "ADRs e decisões da Etapa 2", "Aderência à arquitetura da Etapa 1 e desvios registrados")
tabela(s,
       ["ADR", "Decisão", "Status"],
       [
           ["ADR-01 a 09", "Cliente-servidor REST, camadas, JWT, PostgreSQL, auditoria, Redis", "Mantidas"],
           ["ADR-10 / 13", "Veterinário como usuário, com CRMV e especialidade", "Aceitas"],
           ["ADR-11", "API Gateway/BFF na camada de controllers do monólito", "Aceita"],
           ["ADR-12 / 17", "Segredo JWT sem padrão, sem seed e envio real por SMTP", "Aceitas"],
           ["ADR-14", "Atendimento 1:1 com consulta; prontuário como visão agregada", "Aceita"],
           ["ADR-15", "Notificações em caixa de saída com porta de canal", "Aceita"],
           ["ADR-16", "Portal do cliente com CPF e aprovação de agendamento", "Aceita"],
           ["ADR-18", "LGPD, consentimento, exportação, exclusão e rate limit", "Aceita"],
       ],
       y=1.75, h=4.5, larguras=[2.2, 8.1, 1.5], tamanho=10.5)
caixa(s, 0.85, 6.5, 11.6, 0.35, "Todas as decisões estão em docs/adr, com contexto, decisão, status e consequências.", 9.5, CINZA)
rodape(s, 14)

# ---------------------------------------------------------------- 15 demo
s = slide_novo()
titulo(s, "Roteiro de demonstração", "Fluxo de ponta a ponta para a banca")
bullets(s, [
    "Administrador: login, visão geral, cadastro de recepção/veterinário e auditoria.",
    "Recepção: tutor, pet, agendamento e aprovação de solicitação do cliente.",
    "Veterinário: agenda, bloqueio de dia, atendimento, prontuário e prescrição.",
    "Veterinário: compartilhar prontuário por código e outro veterinário consultar.",
    "Recepção/Admin: pagamento, dashboard financeiro, notificação e relatório.",
    "Cliente: login por CPF, pets, pedido de agendamento e consentimento LGPD.",
    "Admin: conferir todos os eventos na trilha de auditoria.",
], 0.85, 1.75, 11.6, 4.7, 13.5, 8)
caixa(s, 0.9, 6.35, 11.5, 0.4, "Ambiente: PostgreSQL + Redis + API Kotlin + SPA React. Dados criados em tempo real, sem seed.", 10, CINZA)
rodape(s, 15)

# ---------------------------------------------------------------- 16 conclusão
s = slide_novo()
titulo(s, "Conclusão e próximos passos", "Sistema em funcionamento, testado e documentado")
bullets(s, [
    "16/17 requisitos implementados (94,1%), com portal do cliente como extensão.",
    "Arquitetura da Etapa 1 preservada; desvios documentados em ADRs.",
    "Fluxo completo: cadastro → agenda → atendimento → prontuário → financeiro → relatórios.",
    "Segurança, LGPD, auditoria e acessibilidade incorporadas ao produto.",
    "Próximo passo acadêmico: REQ03 (recuperação de senha) e apresentação final.",
    "Evolução: SMTP configurado, HTTPS, backup, monitoramento e relatórios em PDF.",
], 0.85, 1.75, 11.6, 4.5, 13.5, 8)
caixa(s, 0.9, 6.25, 11.5, 0.5, "Obrigado!", 24, VINHO, True, PP_ALIGN.CENTER)
rodape(s, 16)

SAIDA.parent.mkdir(parents=True, exist_ok=True)
prs.save(SAIDA)
print(f"Apresentação gerada: {SAIDA}")
