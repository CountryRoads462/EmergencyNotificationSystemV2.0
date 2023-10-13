package com.andrew.ens.current_creating.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;

@Entity
@Builder
@Table(name = "current_creating")
public class CurrentCreating {

    @Id
    @Column(name = "user_id")
    private long userId;

    @Column(name = "template_id")
    private int templateId;

    @Column(name = "contact_id")
    private int contactId;
}
