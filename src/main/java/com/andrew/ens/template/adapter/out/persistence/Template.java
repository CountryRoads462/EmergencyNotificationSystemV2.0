package com.andrew.ens.template.adapter.out.persistence;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Entity
@Builder
@Table(name = "templates")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    @Column(name = "owner_id")
    private long ownerId;

    @NotNull
    private String name;

    private String text;

    private List<Integer> contactsIds;
}
