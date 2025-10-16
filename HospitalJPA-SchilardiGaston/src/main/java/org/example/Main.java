package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import org.example.entidades.*;
import org.example.servicio.CitaManager;
import org.example.servicio.CitaException; // 👈 IMPORTANTE

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

public class Main {

    private static final boolean RESET_DB = false;

    public static void main(String[] args) {
        System.out.println("--- SISTEMA DE GESTION HOSPITALARIA (EDICION DEMO) ---");

        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            emf = Persistence.createEntityManagerFactory("hospital-persistence-unit");
            em  = emf.createEntityManager();

            em.getTransaction().begin();

            // Opción: reset de base de datos si está habilitado
            if (RESET_DB) {
                em.createNativeQuery("DROP ALL OBJECTS").executeUpdate(); // Limpia todo en H2
                em.clear();
            }

            // Verificar si ya hay hospital creado
            Long hayHospital = em.createQuery(
                            "SELECT COUNT(h) FROM Hospital h WHERE h.nombre = :n", Long.class)
                    .setParameter("n", "Hospital San Martín Central")
                    .getSingleResult();

            if (hayHospital == 0) {
                seedData(em);
                System.out.println("\n==> BASE INICIAL CREADA (Hospital/Deptos/Salas/Médicos/Pacientes)");
            } else {
                System.out.println("\n==> Datos base ya existentes. No se reinsertan.");
            }

            // Programar citas
            Long citasFuturas = em.createQuery(
                            "SELECT COUNT(c) FROM Cita c WHERE c.fechaHora > :now", Long.class)
                    .setParameter("now", LocalDateTime.now())
                    .getSingleResult();

            if (citasFuturas == 0) {
                programarCitas(em);
            } else {
                System.out.println("==> Ya existen citas futuras. No se reprograman.");
            }

            // Consultas de ejemplo
            System.out.println("\n-- Médicos por especialidad (DERMATOLOGIA) --");
            TypedQuery<Medico> qMedDerma = em.createQuery(
                    "SELECT DISTINCT m FROM Medico m WHERE m.especialidadMedica = :esp ORDER BY m.apellido",
                    Medico.class);
            qMedDerma.setParameter("esp", EspecialidadMedica.DERMATOLOGIA);
            qMedDerma.setMaxResults(10);
            for (Medico med : qMedDerma.getResultList()) {
                System.out.println("   * " + med.getApellido().toUpperCase() + ", " + med.getNombre()
                        + " | Mat: " + med.getMatricula().getNumero());
            }

            System.out.println("\n-- Próximas citas (ordenadas por fecha asc) --");
            TypedQuery<Cita> qCitasProx = em.createQuery(
                    "SELECT c FROM Cita c WHERE c.fechaHora > :now ORDER BY c.fechaHora ASC",
                    Cita.class);
            qCitasProx.setParameter("now", LocalDateTime.now());
            List<Cita> proximas = qCitasProx.getResultList();
            for (Cita c : proximas) {
                System.out.println("   • " + c.getFechaHora() + " | Paciente: " + c.getPaciente().getApellido()
                        + " | " + c.getEstado());
            }

            if (!proximas.isEmpty()) {
                Cita aActualizar = proximas.get(0);
                System.out.println("\nEstado anterior cita #" + aActualizar.getId() + ": " + aActualizar.getEstado());
                aActualizar.setEstado(EstadoCita.COMPLETADA);
                System.out.println("Nuevo estado: " + aActualizar.getEstado());
            }

            // Estadísticas
            Long totalPacientes = em.createQuery("SELECT COUNT(p) FROM Paciente p", Long.class).getSingleResult();
            Long totalCitasComp = em.createQuery(
                            "SELECT COUNT(c) FROM Cita c WHERE c.estado = :st", Long.class)
                    .setParameter("st", EstadoCita.COMPLETADA)
                    .getSingleResult();
            Long totalSalas = em.createQuery("SELECT COUNT(s) FROM Sala s", Long.class).getSingleResult();

            System.out.println("\n=== RESUMEN DEL SISTEMA ===");
            System.out.println("Pacientes registrados      : " + totalPacientes);
            System.out.println("Citas COMPLETADAS          : " + totalCitasComp);
            System.out.println("Salas registradas          : " + totalSalas);

            em.getTransaction().commit();
            System.out.println("\n>>> SISTEMA EJECUTADO EXITOSAMENTE (DEMO ALTERNATIVA) <<<");

        } catch (Exception ex) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                System.out.println("\n!!! ROLLBACK EJECUTADO (ERROR) !!!");
            }
            ex.printStackTrace();
        } finally {
            if (em != null) em.close();
            if (emf != null) emf.close();
        }
    }

    // ========================= HELPERS =========================

    private static void seedData(EntityManager em) {
        Hospital hospital = Hospital.builder()
                .nombre("Hospital San Martín Central")
                .direccion("Ituzaingó 1450, Ciudad")
                .telefono("+54 261 555-0101")
                .build();

        Departamento neuro = Departamento.builder()
                .nombre("Neurología")
                .especialidadMedica(EspecialidadMedica.NEUROLOGIA)
                .hospital(hospital)
                .build();

        Departamento cirugia = Departamento.builder()
                .nombre("Cirugía General")
                .especialidadMedica(EspecialidadMedica.CIRUGIA_GENERAL)
                .hospital(hospital)
                .build();

        Departamento derma = Departamento.builder()
                .nombre("Dermatología")
                .especialidadMedica(EspecialidadMedica.DERMATOLOGIA)
                .hospital(hospital)
                .build();

        hospital.agregarDepartamento(neuro);
        hospital.agregarDepartamento(cirugia);
        hospital.agregarDepartamento(derma);

        Sala sNeu_201 = Sala.builder().numero("S-201").tipo("Consulta Neurológica").departamento(neuro).build();
        Sala sCir_305 = Sala.builder().numero("S-305").tipo("Quirofano 3").departamento(cirugia).build();
        Sala sDer_112 = Sala.builder().numero("S-112").tipo("DermatoBox A").departamento(derma).build();

        Medico m1 = Medico.builder()
                .nombre("Andrés").apellido("Álvarez").dni("30888999")
                .fechaNacimiento(LocalDate.of(1984, Month.APRIL, 19))
                .tipoSangre(TipoSangre.O_POSITIVO)
                .especialidadMedica(EspecialidadMedica.NEUROLOGIA)
                .matricula(new Matricula("MP-45678"))
                .departamento(neuro)
                .build();

        Medico m2 = Medico.builder()
                .nombre("Lucía").apellido("Rojas").dni("29555111")
                .fechaNacimiento(LocalDate.of(1987, Month.JANUARY, 7))
                .tipoSangre(TipoSangre.A_POSITIVO)
                .especialidadMedica(EspecialidadMedica.CIRUGIA_GENERAL)
                .matricula(new Matricula("MP-334455"))
                .departamento(cirugia)
                .build();

        Medico m3 = Medico.builder()
                .nombre("Julián").apellido("Pereyra").dni("31222444")
                .fechaNacimiento(LocalDate.of(1990, Month.SEPTEMBER, 2))
                .tipoSangre(TipoSangre.B_NEGATIVO)
                .especialidadMedica(EspecialidadMedica.DERMATOLOGIA)
                .matricula(new Matricula("MP-778899"))
                .departamento(derma)
                .build();

        neuro.agregarMedico(m1);
        cirugia.agregarMedico(m2);
        derma.agregarMedico(m3);

        Paciente p1 = Paciente.builder()
                .nombre("María").apellido("López").dni("40222111")
                .fechaNacimiento(LocalDate.of(1999, Month.FEBRUARY, 11))
                .tipoSangre(TipoSangre.AB_POSITIVO)
                .telefono("+54 261 600-1000")
                .direccion("Colón 520, Godoy Cruz")
                .hospital(hospital)
                .build();

        Paciente p2 = Paciente.builder()
                .nombre("Sebastián").apellido("Benítez").dni("38999123")
                .fechaNacimiento(LocalDate.of(1995, Month.JUNE, 23))
                .tipoSangre(TipoSangre.O_NEGATIVO)
                .telefono("+54 261 600-2000")
                .direccion("Sarmiento 220, Guaymallén")
                .hospital(hospital)
                .build();

        Paciente p3 = Paciente.builder()
                .nombre("Camila").apellido("Herrera").dni("37777123")
                .fechaNacimiento(LocalDate.of(2001, Month.NOVEMBER, 5))
                .tipoSangre(TipoSangre.B_POSITIVO)
                .telefono("+54 261 600-3000")
                .direccion("Belgrano 900, Las Heras")
                .hospital(hospital)
                .build();

        hospital.agregarPaciente(p1);
        hospital.agregarPaciente(p2);
        hospital.agregarPaciente(p3);

        em.persist(hospital);
    }

    private static void programarCitas(EntityManager em) {
        Medico m1 = em.createQuery("SELECT m FROM Medico m WHERE m.matricula.numero = :n", Medico.class)
                .setParameter("n", "MP-45678").getSingleResult();
        Medico m2 = em.createQuery("SELECT m FROM Medico m WHERE m.matricula.numero = :n", Medico.class)
                .setParameter("n", "MP-334455").getSingleResult();
        Medico m3 = em.createQuery("SELECT m FROM Medico m WHERE m.matricula.numero = :n", Medico.class)
                .setParameter("n", "MP-778899").getSingleResult();

        Paciente p1 = em.createQuery("SELECT p FROM Paciente p WHERE p.dni = :dni", Paciente.class)
                .setParameter("dni", "40222111").getSingleResult();
        Paciente p2 = em.createQuery("SELECT p FROM Paciente p WHERE p.dni = :dni", Paciente.class)
                .setParameter("dni", "38999123").getSingleResult();
        Paciente p3 = em.createQuery("SELECT p FROM Paciente p WHERE p.dni = :dni", Paciente.class)
                .setParameter("dni", "37777123").getSingleResult();

        Sala sNeu_201 = em.createQuery("SELECT s FROM Sala s WHERE s.numero = :n", Sala.class)
                .setParameter("n", "S-201").getSingleResult();
        Sala sCir_305 = em.createQuery("SELECT s FROM Sala s WHERE s.numero = :n", Sala.class)
                .setParameter("n", "S-305").getSingleResult();
        Sala sDer_112 = em.createQuery("SELECT s FROM Sala s WHERE s.numero = :n", Sala.class)
                .setParameter("n", "S-112").getSingleResult();

        LocalDateTime f1 = LocalDateTime.of(2025, 11, 10, 9, 30);
        LocalDateTime f2 = LocalDateTime.of(2025, 12, 2, 14, 0);
        LocalDateTime f3 = LocalDateTime.of(2025, 12, 15, 11, 15);

        CitaManager cm = new CitaManager();

        try { // 👇 Manejo seguro de excepciones
            cm.programarCita(p1, m1, sNeu_201, f1, new BigDecimal("18000.00"));
            cm.programarCita(p2, m2, sCir_305, f2, new BigDecimal("45000.00"));
            cm.programarCita(p3, m3, sDer_112, f3, new BigDecimal("12500.00"));
            System.out.println("==> CITAS PROGRAMADAS (3 nuevas)");
        } catch (CitaException e) {
            System.out.println("!!! ERROR al programar citas: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
