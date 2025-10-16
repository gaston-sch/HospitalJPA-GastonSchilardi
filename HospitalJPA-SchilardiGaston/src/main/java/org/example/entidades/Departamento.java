
package org.example.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // Crea el constructor vacío para JPA.
@AllArgsConstructor(access = AccessLevel.PRIVATE)

@Entity
@Table(name="Departamentos")
public class Departamento {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long iddto;
    @Setter(AccessLevel.NONE)
    private final String nombre;
    @Enumerated(EnumType.STRING)
    private final EspecialidadMedica especialidadMedica;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
    @OneToMany(
            mappedBy = "departamento", // La relación ya está mapeada por el atributo "departamento" en la clase Medico.
            cascade = CascadeType.ALL
    )
    private final List<Medico> medicos = new ArrayList<>();
    @OneToMany(
            mappedBy = "departamento", // La relación ya está mapeada por el atributo "departamento" en la clase Sala.
            cascade = CascadeType.ALL
    )
    private final List<Sala> salas = new ArrayList<>();

    private Departamento(DepartamentoBuilder builder) {
        this.nombre = validarString(builder.nombre, "El nombre del departamento no puede ser nulo ni vacío");
        this.especialidadMedica = Objects.requireNonNull(builder.especialidadMedica, "La especialidad no puede ser nula");
    }

    public static class DepartamentoBuilder {
        private String nombre;
        private EspecialidadMedica especialidadMedica;

        public DepartamentoBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public DepartamentoBuilder especialidad(EspecialidadMedica especialidadMedica) {
            this.especialidadMedica = especialidadMedica;
            return this;
        }

        public Departamento build() {
            return new Departamento(this);
        }
    }

    public void setHospital(Hospital hospital) {
        if (this.hospital != hospital) {
            if (this.hospital != null) {
                this.hospital.getInternalDepartamentos().remove(this);
            }
            this.hospital = hospital;
            if (hospital != null) {
                hospital.getInternalDepartamentos().add(this);
            }
        }
    }

    public void agregarMedico(Medico medico) {
        if (medico != null && !medicos.contains(medico)) {
            medicos.add(medico);
            medico.setDepartamento(this);
        }
    }

    public Sala crearSala(String numero, String tipo) {
        Sala sala = Sala.builder()
                .numero(numero)
                .tipo(tipo)
                .departamento(this)
                .build();
        salas.add(sala);
        return sala;
    }

    public List<Medico> getMedicos() {
        return Collections.unmodifiableList(medicos);
    }

    public List<Sala> getSalas() {
        return Collections.unmodifiableList(salas);
    }


    private String validarString(String valor, String mensajeError) {
        Objects.requireNonNull(valor, mensajeError);
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }
}
