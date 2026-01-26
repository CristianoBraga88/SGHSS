package br.com.vidaplus.sghss_api.repository;

import br.com.vidaplus.sghss_api.model.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, UUID> {

    // Busca logs por módulo
    List<LogAuditoria> findByModulo(String modulo);

    // Busca logs por ação
    List<LogAuditoria> findByAcao(String acao);

    // Busca logs por usuário
    List<LogAuditoria> findByUsuarioId(UUID usuarioId);

    // Busca logs por email do usuário
    List<LogAuditoria> findByUsuarioEmail(String usuarioEmail);

    // Busca logs por status da operação
    List<LogAuditoria> findByStatusOperacao(String statusOperacao);

    // Busca logs por período
    @Query("SELECT l FROM LogAuditoria l WHERE l.dataHora BETWEEN :inicio AND :fim")
    List<LogAuditoria> findByPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    // Busca logs de erros status
    @Query("SELECT l FROM LogAuditoria l WHERE UPPER(l.statusOperacao) LIKE '%ERROR%' " +
            "OR UPPER(l.statusOperacao) LIKE '%FALHA%' " +
            "OR UPPER(l.statusOperacao) LIKE '%FAIL%'")
    List<LogAuditoria> findLogsErro();

    // Busca logs de ações críticas login, exclusão
    @Query("SELECT l FROM LogAuditoria l WHERE l.acao IN ('LOGIN', 'LOGOUT', 'EXCLUIR', 'AUTORIZAR', 'NEGAR')")
    List<LogAuditoria> findLogsCriticos();

    // Busca logs por IP de origem
    List<LogAuditoria> findByIpOrigem(String ipOrigem);

    // Busca logs por recurso afetado
    @Query("SELECT l FROM LogAuditoria l WHERE l.recursoAfetado = :recurso")
    List<LogAuditoria> findByRecursoAfetado(@Param("recurso") String recurso);

    // Busca logs com mensagem de erro
    @Query("SELECT l FROM LogAuditoria l WHERE l.mensagemErro IS NOT NULL")
    List<LogAuditoria> findComMensagemErro();

    // Estatísticas de logs por módulo
    @Query("SELECT l.modulo, COUNT(l) FROM LogAuditoria l GROUP BY l.modulo")
    List<Object[]> countByModulo();

    // Estatísticas de logs por hora do dia
    @Query("SELECT HOUR(l.dataHora), COUNT(l) FROM LogAuditoria l " +
            "GROUP BY HOUR(l.dataHora) ORDER BY HOUR(l.dataHora)")
    List<Object[]> countByHora();

    // Logs de acesso por IP suspeito
    @Query("SELECT l.ipOrigem, COUNT(l) as tentativas FROM LogAuditoria l " +
            "WHERE l.acao = 'LOGIN' AND l.statusOperacao = 'FALHA' " +
            "AND l.dataHora >= :dataLimite " +
            "GROUP BY l.ipOrigem HAVING COUNT(l) > 5")
    List<Object[]> findIpsSuspeitos(@Param("dataLimite") LocalDateTime dataLimite);

    // Logs para relatório LGPD dados sensíveis
    @Query("SELECT l FROM LogAuditoria l WHERE " +
            "l.recursoAfetado IN ('PACIENTES', 'PRONTUARIOS', 'CONSULTAS') " +
            "AND l.acao IN ('CRIAR', 'ATUALIZAR', 'EXCLUIR', 'CONSULTAR')")
    List<LogAuditoria> findLogsLGPD();

    // Limpa logs antigos
    @Query("DELETE FROM LogAuditoria l WHERE l.dataHora < :dataLimite")
    void deleteLogsAntigos(@Param("dataLimite") LocalDateTime dataLimite);
}
