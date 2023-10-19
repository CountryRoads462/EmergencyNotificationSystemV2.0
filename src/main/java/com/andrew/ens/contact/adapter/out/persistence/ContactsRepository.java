package com.andrew.ens.contact.adapter.out.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ContactsRepository extends CrudRepository<Contact, Integer> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE contacts " +
            "SET email = :email " +
            "WHERE id = :contactId", nativeQuery = true)
    void setContactEmail(int contactId, String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE contacts " +
            "SET template_id = :templateId " +
            "WHERE id = :contactId", nativeQuery = true)
    void setContactTemplateId(int contactId, int templateId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE contacts " +
            "SET phone_number = :phoneNumber " +
            "WHERE id= :contactId", nativeQuery = true)
    void setContactPhoneNumber(int contactId, String phoneNumber);

    @Transactional
    @Query(value = "INSERT INTO contacts(name) " +
            "VALUES(:name) " +
            "RETURNING id", nativeQuery = true)
    int createIncompleteContact(String name);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM contacts WHERE id > 0", nativeQuery = true)
    void deleteAllContacts();

    @Query(value = "SELECT * FROM contacts WHERE template_id = :templateId", nativeQuery = true)
    List<Contact> getAllContactsByTemplateId(int templateId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM contacts WHERE id = :contactId", nativeQuery = true)
    void deleteContactById(int contactId);

    @Query(value = "SELECT COUNT(*) FROM contacts " +
            "WHERE name = :name AND template_id = :templateId", nativeQuery = true)
    int getNumberOfContactsWithNameAndTemplateId(String name, int templateId);

    @Query(value = "SELECT COUNT(*) FROM contacts " +
            "WHERE email = :email AND template_id = :templateId", nativeQuery = true)
    int getNumberOfContactsWithEmailAndTemplateId(String email, int templateId);

    @Query(value = "SELECT COUNT(*) FROM contacts " +
            "WHERE phone_number = :phoneNumber AND template_id = :templateId", nativeQuery = true)
    int getNumberOfContactsWithPhoneNumberAndTemplateId(String phoneNumber, int templateId);


}
