"""Gera a apresentação da Sprint 2 do VetCare.

Uso:
    uv run --with python-pptx python docs/slides/gerar_slides.py
"""

from pathlib import Path

from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.util import Inches, Pt, Emu

RAIZ = Path(__file__).resolve().parents[2]
IMG = RAIZ / "docs" / "relatorio" / "img"
SAIDA = RAIZ / "docs" / "slides" / "VetCare-Sprint-2.pptx"

VERDE = RGBColor(0x12, 0x6A, 0x53)
VERDE_CLARO = RGBColor(0xEE, 0xFB, 0xF5)
VERDE_MEDIO = RGBColor(0x22, 0xA5, 0x7C)
ESCURO = RGBColor(0x1E, 0x29, 0x3B)
CINZA = RGBColor(0x64, 0x74, 0x8B)
BRANCO = RGBColor(0xFF, 0xFF, 0xFF)
FUNDO = RGBColor(0xF8, 0xFA, 0xFC)

LARGURA = Inches(13.333)
ALTURA = Inches(7.5)

prs = Presentation()
prs.slide_width = LARGURA
prs.slide_height = ALTURA


def slide_novo(fundo=FUNDO):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    bg = slide.background.fill
    bg.solid()
    bg.fore_color.rgb = fundo
    return slide


def caixa(slide, x, y, w, h, texto="", tamanho=18, cor=ESCURO, negrito=False,
          alinhamento=PP_ALIGN.LEFT, fonte="Segoe UI", margem=0):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_right = Inches(margem)
    tf.margin_top = tf.margin_bottom = 0
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
    caixa(slide, 0.75, 0.5, 11.8, 0.7, texto, 30, VERDE, True)
    if subtitulo:
        caixa(slide, 0.75, 1.18, 11.8, 0.4, subtitulo, 14, CINZA)
    barra = slide.shapes.add_shape(1, Inches(0.75), Inches(1.62), Inches(1.1), Inches(0.07))
    barra.fill.solid()
    barra.fill.fore_color.rgb = VERDE_MEDIO
    barra.line.fill.background()


def bullets(slide, itens, x=0.85, y=1.9, w=11.6, h=4.8, tamanho=17, espaco=8):
    box = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = box.text_frame
    tf.word_wrap = True
    for i, item in enumerate(itens):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.space_after = Pt(espaco)
        p.line_spacing = 1.15
        if isinstance(item, tuple):
            texto, nivel = item
        else:
            texto, nivel = item, 0
        p.level = nivel
        run = p.add_run()
        run.text = ("• " if nivel == 0 else "– ") + texto
        run.font.size = Pt(tamanho - nivel * 2)
        run.font.color.rgb = ESCURO if nivel == 0 else CINZA
        run.font.name = "Segoe UI"
        run.font.bold = nivel == 0 and i == 0
    return box


def tabela(slide, cabecalho, linhas, x=0.8, y=1.95, w=11.7, h=3.8,
           larguras=None, tamanho=12):
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
        cell.fill.fore_color.rgb = VERDE
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
            cell.fill.fore_color.rgb = BRANCO if i % 2 else VERDE_CLARO
            cell.vertical_anchor = MSO_ANCHOR.MIDDLE
            for p in cell.text_frame.paragraphs:
                for r in p.runs:
                    r.font.size = Pt(tamanho)
                    r.font.color.rgb = ESCURO
                    r.font.name = "Segoe UI"
    return table


def cartao(slide, x, y, w, h, rotulo, valor, cor=VERDE):
    card = slide.shapes.add_shape(1, Inches(x), Inches(y), Inches(w), Inches(h))
    card.fill.solid()
    card.fill.fore_color.rgb = BRANCO
    card.line.color.rgb = RGBColor(0xE2, 0xE8, 0xF0)
    caixa(slide, x + 0.25, y + 0.25, w - 0.5, 0.35, rotulo, 12, CINZA)
    caixa(slide, x + 0.25, y + 0.62, w - 0.5, 0.6, valor, 22, cor, True)


def imagem(slide, nome, x, y, largura, legenda=None):
    caminho = IMG / nome
    pic = slide.shapes.add_picture(str(caminho), Inches(x), Inches(y), width=Inches(largura))
    if legenda:
        caixa(slide, x, y + pic.height / 914400 + 0.12, largura, 0.35, legenda, 11, CINZA,
              alinhamento=PP_ALIGN.CENTER)
    return pic


def rodape(slide, numero):
    caixa(slide, 0.75, 7.02, 8, 0.3, "VetCare · Sprint 2 · Engenharia de Software 1 · UFBA 2026", 9, CINZA)
    caixa(slide, 11.9, 7.02, 0.7, 0.3, str(numero), 9, CINZA, alinhamento=PP_ALIGN.RIGHT)


# ---------------------------------------------------------------- slide 1
s = slide_novo(VERDE)
faixa = s.shapes.add_shape(1, Inches(0), Inches(0), Inches(0.22), ALTURA)
faixa.fill.solid()
faixa.fill.fore_color.rgb = VERDE_MEDIO
caixa(s, 1.0, 1.0, 11, 0.4, "UNIVERSIDADE FEDERAL DA BAHIA · INSTITUTO DE COMPUTAÇÃO", 14, RGBColor(0xAE, 0xEA, 0xD0), True)
caixa(s, 1.0, 2.15, 11.3, 1.8, "VetCare", 62, BRANCO, True)
caixa(s, 1.0, 3.45, 10.8, 1.2, "Sprint 2 — portais por perfil,\nveterinários e agenda completa", 30, RGBColor(0xD5, 0xF5, 0xE5))
caixa(s, 1.0, 5.2, 10.8, 0.5, "Trabalho II · Implementação de ~50% dos requisitos funcionais", 16, RGBColor(0xAE, 0xEA, 0xD0))
caixa(s, 1.0, 6.4, 6, 0.4, "Vitor de Jesus Santos", 15, BRANCO, True)
caixa(s, 1.0, 6.8, 8, 0.4, "Prof.ª Dra. Larissa Barbosa Leôncio Pinheiro · Novembro de 2026", 12, RGBColor(0xAE, 0xEA, 0xD0))

# ---------------------------------------------------------------- slide 2
s = slide_novo()
titulo(s, "Objetivo e entregáveis", "O que esta sprint se propõe a entregar")
bullets(s, [
    "Implementar aproximadamente 50% dos requisitos funcionais do VetCare",
    "Manter a arquitetura definida na Etapa 1, registrando desvios em ADRs",
    "Entregar o sistema em funcionamento, não apenas protótipos",
    "Repositório público no GitHub com README e instruções de execução",
    "Documento PDF e apresentação com escopo, decisões e evidências",
], 0.85, 2.0, 6.4, 4.4, 17)

cartao(s, 7.6, 2.05, 2.35, 1.35, "Requisitos", "8 de 17")
cartao(s, 10.15, 2.05, 2.35, 1.35, "Cobertura", "47,1%")
cartao(s, 7.6, 3.65, 2.35, 1.35, "Testes back", "40")
cartao(s, 10.15, 3.65, 2.35, 1.35, "Testes front", "10")
caixa(s, 7.6, 5.4, 4.9, 1.0, "Login único com portais distintos para admin, recepção e veterinário.", 14, CINZA)
rodape(s, 2)

# ---------------------------------------------------------------- slide 3
s = slide_novo()
titulo(s, "Recorte acumulado", "Oito requisitos de prioridade Alta fecham o módulo de agenda")
tabela(s,
       ["Req.", "Descrição", "Prioridade", "Situação"],
       [
           ["REQ01", "Manter Usuário — CRUD e perfis", "Alta", "Implementado"],
           ["REQ02", "Autenticar Usuário — login e JWT", "Alta", "Implementado"],
           ["REQ04", "Manter Tutor — CRUD", "Alta", "Implementado"],
           ["REQ05", "Manter Animal — vínculo com tutor", "Alta", "Implementado"],
           ["REQ06", "Manter Veterinário — CRMV e especialidade", "Alta", "Implementado"],
           ["REQ07", "Agendar Consulta — com validação de conflito", "Alta", "Implementado"],
           ["REQ08", "Cancelar Consulta — libera o horário", "Alta", "Implementado"],
           ["REQ09", "Consultar Agenda — dia, semana e profissional", "Alta", "Implementado"],
       ],
       y=1.9, h=4.5, larguras=[1.4, 6.3, 1.8, 2.2], tamanho=11)
caixa(s, 0.85, 6.55, 11.6, 0.5,
      "Fora do escopo: REQ03, REQ10–REQ17. Login único com portais por perfil.", 12, CINZA)
rodape(s, 3)

# ---------------------------------------------------------------- slide 4
s = slide_novo()
titulo(s, "Arquitetura preservada da Etapa 1", "Cliente-servidor com API REST em camadas")
camadas = [
    ("Apresentação", "SPA React 19 + Tailwind — portais admin, recepção e veterinário", VERDE_CLARO, VERDE),
    ("API", "Controllers REST, DTOs, validação, CORS, Swagger, erros", BRANCO, ESCURO),
    ("Aplicação", "Casos de uso: auth, usuários, tutores, animais, veterinários, agenda, auditoria", BRANCO, ESCURO),
    ("Domínio", "Entidades, perfis, status e interfaces de repositório", BRANCO, ESCURO),
    ("Infraestrutura", "JWT/Bcrypt, JPA, Flyway, PostgreSQL, Redis, Actuator", VERDE_CLARO, VERDE),
]
y = 1.95
for nome, descricao, fundo, cor in camadas:
    box = s.shapes.add_shape(1, Inches(0.85), Inches(y), Inches(11.6), Inches(0.78))
    box.fill.solid()
    box.fill.fore_color.rgb = fundo
    box.line.color.rgb = RGBColor(0xCD, 0xEE, 0xE0) if fundo == VERDE_CLARO else RGBColor(0xE2, 0xE8, 0xF0)
    caixa(s, 1.05, y + 0.14, 2.5, 0.5, nome, 15, cor, True)
    caixa(s, 3.5, y + 0.18, 8.7, 0.5, descricao, 12, CINZA)
    y += 0.9
caixa(s, 0.85, 6.55, 11.6, 0.4,
      "ADR-10/13: veterinário = usuário com CRMV · ADR-11: gateway/BFF na camada api", 11, CINZA)
rodape(s, 4)

# ---------------------------------------------------------------- slide 5
s = slide_novo()
titulo(s, "Stack e modelo de dados")
caixa(s, 0.85, 1.95, 5.5, 0.4, "Tecnologias", 16, VERDE, True)
bullets(s, [
    "Kotlin 2.3 + Spring Boot 4.1 + Maven Wrapper",
    "Spring Security, JWT, BCrypt, RBAC",
    "Spring Data JPA + Flyway + PostgreSQL 16",
    "Redis 7 para cache de agenda",
    "React 19 + Vite + TypeScript + Tailwind",
    "React Hook Form + Zod + Axios",
], 0.95, 2.45, 5.6, 3.6, 14, 6)

caixa(s, 6.8, 1.95, 5.6, 0.4, "Entidades", 16, VERDE, True)
tabela(s,
       ["Tabela", "Papel"],
       [
           ["usuarios", "Contas, perfis, CRMV e especialidade"],
           ["tutores", "Responsáveis pelos animais"],
           ["animais", "Pacientes vinculados ao tutor"],
           ["consultas", "Agenda, horário ativo e cancelamento"],
           ["auditoria", "Trilha imutável de eventos"],
       ],
       x=6.8, y=2.45, w=5.7, h=2.9, larguras=[1.8, 3.9], tamanho=12)
caixa(s, 6.85, 5.6, 5.6, 0.8, "CPF e e-mail únicos; exclusões lógicas preservam o histórico.", 12, CINZA)
rodape(s, 5)

# ---------------------------------------------------------------- slide 6
s = slide_novo()
titulo(s, "Segurança e controle de acesso", "RBAC aplicado na borda da API")
tabela(s,
       ["Perfil", "Portal", "Permissões na sprint"],
       [
           ["Administrador", "/portal/admin", "Indicadores, usuários, veterinários, cadastros, agenda e cancelamento"],
           ["Recepcionista", "/portal/recepcao", "Agenda do dia/semana, cadastros, agendamento e cancelamento"],
           ["Veterinário", "/portal/veterinario", "Leitura de cadastros e apenas a própria agenda"],
       ],
       y=1.95, h=2.1, larguras=[2.0, 2.3, 7.4], tamanho=11)
bullets(s, [
    "Senhas com BCrypt; token JWT com perfil e validade de 120 minutos",
    "Segredo JWT sem valor padrão e seed opt-in (ADR-12)",
    "Portais protegidos no front e permissões revalidadas na API",
    "401 sem token e 403 sem permissão, com JSON padronizado",
    "Auditoria de login, falhas de login e todas as mutações (usuário, IP, data/hora)",
    "Teste automatizado comprova que recepcionista não acessa /api/usuarios",
], 0.9, 4.3, 11.5, 2.5, 13, 4)
rodape(s, 6)

# ---------------------------------------------------------------- slide 7
s = slide_novo()
titulo(s, "Agenda completa (REQ07, REQ08 e REQ09)", "Agendar, cancelar e visualizar por dia, semana e profissional")
bullets(s, [
    "Animal e tutor existem, estão ativos e o animal pertence ao tutor informado",
    "Profissional tem perfil VETERINARIO e está ativo",
    "Data/hora obrigatoriamente futura",
    "Sem conflito: o veterinário não pode ter outra consulta no mesmo horário",
    "Cancelamento libera o horário, exige motivo e gera auditoria",
    "Visões de dia e semana com navegação e filtro por profissional",
    "Agendamentos e cancelamentos invalidam o cache Redis da agenda",
    "Veterinário enxerga apenas a própria agenda; recepção e admin veem tudo",
], 0.85, 1.95, 11.5, 3.6, 16, 8)
caixa(s, 0.9, 5.75, 11.5, 0.8,
      "Erro de conflito retorna 409 com mensagem clara: “O veterinário já possui uma consulta neste horário.”",
      14, VERDE, True)
rodape(s, 7)

# ---------------------------------------------------------------- slide 8
s = slide_novo()
titulo(s, "Sistema em funcionamento", "Capturas da aplicação real com dados de demonstração")
imagem(s, "09-portal-admin.png", 0.7, 1.85, 6.6, "Portal administrativo com indicadores e atalhos")
imagem(s, "10-portal-recepcao.png", 7.75, 1.85, 4.9, "Portal da recepção: agenda do dia e ações rápidas")
rodape(s, 8)

# ---------------------------------------------------------------- slide 9
s = slide_novo()
titulo(s, "Portais e novas operações", "A mesma autenticação, visões diferentes por perfil")
imagem(s, "11-portal-veterinario.png", 0.7, 1.85, 5.9, "Portal do veterinário: apenas a própria agenda")
imagem(s, "12-veterinarios.png", 6.85, 1.85, 5.9, "Gestão de veterinários com CRMV (REQ06)")
rodape(s, 9)

# ---------------------------------------------------------------- slide 10
s = slide_novo()
titulo(s, "Testes e evidências", "Verificação automatizada e ensaio de ponta a ponta")
tabela(s,
       ["Suíte", "Testes", "Cobertura principal"],
       [
           ["Back-end (JUnit + MockMvc)", "40", "Auth, RBAC, CRUDs, auditoria, cache, conflito e cancelamento"],
           ["Front-end (Vitest + RTL)", "10", "Formatadores, login, datas e contas de demonstração"],
       ],
       y=1.95, h=1.6, larguras=[3.8, 1.3, 6.6], tamanho=12)
caixa(s, 0.85, 3.85, 5.7, 0.4, "Ensaio real contra a stack", 15, VERDE, True)
bullets(s, [
    "POST tutor → 201 · POST animal → 201",
    "POST consulta → 201 · repetição → 409",
    "POST /cancelar → 200 · reagendar → 201",
    "GET agenda → consulta criada presente",
    "GET /usuarios: recepção 403, admin 200",
    "Health-check: db UP e redis UP",
], 0.95, 4.3, 6.0, 2.3, 13, 5)
caixa(s, 6.9, 3.85, 5.7, 0.4, "Auditoria registrada", 15, VERDE, True)
caixa(s, 6.9, 4.35, 5.6, 1.8,
      "CONSULTA_AGENDADA · Consulta 2\nANIMAL_CRIADO · Animal 4\nTUTOR_CRIADO · Tutor 3\nLOGIN_SUCESSO · Usuario 2",
      13, ESCURO, fonte="Consolas")
rodape(s, 10)

# ---------------------------------------------------------------- slide 11
s = slide_novo()
titulo(s, "ADRs e próximos passos")
caixa(s, 0.85, 1.95, 5.6, 0.4, "Decisões registradas", 16, VERDE, True)
bullets(s, [
    "ADR-10: veterinário como usuário com perfil",
    "ADR-11: API Gateway/BFF na camada api",
    "ADR-12: segredo JWT e seed opt-in",
    "ADR-13: CRMV e especialidade em Usuario",
    "ADR-01 a ADR-09 mantidas sem alteração",
], 0.95, 2.4, 5.6, 2.0, 14, 6)

caixa(s, 6.9, 1.95, 5.6, 0.4, "Próximas sprints", 16, VERDE, True)
bullets(s, [
    "REQ10/11: atendimento e prontuário",
    "REQ12/13: vacinação e prescrições",
    "REQ14–17: financeiro, notificações e relatórios",
], 7.0, 2.4, 5.6, 3.2, 13, 5)
caixa(s, 0.9, 5.65, 11.5, 0.7, "A base operacional está pronta para evoluir com baixo acoplamento.", 17, VERDE, True)
rodape(s, 11)

# ---------------------------------------------------------------- slide 12
s = slide_novo(VERDE)
caixa(s, 1.0, 2.3, 11, 1.0, "Obrigado!", 54, BRANCO, True)
caixa(s, 1.0, 3.55, 11, 0.5, "VetCare — Sprint 2", 24, RGBColor(0xD5, 0xF5, 0xE5))
caixa(s, 1.0, 4.5, 11, 0.4, "Vitor de Jesus Santos · Engenharia de Software 1 · UFBA 2026", 15, RGBColor(0xAE, 0xEA, 0xD0))
caixa(s, 1.0, 5.4, 11, 0.5, "github.com/<usuario>/vetcare", 14, RGBColor(0xAE, 0xEA, 0xD0))

SAIDA.parent.mkdir(parents=True, exist_ok=True)
prs.save(SAIDA)
print(f"Apresentação gerada: {SAIDA}")
