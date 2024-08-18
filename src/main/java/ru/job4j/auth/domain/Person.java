package ru.job4j.auth.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.job4j.auth.controller.Operation;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull(message = "Id must be non null", groups = {
            Operation.OnUpdate.class, Operation.OnDelete.class
    })
    private Integer id;

    @JsonProperty("username")
    @NotBlank(message = "Username must be not empty", groups = {
            Operation.OnCreate.class, Operation.OnUpdate.class
    })
    private String login;

    @Min(value = 7, message = "Year must be more than 7", groups = {
            Operation.OnCreate.class, Operation.OnUpdate.class
    })
    private String password;
}