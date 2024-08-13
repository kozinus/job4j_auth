package ru.job4j.auth.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.auth.domain.Person;
import ru.job4j.auth.repository.PersonRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;

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

    public boolean update(Person person) {
        boolean flag = findPersonById(person.getId()).isPresent();
        if (flag) {
            personRepository.save(person);
        }
        return flag;
    }

    public Optional<Person> findPersonById(int id) {
        return personRepository.findById(id);
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }
}
