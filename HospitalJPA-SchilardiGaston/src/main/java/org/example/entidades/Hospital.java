
package org.example.entidades;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@ToString(exclude = {"departamentos", "pacientes"})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // Crea el constructor vacío para JPA.
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name="Hospitales")
public class Hospital {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idH;
    @Setter(AccessLevel.NONE)
    private final String nombre;
    @Setter(AccessLevel.NONE)
    private final String direccion;
    private final String telefono;
    @OneToMany(
            mappedBy = "hospital", // La relación ya está mapeada por el atributo
            cascade = CascadeType.ALL, // Si se borra un Hospital, se borran sus departamentos.
            orphanRemoval = true // Si quitas un Departamento de esta lista, se borra de la BD.
    )
    private final List<Departamento> departamentos = new ArrayList<>();
    @OneToMany(
            mappedBy = "hospital", // La relación ya está mapeada por el atributo.
            cascade = CascadeType.ALL, // Si se borra un Hospital, se borran sus citas.
            orphanRemoval = true // Si quitas un paciente de esta lista, se borra de la BD.
    )
    private final List<Paciente> pacientes = new ArrayList<>();

    public void agregarDepartamento(Departamento departamento) {
        if (departamento != null && !departamentos.contains(departamento)) {
            departamentos.add(departamento);
            departamento.setHospital(this);
        }
    }

    public void agregarPaciente(Paciente paciente) {
        if (paciente != null && !pacientes.contains(paciente)) {
            pacientes.add(paciente);
            paciente.setHospital(this);
        }
    }

    public List<Departamento> getDepartamentos() {
        return Collections.unmodifiableList(departamentos);
    }

    public List<Paciente> getPacientes() {
        return Collections.unmodifiableList(pacientes);
    }

    List<Departamento> getInternalDepartamentos() {
        return departamentos;
    }

    List<Paciente> getInternalPacientes() {
        return pacientes;
    }


    private String validarString(String valor, String mensajeError) {
        Objects.requireNonNull(valor, mensajeError);
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }


}
