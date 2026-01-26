package br.com.vidaplus.sghss_api.repository;

import br.com.vidaplus.sghss_api.model.ProfissionalSaude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, UUID> {

    // Busca por CRM/COREN
    Optional<ProfissionalSaude> findByCrmCoren(String crmCoren);

    // Verifica se CRM/COREN já existe
    boolean existsByCrmCoren(String crmCoren);

    // Busca por especialidade
    List<ProfissionalSaude> findByEspecialidade(String especialidade);

    // Busca por especialidade
    List<ProfissionalSaude> findByEspecialidadeIgnoreCase(String especialidade);

    // Busca profissionais disponíveis
    List<ProfissionalSaude> findByDisponivelTrue();

    // Busca profissionais por unidade de trabalho
    List<ProfissionalSaude> findByUnidadeTrabalho(String unidadeTrabalho);

    // Busca profissionais com carga horária maior
    List<ProfissionalSaude> findByCargaHorariaSemanalGreaterThanEqual(Integer cargaHoraria);

    // Busca profissionais por especialidade e disponibilidade
    @Query("SELECT p FROM ProfissionalSaude p WHERE p.especialidade = :especialidade AND p.disponivel = true")
    List<ProfissionalSaude> findDisponiveisByEspecialidade(@Param("especialidade") String especialidade);

    // Busca profissionais com formação específica
    @Query("SELECT p FROM ProfissionalSaude p WHERE LOWER(p.formacao) LIKE LOWER(CONCAT('%', :formacao, '%'))")
    List<ProfissionalSaude> findByFormacaoContaining(@Param("formacao") String formacao);

    // Contagem de profissionais por especialidade
    @Query("SELECT p.especialidade, COUNT(p) FROM ProfissionalSaude p GROUP BY p.especialidade")
    List<Object[]> countByEspecialidade();

    // Busca profissionais com valor consulta dentro de faixa
    @Query("SELECT p FROM ProfissionalSaude p WHERE p.valorConsulta BETWEEN :min AND :max")
    List<ProfissionalSaude> findByValorConsultaBetween(@Param("min") Double min, @Param("max") Double max);

    // Busca profissionais ativos
    @Query("SELECT p FROM ProfissionalSaude p WHERE p.ativo = true")
    List<ProfissionalSaude> findAtivos();

    boolean existsByEmailIgnoreCase(@NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email);
}
