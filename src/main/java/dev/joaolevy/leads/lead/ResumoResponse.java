package dev.joaolevy.leads.lead;

import java.util.Map;

/** Quantidade de leads em cada etapa do funil e follow-ups vencidos. */
public record ResumoResponse(long total, Map<LeadStatus, Long> porStatus, long followupsPendentes) {
}
