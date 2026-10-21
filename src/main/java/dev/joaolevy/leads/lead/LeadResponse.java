package dev.joaolevy.leads.lead;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeadResponse(
        Long id,
        String nome,
        String nicho,
        String cidade,
        String whatsapp,
        String linkWhatsapp,
        String instagram,
        BigDecimal avaliacao,
        LeadStatus status,
        LocalDate abordadoEm,
        LocalDate followupEm,
        String observacao,
        LocalDateTime criadoEm
) {

    static LeadResponse de(Lead lead) {
        return new LeadResponse(
                lead.getId(), lead.getNome(), lead.getNicho(), lead.getCidade(),
                lead.getWhatsapp(), "https://wa.me/" + lead.getWhatsapp(),
                lead.getInstagram(), lead.getAvaliacao(), lead.getStatus(),
                lead.getAbordadoEm(), lead.getFollowupEm(), lead.getObservacao(), lead.getCriadoEm());
    }
}
