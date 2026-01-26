package br.com.vidaplus.sghss_api.repository;

import br.com.vidaplus.sghss_api.model.Consulta;
import br.com.vidaplus.sghss_api.model.Paciente;
import br.com.vidaplus.sghss_api.model.ProfissionalSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    // Busca consultas por paciente
    List<Consulta> findByPaciente(Paciente paciente);

    // Busca consultas por profissional
    List<Consulta> findByProfissional(ProfissionalSaude profissional);

    // Busca consultas por status
    List<Consulta> findByStatus(Consulta.StatusConsulta status);

    // Busca consultas por tipo
    List<Consulta> findByTipo(Consulta.TipoConsulta tipo);

    // Busca consultas por data/hora exata
    List<Consulta> findByDataHora(LocalDateTime dataHora);

    // Busca consultas entre datas
    @Query("SELECT c FROM Consulta c WHERE c.dataHora BETWEEN :inicio AND :fim")
    List<Consulta> findByDataHoraBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    // Busca consultas agendadas para hoje
    @Query("SELECT c FROM Consulta c WHERE DATE(c.dataHora) = CURRENT_DATE")
    List<Consulta> findConsultasHoje();

    // Busca consultas por paciente e período
    @Query("SELECT c FROM Consulta c WHERE c.paciente = :paciente " +
            "AND c.dataHora BETWEEN :inicio AND :fim")
    List<Consulta> findByPacienteAndPeriodo(
            @Param("paciente") Paciente paciente,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    // Busca consultas por profissional e período
    @Query("SELECT c FROM Consulta c WHERE c.profissional = :profissional " +
            "AND c.dataHora BETWEEN :inicio AND :fim")
    List<Consulta> findByProfissionalAndPeriodo(
            @Param("profissional") ProfissionalSaude profissional,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    // Verifica conflito de horário para profissional
    @Query("SELECT COUNT(c) > 0 FROM Consulta c WHERE c.profissional = :profissional " +
            "AND c.status NOT IN ('CANCELADA', 'FALTA') " +
            "AND c.dataHora = :dataHora")
    boolean existsConflitoHorarioProfissional(
            @Param("profissional") ProfissionalSaude profissional,
            @Param("dataHora") LocalDateTime dataHora
    );

    // Verifica conflito de horário para paciente
    @Query("SELECT COUNT(c) > 0 FROM Consulta c WHERE c.paciente = :paciente " +
            "AND c.status NOT IN ('CANCELADA', 'FALTA') " +
            "AND c.dataHora = :dataHora")
    boolean existsConflitoHorarioPaciente(
            @Param("paciente") Paciente paciente,
            @Param("dataHora") LocalDateTime dataHora
    );

    // Conta consultas por status
    @Query("SELECT c.status, COUNT(c) FROM Consulta c GROUP BY c.status")
    List<Object[]> countByStatus();

    // Busca consultas canceladas com motivo
    @Query("SELECT c FROM Consulta c WHERE c.status = 'CANCELADA' " +
            "AND c.motivoCancelamento IS NOT NULL")
    List<Consulta> findCanceladasComMotivo();

    // Agenda disponíveis para telemedicina
    @Query("SELECT c FROM Consulta c WHERE c.tipo = 'TELEMEDICINA' " +
            "AND c.status = 'AGENDADA' " +
            "AND c.dataHora >= CURRENT_TIMESTAMP")
    List<Consulta> findTelemedicinaAgendadas();

    // Busca consultas sem prontuário associado
    @Query("SELECT c FROM Consulta c WHERE c.prontuario IS NULL " +
            "AND c.status = 'CONCLUIDA'")
    List<Consulta> findConcluidasSemProntuario();

    // Estatísticas mensais
    @Query("SELECT MONTH(c.dataHora) as mes, COUNT(c) as total " +
            "FROM Consulta c WHERE YEAR(c.dataHora) = :ano " +
            "GROUP BY MONTH(c.dataHora)")
    List<Object[]> getEstatisticasMensais(@Param("ano") int ano);
}
