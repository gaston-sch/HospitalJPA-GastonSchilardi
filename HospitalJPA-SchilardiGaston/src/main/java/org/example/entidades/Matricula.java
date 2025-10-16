package org.example.entidades;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // Crea el constructor vacío para JPA.

@Embeddable
public class Matricula {
    private final String numero;

    public Matricula(String numero) {
        this.numero = validarMatricula(numero);
    }

    //guardian del formato de matricula
    private String validarMatricula(String numero) {
        Objects.requireNonNull(numero, "El número de matrícula no puede ser nulo");
        if (!numero.matches("MP-\\d{4,6}")) {
            throw new IllegalArgumentException("Formato de matrícula inválido. Debe ser como MP-12345");
        }
        return numero;
    }
}
