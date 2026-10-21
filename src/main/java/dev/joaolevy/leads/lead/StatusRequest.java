package dev.joaolevy.leads.lead;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusRequest(@NotNull LeadStatus status, @Size(max = 500) String observacao) {
}
