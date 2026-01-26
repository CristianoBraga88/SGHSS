package br.com.vidaplus.sghss_api.security;

import br.com.vidaplus.sghss_api.exception.SecurityException;
import br.com.vidaplus.sghss_api.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Getter
    @Value("${app.jwt.expiration:86400000}")
    private Long jwtExpirationMs;

    @Getter
    @Value("${app.jwt.refresh-expiration:604800000}")
    private Long refreshTokenExpirationMs;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("JWT secret deve ter pelo menos 32 caracteres");
        }

        this.algorithm = Algorithm.HMAC256(jwtSecret);
        this.verifier = JWT.require(algorithm).build();

        log.info(" JWT Service inicializado (Auth0)");
        log.info("Token expiration: {} ms ({} horas)",
                jwtExpirationMs, TimeUnit.MILLISECONDS.toHours(jwtExpirationMs));
        log.info("Refresh token expiration: {} ms ({} dias)",
                refreshTokenExpirationMs, TimeUnit.MILLISECONDS.toDays(refreshTokenExpirationMs));
    }

    // ========== TOKEN GENERATION ==========

    /**
     * Gera um token JWT para autenticação
     */
    public String generateToken(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + jwtExpirationMs);

        return JWT.create()
                .withSubject(usuario.getEmail())
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim("userId", usuario.getId().toString())
                .withClaim("tipoUsuario", usuario.getTipo().name())
                .withClaim("authorities", authorities)
                .withClaim("nome", extractNomeUsuario(usuario))
                .withClaim("ativo", usuario.getAtivo())
                .sign(algorithm);
    }

    /**
     * Gera um refresh token
     */
    public String generateRefreshToken(String email) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + refreshTokenExpirationMs);

        return JWT.create()
                .withSubject(email)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim("tokenType", "REFRESH")
                .sign(algorithm);
    }

    /**
     * Gera um token para reset de senha validade: 1 hora
     */
    public String generatePasswordResetToken(String email) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + 3600000); // 1 hora

        return JWT.create()
                .withSubject(email)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim("tokenType", "PASSWORD_RESET")
                .sign(algorithm);
    }

    // ========== TOKEN VALIDATION ==========

    /**
     * Valida um token JWT
     */
    public boolean validateToken(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (TokenExpiredException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
            throw SecurityException.tokenExpirado();
        } catch (JWTVerificationException e) {
            log.warn(" Token JWT inválido: {}", e.getMessage());
            throw SecurityException.tokenInvalido();
        }
    }

    /**
     * Valida um token JWT para um usuário específico
     */
    public boolean validateTokenForUser(String token, String email) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject().equals(email);
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Gera um token JWT para um usuário
     */
    public String generateToken(Usuario usuario) {
        String authorities = usuario.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(java.util.stream.Collectors.joining(","));

        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + jwtExpirationMs);

        return JWT.create()
                .withSubject(usuario.getEmail())
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim("userId", usuario.getId().toString())
                .withClaim("tipoUsuario", usuario.getTipo().name())
                .withClaim("authorities", authorities)
                .withClaim("nome", extractNomeUsuario(usuario))
                .withClaim("ativo", usuario.getAtivo())
                .sign(algorithm);
    }

    /**
     * Valida um refresh token
     */
    public boolean validateRefreshToken(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return "REFRESH".equals(decodedJWT.getClaim("tokenType").asString());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Valida um token de reset de senha
     */
    public boolean validatePasswordResetToken(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return "PASSWORD_RESET".equals(decodedJWT.getClaim("tokenType").asString());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Extrai o email subject do token
     */
    public String extractEmail(String token) {
        return decodeToken(token).getSubject();
    }

    /**
     * Extrai o ID do usuário do token
     */
    public String extractUserId(String token) {
        return decodeToken(token).getClaim("userId").asString();
    }

    /**
     * Extrai o tipo de usuário do token
     */
    public String extractTipoUsuario(String token) {
        return decodeToken(token).getClaim("tipoUsuario").asString();
    }

    /**
     * Extrai as authorities do token
     */
    public String extractAuthorities(String token) {
        return decodeToken(token).getClaim("authorities").asString();
    }

    /**
     * Extrai a data de expiração do token
     */
    public Date extractExpiration(String token) {
        return decodeToken(token).getExpiresAt();
    }

    /**
     * Extrai a data de emissão do token
     */
    public Date extractIssuedAt(String token) {
        return decodeToken(token).getIssuedAt();
    }

    /**
     * Verifica se o token está expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Decodifica um token JWT
     */
    public DecodedJWT decodeToken(String token) {
        try {
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error(" Falha ao decodificar token: {}", e.getMessage());
            throw SecurityException.tokenInvalido();
        }
    }

    /**
     * Calcula o tempo restante do token em milissegundos
     */
    public long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Cria um novo token a partir de um existente refresh
     */
    public String refreshToken(String oldToken) {
        DecodedJWT decodedJWT = decodeToken(oldToken);
        String email = decodedJWT.getSubject();

        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .withClaim("userId", decodedJWT.getClaim("userId").asString())
                .withClaim("tipoUsuario", decodedJWT.getClaim("tipoUsuario").asString())
                .withClaim("authorities", decodedJWT.getClaim("authorities").asString())
                .withClaim("nome", decodedJWT.getClaim("nome").asString())
                .withClaim("ativo", decodedJWT.getClaim("ativo").asBoolean())
                .sign(algorithm);
    }

    private String extractNomeUsuario(Usuario usuario) {
        if (usuario instanceof br.com.vidaplus.sghss_api.model.Paciente paciente) {
            return paciente.getNomeCompleto();
        } else if (usuario instanceof br.com.vidaplus.sghss_api.model.ProfissionalSaude profissional) {
            return profissional.getUsername();
        }
        return "Administrador";
    }

}
