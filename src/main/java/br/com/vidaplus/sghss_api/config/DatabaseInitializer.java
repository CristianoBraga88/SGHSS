package br.com.vidaplus.sghss_api.config;

import br.com.vidaplus.sghss_api.model.Usuario;
import br.com.vidaplus.sghss_api.model.ProfissionalSaude;
import br.com.vidaplus.sghss_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // Verificar se admin já existe
            if (usuarioRepository.findByEmailIgnoreCase("admin@hospital.com").isEmpty()) {
                log.info("🔧 Criando usuário admin...");

                // Criar admin
                ProfissionalSaude admin = new ProfissionalSaude();
                admin.setEmail("admin@hospital.com");
                admin.setSenhaHash(passwordEncoder.encode("admin123"));
                admin.setTipo(Usuario.TipoUsuario.PROFISSIONAL_SAUDE);
                admin.setAtivo(true);
                admin.setDataCriacao(LocalDateTime.now());
                admin.setNomeCompleto("Administrador do Sistema");
                admin.setCrmCoren("ADMIN-000");
                admin.setEspecialidade("ADMINISTRADOR");
                admin.setUnidadeTrabalho("ADMINISTRAÇÃO");
                admin.setDisponivel(true);

                usuarioRepository.save(admin);

                log.info("✅ Usuário admin criado com sucesso!");
                log.info("📧 Email: admin@hospital.com");
                log.info("🔑 Senha: admin123");
            } else {
                log.info("ℹ️ Usuário admin já existe no banco de dados");
            }
        };
    }
}