package br.com.vidaplus.sghss_api.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class AuditoriaException extends RuntimeException {

    private final String auditoriaCode;
    private final String modulo;
    private final String operacao;
    private final Map<String, Object> detalhesAuditoria;

    public AuditoriaException(String message) {
        super(message);
        this.auditoriaCode = "AUDITORIA_ERROR";
        this.modulo = "SISTEMA";
        this.operacao = "DESCONHECIDA";
        this.detalhesAuditoria = new HashMap<>();
    }

    public AuditoriaException(String message, String modulo, String operacao) {
        super(message);
        this.auditoriaCode = "AUDITORIA_ERROR";
        this.modulo = modulo;
        this.operacao = operacao;
        this.detalhesAuditoria = new HashMap<>();
    }

    public AuditoriaException(String message, String auditoriaCode, String modulo, String operacao) {
        super(message);
        this.auditoriaCode = auditoriaCode;
        this.modulo = modulo;
        this.operacao = operacao;
        this.detalhesAuditoria = new HashMap<>();
    }

    public AuditoriaException(String message, String auditoriaCode, String modulo,
                              String operacao, Map<String, Object> detalhesAuditoria) {
        super(message);
        this.auditoriaCode = auditoriaCode;
        this.modulo = modulo;
        this.operacao = operacao;
        this.detalhesAuditoria = detalhesAuditoria != null ? detalhesAuditoria : new HashMap<>();
    }

    public static AuditoriaException logNaoRegistrado(String modulo, String operacao) {
        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("gravidade", "ALTA");
        detalhes.put("suggestion", "Contate o administrador do sistema imediatamente");

        return new AuditoriaException(
                String.format("Falha ao registrar log de auditoria para %s.%s", modulo, operacao),
                "LOG_NAO_REGISTRADO",
                modulo,
                operacao,
                detalhes
        );
    }

    public static AuditoriaException dadosSensiveisNaoAuditados(String recurso, String id) {
        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("recurso", recurso);
        detalhes.put("id", id);
        detalhes.put("gravidade", "CRITICA");
        detalhes.put("suggestion", "Operação bloqueada por falha na auditoria de dados sensíveis");

        return new AuditoriaException(
                String.format("Falha na auditoria de dados sensíveis do recurso %s ID %s", recurso, id),
                "DADOS_SENSIVEIS_NAO_AUDITADOS",
                "LGPD",
                "AUDITORIA_DADOS_SENSIVEIS",
                detalhes
        );
    }

    public static AuditoriaException acessoNaoAuditado(String usuario, String recurso) {
        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("usuario", usuario);
        detalhes.put("recurso", recurso);
        detalhes.put("gravidade", "MEDIA");
        detalhes.put("suggestion", "Verifique as configurações de auditoria do sistema");

        return new AuditoriaException(
                String.format("Acesso do usuário %s ao recurso %s não foi auditado", usuario, recurso),
                "ACESSO_NAO_AUDITADO",
                "SEGURANCA",
                "AUDITORIA_ACESSO",
                detalhes
        );
    }

    public static AuditoriaException alteracaoNaoAuditada(String recurso, String id, String campo) {
        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("recurso", recurso);
        detalhes.put("id", id);
        detalhes.put("campo", campo);
        detalhes.put("gravidade", "ALTA");
        detalhes.put("suggestion", "A operação foi revertida por falta de auditoria");

        return new AuditoriaException(
                String.format("Alteração no campo %s do recurso %s ID %s não auditada", campo, recurso, id),
                "ALTERACAO_NAO_AUDITADA",
                recurso,
                "ATUALIZACAO",
                detalhes
        );
    }

    public static AuditoriaException exclusaoNaoAuditada(String recurso, String id) {
        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("recurso", recurso);
        detalhes.put("id", id);
        detalhes.put("gravidade", "CRITICA");
        detalhes.put("suggestion", "A exclusão foi cancelada por falta de auditoria");

        return new AuditoriaException(
                String.format("Exclusão do recurso %s ID %s não auditada", recurso, id),
                "EXCLUSAO_NAO_AUDITADA",
                recurso,
                "EXCLUSAO",
                detalhes
        );
    }

    public static AuditoriaException consultaNaoAuditada(String usuario, String recurso, String filtros) {
        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("usuario", usuario);
        detalhes.put("recurso", recurso);
        detalhes.put("filtros", filtros);
        detalhes.put("gravidade", "BAIXA");
        detalhes.put("suggestion", "Configure auditoria para consultas neste recurso");

        return new AuditoriaException(
                String.format("Consulta do usuário %s ao recurso %s não auditada", usuario, recurso),
                "CONSULTA_NAO_AUDITADA",
                recurso,
                "CONSULTA",
                detalhes
        );
    }

    public static AuditoriaException backupAuditoriaFalhou() {
        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("gravidade", "ALTA");
        detalhes.put("suggestion", "Execute manualmente o backup dos logs de auditoria");

        return new AuditoriaException(
                "Falha no backup automático dos logs de auditoria",
                "BACKUP_AUDITORIA_FALHOU",
                "SISTEMA",
                "BACKUP_LOGS",
                detalhes
        );
    }

    public static AuditoriaException retencaoLogsExpirada(int dias) {
        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("diasRetencao", dias);
        detalhes.put("gravidade", "MEDIA");
        detalhes.put("suggestion", "Ajuste a política de retenção de logs ou faça backup manual");

        return new AuditoriaException(
                String.format("Período de retenção de logs (%d dias) expirado sem backup", dias),
                "RETENCAO_LOGS_EXPIRADA",
                "SISTEMA",
                "RETENCAO_LOGS",
                detalhes
        );
    }

    public static AuditoriaException lgpdViolacaoAuditoria(String operacao, String motivo) {
        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("operacao", operacao);
        detalhes.put("motivo", motivo);
        detalhes.put("gravidade", "CRITICA");
        detalhes.put("suggestion", "Operação bloqueada por violação de LGPD na auditoria");

        return new AuditoriaException(
                String.format("Violação de LGPD na auditoria da operação: %s. Motivo: %s", operacao, motivo),
                "LGPD_VIOLACAO_AUDITORIA",
                "LGPD",
                operacao,
                detalhes
        );
    }

    public AuditoriaException withDetalhe(String key, Object value) {
        this.detalhesAuditoria.put(key, value);
        return this;
    }

    public AuditoriaException withGravidade(String gravidade) {
        this.detalhesAuditoria.put("gravidade", gravidade);
        return this;
    }
}
