package br.com.vidaplus.sghss_api.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ValidationException extends RuntimeException {

    private final String field;
    private final Object rejectedValue;
    private final String validationCode;
    private final Map<String, Object> details;

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.rejectedValue = null;
        this.validationCode = "VALIDATION_ERROR";
        this.details = new HashMap<>();
    }

    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
        this.rejectedValue = null;
        this.validationCode = "FIELD_VALIDATION_ERROR";
        this.details = new HashMap<>();
    }

    public ValidationException(String message, String field, Object rejectedValue) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.validationCode = "FIELD_VALIDATION_ERROR";
        this.details = new HashMap<>();
    }

    public ValidationException(String message, String field, Object rejectedValue, String validationCode) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.validationCode = validationCode;
        this.details = new HashMap<>();
    }

    public ValidationException(String message, String field, Object rejectedValue,
                               String validationCode, Map<String, Object> details) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.validationCode = validationCode;
        this.details = details != null ? details : new HashMap<>();
    }

    public static ValidationException cpfInvalido(String cpf) {
        Map<String, Object> details = new HashMap<>();
        details.put("format", "###.###.###-## ou 11 dígitos");
        details.put("suggestion", "Informe um CPF válido com 11 dígitos");

        return new ValidationException(
                String.format("CPF %s é inválido", cpf),
                "cpf",
                cpf,
                "CPF_INVALIDO",
                details
        );
    }

    public static ValidationException dataFuturaInvalida(String field, Object value) {
        return new ValidationException(
                String.format("Data %s não pode ser no futuro", value),
                field,
                value,
                "DATA_FUTURA_INVALIDA"
        );
    }

    public static ValidationException dataPassadaInvalida(String field, Object value) {
        return new ValidationException(
                String.format("Data %s não pode ser no passado", value),
                field,
                value,
                "DATA_PASSADA_INVALIDA"
        );
    }

    public static ValidationException valorForaIntervalo(String field, Object value,
                                                         Number min, Number max) {
        Map<String, Object> details = new HashMap<>();
        details.put("min", min);
        details.put("max", max);
        details.put("suggestion", String.format("Informe um valor entre %s e %s", min, max));

        return new ValidationException(
                String.format("Valor %s fora do intervalo permitido (%s - %s)", value, min, max),
                field,
                value,
                "VALOR_FORA_INTERVALO",
                details
        );
    }

    public static ValidationException emailInvalido(String email) {
        Map<String, Object> details = new HashMap<>();
        details.put("format", "usuario@dominio.com");
        details.put("suggestion", "Informe um email válido");

        return new ValidationException(
                String.format("Email %s é inválido", email),
                "email",
                email,
                "EMAIL_INVALIDO",
                details
        );
    }

    public static ValidationException telefoneInvalido(String telefone) {
        Map<String, Object> details = new HashMap<>();
        details.put("format", "(XX) XXXXX-XXXX ou 10/11 dígitos");
        details.put("suggestion", "Informe um telefone válido com 10 ou 11 dígitos");

        return new ValidationException(
                String.format("Telefone %s é inválido", telefone),
                "telefone",
                telefone,
                "TELEFONE_INVALIDO",
                details
        );
    }

    public static ValidationException senhaFraca() {
        Map<String, Object> details = new HashMap<>();
        details.put("requirements", "Mínimo 6 caracteres, com letras e números");
        details.put("suggestion", "Use uma senha mais forte com números e caracteres especiais");

        return new ValidationException(
                "Senha muito fraca",
                "senha",
                null,
                "SENHA_FRACA",
                details
        );
    }

    public static ValidationException camposObrigatorios(String... campos) {
        Map<String, Object> details = new HashMap<>();
        details.put("camposObrigatorios", campos);
        details.put("suggestion", "Preencha todos os campos obrigatórios");

        return new ValidationException(
                "Campos obrigatórios não preenchidos",
                null,
                null,
                "CAMPOS_OBRIGATORIOS",
                details
        );
    }

    public static ValidationException formatoInvalido(String field, Object value, String formato) {
        Map<String, Object> details = new HashMap<>();
        details.put("formatoEsperado", formato);
        details.put("suggestion", String.format("Use o formato: %s", formato));

        return new ValidationException(
                String.format("Formato inválido para %s: %s", field, value),
                field,
                value,
                "FORMATO_INVALIDO",
                details
        );
    }

    public static ValidationException crmCorenInvalido(String crmCoren) {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Informe um CRM/COREN válido");

        return new ValidationException(
                String.format("CRM/COREN %s é inválido", crmCoren),
                "crmCoren",
                crmCoren,
                "CRM_COREN_INVALIDO",
                details
        );
    }

    public static ValidationException idadeMinima(String field, int idade, int minima) {
        Map<String, Object> details = new HashMap<>();
        details.put("idadeInformada", idade);
        details.put("idadeMinima", minima);
        details.put("suggestion", String.format("Idade mínima requerida: %s anos", minima));

        return new ValidationException(
                String.format("Idade %s é inferior à mínima permitida (%s)", idade, minima),
                field,
                idade,
                "IDADE_MINIMA",
                details
        );
    }

    public static ValidationException horarioForaExpediente(String horario, String expediente) {
        Map<String, Object> details = new HashMap<>();
        details.put("expediente", expediente);
        details.put("suggestion", "Escolha um horário dentro do expediente");

        return new ValidationException(
                String.format("Horário %s fora do expediente (%s)", horario, expediente),
                "dataHora",
                horario,
                "HORARIO_FORA_EXPEDIENTE",
                details
        );
    }

    public static ValidationException duracaoInvalida(int duracao, int min, int max) {
        Map<String, Object> details = new HashMap<>();
        details.put("duracaoMinima", min);
        details.put("duracaoMaxima", max);
        details.put("suggestion", String.format("Duração deve estar entre %s e %s minutos", min, max));

        return new ValidationException(
                String.format("Duração %s minutos inválida", duracao),
                "duracaoMinutos",
                duracao,
                "DURACAO_INVALIDA",
                details
        );
    }

    public ValidationException withDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
}
