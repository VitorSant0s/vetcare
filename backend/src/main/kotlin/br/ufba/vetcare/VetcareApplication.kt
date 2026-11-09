package br.ufba.vetcare

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import java.util.TimeZone

@SpringBootApplication
@EnableCaching
class VetcareApplication {

    /**
     * A agenda trabalha com data/hora local da clínica. Fixar o fuso evita divergência
     * quando a aplicação roda em um contêiner configurado em UTC.
     */
    @PostConstruct
    fun configurarFusoHorario() {
        TimeZone.setDefault(TimeZone.getTimeZone(System.getenv("TZ") ?: "America/Bahia"))
    }
}

fun main(args: Array<String>) {
    runApplication<VetcareApplication>(*args)
}
