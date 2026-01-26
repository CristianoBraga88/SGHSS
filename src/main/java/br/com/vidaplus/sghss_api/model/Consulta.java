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
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalSaude profissional;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConsulta tipo;

    @Column(name = "duracao_minutos")
    private Integer duracaoMinutos = 30;

    @Column(columnDefinition = "TEXT")
    private String sintomas;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "cancelada_por")
    private String canceladaPor;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @OneToOne(mappedBy = "consulta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Prontuario prontuario;

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    public enum StatusConsulta {
        AGENDADA,
        CONFIRMADA,
        EM_ANDAMENTO,
        CONCLUIDA,
        CANCELADA,
        FALTA
    }

    public enum TipoConsulta {
        PRESENCIAL,
        TELEMEDICINA,
        RETORNO,
        URGENCIA
    }
}
