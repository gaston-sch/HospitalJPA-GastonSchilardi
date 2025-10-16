package org.example.entidades;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.NoArgsConstructor;


@SuperBuilder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force=true)

@Entity
@Table(name="Medicos")
public class Medico extends Persona {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idM;

    @Embedded
    private final Matricula matricula; // Objeto final, inyectado por el builder
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final EspecialidadMedica especialidadMedica;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
    private Departamento departamento; // <--- Sin 'final' para inyección JPA

    @OneToMany(
            mappedBy = "medico",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Cita> citas;  // se inicializa en el constructor protegido


    protected Medico(MedicoBuilder<?, ?> builder) {
        super(builder);
        // CRÍTICO: Recibe el objeto Matricula (ya validado) directamente del builder
        this.matricula = Objects.requireNonNull(builder.matricula, "La matrícula no puede ser nula");
        this.especialidadMedica = Objects.requireNonNull(builder.especialidadMedica, "La especialidad no puede ser nula");
        this.citas = new ArrayList<>(); // Inicialización crítica de colección
    }


    // CRÍTICO: El Builder ahora maneja el objeto Matricula
    public static abstract class MedicoBuilder<C extends Medico, B extends MedicoBuilder<C, B>> extends PersonaBuilder<C, B> {
        private Matricula matricula; // <--- CAMPO AHORA ES TIPO OBJETO MATRICULA
        private EspecialidadMedica especialidadMedica;

        public B matricula(Matricula matricula) { // <--- MÉTODO AHORA ACEPTA OBJETO MATRICULA
            this.matricula = matricula;
            return self();
        }

        public B especialidadMedica(EspecialidadMedica especialidadMedica) {
            this.especialidadMedica = especialidadMedica;
            return self();
        }
    }

    // Método setDepartamento para bidireccionalidad
    public void setDepartamento(Departamento departamento) {
        if (this.departamento != departamento) {
            // Elimina la referencia de la colección anterior (si existe)
            if (this.departamento != null) {
                // Nota: Usar un getter interno como getInternalMedicos() si el getter público es inmutable.
                // Si solo usas getMedicos(), el IDE te lo marcará como error.
                this.departamento.getMedicos().remove(this);
            }
            this.departamento = departamento;
            // Establece la nueva referencia bidireccional (si no es null)
            if (departamento != null) {
                // Nota: Asumiendo que Departamento tiene un método agregarMedico(Medico) que hace la bidireccionalidad
                departamento.agregarMedico(this);
            }
        }
    }

    // Métodos de colección
    public void addCita (Cita cita){
        this.citas.add(cita);
    }

    public List<Cita> getCitas () {
        return Collections.unmodifiableList(new ArrayList<>(citas));
    }
}