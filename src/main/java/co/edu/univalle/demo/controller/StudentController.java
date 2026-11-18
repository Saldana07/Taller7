package co.edu.univalle.demo.controller;

import co.edu.univalle.demo.model.Student;
import co.edu.univalle.demo.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone el API en /api/demo/student (el prefijo /api/demo es el context-path).
 */
@RestController
@RequestMapping("/student")
@CrossOrigin
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** GET /api/demo/student/all */
    @GetMapping("/all")
    public List<Student> findAll() {
        return studentService.findAll();
    }

    /** GET /api/demo/student/id?id=1 */
    @GetMapping("/id")
    public ResponseEntity<Student> findById(@RequestParam Long id) {
        return studentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/demo/student/name?name=Manuel */
    @GetMapping("/name")
    public List<Student> findByName(@RequestParam String name) {
        return studentService.findByName(name);
    }

    /** GET /api/demo/student/age?age=30 */
    @GetMapping("/age")
    public List<Student> findByAge(@RequestParam Integer age) {
        return studentService.findByAge(age);
    }

    /** POST /api/demo/student/save */
    @PostMapping("/save")
    public ResponseEntity<Student> save(@RequestBody Student student) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.save(student));
    }

    /** PUT /api/demo/student/update  (el body debe incluir el id) */
    @PutMapping("/update")
    public ResponseEntity<Student> update(@RequestBody Student student) {
        return studentService.update(student)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/demo/student/delete?id=1 */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        return studentService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
