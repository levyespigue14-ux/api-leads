package dev.joaolevy.leads.lead;

import dev.joaolevy.leads.common.ConflitoException;
import dev.joaolevy.leads.common.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class LeadService {

    /** Status em que o lead ainda está esperando resposta. */
    private static final List<LeadStatus> AGUARDANDO = List.of(LeadStatus.ABORDADO, LeadStatus.FOLLOWUP);

    private final LeadRepository repository;
    private final Clock clock;
    private final int diasAteFollowup;

    public LeadService(LeadRepository repository, Clock clock,
                       @Value("${leads.dias-ate-followup:3}") int diasAteFollowup) {
        this.repository = repository;
        this.clock = clock;
        this.diasAteFollowup = diasAteFollowup;
    }

    @Transactional
    public LeadResponse criar(LeadRequest dados) {
        String whatsapp = normalizarTelefone(dados.whatsapp());
        if (repository.existsByWhatsapp(whatsapp)) {
            throw new ConflitoException("Já existe um lead com o WhatsApp " + whatsapp);
        }
        Lead lead = new Lead();
        preencher(lead, dados, whatsapp);
        return LeadResponse.de(repository.save(lead));
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> listar(LeadStatus status, String nicho, String cidade, Pageable pageable) {
        Specification<Lead> filtro = Specification.where(LeadFiltros.comStatus(status))
                .and(LeadFiltros.comNicho(nicho))
                .and(LeadFiltros.naCidade(cidade));
        return repository.findAll(filtro, pageable).map(LeadResponse::de);
    }

    @Transactional(readOnly = true)
    public LeadResponse buscar(Long id) {
        return LeadResponse.de(encontrar(id));
    }

    @Transactional
    public LeadResponse atualizar(Long id, LeadRequest dados) {
        Lead lead = encontrar(id);
        String whatsapp = normalizarTelefone(dados.whatsapp());
        if (repository.existsByWhatsappAndIdNot(whatsapp, id)) {
            throw new ConflitoException("Já existe outro lead com o WhatsApp " + whatsapp);
        }
        preencher(lead, dados, whatsapp);
        return LeadResponse.de(lead);
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(encontrar(id));
    }

    /** Marca a primeira abordagem e já agenda o follow-up. */
    @Transactional
    public LeadResponse registrarAbordagem(Long id) {
        Lead lead = encontrar(id);
        if (lead.getStatus() != LeadStatus.NOVO) {
            throw new ConflitoException("Só leads novos podem ser abordados (status atual: " + lead.getStatus() + ")");
        }
        LocalDate hoje = LocalDate.now(clock);
        lead.setStatus(LeadStatus.ABORDADO);
        lead.setAbordadoEm(hoje);
        lead.setFollowupEm(hoje.plusDays(diasAteFollowup));
        return LeadResponse.de(lead);
    }

    /** Registra que o follow-up foi feito e agenda o próximo. */
    @Transactional
    public LeadResponse registrarFollowup(Long id) {
        Lead lead = encontrar(id);
        if (!AGUARDANDO.contains(lead.getStatus())) {
            throw new ConflitoException("Follow-up só vale para leads abordados (status atual: " + lead.getStatus() + ")");
        }
        lead.setStatus(LeadStatus.FOLLOWUP);
        lead.setFollowupEm(LocalDate.now(clock).plusDays(diasAteFollowup));
        return LeadResponse.de(lead);
    }

    @Transactional
    public LeadResponse mudarStatus(Long id, StatusRequest pedido) {
        Lead lead = encontrar(id);
        lead.setStatus(pedido.status());
        if (!AGUARDANDO.contains(pedido.status())) {
            lead.setFollowupEm(null);   // respondeu, fechou ou desistiu: não precisa mais de follow-up
        }
        if (pedido.observacao() != null && !pedido.observacao().isBlank()) {
            lead.setObservacao(pedido.observacao());
        }
        return LeadResponse.de(lead);
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> followupsPendentes() {
        return repository.findByStatusInAndFollowupEmLessThanEqualOrderByFollowupEmAsc(AGUARDANDO, LocalDate.now(clock))
                .stream().map(LeadResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ResumoResponse resumo() {
        Map<LeadStatus, Long> porStatus = new EnumMap<>(LeadStatus.class);
        for (LeadStatus s : LeadStatus.values()) {
            porStatus.put(s, repository.countByStatus(s));
        }
        long pendentes = repository
                .findByStatusInAndFollowupEmLessThanEqualOrderByFollowupEmAsc(AGUARDANDO, LocalDate.now(clock)).size();
        return new ResumoResponse(repository.count(), porStatus, pendentes);
    }

    /** Deixa só os dígitos e coloca o 55 do Brasil quando faltar. */
    static String normalizarTelefone(String telefone) {
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() == 10 || digitos.length() == 11) {
            digitos = "55" + digitos;
        }
        return digitos;
    }

    private Lead encontrar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lead " + id + " não encontrado"));
    }

    private static void preencher(Lead lead, LeadRequest dados, String whatsapp) {
        lead.setNome(dados.nome().trim());
        lead.setNicho(dados.nicho().trim());
        lead.setCidade(dados.cidade().trim());
        lead.setWhatsapp(whatsapp);
        lead.setInstagram(dados.instagram());
        lead.setAvaliacao(dados.avaliacao());
        lead.setObservacao(dados.observacao());
    }
}
