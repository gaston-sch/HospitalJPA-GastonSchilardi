# Sistema de Gestión Hospitalaria (JPA + Hibernate + H2)

Proyecto demo académico para la cátedra **Desarrollo de Software**. Implementa un dominio hospitalario con **JPA/Hibernate** y **H2**: Hospital, Departamentos, Salas, Médicos, Pacientes, Historias Clínicas y Citas, más un servicio de dominio `CitaManager` con validaciones y excepciones chequeadas (`CitaException`).

## Requisitos

- **Java 17 o 21** (verificado con Gradle 8.14)
- **Gradle Wrapper** (incluido: `gradlew` / `gradlew.bat`)
- (Opcional) IntelliJ IDEA

## Cómo ejecutar

```bash
# Limpia y ejecuta la app
.\gradlew clean run
