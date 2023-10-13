package com.andrew.ens.current_creating.adapter.out.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrentCreatingRepository extends CrudRepository<CurrentCreating, Long> {

    @Query(value = "SELECT template_id FROM current_creating WHERE user_id = :user_id", nativeQuery = true)
    int getTemplateIdByUserId(@Param("user_id") long userId);

    @Query(value = "SELECT contact_id FROM current_creating WHERE user_id = :user_id", nativeQuery = true)
    int getContactIdByUserId(@Param("user_id") long userId);

    @Modifying
    @Query(value = "UPDATE current_creating" +
            "SET contact_id = :contact_id" +
            "WHERE user_id = :user_id;", nativeQuery = true)
    void setContactIdByUserId(@Param("user_id") long userId, @Param("contact_id") int contactId);
}
