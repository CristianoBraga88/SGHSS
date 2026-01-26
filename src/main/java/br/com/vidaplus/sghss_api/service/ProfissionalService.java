package br.com.vidaplus.sghss_api.service;

import br.com.vidaplus.sghss_api.config.AppConfig;
import br.com.vidaplus.sghss_api.exception.BusinessException;
import br.com.vidaplus.sghss_api.exception.ResourceNotFoundException;
import br.com.vidaplus.sghss_api.exception.SecurityException;
import br.com.vidaplus.sghss_api.exception.ValidationException;
import br.com.vidaplus.sghss_api.model.ProfissionalSaude;
import br.com.vidaplus.sghss_api.model.Usuario;
import br.com.vidaplus.sghss_api.repository.ProfissionalSaudeRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalSaudeRepository profissionalRepository;
    private final AppConfig.ValidationUtils validationUtils;
    private final AppConfig.FormattingUtils formattingUtils;

    @Data
    @Builder
    @AllArgsConstructor
    public static class CreateRequest {
        private String crmCoren;
        private String nomeCompleto;
        private String especialidade;
        private String email;
        private String senha;
        private String unidadeTrabalho;
        private String telefoneProfissional;
        private Integer cargaHorariaSemanal;
        private Double valorConsulta;
        private String formacao;
        private String experiencia;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class UpdateRequest {
        private String nomeCompleto;
        private String especialidade;
        private String email;
        private String unidadeTrabalho;
        private String telefoneProfissional;
        private Integer cargaHorariaSemanal;
        private Double valorConsulta;
        private String formacao;
        private String experiencia;
        private Boolean disponivel;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String crmCoren;
        private String nomeCompleto;
        private String especialidade;
        private String email;
        private String unidadeTrabalho;
        private String telefoneProfissional;
        private Integer cargaHorariaSemanal;
        private Double valorConsulta;
        private String formacao;
        private String experiencia;
        private Boolean disponivel;
        private Boolean ativo;
        private String dataCriacao;
        private String dataAtualizacao;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class SimpleResponse {
        private UUID id;
        private String nomeCompleto;
        private String especialidade;
        private String crmCoren;
        private Boolean disponivel;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class AgendaResponse {
        private UUID id;
        private String nomeCompleto;
        private String especialidade;
        private String unidadeTrabalho;
        private Boolean disponivel;
        private Integer consultasAgendadasHoje;
        private Integer proximoHorarioDisponivel;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class FilterRequest {
        private String nome;
        private String especialidade;
        private String crmCoren;
        private String unidadeTrabalho;
        private Boolean disponivel;
        private Boolean ativo;
        private Double valorConsultaMin;
        private Double valorConsultaMax;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class StatsResponse {
        private Long totalProfissionais;
        private Long profissionaisDisponiveis;
        private Long porEspecialidade;
        private String especialidadeMaisComum;
        private Double valorConsultaMedio;
    }

    /**
     * Cria um novo profissional
     */
    @Transactional
    public Response create(CreateRequest request) {
        log.info("Criando novo profissional: {}", request.getNomeCompleto());

        // Validações
        validateCreateRequest(request);

        // Verificar se CRM/COREN já existe
        if (profissionalRepository.existsByCrmCoren(request.getCrmCoren())) {
            throw BusinessException.crmCorenJaCadastrado(request.getCrmCoren());
        }

        // Verificar se email já existe
        if (profissionalRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw BusinessException.emailJaCadastrado(request.getEmail());
        }

        // Criar profissional manualmente
        ProfissionalSaude profissional = ProfissionalSaude.builder()
                .email(request.getEmail())
                .senhaHash(org.springframework.security.crypto.bcrypt.BCrypt.hashpw(
                        request.getSenha(),
                        org.springframework.security.crypto.bcrypt.BCrypt.gensalt(12)))
                .tipo(Usuario.TipoUsuario.PROFISSIONAL_SAUDE)
                .ativo(true)
                .dataCriacao(LocalDateTime.now())
                .crmCoren(request.getCrmCoren())
                .nomeCompleto(request.getNomeCompleto())
                .especialidade(request.getEspecialidade())
                .disponivel(true)
                .unidadeTrabalho(request.getUnidadeTrabalho())
                .telefoneProfissional(request.getTelefoneProfissional() != null ?
                        formattingUtils.formatPhone(request.getTelefoneProfissional()) : null)
                .cargaHorariaSemanal(request.getCargaHorariaSemanal())
                .valorConsulta(request.getValorConsulta())
                .formacao(request.getFormacao())
                .experiencia(request.getExperiencia())
                .build();

        // Salvar
        profissional = profissionalRepository.save(profissional);
        log.info("Profissional criado com ID: {}", profissional.getId());

        return toResponse(profissional);
    }

    /**
     * Busca profissional por ID
     */
    @Transactional(readOnly = true)
    public Response findById(UUID id) {
        log.debug("Buscando profissional por ID: {}", id);

        ProfissionalSaude profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forProfissionalId(id));

        return toResponse(profissional);
    }

    /**
     * Busca profissional por CRM/COREN
     */
    @Transactional(readOnly = true)
    public Response findByCrmCoren(String crmCoren) {
        log.debug("Buscando profissional por CRM/COREN: {}", crmCoren);

        ProfissionalSaude profissional = profissionalRepository.findByCrmCoren(crmCoren)
                .orElseThrow(() -> ResourceNotFoundException.forProfissional("crmCoren", crmCoren));

        return toResponse(profissional);
    }

    /**
     * Atualiza um profissional
     */
    @Transactional
    public Response update(UUID id, UpdateRequest request) {
        log.info("Atualizando profissional ID: {}", id);

        ProfissionalSaude profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forProfissionalId(id));

        // Verificar permissões
        checkProfissionalUpdateAccess(profissional);

        // Validações de atualização
        validateUpdateRequest(request, profissional);

        // Atualizar campos permitidos
        ProfissionalSaude updatedProfissional = ProfissionalSaude.builder()
                .id(profissional.getId())
                .email(request.getEmail() != null ? request.getEmail() : profissional.getEmail())
                .senhaHash(profissional.getSenhaHash())
                .tipo(profissional.getTipo())
                .ativo(profissional.getAtivo())
                .dataCriacao(profissional.getDataCriacao())
                .crmCoren(profissional.getCrmCoren())
                .nomeCompleto(request.getNomeCompleto() != null ? request.getNomeCompleto() : profissional.getNomeCompleto())
                .especialidade(request.getEspecialidade() != null ? request.getEspecialidade() : profissional.getEspecialidade())
                .disponivel(request.getDisponivel() != null ? request.getDisponivel() : profissional.getDisponivel())
                .unidadeTrabalho(request.getUnidadeTrabalho() != null ? request.getUnidadeTrabalho() : profissional.getUnidadeTrabalho())
                .telefoneProfissional(request.getTelefoneProfissional() != null ?
                        formattingUtils.formatPhone(request.getTelefoneProfissional()) :
                        profissional.getTelefoneProfissional())
                .cargaHorariaSemanal(request.getCargaHorariaSemanal() != null ?
                        request.getCargaHorariaSemanal() : profissional.getCargaHorariaSemanal())
                .valorConsulta(request.getValorConsulta() != null ?
                        request.getValorConsulta() : profissional.getValorConsulta())
                .formacao(request.getFormacao() != null ? request.getFormacao() : profissional.getFormacao())
                .experiencia(request.getExperiencia() != null ? request.getExperiencia() : profissional.getExperiencia())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        profissional = profissionalRepository.save(updatedProfissional);
        log.info("Profissional atualizado ID: {}", id);

        return toResponse(profissional);
    }

    /**
     * Remove um profissional
     */
    @Transactional
    public void delete(UUID id) {
        log.info("Inativando profissional ID: {}", id);

        ProfissionalSaude profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forProfissionalId(id));

        // Verificar se usuário tem permissão para deletar
        checkProfissionalDeleteAccess();

        // Soft delete (inativar)
        profissional.setAtivo(false);
        profissional.setDisponivel(false);
        profissional.setDataAtualizacao(LocalDateTime.now());

        profissionalRepository.save(profissional);
        log.info("Profissional inativado ID: {}", id);
    }

    /**
     * Lista profissionais com paginação
     */
    @Transactional(readOnly = true)
    public Page<Response> findAll(Pageable pageable, FilterRequest filter) {
        log.debug("Listando profissionais - Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<ProfissionalSaude> profissionaisPage;

        if (filter != null) {
            // Obter TODOS os profissionais primeiro
            List<ProfissionalSaude> todosProfissionais = profissionalRepository.findAll();

            // Aplicar filtros na lista
            List<ProfissionalSaude> filtrados = todosProfissionais.stream()
                    .filter(p -> filter.getNome() == null ||
                            (p.getEmail() != null && p.getEmail().toLowerCase().contains(
                                    filter.getNome().toLowerCase())))
                    .filter(p -> filter.getEspecialidade() == null ||
                            (p.getEspecialidade() != null && p.getEspecialidade().equalsIgnoreCase(
                                    filter.getEspecialidade())))
                    .filter(p -> filter.getDisponivel() == null ||
                            p.getDisponivel().equals(filter.getDisponivel()))
                    .filter(p -> filter.getAtivo() == null ||
                            p.getAtivo().equals(filter.getAtivo()))
                    .filter(p -> filter.getUnidadeTrabalho() == null ||
                            (p.getUnidadeTrabalho() != null && p.getUnidadeTrabalho().equalsIgnoreCase(
                                    filter.getUnidadeTrabalho())))
                    .filter(p -> filter.getCrmCoren() == null ||
                            (p.getCrmCoren() != null && p.getCrmCoren().equalsIgnoreCase(
                                    filter.getCrmCoren())))
                    .filter(p -> filter.getValorConsultaMin() == null ||
                            (p.getValorConsulta() != null && p.getValorConsulta() >= filter.getValorConsultaMin()))
                    .filter(p -> filter.getValorConsultaMax() == null ||
                            (p.getValorConsulta() != null && p.getValorConsulta() <= filter.getValorConsultaMax()))
                    .collect(Collectors.toList());

            // Paginar manualmente
            int total = filtrados.size();
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), total);

            List<ProfissionalSaude> pagina = (start <= end) ?
                    filtrados.subList(start, end) :
                    java.util.Collections.emptyList();

            profissionaisPage = new PageImpl<>(pagina, pageable, total);
        } else {
            profissionaisPage = profissionalRepository.findAll(pageable);
        }

        return profissionaisPage.map(this::toResponse);
    }

    /**
     * Busca profissionais por especialidade
     */
    @Transactional(readOnly = true)
    public List<SimpleResponse> findByEspecialidade(String especialidade) {
        log.debug("Buscando profissionais por especialidade: {}", especialidade);

        List<ProfissionalSaude> profissionais = profissionalRepository.findByEspecialidade(especialidade);

        return profissionais.stream()
                .map(this::toSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca profissionais disponíveis
     */
    @Transactional(readOnly = true)
    public List<SimpleResponse> findDisponiveis() {
        log.debug("Buscando profissionais disponíveis");

        List<ProfissionalSaude> profissionais = profissionalRepository.findByDisponivelTrue();

        return profissionais.stream()
                .map(this::toSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca profissionais por unidade de trabalho
     */
    @Transactional(readOnly = true)
    public List<SimpleResponse> findByUnidadeTrabalho(String unidade) {
        log.debug("Buscando profissionais por unidade: {}", unidade);

        List<ProfissionalSaude> profissionais = profissionalRepository.findByUnidadeTrabalho(unidade);

        return profissionais.stream()
                .map(this::toSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtém estatísticas de profissionais
     */
    @Transactional(readOnly = true)
    public StatsResponse getStatistics() {
        log.debug("Obtendo estatísticas de profissionais");

        List<ProfissionalSaude> todos = profissionalRepository.findAll();

        long total = todos.size();
        long disponiveis = todos.stream().filter(ProfissionalSaude::getDisponivel).count();

        // Contagem por especialidade
        long porEspecialidade = todos.stream()
                .map(ProfissionalSaude::getEspecialidade)
                .distinct()
                .count();

        // Especialidade mais comum
        String especialidadeMaisComum = todos.stream()
                .filter(p -> p.getEspecialidade() != null)
                .collect(Collectors.groupingBy(
                        ProfissionalSaude::getEspecialidade,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("Nenhuma");

        // Valor médio da consulta
        double valorConsultaMedio = todos.stream()
                .filter(p -> p.getValorConsulta() != null && p.getValorConsulta() > 0)
                .mapToDouble(ProfissionalSaude::getValorConsulta)
                .average()
                .orElse(0.0);

        return StatsResponse.builder()
                .totalProfissionais(total)
                .profissionaisDisponiveis(disponiveis)
                .porEspecialidade(porEspecialidade)
                .especialidadeMaisComum(especialidadeMaisComum)
                .valorConsultaMedio(valorConsultaMedio)
                .build();
    }

    /**
     * Ativa/desativa um profissional
     */
    @Transactional
    public void toggleStatus(UUID id, boolean ativo) {
        log.info("Alterando status do profissional ID: {} para {}", id, ativo ? "ATIVO" : "INATIVO");

        ProfissionalSaude profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forProfissionalId(id));

        checkProfissionalUpdateAccess(profissional);

        profissional.setAtivo(ativo);
        if (!ativo) {
            profissional.setDisponivel(false);
        }
        profissional.setDataAtualizacao(LocalDateTime.now());

        profissionalRepository.save(profissional);
        log.info("Status do profissional ID: {} alterado para {}", id, ativo ? "ATIVO" : "INATIVO");
    }

    /**
     * Altera disponibilidade do profissional
     */
    @Transactional
    public void toggleDisponibilidade(UUID id, boolean disponivel) {
        log.info("Alterando disponibilidade do profissional ID: {} para {}", id, disponivel);

        ProfissionalSaude profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forProfissionalId(id));

        // Profissional só pode alterar sua própria disponibilidade
        checkProfissionalSelfAccess(profissional);

        if (!profissional.getAtivo()) {
            throw BusinessException.profissionalInativo("Profissional ID: " + id);
        }

        profissional.setDisponivel(disponivel);
        profissional.setDataAtualizacao(LocalDateTime.now());

        profissionalRepository.save(profissional);
        log.info("Disponibilidade do profissional ID: {} alterada para {}", id, disponivel);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void validateCreateRequest(CreateRequest request) {
        if (request.getCrmCoren() == null || request.getCrmCoren().trim().isEmpty()) {
            throw new ValidationException("CRM/COREN é obrigatório", "crmCoren");
        }

        if (request.getNomeCompleto() == null || request.getNomeCompleto().trim().isEmpty()) {
            throw new ValidationException("Nome completo é obrigatório", "nomeCompleto");
        }

        if (request.getEspecialidade() == null || request.getEspecialidade().trim().isEmpty()) {
            throw new ValidationException("Especialidade é obrigatória", "especialidade");
        }

        if (request.getUnidadeTrabalho() == null || request.getUnidadeTrabalho().trim().isEmpty()) {
            throw new ValidationException("Unidade de trabalho é obrigatória", "unidadeTrabalho");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email é obrigatório", "email");
        }

        if (!validationUtils.isValidEmail(request.getEmail())) {
            throw ValidationException.emailInvalido(request.getEmail());
        }

        if (request.getTelefoneProfissional() != null && !validationUtils.isValidPhone(request.getTelefoneProfissional())) {
            throw ValidationException.telefoneInvalido(request.getTelefoneProfissional());
        }

        if (request.getCargaHorariaSemanal() != null &&
                (request.getCargaHorariaSemanal() < 1 || request.getCargaHorariaSemanal() > 80)) {
            throw ValidationException.valorForaIntervalo(
                    "cargaHorariaSemanal",
                    request.getCargaHorariaSemanal(),
                    1, 80
            );
        }

        if (request.getValorConsulta() != null && request.getValorConsulta() < 0) {
            throw new ValidationException("Valor da consulta não pode ser negativo", "valorConsulta");
        }

        if (request.getSenha() == null || request.getSenha().length() < 6) {
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres", "senha");
        }
    }

    private void validateUpdateRequest(UpdateRequest request, ProfissionalSaude profissional) {
        if (request.getEmail() != null && !validationUtils.isValidEmail(request.getEmail())) {
            throw ValidationException.emailInvalido(request.getEmail());
        }

        if (request.getTelefoneProfissional() != null && !validationUtils.isValidPhone(request.getTelefoneProfissional())) {
            throw ValidationException.telefoneInvalido(request.getTelefoneProfissional());
        }

        if (request.getCargaHorariaSemanal() != null &&
                (request.getCargaHorariaSemanal() < 1 || request.getCargaHorariaSemanal() > 80)) {
            throw ValidationException.valorForaIntervalo(
                    "cargaHorariaSemanal",
                    request.getCargaHorariaSemanal(),
                    1, 80
            );
        }

        if (request.getValorConsulta() != null && request.getValorConsulta() < 0) {
            throw new ValidationException("Valor da consulta não pode ser negativo", "valorConsulta");
        }
    }

    private void checkProfissionalUpdateAccess(ProfissionalSaude profissional) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Usuário não autenticado", "USUARIO_NAO_AUTENTICADO");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Usuario)) {
            throw new SecurityException("Tipo de usuário inválido", "USUARIO_TIPO_INVALIDO");
        }

        Usuario usuario = (Usuario) principal;

        // Apenas administradores podem atualizar dados de outros profissionais
        if (usuario.getTipo() != Usuario.TipoUsuario.ADMINISTRADOR &&
                !usuario.getId().equals(profissional.getId())) {
            throw new SecurityException(
                    "Apenas administradores podem atualizar dados de outros profissionais",
                    "ACESSO_NEGADO_ATUALIZACAO_PROFISSIONAL"
            );
        }
    }

    private void checkProfissionalDeleteAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Usuário não autenticado", "USUARIO_NAO_AUTENTICADO");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Usuario)) {
            throw new SecurityException("Tipo de usuário inválido", "USUARIO_TIPO_INVALIDO");
        }

        Usuario usuario = (Usuario) principal;

        // Apenas administradores podem deletar profissionais
        if (usuario.getTipo() != Usuario.TipoUsuario.ADMINISTRADOR) {
            throw new SecurityException(
                    "Apenas administradores podem inativar profissionais",
                    "ACESSO_NEGADO_EXCLUSAO_PROFISSIONAL"
            );
        }
    }

    private void checkProfissionalSelfAccess(ProfissionalSaude profissional) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Usuário não autenticado", "USUARIO_NAO_AUTENTICADO");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Usuario)) {
            throw new SecurityException("Tipo de usuário inválido", "USUARIO_TIPO_INVALIDO");
        }

        Usuario usuario = (Usuario) principal;

        // Profissional só pode alterar seus próprios dados
        if (!usuario.getId().equals(profissional.getId())) {
            throw new SecurityException(
                    "Apenas o próprio profissional pode alterar sua disponibilidade",
                    "ACESSO_NEGADO_DISPONIBILIDADE"
            );
        }
    }

    private Response toResponse(ProfissionalSaude profissional) {
        return Response.builder()
                .id(profissional.getId())
                .crmCoren(profissional.getCrmCoren())
                .nomeCompleto(profissional.getNomeCompleto())
                .especialidade(profissional.getEspecialidade())
                .email(profissional.getEmail())
                .unidadeTrabalho(profissional.getUnidadeTrabalho())
                .telefoneProfissional(profissional.getTelefoneProfissional())
                .cargaHorariaSemanal(profissional.getCargaHorariaSemanal())
                .valorConsulta(profissional.getValorConsulta())
                .formacao(profissional.getFormacao())
                .experiencia(profissional.getExperiencia())
                .disponivel(profissional.getDisponivel())
                .ativo(profissional.getAtivo())
                .dataCriacao(profissional.getDataCriacao() != null ?
                        formattingUtils.formatDateTime(profissional.getDataCriacao()) : null)
                .dataAtualizacao(profissional.getDataAtualizacao() != null ?
                        formattingUtils.formatDateTime(profissional.getDataAtualizacao()) : null)
                .build();
    }

    private SimpleResponse toSimpleResponse(ProfissionalSaude profissional) {
        return SimpleResponse.builder()
                .id(profissional.getId())
                .nomeCompleto(profissional.getNomeCompleto())
                .especialidade(profissional.getEspecialidade())
                .crmCoren(profissional.getCrmCoren())
                .disponivel(profissional.getDisponivel())
                .build();
    }
}