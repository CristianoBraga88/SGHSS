package br.com.vidaplus.sghss_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class PasswordEncoderConfig {

    /**
     * Configura o encoder de senhas com força 12 (recomendado para produção)
     * BCrypt é um algoritmo de hash seguro e lento, ideal para senhas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength 12 = 2^12 rounds = 4096 iterações
        // Quanto maior o número, mais seguro mas mais lento
        // 12 é um bom equilíbrio entre segurança e performance
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Bean utilitário para encode/decode de senhas fora do contexto Spring Security
     */
    @Bean
    public PasswordUtils passwordUtils(PasswordEncoder passwordEncoder) {
        return new PasswordUtils(passwordEncoder);
    }

    /**
     * Classe utilitária para operações com senhas
     */
    public static class PasswordUtils {
        private final PasswordEncoder passwordEncoder;

        public PasswordUtils(PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        /**
         * Codifica uma senha em texto plano
         */
        public String encode(String rawPassword) {
            return passwordEncoder.encode(rawPassword);
        }

        /**
         * Verifica se uma senha em texto plano corresponde a um hash
         */
        public boolean matches(String rawPassword, String encodedPassword) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }

        /**
         * Verifica se um hash precisa ser atualizado (força do algoritmo mudou)
         */
        public boolean needsUpgrade(String encodedPassword) {
            return passwordEncoder.upgradeEncoding(encodedPassword);
        }

        /**
         * Gera uma senha aleatória segura
         */
        public String generateSecurePassword(int length) {
            String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String lower = "abcdefghijklmnopqrstuvwxyz";
            String digits = "0123456789";
            String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";

            String allChars = upper + lower + digits + special;
            java.security.SecureRandom random = new java.security.SecureRandom();

            StringBuilder password = new StringBuilder(length);

            // Garante pelo menos um de cada tipo
            password.append(upper.charAt(random.nextInt(upper.length())));
            password.append(lower.charAt(random.nextInt(lower.length())));
            password.append(digits.charAt(random.nextInt(digits.length())));
            password.append(special.charAt(random.nextInt(special.length())));

            // Preenche o restante
            for (int i = 4; i < length; i++) {
                password.append(allChars.charAt(random.nextInt(allChars.length())));
            }

            // Embaralha a senha
            char[] passwordArray = password.toString().toCharArray();
            for (int i = passwordArray.length - 1; i > 0; i--) {
                int index = random.nextInt(i + 1);
                char temp = passwordArray[index];
                passwordArray[index] = passwordArray[i];
                passwordArray[i] = temp;
            }

            return new String(passwordArray);
        }

        /**
         * Valida a força de uma senha
         */
        public PasswordStrength validatePasswordStrength(String password) {
            int score = 0;
            List<String> suggestions = new java.util.ArrayList<>();

            // Verifica comprimento
            if (password.length() >= 12) score += 2;
            else if (password.length() >= 8) score += 1;
            else suggestions.add("A senha deve ter pelo menos 8 caracteres");

            // Verifica letras maiúsculas
            if (password.matches(".*[A-Z].*")) score += 1;
            else suggestions.add("Adicione pelo menos uma letra maiúscula");

            // Verifica letras minúsculas
            if (password.matches(".*[a-z].*")) score += 1;
            else suggestions.add("Adicione pelo menos uma letra minúscula");

            // Verifica números
            if (password.matches(".*\\d.*")) score += 1;
            else suggestions.add("Adicione pelo menos um número");

            // Verifica caracteres especiais
            if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score += 1;
            else suggestions.add("Adicione pelo menos um caractere especial");

            // Verifica sequências comuns
            if (password.matches(".*(123|abc|qwert|asdf).*")) {
                score -= 1;
                suggestions.add("Evite sequências comuns");
            }

            // Determina força
            String strength;
            if (score >= 5) strength = "FORTE";
            else if (score >= 3) strength = "MODERADA";
            else strength = "FRACA";

            return new PasswordStrength(strength, score, suggestions);
        }

        /**
         * Classe para representar a força de uma senha
         */
        public static class PasswordStrength {
            private final String strength;
            private final int score;
            private final List<String> suggestions;

            public PasswordStrength(String strength, int score, List<String> suggestions) {
                this.strength = strength;
                this.score = score;
                this.suggestions = suggestions;
            }

            public String getStrength() { return strength; }
            public int getScore() { return score; }
            public List<String> getSuggestions() { return suggestions; }
            public boolean isStrong() { return "FORTE".equals(strength); }
        }
    }
}
