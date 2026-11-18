package co.edu.univalle.demo.service;

import co.edu.univalle.demo.model.Student;
import co.edu.univalle.demo.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Lógica de negocio: usa los métodos del repositorio que implementa JPA. */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    // Inyección de dependencias por constructor (no necesita @Autowired)
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    public List<Student> findByName(String name) {
        return studentRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Student> findByAge(Integer age) {
        return studentRepository.findByAge(age);
    }

    public Student save(Student student) {
        student.setId(null); // el id lo genera la base de datos
        return studentRepository.save(student);
    }

    public Optional<Student> update(Student student) {
        if (student.getId() == null || !studentRepository.existsById(student.getId())) {
            return Optional.empty();
        }
        return Optional.of(studentRepository.save(student));
    }

    public boolean delete(Long id) {
        if (!studentRepository.existsById(id)) {
            return false;
        }
        studentRepository.deleteById(id);
        return true;
    }
}
