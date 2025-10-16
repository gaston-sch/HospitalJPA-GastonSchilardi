
package org.example.entidades;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // Crea el constructor vacío para JPA.
@AllArgsConstructor(access = AccessLevel.PRIVATE)

@Entity
@Table(name="Historias_Clinicas")
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idHC;
    private final String numeroHistoria;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private final Paciente paciente;

    private final LocalDateTime fechaCreacion;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "historia_diagnosticos", joinColumns = @JoinColumn(name = "historia_id"))
    @Column(name = "diagnostico")
    private final List<String> diagnosticos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "historia_tratamientos", joinColumns = @JoinColumn(name = "historia_id"))
    @Column(name = "tratamiento")
    private final List<String> tratamientos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "historia_alergias", joinColumns = @JoinColumn(name = "historia_id"))
    @Column(name = "alergia")
    private final List<String> alergias = new ArrayList<>();


    private HistoriaClinica(HistoriaClinicaBuilder builder) {
        this.paciente = Objects.requireNonNull(builder.paciente, "El paciente no puede ser nulo");
        this.fechaCreacion = builder.fechaCreacion != null ? builder.fechaCreacion : LocalDateTime.now();
        this.numeroHistoria = generarNumeroHistoria();
    }

    public static class HistoriaClinicaBuilder {
        private Paciente paciente;
        private LocalDateTime fechaCreacion;

        public HistoriaClinicaBuilder paciente(Paciente paciente) {
            this.paciente = paciente;
            return this;
        }

        public HistoriaClinicaBuilder fechaCreacion(LocalDateTime fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public HistoriaClinica build() {
            return new HistoriaClinica(this);
        }
    }

    private String generarNumeroHistoria() {
        return "HC-" + paciente.getDni() + "-" + fechaCreacion.getYear();
    }

    public void agregarDiagnostico(String diagnostico) {
        if (diagnostico != null && !diagnostico.trim().isEmpty()) {
            diagnosticos.add(diagnostico);
        }
    }

    public void agregarTratamiento(String tratamiento) {
        if (tratamiento != null && !tratamiento.trim().isEmpty()) {
            tratamientos.add(tratamiento);
        }
    }

    public void agregarAlergia(String alergia) {
        if (alergia != null && !alergia.trim().isEmpty()) {
            alergias.add(alergia);
        }
    }

    public List<String> getDiagnosticos() {
        return Collections.unmodifiableList(diagnosticos);
    }

    public List<String> getTratamientos() {
        return Collections.unmodifiableList(tratamientos);
    }

    public List<String> getAlergias() {
        return Collections.unmodifiableList(alergias);
    }

}
