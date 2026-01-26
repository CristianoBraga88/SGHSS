package br.com.vidaplus.sghss_api.service;

import br.com.vidaplus.sghss_api.config.AppConfig;
import br.com.vidaplus.sghss_api.config.ModelMapperConfig;
import br.com.vidaplus.sghss_api.exception.BusinessException;
import br.com.vidaplus.sghss_api.exception.ResourceNotFoundException;
import br.com.vidaplus.sghss_api.exception.SecurityException;
import br.com.vidaplus.sghss_api.exception.ValidationException;
import br.com.vidaplus.sghss_api.model.Paciente;
import br.com.vidaplus.sghss_api.model.Usuario;
import br.com.vidaplus.sghss_api.repository.PacienteRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ModelMapperConfig.ModelMapperUtils modelMapperUtils;
    private final AppConfig.ValidationUtils validationUtils;
    private final AppConfig.FormattingUtils formattingUtils;

    @Data
    @Builder
    @AllArgsConstructor
    public static class CreateRequest {
        private String cpf;
        private String nomeCompleto;
        private LocalDate dataNascimento;
        private String email;
        private String senha;
        private String telefone;
        private String endereco;
        private String convenio;
        private String contatoEmergencia;
        private String telefoneEmergencia;
        private String alergias;
        private String medicamentosUso;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class UpdateRequest {
        private String nomeCompleto;
        private String telefone;
        private String endereco;
        private String convenio;
        private String contatoEmergencia;
        private String telefoneEmergencia;
        private String alergias;
        private String medicamentosUso;
        private String email;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String cpf;
        private String nomeCompleto;
        private LocalDate dataNascimento;
        private String email;
        private String telefone;
        private String endereco;
        private String convenio;
        private String contatoEmergencia;
        private String telefoneEmergencia;
        private String alergias;
        private String medicamentosUso;
        private Boolean ativo;
        private Integer idade;
        private String dataCriacao;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class SimpleResponse {
        private UUID id;
        private String nomeCompleto;
        private String cpf;
        private String email;
        private String telefone;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class FilterRequest {
        private String nome;
        private String cpf;
        private String convenio;
        private LocalDate dataNascimentoInicio;
        private LocalDate dataNascimentoFim;
        private Boolean ativo;
        private String cidade;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class StatsResponse {
        private Long totalPacientes;
        private Long pacientesAtivos;
        private Long pacientesNovosUltimoMes;
        private String convenioMaisComum;
        private Double mediaIdade;
    }

    /**
     * Cria um novo paciente
     */
    @Transactional
    public Response create(CreateRequest request) {
        log.info("Criando novo paciente: {}", request.getNomeCompleto());

        // Validações
        validateCreateRequest(request);

        // Verificar se CPF já existe
        if (pacienteRepository.existsByCpf(request.getCpf())) {
            throw BusinessException.cpfJaCadastrado(request.getCpf());
        }

        // Verificar se email já existe
        if (pacienteRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw BusinessException.emailJaCadastrado(request.getEmail());
        }

        // Criar paciente
        Paciente paciente = Paciente.builder()
                .cpf(formattingUtils.formatCPF(request.getCpf()))
                .nomeCompleto(request.getNomeCompleto())
                .dataNascimento(request.getDataNascimento())
                .email(request.getEmail())
                .senhaHash(org.springframework.security.crypto.bcrypt.BCrypt.hashpw(
                        request.getSenha(), org.springframework.security.crypto.bcrypt.BCrypt.gensalt(12)))
                .telefone(request.getTelefone() != null ? formattingUtils.formatPhone(request.getTelefone()) : null)
                .endereco(request.getEndereco())
                .convenio(request.getConvenio())
                .contatoEmergencia(request.getContatoEmergencia())
                .telefoneEmergencia(request.getTelefoneEmergencia())
                .alergias(request.getAlergias())
                .medicamentosUso(request.getMedicamentosUso())
                .tipo(Usuario.TipoUsuario.PACIENTE)
                .ativo(true)
                .dataCriacao(LocalDateTime.now())
                .build();

        // Salvar
        paciente = pacienteRepository.save(paciente);
        log.info("Paciente criado com ID: {}", paciente.getId());

        return toResponse(paciente);
    }

    /**
     * Busca paciente por ID
     */
    @Transactional(readOnly = true)
    public Response findById(UUID id) {
        log.debug("Buscando paciente por ID: {}", id);

        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forPacienteId(id));

        // Verificar se usuário tem permissão para ver este paciente
        checkPacienteAccess(paciente);

        return toResponse(paciente);
    }

    /**
     * Busca paciente por CPF
     */
    @Transactional(readOnly = true)
    public Response findByCpf(String cpf) {
        log.debug("Buscando paciente por CPF: {}", cpf);

        String cpfFormatado = formattingUtils.formatCPF(cpf);
        Paciente paciente = pacienteRepository.findByCpf(cpfFormatado)
                .orElseThrow(() -> ResourceNotFoundException.forPaciente("cpf", cpf));

        checkPacienteAccess(paciente);

        return toResponse(paciente);
    }

    /**
     * Atualiza um paciente
     */
    @Transactional
    public Response update(UUID id, UpdateRequest request) {
        log.info("Atualizando paciente ID: {}", id);

        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forPacienteId(id));

        // Verificar permissões
        checkPacienteUpdateAccess(paciente);

        // Validações de atualização
        validateUpdateRequest(request, paciente);

        // Atualizar campos permitidos
        if (request.getNomeCompleto() != null) {
            paciente.setNomeCompleto(request.getNomeCompleto());
        }

        if (request.getTelefone() != null) {
            if (!validationUtils.isValidPhone(request.getTelefone())) {
                throw ValidationException.telefoneInvalido(request.getTelefone());
            }
            paciente.setTelefone(formattingUtils.formatPhone(request.getTelefone()));
        }

        if (request.getEndereco() != null) {
            paciente.setEndereco(request.getEndereco());
        }

        if (request.getConvenio() != null) {
            paciente.setConvenio(request.getConvenio());
        }

        if (request.getContatoEmergencia() != null) {
            paciente.setContatoEmergencia(request.getContatoEmergencia());
        }

        if (request.getTelefoneEmergencia() != null) {
            paciente.setTelefoneEmergencia(request.getTelefoneEmergencia());
        }

        if (request.getAlergias() != null) {
            paciente.setAlergias(request.getAlergias());
        }

        if (request.getMedicamentosUso() != null) {
            paciente.setMedicamentosUso(request.getMedicamentosUso());
        }

        if (request.getEmail() != null && !request.getEmail().equals(paciente.getEmail())) {
            // Verificar se novo email já existe
            if (pacienteRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw BusinessException.emailJaCadastrado(request.getEmail());
            }
            paciente.setEmail(request.getEmail());
        }

        // Atualizar data de modificação
        paciente.setDataAtualizacao(LocalDateTime.now());

        paciente = pacienteRepository.save(paciente);
        log.info("Paciente atualizado ID: {}", id);

        return toResponse(paciente);
    }

    /**
     * Remove um paciente
     */
    @Transactional
    public void delete(UUID id) {
        log.info("Inativando paciente ID: {}", id);

        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forPacienteId(id));

        // Verificar se usuário tem permissão para deletar
        checkPacienteDeleteAccess(paciente);

        // Soft delete (inativar)
        paciente.setAtivo(false);
        paciente.setDataAtualizacao(LocalDateTime.now());

        pacienteRepository.save(paciente);
        log.info("Paciente inativado ID: {}", id);
    }

    /**
     * Lista pacientes com paginação
     */
    @Transactional(readOnly = true)
    public Page<Response> findAll(Pageable pageable, FilterRequest filter) {
        log.debug("Listando pacientes - Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Paciente> pacientesPage;

        if (filter != null) {
            // Aplicar filtros
            pacientesPage = pacienteRepository.findAll(pageable);

            List<Paciente> filtered = pacientesPage.getContent().stream()
                    .filter(p -> filter.getNome() == null ||
                            (p.getNomeCompleto() != null &&
                                    p.getNomeCompleto().toLowerCase().contains(filter.getNome().toLowerCase())))
                    .filter(p -> filter.getAtivo() == null || p.getAtivo().equals(filter.getAtivo()))
                    .collect(Collectors.toList());

            // Criar nova página com resultados filtrados
            pacientesPage = new PageImpl<>(filtered, pageable, filtered.size());
        } else {
            pacientesPage = pacienteRepository.findAll(pageable);
        }

        return pacientesPage.map(this::toResponse);
    }

    /**
     * Busca pacientes por nome
     */
    @Transactional(readOnly = true)
    public List<SimpleResponse> searchByNome(String nome) {
        log.debug("Buscando pacientes por nome: {}", nome);

        if (nome == null || nome.trim().length() < 2) {
            throw new ValidationException("Termo de busca deve ter pelo menos 2 caracteres", "nome");
        }

        List<Paciente> pacientes = pacienteRepository.findByNomeCompletoContainingIgnoreCase(nome);

        return pacientes.stream()
                .map(this::toSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca pacientes por convênio
     */
    @Transactional(readOnly = true)
    public List<SimpleResponse> findByConvenio(String convenio) {
        log.debug("Buscando pacientes por convênio: {}", convenio);

        List<Paciente> pacientes = pacienteRepository.findByConvenio(convenio);

        return pacientes.stream()
                .map(this::toSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtém estatísticas de pacientes
     */
    @Transactional(readOnly = true)
    public StatsResponse getStatistics() {
        log.debug("Obtendo estatísticas de pacientes");

        List<Paciente> todos = pacienteRepository.findAll();

        long total = todos.size();
        long ativos = todos.stream().filter(Paciente::getAtivo).count();

        // Pacientes novos no último mês
        LocalDate umMesAtras = LocalDate.now().minusMonths(1);
        long novosUltimoMes = todos.stream()
                .filter(p -> p.getDataCriacao() != null &&
                        p.getDataCriacao().toLocalDate().isAfter(umMesAtras))
                .count();

        // Convênio mais comum
        String convenioMaisComum = todos.stream()
                .filter(p -> p.getConvenio() != null && !p.getConvenio().isEmpty())
                .collect(Collectors.groupingBy(
                        Paciente::getConvenio,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("Nenhum");

        // Média de idade
        double mediaIdade = todos.stream()
                .filter(p -> p.getDataNascimento() != null)
                .mapToInt(p -> Period.between(p.getDataNascimento(), LocalDate.now()).getYears())
                .average()
                .orElse(0.0);

        return StatsResponse.builder()
                .totalPacientes(total)
                .pacientesAtivos(ativos)
                .pacientesNovosUltimoMes(novosUltimoMes)
                .convenioMaisComum(convenioMaisComum)
                .mediaIdade(mediaIdade)
                .build();
    }

    /**
     * Ativa/desativa um paciente
     */
    @Transactional
    public void toggleStatus(UUID id, boolean ativo) {
        log.info("Alterando status do paciente ID: {} para {}", id, ativo ? "ATIVO" : "INATIVO");

        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forPacienteId(id));

        checkPacienteUpdateAccess(paciente);

        paciente.setAtivo(ativo);
        paciente.setDataAtualizacao(LocalDateTime.now());

        pacienteRepository.save(paciente);
        log.info("Status do paciente ID: {} alterado para {}", id, ativo ? "ATIVO" : "INATIVO");
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void validateCreateRequest(CreateRequest request) {
        if (request.getCpf() == null || request.getCpf().trim().isEmpty()) {
            throw new ValidationException("CPF é obrigatório", "cpf");
        }

        if (!validationUtils.isValidCPF(request.getCpf())) {
            throw ValidationException.cpfInvalido(request.getCpf());
        }

        if (request.getNomeCompleto() == null || request.getNomeCompleto().trim().isEmpty()) {
            throw new ValidationException("Nome completo é obrigatório", "nomeCompleto");
        }

        if (request.getDataNascimento() == null) {
            throw new ValidationException("Data de nascimento é obrigatória", "dataNascimento");
        }

        if (request.getDataNascimento().isAfter(LocalDate.now().minusYears(1))) {
            throw new ValidationException("Data de nascimento inválida", "dataNascimento");
        }

        if (request.getTelefone() != null && !validationUtils.isValidPhone(request.getTelefone())) {
            throw ValidationException.telefoneInvalido(request.getTelefone());
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email é obrigatório", "email");
        }

        if (!validationUtils.isValidEmail(request.getEmail())) {
            throw ValidationException.emailInvalido(request.getEmail());
        }

        if (request.getSenha() == null || request.getSenha().length() < 6) {
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres", "senha");
        }
    }

    private void validateUpdateRequest(UpdateRequest request, Paciente paciente) {
        if (request.getEmail() != null && !validationUtils.isValidEmail(request.getEmail())) {
            throw ValidationException.emailInvalido(request.getEmail());
        }

        if (request.getTelefone() != null && !validationUtils.isValidPhone(request.getTelefone())) {
            throw ValidationException.telefoneInvalido(request.getTelefone());
        }
    }

    private void checkPacienteAccess(Paciente paciente) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return; // Será tratado pelo Spring Security
        }

        Usuario usuario = (Usuario) authentication.getPrincipal();

        // Paciente pode ver seus próprios dados
        if (usuario.getTipo() == Usuario.TipoUsuario.PACIENTE &&
                usuario.getId().equals(paciente.getId())) {
            return;
        }

        // Profissionais e administradores podem ver qualquer paciente
        if (usuario.getTipo() == Usuario.TipoUsuario.PROFISSIONAL_SAUDE ||
                usuario.getTipo() == Usuario.TipoUsuario.ADMINISTRADOR) {
            return;
        }

        throw new SecurityException(
                "Acesso negado a dados do paciente",
                "ACESSO_NEGADO_PACIENTE"
        );
    }

    private void checkPacienteUpdateAccess(Paciente paciente) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Usuário não autenticado", "USUARIO_NAO_AUTENTICADO");
        }

        Usuario usuario = (Usuario) authentication.getPrincipal();

        // Apenas administradores podem atualizar dados de outros pacientes
        if (usuario.getTipo() != Usuario.TipoUsuario.ADMINISTRADOR &&
                !usuario.getId().equals(paciente.getId())) {
            throw new SecurityException(
                    "Apenas administradores podem atualizar dados de outros pacientes",
                    "ACESSO_NEGADO_ATUALIZACAO"
            );
        }
    }

    private void checkPacienteDeleteAccess(Paciente paciente) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Usuário não autenticado", "USUARIO_NAO_AUTENTICADO");
        }

        Usuario usuario = (Usuario) authentication.getPrincipal();

        // Apenas administradores podem deletar pacientes
        if (usuario.getTipo() != Usuario.TipoUsuario.ADMINISTRADOR) {
            throw new SecurityException(
                    "Apenas administradores podem inativar pacientes",
                    "ACESSO_NEGADO_EXCLUSAO"
            );
        }
    }

    private Response toResponse(Paciente paciente) {
        return Response.builder()
                .id(paciente.getId())
                .cpf(paciente.getCpf())
                .nomeCompleto(paciente.getNomeCompleto())
                .dataNascimento(paciente.getDataNascimento())
                .email(paciente.getEmail())
                .telefone(paciente.getTelefone())
                .endereco(paciente.getEndereco())
                .convenio(paciente.getConvenio())
                .contatoEmergencia(paciente.getContatoEmergencia())
                .telefoneEmergencia(paciente.getTelefoneEmergencia())
                .alergias(paciente.getAlergias())
                .medicamentosUso(paciente.getMedicamentosUso())
                .ativo(paciente.getAtivo())
                .idade(paciente.getDataNascimento() != null ?
                        Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears() : null)
                .dataCriacao(paciente.getDataCriacao() != null ?
                        formattingUtils.formatDateTime(paciente.getDataCriacao()) : null)
                .build();
    }

    private SimpleResponse toSimpleResponse(Paciente paciente) {
        return SimpleResponse.builder()
                .id(paciente.getId())
                .nomeCompleto(paciente.getNomeCompleto())
                .cpf(paciente.getCpf())
                .email(paciente.getEmail())
                .telefone(paciente.getTelefone())
                .build();
    }
}