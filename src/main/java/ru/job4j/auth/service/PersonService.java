package ru.job4j.auth.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.auth.domain.Person;
import ru.job4j.auth.repository.PersonRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;

    private BCryptPasswordEncoder encoder;

    public Optional<Person> save(Person person) {
        Optional<Person> saved = Optional.empty();
        try {
            saved = Optional.of(personRepository.save(person));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return saved;
    }

    public boolean deleteById(int id) {
        boolean flag = findPersonById(id).isPresent();
        if (flag) {
            personRepository.deleteById(id);
        }
        return flag;
    }

    public Optional<Person> update(Person person) throws InvocationTargetException, IllegalAccessException {
        var currentOpt = findPersonById(person.getId());
        Person current;
        boolean flag = currentOpt.isPresent();
        if (flag) {
            current = currentOpt.get();
            var methods = current.getClass().getDeclaredMethods();
            var namePerMethod = new HashMap<String, Method>();
            for (var method: methods) {
                var name = method.getName();
                if (name.startsWith("get") || name.startsWith("set")) {
                    namePerMethod.put(name, method);
                }
            }
            for (var name : namePerMethod.keySet()) {
                if (name.startsWith("get")) {
                    var getMethod = namePerMethod.get(name);
                    var setMethod = namePerMethod.get(name.replace("get", "set"));
                    if (setMethod == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Impossible invoke set method from object : " + current + ", Check set and get pairs.");
                    }
                    var newValue = getMethod.invoke(person);
                    if (newValue != null) {
                        setMethod.invoke(current, newValue);
                    }
                }
            }
            if (person.getPassword() != null) {
                current.setPassword(encoder.encode(current.getPassword()));
            }
            personRepository.save(current);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found and cannot be modified");
        }
        return Optional.of(current);
    }

    public Optional<Person> findPersonById(int id) {
        return personRepository.findById(id);
    }

    public Optional<Person> findPersonByName(String name) {
        return personRepository.findByLogin(name);
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }
}
