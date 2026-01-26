package br.com.vidaplus.sghss_api.security;

import br.com.vidaplus.sghss_api.model.Usuario;
import br.com.vidaplus.sghss_api.exception.SecurityException;
import br.com.vidaplus.sghss_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug(" Buscando usuário por email: {}", email);

        // Verificar se o IP está bloqueado
        String clientIP = loginAttemptService.getClientIP();
        if (loginAttemptService.isBlocked(clientIP)) {
            log.warn(" IP bloqueado: {} - Muitas tentativas falhas", clientIP);
            throw SecurityException.muitasTentativasLogin(clientIP);
        }

        // Buscar usuário no banco
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn(" Usuário não encontrado com email: {}", email);
                    loginAttemptService.loginFailed(email, clientIP);
                    return new UsernameNotFoundException("Usuário não encontrado com email: " + email);
                });

        log.debug(" Usuário encontrado: {} - Tipo: {}", email, usuario.getTipo());

        // Verificar se o usuário está ativo
        if (!usuario.isEnabled()) {
            log.warn("  Tentativa de login para usuário inativo: {}", email);
            loginAttemptService.loginFailed(email, clientIP);
            throw SecurityException.usuarioInativo(email);
        }

        // Verificar se a conta não está bloqueada
        if (!usuario.isAccountNonLocked()) {
            log.warn(" Tentativa de login para usuário bloqueado: {}", email);
            throw SecurityException.usuarioBloqueado(email);
        }

        // Verificar se as credenciais não expiraram
        if (!usuario.isCredentialsNonExpired()) {
            log.warn(" Credenciais expiradas para usuário: {}", email);
            throw SecurityException.senhaExpirada();
        }

        // Login bem-sucedido
        loginAttemptService.loginSucceeded(email, clientIP);
        log.info(" Login bem-sucedido: {} - Tipo: {}", email, usuario.getTipo());

        // Atualizar último acesso user;

        return usuario;
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(String userId) {
        log.debug(" Buscando usuário por ID: {}", userId);

        return usuarioRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> {
                    log.warn(" Usuário não encontrado com ID: {}", userId);
                    return new UsernameNotFoundException("Usuário não encontrado com ID: " + userId);
                });
    }

    @Transactional(readOnly = true)
    public Usuario loadUsuarioByEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn(" Usuário não encontrado com email: {}", email);
                    return new UsernameNotFoundException("Usuário não encontrado com email: " + email);
                });
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmailIgnoreCase(email);
    }

    @Transactional
    public void updateLastLogin(Usuario usuario) {
        log.debug(" Último login atualizado para: {}", usuario.getEmail());
    }

    @Transactional
    public void incrementFailedAttempts(String email) {
        usuarioRepository.findByEmailIgnoreCase(email).ifPresent(usuario -> {
            log.debug(" Tentativa falha incrementada para: {}", email);
        });
    }

    @Transactional
    public void resetFailedAttempts(String email) {
        usuarioRepository.findByEmailIgnoreCase(email).ifPresent(usuario -> {
            log.debug(" Tentativas falhas resetadas para: {}", email);
        });
    }

    @Transactional
    public void lockUserAccount(String email) {
        usuarioRepository.findByEmailIgnoreCase(email).ifPresent(usuario -> {
            log.warn(" Conta bloqueada: {}", email);
        });
    }

    @Transactional
    public void unlockUserAccount(String email) {
        usuarioRepository.findByEmailIgnoreCase(email).ifPresent(usuario -> {
            log.info(" Conta desbloqueada: {}", email);
        });
    }

    @Transactional(readOnly = true)
    public boolean isUserActive(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .map(Usuario::isEnabled)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isUserLocked(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .map(usuario -> !usuario.isAccountNonLocked())
                .orElse(false);
    }

    public String getClientIP() {
        return loginAttemptService.getClientIP();
    }
}
