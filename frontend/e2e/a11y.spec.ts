import AxeBuilder from '@axe-core/playwright'
import { expect, test, type Page } from '@playwright/test'

const WCAG = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa']

async function semViolacoesGraves(page: Page) {
  const resultado = await new AxeBuilder({ page }).withTags(WCAG).analyze()
  const graves = resultado.violations.filter((violacao) =>
    ['critical', 'serious'].includes(violacao.impact ?? ''),
  )
  expect(graves, JSON.stringify(graves, null, 2)).toEqual([])
}

const admin = {
  id: 1,
  nome: 'Administrador',
  email: 'admin@vetcare.com',
  perfil: 'ADMINISTRADOR',
  perfilDescricao: 'Administrador',
  ativo: true,
  criadoEm: '2026-01-01T00:00:00Z',
}

test('login interno sem violações graves de acessibilidade', async ({ page }) => {
  await page.goto('/login')
  await expect(page.getByRole('heading', { name: /acesse sua conta/i })).toBeVisible()
  await semViolacoesGraves(page)
})

test('login do cliente sem violações graves de acessibilidade', async ({ page }) => {
  await page.goto('/cliente/login')
  await expect(page.getByRole('heading', { name: /acesse o portal/i })).toBeVisible()
  await semViolacoesGraves(page)
})

test('modal de usuário prende o foco, fecha com Escape e passa no axe', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('vetcare.token', 'token-de-teste'))
  await page.route('**/api/auth/me', (route) => route.fulfill({ json: admin }))
  await page.route('**/api/usuarios**', (route) =>
    route.fulfill({
      json: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 },
    }),
  )

  await page.goto('/usuarios')
  await page.getByRole('button', { name: /novo usuário/i }).click()

  const dialog = page.getByRole('dialog')
  await expect(dialog).toBeVisible()
  await expect
    .poll(() =>
      page.evaluate(() => {
        const elemento = document.querySelector('[role="dialog"]')
        return !!elemento && elemento.contains(document.activeElement)
      }),
    )
    .toBe(true)

  await semViolacoesGraves(page)

  await page.keyboard.press('Escape')
  await expect(dialog).toBeHidden()
  await expect(page.getByRole('button', { name: /novo usuário/i })).toBeFocused()
})
