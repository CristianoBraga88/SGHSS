package br.com.vidaplus.sghss_api.repository;

import br.com.vidaplus.sghss_api.model.Paciente;
import br.com.vidaplus.sghss_api.model.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, UUID> {

    // Busca prontuários por paciente
    List<Prontuario> findByPaciente(Paciente paciente);

    // Busca prontuário por consulta
    Prontuario findByConsultaId(UUID consultaId);

    // Busca prontuários por profissional criador
    List<Prontuario> findByProfissionalCriadorId(UUID profissionalId);

    // Busca prontuários entre datas
    @Query("SELECT p FROM Prontuario p WHERE p.dataCriacao BETWEEN :inicio AND :fim")
    List<Prontuario> findByPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    // Busca prontuários com diagnóstico específico
    @Query("SELECT p FROM Prontuario p WHERE LOWER(p.diagnostico) LIKE LOWER(CONCAT('%', :diagnostico, '%'))")
    List<Prontuario> findByDiagnosticoContaining(@Param("diagnostico") String diagnostico);

    // Busca prontuários por medicamento prescrito
    @Query("SELECT p FROM Prontuario p WHERE LOWER(p.prescricaoMedica) LIKE LOWER(CONCAT('%', :medicamento, '%'))")
    List<Prontuario> findByMedicamentoPrescrito(@Param("medicamento") String medicamento);

    // Busca prontuários com exames solicitados
    @Query("SELECT p FROM Prontuario p WHERE p.examesSolicitados IS NOT NULL " +
            "AND p.examesSolicitados != ''")
    List<Prontuario> findComExamesSolicitados();

    // Último prontuário do paciente
    @Query("SELECT p FROM Prontuario p WHERE p.paciente = :paciente " +
            "ORDER BY p.dataCriacao DESC LIMIT 1")
    Prontuario findUltimoByPaciente(@Param("paciente") Paciente paciente);

    // Prontuários com pressão arterial alta
    @Query("SELECT p FROM Prontuario p WHERE p.pressaoArterial LIKE '14%' " +
            "OR p.pressaoArterial LIKE '15%' " +
            "OR p.pressaoArterial LIKE '16%' " +
            "OR p.pressaoArterial LIKE '17%' " +
            "OR p.pressaoArterial LIKE '18%' " +
            "OR p.pressaoArterial LIKE '19%' " +
            "OR p.pressaoArterial LIKE '20%'")
    List<Prontuario> findComPressaoAlta();

    // Prontuários com febre
    @Query("SELECT p FROM Prontuario p WHERE p.temperaturaC > 37.5")
    List<Prontuario> findComFebre();

    // Histórico completo do paciente ordenado
    @Query("SELECT p FROM Prontuario p WHERE p.paciente = :paciente " +
            "ORDER BY p.dataCriacao DESC")
    List<Prontuario> findHistoricoCompleto(@Param("paciente") Paciente paciente);

    // Contagem de prontuários por profissional
    @Query("SELECT p.profissionalCriador.id, COUNT(p) FROM Prontuario p " +
            "GROUP BY p.profissionalCriador.id")
    List<Object[]> countByProfissional();

    // Busca prontuários atualizados recentemente
    @Query("SELECT p FROM Prontuario p WHERE p.dataAtualizacao >= :dataLimite")
    List<Prontuario> findAtualizadosDesde(@Param("dataLimite") LocalDateTime dataLimite);

    // Prontuários com assinatura digital
    @Query("SELECT p FROM Prontuario p WHERE p.assinaturaDigital IS NOT NULL")
    List<Prontuario> findComAssinaturaDigital();
}