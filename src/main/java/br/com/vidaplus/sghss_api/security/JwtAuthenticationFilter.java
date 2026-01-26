package br.com.vidaplus.sghss_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Endpoints públicos que não requerem autenticação
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            // Autenticação
            "/api/auth/**",
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh-token",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",

            // Documentação
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/v3/api-docs.json",

            // Health checks
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus",

            // Web resources
            "/favicon.ico",
            "/error",
            "/robots.txt",

            // Public API (se houver)
            "/api/public/**"
    );

    // Endpoints que requerem apenas autenticação básica, sem verificação de roles
    private static final List<String> AUTHENTICATED_ENDPOINTS = Arrays.asList(
            "/api/pacientes/me/**",
            "/api/consultas/me/**",
            "/api/prontuarios/me/**"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();

        log.debug("Filtro JWT - URI: {} - Método: {}", requestURI, method);

        try {
            // Verificar se é um endpoint público
            if (isPublicEndpoint(requestURI)) {
                log.debug("Endpoint público - Processando sem autenticação");
                filterChain.doFilter(request, response);
                return;
            }

            // Extrair o token JWT
            final String jwt = extractJwtFromRequest(request);

            if (!StringUtils.hasText(jwt)) {
                log.warn("Requisição sem token JWT para endpoint protegido: {}", requestURI);
                handleMissingToken(response, requestURI);
                return;
            }

            // Validar o token
            if (!jwtService.validateToken(jwt)) {
                log.warn("Token JWT inválido para: {}", requestURI);
                handleInvalidToken(response, "Token inválido");
                return;
            }

            // Extrair informações do token
            String email = jwtService.extractEmail(jwt);

            if (!StringUtils.hasText(email)) {
                log.warn("Token JWT sem email/subject: {}", requestURI);
                handleInvalidToken(response, "Token sem identificação de usuário");
                return;
            }

            // Verificar se o token está expirado
            if (jwtService.isTokenExpired(jwt)) {
                log.warn("Token JWT expirado para usuário: {}", email);
                handleInvalidToken(response, "Token expirado");
                return;
            }

            // Carregar usuário do banco de dados
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Criar autenticação
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // Definir autenticação no contexto de segurança
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Usuário autenticado: {} - Roles: {}",
                    email, userDetails.getAuthorities());

            // Adicionar headers para auditoria/logging
            addAuditHeaders(response, jwt, email);

            // Continuar com a cadeia de filtros
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Erro no filtro JWT para URI: {} - Erro: {}", requestURI, e.getMessage(), e);
            handleFilterException(response, e);
        }
    }

    /**
     * Extrai o token JWT do cabeçalho Authorization
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // Tenta obter do cabeçalho Authorization
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Tenta obter do parâmetro de query para WebSocket
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        // Tenta obter do cookie para aplicações web
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // Tenta obter do header X-Access-Token
        String accessToken = request.getHeader("X-Access-Token");
        if (StringUtils.hasText(accessToken)) {
            return accessToken;
        }

        return null;
    }

    /**
     * Verifica se o endpoint é público
     */
    private boolean isPublicEndpoint(String requestURI) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }

    /**
     * Verifica se o endpoint requer apenas autenticação sem roles específicas
     */
    private boolean isAuthenticatedOnlyEndpoint(String requestURI) {
        return AUTHENTICATED_ENDPOINTS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }

    /**
     * Adiciona headers para auditoria e logging
     */
    private void addAuditHeaders(HttpServletResponse response, String jwt, String email) {
        try {
            String userId = jwtService.extractUserId(jwt);
            String tipoUsuario = jwtService.extractTipoUsuario(jwt);
            String authorities = jwtService.extractAuthorities(jwt);

            if (userId != null) {
                response.setHeader("X-User-Id", userId);
            }
            if (tipoUsuario != null) {
                response.setHeader("X-User-Type", tipoUsuario);
            }
            if (authorities != null) {
                response.setHeader("X-User-Roles", authorities);
            }

            response.setHeader("X-Authenticated-User", email);

        } catch (Exception e) {
            log.debug("Não foi possível extrair informações do token para headers: {}", e.getMessage());
        }
    }

    /**
     * Manipula requisições sem token
     */
    private void handleMissingToken(HttpServletResponse response, String requestURI) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", System.currentTimeMillis());
        error.put("status", 401);
        error.put("error", "Missing Token");
        error.put("message", "Token JWT não fornecido. Faça login para acessar este recurso.");
        error.put("path", requestURI);

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    /**
     * Manipula tokens inválidos
     */
    private void handleInvalidToken(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", System.currentTimeMillis());
        error.put("status", 401);
        error.put("error", "Invalid Token");
        error.put("message", message);
        error.put("path", "");

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    /**
     * Manipula exceções no filtro
     */
    private void handleFilterException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", System.currentTimeMillis());
        error.put("status", 500);
        error.put("error", "Filter Error");
        error.put("message", "Erro interno durante a autenticação");
        error.put("path", "");

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    /**
     * Verifica se o usuário atual tem uma role específica
     */
    public boolean hasRole(HttpServletRequest request, String role) {
        String jwt = extractJwtFromRequest(request);
        if (!StringUtils.hasText(jwt)) {
            return false;
        }

        try {
            String authorities = jwtService.extractAuthorities(jwt);
            return authorities != null && authorities.contains(role);
        } catch (Exception e) {
            log.debug("Falha ao verificar role: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário atual é do tipo especificado
     */
    public boolean isUserType(HttpServletRequest request, String userType) {
        String jwt = extractJwtFromRequest(request);
        if (!StringUtils.hasText(jwt)) {
            return false;
        }

        try {
            String tipoUsuario = jwtService.extractTipoUsuario(jwt);
            return userType.equalsIgnoreCase(tipoUsuario);
        } catch (Exception e) {
            log.debug("Falha ao verificar tipo de usuário: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtém o ID do usuário atual da requisição
     */
    public String getCurrentUserId(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        if (!StringUtils.hasText(jwt)) {
            return null;
        }

        try {
            return jwtService.extractUserId(jwt);
        } catch (Exception e) {
            log.debug("Falha ao obter ID do usuário: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtém o email do usuário atual da requisição
     */
    public String getCurrentUserEmail(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        if (!StringUtils.hasText(jwt)) {
            return null;
        }

        try {
            return jwtService.extractEmail(jwt);
        } catch (Exception e) {
            log.debug("Falha ao obter email do usuário: {}", e.getMessage());
            return null;
        }
    }
}