package com.andrew.ens.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends CrudRepository<ENSUser, Long> {

    @Modifying
    @Query(value = "INSERT INTO users(first_name, last_name, id, username)" +
            "VALUES(:firstName, :lastName, :id, :username)" +
            "ON CONFLICT DO NOTHING;", nativeQuery = true)
    void createUser(String firstName, String lastName, long id, String username);

    boolean existsById(long id);

    @Query(value = "SELECT number_of_templates FROM users WHERE id = :id;", nativeQuery = true)
    int getNumberOfTemplatesById(long id);
}