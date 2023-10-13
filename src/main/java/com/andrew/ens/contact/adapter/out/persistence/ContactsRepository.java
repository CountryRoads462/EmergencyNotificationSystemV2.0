package com.andrew.ens.contact.adapter.out.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactsRepository extends CrudRepository<Contact, Integer> {

    @Modifying
    @Query(value = "UPDATE contacts" +
            "SET email = :email" +
            "WHERE contact_id = :contact_id", nativeQuery = true)
    void setContactEmail(@Param("contact_id") int contactId, String email);

    @Modifying
    @Query(value = "UPDATE contacts" +
            "SET phone_number = :phone_number" +
            "WHERE contact_id = :contact_id", nativeQuery = true)
    void setContactPhoneNumber(@Param("contact_id") int contactId, @Param("phone_number") String phoneNumber);

    @Modifying
    @Query(value = "INSERT INTO contacts(name)" +
            "VALUES(:name)" +
            "RETURNING id", nativeQuery = true)
    int createIncompleteContact(String name);
}
