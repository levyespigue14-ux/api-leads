package dev.joaolevy.leads.lead;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 60)
    private String nicho;

    @Column(nullable = false, length = 80)
    private String cidade;

    /** Só dígitos, com DDI e DDD (ex.: 5586999990000). */
    @Column(nullable = false, unique = true, length = 15)
    private String whatsapp;

    @Column(length = 60)
    private String instagram;

    @Column(precision = 2, scale = 1)
    private BigDecimal avaliacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadStatus status = LeadStatus.NOVO;

    private LocalDate abordadoEm;

    private LocalDate followupEm;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
        if (status == null) {
            status = LeadStatus.NOVO;
        }
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getNicho() { return nicho; }
    public void setNicho(String nicho) { this.nicho = nicho; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }
    public BigDecimal getAvaliacao() { return avaliacao; }
    public void setAvaliacao(BigDecimal avaliacao) { this.avaliacao = avaliacao; }
    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }
    public LocalDate getAbordadoEm() { return abordadoEm; }
    public void setAbordadoEm(LocalDate abordadoEm) { this.abordadoEm = abordadoEm; }
    public LocalDate getFollowupEm() { return followupEm; }
    public void setFollowupEm(LocalDate followupEm) { this.followupEm = followupEm; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
