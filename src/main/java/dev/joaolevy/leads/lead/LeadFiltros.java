package dev.joaolevy.leads.lead;

import org.springframework.data.jpa.domain.Specification;

/** Filtros opcionais da listagem: cada um só entra na consulta se for informado. */
final class LeadFiltros {

    private LeadFiltros() {
    }

    static Specification<Lead> comStatus(LeadStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    static Specification<Lead> comNicho(String nicho) {
        return (root, query, cb) -> vazio(nicho) ? null
                : cb.equal(cb.lower(root.get("nicho")), nicho.trim().toLowerCase());
    }

    static Specification<Lead> naCidade(String cidade) {
        return (root, query, cb) -> vazio(cidade) ? null
                : cb.equal(cb.lower(root.get("cidade")), cidade.trim().toLowerCase());
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
