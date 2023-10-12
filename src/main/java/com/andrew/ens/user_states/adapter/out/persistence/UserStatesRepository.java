package com.andrew.ens.user_states.adapter.out.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatesRepository extends CrudRepository<UserStatus, Long> {
}
