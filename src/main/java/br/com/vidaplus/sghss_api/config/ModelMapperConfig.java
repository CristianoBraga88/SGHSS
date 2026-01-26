package br.com.vidaplus.sghss_api.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Configuração para mapeamento STRICT somente se os nomes baterem exatamente
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true) // Ignora campos nulos
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        configureCustomMappings(modelMapper);

        return modelMapper;
    }

    private void configureCustomMappings(ModelMapper modelMapper) {}

    @Bean
    public ModelMapperUtils modelMapperUtils(ModelMapper modelMapper) {
        return new ModelMapperUtils(modelMapper);
    }

    /**
     * Classe utilitária ModelMapper
     */
    public static class ModelMapperUtils {
        private final ModelMapper modelMapper;

        public ModelMapperUtils(ModelMapper modelMapper) {
            this.modelMapper = modelMapper;
        }

        /**
         * Converte uma entity para DTO
         */
        public <S, D> D toDto(S source, Class<D> destinationType) {
            if (source == null) {
                return null;
            }
            return modelMapper.map(source, destinationType);
        }

        /**
         * Converte um DTO para entity
         */
        public <S, D> D toEntity(S source, Class<D> destinationType) {
            if (source == null) {
                return null;
            }
            return modelMapper.map(source, destinationType);
        }

        /**
         * Atualiza uma entity com os dados de um DTO
         */
        public <S, D> void updateEntity(S source, D destination) {
            if (source != null && destination != null) {
                modelMapper.map(source, destination);
            }
        }

        /**
         * Converte uma lista de entities para DTOs
         */
        public <S, D> java.util.List<D> toDtoList(java.util.List<S> sourceList, Class<D> destinationType) {
            if (sourceList == null) {
                return new java.util.ArrayList<>();
            }
            return sourceList.stream()
                    .map(source -> toDto(source, destinationType))
                    .collect(java.util.stream.Collectors.toList());
        }
    }
}
