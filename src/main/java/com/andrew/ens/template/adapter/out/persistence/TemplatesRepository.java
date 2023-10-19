package com.andrew.ens.template.adapter.out.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplatesRepository extends CrudRepository<Template, Integer> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE templates SET text = :text WHERE id = :templateId", nativeQuery = true)
    void setText(String text, int templateId);

    @Transactional
    @Query(value = "INSERT INTO templates(name, owner_id) " +
            "VALUES(:name, :ownerId) " +
            "RETURNING id", nativeQuery = true)
    int createIncompleteTemplate(String name, long ownerId);

    @Query(value = "SELECT * FROM templates " +
            "WHERE owner_id = :ownerId", nativeQuery = true)
    List<Template> getTemplatesByOwnerId(long ownerId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM templates WHERE id = :templateId", nativeQuery = true)
    void deleteTemplateById(int templateId);

    @Query(value = "SELECT * FROM templates WHERE id = :templateId", nativeQuery = true)
    Optional<Template> getTemplateById(int templateId);

    @Query(value = "SELECT COUNT(*) FROM templates " +
            "WHERE owner_id = :ownerId AND name = :name", nativeQuery = true)
    int getCountOfTemplatesWithNameAndOwnerId(long ownerId, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE templates " +
            "SET name = :newName " +
            "WHERE id = :templateId", nativeQuery = true)
    void setTemplateNameByTemplateId(int templateId, String newName);
}
