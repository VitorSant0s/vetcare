import { Link } from 'react-router-dom'
import { ArrowLeft, LockKeyhole, ShieldCheck } from 'lucide-react'

const secoes = [
  {
    titulo: '1. Quem é o controlador',
    texto:
      'O VetCare é operado pela clínica veterinária responsável pelo seu atendimento. A clínica define as finalidades e os meios de tratamento dos dados pessoais e pode ser contatada pelo e-mail informado no cadastro.',
  },
  {
    titulo: '2. Quais dados tratamos',
    texto:
      'Dados de identificação e contato do tutor (nome, CPF, e-mail, telefone e endereço), dados dos animais (nome, espécie, raça, sexo, nascimento e histórico clínico), consultas, atendimentos, prescrições, vacinas e informações de pagamento. Também registramos logs de acesso e auditoria.',
  },
  {
    titulo: '3. Para que usamos',
    texto:
      'Prestar atendimento veterinário, manter prontuário clínico, agendar consultas, emitir prescrições, controlar vacinação, processar pagamentos, enviar avisos sobre consultas e vacinas, cumprir obrigações legais e proteger o sistema contra fraudes e acessos indevidos.',
  },
  {
    titulo: '4. Bases legais',
    texto:
      'Tratamos dados para execução de contrato e procedimentos preliminares, cumprimento de obrigação legal ou regulatória, exercício regular de direitos, proteção da vida e tutela da saúde, além de legítimo interesse para segurança da informação. Quando exigido, solicitamos consentimento específico.',
  },
  {
    titulo: '5. Compartilhamento',
    texto:
      'Os dados podem ser acessados por profissionais e pela recepção da clínica, conforme o perfil e a necessidade do atendimento. Não vendemos dados pessoais. Compartilhamentos com prestadores de serviço ocorrem apenas para viabilizar o funcionamento do sistema, com obrigações de confidencialidade.',
  },
  {
    titulo: '6. Retenção',
    texto:
      'Dados clínicos e financeiros são mantidos pelo prazo necessário ao atendimento e ao cumprimento de obrigações legais, incluindo defesa em processos. Registros de auditoria são mantidos para segurança e rastreabilidade. Após os prazos aplicáveis, os dados são eliminados ou anonimizados.',
  },
  {
    titulo: '7. Seus direitos',
    texto:
      'Você pode solicitar confirmação de tratamento, acesso, correção, portabilidade, informação sobre compartilhamento, revogação de consentimento e eliminação de dados pessoais. No portal do cliente é possível baixar uma cópia dos seus dados e solicitar a exclusão da conta. A exclusão anonimiza os dados pessoais, mas preserva registros clínicos e financeiros quando houver obrigação legal de retenção.',
  },
  {
    titulo: '8. Segurança',
    texto:
      'Adotamos senhas com hash, tokens de acesso com expiração e revogação, controle de acesso por perfil, registro de auditoria, proteção contra força bruta, cabeçalhos de segurança e restrição de origem. Nenhum sistema é totalmente imune a incidentes; caso ocorra, a clínica adotará as medidas de contenção e comunicação previstas na LGPD.',
  },
  {
    titulo: '9. Cookies e armazenamento local',
    texto:
      'O portal utiliza armazenamento local do navegador para guardar o token de sessão. Não utilizamos cookies de publicidade. O token é removido no logout e expira automaticamente.',
  },
  {
    titulo: '10. Contato e atualizações',
    texto:
      'Para exercer direitos ou esclarecer dúvidas, procure a clínica pelo canal de atendimento informado no cadastro. Esta política pode ser atualizada; a versão vigente é identificada pelo número 1.0 e pela data de atualização.',
  },
]

export function Privacidade() {
  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-4xl items-center gap-3 px-5 py-4">
          <Link
            to="/login"
            className="rounded-lg border border-slate-200 bg-white p-2 text-slate-500 hover:bg-slate-50"
            aria-label="Voltar"
          >
            <ArrowLeft className="h-4 w-4" />
          </Link>
          <div>
            <p className="text-xs font-medium tracking-widest text-brand-600 uppercase">VetCare</p>
            <h1 className="text-xl font-semibold text-slate-900">Política de Privacidade e LGPD</h1>
          </div>
          <div className="ml-auto hidden h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700 sm:flex">
            <ShieldCheck className="h-5 w-5" />
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-4xl px-5 py-8">
        <div className="mb-6 rounded-xl border border-brand-200 bg-brand-50 px-4 py-3 text-sm text-brand-900">
          <p className="flex items-center gap-2 font-medium">
            <LockKeyhole className="h-4 w-4" /> Versão 1.0 · atualizada em 10/11/2026
          </p>
          <p className="mt-1 text-brand-800">
            Este documento explica como a clínica trata os dados pessoais de tutores e animais no
            VetCare, em conformidade com a Lei Geral de Proteção de Dados (Lei 13.709/2018).
          </p>
        </div>

        <div className="space-y-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          {secoes.map((secao) => (
            <section key={secao.titulo}>
              <h2 className="text-base font-semibold text-slate-900">{secao.titulo}</h2>
              <p className="mt-1 text-sm leading-relaxed text-slate-600">{secao.texto}</p>
            </section>
          ))}
        </div>

        <p className="mt-6 text-center text-xs text-slate-500">
          Dúvidas sobre privacidade? Fale com a clínica pelo canal de atendimento do seu cadastro.
        </p>
      </main>
    </div>
  )
}
