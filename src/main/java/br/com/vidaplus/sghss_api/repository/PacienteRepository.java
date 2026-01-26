package br.com.vidaplus.sghss_api.repository;

import br.com.vidaplus.sghss_api.model.Paciente;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    // Busca paciente por CPF
    Optional<Paciente> findByCpf(String cpf);

    boolean existsByEmailIgnoreCase(@Email(message = "Email inválido") String email);

    // Verifica se CPF já existe
    boolean existsByCpf(String cpf);

    // Busca pacientes por nome
    List<Paciente> findByNomeCompletoContainingIgnoreCase(String nome);


    // Busca pacientes por convenio
    List<Paciente> findByConvenio(String convenio);

    // Busca pacientes por data de nascimento
    List<Paciente> findByDataNascimento(LocalDate dataNascimento);

    // Busca pacientes por cidade no endereço
    @Query("SELECT p FROM Paciente p WHERE LOWER(p.endereco) LIKE LOWER(CONCAT('%', :cidade, '%'))")
    List<Paciente> findByCidadeEndereco(@Param("cidade") String cidade);

    // Busca pacientes com alergias específicas
    @Query("SELECT p FROM Paciente p WHERE LOWER(p.alergias) LIKE LOWER(CONCAT('%', :alergia, '%'))")
    List<Paciente> findByAlergia(@Param("alergia") String alergia);

    // Busca pacientes ativos
    @Query("SELECT p FROM Paciente p WHERE p.ativo = true")
    List<Paciente> findAtivos();

    // Contagem de pacientes por convenio
    @Query("SELECT p.convenio, COUNT(p) FROM Paciente p GROUP BY p.convenio")
    List<Object[]> countByConvenio();

    // Busca pacientes sem consulta nos últimos X meses
    @Query("SELECT p FROM Paciente p WHERE p.id NOT IN " +
            "(SELECT c.paciente.id FROM Consulta c WHERE c.dataHora >= :dataLimite)")
    List<Paciente> findSemConsultaDesde(@Param("dataLimite") LocalDate dataLimite);
}