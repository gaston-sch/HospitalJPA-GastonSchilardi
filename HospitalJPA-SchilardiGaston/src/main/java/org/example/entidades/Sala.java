
package org.example.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Getter
@ToString(exclude = {"citas"})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // Crea el constructor vacío para JPA.
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name="Salas")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idS;
    @Setter(AccessLevel.NONE)
    private final String numero;
    private final String tipo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;
    @OneToMany(mappedBy = "sala")
    private List<Cita> citas;

    private Sala(SalaBuilder builder) {
        this.numero = validarString(builder.numero, "El número de sala no puede ser nulo ni vacío");
        this.tipo = validarString(builder.tipo, "El tipo de sala no puede ser nulo ni vacío");
        this.departamento = Objects.requireNonNull(builder.departamento, "El departamento no puede ser nulo");
        this.citas = new ArrayList<>();
    }

    public static class SalaBuilder {
        private String numero;
        private String tipo;
        private Departamento departamento;

        public SalaBuilder numero(String numero) {
            this.numero = numero;
            return this;
        }

        public SalaBuilder tipo(String tipo) {
            this.tipo = tipo;
            return this;
        }

        public SalaBuilder departamento(Departamento departamento) {
            this.departamento = departamento;
            return this;
        }

        public Sala build() {
            return new Sala(this);
        }
    }

    public void addCita(Cita cita) {
        this.citas.add(cita);
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(new ArrayList<>(citas));
    }

    private String validarString(String valor, String mensajeError) {
        Objects.requireNonNull(valor, mensajeError);
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

}
