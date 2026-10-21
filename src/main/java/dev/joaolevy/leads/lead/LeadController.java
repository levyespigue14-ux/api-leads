package dev.joaolevy.leads.lead;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/leads")
@Tag(name = "Leads", description = "Cadastro de leads e acompanhamento do funil de prospecção")
public class LeadController {

    private final LeadService service;

    public LeadController(LeadService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastra um lead")
    public ResponseEntity<LeadResponse> criar(@Valid @RequestBody LeadRequest dados) {
        LeadResponse criado = service.criar(dados);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @GetMapping
    @Operation(summary = "Lista leads, com filtros opcionais por status, nicho e cidade")
    public Page<LeadResponse> listar(@RequestParam(required = false) LeadStatus status,
                                     @RequestParam(required = false) String nicho,
                                     @RequestParam(required = false) String cidade,
                                     @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC)
                                     Pageable pageable) {
        return service.listar(status, nicho, cidade, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um lead pelo id")
    public LeadResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um lead")
    public LeadResponse atualizar(@PathVariable Long id, @Valid @RequestBody LeadRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um lead")
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }

    @PatchMapping("/{id}/abordagem")
    @Operation(summary = "Registra a primeira mensagem enviada e agenda o follow-up")
    public LeadResponse abordar(@PathVariable Long id) {
        return service.registrarAbordagem(id);
    }

    @PatchMapping("/{id}/followup")
    @Operation(summary = "Registra um follow-up feito e agenda o próximo")
    public LeadResponse followup(@PathVariable Long id) {
        return service.registrarFollowup(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Muda o status (ex.: RESPONDEU, FECHADO, SEM_INTERESSE)")
    public LeadResponse mudarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest pedido) {
        return service.mudarStatus(id, pedido);
    }

    @GetMapping("/followups")
    @Operation(summary = "Leads com follow-up vencido (hoje ou antes)")
    public List<LeadResponse> followupsPendentes() {
        return service.followupsPendentes();
    }

    @GetMapping("/resumo")
    @Operation(summary = "Quantidade de leads por etapa do funil")
    public ResumoResponse resumo() {
        return service.resumo();
    }
}
