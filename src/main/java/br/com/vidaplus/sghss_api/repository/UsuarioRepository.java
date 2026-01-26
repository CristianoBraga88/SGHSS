package br.com.vidaplus.sghss_api.repository;

import br.com.vidaplus.sghss_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    // Busca usuário por email
    Optional<Usuario> findByEmailIgnoreCase(String email);

    // Verifica se email já existe
    boolean existsByEmailIgnoreCase(String email);

    // Busca usuário por email e tipo
    Optional<Usuario> findByEmailIgnoreCaseAndTipo(String email, Usuario.TipoUsuario tipo);

    // Busca usuários ativos por tipo
    @Query("SELECT u FROM Usuario u WHERE u.tipo = :tipo AND u.ativo = true")
    java.util.List<Usuario> findAtivosByTipo(@Param("tipo") Usuario.TipoUsuario tipo);

    // Busca usuários inativos
    @Query("SELECT u FROM Usuario u WHERE u.ativo = false")
    java.util.List<Usuario> findInativos();

    // Ativa/desativa usuário
    @Query("UPDATE Usuario u SET u.ativo = :ativo WHERE u.id = :id")
    void updateAtivoStatus(@Param("id") UUID id, @Param("ativo") boolean ativo);
}
