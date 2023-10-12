package com.andrew.ens.user_status.adapter.out.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatesRepository extends CrudRepository<UserStatus, Long> {
}
