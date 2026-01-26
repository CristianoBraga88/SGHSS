package br.com.vidaplus.sghss_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prontuarios")
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id")
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalSaude profissionalCriador;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String texto;

    @Column(nullable = false)
    private Integer versao = 1;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "prescricao_medica", columnDefinition = "TEXT")
    private String prescricaoMedica;

    @Column(name = "exames_solicitados", columnDefinition = "TEXT")
    private String examesSolicitados;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "peso_kg")
    private Double pesoKg;

    @Column(name = "altura_cm")
    private Double alturaCm;

    @Column(name = "pressao_arterial", length = 20)
    private String pressaoArterial;

    @Column(name = "temperatura_c")
    private Double temperaturaC;

    @Column(name = "frequencia_cardiaca")
    private Integer frequenciaCardiaca;

    @Column(name = "saturacao_o2")
    private Integer saturacaoO2;

    @Column(name = "assinatura_digital", columnDefinition = "TEXT")
    private String assinaturaDigital;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
        this.versao += 1;
    }
}