package com.andrew.ens.user.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Entity
@Builder
@Table(name = "users")
public class EnsUser {

    @Id
    @NotNull
    @Column(unique = true)
    private long id;

    @NotNull
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Size(min = 5, max = 32)
    private String username;

    @Column(name = "chosen_template_id")
    private int chosenTemplateId;
}
