package dev.joaolevy.leads.lead;

import dev.joaolevy.leads.common.ConflitoException;
import dev.joaolevy.leads.common.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 21);

    @Mock
    private LeadRepository repository;

    private LeadService service;

    @BeforeEach
    void setUp() {
        Clock fixo = Clock.fixed(HOJE.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new LeadService(repository, fixo, 3);
    }

    @Test
    void normalizaTelefoneColocandoDdiDoBrasil() {
        assertThat(LeadService.normalizarTelefone("(86) 99999-0000")).isEqualTo("5586999990000");
        assertThat(LeadService.normalizarTelefone("+55 86 99999-0000")).isEqualTo("5586999990000");
    }

    @Test
    void naoCadastraWhatsappRepetido() {
        when(repository.existsByWhatsapp("5586999990000")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(pedido("(86) 99999-0000")))
                .isInstanceOf(ConflitoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void abordagemMudaStatusEAgendaFollowup() {
        Lead lead = leadNovo();
        when(repository.findById(1L)).thenReturn(Optional.of(lead));

        LeadResponse resposta = service.registrarAbordagem(1L);

        assertThat(resposta.status()).isEqualTo(LeadStatus.ABORDADO);
        assertThat(resposta.abordadoEm()).isEqualTo(HOJE);
        assertThat(resposta.followupEm()).isEqualTo(HOJE.plusDays(3));
    }

    @Test
    void naoAbordaLeadQueJaFoiAbordado() {
        Lead lead = leadNovo();
        lead.setStatus(LeadStatus.ABORDADO);
        when(repository.findById(1L)).thenReturn(Optional.of(lead));

        assertThatThrownBy(() -> service.registrarAbordagem(1L)).isInstanceOf(ConflitoException.class);
    }

    @Test
    void respostaDoClienteCancelaFollowup() {
        Lead lead = leadNovo();
        lead.setStatus(LeadStatus.ABORDADO);
        lead.setFollowupEm(HOJE);
        when(repository.findById(1L)).thenReturn(Optional.of(lead));

        LeadResponse resposta = service.mudarStatus(1L, new StatusRequest(LeadStatus.RESPONDEU, "pediu orçamento"));

        assertThat(resposta.status()).isEqualTo(LeadStatus.RESPONDEU);
        assertThat(resposta.followupEm()).isNull();
        assertThat(resposta.observacao()).isEqualTo("pediu orçamento");
    }

    @Test
    void leadInexistenteGeraNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private static LeadRequest pedido(String whatsapp) {
        return new LeadRequest("Confeitaria Teste", "confeitaria", "Teresina", whatsapp, null, null, null);
    }

    private static Lead leadNovo() {
        Lead lead = new Lead();
        lead.setNome("Confeitaria Teste");
        lead.setNicho("confeitaria");
        lead.setCidade("Teresina");
        lead.setWhatsapp("5586999990000");
        return lead;
    }
}
