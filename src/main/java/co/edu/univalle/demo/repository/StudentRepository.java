package co.edu.univalle.demo.repository;

import co.edu.univalle.demo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JpaRepository entrega los métodos genéricos del CRUD (save, findById, findAll, deleteById...).
 * Aquí se agregan consultas personalizadas.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Consulta derivada del nombre del método
    List<Student> findByNameContainingIgnoreCase(String name);

    // Consulta personalizada con JPQL
    @Query("SELECT s FROM Student s WHERE s.age = :age")
    List<Student> findByAge(@Param("age") Integer age);
}
