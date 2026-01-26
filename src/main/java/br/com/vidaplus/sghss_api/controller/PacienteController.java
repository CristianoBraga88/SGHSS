package br.com.vidaplus.sghss_api.controller;

import br.com.vidaplus.sghss_api.service.PacienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @Data
    @Builder
    @AllArgsConstructor
    public static class CreateRequestDto {
        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
        private String cpf;

        @NotBlank(message = "Nome completo é obrigatório")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        private String nomeCompleto;

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve ser no passado")
        @DateTimeFormat(pattern = "dd/MM/yyyy")
        private LocalDate dataNascimento;

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String senha;

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos")
        private String telefone;

        @Size(max = 200, message = "Endereço muito longo")
        private String endereco;

        @Size(max = 50, message = "Convênio muito longo")
        private String convenio;

        private String contatoEmergencia;
        private String telefoneEmergencia;
        private String alergias;
        private String medicamentosUso;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class UpdateRequestDto {
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        private String nomeCompleto;

        @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos")
        private String telefone;

        @Size(max = 200, message = "Endereço muito longo")
        private String endereco;

        @Size(max = 50, message = "Convênio muito longo")
        private String convenio;

        private String contatoEmergencia;
        private String telefoneEmergencia;
        private String alergias;
        private String medicamentosUso;

        @Email(message = "Email inválido")
        private String email;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class FilterRequestDto {
        private String nome;
        private String cpf;
        private String convenio;

        @DateTimeFormat(pattern = "dd/MM/yyyy")
        private LocalDate dataNascimentoInicio;

        @DateTimeFormat(pattern = "dd/MM/yyyy")
        private LocalDate dataNascimentoFim;

        private Boolean ativo;
        private String cidade;
    }

    @PostMapping
    public ResponseEntity<PacienteService.Response> create(@Valid @RequestBody CreateRequestDto request) {
        log.info("Recebida requisição para criar paciente: {}", request.getNomeCompleto());

        PacienteService.CreateRequest serviceRequest = PacienteService.CreateRequest.builder()
                .cpf(request.getCpf())
                .nomeCompleto(request.getNomeCompleto())
                .dataNascimento(request.getDataNascimento())
                .email(request.getEmail())
                .senha(request.getSenha())
                .telefone(request.getTelefone())
                .endereco(request.getEndereco())
                .convenio(request.getConvenio())
                .contatoEmergencia(request.getContatoEmergencia())
                .telefoneEmergencia(request.getTelefoneEmergencia())
                .alergias(request.getAlergias())
                .medicamentosUso(request.getMedicamentosUso())
                .build();

        PacienteService.Response response = pacienteService.create(serviceRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteService.Response> findById(@PathVariable UUID id) {
        log.debug("Buscando paciente por ID: {}", id);
        PacienteService.Response response = pacienteService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<PacienteService.Response> findByCpf(@PathVariable String cpf) {
        log.debug("Buscando paciente por CPF: {}", cpf);
        PacienteService.Response response = pacienteService.findByCpf(cpf);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteService.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRequestDto request) {
        log.info("Atualizando paciente ID: {}", id);

        PacienteService.UpdateRequest serviceRequest = PacienteService.UpdateRequest.builder()
                .nomeCompleto(request.getNomeCompleto())
                .telefone(request.getTelefone())
                .endereco(request.getEndereco())
                .convenio(request.getConvenio())
                .contatoEmergencia(request.getContatoEmergencia())
                .telefoneEmergencia(request.getTelefoneEmergencia())
                .alergias(request.getAlergias())
                .medicamentosUso(request.getMedicamentosUso())
                .email(request.getEmail())
                .build();

        PacienteService.Response response = pacienteService.update(id, serviceRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("Inativando paciente ID: {}", id);
        pacienteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<PacienteService.Response>> findAll(
            Pageable pageable,
            @ModelAttribute FilterRequestDto filter) {
        log.debug("Listando pacientes - Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Converter DTO do controller para request do service
        PacienteService.FilterRequest serviceFilter = filter != null ?
                PacienteService.FilterRequest.builder()
                        .nome(filter.getNome())
                        .cpf(filter.getCpf())
                        .convenio(filter.getConvenio())
                        .dataNascimentoInicio(filter.getDataNascimentoInicio())
                        .dataNascimentoFim(filter.getDataNascimentoFim())
                        .ativo(filter.getAtivo())
                        .cidade(filter.getCidade())
                        .build() : null;

        Page<PacienteService.Response> page = pacienteService.findAll(pageable, serviceFilter);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<PacienteService.SimpleResponse>> searchByNome(
            @RequestParam String nome) {
        log.debug("Buscando pacientes por nome: {}", nome);
        List<PacienteService.SimpleResponse> pacientes = pacienteService.searchByNome(nome);
        return ResponseEntity.ok(pacientes);
    }

    @GetMapping("/convenio/{convenio}")
    public ResponseEntity<List<PacienteService.SimpleResponse>> findByConvenio(
            @PathVariable String convenio) {
        log.debug("Buscando pacientes por convênio: {}", convenio);
        List<PacienteService.SimpleResponse> pacientes = pacienteService.findByConvenio(convenio);
        return ResponseEntity.ok(pacientes);
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<PacienteService.StatsResponse> getStatistics() {
        log.debug("Obtendo estatísticas de pacientes");
        PacienteService.StatsResponse stats = pacienteService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(
            @PathVariable UUID id,
            @RequestParam boolean ativo) {
        log.info("Alterando status do paciente ID: {} para {}", id, ativo ? "ATIVO" : "INATIVO");
        pacienteService.toggleStatus(id, ativo);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Erro no controller de pacientes: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro interno do servidor: " + e.getMessage());
    }
}