package com.andrew.ens.template.adapter.out.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TemplatesRepository extends CrudRepository<Template, Integer> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE templates" +
            "SET text = :text" +
            "WHERE id = :template_id", nativeQuery = true)
    void setText(String text, @Param("template_id") int templateId);

    @Transactional
    @Query(value = "INSERT INTO templates(name, owner_id)" +
            "VALUES(:name, :owner_id)" +
            "RETURNING id", nativeQuery = true)
    int createIncompleteTemplate(String name, @Param("owner_id") long ownerId);

    @Query(value = "SELECT text FROM templates WHERE template_id = :template_id", nativeQuery = true)
    String getTemplateText(@Param("template_id") int templateId);

    @Query(value = "SELECT * FROM templates " +
            "WHERE owner_id = :owner_id", nativeQuery = true)
    List<Template> getTemplatesByOwnerId(@Param("owner_id") long ownerId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM templates WHERE id = :template_id", nativeQuery = true)
    void deleteTemplateById(@Param("template_id") int templateId);


}
