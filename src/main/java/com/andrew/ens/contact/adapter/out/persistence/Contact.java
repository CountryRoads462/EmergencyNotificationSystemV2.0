package com.andrew.ens.contact.adapter.out.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Entity
@Getter
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private int id;

    @NotNull
    private String name;

    @Email
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "template_id")
    private int templateId;
}
