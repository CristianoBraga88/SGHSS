package br.com.vidaplus.sghss_api.controller;

import br.com.vidaplus.sghss_api.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Data
    @Builder
    @AllArgsConstructor
    public static class LoginRequestDto {
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String senha;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RegisterPacienteDto {
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String senha;

        @NotBlank(message = "CPF é obrigatório")
        private String cpf;

        @NotBlank(message = "Nome completo é obrigatório")
        private String nomeCompleto;

        @Past(message = "Data de nascimento deve ser no passado")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate dataNascimento;

        private String telefone;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RegisterProfissionalDto {
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String senha;

        @NotBlank(message = "Nome completo é obrigatório")
        private String nomeCompleto;

        @NotBlank(message = "CRM/COREN é obrigatório")
        private String crmCoren;

        @NotBlank(message = "Especialidade é obrigatória")
        private String especialidade;

        @NotBlank(message = "Unidade de trabalho é obrigatória")
        private String unidadeTrabalho;

        private String telefone;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthService.LoginResponse> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Recebida requisição de login para: {}", request.getEmail());

        AuthService.LoginRequest serviceRequest = AuthService.LoginRequest.builder()
                .email(request.getEmail())
                .senha(request.getSenha())
                .build();

        AuthService.LoginResponse response = authService.login(serviceRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrar/paciente")
    public ResponseEntity<AuthService.LoginResponse> registerPaciente(@Valid @RequestBody RegisterPacienteDto request) {
        log.info("Recebida requisição de registro de paciente para: {}", request.getEmail());

        AuthService.RegisterRequest serviceRequest = AuthService.RegisterRequest.builder()
                .email(request.getEmail())
                .senha(request.getSenha())
                .tipoUsuario("PACIENTE")
                .cpf(request.getCpf())
                .nomeCompleto(request.getNomeCompleto())
                .dataNascimento(request.getDataNascimento())
                .telefone(request.getTelefone())
                .build();

        AuthService.LoginResponse response = authService.registerPaciente(serviceRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrar/profissional")
    public ResponseEntity<AuthService.LoginResponse> registerProfissional(@Valid @RequestBody RegisterProfissionalDto request) {
        log.info("Recebida requisição de registro de profissional para: {}", request.getEmail());

        AuthService.RegisterRequest serviceRequest = AuthService.RegisterRequest.builder()
                .email(request.getEmail())
                .senha(request.getSenha())
                .tipoUsuario("PROFISSIONAL_SAUDE")
                .nomeCompleto(request.getNomeCompleto())
                .crmCoren(request.getCrmCoren())
                .especialidade(request.getEspecialidade())
                .unidadeTrabalho(request.getUnidadeTrabalho())
                .build();

        AuthService.LoginResponse response = authService.registerProfissional(serviceRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Auth API está funcionando");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Erro no controller de autenticação: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro interno do servidor: " + e.getMessage());
    }
}