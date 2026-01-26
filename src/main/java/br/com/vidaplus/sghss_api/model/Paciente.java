package br.com.vidaplus.sghss_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pacientes")
public class Paciente extends Usuario {

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(length = 20)
    private String telefone;

    @Column(length = 200)
    private String endereco;

    @Column(length = 50)
    private String convenio;

    @Column(name = "contato_emergencia", length = 20)
    private String contatoEmergencia;

    @Column(name = "telefone_emergencia", length = 20)
    private String telefoneEmergencia;

    @Column(name = "alergias", columnDefinition = "TEXT")
    private String alergias;

    @Column(name = "medicamentos_uso", columnDefinition = "TEXT")
    private String medicamentosUso;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Consulta> consultas = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prontuario> prontuarios = new ArrayList<>();
}