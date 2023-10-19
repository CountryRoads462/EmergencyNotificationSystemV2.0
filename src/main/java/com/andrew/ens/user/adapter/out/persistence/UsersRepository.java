package com.andrew.ens.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UsersRepository extends CrudRepository<ENSUser, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO users(first_name, last_name, id, username) " +
            "VALUES(:firstName, :lastName, :id, :username) " +
            "ON CONFLICT DO NOTHING", nativeQuery = true)
    void createUser(String firstName, String lastName, long id, String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users " +
            "SET chosen_template_id = :templateId " +
            "WHERE id = :userId", nativeQuery = true)
    void setChosenTemplate(long userId, int templateId);

    @Query(value = "SELECT chosen_template_id FROM users WHERE id = :userId", nativeQuery = true)
    Optional<Integer> getChosenTemplateId(long userId);
}