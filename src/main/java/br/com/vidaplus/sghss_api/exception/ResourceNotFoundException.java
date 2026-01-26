package br.com.vidaplus.sghss_api.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s não encontrado com %s: '%s'",
                resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = "Recurso";
        this.fieldName = "id";
        this.fieldValue = "desconhecido";
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue, String message) {
        super(message);
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // Convenience constructors for common cases
    public static ResourceNotFoundException forPaciente(String fieldName, Object fieldValue) {
        return new ResourceNotFoundException("Paciente", fieldName, fieldValue);
    }

    public static ResourceNotFoundException forProfissional(String fieldName, Object fieldValue) {
        return new ResourceNotFoundException("Profissional", fieldName, fieldValue);
    }

    public static ResourceNotFoundException forConsulta(String fieldName, Object fieldValue) {
        return new ResourceNotFoundException("Consulta", fieldName, fieldValue);
    }

    public static ResourceNotFoundException forProntuario(String fieldName, Object fieldValue) {
        return new ResourceNotFoundException("Prontuário", fieldName, fieldValue);
    }

    public static ResourceNotFoundException forUsuario(String fieldName, Object fieldValue) {
        return new ResourceNotFoundException("Usuário", fieldName, fieldValue);
    }

    public static ResourceNotFoundException forPacienteId(Object id) {
        return forPaciente("id", id);
    }

    public static ResourceNotFoundException forProfissionalId(Object id) {
        return forProfissional("id", id);
    }

    public static ResourceNotFoundException forConsultaId(Object id) {
        return forConsulta("id", id);
    }
}
