package br.com.vidaplus.sghss_api.controller;

import br.com.vidaplus.sghss_api.service.ProfissionalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    @Data
    @Builder
    @AllArgsConstructor
    public static class CreateRequestDto {
        @NotBlank(message = "CRM/COREN é obrigatório")
        @Size(max = 20, message = "CRM/COREN muito longo")
        private String crmCoren;

        @NotBlank(message = "Nome completo é obrigatório")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        private String nomeCompleto;

        @NotBlank(message = "Especialidade é obrigatória")
        @Size(max = 50, message = "Especialidade muito longa")
        private String especialidade;

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String senha;

        @NotBlank(message = "Unidade de trabalho é obrigatória")
        @Size(max = 100, message = "Unidade de trabalho muito longa")
        private String unidadeTrabalho;

        @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos")
        private String telefoneProfissional;

        @Min(value = 1, message = "Carga horária mínima é 1 hora")
        @Max(value = 80, message = "Carga horária máxima é 80 horas")
        private Integer cargaHorariaSemanal;

        @DecimalMin(value = "0.0", message = "Valor da consulta não pode ser negativo")
        private Double valorConsulta;

        private String formacao;
        private String experiencia;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class UpdateRequestDto {
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        private String nomeCompleto;

        @Size(max = 50, message = "Especialidade muito longa")
        private String especialidade;

        @Email(message = "Email inválido")
        private String email;

        @Size(max = 100, message = "Unidade de trabalho muito longa")
        private String unidadeTrabalho;

        @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos")
        private String telefoneProfissional;

        @Min(value = 1, message = "Carga horária mínima é 1 hora")
        @Max(value = 80, message = "Carga horária máxima é 80 horas")
        private Integer cargaHorariaSemanal;

        @DecimalMin(value = "0.0", message = "Valor da consulta não pode ser negativo")
        private Double valorConsulta;

        private String formacao;
        private String experiencia;
        private Boolean disponivel;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class FilterRequestDto {
        private String nome;
        private String especialidade;
        private String crmCoren;
        private String unidadeTrabalho;
        private Boolean disponivel;
        private Boolean ativo;
        private Double valorConsultaMin;
        private Double valorConsultaMax;
    }

    @PostMapping
    public ResponseEntity<ProfissionalService.Response> create(@Valid @RequestBody CreateRequestDto request) {
        log.info("Recebida requisição para criar profissional: {}", request.getNomeCompleto());

        ProfissionalService.CreateRequest serviceRequest = ProfissionalService.CreateRequest.builder()
                .crmCoren(request.getCrmCoren())
                .nomeCompleto(request.getNomeCompleto())
                .especialidade(request.getEspecialidade())
                .email(request.getEmail())
                .senha(request.getSenha())
                .unidadeTrabalho(request.getUnidadeTrabalho())
                .telefoneProfissional(request.getTelefoneProfissional())
                .cargaHorariaSemanal(request.getCargaHorariaSemanal())
                .valorConsulta(request.getValorConsulta())
                .formacao(request.getFormacao())
                .experiencia(request.getExperiencia())
                .build();

        ProfissionalService.Response response = profissionalService.create(serviceRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalService.Response> findById(@PathVariable UUID id) {
        log.debug("Buscando profissional por ID: {}", id);
        ProfissionalService.Response response = profissionalService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/crm-coren/{crmCoren}")
    public ResponseEntity<ProfissionalService.Response> findByCrmCoren(@PathVariable String crmCoren) {
        log.debug("Buscando profissional por CRM/COREN: {}", crmCoren);
        ProfissionalService.Response response = profissionalService.findByCrmCoren(crmCoren);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalService.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRequestDto request) {
        log.info("Atualizando profissional ID: {}", id);

        ProfissionalService.UpdateRequest serviceRequest = ProfissionalService.UpdateRequest.builder()
                .nomeCompleto(request.getNomeCompleto())
                .especialidade(request.getEspecialidade())
                .email(request.getEmail())
                .unidadeTrabalho(request.getUnidadeTrabalho())
                .telefoneProfissional(request.getTelefoneProfissional())
                .cargaHorariaSemanal(request.getCargaHorariaSemanal())
                .valorConsulta(request.getValorConsulta())
                .formacao(request.getFormacao())
                .experiencia(request.getExperiencia())
                .disponivel(request.getDisponivel())
                .build();

        ProfissionalService.Response response = profissionalService.update(id, serviceRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("Inativando profissional ID: {}", id);
        profissionalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProfissionalService.Response>> findAll(
            Pageable pageable,
            @ModelAttribute FilterRequestDto filter) {
        log.debug("Listando profissionais - Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        ProfissionalService.FilterRequest serviceFilter = filter != null ?
                ProfissionalService.FilterRequest.builder()
                        .nome(filter.getNome())
                        .especialidade(filter.getEspecialidade())
                        .crmCoren(filter.getCrmCoren())
                        .unidadeTrabalho(filter.getUnidadeTrabalho())
                        .disponivel(filter.getDisponivel())
                        .ativo(filter.getAtivo())
                        .valorConsultaMin(filter.getValorConsultaMin())
                        .valorConsultaMax(filter.getValorConsultaMax())
                        .build() : null;

        Page<ProfissionalService.Response> page = profissionalService.findAll(pageable, serviceFilter);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/especialidade/{especialidade}")
    public ResponseEntity<List<ProfissionalService.SimpleResponse>> findByEspecialidade(
            @PathVariable String especialidade) {
        log.debug("Buscando profissionais por especialidade: {}", especialidade);
        List<ProfissionalService.SimpleResponse> profissionais = profissionalService.findByEspecialidade(especialidade);
        return ResponseEntity.ok(profissionais);
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<ProfissionalService.SimpleResponse>> findDisponiveis() {
        log.debug("Buscando profissionais disponíveis");
        List<ProfissionalService.SimpleResponse> profissionais = profissionalService.findDisponiveis();
        return ResponseEntity.ok(profissionais);
    }

    @GetMapping("/unidade/{unidade}")
    public ResponseEntity<List<ProfissionalService.SimpleResponse>> findByUnidadeTrabalho(
            @PathVariable String unidade) {
        log.debug("Buscando profissionais por unidade: {}", unidade);
        List<ProfissionalService.SimpleResponse> profissionais = profissionalService.findByUnidadeTrabalho(unidade);
        return ResponseEntity.ok(profissionais);
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<ProfissionalService.StatsResponse> getStatistics() {
        log.debug("Obtendo estatísticas de profissionais");
        ProfissionalService.StatsResponse stats = profissionalService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(
            @PathVariable UUID id,
            @RequestParam boolean ativo) {
        log.info("Alterando status do profissional ID: {} para {}", id, ativo ? "ATIVO" : "INATIVO");
        profissionalService.toggleStatus(id, ativo);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<Void> toggleDisponibilidade(
            @PathVariable UUID id,
            @RequestParam boolean disponivel) {
        log.info("Alterando disponibilidade do profissional ID: {} para {}", id, disponivel);
        profissionalService.toggleDisponibilidade(id, disponivel);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Erro no controller de profissionais: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro interno do servidor: " + e.getMessage());
    }
}