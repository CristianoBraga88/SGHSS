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
@Table(name = "logs_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String modulo;

    @Column(nullable = false, length = 50)
    private String acao;

    @Column(name = "descricao_acao", columnDefinition = "TEXT")
    private String descricaoAcao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "usuario_email", length = 100)
    private String usuarioEmail;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "recurso_afetado", length = 200)
    private String recursoAfetado;

    @Column(name = "id_recurso_afetado", length = 100)
    private String idRecursoAfetado;

    @Column(name = "detalhes", columnDefinition = "JSON")
    private String detalhes;

    @Column(name = "status_operacao", length = 20)
    private String statusOperacao;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(name = "duracao_ms")
    private Long duracaoMs;

    public enum ModuloSistema {
        AUTENTICACAO,
        PACIENTES,
        PROFISSIONAIS,
        CONSULTAS,
        PRONTUARIOS,
        PRESCRICOES,
        AGENDAMENTOS,
        RELATORIOS,
        SISTEMA
    }

    public enum AcaoAuditoria {
        LOGIN,
        LOGOUT,
        CRIAR,
        ATUALIZAR,
        EXCLUIR,
        CONSULTAR,
        ACESSAR,
        EXPORTAR,
        IMPORTAR,
        AUTORIZAR,
        NEGAR
    }
}
