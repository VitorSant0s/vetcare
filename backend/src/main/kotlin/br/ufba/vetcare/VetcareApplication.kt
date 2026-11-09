package br.ufba.vetcare

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.TimeZone

@SpringBootApplication
@EnableCaching
@EnableScheduling
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
