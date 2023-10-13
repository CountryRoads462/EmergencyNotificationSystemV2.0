package com.andrew.ens.template.adapter.out.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplatesRepository extends CrudRepository<Template, Integer> {

    @Modifying
    @Query("UPDATE templates" +
            "SET text = :text" +
            "WHERE template_id = :template_id;")
    void setText(String text, @Param("template_id") int templateId);

    @Modifying
    @Query("INSERT INTO templates(name, owner_id)" +
            "VALUES(:name, :owner_id)" +
            "RETURNING id;")
    int createIncompleteTemplate(String name, @Param("owner_id") long ownerId);
}
