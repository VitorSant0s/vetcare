package br.ufba.vetcare.domain.repository

import br.ufba.vetcare.domain.Auditoria
import org.springframework.data.jpa.repository.JpaRepository

interface AuditoriaRepository : JpaRepository<Auditoria, Long>
