package dev.joaolevy.leads.lead;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Dados para cadastrar ou atualizar um lead. */
public record LeadRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 60) String nicho,
        @NotBlank @Size(max = 80) String cidade,
        @NotBlank
        @Pattern(regexp = "^[0-9()+\\-\\s]{10,20}$", message = "telefone inválido")
        String whatsapp,
        @Size(max = 60) String instagram,
        @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal avaliacao,
        @Size(max = 500) String observacao
) {
}
