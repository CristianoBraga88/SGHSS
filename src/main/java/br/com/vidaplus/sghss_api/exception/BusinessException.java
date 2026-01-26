package br.com.vidaplus.sghss_api.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> details;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.details = new HashMap<>();
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }

    public BusinessException(String message, String errorCode, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details != null ? details : new HashMap<>();
    }

    public static BusinessException cpfJaCadastrado(String cpf) {
        Map<String, Object> details = new HashMap<>();
        details.put("cpf", cpf);
        details.put("suggestion", "Verifique se o paciente já está cadastrado ou utilize outro CPF");

        return new BusinessException(
                String.format("CPF %s já está cadastrado no sistema", cpf),
                "CPF_JA_CADASTRADO",
                details
        );
    }

    public static BusinessException crmCorenJaCadastrado(String crmCoren) {
        Map<String, Object> details = new HashMap<>();
        details.put("crmCoren", crmCoren);
        details.put("suggestion", "Verifique se o profissional já está cadastrado");

        return new BusinessException(
                String.format("CRM/COREN %s já está cadastrado no sistema", crmCoren),
                "CRM_COREN_JA_CADASTRADO",
                details
        );
    }

    public static BusinessException emailJaCadastrado(String email) {
        Map<String, Object> details = new HashMap<>();
        details.put("email", email);
        details.put("suggestion", "Utilize outro email ou recupere sua senha");

        return new BusinessException(
                String.format("Email %s já está cadastrado no sistema", email),
                "EMAIL_JA_CADASTRADO",
                details
        );
    }

    public static BusinessException horarioIndisponivel(String dataHora, String profissional) {
        Map<String, Object> details = new HashMap<>();
        details.put("dataHora", dataHora);
        details.put("profissional", profissional);
        details.put("suggestion", "Escolha outro horário ou consulte a agenda do profissional");

        return new BusinessException(
                String.format("Horário %s não disponível para o profissional %s", dataHora, profissional),
                "HORARIO_INDISPONIVEL",
                details
        );
    }

    public static BusinessException profissionalIndisponivel(String profissional) {
        Map<String, Object> details = new HashMap<>();
        details.put("profissional", profissional);
        details.put("suggestion", "Escolha outro profissional ou tente novamente mais tarde");

        return new BusinessException(
                String.format("Profissional %s não está disponível para consultas", profissional),
                "PROFISSIONAL_INDISPONIVEL",
                details
        );
    }

    public static BusinessException consultaNaoPodeSerCancelada(String status) {
        Map<String, Object> details = new HashMap<>();
        details.put("statusAtual", status);
        details.put("suggestion", "Apenas consultas agendadas podem ser canceladas");

        return new BusinessException(
                String.format("Consulta não pode ser cancelada no status: %s", status),
                "CONSULTA_NAO_PODE_CANCELAR",
                details
        );
    }

    public static BusinessException pacienteInativo(String paciente) {
        Map<String, Object> details = new HashMap<>();
        details.put("paciente", paciente);
        details.put("suggestion", "Ative o paciente antes de realizar operações");

        return new BusinessException(
                String.format("Paciente %s está inativo no sistema", paciente),
                "PACIENTE_INATIVO",
                details
        );
    }

    public static BusinessException profissionalInativo(String profissional) {
        Map<String, Object> details = new HashMap<>();
        details.put("profissional", profissional);
        details.put("suggestion", "Ative o profissional antes de realizar operações");

        return new BusinessException(
                String.format("Profissional %s está inativo no sistema", profissional),
                "PROFISSIONAL_INATIVO",
                details
        );
    }

    public static BusinessException prontuarioNaoPertenceConsulta() {
        return new BusinessException(
                "Prontuário não pertence à consulta informada",
                "PRONTUARIO_NAO_PERTENCE_CONSULTA"
        );
    }

    public static BusinessException limiteConsultasExcedido(String paciente, int limite) {
        Map<String, Object> details = new HashMap<>();
        details.put("paciente", paciente);
        details.put("limite", limite);
        details.put("suggestion", "Cancelar consultas antigas ou aguardar término das atuais");

        return new BusinessException(
                String.format("Paciente %s excedeu o limite de %d consultas ativas", paciente, limite),
                "LIMITE_CONSULTAS_EXCEDIDO",
                details
        );
    }

    public static BusinessException dadosSensiveisNaoInformados() {
        return new BusinessException(
                "Dados sensíveis (CPF) não podem ser expostos",
                "DADOS_SENSIVEIS_NAO_INFORMADOS"
        );
    }

    public static BusinessException lgpdRestricaoAcesso() {
        return new BusinessException(
                "Acesso restrito por políticas de LGPD",
                "LGPD_RESTRICAO_ACESSO"
        );
    }

    public BusinessException withDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
}
