package com.andrew.ens.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UsersRepository extends CrudRepository<ENSUser, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO users(first_name, last_name, id, username, number_of_templates)" +
            "VALUES(:firstName, :lastName, :id, :username, 0)" +
            "ON CONFLICT DO NOTHING", nativeQuery = true)
    void createUser(String firstName, String lastName, long id, String username);

    boolean existsById(long id);

    @Query(value = "SELECT number_of_templates FROM users WHERE id = :id", nativeQuery = true)
    Integer getNumberOfTemplatesById(long id);

    @Query(value = "SELECT chosen_template_id FROM users WHERE user_id = :user_id", nativeQuery = true)
    Integer userHasChosenTemplate(@Param("user_id") long userId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users" +
            "SET chosen_template_id = :template_id" +
            "WHERE user_id = :user_id", nativeQuery = true)
    void setChosenTemplate(@Param("user_id") long userId, @Param("template_id") int templateId);
}