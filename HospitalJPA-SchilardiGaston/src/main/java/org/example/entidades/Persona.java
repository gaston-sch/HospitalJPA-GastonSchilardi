package org.example.entidades;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Objects;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@SuperBuilder
@Getter
@ToString

@NoArgsConstructor(access = AccessLevel.PROTECTED, force=true)
@MappedSuperclass
public abstract class Persona {


    protected final String nombre;
    protected final String apellido;
    protected final String dni;
    protected final LocalDate fechaNacimiento;
    @Enumerated(EnumType.STRING)
    protected final TipoSangre tipoSangre;


    protected Persona(PersonaBuilder<?, ?> builder) {
        this.nombre = validarString(builder.nombre, "El nombre no puede ser nulo ni vacío");
        this.apellido = validarString(builder.apellido, "El apellido no puede ser nulo ni vacío");
        this.dni = validarDni(builder.dni);
        this.fechaNacimiento = Objects.requireNonNull(builder.fechaNacimiento, "La fecha de nacimiento no puede ser nula");
        this.tipoSangre = Objects.requireNonNull(builder.tipoSangre, "El tipo de sangre no puede ser nulo");
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public int getEdad() {
        return LocalDate.now().getYear() - fechaNacimiento.getYear();
    }

    //Guardian.     nombre no puede estar vacio
    private String validarString(String valor, String mensajeError) {

    Objects.requireNonNull(valor, mensajeError);
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }
    //Guardian.      Dni debe tener 7 u 8 digitos si o si
    private String validarDni(String dni) {
        Objects.requireNonNull(dni, "El DNI no puede ser nulo");
        if (!dni.matches("\\d{7,8}")) {
            throw new IllegalArgumentException("El DNI debe tener 7 u 8 dígitos");
        }
        return dni;
    }


}
