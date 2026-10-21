package dev.joaolevy.leads.lead;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testa a API de ponta a ponta (HTTP → serviço → banco H2). */
@SpringBootTest
@AutoConfigureMockMvc
class LeadApiTest {

    /** Hoje é 21/10/2026 nos testes; o follow-up vence em 3 dias. */
    @TestConfiguration
    static class RelogioDeTeste {
        @Bean
        @Primary
        Clock relogioFixo() {
            return Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private LeadRepository repository;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    @Test
    void cadastraLeadENormalizaWhatsapp() throws Exception {
        mvc.perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON)
                        .content(lead("Ana Cakes", "(86) 99981-0000")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.whatsapp").value("5586999810000"))
                .andExpect(jsonPath("$.linkWhatsapp").value("https://wa.me/5586999810000"))
                .andExpect(jsonPath("$.status").value("NOVO"));
    }

    @Test
    void recusaDadosInvalidos() throws Exception {
        mvc.perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"nicho\":\"confeitaria\",\"cidade\":\"Teresina\",\"whatsapp\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.whatsapp").exists());
    }

    @Test
    void recusaWhatsappRepetido() throws Exception {
        criar("Ana Cakes", "(86) 99981-0000");

        mvc.perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON)
                        .content(lead("Outra", "86 99981-0000")))
                .andExpect(status().isConflict());
    }

    @Test
    void leadInexistenteDa404() throws Exception {
        mvc.perform(get("/api/leads/999")).andExpect(status().isNotFound());
    }

    @Test
    void fluxoDeAbordagemEFollowup() throws Exception {
        long id = criar("Ana Cakes", "(86) 99981-0000");

        mvc.perform(patch("/api/leads/{id}/abordagem", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABORDADO"))
                .andExpect(jsonPath("$.abordadoEm").value("2026-10-21"))
                .andExpect(jsonPath("$.followupEm").value("2026-10-24"));

        // ainda não venceu: a lista de follow-ups pendentes está vazia
        mvc.perform(get("/api/leads/followups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(get("/api/leads/resumo"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.porStatus.ABORDADO").value(1));
    }

    @Test
    void filtraPorNichoEStatus() throws Exception {
        criar("Ana Cakes", "(86) 99981-0000");
        mvc.perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Sushi Sumi\",\"nicho\":\"sushi\",\"cidade\":\"Teresina\",\"whatsapp\":\"(86) 99911-2088\"}"));

        mvc.perform(get("/api/leads").param("nicho", "Confeitaria").param("status", "NOVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Ana Cakes"));
    }

    private long criar(String nome, String whatsapp) throws Exception {
        String corpo = mvc.perform(post("/api/leads").contentType(MediaType.APPLICATION_JSON)
                        .content(lead(nome, whatsapp)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode no = json.readTree(corpo);
        return no.get("id").asLong();
    }

    private static String lead(String nome, String whatsapp) {
        return """
                {"nome":"%s","nicho":"confeitaria","cidade":"Teresina","whatsapp":"%s","avaliacao":5.0}
                """.formatted(nome, whatsapp);
    }
}
