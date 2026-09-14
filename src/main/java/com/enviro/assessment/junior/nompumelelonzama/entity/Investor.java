package com.enviro.assessment.junior.nompumelelonzama.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "investors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Investor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private int age;
    private String email;

    @OneToOne(mappedBy = "investor", cascade = CascadeType.ALL)
    @JsonIgnore
    private Portfolio portfolio;
}