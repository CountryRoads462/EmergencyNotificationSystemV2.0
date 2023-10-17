package com.andrew.ens.template.application.port.in;

import com.andrew.ens.template.adapter.out.persistence.Template;

import java.util.List;

public interface GetTemplatesByOwnerIdUseCase {
    List<Template> getTemplatesByOwnerId(long ownerId);
}
