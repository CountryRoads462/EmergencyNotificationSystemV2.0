package com.andrew.ens.template.adapter.out.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Entity
@Getter
@Table(name = "templates")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private int id;

    @NotNull
    @Column(name = "owner_id")
    private long ownerId;

    @NotNull
    private String name;

    private String text;
}
