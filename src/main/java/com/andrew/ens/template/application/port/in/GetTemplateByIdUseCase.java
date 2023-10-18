package com.andrew.ens.template.application.port.in;

import com.andrew.ens.template.adapter.out.persistence.Template;

import java.util.Optional;

public interface GetTemplateByIdUseCase {
    Optional<Template> getTemplateById(int templateId);
}
