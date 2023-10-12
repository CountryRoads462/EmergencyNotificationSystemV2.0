package com.andrew.ens.user_status.adapter.out.persistence;

import com.andrew.ens.user_status.domain.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Entity
@Builder
@Getter
@AllArgsConstructor
@Table(name = "user_states")
public class UserStatus {

    @Id
    @NotNull
    @Column(name = "user_id")
    private long userId;

    @NotNull
    private Status status;
}
