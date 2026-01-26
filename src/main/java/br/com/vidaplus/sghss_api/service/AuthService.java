package br.com.vidaplus.sghss_api.service;

import br.com.vidaplus.sghss_api.exception.SecurityException;
import br.com.vidaplus.sghss_api.exception.ValidationException;
import br.com.vidaplus.sghss_api.model.Usuario;
import br.com.vidaplus.sghss_api.repository.UsuarioRepository;
import br.com.vidaplus.sghss_api.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;

    @Data
    @Builder
    @AllArgsConstructor
    public static class LoginRequest {
        private String email;
        private String senha;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String tipoToken;
        private String email;
        private String nome;
        private String tipoUsuario;
        private Long expiraEm;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RegisterRequest {
        private String email;
        private String senha;
        private String tipoUsuario;
        private String cpf;
        private String nomeCompleto;
        private LocalDate dataNascimento;
        private String telefone;
        private String crmCoren;
        private String especialidade;
        private String unidadeTrabalho;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        private String refreshToken;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        private String senhaAtual;
        private String novaSenha;
        private String confirmacaoSenha;
    }

    /**
     * LOGIN
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Tentativa de login para: {}", request.getEmail());

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email é obrigatório");
        }
        if (request.getSenha() == null || request.getSenha().trim().isEmpty()) {
            throw new ValidationException("Senha é obrigatória");
        }

        // Buscar usuário
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: {}", request.getEmail());
                    return SecurityException.usuarioNaoAutenticado();
                });

        if (!passwordEncoder.matches(request.getSenha(), usuario.getPassword())) {
            log.warn("Senha incorreta para: {}", request.getEmail());
            throw SecurityException.usuarioNaoAutenticado();
        }

        // Verificar se usuário está ativo
        if (!usuario.isEnabled()) {
            log.warn("Usuário inativo: {}", request.getEmail());
            throw SecurityException.usuarioInativo(usuario.getEmail());
        }

        // Gerar token
        String jwtToken = jwtService.generateToken(usuario);

        // Criar resposta
        LoginResponse response = LoginResponse.builder()
                .token(jwtToken)
                .tipoToken("Bearer")
                .email(usuario.getEmail())
                .nome(extractNomeUsuario(usuario))
                .tipoUsuario(usuario.getTipo().name())
                .expiraEm(jwtService.getJwtExpirationMs())
                .build();

        log.info("Login bem-sucedido para: {} - Tipo: {}", request.getEmail(), usuario.getTipo());

        return response;
    }

    /**
     * Registra um novo paciente
     */
    @Transactional
    public LoginResponse registerPaciente(RegisterRequest request) {
        log.info("Registrando novo paciente: {}", request.getEmail());

        // Validações
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email é obrigatório");
        }
        if (request.getSenha() == null || request.getSenha().trim().isEmpty()) {
            throw new ValidationException("Senha é obrigatória");
        }
        if (request.getSenha().length() < 6) {
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres");
        }

        // VALIDAÇÕES DOS CAMPOS OBRIGATÓRIOS
        if (request.getCpf() == null || request.getCpf().trim().isEmpty()) {
            throw new ValidationException("CPF é obrigatório");
        }
        if (request.getNomeCompleto() == null || request.getNomeCompleto().trim().isEmpty()) {
            throw new ValidationException("Nome completo é obrigatório");
        }
        if (request.getDataNascimento() == null) {
            throw new ValidationException("Data de nascimento é obrigatória");
        }

        // Verificar se email já existe
        if (usuarioRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ValidationException("Email já cadastrado");
        }

        // Criar paciente COM TODOS OS CAMPOS OBRIGATÓRIOS
        br.com.vidaplus.sghss_api.model.Paciente paciente = new br.com.vidaplus.sghss_api.model.Paciente();
        paciente.setEmail(request.getEmail());
        paciente.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        paciente.setTipo(Usuario.TipoUsuario.PACIENTE);
        paciente.setAtivo(true);
        paciente.setDataCriacao(LocalDateTime.now());

        // CAMPOS OBRIGATÓRIOS
        paciente.setCpf(request.getCpf());
        paciente.setNomeCompleto(request.getNomeCompleto());
        paciente.setDataNascimento(request.getDataNascimento());

        // CAMPOS OPCIONAIS
        if (request.getTelefone() != null) {
            paciente.setTelefone(request.getTelefone());
        }

        // Salvar no banco
        paciente = usuarioRepository.save(paciente);
        log.info("Paciente registrado com ID: {}", paciente.getId());

        // Gerar token
        String jwtToken = jwtService.generateToken(paciente);

        return LoginResponse.builder()
                .token(jwtToken)
                .tipoToken("Bearer")
                .email(paciente.getEmail())
                .nome(paciente.getNomeCompleto())
                .tipoUsuario(paciente.getTipo().name())
                .expiraEm(jwtService.getJwtExpirationMs())
                .build();
    }

    /**
     * Registra um novo profissional
     */
    @Transactional
    public LoginResponse registerProfissional(RegisterRequest request) {
        log.info("Registrando novo profissional: {}", request.getEmail());

        // Validações
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email é obrigatório");
        }
        if (request.getSenha() == null || request.getSenha().trim().isEmpty()) {
            throw new ValidationException("Senha é obrigatória");
        }
        if (request.getSenha().length() < 6) {
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres");
        }

        // VALIDAÇÕES DOS CAMPOS OBRIGATÓRIOS DO PROFISSIONAL
        if (request.getNomeCompleto() == null || request.getNomeCompleto().trim().isEmpty()) {
            throw new ValidationException("Nome completo é obrigatório");
        }
        if (request.getCrmCoren() == null || request.getCrmCoren().trim().isEmpty()) {
            throw new ValidationException("CRM/COREN é obrigatório");
        }
        if (request.getEspecialidade() == null || request.getEspecialidade().trim().isEmpty()) {
            throw new ValidationException("Especialidade é obrigatória");
        }
        if (request.getUnidadeTrabalho() == null || request.getUnidadeTrabalho().trim().isEmpty()) {
            throw new ValidationException("Unidade de trabalho é obrigatória");
        }

        // Verificar se email já existe
        if (usuarioRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ValidationException("Email já cadastrado");
        }

        // Criar profissional COM TODOS OS CAMPOS OBRIGATÓRIOS
        br.com.vidaplus.sghss_api.model.ProfissionalSaude profissional = new br.com.vidaplus.sghss_api.model.ProfissionalSaude();
        profissional.setEmail(request.getEmail());
        profissional.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        profissional.setTipo(Usuario.TipoUsuario.PROFISSIONAL_SAUDE);
        profissional.setAtivo(true);
        profissional.setDataCriacao(LocalDateTime.now());

        // CAMPOS OBRIGATÓRIOS DO PROFISSIONAL
        profissional.setNomeCompleto(request.getNomeCompleto());
        profissional.setCrmCoren(request.getCrmCoren());
        profissional.setEspecialidade(request.getEspecialidade());
        profissional.setUnidadeTrabalho(request.getUnidadeTrabalho());
        profissional.setDisponivel(true);

        // Salvar no banco
        profissional = usuarioRepository.save(profissional);
        log.info("Profissional registrado com ID: {}", profissional.getId());

        // Gerar token
        String jwtToken = jwtService.generateToken(profissional);

        return LoginResponse.builder()
                .token(jwtToken)
                .tipoToken("Bearer")
                .email(profissional.getEmail())
                .nome(profissional.getNomeCompleto())
                .tipoUsuario(profissional.getTipo().name())
                .expiraEm(jwtService.getJwtExpirationMs())
                .build();
    }

    /**
     * Obtém informações do usuário atual
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentUserInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("message", "Endpoint /me precisa de token JWT para funcionar");
        info.put("status", "implementar_mais_tarde");
        return info;
    }

    // ========== MÉTODOS PRIVADOS ==========

    private String extractNomeUsuario(Usuario usuario) {
        if (usuario instanceof br.com.vidaplus.sghss_api.model.Paciente paciente) {
            return paciente.getNomeCompleto() != null ? paciente.getNomeCompleto() : "Paciente";
        } else if (usuario instanceof br.com.vidaplus.sghss_api.model.ProfissionalSaude profissional) {
            return profissional.getNomeCompleto() != null ? profissional.getNomeCompleto() : "Profissional";
        }
        return "Usuário";
    }
}