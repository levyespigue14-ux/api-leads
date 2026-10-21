package dev.joaolevy.leads.common;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponse(LocalDateTime momento, int status, String erro, Map<String, String> campos) {

    public static ErroResponse de(int status, String erro) {
        return new ErroResponse(LocalDateTime.now(), status, erro, Map.of());
    }
}
