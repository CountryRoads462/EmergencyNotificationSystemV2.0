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
            "WHERE owner_id = :owner_id;")
    void setText(String text, @Param("owner_id") long ownerId);
}
