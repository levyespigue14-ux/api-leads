package dev.joaolevy.leads.lead;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {

    boolean existsByWhatsapp(String whatsapp);

    boolean existsByWhatsappAndIdNot(String whatsapp, Long id);

    List<Lead> findByStatusInAndFollowupEmLessThanEqualOrderByFollowupEmAsc(Collection<LeadStatus> status,
                                                                          LocalDate data);

    long countByStatus(LeadStatus status);
}
