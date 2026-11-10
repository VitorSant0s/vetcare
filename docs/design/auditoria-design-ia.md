# Auditoria de design e acessibilidade — VetCare

Data: 10/11/2026
Escopo: SPA React/Tailwind do VetCare (portais administrativo, recepção, veterinário e cliente).

## Método

1. Revisão dos componentes compartilhados (`Botao`, `Campo`, `Modal`, `Toast`, `Layout`).
2. Cálculo de contraste WCAG 2.2 para as cores de texto e fundos usados nas telas.
3. Inspeção de navegação por teclado e estrutura semântica no código.
4. Verificação de estados de carregamento, vazio, erro e responsividade.
5. Correções de alto impacto aplicadas no design system e nas telas.

## Achados e correções

| # | Achado | Severidade | Evidência | Correção aplicada |
|---|---|---|---|---|
| 1 | Texto secundário em `slate-400` sobre branco | Alta | Contraste 2,56:1 (WCAG AA exige 4,5:1) | Trocado globalmente para `slate-500` (4,76:1) |
| 2 | Hero da recepção com branco sobre `amber-500` | Alta | Contraste 2,15:1 | Gradiente alterado para `amber-700`/`brand-800` (5,02:1) |
| 3 | Hero do veterinário terminando em `brand-500` | Média | Branco sobre `brand-500` = 3,12:1 | Gradiente termina em `brand-600` (4,65:1) |
| 4 | Modal sem `role="dialog"`, `aria-modal`, foco preso ou retorno de foco | Alta | Componente `Modal` | `role`, `aria-modal`, `aria-labelledby`, foco inicial, Tab preso, Escape fecha e foco retorna ao gatilho |
| 5 | Menu mobile sem fechar com Escape e sem `aria-expanded` | Média | `Layout` e `LayoutCliente` | Escape fecha, botão informa `aria-expanded` e `aria-controls` |
| 6 | Ausência de link “Pular para o conteúdo” | Média | Layouts | Skip link visível ao focar, apontando para `#conteudo` |
| 7 | Campos obrigatórios só com asterisco visual | Média | `Campo` | Asterisco com `aria-hidden` + texto “(obrigatório)” para leitor de tela |
| 8 | Mensagens de erro sem anúncio | Média | `Campo` | `role="alert"` nas mensagens |
| 9 | Loading sem anúncio | Baixa | `Carregando` | `role="status"` e `aria-live="polite"` |
| 10 | Animações sem respeitar preferência do usuário | Baixa | CSS global | `prefers-reduced-motion` desativa animações/transições |
| 11 | Impressão de relatórios incluía menu e botões | Baixa | CSS | `@media print` oculta navegação e botões |
| 12 | Contraste de textos pequenos em cards e tabelas | Média | Várias telas | Substituição global de `text-slate-400` por `text-slate-500` |
| 13 | Erros de formulário não associados ao campo | Alta | `Campo` | Contexto de campo com `id`, `aria-invalid`, `aria-describedby` e erro com `role="alert"` |
| 14 | Cabeçalhos de tabela sem escopo | Média | Listagens | `scope="col"` em todas as colunas |
| 15 | Menu mobile sem foco preso | Média | Layouts | Foco inicial, Tab preso, Escape e retorno ao botão |
| 16 | Falta de teste automatizado de acessibilidade | Alta | CI | Playwright + axe-core cobrindo logins e modal |

## O que já estava correto

- Um único `h1` por página e hierarquia de títulos consistente.
- Botões de ícone com `aria-label` (“Editar”, “Excluir”, “Fechar menu”).
- `NavLink` com `aria-current` automático.
- Formulários com `<label>` real em volta do campo, não apenas placeholder.
- Foco visível global (`:focus-visible` com outline de 2px).
- Toasts com `role="status"`.
- Layout responsivo com menu lateral fixo no desktop e drawer no mobile.
- Estados de carregamento, vazio, erro e confirmação nas telas principais.
- SVGs decorativos com `aria-hidden`.

## Pendências recomendadas

| Prioridade | Item |
|---|---|
| Alta | Rodar Lighthouse além do axe e acompanhar Core Web Vitals |
| Média | Adicionar `<caption>` descritivo em cada tabela de dados |
| Média | Ampliar testes de teclado para menu mobile e todos os formulários |
| Média | Testes axe nas demais telas autenticadas (agenda, prontuário, financeiro) |
| Baixa | Revisar texto alternativo de gráficos e barras do financeiro |
| Baixa | Code splitting: bundle principal em ~630 kB |
| Baixa | Documentar tokens de design em um arquivo único |

## Fluxo de redesign assistido por IA proposto

1. **Briefing e inventário:** mapear telas, componentes e estados existentes.
2. **Estrutura:** usar Relume/Figma AI apenas para sitemap, wireframes e exploração de layout.
3. **Componentes:** gerar peças React/Tailwind com v0, sempre dentro dos componentes e tokens já existentes.
4. **Contrato de acessibilidade no prompt:** HTML semântico, foco visível, labels reais, contraste AA, teclado, retorno de foco e estados de erro.
5. **Revisão humana:** comparar com o design system, testar teclado, zoom 200%/400%, leitor de tela e responsividade.
6. **Automação:** TypeScript estrito, ESLint jsx-a11y, axe/Playwright, testes de componentes e snapshots visuais.
7. **Medição:** Core Web Vitals, contraste, tamanho de bundle e cobertura de testes.

## Automação implementada

- `frontend/e2e/a11y.spec.ts` com Playwright + axe-core.
- Cobertura atual: login interno, login do cliente e modal de usuário (foco preso, Escape e retorno de foco).
- Regra de falha: violações `critical` ou `serious` quebram a suíte.
- Job `e2e` no GitHub Actions instala Chromium e roda `npm run test:e2e`.
- Relatório do Playwright é publicado como artefato quando a suíte falha.

## Resultado

As correções de contraste e acessibilidade foram aplicadas ao código. O design mantém a identidade visual (verde VetCare, cartões claros, tipografia Inter) e ganhou foco visível, leitura por teclado e melhor legibilidade.
