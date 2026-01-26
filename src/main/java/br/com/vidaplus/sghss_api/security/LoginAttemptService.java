package br.com.vidaplus.sghss_api.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginAttemptService {

    @Value("${app.security.max-login-attempts:5}")
    private int maxAttempts;

    @Value("${app.security.login-block-time:900000}") // 15 minutos padrão
    private long blockTimeMs;

    // Cache para armazenar tentativas falhas por IP
    private final Map<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();

    // Cache para armazenar tentativas falhas por email
    private final Map<String, LoginAttempt> emailAttemptsCache = new ConcurrentHashMap<>();

    /**
     * Registra uma tentativa de login falha
     */
    public void loginFailed(String email, String ipAddress) {
        cleanExpiredAttempts();

        // Registrar por IP
        LoginAttempt ipAttempt = attemptsCache.getOrDefault(ipAddress, new LoginAttempt());
        ipAttempt.incrementAttempts();
        ipAttempt.setLastAttempt(LocalDateTime.now());
        attemptsCache.put(ipAddress, ipAttempt);

        // Registrar por email
        LoginAttempt emailAttempt = emailAttemptsCache.getOrDefault(email, new LoginAttempt());
        emailAttempt.incrementAttempts();
        emailAttempt.setLastAttempt(LocalDateTime.now());
        emailAttemptsCache.put(email, emailAttempt);

        log.warn(" Tentativa de login falha - Email: {}, IP: {}, Tentativas IP: {}, Tentativas Email: {}",
                email, ipAddress, ipAttempt.getAttempts(), emailAttempt.getAttempts());

        // Bloquear se excedeu o limite
        if (ipAttempt.getAttempts() >= maxAttempts) {
            log.warn(" IP bloqueado: {} - Excedeu {} tentativas falhas", ipAddress, maxAttempts);
        }

        if (emailAttempt.getAttempts() >= maxAttempts) {
            log.warn(" Email bloqueado: {} - Excedeu {} tentativas falhas", email, maxAttempts);
        }
    }

    /**
     * Registra um login bem-sucedido
     */
    public void loginSucceeded(String email, String ipAddress) {
        // Limpar tentativas para este IP e email
        attemptsCache.remove(ipAddress);
        emailAttemptsCache.remove(email);

        log.debug(" Login bem-sucedido - Email: {}, IP: {}", email, ipAddress);
    }

    /**
     * Verifica se um IP está bloqueado
     */
    public boolean isBlocked(String ipAddress) {
        cleanExpiredAttempts();

        LoginAttempt attempt = attemptsCache.get(ipAddress);
        if (attempt == null) {
            return false;
        }

        // Verificar se ainda está no período de bloqueio
        if (attempt.getAttempts() >= maxAttempts) {
            LocalDateTime blockUntil = attempt.getLastAttempt().plusNanos(blockTimeMs * 1_000_000);
            if (LocalDateTime.now().isBefore(blockUntil)) {
                return true;
            } else {
                // Período de bloqueio expirou
                attemptsCache.remove(ipAddress);
                return false;
            }
        }

        return false;
    }

    /**
     * Verifica se um email está bloqueado
     */
    public boolean isEmailBlocked(String email) {
        cleanExpiredAttempts();

        LoginAttempt attempt = emailAttemptsCache.get(email);
        if (attempt == null) {
            return false;
        }

        if (attempt.getAttempts() >= maxAttempts) {
            LocalDateTime blockUntil = attempt.getLastAttempt().plusNanos(blockTimeMs * 1_000_000);
            if (LocalDateTime.now().isBefore(blockUntil)) {
                return true;
            } else {
                emailAttemptsCache.remove(email);
                return false;
            }
        }

        return false;
    }

    /**
     * Obtém o número de tentativas restantes antes do bloqueio
     */
    public int getRemainingAttempts(String ipAddress) {
        LoginAttempt attempt = attemptsCache.get(ipAddress);
        if (attempt == null) {
            return maxAttempts;
        }
        return Math.max(0, maxAttempts - attempt.getAttempts());
    }

    /**
     * Obtém o tempo restante de bloqueio em minutos
     */
    public long getRemainingBlockTimeMinutes(String ipAddress) {
        LoginAttempt attempt = attemptsCache.get(ipAddress);
        if (attempt == null || attempt.getAttempts() < maxAttempts) {
            return 0;
        }

        LocalDateTime blockUntil = attempt.getLastAttempt().plusNanos(blockTimeMs * 1_000_000);
        long remainingMs = java.time.Duration.between(LocalDateTime.now(), blockUntil).toMillis();

        return TimeUnit.MILLISECONDS.toMinutes(remainingMs);
    }

    /**
     * Remove bloqueio manualmente (para administração)
     */
    public void unblock(String ipAddress, String email) {
        attemptsCache.remove(ipAddress);
        emailAttemptsCache.remove(email);
        log.info(" Bloqueio removido - IP: {}, Email: {}", ipAddress, email);
    }

    /**
     * Obtém estatísticas de tentativas
     */
    public Map<String, Object> getStatistics() {
        cleanExpiredAttempts();

        long blockedIPs = attemptsCache.values().stream()
                .filter(attempt -> attempt.getAttempts() >= maxAttempts)
                .count();

        long blockedEmails = emailAttemptsCache.values().stream()
                .filter(attempt -> attempt.getAttempts() >= maxAttempts)
                .count();

        return Map.of(
                "totalBlockedIPs", blockedIPs,
                "totalBlockedEmails", blockedEmails,
                "activeIPAttempts", attemptsCache.size(),
                "activeEmailAttempts", emailAttemptsCache.size(),
                "maxAttempts", maxAttempts,
                "blockTimeMinutes", TimeUnit.MILLISECONDS.toMinutes(blockTimeMs)
        );
    }

    /**
     * Limpa tentativas expiradas do cache
     */
    private void cleanExpiredAttempts() {
        LocalDateTime now = LocalDateTime.now();

        // Limpar por IP
        attemptsCache.entrySet().removeIf(entry -> {
            LoginAttempt attempt = entry.getValue();
            if (attempt.getAttempts() >= maxAttempts) {
                LocalDateTime blockUntil = attempt.getLastAttempt().plusNanos(blockTimeMs * 1_000_000);
                return now.isAfter(blockUntil);
            }
            return false;
        });

        // Limpar por email
        emailAttemptsCache.entrySet().removeIf(entry -> {
            LoginAttempt attempt = entry.getValue();
            if (attempt.getAttempts() >= maxAttempts) {
                LocalDateTime blockUntil = attempt.getLastAttempt().plusNanos(blockTimeMs * 1_000_000);
                return now.isAfter(blockUntil);
            }
            return false;
        });
    }

    /**
     * Obtém o IP do cliente da requisição
     */
    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    /**
     * Obtém o IP do cliente método padrão
     */
    public String getClientIP() {
        // Em um ambiente real, você obteria isso do contexto da requisição
        // Por enquanto, retorna um placeholder
        return "127.0.0.1";
    }

    /**
     * Classe interna para representar uma tentativa de login
     */
    private static class LoginAttempt {
        private int attempts;
        @Setter
        private LocalDateTime lastAttempt;

        public LoginAttempt() {
            this.attempts = 0;
            this.lastAttempt = LocalDateTime.now();
        }

        public void incrementAttempts() {
            this.attempts++;
            this.lastAttempt = LocalDateTime.now();
        }

        public int getAttempts() {
            return attempts;
        }

        public LocalDateTime getLastAttempt() {
            return lastAttempt;
        }

    }
}
