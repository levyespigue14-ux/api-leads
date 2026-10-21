package dev.joaolevy.leads.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class RelogioConfig {

    /** Relógio injetável: nos testes dá pra fixar a data de "hoje". */
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Fortaleza"));
    }
}
