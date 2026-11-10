import { useForm, useFieldArray } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Trash2 } from 'lucide-react'
import type { Consulta } from '../types'
import { atendimentosApi, mensagemDeErro } from '../lib/api'
import { formatarDataHora } from '../lib/format'
import { AreaTexto, Botao, Campo, Entrada, Modal } from './ui'
import { useState } from 'react'

const schema = z.object({
  anamnese: z.string().optional(),
  diagnostico: z.string().min(3, 'Informe o diagnóstico'),
  procedimentos: z.string().optional(),
  observacoes: z.string().optional(),
  prescricoes: z
    .array(
      z.object({
        medicamento: z.string().min(1, 'Informe o medicamento'),
        dosagem: z.string().optional(),
        frequencia: z.string().optional(),
        duracao: z.string().optional(),
        orientacoes: z.string().optional(),
      }),
    )
    .max(20, 'Máximo de 20 prescrições'),
})

type Formulario = z.infer<typeof schema>

const vazio: Formulario = {
  anamnese: '',
  diagnostico: '',
  procedimentos: '',
  observacoes: '',
  prescricoes: [],
}

export function AtendimentoModal({
  consulta,
  onFechar,
  onSucesso,
}: {
  consulta: Consulta | null
  onFechar: () => void
  onSucesso: (animalId: number) => void
}) {
  const [salvando, setSalvando] = useState(false)
  const [erroApi, setErroApi] = useState('')
  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<Formulario>({ resolver: zodResolver(schema), defaultValues: vazio })
  const { fields, append, remove } = useFieldArray({ control, name: 'prescricoes' })

  function fechar() {
    reset(vazio)
    setErroApi('')
    onFechar()
  }

  async function salvar(dados: Formulario) {
    if (!consulta) return
    setSalvando(true)
    setErroApi('')
    try {
      await atendimentosApi.registrar(consulta.id, {
        anamnese: dados.anamnese?.trim() || null,
        diagnostico: dados.diagnostico.trim(),
        procedimentos: dados.procedimentos?.trim() || null,
        observacoes: dados.observacoes?.trim() || null,
        prescricoes: dados.prescricoes.map((p) => ({
          medicamento: p.medicamento.trim(),
          dosagem: p.dosagem?.trim() || null,
          frequencia: p.frequencia?.trim() || null,
          duracao: p.duracao?.trim() || null,
          orientacoes: p.orientacoes?.trim() || null,
        })),
      })
      const animalId = consulta.animalId
      reset(vazio)
      onSucesso(animalId)
    } catch (error) {
      setErroApi(mensagemDeErro(error))
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      aberto={!!consulta}
      onFechar={fechar}
      titulo="Registrar atendimento"
      descricao={
        consulta
          ? `${consulta.animalNome} · ${consulta.tutorNome} · ${formatarDataHora(consulta.dataHora)}`
          : undefined
      }
      largura="max-w-2xl"
    >
      <form onSubmit={handleSubmit(salvar)} className="space-y-4" noValidate>
        <Campo label="Anamnese" erro={errors.anamnese?.message}>
          <AreaTexto rows={2} placeholder="Queixa, histórico e evolução do quadro" {...register('anamnese')} />
        </Campo>
        <Campo label="Diagnóstico" erro={errors.diagnostico?.message} obrigatorio>
          <AreaTexto rows={2} placeholder="Diagnóstico do atendimento" {...register('diagnostico')} />
        </Campo>
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo label="Procedimentos realizados" erro={errors.procedimentos?.message}>
            <AreaTexto rows={2} placeholder="Exame físico, exames, curativos" {...register('procedimentos')} />
          </Campo>
          <Campo label="Observações" erro={errors.observacoes?.message}>
            <AreaTexto rows={2} placeholder="Retorno, cuidados e recomendações" {...register('observacoes')} />
          </Campo>
        </div>

        <div className="rounded-xl border border-slate-200 p-3">
          <div className="mb-2 flex items-center justify-between">
            <p className="text-sm font-medium text-slate-700">Prescrições</p>
            <Botao
              type="button"
              variante="secundario"
              className="px-3 py-1.5 text-xs"
              onClick={() =>
                append({ medicamento: '', dosagem: '', frequencia: '', duracao: '', orientacoes: '' })
              }
            >
              <Plus className="h-3.5 w-3.5" /> Adicionar
            </Botao>
          </div>
          {fields.length === 0 ? (
            <p className="py-2 text-xs text-slate-500">
              Nenhuma prescrição adicionada. O atendimento pode ser salvo sem prescrição.
            </p>
          ) : (
            <div className="space-y-3">
              {fields.map((field, indice) => (
                <div key={field.id} className="rounded-lg bg-slate-50 p-3">
                  <div className="mb-2 flex items-center justify-between">
                    <span className="text-xs font-medium text-slate-500">Prescrição {indice + 1}</span>
                    <button
                      type="button"
                      onClick={() => remove(indice)}
                      className="rounded p-1 text-slate-500 hover:bg-red-50 hover:text-red-600"
                      aria-label="Remover prescrição"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                  <div className="grid gap-2 sm:grid-cols-2">
                    <Entrada placeholder="Medicamento *" {...register(`prescricoes.${indice}.medicamento`)} />
                    <Entrada placeholder="Dosagem" {...register(`prescricoes.${indice}.dosagem`)} />
                    <Entrada placeholder="Frequência" {...register(`prescricoes.${indice}.frequencia`)} />
                    <Entrada placeholder="Duração" {...register(`prescricoes.${indice}.duracao`)} />
                  </div>
                  <Entrada
                    className="mt-2"
                    placeholder="Orientações"
                    {...register(`prescricoes.${indice}.orientacoes`)}
                  />
                  {errors.prescricoes?.[indice]?.medicamento && (
                    <p className="mt-1 text-xs text-red-600">
                      {errors.prescricoes[indice]?.medicamento?.message}
                    </p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {erroApi && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {erroApi}
          </div>
        )}

        <div className="flex justify-end gap-2">
          <Botao type="button" variante="contorno" onClick={fechar} disabled={salvando}>
            Cancelar
          </Botao>
          <Botao type="submit" carregando={salvando}>
            Salvar atendimento
          </Botao>
        </div>
      </form>
    </Modal>
  )
}
