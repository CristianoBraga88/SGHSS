package br.com.vidaplus.sghss_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AppConfig {

    /**
     * Configuração do ObjectMapper para JSON
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());

        // Configurações
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }

    /**
     * Executor assíncrono para operações em background
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

    /**
     * Bean utilitário para validações
     */
    @Bean
    public ValidationUtils validationUtils() {
        return new ValidationUtils();
    }

    /**
     * Bean utilitário para formatação
     */
    @Bean
    public FormattingUtils formattingUtils() {
        return new FormattingUtils();
    }

    public static class ValidationUtils {

        public boolean isValidEmail(String email) {
            if (email == null) return false;
            return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
        }

        public boolean isValidCPF(String cpf) {
            if (cpf == null) return false;
            cpf = cpf.replaceAll("\\D", "");
            return cpf.matches("\\d{11}");
        }

        public boolean isValidPhone(String phone) {
            if (phone == null) return false;
            phone = phone.replaceAll("\\D", "");
            return phone.matches("\\d{10,11}");
        }

        public boolean isValidDateRange(java.time.LocalDate start, java.time.LocalDate end) {
            if (start == null || end == null) return false;
            return !start.isAfter(end);
        }
    }

    public static class FormattingUtils {

        public String formatCPF(String cpf) {
            if (cpf == null || cpf.length() != 11) return cpf;
            return cpf.substring(0, 3) + "." +
                    cpf.substring(3, 6) + "." +
                    cpf.substring(6, 9) + "-" +
                    cpf.substring(9);
        }

        public String formatPhone(String phone) {
            if (phone == null) return phone;
            phone = phone.replaceAll("\\D", "");

            if (phone.length() == 10) {
                return "(" + phone.substring(0, 2) + ") " +
                        phone.substring(2, 6) + "-" +
                        phone.substring(6);
            } else if (phone.length() == 11) {
                return "(" + phone.substring(0, 2) + ") " +
                        phone.substring(2, 7) + "-" +
                        phone.substring(7);
            }
            return phone;
        }

        public String formatDate(java.time.LocalDate date) {
            if (date == null) return "";
            return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        public String formatDateTime(java.time.LocalDateTime dateTime) {
            if (dateTime == null) return "";
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        }
    }
}
