package br.com.vidaplus.sghss_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profissionais_saude")
public class ProfissionalSaude extends Usuario {

    @Column(name = "crm_coren", nullable = false, unique = true, length = 20)
    private String crmCoren;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(nullable = false, length = 50)
    private String especialidade;

    @Column(nullable = false)
    private Boolean disponivel = true;

    @Column(name = "unidade_trabalho", nullable = false, length = 100)
    private String unidadeTrabalho;

    @Column(name = "telefone_profissional", length = 20)
    private String telefoneProfissional;

    @Column(name = "carga_horaria_semanal")
    private Integer cargaHorariaSemanal;

    @Column(name = "valor_consulta")
    private Double valorConsulta;  // REMOVIDO: precision = 10, scale = 2

    @Column(columnDefinition = "TEXT")
    private String formacao;

    @Column(columnDefinition = "TEXT")
    private String experiencia;

    @OneToMany(mappedBy = "profissional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Consulta> consultas = new ArrayList<>();
}