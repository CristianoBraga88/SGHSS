package br.com.vidaplus.sghss_api.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SecurityException extends RuntimeException {

    private final String securityCode;
    private final Map<String, Object> details;

    public SecurityException(String message) {
        super(message);
        this.securityCode = "SECURITY_ERROR";
        this.details = new HashMap<>();
    }

    public SecurityException(String message, String securityCode) {
        super(message);
        this.securityCode = securityCode;
        this.details = new HashMap<>();
    }

    public SecurityException(String message, String securityCode, Map<String, Object> details) {
        super(message);
        this.securityCode = securityCode;
        this.details = details != null ? details : new HashMap<>();
    }

    public static SecurityException tokenInvalido() {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Faça login novamente para obter um token válido");

        return new SecurityException(
                "Token JWT inválido ou malformado",
                "TOKEN_INVALIDO",
                details
        );
    }

    public static SecurityException tokenExpirado() {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Faça login novamente para renovar o token");

        return new SecurityException(
                "Token JWT expirado",
                "TOKEN_EXPIRADO",
                details
        );
    }

    public static SecurityException acessoNegado() {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Contate o administrador para solicitar permissões");

        return new SecurityException(
                "Acesso negado. Permissões insuficientes.",
                "ACESSO_NEGADO",
                details
        );
    }

    public static SecurityException usuarioNaoAutenticado() {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Faça login para acessar este recurso");

        return new SecurityException(
                "Usuário não autenticado",
                "USUARIO_NAO_AUTENTICADO",
                details
        );
    }

    public static SecurityException usuarioInativo(String email) {
        Map<String, Object> details = new HashMap<>();
        details.put("email", email);
        details.put("suggestion", "Contate o administrador para ativar sua conta");

        return new SecurityException(
                String.format("Usuário %s está inativo", email),
                "USUARIO_INATIVO",
                details
        );
    }

    public static SecurityException usuarioBloqueado(String email) {
        Map<String, Object> details = new HashMap<>();
        details.put("email", email);
        details.put("suggestion", "Contate o administrador para desbloquear sua conta");

        return new SecurityException(
                String.format("Usuário %s está bloqueado", email),
                "USUARIO_BLOQUEADO",
                details
        );
    }

    public static SecurityException muitasTentativasLogin(String ip) {
        Map<String, Object> details = new HashMap<>();
        details.put("ipOrigem", ip);
        details.put("suggestion", "Tente novamente em 15 minutos ou contate o suporte");

        return new SecurityException(
                "Muitas tentativas de login. Conta temporariamente bloqueada.",
                "MUITAS_TENTATIVAS_LOGIN",
                details
        );
    }

    public static SecurityException senhaExpirada() {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Altere sua senha para continuar");

        return new SecurityException(
                "Senha expirada. É necessário alterá-la.",
                "SENHA_EXPIRADA",
                details
        );
    }

    public static SecurityException dispositivoNaoAutorizado() {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Use um dispositivo autorizado ou contate o administrador");

        return new SecurityException(
                "Dispositivo não autorizado para acesso",
                "DISPOSITIVO_NAO_AUTORIZADO",
                details
        );
    }

    public static SecurityException ipNaoAutorizado(String ip) {
        Map<String, Object> details = new HashMap<>();
        details.put("ipBloqueado", ip);
        details.put("suggestion", "Contate o administrador para liberar seu IP");

        return new SecurityException(
                String.format("Acesso não permitido do IP %s", ip),
                "IP_NAO_AUTORIZADO",
                details
        );
    }

    public static SecurityException horarioAcessoNegado() {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Acesso permitido apenas em horário comercial");

        return new SecurityException(
                "Acesso negado fora do horário permitido",
                "HORARIO_ACESSO_NEGADO",
                details
        );
    }

    public static SecurityException dadosSensiveisAcessoNegado() {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Apenas profissionais autorizados podem acessar estes dados");

        return new SecurityException(
                "Acesso negado a dados sensíveis",
                "DADOS_SENSIVEIS_ACESSO_NEGADO",
                details
        );
    }

    public static SecurityException lgpdViolacaoConsulta() {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Apenas o próprio paciente ou profissionais envolvidos podem acessar");

        return new SecurityException(
                "Violação de LGPD. Acesso a dados médicos não autorizado.",
                "LGPD_VIOLACAO_CONSULTA",
                details
        );
    }

    public static SecurityException auditoriaFalha(String operacao) {
        Map<String, Object> details = new HashMap<>();
        details.put("operacao", operacao);
        details.put("suggestion", "Contate o administrador do sistema");

        return new SecurityException(
                String.format("Falha na auditoria da operação: %s", operacao),
                "AUDITORIA_FALHA",
                details
        );
    }

    public static SecurityException tokenRefreshInvalido() {
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Faça login novamente");

        return new SecurityException(
                "Refresh token inválido ou expirado",
                "TOKEN_REFRESH_INVALIDO",
                details
        );
    }

    public SecurityException withDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
}
