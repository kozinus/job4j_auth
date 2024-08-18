package ru.job4j.auth.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.auth.domain.Person;
import ru.job4j.auth.service.PersonService;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class PersonController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonController.class.getSimpleName());

    private final ObjectMapper objectMapper;

    private final PersonService persons;

    private BCryptPasswordEncoder encoder;

    @GetMapping("/all")
    public List<Person> findAll() {
        return this.persons.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> findById(@PathVariable int id) {
        return persons.findPersonById(id)
                .map(x -> {
                    var body = new TreeMap<>() {{
                        put("Id", x.getId());
                        put("Username", encoder.encode(x.getLogin()));
                        put("Password", x.getPassword());
                    }}.toString();
                    return ResponseEntity.status(HttpStatus.OK)
                            .header("EncodedUsernameProfile", "Job4j")
                            .contentType(MediaType.TEXT_PLAIN)
                            .contentLength(body.length())
                            .body(body);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User is not found.")
                );
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        var password = person.getPassword();
        var username = person.getLogin();
        if (username == null || password == null) {
            throw new NullPointerException("Username and password mustn't be empty");
        }
        if (persons.findPersonByName(username).isPresent()) {
            throw new IllegalArgumentException("Invalid username. Username must be unique");
        }
        if (username.length() < 1) {
            throw new IllegalArgumentException("Invalid username. Username must contain at least one character");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Invalid password. Password length must be more than 7 characters.");
        }
        person.setPassword(encoder.encode(person.getPassword()));
        return persons.save(person)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build()
        );
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        if (persons.update(person)) {
            return ResponseEntity.ok().build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found and cannot be modified");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        if (persons.deleteById(id)) {
            return ResponseEntity.ok().build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found and cannot be deleted");
        }
    }

    @ExceptionHandler(value = { IllegalArgumentException.class })
    public void exceptionHandler(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("message", e.getMessage());
            put("type", e.getClass());
        }}));
        LOGGER.error(e.getLocalizedMessage());
    }
}